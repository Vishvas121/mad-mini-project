-- Create courses table
CREATE TABLE IF NOT EXISTS courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  description TEXT,
  thumbnail_url TEXT,
  instructor TEXT DEFAULT 'YouTube Instructor',
  level TEXT DEFAULT 'Beginner',
  price NUMERIC DEFAULT 0,
  duration TEXT DEFAULT 'Self-paced',
  source TEXT DEFAULT 'YouTube',
  youtube_playlist_id TEXT UNIQUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE courses ENABLE ROW LEVEL SECURITY;

-- Allow public read access to courses
CREATE POLICY "Courses are viewable by everyone" ON courses 
FOR SELECT USING (true);

-- Ensure updated_at is handled
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_courses_updated_at
    BEFORE UPDATE ON courses
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();
