'use client';

import { pricingPlans } from '@/lib/constants';
import Link from 'next/link';
import { Check } from 'lucide-react';

export function PricingSection() {
  return (
    <section className="section-container">
      <div className="max-w-6xl mx-auto">
        {/* Section header */}
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-bold mb-4 text-white">
            Simple, Transparent{' '}
            <span className="gradient-text">Pricing</span>
          </h2>
          <p className="text-lg text-slate-400 max-w-2xl mx-auto">
            Start free and scale as you grow. Choose the plan that fits your needs
          </p>
        </div>

        {/* Pricing cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-2xl mx-auto">
          {pricingPlans.map((plan) => (
            <div
              key={plan.id}
              className={`relative p-8 rounded-2xl transition-all duration-300 ${
                plan.highlighted
                  ? 'glass-lg neon-border-violet bg-gradient-to-br from-violet-500/10 to-blue-500/5 ring-2 ring-violet-400/30 transform scale-105'
                  : 'glass-lg neon-border-blue'
              }`}
            >
              {/* Popular badge */}
              {plan.highlighted && (
                <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1 bg-gradient-to-r from-violet-500 to-blue-500 text-white text-sm font-semibold rounded-full">
                  Most Popular
                </div>
              )}

              {/* Plan name and price */}
              <h3 className="text-2xl font-bold text-white mb-2">{plan.name}</h3>
              <p className="text-slate-400 text-sm mb-6">{plan.description}</p>

              {/* Price */}
              <div className="mb-8">
                <span className="text-4xl font-bold text-white">${plan.price}</span>
                <span className="text-slate-400 ml-2">/month</span>
              </div>

              {/* CTA Button */}
              <Link
                href="/dashboard"
                className={`w-full py-3 rounded-lg font-semibold mb-8 transition-all duration-300 ${
                  plan.highlighted
                    ? 'bg-gradient-to-r from-violet-500 to-blue-500 text-white hover:shadow-lg hover:shadow-violet-500/50'
                    : 'bg-white/10 border border-white/20 text-white hover:bg-white/20'
                }`}
              >
                Get Started
              </Link>

              {/* Features list */}
              <div className="space-y-4">
                {plan.features.map((feature, index) => (
                  <div key={index} className="flex items-start gap-3">
                    <Check className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
                    <span className="text-slate-300">{feature}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Additional info */}
        <div className="text-center mt-16">
          <p className="text-slate-400">
            All plans include a 7-day free trial. No credit card required.
          </p>
        </div>
      </div>
    </section>
  );
}
