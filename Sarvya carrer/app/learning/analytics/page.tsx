'use client';

import { useState, useEffect } from 'react';
import {
  BarChart, Bar, LineChart, Line, RadarChart, Radar,
  PolarGrid, PolarAngleAxis, PolarRadiusAxis,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import {
  BarChart3, Brain, Flame, Star, Trophy, Target,
  TrendingUp, Clock, BookOpen, Zap, ChevronLeft,
  Calendar, Award, ArrowUp, ArrowDown, Minus,
} from 'lucide-react';
import Link from 'next/link';

const COLORS = ['#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#06b6d4', '#84cc16'];

const LEVEL_NAMES: Record<number, string> = {
  1: 'Novice', 2: 'Apprentice', 3: 'Developer', 4: 'Engineer',
  5: 'Senior', 6: 'Lead', 7: 'Principal', 8: 'Architect',
  9: 'Expert', 10: 'Master',
};

function StatCard({ label, value, sub, icon, color }: any) {
  return (
    <div className={`bg-gradient-to-br ${color} border rounded-2xl p-5`}>
      <div className="flex items-start justify-between mb-3">
        <div className="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center">{icon}</div>
      </div>
      <p className="text-2xl font-black text-white mb-0.5">{value}</p>
      <p className="text-xs font-bold text-white/60 uppercase tracking-wider">{label}</p>
      {sub && <p className="text-xs text-white/40 mt-1">{sub}</p>}
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center h-40 text-slate-500">
      <BarChart3 className="w-8 h-8 mb-2 opacity-30" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

export default function AnalyticsPage() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'overview' | 'skills' | 'quizzes' | 'activity'>('overview');

  useEffect(() => {
    fetch('/api/learning-analytics')
      .then((r) => r.json())
      .then((d) => { setData(d); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center mx-auto mb-4 animate-pulse">
            <BarChart3 className="w-8 h-8 text-white" />
          </div>
          <p className="text-slate-400">Loading your analytics…</p>
        </div>
      </div>
    );
  }

  const stats = data?.stats || {};
  const weeklyActivity = data?.weeklyActivity || [];
  const quizTrend = data?.quizTrend || [];
  const skillRadar = data?.skillRadar || [];
  const skills = data?.skills || [];
  const categoryDistribution = data?.categoryDistribution || [];
  const level = stats.level || 1;
  const xpForNextLevel = (level * level) * 100;
  const xpProgress = Math.min(100, Math.round((stats.totalXP / xpForNextLevel) * 100));

  const tabs = [
    { id: 'overview', label: 'Overview', icon: <BarChart3 className="w-4 h-4" /> },
    { id: 'skills', label: 'Skills', icon: <Brain className="w-4 h-4" /> },
    { id: 'quizzes', label: 'Quizzes', icon: <Zap className="w-4 h-4" /> },
    { id: 'activity', label: 'Activity', icon: <Calendar className="w-4 h-4" /> },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      {/* Header */}
      <div className="border-b border-white/10 bg-gradient-to-r from-violet-500/5 to-blue-500/5">
        <div className="max-w-7xl mx-auto px-4 md:px-8 py-8">
          <Link href="/learning" className="flex items-center gap-2 text-slate-400 hover:text-white mb-6 transition-colors text-sm w-fit">
            <ChevronLeft className="w-4 h-4" /> Back to Learning Hub
          </Link>
          <div className="flex items-center justify-between flex-wrap gap-4">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center">
                <BarChart3 className="w-7 h-7 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-black">Learning Analytics</h1>
                <p className="text-slate-400 text-sm">Track your progress, skills, and learning patterns</p>
              </div>
            </div>

            {/* Level Badge */}
            <div className="bg-white/5 border border-white/10 rounded-2xl px-5 py-3 flex items-center gap-4">
              <div className="text-center">
                <p className="text-2xl font-black text-violet-400">Lv.{level}</p>
                <p className="text-xs text-slate-500">{LEVEL_NAMES[Math.min(level, 10)] || 'Legend'}</p>
              </div>
              <div className="w-32">
                <div className="flex justify-between text-xs text-slate-500 mb-1">
                  <span>{stats.totalXP} XP</span>
                  <span>{xpForNextLevel} XP</span>
                </div>
                <div className="h-2 bg-white/10 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-violet-500 to-blue-500 rounded-full transition-all duration-700"
                    style={{ width: `${xpProgress}%` }}
                  />
                </div>
                <p className="text-[10px] text-slate-500 mt-1 text-right">{xpProgress}% to Lv.{level + 1}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 py-8">
        {/* Tabs */}
        <div className="flex gap-1 bg-white/5 border border-white/10 rounded-xl p-1 mb-8 w-fit">
          {tabs.map((t) => (
            <button
              key={t.id}
              onClick={() => setActiveTab(t.id as any)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                activeTab === t.id
                  ? 'bg-white/15 text-white'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              {t.icon} {t.label}
            </button>
          ))}
        </div>

        {/* ── OVERVIEW TAB ── */}
        {activeTab === 'overview' && (
          <div className="space-y-8">
            {/* Stat Cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <StatCard
                label="Total XP"
                value={stats.totalXP?.toLocaleString() || '0'}
                sub={`Level ${level} · ${xpProgress}% to next`}
                icon={<Star className="w-5 h-5 text-yellow-400" />}
                color="from-yellow-500/10 to-orange-500/10 border-yellow-500/20"
              />
              <StatCard
                label="Day Streak"
                value={`${stats.streak || 0} 🔥`}
                sub="Keep it going!"
                icon={<Flame className="w-5 h-5 text-orange-400" />}
                color="from-orange-500/10 to-red-500/10 border-orange-500/20"
              />
              <StatCard
                label="Avg Quiz Score"
                value={`${stats.avgScore || 0}%`}
                sub={`${stats.totalQuizzes || 0} quizzes taken`}
                icon={<Target className="w-5 h-5 text-green-400" />}
                color="from-green-500/10 to-emerald-500/10 border-green-500/20"
              />
              <StatCard
                label="Courses"
                value={`${stats.completedCourses || 0}/${stats.totalEnrolled || 0}`}
                sub="Completed / Enrolled"
                icon={<BookOpen className="w-5 h-5 text-blue-400" />}
                color="from-blue-500/10 to-cyan-500/10 border-blue-500/20"
              />
            </div>

            {/* Weekly Activity Chart */}
            <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
              <h3 className="font-bold text-white mb-1">Weekly Study Activity</h3>
              <p className="text-xs text-slate-400 mb-6">Minutes studied per day (last 7 days)</p>
              {weeklyActivity.some((d: any) => d.minutes > 0) ? (
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={weeklyActivity} barSize={32}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                    <XAxis dataKey="day" tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }}
                      formatter={(v: any) => [`${v} min`, 'Study Time']}
                    />
                    <Bar dataKey="minutes" fill="url(#barGrad)" radius={[6, 6, 0, 0]} />
                    <defs>
                      <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#8b5cf6" />
                        <stop offset="100%" stopColor="#3b82f6" />
                      </linearGradient>
                    </defs>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No activity yet — take a quiz to start tracking!" />
              )}
            </div>

            {/* Quiz Trend + Category Distribution */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
                <h3 className="font-bold text-white mb-1">Quiz Score Trend</h3>
                <p className="text-xs text-slate-400 mb-6">Your last 10 quiz scores</p>
                {quizTrend.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={quizTrend}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                      <XAxis dataKey="quiz" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} label={{ value: 'Quiz #', position: 'insideBottom', fill: '#475569', fontSize: 11 }} />
                      <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                      <Tooltip
                        contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }}
                        formatter={(v: any, _: any, props: any) => [`${v}%`, props.payload.topic]}
                      />
                      <Line type="monotone" dataKey="score" stroke="#8b5cf6" strokeWidth={2.5} dot={{ fill: '#8b5cf6', r: 4 }} activeDot={{ r: 6 }} />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <EmptyState message="Take quizzes to see your score trend" />
                )}
              </div>

              <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
                <h3 className="font-bold text-white mb-1">Skills by Category</h3>
                <p className="text-xs text-slate-400 mb-6">Distribution of assessed skills</p>
                {categoryDistribution.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie data={categoryDistribution} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={3} dataKey="value">
                        {categoryDistribution.map((_: any, i: number) => (
                          <Cell key={i} fill={COLORS[i % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }} />
                      <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 11, color: '#94a3b8' }} />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <EmptyState message="Take quizzes across topics to see distribution" />
                )}
              </div>
            </div>
          </div>
        )}

        {/* ── SKILLS TAB ── */}
        {activeTab === 'skills' && (
          <div className="space-y-8">
            {/* Radar Chart */}
            <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
              <h3 className="font-bold text-white mb-1">Skill Radar</h3>
              <p className="text-xs text-slate-400 mb-6">Your proficiency across top skills</p>
              {skillRadar.length >= 3 ? (
                <ResponsiveContainer width="100%" height={320}>
                  <RadarChart data={skillRadar}>
                    <PolarGrid stroke="rgba(255,255,255,0.08)" />
                    <PolarAngleAxis dataKey="skill" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                    <PolarRadiusAxis angle={30} domain={[0, 100]} tick={{ fill: '#475569', fontSize: 10 }} />
                    <Radar name="Score" dataKey="score" stroke="#8b5cf6" fill="#8b5cf6" fillOpacity={0.25} strokeWidth={2} />
                    <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }} formatter={(v: any, _: any, p: any) => [`${v}%`, p.payload.fullName]} />
                  </RadarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="Take quizzes on 3+ topics to see your skill radar" />
              )}
            </div>

            {/* Skill Bars */}
            <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
              <h3 className="font-bold text-white mb-6">Skill Breakdown</h3>
              {skills.length > 0 ? (
                <div className="space-y-4">
                  {skills.map((skill: any) => {
                    const scoreColor =
                      skill.score >= 80 ? 'from-green-500 to-emerald-500' :
                      skill.score >= 60 ? 'from-blue-500 to-cyan-500' :
                      skill.score >= 40 ? 'from-yellow-500 to-orange-500' :
                      'from-red-500 to-rose-500';
                    const badge =
                      skill.score >= 80 ? { label: 'Expert', color: 'text-green-400 bg-green-500/10 border-green-500/20' } :
                      skill.score >= 60 ? { label: 'Proficient', color: 'text-blue-400 bg-blue-500/10 border-blue-500/20' } :
                      skill.score >= 40 ? { label: 'Learning', color: 'text-yellow-400 bg-yellow-500/10 border-yellow-500/20' } :
                      { label: 'Beginner', color: 'text-red-400 bg-red-500/10 border-red-500/20' };

                    return (
                      <div key={skill.skill_name}>
                        <div className="flex items-center justify-between mb-1.5">
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-white text-sm">{skill.skill_name}</span>
                            <span className={`text-[10px] px-2 py-0.5 rounded-full border font-bold ${badge.color}`}>{badge.label}</span>
                          </div>
                          <div className="flex items-center gap-3 text-xs text-slate-400">
                            <span>{skill.questions_attempted} attempts</span>
                            <span className="font-bold text-white">{skill.score}%</span>
                          </div>
                        </div>
                        <div className="h-2.5 bg-white/10 rounded-full overflow-hidden">
                          <div
                            className={`h-full bg-gradient-to-r ${scoreColor} rounded-full transition-all duration-700`}
                            style={{ width: `${skill.score}%` }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <EmptyState message="No skills assessed yet — take a quiz to start!" />
              )}
            </div>
          </div>
        )}

        {/* ── QUIZZES TAB ── */}
        {activeTab === 'quizzes' && (
          <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <StatCard
                label="Total Quizzes"
                value={stats.totalQuizzes || 0}
                icon={<Zap className="w-5 h-5 text-violet-400" />}
                color="from-violet-500/10 to-purple-500/10 border-violet-500/20"
              />
              <StatCard
                label="Average Score"
                value={`${stats.avgScore || 0}%`}
                icon={<Target className="w-5 h-5 text-green-400" />}
                color="from-green-500/10 to-emerald-500/10 border-green-500/20"
              />
              <StatCard
                label="Best Streak"
                value={`${stats.streak || 0} days`}
                icon={<Flame className="w-5 h-5 text-orange-400" />}
                color="from-orange-500/10 to-red-500/10 border-orange-500/20"
              />
            </div>

            <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
              <h3 className="font-bold text-white mb-1">Score Progression</h3>
              <p className="text-xs text-slate-400 mb-6">How your quiz scores have improved over time</p>
              {quizTrend.length > 0 ? (
                <ResponsiveContainer width="100%" height={280}>
                  <LineChart data={quizTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                    <XAxis dataKey="quiz" tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }}
                      formatter={(v: any, _: any, p: any) => [`${v}%`, `${p.payload.topic} (${p.payload.difficulty})`]}
                    />
                    <Line type="monotone" dataKey="score" stroke="#8b5cf6" strokeWidth={3} dot={{ fill: '#8b5cf6', r: 5, strokeWidth: 2, stroke: '#1e293b' }} activeDot={{ r: 7 }} />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="Take quizzes to see your progression" />
              )}
            </div>

            <div className="bg-white/5 border border-white/10 rounded-2xl p-6 text-center">
              <Brain className="w-10 h-10 text-violet-400 mx-auto mb-3" />
              <h3 className="font-bold text-white mb-2">Ready for another quiz?</h3>
              <p className="text-slate-400 text-sm mb-4">AI adapts difficulty to keep you in the optimal learning zone</p>
              <Link
                href="/learning/quiz"
                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-600 to-blue-600 rounded-xl font-bold hover:opacity-90 transition-all"
              >
                <Zap className="w-4 h-4" /> Start Adaptive Quiz
              </Link>
            </div>
          </div>
        )}

        {/* ── ACTIVITY TAB ── */}
        {activeTab === 'activity' && (
          <div className="space-y-8">
            <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
              <h3 className="font-bold text-white mb-1">Daily Study Minutes</h3>
              <p className="text-xs text-slate-400 mb-6">Last 7 days of learning activity</p>
              {weeklyActivity.some((d: any) => d.minutes > 0) ? (
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={weeklyActivity} barSize={36}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                    <XAxis dataKey="day" tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }}
                      formatter={(v: any) => [`${v} min`, 'Study Time']}
                    />
                    <Bar dataKey="minutes" fill="url(#actGrad)" radius={[6, 6, 0, 0]} />
                    <defs>
                      <linearGradient id="actGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#10b981" />
                        <stop offset="100%" stopColor="#3b82f6" />
                      </linearGradient>
                    </defs>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No activity recorded yet" />
              )}
            </div>

            <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
              <h3 className="font-bold text-white mb-1">XP Earned Per Day</h3>
              <p className="text-xs text-slate-400 mb-6">Experience points from quizzes and learning</p>
              {weeklyActivity.some((d: any) => d.xp > 0) ? (
                <ResponsiveContainer width="100%" height={200}>
                  <LineChart data={weeklyActivity}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                    <XAxis dataKey="day" tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, color: '#fff' }}
                      formatter={(v: any) => [`+${v} XP`, 'XP Earned']}
                    />
                    <Line type="monotone" dataKey="xp" stroke="#f59e0b" strokeWidth={2.5} dot={{ fill: '#f59e0b', r: 4 }} />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="Complete quizzes to earn XP" />
              )}
            </div>

            {/* Activity Summary */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {weeklyActivity.map((day: any) => (
                <div key={day.date} className="bg-white/5 border border-white/10 rounded-xl p-4">
                  <p className="text-xs text-slate-400 mb-2">{new Date(day.date).toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}</p>
                  <div className="space-y-1.5">
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-400">Study time</span>
                      <span className="font-bold text-white">{day.minutes} min</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-400">Quizzes</span>
                      <span className="font-bold text-white">{day.quizzes}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-400">XP earned</span>
                      <span className="font-bold text-yellow-400">+{day.xp}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
