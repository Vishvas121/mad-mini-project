'use client';

import { Suspense, useState, useTransition } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Search, Filter, ChevronDown, Loader2 } from 'lucide-react';

interface CourseFiltersProps {
    categories: string[];
    levels: string[];
}

export function CourseFilters({ categories, levels }: CourseFiltersProps) {
    const router = useRouter();
    const searchParams = useSearchParams();
    const [isPending, startTransition] = useTransition();

    const currentSearch = searchParams.get('search') || '';
    const currentCategory = searchParams.get('category') || 'all';
    const currentLevel = searchParams.get('level') || 'all';

    const [search, setSearch] = useState(currentSearch);

    function updateFilters(updates: Record<string, string>) {
        const params = new URLSearchParams(searchParams.toString());
        params.set('page', '1'); // Reset pagination on filter change

        Object.entries(updates).forEach(([key, value]) => {
            if (value === 'all' || !value) {
                params.delete(key);
            } else {
                params.set(key, value);
            }
        });

        startTransition(() => {
            router.push(`/courses?${params.toString()}`);
        });
    }

    return (
        <div className="space-y-6">
            {/* Search bar */}
            <div className="relative group">
                <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none">
                    {isPending ? (
                        <Loader2 className="w-5 h-5 text-primary animate-spin" />
                    ) : (
                        <Search className="w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                    )}
                </div>
                <input
                    type="text"
                    placeholder="Search by title, instructor or content..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && updateFilters({ search })}
                    className="w-full h-14 pl-12 pr-4 bg-white/5 border border-white/10 rounded-xl outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/20 text-foreground transition-all"
                />
                {search && (
                    <button
                        onClick={() => { setSearch(''); updateFilters({ search: '' }); }}
                        className="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-bold text-muted-foreground hover:text-foreground"
                    >
                        Clear
                    </button>
                )}
            </div>

            <div className="flex flex-col gap-6">
                {/* Category Filter */}
                <div className="flex flex-col gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground ml-1">Category</span>
                    <div className="flex overflow-x-auto pb-2 -mx-4 px-4 gap-2 no-scrollbar scroll-smooth">
                        {['all', ...categories].map((cat) => (
                            <button
                                key={cat}
                                onClick={() => updateFilters({ category: cat })}
                                className={`px-4 py-2 rounded-lg text-sm font-bold transition-all whitespace-nowrap flex-shrink-0 ${currentCategory === cat
                                    ? 'bg-primary text-white shadow-lg shadow-primary/20 scale-105'
                                    : 'bg-white/5 text-muted-foreground hover:bg-white/10'
                                    }`}
                            >
                                {cat.charAt(0).toUpperCase() + cat.slice(1)}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Level Filter */}
                <div className="flex flex-col gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground ml-1">Level</span>
                    <div className="flex overflow-x-auto pb-2 -mx-4 px-4 gap-2 no-scrollbar scroll-smooth">
                        {['all', ...levels].map((level) => (
                            <button
                                key={level}
                                onClick={() => updateFilters({ level: level })}
                                className={`px-4 py-2 rounded-lg text-sm font-bold transition-all whitespace-nowrap flex-shrink-0 ${currentLevel === level
                                    ? 'bg-primary text-white shadow-lg shadow-primary/20 scale-105'
                                    : 'bg-white/5 text-muted-foreground hover:bg-white/10'
                                    }`}
                            >
                                {level}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
