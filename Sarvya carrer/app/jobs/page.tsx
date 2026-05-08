'use client';

import { ExternalLink, Briefcase, MapPin, Clock, Building2, Search, Filter, Zap, TrendingUp, Star } from 'lucide-react';
import { useState, useMemo } from 'react';

const JOB_PLATFORMS = [
  { name: 'Hiring Cafe', url: 'https://hiring.cafe', emoji: '☕', desc: 'AI-powered job matching', color: 'from-amber-500 to-orange-500', badge: 'bg-amber-500/20 text-amber-300 border-amber-500/30' },
  { name: 'LinkedIn Jobs', url: 'https://www.linkedin.com/jobs/search/?keywords=software+engineer+india', emoji: '💼', desc: 'World\'s largest network', color: 'from-blue-600 to-blue-700', badge: 'bg-blue-500/20 text-blue-300 border-blue-500/30' },
  { name: 'Internshala', url: 'https://internshala.com/jobs/computer-science-jobs', emoji: '🎓', desc: 'Fresher & intern roles', color: 'from-teal-500 to-cyan-500', badge: 'bg-teal-500/20 text-teal-300 border-teal-500/30' },
  { name: 'Naukri', url: 'https://www.naukri.com/software-engineer-jobs', emoji: '🇮🇳', desc: 'India\'s #1 job portal', color: 'from-rose-500 to-pink-500', badge: 'bg-rose-500/20 text-rose-300 border-rose-500/30' },
  { name: 'Wellfound', url: 'https://wellfound.com/jobs', emoji: '🚀', desc: 'Startup jobs & equity', color: 'from-violet-500 to-purple-500', badge: 'bg-violet-500/20 text-violet-300 border-violet-500/30' },
  { name: 'Glassdoor', url: 'https://www.glassdoor.co.in/Job/india-software-engineer-jobs', emoji: '🔍', desc: 'Jobs + salary insights', color: 'from-green-500 to-emerald-500', badge: 'bg-green-500/20 text-green-300 border-green-500/30' },
];

const JOBS = [
  { id: 1, title: 'Software Engineer II', company: 'Google', location: 'Bengaluru, India', type: 'Full-time', level: 'Mid', domain: 'Backend', salary: '₹30–50 LPA', tags: ['Go', 'Kubernetes', 'Distributed Systems'], url: 'https://careers.google.com/jobs/results/?location=India&q=software+engineer', hot: true },
  { id: 2, title: 'Frontend Engineer', company: 'Flipkart', location: 'Bengaluru, India', type: 'Full-time', level: 'Junior', domain: 'Frontend', salary: '₹15–25 LPA', tags: ['React', 'TypeScript', 'Performance'], url: 'https://www.flipkartcareers.com', hot: true },
  { id: 3, title: 'Full Stack Developer', company: 'Razorpay', location: 'Bengaluru / Remote', type: 'Full-time', level: 'Mid', domain: 'Full Stack', salary: '₹20–35 LPA', tags: ['Node.js', 'React', 'PostgreSQL'], url: 'https://razorpay.com/jobs', hot: false },
  { id: 4, title: 'Backend Engineer', company: 'Zepto', location: 'Mumbai, India', type: 'Full-time', level: 'Junior', domain: 'Backend', salary: '₹12–22 LPA', tags: ['Python', 'FastAPI', 'Redis'], url: 'https://www.zeptonow.com/careers', hot: true },
  { id: 5, title: 'DevOps Engineer', company: 'Infosys', location: 'Pan India', type: 'Full-time', level: 'Mid', domain: 'DevOps', salary: '₹10–18 LPA', tags: ['Docker', 'Kubernetes', 'AWS'], url: 'https://www.infosys.com/careers', hot: false },
  { id: 6, title: 'ML Engineer', company: 'Swiggy', location: 'Bengaluru, India', type: 'Full-time', level: 'Senior', domain: 'AI/ML', salary: '₹35–60 LPA', tags: ['Python', 'PyTorch', 'MLOps'], url: 'https://bytes.swiggy.com/careers', hot: true },
  { id: 7, title: 'React Native Developer', company: 'CRED', location: 'Bengaluru, India', type: 'Full-time', level: 'Mid', domain: 'Mobile', salary: '₹18–30 LPA', tags: ['React Native', 'TypeScript', 'iOS/Android'], url: 'https://careers.cred.club', hot: false },
  { id: 8, title: 'Data Engineer', company: 'PhonePe', location: 'Bengaluru, India', type: 'Full-time', level: 'Mid', domain: 'Data', salary: '₹20–35 LPA', tags: ['Spark', 'Kafka', 'Airflow'], url: 'https://www.phonepe.com/careers', hot: false },
  { id: 9, title: 'SDE Intern', company: 'Microsoft', location: 'Hyderabad, India', type: 'Internship', level: 'Intern', domain: 'Full Stack', salary: '₹80K/month', tags: ['C#', 'Azure', '.NET'], url: 'https://careers.microsoft.com/students/us/en/india', hot: true },
  { id: 10, title: 'Cloud Engineer', company: 'Amazon (AWS)', location: 'Hyderabad / Bengaluru', type: 'Full-time', level: 'Mid', domain: 'DevOps', salary: '₹25–45 LPA', tags: ['AWS', 'Terraform', 'Python'], url: 'https://www.amazon.jobs/en/teams/aws', hot: true },
  { id: 11, title: 'iOS Developer', company: 'Meesho', location: 'Bengaluru, India', type: 'Full-time', level: 'Junior', domain: 'Mobile', salary: '₹14–24 LPA', tags: ['Swift', 'SwiftUI', 'Xcode'], url: 'https://meesho.io/careers', hot: false },
  { id: 12, title: 'Security Engineer', company: 'Paytm', location: 'Noida, India', type: 'Full-time', level: 'Mid', domain: 'Security', salary: '₹18–28 LPA', tags: ['Penetration Testing', 'OWASP', 'Python'], url: 'https://paytm.com/careers', hot: false },
  { id: 13, title: 'GenAI Engineer', company: 'Sarvam AI', location: 'Bengaluru / Remote', type: 'Full-time', level: 'Mid', domain: 'AI/ML', salary: '₹25–50 LPA', tags: ['LLMs', 'LangChain', 'Python'], url: 'https://www.sarvam.ai/careers', hot: true },
  { id: 14, title: 'Platform Engineer', company: 'Atlassian', location: 'Bengaluru, India', type: 'Full-time', level: 'Senior', domain: 'DevOps', salary: '₹40–70 LPA', tags: ['Kubernetes', 'Go', 'Terraform'], url: 'https://www.atlassian.com/company/careers', hot: false },
  { id: 15, title: 'SDE-1 (Fresher)', company: 'Walmart Global Tech', location: 'Bengaluru, India', type: 'Full-time', level: 'Junior', domain: 'Backend', salary: '₹12–18 LPA', tags: ['Java', 'Spring Boot', 'MySQL'], url: 'https://careers.walmart.com/results?q=india', hot: false },
  { id: 16, title: 'Frontend Intern', company: 'Groww', location: 'Bengaluru, India', type: 'Internship', level: 'Intern', domain: 'Frontend', salary: '₹50K/month', tags: ['React', 'JavaScript', 'CSS'], url: 'https://groww.in/careers', hot: true },
];

const DOMAINS = ['All', 'Frontend', 'Backend', 'Full Stack', 'DevOps', 'AI/ML', 'Mobile', 'Data', 'Security'];
const LEVELS = ['All', 'Intern', 'Junior', 'Mid', 'Senior'];
const TYPES = ['All', 'Full-time', 'Internship'];

const levelColor: Record<string, string> = {
  Intern: 'bg-cyan-500/20 text-cyan-300',
  Junior: 'bg-emerald-500/20 text-emerald-300',
  Mid: 'bg-blue-500/20 text-blue-300',
  Senior: 'bg-violet-500/20 text-violet-300',
};

export default function JobsPage() {
  const [search, setSearch] = useState('');
  const [domain, setDomain] = useState('All');
  const [level, setLevel] = useState('All');
  const [type, setType] = useState('All');

  const filtered = useMemo(() => JOBS.filter(j => {
    const q = search.toLowerCase();
    return (
      (!q || j.title.toLowerCase().includes(q) || j.company.toLowerCase().includes(q) || j.tags.some(t => t.toLowerCase().includes(q))) &&
      (domain === 'All' || j.domain === domain) &&
      (level === 'All' || j.level === level) &&
      (type === 'All' || j.type === type)
    );
  }), [search, domain, level, type]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-950 to-black">
      {/* Header */}
      <div className="border-b border-white/10 bg-slate-900/50 backdrop-blur-xl">
        <div className="max-w-7xl mx-auto px-4 md:px-8 py-10">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className="w-10 h-10 rounded-xl bg-amber-500/20 flex items-center justify-center">
                  <Briefcase className="w-5 h-5 text-amber-400" />
                </div>
                <h1 className="text-4xl md:text-5xl font-bold text-white">
                  Job <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-400 to-orange-400">Board</span>
                </h1>
              </div>
              <p className="text-slate-400">{filtered.length} engineering roles across India's top companies</p>
            </div>
            <div className="flex items-center gap-2 px-4 py-2 bg-amber-500/10 border border-amber-500/20 rounded-xl">
              <TrendingUp className="w-4 h-4 text-amber-400" />
              <span className="text-amber-300 text-sm font-medium">Updated April 2025</span>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 py-8 space-y-8">
        {/* Job platforms */}
        <div>
          <p className="text-xs text-slate-500 uppercase tracking-widest font-semibold mb-3">Search on top platforms</p>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {JOB_PLATFORMS.map(p => (
              <a key={p.name} href={p.url} target="_blank" rel="noopener noreferrer"
                className="flex flex-col items-center gap-2 p-4 bg-white/5 hover:bg-white/10 border border-white/10 hover:border-white/20 rounded-2xl transition-all group text-center">
                <span className="text-2xl">{p.emoji}</span>
                <span className="text-white text-xs font-semibold">{p.name}</span>
                <span className="text-slate-500 text-xs">{p.desc}</span>
                <ExternalLink className="w-3 h-3 text-slate-600 group-hover:text-slate-400 transition-colors" />
              </a>
            ))}
          </div>
        </div>

        {/* Search + Filters */}
        <div className="flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
            <input value={search} onChange={e => setSearch(e.target.value)}
              placeholder="Search roles, companies, skills..."
              className="w-full pl-9 pr-4 py-2.5 bg-white/5 border border-white/10 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:border-blue-500/50 transition-all" />
          </div>
          <select value={domain} onChange={e => setDomain(e.target.value)}
            className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-xl text-slate-300 text-sm focus:outline-none focus:border-blue-500/50 transition-all">
            {DOMAINS.map(d => <option key={d} value={d}>{d === 'All' ? 'All Domains' : d}</option>)}
          </select>
          <select value={level} onChange={e => setLevel(e.target.value)}
            className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-xl text-slate-300 text-sm focus:outline-none focus:border-blue-500/50 transition-all">
            {LEVELS.map(l => <option key={l} value={l}>{l === 'All' ? 'All Levels' : l}</option>)}
          </select>
          <select value={type} onChange={e => setType(e.target.value)}
            className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-xl text-slate-300 text-sm focus:outline-none focus:border-blue-500/50 transition-all">
            {TYPES.map(t => <option key={t} value={t}>{t === 'All' ? 'All Types' : t}</option>)}
          </select>
        </div>

        {/* Results count */}
        <p className="text-xs text-slate-500 uppercase tracking-widest">
          Showing <span className="text-white font-semibold">{filtered.length}</span> job{filtered.length !== 1 ? 's' : ''}
        </p>

        {/* Job cards */}
        {filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <span className="text-5xl mb-4">🔍</span>
            <p className="text-white font-semibold text-lg">No jobs found</p>
            <p className="text-slate-400 text-sm mt-1">Try adjusting your filters</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
            {filtered.map(job => (
              <div key={job.id} className="group bg-white/5 border border-white/10 hover:border-white/20 rounded-2xl p-5 flex flex-col gap-3 transition-all hover:bg-white/[0.07]">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center text-lg font-bold text-white shrink-0">
                      {job.company[0]}
                    </div>
                    <div>
                      <h3 className="text-white font-bold text-sm leading-snug">{job.title}</h3>
                      <p className="text-slate-400 text-xs flex items-center gap-1 mt-0.5">
                        <Building2 className="w-3 h-3" />{job.company}
                      </p>
                    </div>
                  </div>
                  {job.hot && (
                    <span className="flex items-center gap-1 px-2 py-0.5 bg-orange-500/20 text-orange-300 border border-orange-500/30 rounded-full text-xs font-semibold shrink-0">
                      <Zap className="w-3 h-3" /> Hot
                    </span>
                  )}
                </div>

                <div className="flex flex-wrap gap-2 text-xs text-slate-400">
                  <span className="flex items-center gap-1"><MapPin className="w-3 h-3 text-violet-400" />{job.location}</span>
                  <span className="flex items-center gap-1"><Clock className="w-3 h-3 text-blue-400" />{job.type}</span>
                </div>

                <div className="flex items-center gap-2 flex-wrap">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${levelColor[job.level]}`}>{job.level}</span>
                  <span className="px-2 py-0.5 bg-white/5 text-slate-400 rounded-full text-xs">{job.domain}</span>
                  <span className="text-emerald-400 text-xs font-semibold ml-auto">{job.salary}</span>
                </div>

                <div className="flex flex-wrap gap-1.5">
                  {job.tags.map(tag => (
                    <span key={tag} className="px-2 py-0.5 bg-white/5 text-slate-400 rounded text-xs">{tag}</span>
                  ))}
                </div>

                <a href={job.url} target="_blank" rel="noopener noreferrer"
                  className="mt-auto flex items-center justify-center gap-2 py-2 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white rounded-xl text-sm font-semibold transition-all hover:scale-[1.02]">
                  Apply Now <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
