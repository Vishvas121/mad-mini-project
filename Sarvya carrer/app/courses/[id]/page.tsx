import { createClient } from '@/lib/supabase-server';
import { auth } from '@clerk/nextjs/server';
import { ArrowLeft, Clock, Users, Star, BookOpen, GraduationCap, ShieldCheck } from 'lucide-react';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import { CourseActionsClient } from '@/components/dashboard/course-actions-client';
import { Metadata } from 'next';

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const { id } = await params;
  const supabase = await createClient();
  const { data: course } = await supabase.from('courses').select('title, description').eq('id', id).single();

  return {
    title: course ? `${course.title} | Engineering Platform` : 'Course Details',
    description: course?.description?.slice(0, 160) || 'Learn more about this engineering course.',
  };
}

interface PageProps {
  params: Promise<{ id: string }>;
}

export default async function CourseDetailPage({ params }: PageProps) {
  const { id } = await params;
  const { userId } = await auth();
  const supabase = await createClient();

  // 1. Fetch Course Data
  const { data: course, error: fetchError } = await supabase
    .from('courses')
    .select('*')
    .eq('id', id)
    .single();

  if (fetchError || !course) {
    console.error('Fetch error:', fetchError);
    return notFound();
  }

  // 2. Fetch User Enrollment & Progress
  let enrolled = false;
  let completed = false;

  if (userId) {
    const { data: enrollData } = await supabase
      .from('enrollments')
      .select('id')
      .eq('user_id', userId)
      .eq('course_id', id)
      .maybeSingle();

    enrolled = !!enrollData;

    const { data: progressData } = await supabase
      .from('progress')
      .select('completed')
      .eq('user_id', userId)
      .eq('course_id', id)
      .maybeSingle();

    completed = progressData?.completed || false;
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-200">
      {/* Navigation Header */}
      <div className="max-w-7xl mx-auto px-4 md:px-8 py-4 md:py-8">
        <Link href="/courses" className="inline-flex items-center gap-2 text-slate-400 hover:text-white transition-all group font-bold text-sm md:text-base">
          <ArrowLeft className="w-4 h-4 md:w-5 md:h-5 group-hover:-translate-x-1 transition-transform" />
          <span>Back to Courses</span>
        </Link>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 pb-12 md:pb-20">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 md:gap-12">

          {/* Main Content: Player & Details */}
          <div className="lg:col-span-2 space-y-6 md:space-y-8">
            {/* 16:9 Player Wrapper */}
            <div className="relative w-full aspect-video rounded-2xl md:rounded-3xl overflow-hidden border border-white/10 shadow-2xl bg-black group">
              {(() => {
                // Extract video ID from youtube_url if available
                const ytUrl = course.youtube_url || '';
                const videoIdMatch = ytUrl.match(/(?:v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/);
                const videoId = videoIdMatch?.[1];
                const embedSrc = videoId
                  ? `https://www.youtube.com/embed/${videoId}?rel=0`
                  : course.youtube_playlist_id
                  ? `https://www.youtube.com/embed/videoseries?list=${course.youtube_playlist_id}`
                  : null;
                return embedSrc ? (
                  <iframe
                    src={embedSrc}
                    title={course.title}
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    className="absolute inset-0 w-full h-full border-0"
                  />
                ) : (
                  <div className="absolute inset-0 flex flex-col items-center justify-center text-slate-500">
                    <GraduationCap className="w-12 h-12 md:w-20 md:h-20 mb-4 opacity-20" />
                    <p className="font-bold text-sm md:text-base">Video not available</p>
                  </div>
                );
              })()}
            </div>

            {/* Title & Description */}
            <div className="space-y-4 md:space-y-6">
              <div className="flex flex-wrap items-center gap-2 md:gap-3">
                <span className="px-2 py-0.5 md:px-3 md:py-1 rounded-full bg-primary/10 text-primary text-[9px] md:text-[10px] font-black uppercase tracking-widest border border-primary/20">
                  {course.category || 'General Engineering'}
                </span>
                <span className="px-2 py-0.5 md:px-3 md:py-1 rounded-full bg-white/5 text-slate-400 text-[9px] md:text-[10px] font-black uppercase tracking-widest border border-white/10">
                  {course.level || 'Beginner'}
                </span>
              </div>

              <h1 className="text-2xl md:text-5xl font-black text-white tracking-tight leading-tight">
                {course.title}
              </h1>

              <div className="flex items-center gap-4 py-3 md:py-4 border-y border-white/5">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 md:w-10 md:h-10 rounded-full bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center text-white font-bold text-xs md:text-base">
                    {course.instructor?.[0] || 'Y'}
                  </div>
                  <div>
                    <p className="text-[8px] md:text-[10px] font-bold text-slate-500 uppercase tracking-widest">Instructor</p>
                    <p className="font-bold text-white text-xs md:text-base">{course.instructor || 'YouTube Expert'}</p>
                  </div>
                </div>
                <div className="h-6 md:h-8 w-px bg-white/10 mx-1 md:mx-2" />
                <div>
                  <p className="text-[8px] md:text-[10px] font-bold text-slate-500 uppercase tracking-widest">Rating</p>
                  <div className="flex items-center gap-1 md:gap-1.5">
                    <Star className="w-3 h-3 md:w-4 md:h-4 fill-yellow-500 text-yellow-500" />
                    <span className="font-bold text-white text-sm md:text-lg">4.9</span>
                  </div>
                </div>
              </div>

              <div className="prose prose-invert max-w-none">
                <h3 className="text-lg md:text-xl font-bold text-white mb-3 md:mb-4">About this Course</h3>
                <p className="text-slate-400 leading-relaxed text-sm md:text-lg">
                  {course.description || 'No description provided for this course.'}
                </p>
              </div>
            </div>
          </div>

          {/* Sidebar: Enrollment & Stats */}
          <aside className="space-y-6">
            <div className="glass-card rounded-2xl md:rounded-3xl p-6 md:p-8 border border-white/10 shadow-2xl lg:sticky lg:top-8">
              <h3 className="text-lg md:text-xl font-bold text-white mb-4 md:mb-6">Course Access</h3>

              <CourseActionsClient
                courseId={course.id}
                enrolled={enrolled}
                completed={completed}
                courseTitle={course.title}
                instructorName={course.instructor}
              />

              <div className="mt-8 space-y-4">
                <div className="flex items-center gap-4 p-4 rounded-2xl bg-white/5 border border-white/5">
                  <Clock className="w-5 h-5 text-blue-400" />
                  <div>
                    <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Duration</p>
                    <p className="font-bold text-white">{course.duration || 'Self-paced'}</p>
                  </div>
                </div>

                <div className="flex items-center gap-4 p-4 rounded-2xl bg-white/5 border border-white/5">
                  <Users className="w-5 h-5 text-cyan-400" />
                  <div>
                    <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Students</p>
                    <p className="font-bold text-white">{(course.enrollment_count || 0).toLocaleString()}+ Enrolled</p>
                  </div>
                </div>

                <div className="flex items-center gap-4 p-4 rounded-2xl bg-white/5 border border-white/5">
                  <ShieldCheck className="w-5 h-5 text-emerald-400" />
                  <div>
                    <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Certificate</p>
                    <p className="font-bold text-white">Shareable Link</p>
                  </div>
                </div>
              </div>

              <div className="mt-8 p-6 rounded-2xl bg-gradient-to-br from-blue-600/20 to-violet-600/20 border border-blue-500/20">
                <p className="text-sm font-bold text-white mb-2">Technical Support</p>
                <p className="text-xs text-slate-400 leading-relaxed mb-4">
                  Need help with this course? Our AI assistant is available 24/7 to answer your questions.
                </p>
                <Link href="/ai-assistant" className="text-xs font-bold text-primary hover:underline">
                  Talk to AI Assistant →
                </Link>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}
