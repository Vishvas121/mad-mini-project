import { NextResponse } from 'next/server';
import Groq from 'groq-sdk';
import { auth } from '@clerk/nextjs/server';
import { createAdminClient } from '@/lib/supabase-server';

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

export async function POST(req: Request) {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    const { goal, currentLevel, targetRole, timeAvailableWeekly, skills } = await req.json();

    if (!goal || !targetRole) {
      return NextResponse.json({ error: 'Goal and target role are required' }, { status: 400 });
    }

    const prompt = `Create a detailed personalized learning path for a software engineering student.

Details:
- Goal: ${goal}
- Current Level: ${currentLevel}
- Target Role: ${targetRole}
- Hours available per week: ${timeAvailableWeekly}
- Current skills: ${skills?.length > 0 ? skills.join(', ') : 'None specified'}

Return ONLY a valid JSON object. No markdown fences, no explanation, just raw JSON:
{
  "title": "Path title here",
  "description": "Two sentence description of this learning path.",
  "estimatedWeeks": 12,
  "phases": [
    {
      "phase": 1,
      "title": "Phase 1 title",
      "duration": "2 weeks",
      "objective": "What the learner will achieve in this phase",
      "topics": [
        {
          "name": "Topic name",
          "type": "course",
          "estimatedHours": 5,
          "description": "Brief description of what to learn",
          "resources": ["Resource name 1", "Resource name 2"]
        }
      ],
      "milestone": "What you can build or demonstrate after this phase"
    }
  ],
  "skills": ["skill1", "skill2", "skill3"],
  "careerOutcome": "Specific career outcome after completing this path"
}

Requirements:
- Create exactly 4 phases that progressively build skills
- Each phase must have 3-4 topics
- topic type must be one of: course, practice, project, reading
- estimatedHours must be a number (integer)
- estimatedWeeks must be a number (integer)
- Be specific, practical, and tailored to the target role`;

    const completion = await groq.chat.completions.create({
      messages: [
        {
          role: 'system',
          content:
            'You are an expert software engineering career coach. You create detailed, actionable, personalized learning paths. You ALWAYS return valid JSON only — no markdown code blocks, no backticks, no explanation text. Just the raw JSON object.',
        },
        { role: 'user', content: prompt },
      ],
      model: 'llama-3.3-70b-versatile',
      temperature: 0.4,
      max_tokens: 4096,
    });

    const raw = completion.choices[0]?.message?.content?.trim() || '';

    // Strip markdown fences if model adds them anyway
    const cleaned = raw
      .replace(/^```json\s*/i, '')
      .replace(/^```\s*/i, '')
      .replace(/\s*```$/i, '')
      .trim();

    // Extract JSON object
    const jsonMatch = cleaned.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      console.error('No JSON found in Groq response:', raw.slice(0, 300));
      return NextResponse.json({ error: 'AI returned invalid response. Please try again.' }, { status: 500 });
    }

    let pathData;
    try {
      pathData = JSON.parse(jsonMatch[0]);
    } catch (parseErr) {
      console.error('JSON parse error:', parseErr, jsonMatch[0].slice(0, 300));
      return NextResponse.json({ error: 'Failed to parse AI response. Please try again.' }, { status: 500 });
    }

    // Validate required fields
    if (!pathData.phases || !Array.isArray(pathData.phases) || pathData.phases.length === 0) {
      return NextResponse.json({ error: 'AI returned incomplete path. Please try again.' }, { status: 500 });
    }

    // Ensure numeric fields are numbers
    pathData.estimatedWeeks = Number(pathData.estimatedWeeks) || 12;
    pathData.phases = pathData.phases.map((p: any, i: number) => ({
      ...p,
      phase: Number(p.phase) || i + 1,
      topics: (p.topics || []).map((t: any) => ({
        ...t,
        estimatedHours: Number(t.estimatedHours) || 5,
        resources: Array.isArray(t.resources) ? t.resources : [],
      })),
    }));

    // Try to save to DB — fail silently if tables don't exist yet
    let savedPathId: string | null = null;
    try {
      const supabase = await createAdminClient();
      const { data: savedPath } = await supabase
        .from('learning_paths')
        .insert({
          user_id: userId,
          title: pathData.title || 'My Learning Path',
          description: pathData.description || '',
          goal,
          target_role: targetRole,
          estimated_weeks: pathData.estimatedWeeks,
          path_data: pathData.phases,
        })
        .select()
        .single();
      savedPathId = savedPath?.id || null;
    } catch (dbErr) {
      // DB tables may not exist yet — path still works without saving
      console.warn('DB save skipped (run migration):', (dbErr as any)?.message);
    }

    return NextResponse.json({ path: pathData, pathId: savedPathId });
  } catch (error: any) {
    console.error('Learning path error:', error);
    return NextResponse.json(
      { error: error?.message || 'Failed to generate learning path' },
      { status: 500 }
    );
  }
}

export async function GET() {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    try {
      const supabase = await createAdminClient();
      const { data: paths, error } = await supabase
        .from('learning_paths')
        .select('*')
        .eq('user_id', userId)
        .order('created_at', { ascending: false });

      if (error) throw error;
      return NextResponse.json({ paths: paths || [] });
    } catch (dbErr) {
      // Tables don't exist yet — return empty list gracefully
      return NextResponse.json({ paths: [] });
    }
  } catch (error: any) {
    return NextResponse.json({ error: 'Failed to fetch paths' }, { status: 500 });
  }
}
