# Deployment Guide: Engineering Career OS

This guide provides step-by-step instructions to deploy your scalable engineering course platform to production using Vercel, Supabase, and Clerk.

## 1. Supabase Setup (Database)
Supabase handles your course data, user enrollments, and progress tracking.

1.  **Create Project**: Sign in to [Supabase](https://supabase.com/) and create a new project.
2.  **Run Migrations**: Go to the **SQL Editor** and run the contents of these files in order:
    - `supabase_migration_courses.sql` (Creates the base `courses` table)
    - `supabase_migration_v2.sql` (Adds categories, enrollments, progress, and performance indexes)
3.  **Get API Keys**: Go to **Project Settings > API** and copy:
    - `Project URL`
    - `anon (public)` API Key
    - `service_role (secret)` API Key (⚠️ Keep this secure!)

## 2. Clerk Setup (Authentication)
Clerk manages user sign-ups, logins, and identity.

1.  **Create Application**: Sign in to [Clerk](https://clerk.com/) and create a new application.
2.  **Configure URLs**: In the Clerk Dashboard, go to **Paths** and set:
    - Sign-in: `/sign-in`
    - Sign-up: `/sign-up`
    - After sign-in: `/dashboard`
    - After sign-up: `/dashboard`
3.  **Get API Keys**: Copy the `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` and `CLERK_SECRET_KEY`.

## 3. External API Keys
1.  **Groq AI**: Get your API key from the [Groq Console](https://console.groq.com/) for the AI Career Assistant.
2.  **YouTube API**: Create a project in the [Google Cloud Console](https://console.cloud.google.com/), enable the YouTube Data API v3, and generate an API key.

## 4. Vercel Deployment
Vercel is the recommended platform for hosting your Next.js application.

1.  **Connect GitHub**: Push your latest code to your GitHub repository and connect it to a new project on [Vercel](https://vercel.com/).
2.  **Environment Variables**: Add the following keys in the Vercel project settings:

| Variable | Source |
| :--- | :--- |
| `NEXT_PUBLIC_SUPABASE_URL` | Supabase URL |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | Supabase Anon Key |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase Service Role Key |
| `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` | Clerk Publishable Key |
| `CLERK_SECRET_KEY` | Clerk Secret Key |
| `GROQ_API_KEY` | Groq API Key |
| `YOUTUBE_API_KEY` | YouTube API Key |
| `NEXT_PUBLIC_CLERK_SIGN_IN_URL` | `/sign-in` |
| `NEXT_PUBLIC_CLERK_SIGN_UP_URL` | `/sign-up` |
| `NEXT_PUBLIC_CLERK_AFTER_SIGN_IN_URL` | `/dashboard` |
| `NEXT_PUBLIC_CLERK_AFTER_SIGN_UP_URL` | `/dashboard` |

3.  **Deploy**: Click **Deploy**. Once finished, update your Clerk "Allowed Redirect Origins" (in the Clerk production settings) to include your new Vercel production domain.

## 5. Post-Deployment: Seeding Data
After deployment, if your database is empty, you can visit the following route while logged in to populate the initial courses:
`https://your-domain.vercel.app/api/populate-courses`

---
**Note**: Ensure you use the `service_role` key ONLY in server-side code (it is already configured this way in `lib/supabase-server.ts`).
