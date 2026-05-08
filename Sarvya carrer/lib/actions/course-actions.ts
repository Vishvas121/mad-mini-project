'use server';

import { createAdminClient } from '@/lib/supabase-server';
import { auth } from '@clerk/nextjs/server';
import { revalidatePath } from 'next/cache';

function revalidateAll(courseId: string) {
    revalidatePath('/', 'layout');
    revalidatePath('/dashboard');
    revalidatePath('/my-courses');
    revalidatePath('/courses');
    revalidatePath(`/courses/${courseId}`);
}

export async function enrollInCourse(courseId: string) {
    const { userId } = await auth();
    if (!userId) throw new Error('Unauthorized');

    const supabase = await createAdminClient();

    const { error } = await supabase
        .from('enrollments')
        .upsert({ user_id: userId, course_id: courseId }, { onConflict: 'user_id,course_id' });

    if (error) throw new Error(`Failed to enroll: ${error.message}`);

    revalidateAll(courseId);
}

export async function updateCourseProgress(courseId: string, completed: boolean) {
    const { userId } = await auth();
    if (!userId) throw new Error('Unauthorized');

    const supabase = await createAdminClient();

    const { error } = await supabase
        .from('progress')
        .upsert({
            user_id: userId,
            course_id: courseId,
            completed,
            completed_at: completed ? new Date().toISOString() : null,
        }, { onConflict: 'user_id,course_id' });

    if (error) throw new Error(`Failed to update progress: ${error.message}`);

    revalidateAll(courseId);
}
