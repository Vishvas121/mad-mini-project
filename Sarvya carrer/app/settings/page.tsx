'use client';

import { useUser, SignOutButton } from "@clerk/nextjs";
import { useTheme } from "@/app/providers";
import {
    User,
    Settings,
    Palette,
    Bell,
    LogOut,
    Shield,
    Smartphone,
    Check,
    Moon,
    Sun,
    Zap,
    Star
} from 'lucide-react';
import { useState } from "react";
import Image from "next/image";

const themes = [
    { id: 'light', name: 'Standard Light', icon: <Sun className="w-5 h-5" />, color: 'bg-white', text: 'text-slate-900' },
    { id: 'dark', name: 'Standard Dark', icon: <Moon className="w-5 h-5" />, color: 'bg-slate-900', text: 'text-white' },
    { id: 'midnight', name: 'Midnight Blue', icon: <Zap className="w-5 h-5 text-blue-400" />, color: 'bg-[#0f172a]', text: 'text-blue-50' },
    { id: 'obsidian', name: 'Obsidian Night', icon: <Star className="w-5 h-5 text-purple-400" />, color: 'bg-[#09090b]', text: 'text-slate-50' },
    { id: 'sunset', name: 'Sunset Glow', icon: <Palette className="w-5 h-5 text-orange-400" />, color: 'bg-[#1c1917]', text: 'text-orange-50' },
];

export default function SettingsPage() {
    const { user, isLoaded } = useUser();
    const {
        theme, setTheme,
        accentColor, setAccentColor,
        glassIntensity, setGlassIntensity,
        animations, setAnimations
    } = useTheme();
    const [activeTab, setActiveTab] = useState('profile');

    if (!isLoaded) return null;

    return (
        <div className="min-h-screen bg-background p-4 md:p-8">
            <div className="max-w-6xl mx-auto">
                <header className="mb-8">
                    <h1 className="text-3xl font-bold text-foreground mb-2 flex items-center gap-2">
                        <Settings className="w-8 h-8 text-primary" /> Settings
                    </h1>
                    <p className="text-muted-foreground">Manage your account preferences and customize your experience.</p>
                </header>

                <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                    {/* Sidebar Navigation */}
                    <aside className="space-y-2">
                        <button
                            onClick={() => setActiveTab('profile')}
                            className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all ${activeTab === 'profile' ? 'bg-primary/10 text-primary border border-primary/20' : 'text-muted-foreground hover:bg-muted'
                                }`}
                        >
                            <User className="w-5 h-5" /> Profile
                        </button>
                        <button
                            onClick={() => setActiveTab('appearance')}
                            className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all ${activeTab === 'appearance' ? 'bg-primary/10 text-primary border border-primary/20' : 'text-muted-foreground hover:bg-muted'
                                }`}
                        >
                            <Palette className="w-5 h-5" /> Appearance
                        </button>
                        <button
                            onClick={() => setActiveTab('notifications')}
                            className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all ${activeTab === 'notifications' ? 'bg-primary/10 text-primary border border-primary/20' : 'text-muted-foreground hover:bg-muted'
                                }`}
                        >
                            <Bell className="w-5 h-5" /> Notifications
                        </button>
                        <button
                            onClick={() => setActiveTab('security')}
                            className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all ${activeTab === 'security' ? 'bg-primary/10 text-primary border border-primary/20' : 'text-muted-foreground hover:bg-muted'
                                }`}
                        >
                            <Shield className="w-5 h-5" /> Security
                        </button>

                        <div className="pt-4 mt-4 border-t border-border">
                            <SignOutButton>
                                <button className="w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium text-rose-500 hover:bg-rose-500/10 transition-all">
                                    <LogOut className="w-5 h-5" /> Logout
                                </button>
                            </SignOutButton>
                        </div>
                    </aside>

                    {/* Main Content */}
                    <main className="lg:col-span-3 space-y-8">
                        {activeTab === 'profile' && (
                            <section className="glass-lg p-6 md:p-8 rounded-2xl border border-border">
                                <h2 className="text-xl font-bold text-foreground mb-6">Profile Information</h2>
                                <div className="flex flex-col md:flex-row items-center gap-8 mb-8">
                                    <div className="relative group">
                                        <div className="w-24 h-24 rounded-full overflow-hidden border-4 border-primary/20 group-hover:border-primary/40 transition-all">
                                            {user?.imageUrl ? (
                                                <Image src={user.imageUrl} alt="Avatar" width={96} height={96} className="object-cover" />
                                            ) : (
                                                <div className="w-full h-full bg-muted flex items-center justify-center">
                                                    <User className="w-12 h-12 text-muted-foreground" />
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex-1 space-y-4 text-center md:text-left">
                                        <div>
                                            <label className="text-sm font-medium text-muted-foreground uppercase tracking-wider">Full Name</label>
                                            <div className="text-xl font-bold text-foreground">{user?.fullName || 'Anonymous User'}</div>
                                        </div>
                                        <div>
                                            <label className="text-sm font-medium text-muted-foreground uppercase tracking-wider">Email Address</label>
                                            <div className="text-foreground">{user?.primaryEmailAddress?.emailAddress}</div>
                                        </div>
                                    </div>
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div className="p-4 bg-muted/30 rounded-xl border border-border">
                                        <div className="text-sm font-medium text-muted-foreground mb-1">Account ID</div>
                                        <div className="font-mono text-xs truncate text-foreground">{user?.id}</div>
                                    </div>
                                    <div className="p-4 bg-muted/30 rounded-xl border border-border">
                                        <div className="text-sm font-medium text-muted-foreground mb-1">Joined Date</div>
                                        <div className="text-foreground">{user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}</div>
                                    </div>
                                </div>
                            </section>
                        )}

                        {activeTab === 'appearance' && (
                            <section className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
                                {/* Theme Selection */}
                                <div className="glass-lg p-6 md:p-8 rounded-2xl border border-border">
                                    <h2 className="text-xl font-bold text-foreground mb-2 flex items-center gap-2">
                                        <Palette className="w-5 h-5 text-primary" /> Interface Themes
                                    </h2>
                                    <p className="text-muted-foreground mb-8 text-sm">Choose between different premium visual styles for your dashboard.</p>

                                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                        {themes.map((t) => (
                                            <button
                                                key={t.id}
                                                onClick={() => setTheme(t.id as any)}
                                                className={`relative flex items-center justify-between p-4 rounded-xl border-2 transition-all group ${theme === t.id
                                                    ? 'border-primary ring-2 ring-primary/20 bg-primary/5'
                                                    : 'border-border hover:border-muted-foreground/30 bg-muted/20'
                                                    }`}
                                            >
                                                <div className="flex items-center gap-4">
                                                    <div className={`p-2 rounded-lg transition-transform group-hover:scale-110 ${t.id === 'light' ? 'bg-slate-100 text-slate-600' : 'bg-slate-800'}`}>
                                                        {t.icon}
                                                    </div>
                                                    <div className="font-bold text-foreground">{t.name}</div>
                                                </div>
                                                {theme === t.id && (
                                                    <Check className="w-5 h-5 text-primary animate-in zoom-in" />
                                                )}

                                                <div className="absolute right-4 bottom-4 flex gap-1">
                                                    <div className={`w-3 h-3 rounded-full ${t.color}`} />
                                                    <div className="w-3 h-3 rounded-full bg-primary" />
                                                </div>
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                {/* Accent Color Selection */}
                                <div className="glass-lg p-6 md:p-8 rounded-2xl border border-border">
                                    <h2 className="text-xl font-bold text-foreground mb-2 flex items-center gap-2">
                                        <div className="w-5 h-5 rounded-full bg-primary" /> Accent Color
                                    </h2>
                                    <p className="text-muted-foreground mb-8 text-sm">Personalize your workspace with a unique primary color accent.</p>

                                    <div className="flex flex-wrap gap-4">
                                        {[
                                            { id: 'blue', label: 'Classic Blue', hex: 'bg-blue-500' },
                                            { id: 'indigo', label: 'Royal Indigo', hex: 'bg-indigo-500' },
                                            { id: 'violet', label: 'Ethereal Violet', hex: 'bg-violet-500' },
                                            { id: 'rose', label: 'Deep Rose', hex: 'bg-rose-500' },
                                            { id: 'emerald', label: 'Vibrant Emerald', hex: 'bg-emerald-500' },
                                        ].map((color) => (
                                            <button
                                                key={color.id}
                                                onClick={() => setAccentColor(color.id as any)}
                                                className={`group flex flex-col items-center gap-2 p-3 rounded-xl border-2 transition-all ${accentColor === color.id ? 'border-primary bg-primary/5' : 'border-border hover:bg-muted'}`}
                                            >
                                                <div className={`w-10 h-10 rounded-full ${color.hex} shadow-lg group-hover:scale-110 transition-transform flex items-center justify-center`}>
                                                    {accentColor === color.id && <Check className="w-6 h-6 text-white" />}
                                                </div>
                                                <span className="text-xs font-semibold text-foreground">{color.label}</span>
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                {/* Advanced UI Options */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    {/* Glassmorphism Intensity */}
                                    <div className="glass-lg p-6 rounded-2xl border border-border">
                                        <h3 className="text-lg font-bold text-foreground mb-1">Glass Intensity</h3>
                                        <p className="text-sm text-muted-foreground mb-6">Adjust the depth and blur of UI elements.</p>
                                        <div className="flex gap-2 p-1 bg-muted/30 rounded-lg">
                                            {['low', 'medium', 'high'].map((intensity) => (
                                                <button
                                                    key={intensity}
                                                    onClick={() => setGlassIntensity(intensity as any)}
                                                    className={`flex-1 py-2 text-xs font-bold rounded-md transition-all ${glassIntensity === intensity
                                                            ? 'bg-primary text-primary-foreground shadow-md'
                                                            : 'text-muted-foreground hover:text-foreground'
                                                        }`}
                                                >
                                                    {intensity.toUpperCase()}
                                                </button>
                                            ))}
                                        </div>
                                    </div>

                                    {/* Visual Motion */}
                                    <div className="glass-lg p-6 rounded-2xl border border-border">
                                        <h3 className="text-lg font-bold text-foreground mb-1">Visual Motion</h3>
                                        <p className="text-sm text-muted-foreground mb-6">Control the speed and frequency of site animations.</p>
                                        <div className="flex gap-2 p-1 bg-muted/30 rounded-lg">
                                            {['none', 'subtle', 'full'].map((level) => (
                                                <button
                                                    key={level}
                                                    onClick={() => setAnimations(level as any)}
                                                    className={`flex-1 py-2 text-xs font-bold rounded-md transition-all ${animations === level
                                                            ? 'bg-primary text-primary-foreground shadow-md'
                                                            : 'text-muted-foreground hover:text-foreground'
                                                        }`}
                                                >
                                                    {level.toUpperCase()}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            </section>
                        )}

                        {activeTab === 'notifications' && (
                            <section className="glass-lg p-6 md:p-8 rounded-2xl border border-border">
                                <h2 className="text-xl font-bold text-foreground mb-6">Notification Preferences</h2>
                                <div className="space-y-6">
                                    {[
                                        { title: "Course Updates", desc: "Get notified when new content is added to your courses." },
                                        { title: "Career Alerts", desc: "Weekly summaries of job market trends and resume tips." },
                                        { title: "System Alerts", desc: "Important updates about your account and platform maintenance." }
                                    ].map((item, i) => (
                                        <div key={i} className="flex items-center justify-between p-4 bg-muted/20 rounded-xl border border-border">
                                            <div>
                                                <div className="font-bold text-foreground">{item.title}</div>
                                                <div className="text-sm text-muted-foreground">{item.desc}</div>
                                            </div>
                                            <div className="relative inline-flex items-center cursor-pointer">
                                                <input type="checkbox" className="sr-only peer" defaultChecked />
                                                <div className="w-11 h-6 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </section>
                        )}
                    </main>
                </div>
            </div>
        </div>
    );
}
