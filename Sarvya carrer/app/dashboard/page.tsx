import { createAdminClient } from '@/lib/supabase-server';
import { auth, currentUser } from '@clerk/nextjs/server';
import { WelcomeCard } from '@/components/dashboard/welcome-card';
import { CourseCard } from '@/components/dashboard/course-card';
import { TrendingUp, Award, Target, ArrowRight, Brain, Zap, BarChart3, Map } from 'lucide-react';
import Link from 'next/link';
import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Dashboard | Engineering Career OS',
  description: 'Manage your engineering learning journey, track your progress, and explore recommended courses.',
};

export default async function DashboardPage() {
  const { userId } = await auth();
  const user = await currentUser();
  const supabase = await createAdminClient();

  // 1. Fetch Enrolled Courses
  const { data: enrollments } = await supabase
    .from('enrollments')
    .select('course_id, courses(*)')
    .eq('user_id', userId);

  const enrolledCourses = enrollments?.map((e: any) => e.courses).filter(Boolean) || [];

  // 2. Fetch Progress for enrolled courses
  const { data: progressData } = await supabase
    .from('progress')
    .select('course_id, completed')
    .eq('user_id', userId);

  const userProgress: Record<string, number> = {};
  progressData?.forEach((p: any) => {
    userProgress[p.course_id] = p.completed ? 100 : 0;
  });

  // 3. Fetch Recommended Courses (excluding enrolled)
  const enrolledIds = enrolledCourses.map((c: any) => c.id);
  let { data: recommendedCourses } = await supabase
    .from('courses')
    .select('*')
    .limit(6);

  if (enrolledIds.length > 0) {
    recommendedCourses = recommendedCourses?.filter((c: any) => !enrolledIds.includes(c.id)) || [];
  }

  return (
    <div className="flex-1 bg-slate-950 p-4 md:p-8 transition-colors duration-300 min-h-screen">
      <div className="max-w-7xl mx-auto space-y-8 md:space-y-12">
        {/* Welcome section */}
        <WelcomeCard
          name={user?.firstName || 'Developer'}
          completedCourses={progressData?.filter((p: any) => p.completed).length || 0}
          totalCourses={enrolledCourses.length || 0}
          inProgressCourses={enrolledCourses.length - (progressData?.filter((p: any) => p.completed).length || 0)}
        />

        {/* Quick Stats — real data */}
        {(() => {
          const completedCount = progressData?.filter((p: any) => p.completed).length || 0;
          const totalEnrolled = enrolledCourses.length;
          const completionRate = totalEnrolled > 0 ? Math.round((completedCount / totalEnrolled) * 100) : 0;
          return (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-8">
              <div className="glass-card p-6 md:p-8 rounded-2xl md:rounded-3xl border border-white/5 flex items-center gap-4 md:gap-6 hover:border-primary/30 transition-all group">
                <div className="w-12 h-12 md:w-16 md:h-16 rounded-2xl bg-blue-500/10 flex items-center justify-center group-hover:scale-110 transition-transform">
                  <TrendingUp className="w-6 h-6 md:w-8 md:h-8 text-blue-400" />
                </div>
                <div>
                  <p className="text-[10px] md:text-xs font-bold text-slate-500 uppercase tracking-widest">Completion Rate</p>
                  <h4 className="text-xl md:text-2xl font-black text-white">{completionRate}%</h4>
                </div>
              </div>
              <div className="glass-card p-6 md:p-8 rounded-2xl md:rounded-3xl border border-white/5 flex items-center gap-4 md:gap-6 hover:border-emerald-500/30 transition-all group">
                <div className="w-12 h-12 md:w-16 md:h-16 rounded-2xl bg-emerald-500/10 flex items-center justify-center group-hover:scale-110 transition-transform">
                  <Award className="w-6 h-6 md:w-8 md:h-8 text-emerald-400" />
                </div>
                <div>
                  <p className="text-[10px] md:text-xs font-bold text-slate-500 uppercase tracking-widest">Completed</p>
                  <h4 className="text-xl md:text-2xl font-black text-white">{completedCount} Course{completedCount !== 1 ? 's' : ''}</h4>
                </div>
              </div>
              <div className="glass-card p-6 md:p-8 rounded-2xl md:rounded-3xl border border-white/5 flex items-center gap-4 md:gap-6 hover:border-violet-500/30 transition-all group sm:col-span-2 lg:col-span-1">
                <div className="w-12 h-12 md:w-16 md:h-16 rounded-2xl bg-violet-500/10 flex items-center justify-center group-hover:scale-110 transition-transform">
                  <Target className="w-6 h-6 md:w-8 md:h-8 text-violet-400" />
                </div>
                <div>
                  <p className="text-[10px] md:text-xs font-bold text-slate-500 uppercase tracking-widest">Enrolled</p>
                  <h4 className="text-xl md:text-2xl font-black text-white">{totalEnrolled} Course{totalEnrolled !== 1 ? 's' : ''}</h4>
                </div>
              </div>
            </div>
          );
        })()}

        {/* AI Personalized Learning Widget */}
        <section className="bg-gradient-to-br from-violet-500/10 via-blue-500/5 to-transparent border border-violet-500/20 rounded-3xl p-6 md:p-8">
          <div className="flex items-start justify-between flex-wrap gap-4">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center flex-shrink-0">
                <Brain className="w-6 h-6 text-white" />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h3 className="text-lg font-black text-white">AI Personalized Learning</h3>
                  <span className="text-[9px] font-black px-1.5 py-0.5 bg-violet-500/20 text-violet-400 rounded-full border border-violet-500/30 uppercase tracking-wide">NEW</span>
                </div>
                <p className="text-slate-400 text-sm">Adaptive quizzes, personalized paths, and skill analytics powered by AI</p>
              </div>
            </div>
            <Link href="/learning" className="flex items-center gap-2 px-4 py-2 bg-violet-600 rounded-xl text-sm font-bold hover:bg-violet-500 transition-colors flex-shrink-0">
              Explore <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mt-6">
            <Link href="/learning/quiz" className="flex items-center gap-3 bg-white/5 border border-white/10 rounded-xl p-4 hover:bg-white/10 hover:border-violet-500/30 transition-all group">
              <div className="w-9 h-9 bg-yellow-500/10 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <Zap className="w-4 h-4 text-yellow-400" />
              </div>
              <div>
                <p className="text-sm font-bold text-white">Adaptive Quiz</p>
                <p className="text-xs text-slate-500">AI adjusts difficulty</p>
              </div>
            </Link>
            <Link href="/learning/path" className="flex items-center gap-3 bg-white/5 border border-white/10 rounded-xl p-4 hover:bg-white/10 hover:border-blue-500/30 transition-all group">
              <div className="w-9 h-9 bg-blue-500/10 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <Map className="w-4 h-4 text-blue-400" />
              </div>
              <div>
                <p className="text-sm font-bold text-white">Learning Path</p>
                <p className="text-xs text-slate-500">Personalized roadmap</p>
              </div>
            </Link>
            <Link href="/learning/analytics" className="flex items-center gap-3 bg-white/5 border border-white/10 rounded-xl p-4 hover:bg-white/10 hover:border-green-500/30 transition-all group">
              <div className="w-9 h-9 bg-green-500/10 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <BarChart3 className="w-4 h-4 text-green-400" />
              </div>
              <div>
                <p className="text-sm font-bold text-white">Analytics</p>
                <p className="text-xs text-slate-500">Track your growth</p>
              </div>
            </Link>
          </div>
        </section>

        {/* Continue Learning */}
        {enrolledCourses.length > 0 && (          <section className="space-y-6 md:space-y-8">
            <div className="flex items-center justify-between px-1">
              <h3 className="text-lg md:text-2xl font-black text-white tracking-tight">Continue Learning</h3>
              <Link href="/my-courses" className="text-[10px] md:text-xs font-black text-primary uppercase tracking-widest flex items-center gap-1.5 hover:gap-2.5 transition-all">
                View All <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
              {enrolledCourses.map((course: any) => (
                <CourseCard
                  key={`enroll-${course.id}`}
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
          </section>
        )}

        {/* Recommended Courses Grid */}
        <section className="space-y-6 md:space-y-8">
          <div className="flex items-center justify-between px-1">
            <h3 className="text-lg md:text-2xl font-black text-white tracking-tight">Recommended for You</h3>
            <Link href="/courses" className="text-[10px] md:text-xs font-black text-primary uppercase tracking-widest flex items-center gap-1.5 hover:gap-2.5 transition-all">
              Discover All <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
            {recommendedCourses?.map((course: any) => (
              <CourseCard
                key={`rec-${course.id}`}
                id={course.id}
                title={course.title}
                instructor={course.instructor}
                thumbnail_url={course.thumbnail_url}
                category={course.category}
                level={course.level}
                enrolled={enrolledIds.includes(course.id)}
                progress={userProgress[course.id] || 0}
              />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
