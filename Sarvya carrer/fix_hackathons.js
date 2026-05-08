const fs = require('fs');

const content = `'use client';

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
const EMPTY_FORM: SuggestForm = { 