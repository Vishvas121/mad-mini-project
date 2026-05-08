import { NextResponse } from 'next/server';
import { auth } from '@clerk/nextjs/server';
import { createAdminClient } from '@/lib/supabase-server';

export async function GET() {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    // Default empty response if DB tables don't exist yet
    const emptyResponse = {
      profile: null,
      stats: { totalXP: 0, streak: 0, avgScore: 0, totalQuizzes: 0, completedCourses: 0, totalEnrolled: 0, level: 1 },
      weeklyActivity: Array.from({ length: 7 }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (6 - i));
        return {
          date: d.toISOString().split('T')[0],
          day: d.toLocaleDateString('en-US', { weekday: 'short' }),
          minutes: 0, xp: 0, quizzes: 0,
        };
      }),
      quizTrend: [],
      skillRadar: [],
      skills: [],
      categoryDistribution: [],
    };

    try {
      const supabase = await createAdminClient();

      const [profileRes, skillsRes, quizSessionsRes, activityRes, enrollmentsRes, progressRes] =
        await Promise.all([
          supabase.from('learning_profiles').select('*').eq('user_id', userId).single(),
          supabase.from('skill_scores').select('*').eq('user_id', userId).order('score', { ascending: false }),
          supabase.from('quiz_sessions').select('*').eq('user_id', userId).eq('completed', true).order('created_at', { ascending: false }).limit(30),
          supabase.from('daily_activity').select('*').eq('user_id', userId).order('activity_date', { ascending: false }).limit(30),
          supabase.from('enrollments').select('course_id').eq('user_id', userId),
          supabase.from('progress').select('*').eq('user_id', userId),
        ]);

      const profile = profileRes.data;
      const skills = skillsRes.data || [];
      const quizSessions = quizSessionsRes.data || [];
      const activity = activityRes.data || [];
      const enrollments = enrollmentsRes.data || [];
      const progressData = progressRes.data || [];

      const totalQuizzes = quizSessions.length;
      const avgScore = totalQuizzes > 0
        ? Math.round(quizSessions.reduce((s, q) => s + (q.score_percentage || 0), 0) / totalQuizzes)
        : 0;

      const last7Days = Array.from({ length: 7 }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (6 - i));
        return d.toISOString().split('T')[0];
      });

      const weeklyActivity = last7Days.map((date) => {
        const day = activity.find((a) => a.activity_date === date);
        return {
          date,
          day: new Date(date).toLocaleDateString('en-US', { weekday: 'short' }),
          minutes: day?.minutes_studied || 0,
          xp: day?.xp_earned || 0,
          quizzes: day?.quizzes_taken || 0,
        };
      });

      const quizTrend = quizSessions.slice(0, 10).reverse().map((q, i) => ({
        quiz: i + 1,
        score: q.score_percentage || 0,
        topic: q.topic,
        difficulty: q.difficulty,
      }));

      const skillRadar = skills.slice(0, 8).map((s) => ({
        skill: s.skill_name.length > 12 ? s.skill_name.slice(0, 12) + '…' : s.skill_name,
        score: s.score,
        fullName: s.skill_name,
      }));

      const categoryMap: Record<string, number> = {};
      skills.forEach((s) => {
        categoryMap[s.category] = (categoryMap[s.category] || 0) + 1;
      });

      return NextResponse.json({
        profile,
        stats: {
          totalXP: profile?.total_xp || 0,
          streak: profile?.streak_days || 0,
          avgScore,
          totalQuizzes,
          completedCourses: progressData.filter((p) => p.completed).length,
          totalEnrolled: enrollments.length,
          level: profile?.level || 1,
        },
        weeklyActivity,
        quizTrend,
        skillRadar,
        skills: skills.slice(0, 12),
        categoryDistribution: Object.entries(categoryMap).map(([name, value]) => ({ name, value })),
      });
    } catch (dbErr) {
      // Tables don't exist yet — return empty data so UI still renders
      console.warn('Analytics DB query skipped (run migration):', (dbErr as any)?.message);
      return NextResponse.json(emptyResponse);
    }
  } catch (error: any) {
    console.error('Analytics error:', error);
    return NextResponse.json({ error: 'Failed to fetch analytics' }, { status: 500 });
  }
}
