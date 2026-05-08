package com.example.gamefiedsarvya.data.remote

import com.example.gamefiedsarvya.data.models.LearningSession
import com.example.gamefiedsarvya.data.models.StreamCard
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SessionRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    // ── Save session (public feed) ────────────────────────────────────────────

    suspend fun saveSession(session: LearningSession): Result<LearningSession> =
        withContext(Dispatchers.IO) {
            runCatching {
                val withMeta = session.copy(
                    shareCode = generateShareCode(),
                    createdAt = dateFormat.format(Date()),
                    isPublic  = true
                )
                SupabaseClient.db.from("learning_sessions").insert(withMeta)
                withMeta
            }
        }

    // ── Public stream feed ────────────────────────────────────────────────────

    suspend fun fetchPublicStream(limit: Int = 30): Result<List<StreamCard>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseClient.db
                    .from("learning_sessions")
                    .select {
                        filter { eq("is_public", true) }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<StreamCard>()
            }
        }

    // ── Top performers (leaderboard) ──────────────────────────────────────────

    suspend fun fetchTopPerformers(limit: Int = 10): Result<List<StreamCard>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseClient.db
                    .from("learning_sessions")
                    .select {
                        filter { eq("is_public", true) }
                        order("accuracy_pct", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<StreamCard>()
            }
        }

    // ── Fetch by share code ───────────────────────────────────────────────────

    suspend fun fetchSessionByCode(code: String): Result<LearningSession?> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseClient.db
                    .from("learning_sessions")
                    .select { filter { eq("share_code", code) } }
                    .decodeSingleOrNull<LearningSession>()
            }
        }

    // ── User sessions ─────────────────────────────────────────────────────────

    suspend fun fetchUserSessions(userName: String, limit: Int = 15): Result<List<LearningSession>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseClient.db
                    .from("learning_sessions")
                    .select {
                        filter { eq("user_name", userName) }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<LearningSession>()
            }
        }

    // ── Sync user profile ─────────────────────────────────────────────────────

    suspend fun syncUserProfile(
        userName: String,
        tier: String,
        level: Int,
        totalXp: Int,
        accuracy: Float,
        streakDays: Int = 0,
        themeName: String = "DARK_FANTASY",
        unlockedAbilities: List<String> = emptyList(),
        badges: List<String> = emptyList()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val data = mapOf(
                "user_name"          to userName,
                "tier"               to tier,
                "level"              to level,
                "total_xp"           to totalXp,
                "accuracy"           to accuracy,
                "streak_days"        to streakDays,
                "theme_name"         to themeName,
                "unlocked_abilities" to unlockedAbilities,
                "badges"             to badges,
                "updated_at"         to dateFormat.format(Date())
            )
            SupabaseClient.db.from("user_profiles").upsert(data)
            Unit
        }
    }

    // ── Sync skill tree progress ──────────────────────────────────────────────

    suspend fun syncSkillProgress(
        userName: String,
        unlockedNodes: List<String>,
        xpTotal: Int,
        level: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val data = mapOf(
                "user_name"      to userName,
                "unlocked_nodes" to unlockedNodes,
                "xp_total"       to xpTotal,
                "level"          to level,
                "updated_at"     to dateFormat.format(Date())
            )
            SupabaseClient.db.from("skill_progress").upsert(data)
            Unit
        }
    }

    // ── Sync Learning Hub progress ────────────────────────────────────────────

    suspend fun syncHubProgress(
        userName: String,
        studiedIds: List<String>,
        totalStudyXp: Int,
        studyMinutes: Int,
        preparedTopics: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val data = mapOf(
                "user_name"       to userName,
                "studied_ids"     to studiedIds,
                "total_study_xp"  to totalStudyXp,
                "study_minutes"   to studyMinutes,
                "prepared_topics" to preparedTopics,
                "updated_at"      to dateFormat.format(Date())
            )
            SupabaseClient.db.from("hub_progress").upsert(data)
            Unit
        }
    }

    // ── Sync concept mastery ──────────────────────────────────────────────────

    suspend fun syncConceptMastery(
        userName: String,
        mastery: Map<String, com.example.gamefiedsarvya.data.models.ConceptMastery>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            mastery.values.forEach { cm ->
                val data = mapOf(
                    "user_name"           to userName,
                    "topic"               to cm.topic,
                    "correct_attempts"    to cm.correctAttempts,
                    "total_attempts"      to cm.totalAttempts,
                    "consecutive_correct" to cm.consecutiveCorrect,
                    "is_mastered"         to cm.isMastered,
                    "last_difficulty"     to cm.lastSeenDifficulty.name,
                    "updated_at"          to dateFormat.format(Date())
                )
                SupabaseClient.db.from("concept_mastery").upsert(data)
            }
            Unit
        }
    }

    private fun generateShareCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
