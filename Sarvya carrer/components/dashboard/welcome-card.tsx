'use client';

import Link from 'next/link';
import { ArrowRight, BookOpen, CheckCircle2, Clock, Zap } from 'lucide-react';

interface WelcomeCardProps {
  name?: string;
  completedCourses?: number;
  totalCourses?: number;
  inProgressCourses?: number;
}

export function WelcomeCard({
  name = 'Developer',
  completedCourses = 0,
  totalCourses = 0,
  inProgressCourses = 0,
}: WelcomeCardProps) {
  const progress = totalCourses > 0 ? Math.round((completedCourses / totalCourses) * 100) : 0;

  return (
    <div className="card-elevated p-6 md:p-8 rounded-2xl bg-gradient-to-br from-primary/5 to-secondary/5 hover:from-primary/10 hover:to-secondary/10 transition-all duration-300 dark:from-primary/15 dark:to-secondary/15">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <Zap className="w-5 h-5 text-secondary" />
            <span className="text-sm font-semibold text-secondary">Welcome back!</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-bold text-foreground mb-2">
            Hey {name}, Ready to Level Up?
          </h2>
          <p className="text-muted-foreground mb-4">
            {totalCourses === 0
              ? 'Start your journey — enroll in your first course today!'
              : `You have ${inProgressCourses} course${inProgressCourses !== 1 ? 's' : ''} in progress. Keep pushing!`}
          </p>

          {totalCourses > 0 && (
            <div className="mb-5">
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm font-medium text-muted-foreground">Overall Progress</span>
                <span className="text-sm font-semibold text-primary">{completedCourses}/{totalCourses} completed</span>
              </div>
              <div className="w-full h-2.5 bg-muted rounded-full overflow-hidden">
                <div className="h-full bg-gradient-to-r from-primary to-secondary transition-all duration-700 rounded-full"
                  style={{ width: `${progress}%` }} />
              </div>
            </div>
          )}

          <Link href="/courses"
            className="inline-flex items-center gap-2 px-6 py-3 button-primary font-semibold transition-all duration-300 hover:shadow-md">
            {totalCourses === 0 ? 'Browse Courses' : 'Continue Learning'}
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-3 w-full md:w-auto">
          <div className="bg-card p-4 rounded-xl text-center border border-border dark:bg-white/5 dark:border-white/10">
            <BookOpen className="w-5 h-5 text-blue-400 mx-auto mb-1" />
            <div className="text-2xl font-bold text-primary">{totalCourses}</div>
            <div className="text-xs text-muted-foreground mt-0.5">Enrolled</div>
          </div>
          <div className="bg-card p-4 rounded-xl text-center border border-border dark:bg-white/5 dark:border-white/10">
            <CheckCircle2 className="w-5 h-5 text-emerald-400 mx-auto mb-1" />
            <div className="text-2xl font-bold text-emerald-400">{completedCourses}</div>
            <div className="text-xs text-muted-foreground mt-0.5">Completed</div>
          </div>
          <div className="bg-card p-4 rounded-xl text-center border border-border dark:bg-white/5 dark:border-white/10">
            <Clock className="w-5 h-5 text-violet-400 mx-auto mb-1" />
            <div className="text-2xl font-bold text-violet-400">{inProgressCourses}</div>
            <div className="text-xs text-muted-foreground mt-0.5">In Progress</div>
          </div>
        </div>
      </div>
    </div>
  );
}
