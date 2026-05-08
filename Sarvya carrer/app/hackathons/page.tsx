'use client';

import { hackathons, type Hackathon, type HackathonPlatform } from '@/lib/constants';
import { Calendar, ExternalLink, MapPin, Plus, Search, Tag, Trophy, Users, X } from 'lucide-react';
import { useMemo, useState } from 'react';

const platformConfig: Record<HackathonPlatform, { label: string; color: string; badge: string }> = {
  unstop:     { label: 'Unstop',     color: 'from-orange-500 to-yellow-500', badge: 'bg-orange-500/20 text-orange-300 border border-orange-500/30' },
  hack2skill: { label: 'Hack2Skill', color: 'from-violet-500 to-blue-500',   badge: 'bg-violet-500/20 text-violet-300 border border-violet-500/30' },
  devfolio:   { label: 'Devfolio',   color: 'from-blue-500 to-cyan-500',     badge: 'bg-blue-500/20 text-blue-300 border border-blue-500/30' },
  other:      { label: 'Official',   color: 'from-slate-500 to-slate-600',   badge: 'bg-slate-500/20 text-slate-300 border border-slate-500/30' },
};

const statusConfig = {
  upcoming: { label: 'Upcoming', cls: 'bg-emerald-500/20 text-emerald-300' },
  ongoing:  { label: 'Live Now', cls: 'bg-yellow-500/20 text-yellow-300' },
  past:     { label: 'Ended',    cls: 'bg-slate-500/20 text-slate-400' },
};

const ALL_DOMAINS = Array.from(new Set(hackathons.map(h => h.domain))).sort();
const ALL_PLATFORMS: HackathonPlatform[] = ['unstop', 'hack2skill', 'devfolio', 'other'];
type Tab = 'cards' | 'unstop' | 'hack2skill';
interface SuggestForm { title: string; organizer: string; url: string; date: string; prize: string; domain: string; }
const EMPTY_FORM: SuggestForm = { title: '', organizer: '', url: '', date: '', prize: '', domain: '' };

export default function HackathonsPage() {
  const [activeTab, setActiveTab]     = useState<Tab>('cards');
  const [search, setSearch]           = useState('');
  const [platform, setPlatform]       = useState<HackathonPlatform | 'all'>('all');
  const [domain, setDomain]           = useState('all');
  const [status, setStatus]           = useState<'all' | 'upcoming' | 'ongoing'>('all');
  const [showSuggest, setShowSuggest] = useState(false);
  const [form, setForm]               = useState<SuggestForm>(EMPTY_FORM);
  const [submitted, setSubmitted]     = useState(false);
  const [custom, setCustom]           = useState<Hackathon[]>([]);
  const allHackathons = useMemo(() => [...hackathons, ...custom], [custom]);
  const filtered = useMemo(() => allHackathons.filter(h => {
    const q = search.toLowerCase();
    return (!q || h.title.toLowerCase().includes(q) || h.organizer.toLowerCase().includes(q) || h.tags.some(t => t.toLowerCase().includes(q))) &&
      (platform === 'all' || h.platform === platform) &&
      (domain === 'all' || h.domain === domain) &&
      (status === 'all' || h.status === status);
  }), [allHackathons, search, platform, domain, status]);

  function handleSuggest(e: React.FormEvent) {
    e.preventDefault();
    setCustom(prev => [{ id: Date.now(), title: form.title, organizer: form.organizer, date: form.date, location: 'Virtual', prize: form.prize || 'TBA', participants: 0, status: 'upcoming' as const, platform: 'other' as const, url: form.url, tags: [form.domain || 'Open Innovation'], domain: form.domain || 'Open Innovation', teamSize: 'TBA', description: 'Community-submitted hackathon.' }, ...prev]);
    setSubmitted(true);
    setTimeout(() => { setShowSuggest(false); setSubmitted(false); setForm(EMPTY_FORM); }, 1800);
  }

  const activeCount = allHackathons.filter(h => h.status !== 'past').length;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-950 to-black">
      <div className="border-b border-white/10 bg-slate-900/50 backdrop-blur-xl">
        <div className="max-w-6xl mx-auto px-4 md:px-8 py-10">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
              <h1 className="text-4xl md:text-5xl font-bold text-white mb-2">
                Hackathons &amp; <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-violet-400">Competitions</span>
              </h1>
              <p className="text-slate-400">{activeCount} active opportunities across India and globally</p>
            </div>
            <button onClick={() => setShowSuggest(true)} className="flex items-center gap-2 px-4 py-2.5 bg-blue-500/20 hover:bg-blue-500/30 text-blue-300 border border-blue-500/30 rounded-xl text-sm font-semibold transition-colors shrink-0">
              <Plus className="w-4 h-4" /> Add Hackathon
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 md:px-8 py-8 space-y-6">
        <div className="flex flex-wrap gap-1 border-b border-white/10">
          {(['cards', 'unstop', 'hack2skill'] as Tab[]).map(key => (
            <button key={key} onClick={() => setActiveTab(key)}
              className={`px-5 py-2.5 font-semibold text-sm rounded-t-lg transition-all duration-200 ${activeTab === key ? 'bg-blue-500/20 text-blue-300 border-b-2 border-blue-400' : 'text-slate-400 hover:text-white'}`}>
              {key === 'cards' ? '🏆 All Hackathons' : key === 'unstop' ? '🔶 Unstop' : '🟣 Hack2Skill'}
            </button>
          ))}
        </div>

        {activeTab === 'cards' && (
          <>
            <div className="flex flex-col md:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search hackathons, organizers, tags..."
                  className="w-full pl-9 pr-4 py-2.5 bg-white/5 border border-white/10 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:border-blue-500/50 transition-all" />
              </div>
              <select value={domain} onChange={e => setDomain(e.target.value)} className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-xl text-slate-300 text-sm focus:outline-none">
                <option value="all">All Domains</option>
                {ALL_DOMAINS.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
              <select value={status} onChange={e => setStatus(e.target.value as typeof status)} className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-xl text-slate-300 text-sm focus:outline-none">
                <option value="all">All Status</option>
                <option value="upcoming">Upcoming</option>
                <option value="ongoing">Live Now</option>
              </select>
            </div>
            <div className="flex gap-2 flex-wrap">
              {(['all', ...ALL_PLATFORMS] as const).map(p => (
                <button key={p} onClick={() => setPlatform(p)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold transition-all ${platform === p ? 'bg-blue-500 text-white' : 'bg-white/5 text-slate-400 hover:bg-white/10 hover:text-white'}`}>
                  {p === 'all' ? 'All Platforms' : platformConfig[p].label}
                </button>
              ))}
            </div>
            <p className="text-xs text-slate-500 uppercase tracking-widest">
              Showing <span className="text-white font-semibold">{filtered.length}</span> hackathon{filtered.length !== 1 ? 's' : ''}
            </p>
            {filtered.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-20 text-center">
                <span className="text-5xl mb-4">🔍</span>
                <p className="text-white font-semibold text-lg">No hackathons found</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                {filtered.map(h => {
                  const pc = platformConfig[h.platform] ?? platformConfig.other;
                  const sc = statusConfig[h.status] ?? statusConfig.upcoming;
                  return (
                    <div key={h.id} className="group bg-white/5 border border-white/10 hover:border-white/20 p-5 rounded-2xl transition-all flex flex-col gap-3">
                      <div className="flex items-start justify-between gap-2">
                        <span className={`self-start px-2.5 py-1 rounded-full text-xs font-semibold ${pc.badge}`}>{pc.label}</span>
                        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${sc.cls}`}>{sc.label}</span>
                      </div>
                      <div>
                        <h3 className="text-base font-bold text-white leading-snug">{h.title}</h3>
                        <p className="text-slate-500 text-xs mt-1">{h.organizer}</p>
                      </div>
                      <p className="text-slate-400 text-xs leading-relaxed line-clamp-2">{h.description}</p>
                      <div className="flex items-center gap-2 text-yellow-400 font-semibold text-sm">
                        <Trophy className="w-4 h-4 shrink-0" /><span>{h.prize}</span>
                      </div>
                      <div className="space-y-1.5 text-slate-400 text-xs">
                        <div className="flex items-center gap-2"><Calendar className="w-3.5 h-3.5 text-blue-400 shrink-0" /><span>{h.date}</span></div>
                        <div className="flex items-center gap-2"><MapPin className="w-3.5 h-3.5 text-violet-400 shrink-0" /><span>{h.location}</span></div>
                        <div className="flex items-center gap-2"><Users className="w-3.5 h-3.5 text-cyan-400 shrink-0" /><span>{h.participants > 0 ? h.participants.toLocaleString() + '+ participants' : 'Team: ' + h.teamSize}</span></div>
                      </div>
                      <div className="flex flex-wrap gap-1">
                        {h.tags.slice(0, 3).map(tag => (
                          <span key={tag} className="flex items-center gap-1 px-2 py-0.5 bg-white/5 rounded-full text-xs text-slate-400">
                            <Tag className="w-2.5 h-2.5" />{tag}
                          </span>
                        ))}
                      </div>
                      <div className="flex items-center justify-between pt-3 border-t border-white/10 mt-auto">
                        <span className="text-xs text-slate-500">Team: {h.teamSize}</span>
                        <a href={h.url} target="_blank" rel="noopener noreferrer"
                          className={`flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r ${pc.color} text-white rounded-lg font-semibold text-xs transition-all hover:opacity-90 hover:scale-105`}>
                          Register <ExternalLink className="w-3 h-3" />
                        </a>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}

        {activeTab === 'unstop' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold text-white">🔶 Unstop Hackathons</h2>
                <p className="text-slate-400 text-sm mt-1">India's largest student competition platform</p>
              </div>
              <a href="https://unstop.com/hackathons" target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2 bg-orange-500/20 text-orange-300 border border-orange-500/30 rounded-lg text-sm font-semibold hover:bg-orange-500/30 transition-colors">
                Browse All on Unstop <ExternalLink className="w-4 h-4" />
              </a>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {[
                { title: 'Smart India Hackathon 2025', prize: '₹1,00,000', date: 'Aug 20-22, 2025', tags: ['AI', 'GovTech'], url: 'https://unstop.com/hackathons/smart-india-hackathon' },
                { title: 'HackWithInfy 2025', prize: '₹5,00,000', date: 'Jul 10-12, 2025', tags: ['Full Stack', 'Cloud'], url: 'https://unstop.com/hackathons/hackwithinfy' },
                { title: 'Flipkart Grid 6.0', prize: '₹3,00,000', date: 'Jun-Sep 2025', tags: ['ML', 'E-Commerce'], url: 'https://unstop.com/hackathons/flipkart-grid-6' },
                { title: 'Tata Imagination Challenge', prize: '₹2,00,000 + PPO', date: 'Sep 2025', tags: ['Sustainability', 'IoT'], url: 'https://unstop.com/hackathons' },
                { title: 'Myntra HackerRamp 2025', prize: '₹2,50,000', date: 'Jul-Aug 2025', tags: ['Fashion Tech', 'ML'], url: 'https://unstop.com/hackathons' },
                { title: 'Walmart Sparkathon 2025', prize: '₹3,00,000 + Internship', date: 'Aug 2025', tags: ['Retail Tech', 'AI'], url: 'https://unstop.com/hackathons' },
              ].map(h => (
                <a key={h.title} href={h.url} target="_blank" rel="noopener noreferrer"
                  className="group flex flex-col gap-3 p-5 bg-orange-500/5 border border-orange-500/20 hover:border-orange-500/40 rounded-2xl transition-all hover:bg-orange-500/10">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-white font-bold text-sm leading-snug">{h.title}</h3>
                    <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-orange-400 transition-colors shrink-0" />
                  </div>
                  <div className="flex items-center gap-2 text-yellow-400 text-sm font-semibold"><Trophy className="w-4 h-4" />{h.prize}</div>
                  <div className="flex items-center gap-2 text-slate-400 text-xs"><Calendar className="w-3.5 h-3.5 text-blue-400" />{h.date}</div>
                  <div className="flex flex-wrap gap-1.5">{h.tags.map(t => <span key={t} className="px-2 py-0.5 bg-white/5 text-slate-400 rounded-full text-xs">{t}</span>)}</div>
                  <span className="text-orange-400 text-xs font-semibold mt-auto">Register on Unstop →</span>
                </a>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'hack2skill' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold text-white">🟣 Hack2Skill Hackathons</h2>
                <p className="text-slate-400 text-sm mt-1">Premier platform for tech challenges and hackathons</p>
              </div>
              <a href="https://hack2skill.com/hack/hackathons" target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2 bg-violet-500/20 text-violet-300 border border-violet-500/30 rounded-lg text-sm font-semibold hover:bg-violet-500/30 transition-colors">
                Browse All on Hack2Skill <ExternalLink className="w-4 h-4" />
              </a>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {[
                { title: 'Hack2Skill GenAI Hackathon', prize: '$10,000', date: 'May 25-27, 2025', tags: ['GenAI', 'LLMs'], url: 'https://hack2skill.com/hack/hackathons' },
                { title: 'Google Solution Challenge 2025', prize: '$3,000 + Mentorship', date: 'Jan-Apr 2025', tags: ['Google Cloud', 'SDGs'], url: 'https://hack2skill.com/hack/google-solution-challenge' },
                { title: 'Microsoft Imagine Cup 2025', prize: '$100,000', date: 'Oct 2025', tags: ['Azure', 'AI'], url: 'https://hack2skill.com/hack/microsoft-imagine-cup' },
                { title: 'AWS DeepRacer Student League', prize: 'AWS Credits', date: 'Ongoing 2025', tags: ['AWS', 'ML'], url: 'https://hack2skill.com/hack/aws-deepracer' },
                { title: 'NASSCOM AI Gamechangers', prize: '₹10,00,000', date: 'Oct 2025', tags: ['AI', 'Deep Tech'], url: 'https://hack2skill.com/hack/hackathons' },
                { title: 'HackRx 5.0 by Bajaj Finserv', prize: '₹5,00,000', date: 'Sep 10-12, 2025', tags: ['HealthTech', 'NLP'], url: 'https://hack2skill.com/hack/hackathons' },
              ].map(h => (
                <a key={h.title} href={h.url} target="_blank" rel="noopener noreferrer"
                  className="group flex flex-col gap-3 p-5 bg-violet-500/5 border border-violet-500/20 hover:border-violet-500/40 rounded-2xl transition-all hover:bg-violet-500/10">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-white font-bold text-sm leading-snug">{h.title}</h3>
                    <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-violet-400 transition-colors shrink-0" />
                  </div>
                  <div className="flex items-center gap-2 text-yellow-400 text-sm font-semibold"><Trophy className="w-4 h-4" />{h.prize}</div>
                  <div className="flex items-center gap-2 text-slate-400 text-xs"><Calendar className="w-3.5 h-3.5 text-blue-400" />{h.date}</div>
                  <div className="flex flex-wrap gap-1.5">{h.tags.map(t => <span key={t} className="px-2 py-0.5 bg-white/5 text-slate-400 rounded-full text-xs">{t}</span>)}</div>
                  <span className="text-violet-400 text-xs font-semibold mt-auto">Register on Hack2Skill →</span>
                </a>
              ))}
            </div>
          </div>
        )}
      </div>

      {showSuggest && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-slate-900 border border-white/10 rounded-2xl p-6 w-full max-w-md shadow-2xl">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-xl font-bold text-white">Add a Hackathon</h2>
              <button onClick={() => setShowSuggest(false)} className="text-slate-400 hover:text-white"><X className="w-5 h-5" /></button>
            </div>
            {submitted ? (
              <div className="text-center py-8">
                <span className="text-5xl">🎉</span>
                <p className="text-white font-semibold mt-3">Hackathon added!</p>
              </div>
            ) : (
              <form onSubmit={handleSuggest} className="space-y-4">
                {([
                  ['title', 'Hackathon Name *', 'text', true],
                  ['organizer', 'Organizer *', 'text', true],
                  ['url', 'Registration URL *', 'url', true],
                  ['date', 'Date / Deadline', 'text', false],
                  ['prize', 'Prize Pool', 'text', false],
                  ['domain', 'Domain / Track', 'text', false],
                ] as [keyof SuggestForm, string, string, boolean][]).map(([key, label, type, req]) => (
                  <div key={key}>
                    <label className="block text-xs text-slate-400 mb-1">{label}</label>
                    <input type={type} required={req} value={form[key]} onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
                      className="w-full px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-white text-sm focus:outline-none focus:border-blue-500/50 transition-all" />
                  </div>
                ))}
                <button type="submit" className="w-full py-2.5 bg-gradient-to-r from-blue-500 to-violet-500 text-white rounded-xl font-semibold text-sm hover:opacity-90 transition-opacity mt-2">
                  Add Hackathon
                </button>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
