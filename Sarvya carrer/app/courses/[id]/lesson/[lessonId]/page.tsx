'use client';

import { use, useState } from 'react';
import Link from 'next/link';
import { Play, ChevronRight, ChevronLeft, CheckCircle, Lock, Volume2, Settings } from 'lucide-react';

const lessons = [
  { id: 1, title: 'Course Introduction', duration: 12, completed: true },
  { id: 2, title: 'Getting Started', duration: 28, completed: true },
  { id: 3, title: 'Core Concepts', duration: 45, completed: true },
  { id: 4, title: 'Advanced Patterns', duration: 52, completed: false },
  { id: 5, title: 'Real-world Projects', duration: 65, completed: false },
  { id: 6, title: 'Performance Optimization', duration: 38, completed: false },
  { id: 7, title: 'Final Project & Certification', duration: 90, completed: false },
];

export default function LessonPage({
  params,
}: {
  params: Promise<{ id: string; lessonId: string }>;
}) {
  const unwrappedParams = use(params);
  const [isCompleted, setIsCompleted] = useState(false);
  const currentLessonId = parseInt(unwrappedParams.lessonId);
  const currentLesson = lessons.find((l) => l.id === currentLessonId);
  const nextLesson = lessons.find((l) => l.id === currentLessonId + 1);
  const prevLesson = lessons.find((l) => l.id === currentLessonId - 1);

  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-950 to-black flex">
      {/* Sidebar */}
      <div
        className={`${sidebarOpen ? 'w-72' : 'w-0'
          } transition-all duration-300 border-r border-white/10 bg-slate-900/50 backdrop-blur-xl overflow-hidden flex flex-col`}
      >
        <div className="p-6 border-b border-white/10">
          <h3 className="font-bold text-white">Course Curriculum</h3>
          <p className="text-sm text-slate-400 mt-1">
            {lessons.filter((l) => l.completed).length}/{lessons.length} completed
          </p>
        </div>

        {/* Lessons list */}
        <div className="flex-1 overflow-y-auto p-4 space-y-2">
          {lessons.map((lesson) => (
            <Link
              key={lesson.id}
              href={`/courses/${unwrappedParams.id}/lesson/${lesson.id}`}
              className={`block p-3 rounded-lg transition-all duration-200 ${lesson.id === currentLessonId
                ? 'bg-gradient-to-r from-blue-500/30 to-violet-500/30 border border-blue-400/30 text-white'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`}
            >
              <div className="flex items-start gap-2">
                {lesson.completed ? (
                  <CheckCircle className="w-4 h-4 text-emerald-400 flex-shrink-0 mt-0.5" />
                ) : lesson.id > currentLessonId ? (
                  <Lock className="w-4 h-4 text-slate-600 flex-shrink-0 mt-0.5" />
                ) : (
                  <Play className="w-4 h-4 flex-shrink-0 mt-0.5" />
                )}
                <div className="min-w-0">
                  <p className="text-sm font-medium truncate">{lesson.title}</p>
                  <p className="text-xs opacity-75">{lesson.duration} min</p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Video player area */}
        <div className="flex-1 bg-black relative group">
          <div className="w-full h-full flex items-center justify-center">
            <div className="flex flex-col items-center gap-4">
              <Play className="w-20 h-20 text-blue-400 opacity-50" />
              <div className="text-center">
                <p className="text-slate-400">Video Player</p>
                <p className="text-sm text-slate-500">Lesson {currentLessonId}: {currentLesson?.title}</p>
              </div>
            </div>
          </div>

          {/* Overlay controls */}
          <div className="absolute top-4 right-4 flex gap-2">
            <button className="p-2 bg-black/50 hover:bg-black/75 rounded-lg text-white transition-colors">
              <Volume2 className="w-5 h-5" />
            </button>
            <button className="p-2 bg-black/50 hover:bg-black/75 rounded-lg text-white transition-colors">
              <Settings className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Controls and info */}
        <div className="border-t border-white/10 bg-gradient-to-r from-slate-900/50 to-slate-950/50 backdrop-blur-xl p-6">
          <div className="max-w-7xl mx-auto">
            {/* Lesson title */}
            <div className="mb-6">
              <h1 className="text-3xl font-bold text-white mb-2">{currentLesson?.title}</h1>
              <p className="text-slate-400">Duration: {currentLesson?.duration} minutes</p>
            </div>

            {/* Progress bar */}
            <div className="mb-6">
              <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-blue-500 to-cyan-400"
                  style={{
                    width: `${((currentLessonId - 1) / lessons.length) * 100}%`,
                  }}
                />
              </div>
            </div>

            {/* Action buttons */}
            <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
              <div className="flex gap-3 w-full md:w-auto">
                {prevLesson && (
                  <Link
                    href={`/courses/${unwrappedParams.id}/lesson/${prevLesson.id}`}
                    className="inline-flex items-center gap-2 px-6 py-3 bg-white/10 hover:bg-white/20 text-white rounded-lg font-semibold transition-all duration-300"
                  >
                    <ChevronLeft className="w-5 h-5" />
                    Previous
                  </Link>
                )}

                {!isCompleted ? (
                  <button
                    onClick={() => setIsCompleted(true)}
                    className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white rounded-lg font-semibold transition-all duration-300 flex-1 md:flex-none justify-center"
                  >
                    <CheckCircle className="w-5 h-5" />
                    Mark as Complete
                  </button>
                ) : (
                  <button
                    disabled
                    className="inline-flex items-center gap-2 px-6 py-3 bg-emerald-500/30 text-emerald-300 rounded-lg font-semibold flex-1 md:flex-none justify-center"
                  >
                    <CheckCircle className="w-5 h-5" />
                    Completed
                  </button>
                )}

                {nextLesson && (
                  <Link
                    href={`/courses/${unwrappedParams.id}/lesson/${nextLesson.id}`}
                    className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white rounded-lg font-semibold transition-all duration-300 glow-blue"
                  >
                    Next
                    <ChevronRight className="w-5 h-5" />
                  </Link>
                )}
              </div>

              {/* Toggle sidebar */}
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="hidden md:block px-4 py-3 bg-white/10 hover:bg-white/20 text-white rounded-lg font-medium transition-colors"
              >
                {sidebarOpen ? 'Hide' : 'Show'} Curriculum
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
