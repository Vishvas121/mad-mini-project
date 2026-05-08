'use client';

import { createContext, useContext, useEffect, useState } from 'react';

type Theme = 'light' | 'dark' | 'midnight' | 'obsidian' | 'sunset';
type AccentColor = 'blue' | 'indigo' | 'violet' | 'rose' | 'emerald';
type SidebarStyle = 'expanded' | 'collapsed';
type GlassIntensity = 'low' | 'medium' | 'high';
type Animations = 'full' | 'subtle' | 'none';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  accentColor: AccentColor;
  setAccentColor: (color: AccentColor) => void;
  sidebarStyle: SidebarStyle;
  setSidebarStyle: (style: SidebarStyle) => void;
  glassIntensity: GlassIntensity;
  setGlassIntensity: (intensity: GlassIntensity) => void;
  animations: Animations;
  setAnimations: (mode: Animations) => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setThemeState] = useState<Theme>('light');
  const [accentColor, setAccentColorState] = useState<AccentColor>('blue');
  const [sidebarStyle, setSidebarStyleState] = useState<SidebarStyle>('expanded');
  const [glassIntensity, setGlassIntensityState] = useState<GlassIntensity>('medium');
  const [animations, setAnimationsState] = useState<Animations>('full');
  const [mounted, setMounted] = useState(false);

  const applySettings = (settings: {
    theme?: Theme;
    accent?: AccentColor;
    sidebar?: SidebarStyle;
    glass?: GlassIntensity;
    anim?: Animations;
  }) => {
    const root = document.documentElement;

    if (settings.theme) {
      root.classList.remove('dark', 'theme-midnight', 'theme-obsidian', 'theme-sunset');
      if (settings.theme === 'dark') root.classList.add('dark');
      else if (settings.theme !== 'light') {
        root.classList.add(`theme-${settings.theme}`);
        root.classList.add('dark');
      }
      localStorage.setItem('theme', settings.theme);
    }

    if (settings.accent) {
      root.setAttribute('data-accent', settings.accent);
      localStorage.setItem('accentColor', settings.accent);
    }

    if (settings.sidebar) {
      root.setAttribute('data-sidebar', settings.sidebar);
      localStorage.setItem('sidebarStyle', settings.sidebar);
    }

    if (settings.glass) {
      root.setAttribute('data-glass', settings.glass);
      localStorage.setItem('glassIntensity', settings.glass);
    }

    if (settings.anim) {
      root.setAttribute('data-animations', settings.anim);
      localStorage.setItem('animations', settings.anim);
    }
  };

  useEffect(() => {
    const t = (localStorage.getItem('theme') as Theme) || 'midnight';
    const a = (localStorage.getItem('accentColor') as AccentColor) || 'blue';
    const s = (localStorage.getItem('sidebarStyle') as SidebarStyle) || 'expanded';
    const g = (localStorage.getItem('glassIntensity') as GlassIntensity) || 'medium';
    const an = (localStorage.getItem('animations') as Animations) || 'full';

    setThemeState(t);
    setAccentColorState(a);
    setSidebarStyleState(s);
    setGlassIntensityState(g);
    setAnimationsState(an);

    applySettings({ theme: t, accent: a, sidebar: s, glass: g, anim: an });
    setMounted(true);
  }, []);

  const setTheme = (val: Theme) => { setThemeState(val); applySettings({ theme: val }); };
  const setAccentColor = (val: AccentColor) => { setAccentColorState(val); applySettings({ accent: val }); };
  const setSidebarStyle = (val: SidebarStyle) => { setSidebarStyleState(val); applySettings({ sidebar: val }); };
  const setGlassIntensity = (val: GlassIntensity) => { setGlassIntensityState(val); applySettings({ glass: val }); };
  const setAnimations = (val: Animations) => { setAnimationsState(val); applySettings({ anim: val }); };

  return (
    <ThemeContext.Provider value={{
      theme, setTheme,
      accentColor, setAccentColor,
      sidebarStyle, setSidebarStyle,
      glassIntensity, setGlassIntensity,
      animations, setAnimations
    }}>
      <div className={mounted ? '' : 'invisible'}>
        {children}
      </div>
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  // Return safe defaults if used outside provider (e.g. global-error boundary)
  if (context === undefined) {
    return {
      theme: 'dark' as Theme,
      setTheme: () => {},
      accentColor: 'blue' as AccentColor,
      setAccentColor: () => {},
      sidebarStyle: 'expanded' as SidebarStyle,
      setSidebarStyle: () => {},
      glassIntensity: 'medium' as GlassIntensity,
      setGlassIntensity: () => {},
      animations: 'full' as Animations,
      setAnimations: () => {},
    };
  }
  return context;
}
