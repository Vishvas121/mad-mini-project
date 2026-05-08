'use client';

import { engineeringBranches } from '@/lib/constants';
import { Code2, Server, Layers, Cloud, Database, Cpu, ChevronRight, CheckCircle2, Circle, ArrowRight, Sparkles, Map, ExternalLink } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';

const iconMap = {
    Code2: <Code2 className="w-6 h-6" />, Server: <Server className="w-6 h-6" />,
    Layers: <Layers className="w-6 h-6" />, Cloud: <Cloud className="w-6 h-6" />,
    Database: <Database className="w-6 h-6" />, Cpu: <Cpu className="w-6 h-6" />,
};

const roadmapData: Record<string, { level: string; items: { title: string; duration: string; status: string }[] }[]> = {
    'Frontend Engineering': [
        { level: 'Phase 1: Foundations', items: [{ title: 'HTML5, CSS3 & Responsive Design', duration: '2 weeks', status: 'completed' }, { title: 'JavaScript ES6+ Fundamentals', duration: '3 weeks', status: 'current' }, { title: 'Git & Version Control', duration: '1 week', status: 'pending' }] },
        { level: 'Phase 2: Core Frameworks', items: [{ title: 'React & Component Architecture', duration: '4 weeks', status: 'pending' }, { title: 'State Management (Redux/Zustand)', duration: '2 weeks', status: 'pending' }, { title: 'TypeScript for Frontend', duration: '2 weeks', status: 'pending' }] },
        { level: 'Phase 3: Industry Readiness', items: [{ title: 'Next.js & SSR/SSG', duration: '3 weeks', status: 'pending' }, { title: 'Testing (Jest, Cypress)', duration: '2 weeks', status: 'pending' }, { title: 'Performance & Web Vitals', duration: '2 weeks', status: 'pending' }] },
    ],
    'Backend Engineering': [
        { level: 'Phase 1: Foundations', items: [{ title: 'Node.js & Async Programming', duration: '2 weeks', status: 'completed' }, { title: 'REST API Design & Express', duration: '3 weeks', status: 'current' }, { title: 'SQL & PostgreSQL Basics', duration: '2 weeks', status: 'pending' }] },
        { level: 'Phase 2: Core Engineering', items: [{ title: 'Authentication & Authorization (JWT)', duration: '2 weeks', status: 'pending' }, { title: 'Caching with Redis', duration: '2 weeks', status: 'pending' }, { title: 'Message Queues (RabbitMQ/Kafka)', duration: '3 weeks', status: 'pending' }] },
        { level: 'Phase 3: Scale & Production', items: [{ title: 'Microservices Architecture', duration: '4 weeks', status: 'pending' }, { title: 'Docker & Containerization', duration: '2 weeks', status: 'pending' }, { title: 'System Design & Scalability', duration: '3 weeks', status: 'pending' }] },
    ],
    'Full Stack Development': [
        { level: 'Phase 1: Foundations', items: [{ title: 'HTML, CSS, JavaScript Essentials', duration: '3 weeks', status: 'completed' }, { title: 'React Frontend Development', duration: '4 weeks', status: 'current' }, { title: 'Node.js & Express Backend', duration: '3 weeks', status: 'pending' }] },
        { level: 'Phase 2: Full Stack Integration', items: [{ title: 'Database Design (SQL + NoSQL)', duration: '3 weeks', status: 'pending' }, { title: 'REST & GraphQL APIs', duration: '3 weeks', status: 'pending' }, { title: 'Authentication & Security', duration: '2 weeks', status: 'pending' }] },
        { level: 'Phase 3: Production Ready', items: [{ title: 'Next.js Full Stack App', duration: '4 weeks', status: 'pending' }, { title: 'CI/CD & Deployment (Vercel/AWS)', duration: '2 weeks', status: 'pending' }, { title: 'Capstone Project', duration: '4 weeks', status: 'pending' }] },
    ],
    'DevOps & Cloud': [
        { level: 'Phase 1: Foundations', items: [{ title: 'Linux & Shell Scripting', duration: '2 weeks', status: 'completed' }, { title: 'Docker & Containerization', duration: '3 weeks', status: 'current' }, { title: 'Git & CI/CD Basics', duration: '1 week', status: 'pending' }] },
        { level: 'Phase 2: Cloud & Orchestration', items: [{ title: 'AWS Core Services (EC2, S3, RDS)', duration: '4 weeks', status: 'pending' }, { title: 'Kubernetes & Container Orchestration', duration: '4 weeks', status: 'pending' }, { title: 'Infrastructure as Code (Terraform)', duration: '3 weeks', status: 'pending' }] },
        { level: 'Phase 3: Advanced DevOps', items: [{ title: 'Monitoring & Observability (Prometheus)', duration: '2 weeks', status: 'pending' }, { title: 'Security & Compliance (DevSecOps)', duration: '2 weeks', status: 'pending' }, { title: 'AWS Solutions Architect Prep', duration: '4 weeks', status: 'pending' }] },
    ],
    'Data Engineering': [
        { level: 'Phase 1: Foundations', items: [{ title: 'Python for Data Engineering', duration: '2 weeks', status: 'completed' }, { title: 'SQL & Database Fundamentals', duration: '2 weeks', status: 'current' }, { title: 'Data Modeling Concepts', duration: '1 week', status: 'pending' }] },
        { level: 'Phase 2: Pipeline Engineering', items: [{ title: 'Apache Spark & PySpark', duration: '4 weeks', status: 'pending' }, { title: 'Airflow & Workflow Orchestration', duration: '3 weeks', status: 'pending' }, { title: 'Data Warehousing (Snowflake/BigQuery)', duration: '3 weeks', status: 'pending' }] },
        { level: 'Phase 3: Advanced Topics', items: [{ title: 'Kafka & Real-time Streaming', duration: '3 weeks', status: 'pending' }, { title: 'dbt & Analytics Engineering', duration: '2 weeks', status: 'pending' }, { title: 'Cloud Data Platforms (AWS/GCP)', duration: '3 weeks', status: 'pending' }] },
    ],
    'Machine Learning': [
        { level: 'Phase 1: Foundations', items: [{ title: 'Python, NumPy & Pandas', duration: '2 weeks', status: 'completed' }, { title: 'Statistics & Linear Algebra', duration: '3 weeks', status: 'current' }, { title: 'Data Visualization (Matplotlib)', duration: '1 week', status: 'pending' }] },
        { level: 'Phase 2: Core ML', items: [{ title: 'Supervised & Unsupervised Learning', duration: '4 weeks', status: 'pending' }, { title: 'Deep Learning & Neural Networks', duration: '4 weeks', status: 'pending' }, { title: 'Model Evaluation & Tuning', duration: '2 weeks', status: 'pending' }] },
        { level: 'Phase 3: Production ML', items: [{ title: 'NLP & Transformers (HuggingFace)', duration: '4 weeks', status: 'pending' }, { title: 'MLOps & Model Deployment', duration: '3 weeks', status: 'pending' }, { title: 'LLMs & Generative AI', duration: '4 weeks', status: 'pending' }] },
    ],
};

export default function RoadmapsPage() {
    const [selectedBranch, setSelectedBranch] = useState(engineeringBranches[0]);
    const [activeTab, setActiveTab] = useState<'custom' | 'community'>('custom');
    const phases = roadmapData[selectedBranch.title] || [];

    return (
        <div className="min-h-screen bg-background">
            <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10">
                <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/10 rounded-full blur-[120px] animate-pulse" />
                <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-violet-500/10 rounded-full blur-[120px] animate-pulse" style={{ animationDelay: '2s' }} />
            </div>

            {/* Hero */}
            <section className="pt-24 pb-10 px-4 md:px-8 border-b border-border">
                <div className="max-w-6xl mx-auto text-center">
                    <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full glass mb-6">
                        <Map className="w-4 h-4 text-primary" />
                        <span className="text-sm font-medium text-slate-200 uppercase tracking-wider">Career Navigation</span>
                    </div>
                    <h1 className="text-4xl md:text-6xl font-bold text-foreground mb-4">
                        Personalized <span className="gradient-text">Learning Roadmaps</span>
                    </h1>
                    <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
                        Choose your specialization and follow a curated path from foundation to industry expert.
                    </p>
                </div>
            </section>

            {/* Tabs */}
            <div className="max-w-7xl mx-auto px-4 md:px-8 pt-6">
                <div className="flex gap-1 border-b border-white/10">
                    {([['custom', '🗺️ Career Roadmaps'], ['community', '🌐 roadmap.sh']] as const).map(([key, label]) => (
                        <button key={key} onClick={() => setActiveTab(key)}
                            className={`px-5 py-2.5 font-semibold text-sm rounded-t-lg transition-all duration-200 ${activeTab === key ? 'bg-blue-500/20 text-blue-300 border-b-2 border-blue-400' : 'text-slate-400 hover:text-white'}`}>
                            {label}
                        </button>
                    ))}
                </div>
            </div>

            {/* roadmap.sh cards */}
            {activeTab === 'community' && (
                <div className="max-w-7xl mx-auto px-4 md:px-8 py-6 space-y-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h2 className="text-2xl font-bold text-white">🌐 roadmap.sh Roadmaps</h2>
                            <p className="text-slate-400 text-sm mt-1">Community-maintained developer roadmaps — click any to open on roadmap.sh</p>
                        </div>
                        <a href="https://roadmap.sh" target="_blank" rel="noopener noreferrer"
                            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-blue-500 to-cyan-500 text-white rounded-xl text-sm font-semibold hover:opacity-90 transition-opacity shrink-0">
                            Browse All <ExternalLink className="w-3.5 h-3.5" />
                        </a>
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                        {[
                            { title: 'Frontend Developer', url: 'https://roadmap.sh/frontend', emoji: '🖥️', desc: 'HTML, CSS, JS, React, TypeScript and beyond', color: 'from-blue-500/20 to-cyan-500/20', border: 'border-blue-500/30' },
                            { title: 'Backend Developer', url: 'https://roadmap.sh/backend', emoji: '⚙️', desc: 'APIs, databases, auth, caching and scaling', color: 'from-violet-500/20 to-purple-500/20', border: 'border-violet-500/30' },
                            { title: 'Full Stack Developer', url: 'https://roadmap.sh/full-stack', emoji: '🔗', desc: 'End-to-end web development mastery', color: 'from-emerald-500/20 to-teal-500/20', border: 'border-emerald-500/30' },
                            { title: 'DevOps Engineer', url: 'https://roadmap.sh/devops', emoji: '🚀', desc: 'Docker, Kubernetes, CI/CD, cloud infra', color: 'from-orange-500/20 to-amber-500/20', border: 'border-orange-500/30' },
                            { title: 'React Developer', url: 'https://roadmap.sh/react', emoji: '⚛️', desc: 'Deep dive into React ecosystem', color: 'from-cyan-500/20 to-blue-500/20', border: 'border-cyan-500/30' },
                            { title: 'Node.js Developer', url: 'https://roadmap.sh/nodejs', emoji: '🟢', desc: 'Server-side JavaScript mastery', color: 'from-green-500/20 to-emerald-500/20', border: 'border-green-500/30' },
                            { title: 'Python Developer', url: 'https://roadmap.sh/python', emoji: '🐍', desc: 'Python for web, data, and automation', color: 'from-yellow-500/20 to-amber-500/20', border: 'border-yellow-500/30' },
                            { title: 'AI & Data Scientist', url: 'https://roadmap.sh/ai-data-scientist', emoji: '🤖', desc: 'ML, deep learning, and AI engineering', color: 'from-pink-500/20 to-rose-500/20', border: 'border-pink-500/30' },
                            { title: 'Android Developer', url: 'https://roadmap.sh/android', emoji: '📱', desc: 'Kotlin, Jetpack Compose, Android SDK', color: 'from-lime-500/20 to-green-500/20', border: 'border-lime-500/30' },
                            { title: 'System Design', url: 'https://roadmap.sh/system-design', emoji: '🏗️', desc: 'Scalable systems, distributed architecture', color: 'from-slate-500/20 to-zinc-500/20', border: 'border-slate-500/30' },
                            { title: 'Software Architect', url: 'https://roadmap.sh/software-architect', emoji: '📐', desc: 'Design patterns, architecture principles', color: 'from-indigo-500/20 to-violet-500/20', border: 'border-indigo-500/30' },
                            { title: 'Cyber Security', url: 'https://roadmap.sh/cyber-security', emoji: '🔐', desc: 'Security fundamentals, ethical hacking', color: 'from-red-500/20 to-rose-500/20', border: 'border-red-500/30' },
                        ].map(r => (
                            <a key={r.title} href={r.url} target="_blank" rel="noopener noreferrer"
                                className={`group flex flex-col gap-3 p-5 bg-gradient-to-br ${r.color} border ${r.border} rounded-2xl hover:scale-[1.02] transition-all duration-200`}>
                                <div className="flex items-center justify-between">
                                    <span className="text-3xl">{r.emoji}</span>
                                    <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-white transition-colors" />
                                </div>
                                <div>
                                    <h3 className="text-white font-bold">{r.title}</h3>
                                    <p className="text-slate-400 text-xs mt-1">{r.desc}</p>
                                </div>
                                <span className="text-xs text-slate-500 font-medium">roadmap.sh →</span>
                            </a>
                        ))}
                    </div>
                </div>
            )}

            {/* Custom roadmaps */}
            {activeTab === 'custom' && (
                <div className="max-w-7xl mx-auto p-4 md:p-8">
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                        <aside className="lg:col-span-4 space-y-4">
                            <h2 className="text-lg font-bold text-foreground px-2 mb-4">Engineering Branches</h2>
                            <div className="space-y-2">
                                {engineeringBranches.map((branch) => (
                                    <button key={branch.id} onClick={() => setSelectedBranch(branch)}
                                        className={`w-full flex items-center gap-4 p-4 rounded-xl transition-all duration-300 text-left group ${selectedBranch.id === branch.id ? 'bg-primary/10 border border-primary/30' : 'hover:bg-muted/50 border border-transparent'}`}>
                                        <div className={`p-2 rounded-lg ${selectedBranch.id === branch.id ? 'bg-primary text-white' : 'bg-muted text-muted-foreground'} transition-colors`}>
                                            {iconMap[branch.icon as keyof typeof iconMap]}
                                        </div>
                                        <div className="flex-1">
                                            <div className="font-bold text-foreground">{branch.title}</div>
                                            <div className="text-xs text-muted-foreground line-clamp-1">{branch.description}</div>
                                        </div>
                                        <ChevronRight className={`w-4 h-4 transition-transform ${selectedBranch.id === branch.id ? 'translate-x-1 text-primary' : 'text-muted-foreground'}`} />
                                    </button>
                                ))}
                            </div>
                            <div className="mt-8 p-6 glass rounded-2xl border border-primary/20">
                                <div className="flex items-center gap-2 text-primary mb-3">
                                    <Sparkles className="w-5 h-5" />
                                    <span className="font-bold">Need a Custom Roadmap?</span>
                                </div>
                                <p className="text-sm text-slate-400 mb-4">Let our AI Assistant create a unique learning path just for you.</p>
                                <Link href="/ai-assistant" className="inline-flex items-center gap-2 text-sm font-bold text-primary hover:gap-3 transition-all">
                                    Try AI Assistant <ArrowRight className="w-4 h-4" />
                                </Link>
                            </div>
                        </aside>

                        <main className="lg:col-span-8">
                            <div className="glass-lg p-6 md:p-8 rounded-2xl border border-border space-y-8">
                                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-8 border-b border-border">
                                    <div>
                                        <h2 className="text-3xl font-bold text-foreground mb-2">{selectedBranch.title}</h2>
                                        <p className="text-muted-foreground">{selectedBranch.description}</p>
                                    </div>
                                    <div className="flex gap-4">
                                        <div className="text-center px-4 py-2 bg-muted/30 rounded-lg border border-border">
                                            <div className="text-xs text-muted-foreground uppercase font-bold tracking-tighter">Duration</div>
                                            <div className="font-bold text-foreground">{selectedBranch.duration}</div>
                                        </div>
                                        <div className="text-center px-4 py-2 bg-muted/30 rounded-lg border border-border">
                                            <div className="text-xs text-muted-foreground uppercase font-bold tracking-tighter">Difficulty</div>
                                            <div className="font-bold text-foreground">{selectedBranch.difficulty}</div>
                                        </div>
                                    </div>
                                </div>

                                <div className="space-y-12">
                                    {phases.map((phase, phaseIdx) => (
                                        <div key={phaseIdx} className="relative">
                                            {phaseIdx !== phases.length - 1 && (
                                                <div className="absolute left-[19px] top-[40px] bottom-[-40px] w-0.5 bg-gradient-to-b from-primary/50 to-transparent z-0" />
                                            )}
                                            <h3 className="flex items-center gap-4 text-xl font-bold text-foreground mb-6 z-10 relative">
                                                <div className="w-10 h-10 rounded-full bg-primary/20 border border-primary/40 flex items-center justify-center text-primary">{phaseIdx + 1}</div>
                                                {phase.level}
                                            </h3>
                                            <div className="ml-5 pl-10 space-y-4">
                                                {phase.items.map((item, itemIdx) => (
                                                    <div key={itemIdx} className="group p-4 bg-muted/30 hover:bg-muted/50 border border-border rounded-xl transition-all relative">
                                                        <div className="absolute left-[-31px] top-1/2 -translate-y-1/2 w-4 h-4 rounded-full border-2 border-primary bg-background z-10" />
                                                        <div className="flex items-center justify-between">
                                                            <div className="flex items-center gap-3">
                                                                {item.status === 'completed' ? <CheckCircle2 className="w-5 h-5 text-emerald-500" />
                                                                    : item.status === 'current' ? <Sparkles className="w-5 h-5 text-primary animate-pulse" />
                                                                    : <Circle className="w-5 h-5 text-muted-foreground/30" />}
                                                                <span className={`font-medium ${item.status === 'pending' ? 'text-muted-foreground' : 'text-foreground'}`}>{item.title}</span>
                                                            </div>
                                                            <span className="text-xs font-medium text-slate-500 bg-muted px-2 py-1 rounded">{item.duration}</span>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <div className="pt-8 border-t border-border flex flex-wrap gap-4 items-center justify-between">
                                    <div className="text-sm text-muted-foreground">
                                        <span className="font-bold text-primary">{selectedBranch.courses}</span> expert courses in this path
                                    </div>
                                    <Link href="/courses" className="inline-flex items-center gap-2 px-6 py-3 bg-primary hover:bg-primary/90 text-white rounded-lg font-bold transition-all hover:scale-105">
                                        Start This Roadmap <ArrowRight className="w-4 h-4" />
                                    </Link>
                                </div>
                            </div>
                        </main>
                    </div>
                </div>
            )}
        </div>
    );
}
