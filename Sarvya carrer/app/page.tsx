import { SignInButton, SignOutButton, SignedIn, SignedOut, UserButton } from '@clerk/nextjs';
import Link from 'next/link';
import { ArrowRight, BookOpen, Code2, Users, Trophy, Zap } from 'lucide-react';
import { ThemeToggle } from '@/components/theme-toggle';

export default function Home() {
  return (
    <main className="w-full min-h-screen bg-background transition-colors duration-300">
      {/* Navigation */}
      <nav className="sticky top-0 z-50 bg-card border-b border-border transition-colors duration-300">
        <div className="max-w-6xl mx-auto flex items-center justify-between h-16 px-4 md:px-8">
          <div className="text-xl font-bold text-foreground">CareerOS</div>
          <div className="flex items-center gap-6">
            <Link href="/courses" className="text-foreground hover:text-primary transition-colors text-sm">Courses</Link>
            <Link href="/roadmaps" className="text-foreground hover:text-primary transition-colors text-sm font-medium text-primary">Roadmaps</Link>
            <Link href="/dashboard" className="text-foreground hover:text-primary transition-colors text-sm">Dashboard</Link>
            <ThemeToggle />
            <SignedOut>
              <SignInButton mode="modal">
                <button className="button-primary text-sm">Sign In</button>
              </SignInButton>
            </SignedOut>
            <SignedIn>
              <div className="flex items-center gap-4">
                <Link href="/settings" className="text-foreground hover:text-primary transition-colors text-sm font-medium">Settings</Link>
                <UserButton afterSignOutUrl="/" />
              </div>
            </SignedIn>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="section-container">
        <div className="max-w-6xl mx-auto">
          <div className="max-w-3xl mx-auto text-center py-12 md:py-20">
            <h1 className="text-5xl md:text-6xl font-bold text-foreground mb-6 leading-tight">
              Launch your tech career
            </h1>
            <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto leading-relaxed">
              Learn industry-relevant skills from expert engineers. Designed for Indian students aspiring to work at global tech companies.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button className="button-primary inline-flex items-center gap-2">
                Start Learning <ArrowRight size={18} />
              </button>
              <button className="button-outline">Explore Courses</button>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="section-container bg-muted transition-colors duration-300">
        <div className="max-w-6xl mx-auto px-4 md:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-12">
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold text-primary mb-2">50K+</div>
              <p className="text-muted-foreground text-sm">Active Students</p>
            </div>
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold text-primary mb-2">200+</div>
              <p className="text-muted-foreground text-sm">Expert Courses</p>
            </div>
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold text-primary mb-2">95%</div>
              <p className="text-muted-foreground text-sm">Success Rate</p>
            </div>
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold text-primary mb-2">10K+</div>
              <p className="text-muted-foreground text-sm">Placements</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="section-container">
        <div className="max-w-6xl mx-auto px-4 md:px-8">
          <h2 className="text-4xl font-bold text-center mb-16">Why Choose CareerOS?</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {[
              { icon: Code2, title: 'Industry-Aligned Content', desc: 'Learn exactly what companies want' },
              { icon: Users, title: 'Expert Mentors', desc: 'Learn from engineers at FAANG companies' },
              { icon: Zap, title: 'Live Projects', desc: 'Build real-world applications' },
              { icon: BookOpen, title: 'Job Preparation', desc: 'Resume, interview, and negotiation guides' },
              { icon: Trophy, title: 'Certifications', desc: 'Industry-recognized certificates' },
              { icon: ArrowRight, title: 'Career Support', desc: '1-on-1 career guidance included' },
            ].map((feature, i) => (
              <div key={i} className="card-elevated p-6 hover:border-primary/30 transition-all duration-300">
                <feature.icon className="text-primary mb-4" size={28} />
                <h3 className="text-lg font-bold mb-2 text-foreground">{feature.title}</h3>
                <p className="text-muted-foreground text-sm">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Courses Preview */}
      <section className="section-container bg-muted transition-colors duration-300">
        <div className="max-w-6xl mx-auto px-4 md:px-8">
          <h2 className="text-4xl font-bold mb-4">Featured Courses</h2>
          <p className="text-muted-foreground mb-12 max-w-2xl">Handpicked courses for aspiring engineers</p>
          <div className="grid md:grid-cols-3 gap-6">
            {[
              { title: 'Web Development Mastery', level: 'Intermediate', students: '8.2K' },
              { title: 'System Design Fundamentals', level: 'Advanced', students: '5.1K' },
              { title: 'DSA for Interviews', level: 'Intermediate', students: '12.5K' },
            ].map((course, i) => (
              <Link key={i} href="/courses" className="card-elevated p-6 hover:shadow-lg transition-shadow group">
                <div className="h-40 bg-gradient-to-br from-primary/10 to-secondary/10 rounded-lg mb-4 group-hover:from-primary/20 group-hover:to-secondary/20 transition-colors" />
                <h3 className="text-lg font-bold mb-2">{course.title}</h3>
                <div className="flex justify-between text-sm text-muted-foreground">
                  <span>{course.level}</span>
                  <span>{course.students} students</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="section-container">
        <div className="max-w-6xl mx-auto px-4 md:px-8 text-center py-8">
          <h2 className="text-4xl font-bold mb-4">Ready to transform your career?</h2>
          <p className="text-muted-foreground text-lg mb-8">Join thousands of Indian engineers who've secured roles at top companies</p>
          <button className="button-primary inline-flex items-center gap-2">
            Get Started Free <ArrowRight size={18} />
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-muted border-t border-border py-12 transition-colors duration-300">
        <div className="max-w-6xl mx-auto px-4 md:px-8">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div>
              <h3 className="font-bold mb-4">CareerOS</h3>
              <p className="text-muted-foreground text-sm">Launching careers of Indian engineers globally</p>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-sm">Product</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><Link href="#" className="hover:text-foreground">Courses</Link></li>
                <li><Link href="#" className="hover:text-foreground">Pricing</Link></li>
                <li><Link href="#" className="hover:text-foreground">Features</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-sm">Company</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><Link href="#" className="hover:text-foreground">About</Link></li>
                <li><Link href="#" className="hover:text-foreground">Blog</Link></li>
                <li><Link href="#" className="hover:text-foreground">Contact</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-sm">Legal</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><Link href="#" className="hover:text-foreground">Privacy</Link></li>
                <li><Link href="#" className="hover:text-foreground">Terms</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-border pt-8 text-center text-sm text-muted-foreground">
            <p>&copy; 2024 CareerOS. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </main>
  );
}
