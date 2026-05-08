'use client';

import { engineeringBranches } from '@/lib/constants';
import { Code2, Server, Layers, Cloud, Database, Cpu } from 'lucide-react';
import Link from 'next/link';

const iconMap = {
  Code2: <Code2 className="w-6 h-6" />,
  Server: <Server className="w-6 h-6" />,
  Layers: <Layers className="w-6 h-6" />,
  Cloud: <Cloud className="w-6 h-6" />,
  Database: <Database className="w-6 h-6" />,
  Cpu: <Cpu className="w-6 h-6" />,
};

export function BranchesSection() {
  return (
    <section className="section-container">
      <div className="max-w-6xl mx-auto">
        {/* Section header */}
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-bold mb-4 text-white">
            Choose Your{' '}
            <span className="gradient-text">Engineering Path</span>
          </h2>
          <p className="text-lg text-slate-400 max-w-2xl mx-auto">
            Specialization roadmaps curated by industry experts to match your career goals
          </p>
        </div>

        {/* Branches grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {engineeringBranches.map((branch) => (
            <Link key={branch.id} href="/courses">
              <div className="group glass-hover p-6 h-full cursor-pointer transform hover:scale-105 transition-all duration-300">
                {/* Icon */}
                <div className="w-12 h-12 rounded-lg bg-gradient-to-r from-violet-500 to-blue-500 bg-opacity-20 flex items-center justify-center mb-4 text-violet-300 group-hover:text-violet-200 group-hover:glow-violet">
                  {iconMap[branch.icon as keyof typeof iconMap]}
                </div>

                {/* Content */}
                <h3 className="text-xl font-bold text-white mb-2">{branch.title}</h3>
                <p className="text-slate-400 text-sm mb-4">{branch.description}</p>

                {/* Meta info */}
                <div className="flex flex-wrap gap-2 mb-4">
                  <span className="px-3 py-1 text-xs font-medium text-slate-300 bg-blue-500/10 border border-blue-500/20 rounded-full">
                    {branch.courses} courses
                  </span>
                  <span className="px-3 py-1 text-xs font-medium text-slate-300 bg-violet-500/10 border border-violet-500/20 rounded-full">
                    {branch.difficulty}
                  </span>
                </div>

                {/* Duration */}
                <div className="flex items-center justify-between pt-4 border-t border-white/5">
                  <span className="text-sm text-slate-400">{branch.duration}</span>
                  <span className="text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity duration-300">→</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}
