'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { navigationItems } from '@/lib/constants';
import {
  BookOpen, Brain, FileText, LayoutDashboard,
  Sparkles, Map, Briefcase, Code2, X, Zap,
} from 'lucide-react';
import { cn } from '@/lib/utils';

const iconMap = {
  Dashboard: <LayoutDashboard className="w-5 h-5" />,
  Courses: <BookOpen className="w-5 h-5" />,
  'AI Learning': <Zap className="w-5 h-5" />,
  'AI Assistant': <Brain className="w-5 h-5" />,
  'Resume Builder': <FileText className="w-5 h-5" />,
  Hackathons: <Sparkles className="w-5 h-5" />,
  Roadmaps: <Map className="w-5 h-5" />,
  Practice: <Code2 className="w-5 h-5" />,
  Jobs: <Briefcase className="w-5 h-5" />,
};

interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
}

export function Sidebar({ isOpen, setIsOpen }: SidebarProps) {
  const pathname = usePathname();

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/30 lg:hidden z-30"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed lg:static left-0 top-0 h-screen w-64 bg-card border-r border-border flex flex-col transition-all duration-300 z-40 lg:z-auto shadow-sm dark:shadow-lg dark:shadow-black/20',
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        {/* Logo and close button */}
        <div className="flex items-center justify-between p-6 border-b border-border">
          <Link href="/" className="flex items-center gap-2 group">
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-white font-bold group-hover:shadow-md transition-all duration-300">
              C
            </div>
            <div className="font-bold text-foreground hidden sm:block">CareerOS</div>
          </Link>
          <button
            onClick={() => setIsOpen(false)}
            className="lg:hidden p-1 hover:bg-muted rounded-lg transition-colors"
          >
            <X className="w-5 h-5 text-muted-foreground" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
          {navigationItems.map((item) => {
            const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
            return (
              <Link
                key={item.label}
                href={item.href}
                className={cn(
                  'flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all duration-200',
                  isActive
                    ? 'bg-primary/10 text-primary border border-primary/20 dark:bg-primary/20 dark:border-primary/30'
                    : 'text-muted-foreground hover:text-foreground hover:bg-muted dark:hover:bg-white/10'
                )}
              >
                {iconMap[item.label as keyof typeof iconMap]}
                <span className="flex-1">{item.label}</span>
                {item.label === 'AI Learning' && (
                  <span className="text-[9px] font-black px-1.5 py-0.5 bg-violet-500/20 text-violet-400 rounded-full border border-violet-500/30 uppercase tracking-wide">
                    NEW
                  </span>
                )}
              </Link>
            );
          })}
        </nav>

        {/* Footer */}
        <div className="p-4 border-t border-border">
          <div className="bg-muted p-4 rounded-lg text-center dark:bg-white/10">
            <p className="text-xs text-muted-foreground mb-2">Need help?</p>
            <button className="button-primary w-full text-sm">
              Support
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
