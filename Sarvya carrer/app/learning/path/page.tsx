'use client';

import { useState, useEffect } from 'react';
import {
  Map, Brain, ChevronLeft, Sparkles, Clock, Target,
  CheckCircle2, Circle, ChevronDown, ChevronUp,
  BookOpen, Code2, Layers, ArrowRight, AlertCircle,
  Plus, ExternalLink,
} from 'lucide-react';
import Link from 'next/link';

const ROLES = [
  'Frontend Engineer', 'Backend Engineer', 'Full Stack Engineer',
  'DevOps Engineer', 'AI/ML Engineer', 'Data Engineer',
  'Mobile Developer', 'Security Engineer', 'Cloud Architect',
  'Site Reliability Engineer',
];

const LEVELS = ['Complete Beginner', 'Beginner', 'Intermediate', 'Advanced'];
const HOURS = ['2–4', '5–8', '10–15', '20+'];

const SKILL_OPTIONS = [
  'HTML/CSS', 'JavaScript', 'TypeScript', 'React', 'Vue', 'Angular',
  'Node.js', 'Python', 'Java', 'Go', 'Rust', 'C++',
  'SQL', 'MongoDB', 'PostgreSQL', 'Redis',
  'Docker', 'Kubernetes', 'AWS', 'GCP', 'Azure',
  'Git', 'Linux', 'REST APIs', 'GraphQL',
  'Machine Learning', 'Deep Learning', 'Data Science',
];

const TYPE_ICONS: Record<string, React.ReactNode> = {
  course:   <BookOpen className="w-3.5 h-3.5" />,
  practice: <Code2    className="w-3.5 h-3.5" />,
  project:  <Layers   className="w-3.5 h-3.5" />,
  reading:  <BookOpen className="w-3.5 h-3.5" />,
};

const TYPE_COLORS: Record<string, string> = {
  course:   'bg-blue-500/20 text-blue-300 border-blue-500/30',
  practice: 'bg-green-500/20 text-green-300 border-green-500/30',
  project:  'bg-violet-500/20 text-violet-300 border-violet-500/30',
  reading:  'bg-orange-500/20 text-orange-300 border-orange-500/30',
};

interface PathTopic {
  name: string;
  type: string;
  estimatedHours: number;
  description: string;
  resources: string[];
}

interface Phase {
  phase: number;
  title: string;
  duration: string;
  objective: string;
  topics: PathTopic[];
  milestone: string;
}

interface LearningPath {
  title: string;
  description: string;
  estimatedWeeks: number;
  phases: Phase[];
  skills: string[];
  careerOutcome: string;
}

type View = 'form' | 'loading' | 'result' | 'saved';

export default function LearningPathPage() {
  const [view, setView]                     = useState<View>('form');
  const [goal, setGoal]                     = useState('');
  const [currentLevel, setCurrentLevel]     = useState('Beginner');
  const [targetRole, setTargetRole]         = useState('');
  const [hoursPerWeek, setHoursPerWeek]     = useState('5–8');
  const [selectedSkills, setSelectedSkills] = useState<string[]>([]);
  const [generatedPath, setGeneratedPath]   = useState<LearningPath | null>(null);
  const [savedPaths, setSavedPaths]         = useState<any[]>([]);
  const [expandedPhase, setExpandedPhase]   = useState<number | null>(0);
  const [error, setError]                   = useState('');
  const [completedSteps, setCompletedSteps] = useState<Set<string>>(new Set());

  useEffect(() => { fetchSavedPaths(); }, []);

  const fetchSavedPaths = async () => {
    try {
      const res  = await fetch('/api/learning-path');
      const data = await res.json();
      setSavedPaths(data.paths || []);
    } catch { /* silent */ }
  };

  const toggleSkill = (skill: string) =>
    setSelectedSkills(prev =>
      prev.includes(skill) ? prev.filter(s => s !== skill) : [...prev, skill]
    );

  const generatePath = async () => {
    if (!goal.trim())  { setError('Please describe your learning goal'); return; }
    if (!targetRole)   { setError('Please select a target role');        return; }
    setError('');
    setView('loading');

    try {
      const res  = await fetch('/api/learning-path', {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({
          goal, currentLevel, targetRole,
          timeAvailableWeekly: hoursPerWeek,
          skills: selectedSkills,
        }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to generate path');
      if (!data.path) throw new Error('No path returned from AI');
      setGeneratedPath(data.path);
      setExpandedPhase(0);
      setView('result');
      fetchSavedPaths();
    } catch (e: any) {
      setError(e.message || 'Failed to generate path. Please try again.');
      setView('form');
    }
  };

  const toggleStep = (key: string) =>
    setCompletedSteps(prev => {
      const next = new Set(prev);
      next.has(key) ? next.delete(key) : next.add(key);
      return next;
    });

  /* ── LOADING ─────────────────────────────────────────────── */
  if (view === 'loading') return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center">
      <div className="text-center max-w-sm px-4">
        <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center mx-auto mb-6 animate-pulse">
          <Brain className="w-10 h-10 text-white" />
        </div>
        <h2 className="text-2xl font-black text-white mb-2">Building Your Path</h2>
        <p className="text-slate-400 text-sm leading-relaxed">
          Groq AI is crafting a personalised week-by-week roadmap for{' '}
          <strong className="text-white">{targetRole}</strong>…
        </p>
        <div className="flex gap-1.5 justify-center mt-6">
          {[0,1,2].map(i => (
            <div key={i} className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"
              style={{ animationDelay: `${i * 0.15}s` }} />
          ))}
        </div>
      </div>
    </div>
  );

  /* ── RESULT ──────────────────────────────────────────────── */
  if (view === 'result' && generatedPath) {
    const phases     = generatedPath.phases || [];
    const totalTopics = phases.reduce((s, p) => s + (p.topics?.length || 0), 0);
    const totalHours  = phases.reduce(
      (s, p) => s + (p.topics || []).reduce((ts, t) => ts + (Number(t.estimatedHours) || 0), 0), 0
    );

    return (
      <div className="min-h-screen bg-slate-950 text-white">
        <div className="max-w-4xl mx-auto px-4 py-10">

          {/* top nav */}
          <div className="flex items-center justify-between mb-8">
            <button onClick={() => setView('form')}
              className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors text-sm">
              <ChevronLeft className="w-4 h-4" /> Generate New Path
            </button>
            {savedPaths.length > 0 && (
              <button onClick={() => setView('saved')}
                className="text-xs font-bold text-blue-400 hover:text-blue-300 transition-colors">
                Saved Paths ({savedPaths.length})
              </button>
            )}
          </div>

          {/* header card */}
          <div className="bg-gradient-to-br from-blue-500/10 to-violet-500/10 border border-white/10 rounded-3xl p-8 mb-8">
            <div className="flex items-center gap-2 mb-3">
              <Sparkles className="w-4 h-4 text-blue-400" />
              <span className="text-xs font-bold text-blue-400 uppercase tracking-widest">
                AI Generated · Groq llama-3.3-70b
              </span>
            </div>
            <h1 className="text-3xl font-black mb-2">{generatedPath.title}</h1>
            <p className="text-slate-400 leading-relaxed max-w-2xl">{generatedPath.description}</p>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
              {[
                { label: 'Duration',    value: `${generatedPath.estimatedWeeks} weeks`, icon: <Clock    className="w-4 h-4 text-blue-400"   /> },
                { label: 'Phases',      value: `${phases.length} phases`,               icon: <Layers   className="w-4 h-4 text-violet-400" /> },
                { label: 'Topics',      value: `${totalTopics} topics`,                 icon: <BookOpen className="w-4 h-4 text-green-400"  /> },
                { label: 'Total Hours', value: `~${totalHours}h`,                       icon: <Target   className="w-4 h-4 text-orange-400" /> },
              ].map(s => (
                <div key={s.label} className="bg-white/5 rounded-2xl p-4 flex items-center gap-3">
                  {s.icon}
                  <div>
                    <p className="text-xs text-slate-500">{s.label}</p>
                    <p className="font-bold text-white text-sm">{s.value}</p>
                  </div>
                </div>
              ))}
            </div>

            {generatedPath.skills?.length > 0 && (
              <div className="mt-4 flex flex-wrap gap-2">
                {generatedPath.skills.map(s => (
                  <span key={s} className="text-xs px-3 py-1 bg-white/10 rounded-full text-slate-300 border border-white/10">{s}</span>
                ))}
              </div>
            )}
          </div>

          {/* phases */}
          <div className="space-y-4">
            {phases.map((phase, pi) => {
              const isExpanded     = expandedPhase === pi;
              const phaseCompleted = (phase.topics || []).every((_, ti) => completedSteps.has(`${pi}-${ti}`));
              const doneCount      = (phase.topics || []).filter((_, ti) => completedSteps.has(`${pi}-${ti}`)).length;

              return (
                <div key={pi} className="bg-white/5 border border-white/10 rounded-2xl overflow-hidden">
                  <button
                    onClick={() => setExpandedPhase(isExpanded ? null : pi)}
                    className="w-full flex items-center justify-between p-6 hover:bg-white/5 transition-colors text-left"
                  >
                    <div className="flex items-center gap-4">
                      <div className={`w-10 h-10 rounded-xl flex items-center justify-center font-black text-sm flex-shrink-0 ${
                        phaseCompleted ? 'bg-green-500/20 text-green-400' : 'bg-white/10 text-slate-400'
                      }`}>
                        {phaseCompleted ? <CheckCircle2 className="w-5 h-5" /> : `P${phase.phase}`}
                      </div>
                      <div>
                        <p className="font-bold text-white">{phase.title}</p>
                        <p className="text-xs text-slate-400 mt-0.5">
                          {phase.duration} · {doneCount}/{phase.topics?.length || 0} topics done
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-xs text-slate-500 hidden md:block max-w-xs truncate">{phase.objective}</span>
                      {isExpanded
                        ? <ChevronUp   className="w-4 h-4 text-slate-400 flex-shrink-0" />
                        : <ChevronDown className="w-4 h-4 text-slate-400 flex-shrink-0" />}
                    </div>
                  </button>

                  {isExpanded && (
                    <div className="px-6 pb-6 border-t border-white/5 pt-4 space-y-4">
                      <p className="text-sm text-slate-400 italic">🎯 {phase.objective}</p>

                      <div className="space-y-3">
                        {(phase.topics || []).map((topic, ti) => {
                          const key  = `${pi}-${ti}`;
                          const done = completedSteps.has(key);
                          const typeColor = TYPE_COLORS[topic.type] || TYPE_COLORS.course;
                          const typeIcon  = TYPE_ICONS[topic.type]  || TYPE_ICONS.course;

                          return (
                            <div key={ti}
                              className={`flex items-start gap-4 p-4 rounded-xl border transition-all ${
                                done
                                  ? 'bg-green-500/5 border-green-500/20'
                                  : 'bg-white/[0.03] border-white/[0.08] hover:border-white/15'
                              }`}
                            >
                              <button onClick={() => toggleStep(key)} className="mt-0.5 flex-shrink-0">
                                {done
                                  ? <CheckCircle2 className="w-5 h-5 text-green-400" />
                                  : <Circle       className="w-5 h-5 text-slate-600 hover:text-slate-400 transition-colors" />}
                              </button>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 flex-wrap mb-1">
                                  <p className={`font-semibold text-sm ${done ? 'line-through text-slate-500' : 'text-white'}`}>
                                    {topic.name}
                                  </p>
                                  <span className={`text-[10px] px-2 py-0.5 rounded-full border flex items-center gap-1 ${typeColor}`}>
                                    {typeIcon} {topic.type}
                                  </span>
                                  <span className="text-[10px] text-slate-500 flex items-center gap-1">
                                    <Clock className="w-3 h-3" /> ~{topic.estimatedHours}h
                                  </span>
                                </div>
                                <p className="text-xs text-slate-400 leading-relaxed">{topic.description}</p>
                                {topic.resources?.length > 0 && (
                                  <div className="flex flex-wrap gap-1.5 mt-2">
                                    {topic.resources.map((r, ri) => (
                                      <span key={ri}
                                        className="text-[10px] px-2 py-0.5 bg-white/5 border border-white/10 rounded-full text-slate-400 flex items-center gap-1">
                                        <ExternalLink className="w-2.5 h-2.5" /> {r}
                                      </span>
                                    ))}
                                  </div>
                                )}
                              </div>
                            </div>
                          );
                        })}
                      </div>

                      <div className="bg-violet-500/10 border border-violet-500/20 rounded-xl p-3 flex items-start gap-2">
                        <Target className="w-4 h-4 text-violet-400 mt-0.5 flex-shrink-0" />
                        <p className="text-xs text-slate-300">
                          <strong className="text-violet-300">Milestone:</strong> {phase.milestone}
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* career outcome */}
          {generatedPath.careerOutcome && (
            <div className="mt-6 bg-gradient-to-r from-green-500/10 to-emerald-500/10 border border-green-500/20 rounded-2xl p-6">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 bg-green-500/20 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Target className="w-4 h-4 text-green-400" />
                </div>
                <div>
                  <p className="text-xs font-bold text-green-400 uppercase tracking-wider mb-1">Career Outcome</p>
                  <p className="text-slate-300 text-sm leading-relaxed">{generatedPath.careerOutcome}</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  /* ── SAVED PATHS ─────────────────────────────────────────── */
  if (view === 'saved') return (
    <div className="min-h-screen bg-slate-950 text-white">
      <div className="max-w-3xl mx-auto px-4 py-10">
        <div className="flex items-center justify-between mb-8">
          <button onClick={() => setView('form')}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors text-sm">
            <ChevronLeft className="w-4 h-4" /> Back
          </button>
          <button onClick={() => setView('form')}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 rounded-xl text-sm font-bold hover:bg-blue-500 transition-colors">
            <Plus className="w-4 h-4" /> New Path
          </button>
        </div>

        <h1 className="text-2xl font-black mb-6">Your Learning Paths</h1>

        {savedPaths.length === 0 ? (
          <div className="text-center py-20 text-slate-400">No saved paths yet.</div>
        ) : (
          <div className="space-y-4">
            {savedPaths.map(p => (
              <div key={p.id} className="bg-white/5 border border-white/10 rounded-2xl p-6">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="font-bold text-white mb-1">{p.title}</h3>
                    <p className="text-sm text-slate-400 mb-3">{p.description}</p>
                    <div className="flex gap-3 text-xs text-slate-500">
                      <span className="flex items-center gap-1"><Clock className="w-3 h-3" /> {p.estimated_weeks} weeks</span>
                      <span className="flex items-center gap-1"><Target className="w-3 h-3" /> {p.target_role}</span>
                      <span>{new Date(p.created_at).toLocaleDateString()}</span>
                    </div>
                  </div>
                  <button
                    onClick={() => {
                      setGeneratedPath({ ...p, phases: p.path_data });
                      setExpandedPhase(0);
                      setView('result');
                    }}
                    className="flex items-center gap-1.5 px-3 py-2 bg-white/10 rounded-xl text-xs font-bold hover:bg-white/15 transition-colors flex-shrink-0"
                  >
                    View <ArrowRight className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );

  /* ── FORM ────────────────────────────────────────────────── */
  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <div className="max-w-2xl mx-auto px-4 py-10">
        <Link href="/learning"
          className="flex items-center gap-2 text-slate-400 hover:text-white mb-8 transition-colors text-sm w-fit">
          <ChevronLeft className="w-4 h-4" /> Back to Learning Hub
        </Link>

        <div className="flex items-center gap-3 mb-8">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center">
            <Map className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-black">Generate Learning Path</h1>
            <p className="text-slate-400 text-sm">Groq AI creates a personalised week-by-week roadmap</p>
          </div>
        </div>

        {savedPaths.length > 0 && (
          <button onClick={() => setView('saved')}
            className="w-full flex items-center justify-between px-4 py-3 bg-white/5 border border-white/10 rounded-xl mb-6 hover:bg-white/8 transition-colors">
            <span className="text-sm text-slate-300">
              You have <strong className="text-white">{savedPaths.length}</strong> saved path{savedPaths.length > 1 ? 's' : ''}
            </span>
            <ArrowRight className="w-4 h-4 text-slate-400" />
          </button>
        )}

        {error && (
          <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/30 rounded-xl p-4 mb-6 text-red-400 text-sm">
            <AlertCircle className="w-4 h-4 flex-shrink-0" /> {error}
          </div>
        )}

        <div className="space-y-6">
          {/* Goal */}
          <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
            <label className="block text-sm font-bold text-slate-300 mb-3">
              What&apos;s your learning goal?
            </label>
            <textarea
              value={goal}
              onChange={e => setGoal(e.target.value)}
              placeholder="e.g. I want to become a full-stack developer and land a job at a tech startup within 6 months"
              rows={3}
              className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder:text-slate-500 outline-none focus:border-blue-500/50 resize-none"
            />
          </div>

          {/* Target Role */}
          <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
            <label className="block text-sm font-bold text-slate-300 mb-3">Target Role</label>
            <div className="grid grid-cols-2 gap-2">
              {ROLES.map(r => (
                <button key={r} onClick={() => setTargetRole(r)}
                  className={`px-3 py-2.5 rounded-xl text-sm font-medium transition-all text-left ${
                    targetRole === r
                      ? 'bg-blue-600 text-white border border-blue-500'
                      : 'bg-white/5 text-slate-400 border border-white/10 hover:bg-white/10 hover:text-white'
                  }`}>
                  {r}
                </button>
              ))}
            </div>
          </div>

          {/* Level + Hours */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-white/5 border border-white/10 rounded-2xl p-5">
              <label className="block text-sm font-bold text-slate-300 mb-3">Current Level</label>
              <div className="space-y-2">
                {LEVELS.map(l => (
                  <button key={l} onClick={() => setCurrentLevel(l)}
                    className={`w-full px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                      currentLevel === l
                        ? 'bg-violet-600 text-white'
                        : 'bg-white/5 text-slate-400 border border-white/10 hover:bg-white/10'
                    }`}>
                    {l}
                  </button>
                ))}
              </div>
            </div>
            <div className="bg-white/5 border border-white/10 rounded-2xl p-5">
              <label className="block text-sm font-bold text-slate-300 mb-3">Hours / Week</label>
              <div className="space-y-2">
                {HOURS.map(h => (
                  <button key={h} onClick={() => setHoursPerWeek(h)}
                    className={`w-full px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                      hoursPerWeek === h
                        ? 'bg-green-600 text-white'
                        : 'bg-white/5 text-slate-400 border border-white/10 hover:bg-white/10'
                    }`}>
                    {h} hrs
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Current Skills */}
          <div className="bg-white/5 border border-white/10 rounded-2xl p-6">
            <label className="block text-sm font-bold text-slate-300 mb-1">
              Current Skills <span className="text-slate-500 font-normal">(optional)</span>
            </label>
            <p className="text-xs text-slate-500 mb-3">Select skills you already know so AI can skip basics</p>
            <div className="flex flex-wrap gap-2">
              {SKILL_OPTIONS.map(s => (
                <button key={s} onClick={() => toggleSkill(s)}
                  className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
                    selectedSkills.includes(s)
                      ? 'bg-green-600/30 text-green-300 border border-green-500/40'
                      : 'bg-white/5 text-slate-400 border border-white/10 hover:bg-white/10'
                  }`}>
                  {selectedSkills.includes(s) ? '✓ ' : ''}{s}
                </button>
              ))}
            </div>
          </div>

          <button
            onClick={generatePath}
            className="w-full py-4 bg-gradient-to-r from-blue-600 to-violet-600 rounded-xl font-bold text-lg hover:opacity-90 transition-all flex items-center justify-center gap-2 shadow-lg shadow-blue-500/20"
          >
            <Sparkles className="w-5 h-5" /> Generate My Learning Path
          </button>
        </div>
      </div>
    </div>
  );
}
