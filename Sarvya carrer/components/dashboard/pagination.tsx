'use client';

import Link from 'next/link';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
    currentPage: number;
    totalPage: number;
}

export function Pagination({ currentPage, totalPage }: PaginationProps) {
    if (totalPage <= 1) return null;

    return (
        <div className="flex items-center justify-center gap-2 py-8">
            <Link
                href={currentPage > 1 ? `?page=${currentPage - 1}` : '#'}
                className={`p-2 rounded-lg border border-white/10 transition-colors ${currentPage > 1 ? 'hover:bg-white/5 text-white' : 'text-muted-foreground pointer-events-none'
                    }`}
            >
                <ChevronLeft className="w-5 h-5" />
            </Link>

            <div className="flex items-center gap-1">
                {[...Array(totalPage)].map((_, i) => (
                    <Link
                        key={i + 1}
                        href={`?page=${i + 1}`}
                        className={`w-10 h-10 flex items-center justify-center rounded-lg font-bold transition-all ${currentPage === i + 1
                                ? 'bg-primary text-white glow-blue'
                                : 'text-muted-foreground hover:bg-white/5 hover:text-white'
                            }`}
                    >
                        {i + 1}
                    </Link>
                ))}
            </div>

            <Link
                href={currentPage < totalPage ? `?page=${currentPage + 1}` : '#'}
                className={`p-2 rounded-lg border border-white/10 transition-colors ${currentPage < totalPage ? 'hover:bg-white/5 text-white' : 'text-muted-foreground pointer-events-none'
                    }`}
            >
                <ChevronRight className="w-5 h-5" />
            </Link>
        </div>
    );
}
