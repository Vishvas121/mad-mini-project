'use client';

import { Search, Bell, Settings, Menu, LogOut, X } from 'lucide-react';
import { useState, useEffect } from 'react';
import { ThemeToggle } from '@/components/theme-toggle';
import { useUser, SignOutButton, UserButton } from "@clerk/nextjs";
import Link from 'next/link';
import { supabase } from '@/lib/supabase';

interface NavbarProps {
  onMenuClick: () => void;
}

export function DashboardNavbar({ onMenuClick }: NavbarProps) {
  const { user } = useUser();
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (user) {
      fetchNotifications();

      // Subscribe to new notifications
      const channel = supabase
        .channel('notifications')
        .on('postgres_changes', {
          event: 'INSERT',
          schema: 'public',
          table: 'notifications',
          filter: `user_id=eq.${user.id}`
        }, (payload) => {
          setNotifications(prev => [payload.new, ...prev]);
          setUnreadCount(prev => prev + 1);
        })
        .subscribe();

      return () => {
        supabase.removeChannel(channel);
      };
    }
  }, [user]);

  const fetchNotifications = async () => {
    const { data, error } = await supabase
      .from('notifications')
      .select('*')
      .eq('user_id', user?.id)
      .order('created_at', { ascending: false })
      .limit(10);

    if (data) {
      setNotifications(data);
      setUnreadCount(data.filter(n => !n.read).length);
    }
  };

  const markAsRead = async (id: string) => {
    const { error } = await supabase
      .from('notifications')
      .update({ read: true })
      .eq('id', id);

    if (!error) {
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    }
  };

  return (
    <header className="border-b border-border bg-card shadow-sm transition-colors duration-300">
      <div className="px-6 py-4 flex items-center justify-between">
        {/* Left side - Menu and search */}
        <div className="flex items-center gap-4 flex-1">
          <button
            onClick={onMenuClick}
            className="lg:hidden p-2 hover:bg-muted rounded-lg transition-colors"
          >
            <Menu className="w-5 h-5 text-muted-foreground" />
          </button>

          {/* Search */}
          <div className="hidden md:flex items-center gap-2 flex-1 max-w-xs bg-muted border border-border rounded-lg px-4 py-2 hover:bg-input transition-colors">
            <Search className="w-4 h-4 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search courses..."
              className="flex-1 bg-transparent outline-none text-sm text-foreground placeholder:text-muted-foreground"
            />
          </div>
        </div>

        {/* Right side - Notifications and profile */}
        <div className="flex items-center gap-3">
          <ThemeToggle />

          {/* Notifications */}
          <div className="relative">
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative p-2 hover:bg-muted rounded-lg transition-colors group"
            >
              <Bell className="w-5 h-5 text-muted-foreground group-hover:text-foreground" />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full animate-pulse" />
              )}
            </button>

            {showNotifications && (
              <div className="absolute right-0 top-full mt-2 w-80 glass-lg rounded-xl overflow-hidden z-50 animate-in fade-in slide-in-from-top-2 border border-border shadow-2xl">
                <div className="px-4 py-3 border-b border-border flex items-center justify-between bg-muted/30">
                  <h3 className="text-sm font-bold text-foreground">Notifications</h3>
                  <button onClick={() => setShowNotifications(false)}><X className="w-4 h-4 text-muted-foreground" /></button>
                </div>
                <div className="max-h-96 overflow-y-auto">
                  {notifications.length > 0 ? (
                    notifications.map((n) => (
                      <div
                        key={n.id}
                        className={`p-4 border-b border-border/50 hover:bg-muted/50 cursor-pointer transition-colors ${!n.read ? 'bg-primary/5' : ''}`}
                        onClick={() => markAsRead(n.id)}
                      >
                        <div className="text-xs font-bold text-primary mb-1 uppercase tracking-tighter">{n.type}</div>
                        <div className="text-sm font-medium text-foreground mb-1">{n.title}</div>
                        <div className="text-xs text-muted-foreground line-clamp-2">{n.message}</div>
                        <div className="text-[10px] text-muted-foreground/50 mt-2">{new Date(n.created_at).toLocaleString()}</div>
                      </div>
                    ))
                  ) : (
                    <div className="p-8 text-center">
                      <Bell className="w-8 h-8 text-muted-foreground/30 mx-auto mb-2" />
                      <p className="text-sm text-muted-foreground">No notifications yet</p>
                    </div>
                  )}
                </div>
                <div className="px-4 py-2 border-t border-border bg-muted/10">
                  <button className="w-full text-center text-xs text-primary font-bold hover:underline py-1">View All Activity</button>
                </div>
              </div>
            )}
          </div>

          <Link href="/settings" className="p-2 hover:bg-muted rounded-lg transition-colors group">
            <Settings className="w-5 h-5 text-muted-foreground group-hover:text-foreground" />
          </Link>

          <div className="h-6 w-px bg-border mx-2" />

          <UserButton afterSignOutUrl="/" />
        </div>
      </div>
    </header>
  );
}
