import { createAdminClient } from '@/lib/supabase-server';
import { auth } from '@clerk/nextjs/server';
import { CourseCard } from '@/components/dashboard/course-card';
import { BookOpen, GraduationCap, ArrowRight } from 'lucide-react';
import Link from 'next/link';
import { redirect } from 'next/navigation';

export default async function MyCoursesPage() {
    const { userId } = await auth();
    if (!userId) return redirect('/sign-in');

    const supabase = await createAdminClient();

    // Fetch enrolled courses for the user
    const { data: enrollments, error } = await supabase
        .from('enrollments')
        .select(`
      course_id,
      courses (*)
    `)
        .eq('user_id', userId);

    if (error) {
        console.error('Fetch error:', error);
    }

    // Fetch progress status
    const { data: progressData } = await supabase
        .from('progress')
        .select('course_id, completed')
        .eq('user_id', userId);

    const userProgress: Record<string, number> = {};
    progressData?.forEach((p: any) => {
        userProgress[p.course_id] = p.completed ? 100 : 0;
    });

    const enrolledCourses = enrollments?.map((e: any) => e.courses).filter(Boolean) || [];

    return (
        <div className="min-h-screen bg-slate-950 px-4 md:px-8 py-8 md:py-12">
            <div className="max-w-7xl mx-auto space-y-8 md:space-y-12">
                {/* Header */}
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                    <div>
                        <h1 className="text-3xl md:text-4xl font-black text-white tracking-tight mb-2">My <span className="text-primary">Learning</span></h1>
                        <p className="text-slate-400 font-medium text-sm md:text-base">Continue where you left off and track your progress.</p>
                    </div>
                    <Link
                        href="/courses"
                        className="inline-flex items-center gap-2 px-5 py-2.5 md:px-6 md:py-3 bg-white/5 hover:bg-white/10 text-white rounded-xl font-bold transition-all border border-white/10 text-sm md:text-base justify-center"
                    >
                        Explore More Courses
                        <ArrowRight className="w-4 h-4 md:w-5 md:h-5 text-primary" />
                    </Link>
                </div>

                {/* Courses Grid */}
                {enrolledCourses.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                        {enrolledCourses.map((course: any) => (
                            <CourseCard
                                key={course.id}
                                id={course.id}
                                title={course.title}
                                instructor={course.instructor}
                                thumbnail_url={course.thumbnail_url}
                                category={course.category}
                                level={course.level}
                                enrolled={true}
                                progress={userProgress[course.id] || 0}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center py-32 text-center bg-white/[0.02] rounded-3xl border-2 border-dashed border-white/10">
                        <div className="w-24 h-24 bg-primary/10 rounded-full flex items-center justify-center mb-8">
                            <BookOpen className="w-12 h-12 text-primary opacity-50" />
                        </div>
                        <h3 className="text-2xl font-bold text-white mb-3">No courses enrolled yet</h3>
                        <p className="text-slate-400 max-w-md mx-auto mb-8 font-medium leading-relaxed">
                            Start your journey today! Explore our collection of 300+ engineering courses and enroll in your first one.
                        </p>
                        <Link
                            href="/courses"
                            className="px-10 py-4 bg-primary text-white font-black rounded-xl hover:bg-primary/90 transition-all hover:scale-105 shadow-xl shadow-primary/20"
                        >
                            Browse Catalog
                        </Link>
                    </div>
                )}

                {/* Stats Bar */}
                {enrolledCourses.length > 0 && (
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        {[
                            { label: 'Total Enrolled', value: enrolledCourses.length, color: 'text-white', bg: 'bg-blue-500/10 border-blue-500/20' },
                            { label: 'Completed', value: enrolledCourses.filter((c: any) => userProgress[c.id] === 100).length, color: 'text-emerald-400', bg: 'bg-emerald-500/10 border-emerald-500/20' },
                            { label: 'In Progress', value: enrolledCourses.filter((c: any) => userProgress[c.id] > 0 && userProgress[c.id] < 100).length, color: 'text-blue-400', bg: 'bg-blue-500/10 border-blue-500/20' },
                            { label: 'Not Started', value: enrolledCourses.filter((c: any) => !userProgress[c.id]).length, color: 'text-slate-400', bg: 'bg-white/5 border-white/10' },
                        ].map(stat => (
                            <div key={stat.label} className={`p-5 rounded-2xl border ${stat.bg} flex flex-col gap-1`}>
                                <p className="text-[10px] font-black uppercase text-slate-500 tracking-widest">{stat.label}</p>
                                <p className={`text-3xl font-black ${stat.color}`}>{stat.value}</p>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
