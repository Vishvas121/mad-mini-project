import { NextResponse } from 'next/server';
import Groq from 'groq-sdk';
import { auth } from '@clerk/nextjs/server';
import { createAdminClient } from '@/lib/supabase-server';

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

export async function POST(req: Request) {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    const { topic, difficulty, previousScore, questionCount = 5 } = await req.json();

    if (!topic) return NextResponse.json({ error: 'Topic is required' }, { status: 400 });

    // Adapt difficulty based on previous performance
    let adaptedDifficulty = difficulty || 'Intermediate';
    if (previousScore !== undefined) {
      if (previousScore >= 80) {
        adaptedDifficulty = difficulty === 'Beginner' ? 'Intermediate' : 'Advanced';
      } else if (previousScore < 40) {
        adaptedDifficulty = difficulty === 'Advanced' ? 'Intermediate' : 'Beginner';
      }
    }

    const prompt = `Generate exactly ${questionCount} multiple-choice quiz questions about "${topic}" at ${adaptedDifficulty} level for a software engineering student.

Return ONLY a valid JSON array. No markdown, no backticks, no explanation — just the raw JSON array:
[
  {
    "id": 1,
    "question": "Question text here?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correct": "Option A",
    "explanation": "Brief explanation of why this answer is correct",
    "difficulty": "${adaptedDifficulty}",
    "topic": "${topic}"
  }
]

Rules:
- Generate exactly ${questionCount} questions
- Each question must have exactly 4 options
- The "correct" field must exactly match one of the 4 options (verbatim)
- Questions must be practical and relevant to software engineering
- Vary question types: conceptual, code-based, best-practice
- Make questions progressively harder`;

    const completion = await groq.chat.completions.create({
      messages: [
        {
          role: 'system',
          content:
            'You are an expert software engineering educator. Generate precise, accurate quiz questions. Return ONLY valid JSON array — no markdown fences, no backticks, no explanation text.',
        },
        { role: 'user', content: prompt },
      ],
      model: 'llama-3.3-70b-versatile',
      temperature: 0.5,
      max_tokens: 3000,
    });

    const raw = completion.choices[0]?.message?.content?.trim() || '';

    // Strip markdown fences
    const cleaned = raw
      .replace(/^```json\s*/i, '')
      .replace(/^```\s*/i, '')
      .replace(/\s*```$/i, '')
      .trim();

    const jsonMatch = cleaned.match(/\[[\s\S]*\]/);
    if (!jsonMatch) {
      console.error('No JSON array in quiz response:', raw.slice(0, 300));
      return NextResponse.json({ error: 'AI returned invalid response. Please try again.' }, { status: 500 });
    }

    let questions;
    try {
      questions = JSON.parse(jsonMatch[0]);
    } catch {
      return NextResponse.json({ error: 'Failed to parse quiz questions. Please try again.' }, { status: 500 });
    }

    if (!Array.isArray(questions) || questions.length === 0) {
      return NextResponse.json({ error: 'No questions generated. Please try again.' }, { status: 500 });
    }

    // Try to create quiz session in DB — fail silently if tables don't exist
    let sessionId: string | null = null;
    try {
      const supabase = await createAdminClient();
      const { data: session } = await supabase
        .from('quiz_sessions')
        .insert({
          user_id: userId,
          topic,
          difficulty: adaptedDifficulty,
          questions_total: questions.length,
        })
        .select()
        .single();
      sessionId = session?.id || null;
    } catch (dbErr) {
      console.warn('Quiz session DB save skipped (run migration):', (dbErr as any)?.message);
      // Generate a temporary session ID so the quiz still works
      sessionId = `temp_${Date.now()}`;
    }

    return NextResponse.json({ sessionId, questions, adaptedDifficulty, topic });
  } catch (error: any) {
    console.error('Adaptive quiz error:', error);
    return NextResponse.json({ error: error?.message || 'Failed to generate quiz' }, { status: 500 });
  }
}

// Submit quiz results
export async function PUT(req: Request) {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    const { sessionId, answers, timeTaken } = await req.json();

    const correct = answers.filter((a: any) => a.is_correct).length;
    const total = answers.length;
    const scorePercentage = total > 0 ? Math.round((correct / total) * 100) : 0;
    const xpEarned = Math.round(scorePercentage * 0.5) + correct * 10;

    // Try DB operations — all fail silently if tables don't exist
    try {
      const supabase = await createAdminClient();

      // Only update real session IDs (not temp ones)
      if (sessionId && !sessionId.startsWith('temp_')) {
        await supabase
          .from('quiz_sessions')
          .update({
            questions_correct: correct,
            score_percentage: scorePercentage,
            xp_earned: xpEarned,
            time_taken_seconds: timeTaken,
            completed: true,
            completed_at: new Date().toISOString(),
          })
          .eq('id', sessionId)
          .eq('user_id', userId);

        // Save individual answers
        const answerRows = answers.map((a: any) => ({
          session_id: sessionId,
          user_id: userId,
          question_text: a.question,
          selected_answer: a.selected,
          correct_answer: a.correct,
          is_correct: a.is_correct,
          time_taken_seconds: a.timeTaken || 0,
          difficulty: a.difficulty,
        }));
        await supabase.from('quiz_answers').insert(answerRows);
      }

      // Update skill score
      if (answers[0]?.topic) {
        const topic = answers[0].topic;
        const { data: existing } = await supabase
          .from('skill_scores')
          .select('*')
          .eq('user_id', userId)
          .eq('skill_name', topic)
          .single();

        if (existing) {
          const newAttempts = existing.questions_attempted + total;
          const newCorrect = existing.questions_correct + correct;
          await supabase
            .from('skill_scores')
            .update({
              score: Math.round((newCorrect / newAttempts) * 100),
              questions_attempted: newAttempts,
              questions_correct: newCorrect,
              last_assessed_at: new Date().toISOString(),
            })
            .eq('id', existing.id);
        } else {
          await supabase.from('skill_scores').insert({
            user_id: userId,
            skill_name: topic,
            category: 'General',
            score: scorePercentage,
            questions_attempted: total,
            questions_correct: correct,
          });
        }
      }

      // Update daily activity
      const today = new Date().toISOString().split('T')[0];
      const { data: activity } = await supabase
        .from('daily_activity')
        .select('*')
        .eq('user_id', userId)
        .eq('activity_date', today)
        .single();

      if (activity) {
        await supabase
          .from('daily_activity')
          .update({
            quizzes_taken: activity.quizzes_taken + 1,
            xp_earned: activity.xp_earned + xpEarned,
            minutes_studied: activity.minutes_studied + Math.ceil((timeTaken || 0) / 60),
          })
          .eq('id', activity.id);
      } else {
        await supabase.from('daily_activity').insert({
          user_id: userId,
          activity_date: today,
          quizzes_taken: 1,
          xp_earned: xpEarned,
          minutes_studied: Math.ceil((timeTaken || 0) / 60),
        });
      }

      // Ensure learning profile exists
      await supabase
        .from('learning_profiles')
        .upsert({ user_id: userId }, { onConflict: 'user_id', ignoreDuplicates: true });
    } catch (dbErr) {
      console.warn('Quiz submit DB ops skipped (run migration):', (dbErr as any)?.message);
    }

    return NextResponse.json({ scorePercentage, xpEarned, correct, total });
  } catch (error: any) {
    console.error('Quiz submit error:', error);
    return NextResponse.json({ error: 'Failed to submit quiz' }, { status: 500 });
  }
}
