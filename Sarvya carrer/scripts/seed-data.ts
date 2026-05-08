import { createClient } from '@supabase/supabase-js';
import * as dotenv from 'dotenv';
import path from 'path';

// Load env vars
dotenv.config({ path: path.resolve(process.cwd(), '.env.local') });

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL!;
const supabaseServiceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY!;

const supabase = createClient(supabaseUrl, supabaseServiceRoleKey);

const MOCK_CLERK_ID = 'user_2b759f01_mock';
const MOCK_EMAIL = 'mock.user@engineeringcareeros.com';

async function seed() {
    console.log('🌱 Seeding mock data...');

    // 1. Create User in auth.users using Admin API
    const { data: authUser, error: authError } = await supabase.auth.admin.createUser({
        email: MOCK_EMAIL,
        password: 'password123',
        email_confirm: true
    });

    let userId: string;

    if (authError) {
        if (authError.message.includes('already exists')) {
            console.log('ℹ️ Auth user already exists, fetching...');
            const { data: users } = await supabase.auth.admin.listUsers();
            const existingUser = users.users.find(u => u.email === MOCK_EMAIL);
            userId = existingUser!.id;
        } else {
            console.error('❌ Error creating auth user:', authError);
            return;
        }
    } else {
        userId = authUser.user.id;
        console.log('✅ Auth user created:', userId);
    }

    // 2. Insert into public.users
    const { data: userData, error: userError } = await supabase
        .from('users')
        .upsert({
            id: userId,
            clerk_id: MOCK_CLERK_ID,
            email: MOCK_EMAIL,
            full_name: 'Mock Engineering Lead',
            avatar_url: `https://api.dicebear.com/7.x/avataaars/svg?seed=${MOCK_CLERK_ID}`
        }, { onConflict: 'clerk_id' })
        .select();

    if (userError) {
        console.error('❌ Error seeding public user:', userError);
        return;
    }
    console.log('✅ Public user seeded');

    // 3. Insert Mock Resumes
    const { error: resumeError } = await supabase
        .from('resumes')
        .insert([
            {
                user_id: MOCK_CLERK_ID,
                title: 'Full Stack Developer Resume',
                content: { summary: 'Experienced dev...', skills: ['React', 'Node'] }
            },
            {
                user_id: MOCK_CLERK_ID,
                title: 'Cloud Architect Resume',
                content: { summary: 'AWS/Azure expert...', skills: ['Terraform', 'Kubernetes'] }
            }
        ]);

    if (resumeError) console.error('❌ Error seeding resumes:', resumeError);
    else console.log('✅ Mock resumes seeded');

    // 4. Insert Mock Notifications
    const { error: notifError } = await supabase
        .from('notifications')
        .insert([
            {
                user_id: MOCK_CLERK_ID,
                title: 'Welcome to CareerOS!',
                message: 'Your integration is now active. Start exploring your roadmaps.',
                type: 'success'
            },
            {
                user_id: MOCK_CLERK_ID,
                title: 'New Course Available',
                message: 'Advance System Design course is now live. Enroll today!',
                type: 'info'
            }
        ]);

    if (notifError) console.error('❌ Error seeding notifications:', notifError);
    else console.log('✅ Mock notifications seeded');

    console.log('✨ Seeding complete!');
}

seed().catch(console.error);
