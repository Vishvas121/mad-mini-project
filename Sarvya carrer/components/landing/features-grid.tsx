'use client';

import { features } from '@/lib/constants';
import { BookOpen, Brain, FileText, Zap, Map, Users } from 'lucide-react';

const iconMap = {
  BookOpen: <BookOpen className="w-8 h-8" />,
  Brain: <Brain className="w-8 h-8" />,
  FileText: <FileText className="w-8 h-8" />,
  Zap: <Zap className="w-8 h-8" />,
  Map: <Map className="w-8 h-8" />,
  Users: <Users className="w-8 h-8" />,
};

export function FeaturesGrid() {
  return (
    <section id="features" className="section-container">
      <div className="max-w-6xl mx-auto">
        {/* Section header */}
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-bold mb-4 text-white">
            Everything You Need to{' '}
            <span className="gradient-text">Succeed</span>
          </h2>
          <p className="text-lg text-slate-400 max-w-2xl mx-auto">
            A complete platform designed to accelerate your engineering career with cutting-edge tools and resources
          </p>
        </div>

        {/* Features grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature) => (
            <div
              key={feature.id}
              className="group glass-hover p-6 flex flex-col h-full hover:glow-blue relative overflow-hidden"
            >
              {/* Gradient accent */}
              <div className={`absolute -inset-1 bg-gradient-to-r ${feature.color} opacity-0 group-hover:opacity-20 blur transition duration-300 -z-10`} />
              
              {/* Icon */}
              <div className={`w-12 h-12 rounded-lg bg-gradient-to-r ${feature.color} bg-opacity-20 flex items-center justify-center mb-4 text-blue-300 group-hover:text-blue-200`}>
                {iconMap[feature.icon as keyof typeof iconMap]}
              </div>

              {/* Content */}
              <h3 className="text-xl font-bold text-white mb-2">{feature.title}</h3>
              <p className="text-slate-400 flex-grow">{feature.description}</p>
              
              {/* Arrow */}
              <div className="mt-4 flex items-center text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <span className="text-sm font-semibold">Learn more</span>
                <span className="ml-2">→</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
