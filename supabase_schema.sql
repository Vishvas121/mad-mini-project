-- =============================================================================
--  SARVYA QUEST  --  Database Schema v4
--  Project: https://jobmvyrpwuddhybbnsyd.supabase.co
--
--  IMPORTANT: Run this in TWO steps in the Supabase SQL Editor:
--    Step 1: Run everything from line 1 to the "STEP 2" marker
--    Step 2: Run everything from the "STEP 2" marker to end
--
--  Or run the full script — Postgres processes DDL top-to-bottom so
--  tables are created before the view references them.
-- =============================================================================

-- =============================================================================
--  STEP 1: DROP OLD OBJECTS (clean slate)
-- =============================================================================

DROP VIEW  IF EXISTS leaderboard       CASCADE;
DROP TABLE IF EXISTS hub_progress      CASCADE;
DROP TABLE IF EXISTS skill_progress    CASCADE;
DROP TABLE IF EXISTS user_profiles     CASCADE;
DROP TABLE IF EXISTS learning_sessions CASCADE;

-- =============================================================================
--  STEP 1 CONTINUED: CREATE TABLES
-- =============================================================================

-- ---------------------------------------------------------------------------
-- learning_sessions
-- ---------------------------------------------------------------------------
CREATE TABLE learning_sessions (
    id               TEXT        PRIMARY KEY,
    user_name        TEXT        NOT NULL,
    player_level     INT         NOT NULL DEFAULT 1,
    tier             TEXT        NOT NULL DEFAULT 'FOUNDATION',
    topic            TEXT        NOT NULL DEFAULT '',
    language         TEXT        NOT NULL DEFAULT 'en',
    voice_mode       BOOLEAN     NOT NULL DEFAULT FALSE,
    difficulty       TEXT        NOT NULL DEFAULT 'EASY',
    total_questions  INT         NOT NULL DEFAULT 0,
    correct_answers  INT         NOT NULL DEFAULT 0,
    total_time_ms    BIGINT      NOT NULL DEFAULT 0,
    xp_earned        INT         NOT NULL DEFAULT 0,
    events           JSONB       NOT NULL DEFAULT '[]',
    share_code       TEXT        UNIQUE,
    theme_name       TEXT        NOT NULL DEFAULT 'DARK_FANTASY',
    badges_earned    JSONB       NOT NULL DEFAULT '[]',
    is_public        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    accuracy_pct     INT         GENERATED ALWAYS AS (
        CASE WHEN total_questions = 0 THEN 0
             ELSE (correct_answers * 100 / total_questions)
        END
    ) STORED
);

CREATE INDEX idx_ls_share_code ON learning_sessions (share_code);
CREATE INDEX idx_ls_user_name  ON learning_sessions (user_name);
CREATE INDEX idx_ls_public     ON learning_sessions (is_public, created_at DESC);
CREATE INDEX idx_ls_accuracy   ON learning_sessions (accuracy_pct DESC);

-- ---------------------------------------------------------------------------
-- user_profiles
-- ---------------------------------------------------------------------------
CREATE TABLE user_profiles (
    user_name          TEXT        PRIMARY KEY,
    tier               TEXT        NOT NULL DEFAULT 'FOUNDATION',
    level              INT         NOT NULL DEFAULT 1,
    total_xp           INT         NOT NULL DEFAULT 0,
    accuracy           FLOAT       NOT NULL DEFAULT 0.0,
    streak_days        INT         NOT NULL DEFAULT 0,
    last_active        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    theme_name         TEXT        NOT NULL DEFAULT 'DARK_FANTASY',
    unlocked_abilities JSONB       NOT NULL DEFAULT '[]',
    completed_topics   JSONB       NOT NULL DEFAULT '[]',
    badges             JSONB       NOT NULL DEFAULT '[]',
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- skill_progress
-- ---------------------------------------------------------------------------
CREATE TABLE skill_progress (
    user_name      TEXT        PRIMARY KEY,
    unlocked_nodes JSONB       NOT NULL DEFAULT '[]',
    xp_total       INT         NOT NULL DEFAULT 0,
    level          INT         NOT NULL DEFAULT 1,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- hub_progress
-- ---------------------------------------------------------------------------
CREATE TABLE hub_progress (
    user_name       TEXT        PRIMARY KEY,
    studied_ids     JSONB       NOT NULL DEFAULT '[]',
    total_study_xp  INT         NOT NULL DEFAULT 0,
    study_minutes   INT         NOT NULL DEFAULT 0,
    prepared_topics JSONB       NOT NULL DEFAULT '[]',
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
--  STEP 2: CREATE VIEW (after all tables exist)
-- =============================================================================

CREATE VIEW leaderboard AS
SELECT
    user_name,
    tier,
    level,
    total_xp,
    ROUND((accuracy * 100)::numeric, 1) AS accuracy_pct,
    streak_days,
    updated_at
FROM user_profiles
ORDER BY total_xp DESC
LIMIT 100;

-- =============================================================================
--  STEP 3: ROW LEVEL SECURITY
-- =============================================================================

ALTER TABLE learning_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_profiles     ENABLE ROW LEVEL SECURITY;
ALTER TABLE skill_progress    ENABLE ROW LEVEL SECURITY;
ALTER TABLE hub_progress      ENABLE ROW LEVEL SECURITY;

-- learning_sessions policies
CREATE POLICY "ls_select_public" ON learning_sessions
    FOR SELECT USING (is_public = TRUE);

CREATE POLICY "ls_insert_any" ON learning_sessions
    FOR INSERT WITH CHECK (TRUE);

-- user_profiles policies
CREATE POLICY "up_select_any" ON user_profiles
    FOR SELECT USING (TRUE);

CREATE POLICY "up_insert_any" ON user_profiles
    FOR INSERT WITH CHECK (TRUE);

CREATE POLICY "up_update_any" ON user_profiles
    FOR UPDATE USING (TRUE);

-- skill_progress policies
CREATE POLICY "sp_select_any" ON skill_progress
    FOR SELECT USING (TRUE);

CREATE POLICY "sp_insert_any" ON skill_progress
    FOR INSERT WITH CHECK (TRUE);

CREATE POLICY "sp_update_any" ON skill_progress
    FOR UPDATE USING (TRUE);

-- hub_progress policies
CREATE POLICY "hp_select_any" ON hub_progress
    FOR SELECT USING (TRUE);

CREATE POLICY "hp_insert_any" ON hub_progress
    FOR INSERT WITH CHECK (TRUE);

CREATE POLICY "hp_update_any" ON hub_progress
    FOR UPDATE USING (TRUE);

-- =============================================================================
--  CONCEPT MASTERY TABLE  (Learning Loop v2)
-- =============================================================================

CREATE TABLE IF NOT EXISTS concept_mastery (
    user_name           TEXT        NOT NULL,
    topic               TEXT        NOT NULL,
    correct_attempts    INT         NOT NULL DEFAULT 0,
    total_attempts      INT         NOT NULL DEFAULT 0,
    consecutive_correct INT         NOT NULL DEFAULT 0,
    is_mastered         BOOLEAN     NOT NULL DEFAULT FALSE,
    last_difficulty     TEXT        NOT NULL DEFAULT 'EASY',
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_name, topic)
);

ALTER TABLE concept_mastery ENABLE ROW LEVEL SECURITY;
CREATE POLICY "cm_select" ON concept_mastery FOR SELECT USING (TRUE);
CREATE POLICY "cm_insert" ON concept_mastery FOR INSERT WITH CHECK (TRUE);
CREATE POLICY "cm_update" ON concept_mastery FOR UPDATE USING (TRUE);

CREATE INDEX IF NOT EXISTS idx_cm_user     ON concept_mastery (user_name);
CREATE INDEX IF NOT EXISTS idx_cm_mastered ON concept_mastery (user_name, is_mastered);
