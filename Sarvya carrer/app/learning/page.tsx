'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import {
  Brain, Zap, Map, BarChart3, Trophy, Flame,
  Star, ArrowRight, BookOpen, Target, Clock,
  Sparkles, ChevronRight, Play, CheckCircle2,
} from 'lucide-react';

const QUIZ_TOPICS = [
  { name: 'JavaScript', category: 'Web Development', icon: '⚡', color: 'from-yellow-500 to-orange-500' },
  { name: 'React', category: 'Frontend', icon: '⚛️', color: 'from-cyan-500 to-blue-500' },
  { name: 'Node.js', category: 'Backend', icon: '🟢', color: 'from-green-500 to-emerald-500' },
  { name: 'Python', category: 'Data Science', icon: '🐍', color: 'from-blue-500 to-indigo-500' },
  { name: 'System Design', category: 'Architecture', icon: '🏗️', color: 'from-violet-500 to-purple-500' },
  { name: 'Data Structures', category: 'DSA', icon: '🌳', color: 'from-rose-500 to-pink-500' },
  { name: 'SQL', category: 'Database', icon: '🗄️', color: 'from-orange-500 to-red-500' },
  { name: 'Docker', category: 'DevOps', icon: '🐳', color: 'from-sky-500 to-cyan-500' },
  { name: 'Machine Learning', category: 'AI & ML', icon: '🤖', color: 'from-purple-500 to-violet-500' },
  { name: 'TypeScript', category: 'Web Development', icon: '🔷', color: 'from-blue-600 to-blue-400' },
  { name: 'AWS', category: 'DevOps', icon: '☁️', color: 'from-amber-500 to-yellow-500' },
  { name: 'Algorithms', category: 'DSA', icon: '🧮', color: 'from-teal-500 to-green-500' },
];

const LEARNING_PATHS = [
  {
    title: 'Frontend Engineer',
    description: 'HTML → CSS → JS → React → Next.js → TypeScript',
    weeks: 16,
    icon: '🎨',
    color: 'from-cyan-500 to-blue-600',
    skills: ['React', 'TypeScript', 'Next.js', 'CSS'],
  },
  {
    title: 'Backend Engineer',
    description: 'Python/Node.js → APIs → Databases → System Design',
    weeks: 18,
    icon: '⚙️',
    color: 'from-green-500 to-emerald-600',
    skills: ['Node.js', 'PostgreSQL', 'REST APIs', 'Docker'],
  },
  {
    title: 'AI/ML Engineer',
    description: 'Python → Math → ML → Deep Learning → LLMs',
    weeks: 24,
    icon: '🤖',
    color: 'from-violet-500 to-purple-600',
    skills: ['Python', 'TensorFlow', 'PyTorch', 'LangChain'],
  },
  {
    title: 'DevOps Engineer',
    description: 'Linux → Docker → Kubernetes → CI/CD → Cloud',
    weeks: 20,
    icon: '🚀',
    color: 'from-orange-500 to-red-600',
    skills: ['Docker', 'Kubernetes', 'AWS', 'Terraform'],
  },
];

export default function LearningPage() {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/learning-analytics')
      .then((r) => r.json())
      .then((d) => { setStats(d); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const userStats = stats?.stats;
  const topSkills = stats?.skills?.slice(0, 4) || [];

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      {/* Hero */}
      <div className="relative border-b border-white/10 bg-gradient-to-br from-violet-500/10 via-blue-500/5 to-transparent overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-violet-600/20 via-transparent to-transparent pointer-events-none" />
        <div className="max-w-7xl mx-auto px-4 md:px-8 py-12 md:py-20 relative">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center">
              <Brain className="w-5 h-5 text-white" />
            </div>
            <span className="text-xs font-bold uppercase tracking-widest text-violet-400">AI Personalized Learning</span>
          </div>
          <h1 className="text-4xl md:text-6xl font-black mb-4 tracking-tight">
            Learn Smarter,{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-violet-400 to-blue-400">
              Not Harder
            </span>
          </h1>
          <p className="text-slate-400 text-lg md:text-xl max-w-2xl leading-relaxed mb-8">
            AI adapts to your pace, style, and performance. Take quizzes, get personalized paths, and track your growth with real-time analytics.
          </p>
          <div className="flex flex-wrap gap-4">
            <Link
              href="/learning/quiz"
              className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-600 to-blue-600 rounded-xl font-bold hover:opacity-90 transition-all hover:scale-105 shadow-lg shadow-violet-500/25"
            >
              <Zap className="w-4 h-4" /> Start Adaptive Quiz
            </Link>
            <Link
              href="/learning/path"
              className="flex items-center gap-2 px-6 py-3 bg-white/10 border border-white/20 rounded-xl font-bold hover:bg-white/15 transition-all"
            >
              <Map className="w-4 h-4" /> Generate My Path
            </Link>
            <Link
              href="/learning/analytics"
              className="flex items-center gap-2 px-6 py-3 bg-white/10 border border-white/20 rounded-xl font-bold hover:bg-white/15 transition-all"
            >
              <BarChart3 className="w-4 h-4" /> View Analytics
            </Link>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 py-10 md:py-14 space-y-14">
        {/* Stats Row */}
        {!loading && userStats && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { label: 'Total XP', value: userStats.totalXP?.toLocaleString() || '0', icon: <Star className="w-5 h-5 text-yellow-400" />, color: 'from-yellow-500/10 to-orange-500/10 border-yellow-500/20' },
              { label: 'Day Streak', value: `${userStats.streak || 0} 🔥`, icon: <Flame className="w-5 h-5 text-orange-400" />, color: 'from-orange-500/10 to-red-500/10 border-orange-500/20' },
              { label: 'Avg Quiz Score', value: `${userStats.avgScore || 0}%`, icon: <Target className="w-5 h-5 text-green-400" />, color: 'from-green-500/10 to-emerald-500/10 border-green-500/20' },
              { label: 'Level', value: `Lv. ${userStats.level || 1}`, icon: <Trophy className="w-5 h-5 text-violet-400" />, color: 'from-violet-500/10 to-blue-500/10 border-violet-500/20' },
            ].map((s) => (
              <div key={s.label} className={`bg-gradient-to-br ${s.color} border rounded-2xl p-5 flex items-center gap-4`}>
                <div className="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center">{s.icon}</div>
                <div>
                  <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">{s.label}</p>
                  <p className="text-xl font-black text-white">{s.value}</p>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Quick Quiz Topics */}
        <section>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-black tracking-tight">Adaptive Quiz Topics</h2>
              <p className="text-slate-400 text-sm mt-1">AI adjusts difficulty based on your performance</p>
            </div>
            <Link href="/learning/quiz" className="text-xs font-bold text-violet-400 uppercase tracking-widest flex items-center gap-1 hover:gap-2 transition-all">
              All Topics <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
            {QUIZ_TOPICS.map((topic) => (
              <Link
                key={topic.name}
                href={`/learning/quiz?topic=${encodeURIComponent(topic.name)}`}
                className="group relative bg-white/5 border border-white/10 rounded-2xl p-4 hover:border-white/20 hover:bg-white/10 transition-all hover:scale-105 cursor-pointer"
              >
                <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${topic.color} flex items-center justify-center text-xl mb-3 group-hover:scale-110 transition-transform`}>
                  {topic.icon}
                </div>
                <p className="text-sm font-bold text-white">{topic.name}</p>
                <p className="text-xs text-slate-500 mt-0.5">{topic.category}</p>
                <ChevronRight className="absolute top-3 right-3 w-3.5 h-3.5 text-slate-600 group-hover:text-slate-400 transition-colors" />
              </Link>
            ))}
          </div>
        </section>

        {/* Two column: Learning Paths + Skills */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Learning Paths */}
          <section>
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-2xl font-black tracking-tight">Learning Paths</h2>
                <p className="text-slate-400 text-sm mt-1">AI-generated paths tailored to your goals</p>
              </div>
              <Link href="/learning/path" className="text-xs font-bold text-violet-400 uppercase tracking-widest flex items-center gap-1 hover:gap-2 transition-all">
                Create Path <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
            <div className="space-y-3">
              {LEARNING_PATHS.map((path) => (
                <Link
                  key={path.title}
                  href="/learning/path"
                  className="group flex items-center gap-4 bg-white/5 border border-white/10 rounded-2xl p-4 hover:border-white/20 hover:bg-white/8 transition-all"
                >
                  <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${path.color} flex items-center justify-center text-2xl flex-shrink-0`}>
                    {path.icon}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <p className="font-bold text-white">{path.title}</p>
                      <span className="text-xs text-slate-500 flex items-center gap-1">
                        <Clock className="w-3 h-3" /> {path.weeks}w
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-0.5 truncate">{path.description}</p>
                    <div className="flex gap-1.5 mt-2 flex-wrap">
                      {path.skills.map((s) => (
                        <span key={s} className="text-[10px] px-2 py-0.5 bg-white/10 rounded-full text-slate-300">{s}</span>
                      ))}
                    </div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-slate-600 group-hover:text-slate-400 flex-shrink-0 transition-colors" />
                </Link>
              ))}
            </div>
          </section>

          {/* Skill Scores */}
          <section>
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-2xl font-black tracking-tight">Your Skills</h2>
                <p className="text-slate-400 text-sm mt-1">Proficiency scores from quiz performance</p>
              </div>
              <Link href="/learning/analytics" className="text-xs font-bold text-violet-400 uppercase tracking-widest flex items-center gap-1 hover:gap-2 transition-all">
                Full Report <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            {topSkills.length > 0 ? (
              <div className="space-y-4">
                {topSkills.map((skill: any) => (
                  <div key={skill.skill_name} className="bg-white/5 border border-white/10 rounded-2xl p-4">
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-semibold text-white text-sm">{skill.skill_name}</span>
                      <span className="text-sm font-bold text-violet-400">{skill.score}%</span>
                    </div>
                    <div className="h-2 bg-white/10 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-violet-500 to-blue-500 rounded-full transition-all duration-700"
                        style={{ width: `${skill.score}%` }}
                      />
                    </div>
                    <p className="text-xs text-slate-500 mt-1.5">{skill.questions_attempted} questions attempted</p>
                  </div>
                ))}
              </div>
            ) : (
              <div className="bg-white/5 border border-dashed border-white/10 rounded-2xl p-10 text-center">
                <div className="w-16 h-16 bg-violet-500/10 rounded-2xl flex items-center justify-center mx-auto mb-4">
                  <Brain className="w-8 h-8 text-violet-400" />
                </div>
                <p className="font-bold text-white mb-2">No skills assessed yet</p>
                <p className="text-slate-400 text-sm mb-4">Take a quiz to start building your skill profile</p>
                <Link
                  href="/learning/quiz"
                  className="inline-flex items-center gap-2 px-4 py-2 bg-violet-600 rounded-xl text-sm font-bold hover:bg-violet-500 transition-colors"
                >
                  <Play className="w-3.5 h-3.5" /> Take First Quiz
                </Link>
              </div>
            )}
          </section>
        </div>

        {/* Feature Cards */}
        <section>
          <h2 className="text-2xl font-black tracking-tight mb-6">AI Learning Features</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {[
              {
                icon: <Zap className="w-6 h-6 text-yellow-400" />,
                bg: 'from-yellow-500/10 to-orange-500/10 border-yellow-500/20',
                title: 'Adaptive Quizzes',
                desc: 'AI generates questions and adjusts difficulty in real-time based on your answers. Get harder questions when you excel, easier ones when you struggle.',
                href: '/learning/quiz',
                cta: 'Start Quiz',
              },
              {
                icon: <Map className="w-6 h-6 text-blue-400" />,
                bg: 'from-blue-500/10 to-cyan-500/10 border-blue-500/20',
                title: 'Personalized Paths',
                desc: 'Tell the AI your goal and current level. It generates a week-by-week learning roadmap with specific topics, resources, and milestones.',
                href: '/learning/path',
                cta: 'Generate Path',
              },
              {
                icon: <BarChart3 className="w-6 h-6 text-green-400" />,
                bg: 'from-green-500/10 to-emerald-500/10 border-green-500/20',
                title: 'Analytics Dashboard',
                desc: 'Track your learning streak, XP, skill scores, quiz performance trends, and weekly study time with beautiful interactive charts.',
                href: '/learning/analytics',
                cta: 'View Analytics',
              },
            ].map((f) => (
              <div key={f.title} className={`bg-gradient-to-br ${f.bg} border rounded-2xl p-6 flex flex-col`}>
                <div className="w-12 h-12 bg-white/10 rounded-xl flex items-center justify-center mb-4">
                  {f.icon}
                </div>
                <h3 className="text-lg font-bold text-white mb-2">{f.title}</h3>
                <p className="text-slate-400 text-sm leading-relaxed flex-1">{f.desc}</p>
                <Link
                  href={f.href}
                  className="mt-4 flex items-center gap-2 text-sm font-bold text-white hover:gap-3 transition-all"
                >
                  {f.cta} <ArrowRight className="w-4 h-4" />
                </Link>
              </div>
            ))}
          </div>
        </section>

        {/* How it works */}
        <section className="bg-gradient-to-br from-violet-500/5 to-blue-500/5 border border-white/10 rounded-3xl p-8 md:p-12">
          <div className="flex items-center gap-3 mb-8">
            <Sparkles className="w-6 h-6 text-violet-400" />
            <h2 className="text-2xl font-black tracking-tight">How AI Personalization Works</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[
              { step: '01', title: 'Assess', desc: 'Take a quiz on any topic. AI evaluates your current knowledge level.', icon: <BookOpen className="w-5 h-5" /> },
              { step: '02', title: 'Adapt', desc: 'Questions get harder or easier based on your real-time performance.', icon: <Brain className="w-5 h-5" /> },
              { step: '03', title: 'Analyze', desc: 'Your skill scores, weak areas, and learning patterns are tracked.', icon: <BarChart3 className="w-5 h-5" /> },
              { step: '04', title: 'Personalize', desc: 'AI recommends courses and generates a custom learning path for you.', icon: <CheckCircle2 className="w-5 h-5" /> },
            ].map((s) => (
              <div key={s.step} className="flex flex-col items-start">
                <div className="flex items-center gap-3 mb-3">
                  <span className="text-4xl font-black text-white/10">{s.step}</span>
                  <div className="w-9 h-9 bg-violet-500/20 rounded-xl flex items-center justify-center text-violet-400">
                    {s.icon}
                  </div>
                </div>
                <h3 className="font-bold text-white mb-1">{s.title}</h3>
                <p className="text-slate-400 text-sm leading-relaxed">{s.desc}</p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
