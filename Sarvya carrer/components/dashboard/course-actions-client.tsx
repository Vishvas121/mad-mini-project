'use client';

import { useTransition } from 'react';
import { enrollInCourse, updateCourseProgress } from '@/lib/actions/course-actions';
import { Play, CheckCircle, Loader2, BookOpen, Award } from 'lucide-react';
import { useRouter } from 'next/navigation';

interface CourseActionsClientProps {
    courseId: string;
    enrolled: boolean;
    completed: boolean;
    courseTitle?: string;
    instructorName?: string;
}

export function CourseActionsClient({ courseId, enrolled, completed, courseTitle = 'Engineering Course', instructorName = 'Expert Instructor' }: CourseActionsClientProps) {
    const [isPending, startTransition] = useTransition();
    const router = useRouter();

    const handleEnroll = () => {
        startTransition(async () => {
            try {
                await enrollInCourse(courseId);
                router.push('/my-courses');
            } catch (err) {
                console.error(err);
            }
        });
    };

    const handleToggleComplete = () => {
        startTransition(async () => {
            try {
                await updateCourseProgress(courseId, !completed);
                router.refresh();
            } catch (err) {
                console.error(err);
            }
        });
    };

    const handleDownloadCertificate = () => {
        const win = window.open('', '_blank');
        if (!win) return;
        const date = new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' });
        win.document.write(`
<!DOCTYPE html>
<html>
<head>
  <title>Certificate of Completion</title>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Inter:wght@400;600&display=swap');
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Inter', sans-serif; background: #fff; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
    .cert { width: 900px; padding: 60px; border: 12px solid #1e40af; position: relative; text-align: center; }
    .cert::before { content: ''; position: absolute; inset: 8px; border: 2px solid #93c5fd; pointer-events: none; }
    .logo { font-size: 13px; font-weight: 600; color: #1e40af; letter-spacing: 3px; text-transform: uppercase; margin-bottom: 8px; }
    .title { font-family: 'Playfair Display', serif; font-size: 42px; color: #1e3a8a; margin: 20px 0 8px; }
    .subtitle { font-size: 14px; color: #64748b; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 32px; }
    .presented { font-size: 14px; color: #64748b; margin-bottom: 8px; }
    .name { font-family: 'Playfair Display', serif; font-size: 48px; color: #1e40af; border-bottom: 2px solid #93c5fd; display: inline-block; padding-bottom: 8px; margin: 8px 0 24px; }
    .course-label { font-size: 13px; color: #64748b; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 8px; }
    .course { font-family: 'Playfair Display', serif; font-size: 26px; color: #1e3a8a; margin-bottom: 32px; }
    .meta { display: flex; justify-content: space-around; margin-top: 40px; padding-top: 24px; border-top: 1px solid #e2e8f0; }
    .meta-item { text-align: center; }
    .meta-label { font-size: 11px; color: #94a3b8; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 4px; }
    .meta-value { font-size: 13px; font-weight: 600; color: #334155; }
    .badge { display: inline-block; background: #1e40af; color: white; font-size: 11px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase; padding: 6px 20px; border-radius: 20px; margin-bottom: 24px; }
    @media print { body { background: white; } }
  </style>
</head>
<body>
  <div class="cert">
    <div class="logo">Engineering Career OS</div>
    <div class="badge">Certificate of Completion</div>
    <div class="title">Certificate of Achievement</div>
    <div class="subtitle">This is to certify that</div>
    <div class="presented">the following student has successfully completed</div>
    <div class="name">Engineering Student</div>
    <div class="course-label">Course Completed</div>
    <div class="course">${courseTitle}</div>
    <div class="meta">
      <div class="meta-item"><div class="meta-label">Instructor</div><div class="meta-value">${instructorName}</div></div>
      <div class="meta-item"><div class="meta-label">Date Issued</div><div class="meta-value">${date}</div></div>
      <div class="meta-item"><div class="meta-label">Platform</div><div class="meta-value">Engineering Career OS</div></div>
    </div>
  </div>
  <script>window.onload = () => { window.print(); }<\/script>
</body>
</html>`);
        win.document.close();
    };

    if (!enrolled) {
        return (
            <button onClick={handleEnroll} disabled={isPending}
                className="w-full py-4 bg-primary text-white font-bold rounded-xl flex items-center justify-center gap-2 hover:bg-primary/90 transition-all hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 shadow-lg shadow-primary/20">
                {isPending ? <Loader2 className="w-5 h-5 animate-spin" /> : <Play className="w-5 h-5 fill-current" />}
                Enroll in Course
            </button>
        );
    }

    return (
        <div className="space-y-3">
            <button onClick={handleToggleComplete} disabled={isPending}
                className={`w-full py-4 font-bold rounded-xl flex items-center justify-center gap-2 transition-all hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 border-2 ${completed
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20 hover:bg-emerald-500/20'
                    : 'bg-white/5 text-white border-white/10 hover:bg-white/10'}`}>
                {isPending ? <Loader2 className="w-5 h-5 animate-spin" />
                    : completed ? <CheckCircle className="w-5 h-5" />
                    : <BookOpen className="w-5 h-5" />}
                {completed ? 'Course Completed âœ“' : 'Mark as Completed'}
            </button>

            {completed && (
                <button onClick={handleDownloadCertificate}
                    className="w-full py-3 bg-gradient-to-r from-amber-500 to-yellow-500 text-white font-bold rounded-xl flex items-center justify-center gap-2 hover:opacity-90 transition-all hover:scale-[1.02]">
                    <Award className="w-5 h-5" />
                    Download Certificate
                </button>
            )}

            <div className="p-4 rounded-xl bg-primary/10 border border-primary/20 flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                    <Play className="w-5 h-5 text-primary" />
                </div>
                <div>
                    <p className="text-sm font-bold text-white leading-tight">You are enrolled</p>
                    <p className="text-[10px] font-bold text-primary uppercase tracking-widest mt-0.5">Keep learning</p>
                </div>
            </div>
        </div>
    );
}
