import { Suspense } from 'react';
import { createClient } from '@/lib/supabase-server';
import { auth } from '@clerk/nextjs/server';
import { CourseCard } from '@/components/dashboard/course-card';
import { CourseFilters } from '@/components/dashboard/course-filters';
import { Pagination } from '@/components/dashboard/pagination';
import { Loader2 } from 'lucide-react';
import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Engineering Courses | Master Modern Software Engineering',
  description: 'Explore over 300+ professional engineering playlists. Master Web Development, System Design, DevOps, and more with curated content from industry experts.',
};

interface CoursesPageProps {
  searchParams: Promise<{
    search?: string;
    category?: string;
    level?: string;
    page?: string;
  }>;
}

const ITEMS_PER_PAGE = 12;

export default async function CoursesPage({ searchParams }: CoursesPageProps) {
  const { userId } = await auth();
  const params = await searchParams;
  const page = parseInt(params.page || '1');
  const search = params.search || '';
  const category = params.category || 'all';
  const level = params.level || 'all';

  const supabase = await createClient();

  // 1. Fetch Categories & Levels for filters
  const { data: catData } = await supabase.from('courses').select('category').order('category');
  const categories = Array.from(new Set(catData?.map((c: any) => c.category).filter(Boolean)));
  const levels = ['Beginner', 'Intermediate', 'Advanced'];

  // 2. Build Query
  let query = supabase
    .from('courses')
    .select('*', { count: 'exact' });

  if (search) {
    query = query.or(`title.ilike.%${search}%,description.ilike.%${search}%,instructor.ilike.%${search}%`);
  }
  if (category !== 'all') {
    query = query.eq('category', category);
  }
  if (level !== 'all') {
    query = query.eq('level', level);
  }

  // 3. Pagination & Execution
  const from = (page - 1) * ITEMS_PER_PAGE;
  const to = from + ITEMS_PER_PAGE - 1;

  const { data: courses, count, error } = await query
    .order('created_at', { ascending: false })
    .range(from, to);

  if (error) console.error('Supabase fetch error:', error);

  // 4. Fetch Enrollments & Progress for current user
  let userEnrollments: string[] = [];
  let userProgress: Record<string, number> = {};

  if (userId) {
    const { data: enrollmentData } = await supabase
      .from('enrollments')
      .select('course_id')
      .eq('user_id', userId);

    userEnrollments = enrollmentData?.map((e: any) => e.course_id) || [];

    const { data: progressData } = await supabase
      .from('progress')
      .select('course_id, completed')
      .eq('user_id', userId);

    progressData?.forEach((p: any) => {
      userProgress[p.course_id] = p.completed ? 100 : 0; // Simplified for now
    });
  }

  const totalPages = Math.ceil((count || 0) / ITEMS_PER_PAGE);

  return (
    <div className="min-h-screen bg-slate-950">
      {/* Hero Header */}
      <div className="relative border-b border-white/10 bg-gradient-to-b from-blue-500/10 via-transparent to-transparent">
        <div className="max-w-7xl mx-auto px-4 md:px-8 py-10 md:py-16">
          <h1 className="text-3xl md:text-6xl font-black text-white mb-4 md:mb-6 tracking-tight">
            Master <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-300">Engineering</span>
          </h1>
          <p className="text-slate-400 text-lg md:text-xl max-w-2xl leading-relaxed">
            Choose from over 300+ professional playlists curated from industry-leading engineers.
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 py-8 md:py-12">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 md:gap-12">
          {/* Sidebar / Filters */}
          <aside className="lg:col-span-1 space-y-6 md:space-y-8">
            <Suspense fallback={<Loader2 className="animate-spin text-primary" />}>
              <CourseFilters categories={categories as string[]} levels={levels} />
            </Suspense>
          </aside>

          {/* Course Grid */}
          <main className="lg:col-span-3 space-y-6 md:space-y-8">
            <div className="flex items-center justify-between border-b border-white/5 pb-4 md:pb-6">
              <h2 className="text-[10px] md:text-sm font-bold uppercase tracking-widest text-slate-500">
                Found <span className="text-white">{count || 0}</span> result{(count || 0) !== 1 ? 's' : ''}
              </h2>
            </div>

            {courses && courses.length > 0 ? (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                  {courses.map((course: any) => (
                    <CourseCard
                      key={course.id}
                      id={course.id}
                      title={course.title}
                      instructor={course.instructor}
                      thumbnail_url={course.thumbnail_url}
                      category={course.category}
                      level={course.level}
                      enrolled={userEnrollments.includes(course.id)}
                      progress={userProgress[course.id] || 0}
                    />
                  ))}
                </div>
                <Pagination currentPage={page} totalPage={totalPages} />
              </>
            ) : (
              <div className="flex flex-col items-center justify-center py-24 text-center glass-card rounded-3xl border-dashed border-2 border-white/10">
                <div className="w-20 h-20 bg-white/5 rounded-full flex items-center justify-center mb-6">
                  <span className="text-4xl text-slate-600">📭</span>
                </div>
                <h3 className="text-2xl font-bold text-white mb-2">No courses found</h3>
                <p className="text-slate-400">Try adjusting your search or filters to find what you're looking for.</p>
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
