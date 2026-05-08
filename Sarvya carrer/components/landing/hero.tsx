'use client';

import Link from 'next/link';
import { ArrowRight, Sparkles } from 'lucide-react';

export function Hero() {
  return (
    <section className="section-container flex flex-col items-center justify-center min-h-[90vh] relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 -z-10 overflow-hidden">
        <div className="absolute top-20 left-10 w-72 h-72 bg-blue-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-20 right-10 w-72 h-72 bg-violet-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
        <div className="absolute top-1/2 left-1/2 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2" />
      </div>

      <div className="max-w-4xl mx-auto text-center z-10">
        {/* Badge */}
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full glass mb-8">
          <Sparkles className="w-4 h-4 text-blue-400" />
          <span className="text-sm font-medium text-slate-200">Introducing Engineering Career OS</span>
        </div>

        {/* Main headline */}
        <h1 className="text-5xl md:text-7xl font-bold mb-6 text-white leading-tight">
          Master Your{' '}
          <span className="gradient-text">Tech Career</span>
        </h1>

        {/* Subheadline */}
        <p className="text-xl md:text-2xl text-slate-300 mb-8 max-w-2xl mx-auto leading-relaxed">
          Transform into a world-class engineer with AI-powered learning, personalized roadmaps, and industry-leading tools
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center mb-16">
          <Link
            href="/dashboard"
            className="inline-flex items-center justify-center gap-2 px-8 py-4 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white rounded-lg font-semibold transition-all duration-300 glow-blue transform hover:scale-105"
          >
            Start Learning
            <ArrowRight className="w-5 h-5" />
          </Link>
          <Link
            href="#features"
            className="inline-flex items-center justify-center px-8 py-4 glass-hover text-white rounded-lg font-semibold"
          >
            Explore Features
          </Link>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-8 max-w-md mx-auto">
          <div className="text-center">
            <div className="text-3xl font-bold gradient-text">500+</div>
            <div className="text-sm text-slate-400">Courses</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold gradient-text">50K+</div>
            <div className="text-sm text-slate-400">Students</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold gradient-text">95%</div>
            <div className="text-sm text-slate-400">Success Rate</div>
          </div>
        </div>
      </div>
    </section>
  );
}
