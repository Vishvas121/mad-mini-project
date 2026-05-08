import { NextResponse } from 'next/server';
import { auth } from '@clerk/nextjs/server';
import { createAdminClient } from '@/lib/supabase-server';

export async function GET() {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    try {
      const supabase = await createAdminClient();
      const { data } = await supabase
        .from('learning_profiles')
        .select('*')
        .eq('user_id', userId)
        .single();
      return NextResponse.json({ profile: data || null });
    } catch {
      return NextResponse.json({ profile: null });
    }
  } catch {
    return NextResponse.json({ error: 'Failed to fetch profile' }, { status: 500 });
  }
}

export async function POST(req: Request) {
  try {
    const { userId } = await auth();
    if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    const body = await req.json();

    try {
      const supabase = await createAdminClient();
      const { data } = await supabase
        .from('learning_profiles')
        .upsert(
          {
            user_id: userId,
            learning_style: body.learning_style,
            preferred_difficulty: body.preferred_difficulty,
            preferred_categories: body.preferred_categories,
            weekly_goal_hours: body.weekly_goal_hours,
            onboarding_completed: true,
            updated_at: new Date().toISOString(),
          },
          { onConflict: 'user_id' }
        )
        .select()
        .single();
      return NextResponse.json({ profile: data });
    } catch {
      return NextResponse.json({ profile: null, warning: 'Profile not saved — run DB migration' });
    }
  } catch {
    return NextResponse.json({ error: 'Failed to save profile' }, { status: 500 });
  }
}
