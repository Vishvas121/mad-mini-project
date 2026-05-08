'use client';

import Link from 'next/link';
import { Github, Twitter, Linkedin, Mail } from 'lucide-react';

export function Footer() {
  return (
    <footer className="border-t border-white/10 bg-gradient-to-b from-transparent to-blue-950/20">
      <div className="section-container">
        <div className="max-w-6xl mx-auto">
          {/* Main footer content */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-12">
            {/* Brand */}
            <div>
              <h3 className="text-xl font-bold text-white mb-4">Engineering Career OS</h3>
              <p className="text-slate-400 text-sm">
                Transform your engineering career with AI-powered learning and industry tools.
              </p>
            </div>

            {/* Product links */}
            <div>
              <h4 className="text-sm font-semibold text-white mb-4 uppercase tracking-wide">Product</h4>
              <ul className="space-y-2">
                {['Courses', 'AI Assistant', 'Resume Builder', 'Roadmaps'].map((item) => (
                  <li key={item}>
                    <Link href="#" className="text-slate-400 hover:text-white transition-colors text-sm">
                      {item}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>

            {/* Company links */}
            <div>
              <h4 className="text-sm font-semibold text-white mb-4 uppercase tracking-wide">Company</h4>
              <ul className="space-y-2">
                {['About', 'Blog', 'Careers', 'Contact'].map((item) => (
                  <li key={item}>
                    <Link href="#" className="text-slate-400 hover:text-white transition-colors text-sm">
                      {item}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>

            {/* Legal links */}
            <div>
              <h4 className="text-sm font-semibold text-white mb-4 uppercase tracking-wide">Legal</h4>
              <ul className="space-y-2">
                {['Privacy', 'Terms', 'Security', 'Cookies'].map((item) => (
                  <li key={item}>
                    <Link href="#" className="text-slate-400 hover:text-white transition-colors text-sm">
                      {item}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Bottom section */}
          <div className="border-t border-white/5 pt-8 flex flex-col md:flex-row justify-between items-center">
            <p className="text-slate-400 text-sm">
              © 2025 Engineering Career OS. All rights reserved.
            </p>

            {/* Social links */}
            <div className="flex gap-4 mt-4 md:mt-0">
              {[
                { icon: Twitter, href: '#', label: 'Twitter' },
                { icon: Github, href: '#', label: 'GitHub' },
                { icon: Linkedin, href: '#', label: 'LinkedIn' },
                { icon: Mail, href: '#', label: 'Email' },
              ].map(({ icon: Icon, href, label }) => (
                <Link
                  key={label}
                  href={href}
                  className="p-2 rounded-lg bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition-all duration-300 group"
                  aria-label={label}
                >
                  <Icon className="w-5 h-5" />
                </Link>
              ))}
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
