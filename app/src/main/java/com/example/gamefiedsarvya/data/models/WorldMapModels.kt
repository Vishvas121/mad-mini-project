package com.example.gamefiedsarvya.data.models

/**
 * World Map system — one map per tier, each with its own visual style and node types.
 * Fully additive — does not touch existing Zone/Enemy models.
 */

enum class MapNodeType {
    LESSON,       // standard learning node
    QUIZ,         // quick knowledge check
    BOSS,         // mastery challenge
    HUB,          // learning hub access
    LOCKED,       // not yet reachable
    COMPLETED     // finished
}

enum class MapStyle {
    COLORFUL,     // Foundation – forest/city/zones
    STRUCTURED,   // Advanced – chapters/modules/exam paths
    NETWORK       // Professional – skills/domains/career paths
}

data class MapNode(
    val id: String,
    val label: String,
    val type: MapNodeType,
    val topic: String,
    val xpReward: Int,
    val posX: Float,          // 0..1 normalised
    val posY: Float,          // 0..1 normalised
    val prerequisites: List<String> = emptyList(),
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val description: String = "",
    val icon: String = "📖"
)

data class TierWorldMap(
    val tier: LearningTier,
    val style: MapStyle,
    val title: String,
    val nodes: List<MapNode>,
    val connections: List<Pair<String, String>>   // nodeId → nodeId
)

// ── Streak / Gamification ─────────────────────────────────────────────────────

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String = "",   // ISO date string
    val totalDaysStudied: Int = 0
)

// ── Adaptive Recommendation ───────────────────────────────────────────────────

data class AdaptiveRecommendation(
    val nodeId: String,
    val reason: String,
    val urgency: Float   // 0..1
)
