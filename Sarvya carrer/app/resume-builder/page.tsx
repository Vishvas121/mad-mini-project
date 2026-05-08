'use client';

import { useUser } from "@clerk/nextjs";
import { useState, useEffect, useRef } from 'react';
import { Download, Eye, EyeOff, Plus, Save, Sparkles, Loader2, X, Trash2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface Experience { id: string; company: string; position: string; startDate: string; endDate: string; description: string; }
interface Education { id: string; school: string; degree: string; field: string; graduationDate: string; }
interface ResumeData {
  fullName: string; email: string; phone: string; location: string;
  linkedin: string; github: string; summary: string;
  experience: Experience[]; education: Education[]; skills: string[];
}

const defaultResume: ResumeData = {
  fullName: '', email: '', phone: '', location: '', linkedin: '', github: '',
  summary: 'Results-driven software engineer with experience building scalable web applications using React, Node.js, and cloud technologies.',
  experience: [{ id: '1', company: 'Tech Corp', position: 'Software Engineer', startDate: 'Jan 2022', endDate: 'Present', description: '• Built full-stack features serving 100K+ users using React and Node.js\n• Improved API response time by 40% through query optimization and Redis caching\n• Led migration to microservices, reducing deployment time by 60%' }],
  education: [{ id: '1', school: 'State University', degree: 'Bachelor of Technology', field: 'Computer Science & Engineering', graduationDate: '2022' }],
  skills: ['React', 'Node.js', 'TypeScript', 'PostgreSQL', 'Docker', 'AWS', 'Git'],
};

const STORAGE_KEY = 'resume_data_v1';

export default function ResumeBuilderPage() {
  const { user, isLoaded } = useUser();
  const [resume, setResume] = useState<ResumeData>(defaultResume);
  const [showPreview, setShowPreview] = useState(true);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [aiFeedback, setAiFeedback] = useState<string | null>(null);
  const [skillInput, setSkillInput] = useState('');
  const [saved, setSaved] = useState(false);
  const previewRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) { try { setResume(JSON.parse(stored)); } catch {} }
  }, []);

  useEffect(() => {
    if (isLoaded && user) {
      setResume(prev => ({
        ...prev,
        fullName: prev.fullName || user.fullName || '',
        email: prev.email || user.primaryEmailAddress?.emailAddress || '',
      }));
    }
  }, [user, isLoaded]);

  function handleSave() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(resume));
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  function handleDownload() {
    const win = window.open('', '_blank');
    if (!win) return;
    const html = `<!DOCTYPE html><html><head><title>${resume.fullName} Resume</title>
<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;font-size:11pt;color:#111;padding:32px 40px;max-width:800px;margin:0 auto}h1{font-size:20pt;font-weight:700;margin-bottom:2px}h2{font-size:9pt;text-transform:uppercase;letter-spacing:1.5px;color:#444;border-bottom:1px solid #ccc;padding-bottom:3px;margin:14px 0 8px}.contact{font-size:9pt;color:#555;margin-bottom:4px}.entry{margin-bottom:10px}.row{display:flex;justify-content:space-between;align-items:baseline}.company{font-size:9pt;color:#555}.desc{font-size:9.5pt;margin-top:3px;white-space:pre-line}.skills{display:flex;flex-wrap:wrap;gap:6px}.skill{background:#f0f0f0;padding:2px 10px;border-radius:3px;font-size:9pt}@media print{body{padding:20px}}</style>
</head><body>
<h1>${resume.fullName}</h1>
<div class="contact">${[resume.email, resume.phone, resume.location].filter(Boolean).join(' | ')}</div>
${resume.linkedin || resume.github ? `<div class="contact">${[resume.linkedin, resume.github].filter(Boolean).join(' | ')}</div>` : ''}
${resume.summary ? `<h2>Professional Summary</h2><p style="font-size:9.5pt">${resume.summary}</p>` : ''}
${resume.experience.length ? `<h2>Experience</h2>${resume.experience.map(e => `<div class="entry"><div class="row"><strong>${e.position}</strong><span style="font-size:9pt;color:#555">${e.startDate} – ${e.endDate}</span></div><div class="company">${e.company}</div><div class="desc">${e.description}</div></div>`).join('')}` : ''}
${resume.education.length ? `<h2>Education</h2>${resume.education.map(e => `<div class="entry"><div class="row"><strong>${e.degree} in ${e.field}</strong><span style="font-size:9pt;color:#555">${e.graduationDate}</span></div><div class="company">${e.school}</div></div>`).join('')}` : ''}
${resume.skills.length ? `<h2>Skills</h2><div class="skills">${resume.skills.map(s => `<span class="skill">${s}</span>`).join('')}</div>` : ''}
</body></html>`;
    win.document.open();
    win.document.write(html);
    win.document.close();
    setTimeout(() => { win.print(); win.close(); }, 500);
  }

  async function handleAnalyze() {
    setIsAnalyzing(true); setAiFeedback(null);
    try {
      const res = await fetch('/api/chat', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ messages: [{ role: 'user', content: `Analyze this resume for ATS optimization and give 5 specific improvements for a software engineering role:\n\n${JSON.stringify(resume, null, 2)}` }] }),
      });
      const reader = res.body?.getReader();
      const decoder = new TextDecoder();
      let text = '';
      while (true) {
        const { done, value } = await reader!.read();
        if (done) break;
        text += decoder.decode(value, { stream: true });
        setAiFeedback(text);
      }
    } catch { setAiFeedback('Failed to analyze. Please try again.'); }
    finally { setIsAnalyzing(false); }
  }

  const set = (field: keyof ResumeData, value: ResumeData[keyof ResumeData]) =>
    setResume(prev => ({ ...prev, [field]: value }));
  const updateExp = (id: string, field: keyof Experience, value: string) =>
    setResume(prev => ({ ...prev, experience: prev.experience.map(e => e.id === id ? { ...e, [field]: value } : e) }));
  const addExp = () => setResume(prev => ({ ...prev, experience: [...prev.experience, { id: Date.now().toString(), company: '', position: '', startDate: '', endDate: 'Present', description: '' }] }));
  const removeExp = (id: string) => setResume(prev => ({ ...prev, experience: prev.experience.filter(e => e.id !== id) }));
  const updateEdu = (id: string, field: keyof Education, value: string) =>
    setResume(prev => ({ ...prev, education: prev.education.map(e => e.id === id ? { ...e, [field]: value } : e) }));
  const addEdu = () => setResume(prev => ({ ...prev, education: [...prev.education, { id: Date.now().toString(), school: '', degree: '', field: '', graduationDate: '' }] }));
  const removeEdu = (id: string) => setResume(prev => ({ ...prev, education: prev.education.filter(e => e.id !== id) }));
  const addSkill = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && skillInput.trim()) {
      const s = skillInput.trim();
      if (!resume.skills.includes(s)) setResume(prev => ({ ...prev, skills: [...prev.skills, s] }));
      setSkillInput('');
    }
  };
  const removeSkill = (s: string) => setResume(prev => ({ ...prev, skills: prev.skills.filter(x => x !== s) }));

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-950 to-black">
      <div className="border-b border-white/10 bg-slate-900/50 backdrop-blur-xl px-4 md:px-8 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-3 flex-wrap">
          <div>
            <h1 className="text-2xl font-bold text-white">Resume Builder</h1>
            <p className="text-slate-400 text-sm">ATS-optimized · Auto-saved to browser</p>
          </div>
          <div className="flex gap-2 flex-wrap">
            <button onClick={() => setShowPreview(p => !p)} className="flex items-center gap-2 px-3 py-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-white text-sm transition-all">
              {showPreview ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              <span className="hidden sm:inline">{showPreview ? 'Hide' : 'Show'} Preview</span>
            </button>
            <button onClick={handleAnalyze} disabled={isAnalyzing} className="flex items-center gap-2 px-3 py-2 bg-violet-500/20 hover:bg-violet-500/30 border border-violet-500/30 text-violet-300 rounded-lg text-sm font-medium transition-all disabled:opacity-50">
              {isAnalyzing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
              AI Review
            </button>
            <button onClick={handleSave} className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all border ${saved ? 'bg-emerald-500/20 border-emerald-500/30 text-emerald-300' : 'bg-white/5 hover:bg-white/10 border-white/10 text-white'}`}>
              <Save className="w-4 h-4" />{saved ? 'Saved!' : 'Save'}
            </button>
            <button onClick={handleDownload} className="flex items-center gap-2 px-3 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg text-sm font-semibold transition-all">
              <Download className="w-4 h-4" /> Download PDF
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto p-4 md:p-6 space-y-4">
        {aiFeedback && (
          <div className="bg-violet-500/10 border border-violet-500/30 rounded-2xl p-5">
            <div className="flex items-center justify-between mb-3">
              <span className="flex items-center gap-2 text-violet-300 font-semibold text-sm"><Sparkles className="w-4 h-4" /> AI Feedback</span>
              <button onClick={() => setAiFeedback(null)} className="text-slate-400 hover:text-white"><X className="w-4 h-4" /></button>
            </div>
            <div className="prose prose-invert prose-sm max-w-none">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{aiFeedback}</ReactMarkdown>
            </div>
          </div>
        )}

        <div className={`grid gap-6 ${showPreview ? 'grid-cols-1 lg:grid-cols-2' : 'grid-cols-1 max-w-2xl'}`}>
          <div className="space-y-4 max-h-[calc(100vh-180px)] overflow-y-auto pr-1">
            <Card title="Personal Information">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Field label="Full Name" value={resume.fullName} onChange={v => set('fullName', v)} />
                <Field label="Email" value={resume.email} onChange={v => set('email', v)} type="email" />
                <Field label="Phone" value={resume.phone} onChange={v => set('phone', v)} />
                <Field label="Location" value={resume.location} onChange={v => set('location', v)} placeholder="City, State" />
                <Field label="LinkedIn" value={resume.linkedin} onChange={v => set('linkedin', v)} placeholder="linkedin.com/in/..." />
                <Field label="GitHub" value={resume.github} onChange={v => set('github', v)} placeholder="github.com/..." />
              </div>
            </Card>

            <Card title="Professional Summary">
              <textarea value={resume.summary} onChange={e => set('summary', e.target.value)} rows={4}
                className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-blue-500/50 resize-none transition-all"
                placeholder="Results-driven engineer with X years of experience..." />
              <p className="text-slate-500 text-xs mt-1">Tip: Include your role, years of experience, and 2-3 key skills</p>
            </Card>

            <Card title="Experience">
              <div className="space-y-4">
                {resume.experience.map(exp => (
                  <div key={exp.id} className="bg-white/5 border border-white/10 rounded-xl p-4 space-y-3">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <Field label="Company" value={exp.company} onChange={v => updateExp(exp.id, 'company', v)} />
                      <Field label="Position / Title" value={exp.position} onChange={v => updateExp(exp.id, 'position', v)} />
                      <Field label="Start Date" value={exp.startDate} onChange={v => updateExp(exp.id, 'startDate', v)} placeholder="Jan 2022" />
                      <Field label="End Date" value={exp.endDate} onChange={v => updateExp(exp.id, 'endDate', v)} placeholder="Present" />
                    </div>
                    <div>
                      <label className="block text-xs text-slate-400 mb-1">Description (use bullet points starting with action verbs)</label>
                      <textarea value={exp.description} onChange={e => updateExp(exp.id, 'description', e.target.value)} rows={3}
                        className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-blue-500/50 resize-none transition-all"
                        placeholder={"• Built X that achieved Y result\n• Led team of N engineers to deliver Z\n• Improved performance by X%"} />
                    </div>
                    <button onClick={() => removeExp(exp.id)} className="flex items-center gap-1 text-red-400 hover:text-red-300 text-xs font-medium transition-colors">
                      <Trash2 className="w-3 h-3" /> Remove
                    </button>
                  </div>
                ))}
                <button onClick={addExp} className="w-full py-2 border-2 border-dashed border-white/15 hover:border-white/30 text-slate-400 hover:text-white rounded-lg text-sm transition-all flex items-center justify-center gap-2">
                  <Plus className="w-4 h-4" /> Add Experience
                </button>
              </div>
            </Card>

            <Card title="Education">
              <div className="space-y-4">
                {resume.education.map(edu => (
                  <div key={edu.id} className="bg-white/5 border border-white/10 rounded-xl p-4 space-y-3">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <Field label="School / University" value={edu.school} onChange={v => updateEdu(edu.id, 'school', v)} />
                      <Field label="Degree" value={edu.degree} onChange={v => updateEdu(edu.id, 'degree', v)} placeholder="B.Tech / B.E." />
                      <Field label="Field of Study" value={edu.field} onChange={v => updateEdu(edu.id, 'field', v)} placeholder="Computer Science" />
                      <Field label="Graduation Year" value={edu.graduationDate} onChange={v => updateEdu(edu.id, 'graduationDate', v)} placeholder="2024" />
                    </div>
                    <button onClick={() => removeEdu(edu.id)} className="flex items-center gap-1 text-red-400 hover:text-red-300 text-xs font-medium transition-colors">
                      <Trash2 className="w-3 h-3" /> Remove
                    </button>
                  </div>
                ))}
                <button onClick={addEdu} className="w-full py-2 border-2 border-dashed border-white/15 hover:border-white/30 text-slate-400 hover:text-white rounded-lg text-sm transition-all flex items-center justify-center gap-2">
                  <Plus className="w-4 h-4" /> Add Education
                </button>
              </div>
            </Card>

            <Card title="Skills">
              <div className="flex flex-wrap gap-2 mb-3 min-h-[32px]">
                {resume.skills.map(skill => (
                  <span key={skill} className="flex items-center gap-1.5 px-3 py-1 bg-blue-500/20 border border-blue-500/30 text-blue-300 rounded-full text-xs font-medium">
                    {skill}
                    <button onClick={() => removeSkill(skill)} className="hover:text-white transition-colors"><X className="w-3 h-3" /></button>
                  </span>
                ))}
              </div>
              <input value={skillInput} onChange={e => setSkillInput(e.target.value)} onKeyDown={addSkill}
                className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-blue-500/50 transition-all"
                placeholder="Type a skill and press Enter (e.g. React, Python, AWS)..." />
              <p className="text-slate-500 text-xs mt-1">Tip: Match skills exactly to the job description for ATS</p>
            </Card>
          </div>

          {showPreview && (
            <div className="sticky top-4 max-h-[calc(100vh-180px)] overflow-y-auto">
              <p className="text-xs text-slate-500 mb-2 text-center uppercase tracking-widest">ATS Preview</p>
              <div ref={previewRef} className="bg-white text-black rounded-xl shadow-2xl" style={{padding:'32px 40px', fontFamily:'Arial,sans-serif', fontSize:'11pt', lineHeight:'1.4'}}>
                <div style={{borderBottom:'2px solid #111', paddingBottom:'10px', marginBottom:'12px'}}>
                  <h1 style={{fontSize:'20pt', fontWeight:'700', margin:'0 0 3px'}}>{resume.fullName || 'Your Name'}</h1>
                  <p style={{fontSize:'9pt', color:'#555', margin:'0'}}>{[resume.email, resume.phone, resume.location].filter(Boolean).join(' | ')}</p>
                  {(resume.linkedin || resume.github) && <p style={{fontSize:'9pt', color:'#555', margin:'2px 0 0'}}>{[resume.linkedin, resume.github].filter(Boolean).join(' | ')}</p>}
                </div>
                {resume.summary && (
                  <div style={{marginBottom:'12px'}}>
                    <h2 style={{fontSize:'9pt', textTransform:'uppercase', letterSpacing:'1.5px', color:'#444', borderBottom:'1px solid #ccc', paddingBottom:'2px', margin:'0 0 6px'}}>Professional Summary</h2>
                    <p style={{fontSize:'9.5pt', margin:'0'}}>{resume.summary}</p>
                  </div>
                )}
                {resume.experience.length > 0 && (
                  <div style={{marginBottom:'12px'}}>
                    <h2 style={{fontSize:'9pt', textTransform:'uppercase', letterSpacing:'1.5px', color:'#444', borderBottom:'1px solid #ccc', paddingBottom:'2px', margin:'0 0 8px'}}>Experience</h2>
                    {resume.experience.map(exp => (
                      <div key={exp.id} style={{marginBottom:'10px'}}>
                        <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline'}}>
                          <strong style={{fontSize:'10.5pt'}}>{exp.position || 'Position'}</strong>
                          <span style={{fontSize:'9pt', color:'#555'}}>{exp.startDate} – {exp.endDate}</span>
                        </div>
                        <div style={{fontSize:'9pt', color:'#555', marginBottom:'3px'}}>{exp.company}</div>
                        <div style={{fontSize:'9.5pt', whiteSpace:'pre-line'}}>{exp.description}</div>
                      </div>
                    ))}
                  </div>
                )}
                {resume.education.length > 0 && (
                  <div style={{marginBottom:'12px'}}>
                    <h2 style={{fontSize:'9pt', textTransform:'uppercase', letterSpacing:'1.5px', color:'#444', borderBottom:'1px solid #ccc', paddingBottom:'2px', margin:'0 0 8px'}}>Education</h2>
                    {resume.education.map(edu => (
                      <div key={edu.id} style={{marginBottom:'8px'}}>
                        <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline'}}>
                          <strong style={{fontSize:'10.5pt'}}>{edu.degree}{edu.field ? ` in ${edu.field}` : ''}</strong>
                          <span style={{fontSize:'9pt', color:'#555'}}>{edu.graduationDate}</span>
                        </div>
                        <div style={{fontSize:'9pt', color:'#555'}}>{edu.school}</div>
                      </div>
                    ))}
                  </div>
                )}
                {resume.skills.length > 0 && (
                  <div>
                    <h2 style={{fontSize:'9pt', textTransform:'uppercase', letterSpacing:'1.5px', color:'#444', borderBottom:'1px solid #ccc', paddingBottom:'2px', margin:'0 0 8px'}}>Skills</h2>
                    <p style={{fontSize:'9.5pt', margin:'0'}}>{resume.skills.join(' • ')}</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="bg-white/[0.03] border border-white/10 rounded-2xl p-5">
      <h2 className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-4">{title}</h2>
      {children}
    </div>
  );
}

function Field({ label, value, onChange, placeholder, type = 'text' }: {
  label: string; value: string; onChange?: (v: string) => void; placeholder?: string; type?: string;
}) {
  return (
    <div>
      <label className="block text-xs text-slate-400 mb-1">{label}</label>
      <input type={type} value={value} onChange={e => onChange?.(e.target.value)} placeholder={placeholder}
        className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white text-sm placeholder-slate-600 focus:outline-none focus:border-blue-500/50 transition-all" />
    </div>
  );
}