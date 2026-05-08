'use client';

import { ExternalLink, Code2, Trophy, BookOpen, Zap } from 'lucide-react';
import { useState } from 'react';

const PLATFORMS = [
  {
    key: 'neetcode',
    label: 'NeetCode',
    logo: 'https://neetcode.io/favicon.ico',
    logoBg: 'bg-white',
    url: 'https://neetcode.io/practice',
    desc: 'Structured DSA roadmap with video explanations — best for interview prep',
    color: 'from-green-500 to-emerald-500',
    badge: 'bg-green-500/20 text-green-300 border border-green-500/30',
    features: ['Structured roadmap', 'Video solutions', 'Pattern-based learning', 'Company-wise problems'],
  },
  {
    key: 'leetcode',
    label: 'LeetCode',
    logo: 'https://leetcode.com/favicon-32x32.png',
    logoBg: 'bg-[#1a1a2e]',
    url: 'https://leetcode.com/problemset/',
    desc: 'World\'s largest coding interview platform — 3000+ problems',
    color: 'from-orange-500 to-yellow-500',
    badge: 'bg-orange-500/20 text-orange-300 border border-orange-500/30',
    features: ['3000+ problems', 'Company tags', 'Contest mode', 'Discussion forums'],
  },
  {
    key: 'codechef',
    label: 'CodeChef',
    logo: 'https://www.codechef.com/favicon.ico',
    logoBg: 'bg-white',
    url: 'https://www.codechef.com/practice',
    desc: 'Competitive programming with monthly contests and practice problems',
    color: 'from-amber-500 to-orange-500',
    badge: 'bg-amber-500/20 text-amber-300 border border-amber-500/30',
    features: ['Monthly contests', 'Rating system', 'Practice tracks', 'Indian platform'],
  },
  {
    key: 'codeforces',
    label: 'Codeforces',
    logo: 'https://codeforces.com/favicon-32x32.png',
    logoBg: 'bg-white',
    url: 'https://codeforces.com/problemset',
    desc: 'Top competitive programming platform with frequent rated contests',
    color: 'from-blue-500 to-cyan-500',
    badge: 'bg-blue-500/20 text-blue-300 border border-blue-500/30',
    features: ['Rated contests', 'Div 1/2/3/4', 'Educational rounds', 'Global ranking'],
  },
  {
    key: 'hackerrank',
    label: 'HackerRank',
    logo: 'https://www.hackerrank.com/favicon.ico',
    logoBg: 'bg-[#1ba94c]',
    url: 'https://www.hackerrank.com/domains/algorithms',
    desc: 'Skill-based assessments and coding challenges used by recruiters',
    color: 'from-emerald-500 to-teal-500',
    badge: 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30',
    features: ['Skill certificates', 'Recruiter visibility', 'Domain tracks', 'Interview kit'],
  },
  {
    key: 'geeksforgeeks',
    label: 'GeeksforGeeks',
    logo: 'https://www.geeksforgeeks.org/favicon.ico',
    logoBg: 'bg-white',
    url: 'https://practice.geeksforgeeks.org/',
    desc: 'Practice problems with detailed explanations and company-wise questions',
    color: 'from-lime-500 to-green-500',
    badge: 'bg-lime-500/20 text-lime-300 border border-lime-500/30',
    features: ['Company-wise', 'Topic-wise', 'Interview experience', 'Detailed solutions'],
  },
];

const DSA_TOPICS = [
  { name: 'Arrays & Hashing', count: 9, url: 'https://neetcode.io/roadmap', difficulty: 'Easy' },
  { name: 'Two Pointers', count: 5, url: 'https://neetcode.io/roadmap', difficulty: 'Easy' },
  { name: 'Sliding Window', count: 6, url: 'https://neetcode.io/roadmap', difficulty: 'Medium' },
  { name: 'Stack', count: 7, url: 'https://neetcode.io/roadmap', difficulty: 'Medium' },
  { name: 'Binary Search', count: 7, url: 'https://neetcode.io/roadmap', difficulty: 'Medium' },
  { name: 'Linked List', count: 11, url: 'https://neetcode.io/roadmap', difficulty: 'Medium' },
  { name: 'Trees', count: 15, url: 'https://neetcode.io/roadmap', difficulty: 'Medium' },
  { name: 'Graphs', count: 13, url: 'https://neetcode.io/roadmap', difficulty: 'Hard' },
  { name: 'Dynamic Programming', count: 23, url: 'https://neetcode.io/roadmap', difficulty: 'Hard' },
  { name: 'Backtracking', count: 9, url: 'https://neetcode.io/roadmap', difficulty: 'Hard' },
  { name: 'Heap / Priority Queue', count: 7, url: 'https://neetcode.io/roadmap', difficulty: 'Hard' },
  { name: 'Tries', count: 3, url: 'https://neetcode.io/roadmap', difficulty: 'Hard' },
];

const diffColor: Record<string, string> = {
  Easy: 'text-emerald-400 bg-emerald-500/10',
  Medium: 'text-yellow-400 bg-yellow-500/10',
  Hard: 'text-red-400 bg-red-500/10',
};

export default function PracticePage() {
  const [activeSection, setActiveSection] = useState<'platforms' | 'topics'>('platforms');

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-950 to-black">
      {/* Header */}
      <div className="border-b border-white/10 bg-slate-900/50 backdrop-blur-xl">
        <div className="max-w-7xl mx-auto px-4 md:px-8 py-10">
          <div className="flex items-center gap-4 mb-2">
            <div className="w-12 h-12 rounded-2xl bg-green-500/20 flex items-center justify-center">
              <Code2 className="w-6 h-6 text-green-400" />
            </div>
            <div>
              <h1 className="text-4xl md:text-5xl font-bold text-white">
                Practice & <span className="text-transparent bg-clip-text bg-gradient-to-r from-green-400 to-emerald-400">DSA</span>
              </h1>
              <p className="text-slate-400 mt-1">Ace your coding interviews with the best practice platforms</p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 py-8 space-y-8">
        {/* Section tabs */}
        <div className="flex gap-2 border-b border-white/10">
          {([['platforms', 'Platforms'], ['topics', 'DSA Topics']] as const).map(([key, label]) => (
            <button key={key} onClick={() => setActiveSection(key)}
              className={`px-5 py-2.5 font-semibold text-sm rounded-t-lg transition-all duration-200 ${activeSection === key ? 'bg-green-500/20 text-green-300 border-b-2 border-green-400' : 'text-slate-400 hover:text-white'}`}>
              {label}
            </button>
          ))}
        </div>

        {/* Platforms */}
        {activeSection === 'platforms' && (
          <div className="space-y-6">
            {/* Featured: NeetCode + LeetCode */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {PLATFORMS.slice(0, 2).map(p => (
                <div key={p.key} className={`relative overflow-hidden bg-gradient-to-br ${p.color} p-0.5 rounded-2xl`}>
                  <div className="bg-slate-900 rounded-2xl p-6 h-full">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <div className={`w-12 h-12 rounded-xl ${p.logoBg} flex items-center justify-center p-1.5 flex-shrink-0`}>
                          <img src={p.logo} alt={p.label} className="w-full h-full object-contain" />
                        </div>
                        <div>
                          <h2 className="text-xl font-bold text-white">{p.label}</h2>
                          <p className="text-slate-400 text-sm mt-0.5">{p.desc}</p>
                        </div>
                      </div>
                    </div>
                    <div className="flex flex-wrap gap-2 mb-5">
                      {p.features.map(f => (
                        <span key={f} className="flex items-center gap-1 px-2.5 py-1 bg-white/5 text-slate-300 rounded-full text-xs">
                          <Zap className="w-3 h-3 text-yellow-400" />{f}
                        </span>
                      ))}
                    </div>
                    <a href={p.url} target="_blank" rel="noopener noreferrer"
                      className={`flex items-center justify-center gap-2 w-full py-3 bg-gradient-to-r ${p.color} text-white rounded-xl font-bold text-sm hover:opacity-90 transition-opacity`}>
                      Open {p.label} <ExternalLink className="w-4 h-4" />
                    </a>
                  </div>
                </div>
              ))}
            </div>

            {/* Other platforms */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              {PLATFORMS.slice(2).map(p => (
                <a key={p.key} href={p.url} target="_blank" rel="noopener noreferrer"
                  className="group flex flex-col gap-3 p-5 bg-white/5 border border-white/10 hover:border-white/20 rounded-2xl transition-all hover:bg-white/[0.07]">
                  <div className="flex items-center justify-between">
                    <div className={`w-10 h-10 rounded-xl ${p.logoBg} flex items-center justify-center p-1.5 flex-shrink-0`}>
                      <img src={p.logo} alt={p.label} className="w-full h-full object-contain" />
                    </div>
                    <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-white transition-colors" />
                  </div>
                  <div>
                    <h3 className="text-white font-bold">{p.label}</h3>
                    <p className="text-slate-400 text-xs mt-1 line-clamp-2">{p.desc}</p>
                  </div>
                  <span className={`self-start px-2.5 py-1 rounded-full text-xs font-semibold ${p.badge}`}>Visit →</span>
                </a>
              ))}
            </div>
          </div>
        )}

        {/* DSA Topics */}
        {activeSection === 'topics' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <p className="text-slate-400 text-sm">NeetCode 150 — the most important patterns for coding interviews</p>
              <a href="https://neetcode.io/roadmap" target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2 bg-green-500/20 text-green-300 border border-green-500/30 rounded-lg text-sm font-semibold hover:bg-green-500/30 transition-colors">
                Full Roadmap <ExternalLink className="w-3.5 h-3.5" />
              </a>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {DSA_TOPICS.map((topic, i) => (
                <a key={topic.name} href={topic.url} target="_blank" rel="noopener noreferrer"
                  className="group flex items-center justify-between p-4 bg-white/5 border border-white/10 hover:border-green-500/30 rounded-xl transition-all hover:bg-white/[0.07]">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-lg bg-white/5 flex items-center justify-center text-slate-400 text-sm font-bold">{i + 1}</span>
                    <div>
                      <p className="text-white font-semibold text-sm">{topic.name}</p>
                      <p className="text-slate-500 text-xs">{topic.count} problems</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${diffColor[topic.difficulty]}`}>{topic.difficulty}</span>
                    <ExternalLink className="w-3.5 h-3.5 text-slate-600 group-hover:text-green-400 transition-colors" />
                  </div>
                </a>
              ))}
            </div>

            {/* Quick links */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4 border-t border-white/10">
              {[
                { label: 'LeetCode Top 150', url: 'https://leetcode.com/studyplan/top-interview-150/', logo: 'https://leetcode.com/favicon-32x32.png', logoBg: 'bg-[#1a1a2e]', desc: 'Must-do interview problems' },
                { label: 'NeetCode 250', url: 'https://neetcode.io/practice', logo: 'https://neetcode.io/favicon.ico', logoBg: 'bg-white', desc: 'Extended problem set' },
                { label: 'Blind 75', url: 'https://leetcode.com/discuss/general-discussion/460599/blind-75-leetcode-questions', logo: 'https://leetcode.com/favicon-32x32.png', logoBg: 'bg-[#1a1a2e]', desc: 'Classic 75 problems' },
              ].map(l => (
                <a key={l.label} href={l.url} target="_blank" rel="noopener noreferrer"
                  className="flex items-center gap-3 p-4 bg-white/5 border border-white/10 hover:border-white/20 rounded-xl transition-all group">
                  <div className={`w-9 h-9 rounded-lg ${l.logoBg} flex items-center justify-center p-1.5 flex-shrink-0`}>
                    <img src={l.logo} alt={l.label} className="w-full h-full object-contain" />
                  </div>
                  <div>
                    <p className="text-white font-semibold text-sm">{l.label}</p>
                    <p className="text-slate-500 text-xs">{l.desc}</p>
                  </div>
                  <ExternalLink className="w-4 h-4 text-slate-600 group-hover:text-white transition-colors ml-auto" />
                </a>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
