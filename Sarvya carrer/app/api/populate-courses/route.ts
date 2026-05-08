import { NextResponse } from 'next/server';
import { createClient } from '@supabase/supabase-js';

const YOUTUBE_API_KEY = process.env.YOUTUBE_API_KEY;
const SUPABASE_URL = process.env.NEXT_PUBLIC_SUPABASE_URL;
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY;

// Initialize Supabase client with Service Role Key for server-side operations
const supabase = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!);

export async function GET() {
    try {
        if (!YOUTUBE_API_KEY) {
            return NextResponse.json({ error: 'YOUTUBE_API_KEY is not configured' }, { status: 500 });
        }

        // 1. Fetch playlists from YouTube
        const params = new URLSearchParams({
            part: 'snippet',
            type: 'playlist',
            maxResults: '20',
            q: 'engineering full stack course',
            key: YOUTUBE_API_KEY,
        });

        const youtubeResponse = await fetch(
            `https://www.googleapis.com/youtube/v3/search?${params.toString()}`
        );

        if (!youtubeResponse.ok) {
            const errorData = await youtubeResponse.json();
            console.error('YouTube API Error:', errorData);

            if (youtubeResponse.status === 403) {
                return NextResponse.json({ error: 'YouTube API Quota exceeded or invalid key' }, { status: 403 });
            }
            return NextResponse.json({ error: 'Failed to fetch from YouTube' }, { status: youtubeResponse.status });
        }

        const data = await youtubeResponse.json();
        const items = data.items || [];

        if (items.length === 0) {
            return NextResponse.json({ message: 'No playlists found', inserted: 0, skipped: 0 });
        }

        // 2. Transform and Normalize Data
        const coursesToUpsert = items.map((item: any) => ({
            title: item.snippet.title,
            description: item.snippet.description,
            thumbnail_url: item.snippet.thumbnails?.high?.url || item.snippet.thumbnails?.default?.url,
            instructor: item.snippet.channelTitle || 'YouTube Instructor',
            level: 'Beginner',
            price: 0,
            duration: 'Self-paced',
            source: 'YouTube',
            youtube_playlist_id: item.id.playlistId,
        }));

        // 3. Upsert into Supabase
        // Using onConflict to handle idempotency based on the unique youtube_playlist_id
        const { data: upsertedData, error: upsertError } = await supabase
            .from('courses')
            .upsert(coursesToUpsert, {
                onConflict: 'youtube_playlist_id',
                ignoreDuplicates: false // We want to update title/desc if they changed
            })
            .select();

        if (upsertError) {
            console.error('Supabase Upsert Error:', upsertError);
            return NextResponse.json({ error: 'Failed to insert courses into database', details: upsertError.message }, { status: 500 });
        }

        const insertedCount = upsertedData?.length || 0;
        const totalProcessed = coursesToUpsert.length;

        return NextResponse.json({
            success: true,
            processed: totalProcessed,
            upserted: insertedCount,
            message: `Successfully synchronized ${insertedCount} courses from YouTube.`
        });

    } catch (error: any) {
        console.error('Unexpected Sync Error:', error);
        return NextResponse.json({ error: 'An unexpected error occurred', details: error.message }, { status: 500 });
    }
}
