'use client';

import { useState } from 'react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { DashboardNavbar } from '@/components/dashboard/navbar';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <div className="flex h-screen bg-background transition-colors duration-300 overflow-hidden">
      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} setIsOpen={setSidebarOpen} />

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Navbar */}
        <DashboardNavbar onMenuClick={() => setSidebarOpen(!sidebarOpen)} />

        {/* Content */}
        <main className="flex-1 overflow-auto bg-background transition-colors duration-300">
          {children}
        </main>
      </div>
    </div>
  );
}
