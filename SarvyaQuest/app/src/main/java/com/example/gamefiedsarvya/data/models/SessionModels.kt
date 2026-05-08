package com.example.gamefiedsarvya.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Learning Session ──────────────────────────────────────────────────────────

@Serializable
data class LearningSession(
    val id: String = generateId(),
    @SerialName("user_name")       val userName: String = "",
    @SerialName("player_level")    val playerLevel: Int = 1,
    val tier: String = "FOUNDATION",
    val topic: String = "",
    val language: String = "en",
    @SerialName("voice_mode")      val voiceMode: Boolean = false,
    @SerialName("total_questions") val totalQuestions: Int = 0,
    @SerialName("correct_answers") val correctAnswers: Int = 0,
    @SerialName("total_time_ms")   val totalTimeMs: Long = 0L,
    @SerialName("xp_earned")       val xpEarned: Int = 0,
    val events: List<SessionEvent> = emptyList(),
    @SerialName("share_code")      val shareCode: String = "",
    @SerialName("created_at")      val createdAt: String = "",
    @SerialName("is_public")       val isPublic: Boolean = false
) {
    val accuracy: Float
        get() = if (totalQuestions == 0) 0f else correctAnswers.toFloat() / totalQuestions
    val accuracyPct: Int get() = (accuracy * 100).toInt()
}

@Serializable
data class SessionEvent(
    val index: Int = 0,
    val question: String = "",
    @SerialName("selected_answer") val selectedAnswer: String = "",
    @SerialName("correct_answer")  val correctAnswer: String = "",
    @SerialName("was_correct")     val wasCorrect: Boolean = false,
    @SerialName("time_ms")         val timeMs: Long = 0L,
    val hint: String = "",
    val explanation: String = "",
    val topic: String = "",
    val language: String = "en",
    @SerialName("voice_used")      val voiceUsed: Boolean = false
)

// ── Voice Settings ────────────────────────────────────────────────────────────

data class VoiceSettings(
    val sttEnabled: Boolean = false,
    val ttsEnabled: Boolean = false,
    val autoNarrate: Boolean = false,
    val language: String = "en"
)

// ── Stream / Share ────────────────────────────────────────────────────────────

@Serializable
data class StreamCard(
    val id: String = "",
    @SerialName("user_name")        val userName: String = "",
    val tier: String = "",
    val topic: String = "",
    @SerialName("accuracy_pct")     val accuracyPct: Int = 0,
    @SerialName("xp_earned")        val xpEarned: Int = 0,
    @SerialName("share_code")       val shareCode: String = "",
    @SerialName("created_at")       val createdAt: String = "",
    @SerialName("total_questions")  val totalQuestions: Int = 0,
    @SerialName("correct_answers")  val correctAnswers: Int = 0
)

// ── Interaction Tag ───────────────────────────────────────────────────────────

@Serializable
data class InteractionTag(
    val language: String = "en",
    @SerialName("voice_mode") val voiceMode: Boolean = false,
    val topic: String = "",
    val tier: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// ── Badge / Achievement ───────────────────────────────────────────────────────

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpBonus: Int = 0
)

val ALL_BADGES = listOf(
    Badge("first_share",   "First Share",    "Shared your first session",        "🔗", 20),
    Badge("streak_3",      "On Fire",        "3-day learning streak",            "🔥", 30),
    Badge("perfect_score", "Perfect",        "100% accuracy in a session",       "⭐", 50),
    Badge("voice_learner", "Voice Learner",  "Completed a session using voice",  "🎙", 25),
    Badge("multilingual",  "Multilingual",   "Studied in 2+ languages",          "🌐", 40),
    Badge("top_performer", "Top Performer",  "Scored 90%+ in Advanced/Pro tier", "🏆", 60)
)

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun generateId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..16).map { chars.random() }.joinToString("")
}
