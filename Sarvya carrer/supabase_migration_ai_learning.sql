-- ============================================================
-- AI Personalized Learning System — Supabase Migration
-- Run this in your Supabase SQL editor
-- ============================================================

-- 1. Learning Profiles — stores each user's learning style & preferences
CREATE TABLE IF NOT EXISTS learning_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL UNIQUE,
  learning_style TEXT DEFAULT 'visual',        -- visual | auditory | reading | kinesthetic
  preferred_difficulty TEXT DEFAULT 'Beginner', -- Beginner | Intermediate | Advanced
  preferred_categories TEXT[] DEFAULT '{}',
  weekly_goal_hours INTEGER DEFAULT 5,
  streak_days INTEGER DEFAULT 0,
  last_active_date DATE DEFAULT CURRENT_DATE,
  total_xp INTEGER DEFAULT 0,
  level INTEGER DEFAULT 1,
  onboarding_completed BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Skill Scores — tracks proficiency per topic
CREATE TABLE IF NOT EXISTS skill_scores (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  skill_name TEXT NOT NULL,
  category TEXT NOT NULL,
  score INTEGER DEFAULT 0,          -- 0–100
  questions_attempted INTEGER DEFAULT 0,
  questions_correct INTEGER DEFAULT 0,
  last_assessed_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, skill_name)
);

-- 3. Quiz Sessions — each adaptive quiz attempt
CREATE TABLE IF NOT EXISTS quiz_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  topic TEXT NOT NULL,
  difficulty TEXT NOT NULL,
  questions_total INTEGER DEFAULT 0,
  questions_correct INTEGER DEFAULT 0,
  score_percentage NUMERIC DEFAULT 0,
  xp_earned INTEGER DEFAULT 0,
  time_taken_seconds INTEGER DEFAULT 0,
  completed BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  completed_at TIMESTAMPTZ
);

-- 4. Quiz Answers — individual question responses
CREATE TABLE IF NOT EXISTS quiz_answers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID REFERENCES quiz_sessions(id) ON DELETE CASCADE,
  user_id TEXT NOT NULL,
  question_text TEXT NOT NULL,
  selected_answer TEXT,
  correct_answer TEXT NOT NULL,
  is_correct BOOLEAN DEFAULT FALSE,
  time_taken_seconds INTEGER DEFAULT 0,
  difficulty TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Learning Paths — AI-generated personalized paths
CREATE TABLE IF NOT EXISTS learning_paths (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  goal TEXT NOT NULL,
  target_role TEXT,
  estimated_weeks INTEGER DEFAULT 12,
  path_data JSONB NOT NULL DEFAULT '[]',   -- array of path steps
  is_active BOOLEAN DEFAULT TRUE,
  progress_percentage NUMERIC DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Learning Path Progress — tracks step completion
CREATE TABLE IF NOT EXISTS learning_path_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  path_id UUID REFERENCES learning_paths(id) ON DELETE CASCADE,
  step_index INTEGER NOT NULL,
  step_title TEXT NOT NULL,
  completed BOOLEAN DEFAULT FALSE,
  completed_at TIMESTAMPTZ,
  UNIQUE(user_id, path_id, step_index)
);

-- 7. Daily Activity — for streak and heatmap tracking
CREATE TABLE IF NOT EXISTS daily_activity (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  activity_date DATE NOT NULL DEFAULT CURRENT_DATE,
  minutes_studied INTEGER DEFAULT 0,
  quizzes_taken INTEGER DEFAULT 0,
  xp_earned INTEGER DEFAULT 0,
  UNIQUE(user_id, activity_date)
);

-- 8. AI Recommendations — stores AI-generated course recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
  reason TEXT,
  score NUMERIC DEFAULT 0,           -- relevance score 0–1
  dismissed BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- Enable RLS on all new tables
-- ============================================================
ALTER TABLE learning_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE skill_scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE quiz_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE quiz_answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_paths ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_path_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE daily_activity ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_recommendations ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- RLS Policies — users can only access their own data
-- ============================================================
CREATE POLICY "Users manage own learning profile" ON learning_profiles FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own skill scores" ON skill_scores FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own quiz sessions" ON quiz_sessions FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own quiz answers" ON quiz_answers FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own learning paths" ON learning_paths FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own path progress" ON learning_path_progress FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own daily activity" ON daily_activity FOR ALL USING (user_id = auth.jwt() ->> 'sub');
CREATE POLICY "Users manage own recommendations" ON ai_recommendations FOR ALL USING (user_id = auth.jwt() ->> 'sub');

-- ============================================================
-- Indexes for performance
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_learning_profiles_user_id ON learning_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_skill_scores_user_id ON skill_scores(user_id);
CREATE INDEX IF NOT EXISTS idx_skill_scores_category ON skill_scores(category);
CREATE INDEX IF NOT EXISTS idx_quiz_sessions_user_id ON quiz_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_sessions_topic ON quiz_sessions(topic);
CREATE INDEX IF NOT EXISTS idx_quiz_answers_session_id ON quiz_answers(session_id);
CREATE INDEX IF NOT EXISTS idx_learning_paths_user_id ON learning_paths(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_activity_user_date ON daily_activity(user_id, activity_date);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_user_id ON ai_recommendations(user_id);

-- ============================================================
-- Function: update streak on daily activity upsert
-- ============================================================
CREATE OR REPLACE FUNCTION update_learning_streak()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE learning_profiles
  SET
    streak_days = CASE
      WHEN last_active_date = CURRENT_DATE - INTERVAL '1 day' THEN streak_days + 1
      WHEN last_active_date = CURRENT_DATE THEN streak_days
      ELSE 1
    END,
    last_active_date = CURRENT_DATE,
    total_xp = total_xp + NEW.xp_earned,
    updated_at = NOW()
  WHERE user_id = NEW.user_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_daily_activity_upsert ON daily_activity;
CREATE TRIGGER on_daily_activity_upsert
  AFTER INSERT OR UPDATE ON daily_activity
  FOR EACH ROW EXECUTE FUNCTION update_learning_streak();

-- ============================================================
-- Function: auto-level up based on XP
-- ============================================================
CREATE OR REPLACE FUNCTION update_user_level()
RETURNS TRIGGER AS $$
BEGIN
  NEW.level = GREATEST(1, FLOOR(SQRT(NEW.total_xp / 100)) + 1);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_xp_update ON learning_profiles;
CREATE TRIGGER on_xp_update
  BEFORE UPDATE OF total_xp ON learning_profiles
  FOR EACH ROW EXECUTE FUNCTION update_user_level();
