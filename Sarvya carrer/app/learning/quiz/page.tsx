'use client';

import { useState, useEffect, useCallback, useRef, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import {
  Brain, Zap, CheckCircle2, XCircle, ArrowRight,
  Clock, Trophy, RotateCcw, ChevronLeft, Sparkles,
  Target, TrendingUp, AlertCircle,
} from 'lucide-react';
import Link from 'next/link';

const TOPICS = [
  'JavaScript', 'TypeScript', 'React', 'Next.js', 'Node.js',
  'Python', 'SQL', 'PostgreSQL', 'MongoDB', 'Redis',
  'Docker', 'Kubernetes', 'AWS', 'System Design',
  'Data Structures', 'Algorithms', 'Machine Learning',
  'REST APIs', 'GraphQL', 'Git', 'Linux', 'Rust', 'Go',
];

const DIFFICULTIES = ['Beginner', 'Intermediate', 'Advanced'];

interface Question {
  id: number;
  question: string;
  options: string[];
  correct: string;
  explanation: string;
  difficulty: string;
  topic: string;
}

type Phase = 'setup' | 'loading' | 'quiz' | 'result';

function QuizContent() {
  const searchParams = useSearchParams();
  const preselectedTopic = searchParams.get('topic') || '';

  const [phase, setPhase] = useState<Phase>('setup');
  const [topic, setTopic] = useState(preselectedTopic);
  const [difficulty, setDifficulty] = useState('Intermediate');
  const [questions, setQuestions] = useState<Question[]>([]);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [adaptedDifficulty, setAdaptedDifficulty] = useState('');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [showExplanation, setShowExplanation] = useState(false);
  const [answers, setAnswers] = useState<any[]>([]);
  const [timeLeft, setTimeLeft] = useState(30);
  const [questionStartTime, setQuestionStartTime] = useState(Date.now());
  const [quizStartTime, setQuizStartTime] = useState(Date.now());
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  // Timer per question
  useEffect(() => {
    if (phase !== 'quiz' || showExplanation) return;
    setTimeLeft(30);
    setQuestionStartTime(Date.now());

    timerRef.current = setInterval(() => {
      setTimeLeft((t) => {
        if (t <= 1) {
          clearInterval(timerRef.current!);
          handleTimeout();
          return 0;
        }
        return t - 1;
      });
    }, 1000);

    return () => clearInterval(timerRef.current!);
  }, [currentIndex, phase, showExplanation]);

  const handleTimeout = useCallback(() => {
    if (selectedAnswer !== null) return;
    const q = questions[currentIndex];
    const timeTaken = Math.round((Date.now() - questionStartTime) / 1000);
    setAnswers((prev) => [
      ...prev,
      {
        question: q.question,
        selected: null,
        correct: q.correct,
        is_correct: false,
        timeTaken,
        difficulty: q.difficulty,
      },
    ]);
    setSelectedAnswer('__timeout__');
    setShowExplanation(true);
  }, [currentIndex, questions, questionStartTime, selectedAnswer]);

  const startQuiz = async () => {
    if (!topic) { setError('Please select a topic'); return; }
    setError('');
    setPhase('loading');

    try {
      const res = await fetch('/api/adaptive-quiz', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ topic, difficulty, questionCount: 5 }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error);

      setQuestions(data.questions);
      setSessionId(data.sessionId);
      setAdaptedDifficulty(data.adaptedDifficulty);
      setCurrentIndex(0);
      setAnswers([]);
      setSelectedAnswer(null);
      setShowExplanation(false);
      setQuizStartTime(Date.now());
      setPhase('quiz');
    } catch (e: any) {
      setError(e.message || 'Failed to generate quiz');
      setPhase('setup');
    }
  };

  const handleAnswer = (option: string) => {
    if (selectedAnswer !== null) return;
    clearInterval(timerRef.current!);

    const q = questions[currentIndex];
    const isCorrect = option === q.correct;
    const timeTaken = Math.round((Date.now() - questionStartTime) / 1000);

    setSelectedAnswer(option);
    setShowExplanation(true);
    setAnswers((prev) => [
      ...prev,
      {
        question: q.question,
        selected: option,
        correct: q.correct,
        is_correct: isCorrect,
        timeTaken,
        difficulty: q.difficulty,
      },
    ]);
  };

  const nextQuestion = () => {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((i) => i + 1);
      setSelectedAnswer(null);
      setShowExplanation(false);
    } else {
      submitQuiz();
    }
  };

  const submitQuiz = async () => {
    setSubmitting(true);
    const totalTime = Math.round((Date.now() - quizStartTime) / 1000);

    try {
      const res = await fetch('/api/adaptive-quiz', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, answers, timeTaken: totalTime }),
      });
      const data = await res.json();
      setResult(data);
    } catch {
      setResult({ scorePercentage: 0, xpEarned: 0, correct: 0, total: questions.length });
    }
    setSubmitting(false);
    setPhase('result');
  };

  const resetQuiz = () => {
    setPhase('setup');
    setQuestions([]);
    setAnswers([]);
    setSelectedAnswer(null);
    setShowExplanation(false);
    setResult(null);
    setCurrentIndex(0);
  };

  const currentQ = questions[currentIndex];
  const progress = questions.length > 0 ? ((currentIndex + (showExplanation ? 1 : 0)) / questions.length) * 100 : 0;

  // ── SETUP PHASE ──
  if (phase === 'setup') {
    return (
      <div className="min-h-screen bg-slate-950 text-white">
        <div className="max-w-2xl mx-auto px-4 py-12">
          <Link href="/learning" className="flex items-center gap-2 text-slate-400 hover:text-white mb-8 transition-colors text-sm">
            <ChevronLeft className="w-4 h-4" /> Back to Learning Hub
          </Link>

          <div className="flex items-center gap-3 mb-8">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center">
              <Zap className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-black">Adaptive Quiz</h1>
              <p className="text-slate-400 text-sm">AI adjusts difficulty based on your answers</p>
            </div>
          </div>

          {error && (
            <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/30 rounded-xl p-4 mb-6 text-red-400 text-sm">
              <AlertCircle className="w-4 h-4 flex-shrink-0" /> {error}
            </div>
          )}

          <div className="bg-white/5 border border-white/10 rounded-2xl p-6 space-y-6">
            {/* Topic */}
            <div>
              <label className="block text-sm font-bold text-slate-300 mb-3">Select Topic</label>
              <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                {TOPICS.map((t) => (
                  <button
                    key={t}
                    onClick={() => setTopic(t)}
                    className={`px-3 py-2 rounded-xl text-xs font-semibold transition-all ${
                      topic === t
                        ? 'bg-violet-600 text-white border border-violet-500'
                        : 'bg-white/5 text-slate-400 border border-white/10 hover:bg-white/10 hover:text-white'
                    }`}
                  >
                    {t}
                  </button>
                ))}
              </div>
            </div>

            {/* Difficulty */}
            <div>
              <label className="block text-sm font-bold text-slate-300 mb-3">Starting Difficulty</label>
              <div className="flex gap-3">
                {DIFFICULTIES.map((d) => (
                  <button
                    key={d}
                    onClick={() => setDifficulty(d)}
                    className={`flex-1 py-2.5 rounded-xl text-sm font-bold transition-all ${
                      difficulty === d
                        ? d === 'Beginner'
                          ? 'bg-green-600 text-white'
                          : d === 'Intermediate'
                          ? 'bg-yellow-600 text-white'
                          : 'bg-red-600 text-white'
                        : 'bg-white/5 text-slate-400 border border-white/10 hover:bg-white/10'
                    }`}
                  >
                    {d}
                  </button>
                ))}
              </div>
            </div>

            <div className="bg-violet-500/10 border border-violet-500/20 rounded-xl p-4 text-sm text-slate-300">
              <div className="flex items-start gap-2">
                <Brain className="w-4 h-4 text-violet-400 mt-0.5 flex-shrink-0" />
                <p>AI will generate 5 questions and <strong className="text-white">automatically adjust difficulty</strong> based on your score. Score 80%+ to level up, below 40% to level down.</p>
              </div>
            </div>

            <button
              onClick={startQuiz}
              disabled={!topic}
              className="w-full py-4 bg-gradient-to-r from-violet-600 to-blue-600 rounded-xl font-bold text-lg hover:opacity-90 transition-all disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <Zap className="w-5 h-5" /> Generate Quiz with AI
            </button>
          </div>
        </div>
      </div>
    );
  }

  // ── LOADING PHASE ──
  if (phase === 'loading') {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center mx-auto mb-6 animate-pulse">
            <Brain className="w-10 h-10 text-white" />
          </div>
          <h2 className="text-2xl font-black text-white mb-2">Generating Your Quiz</h2>
          <p className="text-slate-400">AI is crafting {difficulty} questions on {topic}…</p>
          <div className="flex gap-1.5 justify-center mt-6">
            {[0, 1, 2].map((i) => (
              <div key={i} className="w-2 h-2 bg-violet-500 rounded-full animate-bounce" style={{ animationDelay: `${i * 0.15}s` }} />
            ))}
          </div>
        </div>
      </div>
    );
  }

  // ── RESULT PHASE ──
  if (phase === 'result') {
    const score = result?.scorePercentage || 0;
    const grade = score >= 80 ? '🏆 Excellent!' : score >= 60 ? '✅ Good Job!' : score >= 40 ? '📚 Keep Practicing' : '💪 Don\'t Give Up!';
    const gradeColor = score >= 80 ? 'text-yellow-400' : score >= 60 ? 'text-green-400' : score >= 40 ? 'text-blue-400' : 'text-orange-400';

    return (
      <div className="min-h-screen bg-slate-950 text-white">
        <div className="max-w-2xl mx-auto px-4 py-12">
          <div className="bg-white/5 border border-white/10 rounded-3xl p-8 text-center">
            <div className="w-24 h-24 rounded-3xl bg-gradient-to-br from-violet-500 to-blue-600 flex items-center justify-center mx-auto mb-6">
              <Trophy className="w-12 h-12 text-white" />
            </div>
            <h2 className={`text-3xl font-black mb-2 ${gradeColor}`}>{grade}</h2>
            <p className="text-slate-400 mb-8">Quiz on <strong className="text-white">{topic}</strong> ({adaptedDifficulty})</p>

            <div className="grid grid-cols-3 gap-4 mb-8">
              <div className="bg-white/5 rounded-2xl p-4">
                <p className="text-3xl font-black text-white">{score}%</p>
                <p className="text-xs text-slate-400 mt-1">Score</p>
              </div>
              <div className="bg-white/5 rounded-2xl p-4">
                <p className="text-3xl font-black text-green-400">{result?.correct}/{result?.total}</p>
                <p className="text-xs text-slate-400 mt-1">Correct</p>
              </div>
              <div className="bg-white/5 rounded-2xl p-4">
                <p className="text-3xl font-black text-yellow-400">+{result?.xpEarned}</p>
                <p className="text-xs text-slate-400 mt-1">XP Earned</p>
              </div>
            </div>

            {/* Answer Review */}
            <div className="text-left space-y-3 mb-8">
              <h3 className="font-bold text-slate-300 text-sm uppercase tracking-wider">Answer Review</h3>
              {answers.map((a, i) => (
                <div key={i} className={`flex items-start gap-3 p-3 rounded-xl ${a.is_correct ? 'bg-green-500/10 border border-green-500/20' : 'bg-red-500/10 border border-red-500/20'}`}>
                  {a.is_correct
                    ? <CheckCircle2 className="w-4 h-4 text-green-400 mt-0.5 flex-shrink-0" />
                    : <XCircle className="w-4 h-4 text-red-400 mt-0.5 flex-shrink-0" />}
                  <div className="min-w-0">
                    <p className="text-sm text-white font-medium line-clamp-2">{a.question}</p>
                    {!a.is_correct && (
                      <p className="text-xs text-slate-400 mt-1">Correct: <span className="text-green-400">{a.correct}</span></p>
                    )}
                  </div>
                </div>
              ))}
            </div>

            <div className="flex gap-3">
              <button
                onClick={resetQuiz}
                className="flex-1 flex items-center justify-center gap-2 py-3 bg-white/10 border border-white/20 rounded-xl font-bold hover:bg-white/15 transition-all"
              >
                <RotateCcw className="w-4 h-4" /> Try Again
              </button>
              <Link
                href="/learning/analytics"
                className="flex-1 flex items-center justify-center gap-2 py-3 bg-gradient-to-r from-violet-600 to-blue-600 rounded-xl font-bold hover:opacity-90 transition-all"
              >
                <TrendingUp className="w-4 h-4" /> View Progress
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ── QUIZ PHASE ──
  if (!currentQ) return null;

  const timerPercent = (timeLeft / 30) * 100;
  const timerColor = timeLeft > 15 ? 'bg-green-500' : timeLeft > 7 ? 'bg-yellow-500' : 'bg-red-500';

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <div className="max-w-2xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-violet-500/20 flex items-center justify-center">
              <Brain className="w-4 h-4 text-violet-400" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-semibold">{topic} · {adaptedDifficulty}</p>
              <p className="text-sm font-bold text-white">Question {currentIndex + 1} of {questions.length}</p>
            </div>
          </div>
          <div className="flex items-center gap-2 bg-white/5 border border-white/10 rounded-xl px-3 py-2">
            <Clock className={`w-4 h-4 ${timeLeft <= 7 ? 'text-red-400 animate-pulse' : 'text-slate-400'}`} />
            <span className={`text-sm font-bold tabular-nums ${timeLeft <= 7 ? 'text-red-400' : 'text-white'}`}>{timeLeft}s</span>
          </div>
        </div>

        {/* Progress bar */}
        <div className="h-1.5 bg-white/10 rounded-full mb-6 overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-violet-500 to-blue-500 rounded-full transition-all duration-300"
            style={{ width: `${progress}%` }}
          />
        </div>

        {/* Timer bar */}
        <div className="h-1 bg-white/10 rounded-full mb-8 overflow-hidden">
          <div
            className={`h-full ${timerColor} rounded-full transition-all duration-1000`}
            style={{ width: `${timerPercent}%` }}
          />
        </div>

        {/* Question */}
        <div className="bg-white/5 border border-white/10 rounded-2xl p-6 mb-6">
          <p className="text-lg font-semibold text-white leading-relaxed">{currentQ.question}</p>
        </div>

        {/* Options */}
        <div className="space-y-3 mb-6">
          {currentQ.options.map((option, i) => {
            let style = 'bg-white/5 border-white/10 text-slate-300 hover:bg-white/10 hover:border-white/20 hover:text-white';
            if (selectedAnswer !== null) {
              if (option === currentQ.correct) style = 'bg-green-500/20 border-green-500/50 text-green-300';
              else if (option === selectedAnswer && option !== currentQ.correct) style = 'bg-red-500/20 border-red-500/50 text-red-300';
              else style = 'bg-white/5 border-white/5 text-slate-500';
            }

            return (
              <button
                key={i}
                onClick={() => handleAnswer(option)}
                disabled={selectedAnswer !== null}
                className={`w-full text-left px-5 py-4 rounded-xl border font-medium transition-all ${style} disabled:cursor-default`}
              >
                <span className="text-xs font-bold mr-3 opacity-50">{String.fromCharCode(65 + i)}</span>
                {option}
              </button>
            );
          })}
        </div>

        {/* Explanation */}
        {showExplanation && (
          <div className="bg-blue-500/10 border border-blue-500/20 rounded-xl p-4 mb-6">
            <div className="flex items-start gap-2">
              <Sparkles className="w-4 h-4 text-blue-400 mt-0.5 flex-shrink-0" />
              <div>
                <p className="text-xs font-bold text-blue-400 uppercase tracking-wider mb-1">Explanation</p>
                <p className="text-sm text-slate-300 leading-relaxed">{currentQ.explanation}</p>
              </div>
            </div>
          </div>
        )}

        {showExplanation && (
          <button
            onClick={nextQuestion}
            disabled={submitting}
            className="w-full py-4 bg-gradient-to-r from-violet-600 to-blue-600 rounded-xl font-bold flex items-center justify-center gap-2 hover:opacity-90 transition-all disabled:opacity-60"
          >
            {submitting ? (
              <>Submitting…</>
            ) : currentIndex < questions.length - 1 ? (
              <><ArrowRight className="w-4 h-4" /> Next Question</>
            ) : (
              <><Trophy className="w-4 h-4" /> See Results</>
            )}
          </button>
        )}
      </div>
    </div>
  );
}

export default function QuizPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-violet-500 border-t-transparent rounded-full animate-spin" />
      </div>
    }>
      <QuizContent />
    </Suspense>
  );
}
