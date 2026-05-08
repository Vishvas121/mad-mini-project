package com.example.gamefiedsarvya.data.models

/**
 * NEW FEATURE: Multi-tier learning system
 * Extends existing game without modifying core models
 */

// ═══════════════════════════════════════════════════════════════════════════════
//  LEARNING TIERS
// ═══════════════════════════════════════════════════════════════════════════════

enum class LearningTier(
    val displayName: String,
    val description: String,
    val ageRange: String,
    val icon: String
) {
    FOUNDATION(
        "Foundation",
        "Simple UI, colorful visuals, easy questions with voice guidance",
        "Class 1-10",
        "🎓"
    ),
    ADVANCED(
        "Advanced",
        "Concept-based questions, timed challenges, boss fights after topics",
        "Class 11-12 + Competitive",
        "🎯"
    ),
    PROFESSIONAL(
        "Professional",
        "Complex problem-solving, strategy-based gameplay, skill tree progression",
        "College / STEM / Courses",
        "🚀"
    );

    fun getDifficultyRange(): List<Difficulty> = when (this) {
        FOUNDATION    -> listOf(Difficulty.EASY)
        ADVANCED      -> listOf(Difficulty.EASY, Difficulty.MEDIUM)
        PROFESSIONAL  -> listOf(Difficulty.MEDIUM, Difficulty.HARD)
    }

    fun getTimeLimitMultiplier(): Float = when (this) {
        FOUNDATION    -> 1.5f   // 50% more time
        ADVANCED      -> 1.0f   // standard
        PROFESSIONAL  -> 0.85f  // 15% less time
    }

    fun getXpMultiplier(): Float = when (this) {
        FOUNDATION    -> 1.0f
        ADVANCED      -> 1.3f
        PROFESSIONAL  -> 1.6f
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  LEARNING HUB CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

data class StudyMaterial(
    val id: String,
    val topic: String,
    val tier: LearningTier,
    val title: String,
    val content: String,              // Markdown-formatted text
    val keyPoints: List<String>,
    val examples: List<String> = emptyList(),
    val relatedTopics: List<String> = emptyList(),
    val estimatedMinutes: Int = 5,
    val xpReward: Int = 10
)

data class LearningProgress(
    val studiedMaterialIds: Set<String> = emptySet(),
    val topicMastery: Map<String, Float> = emptyMap(),  // topic → 0..1
    val studyTimeMinutes: Int = 0,
    val totalStudyXp: Int = 0,
    val preparedTopics: Set<String> = emptySet()        // topics studied before battle
)

// ═══════════════════════════════════════════════════════════════════════════════
//  TIER-SPECIFIC SETTINGS
// ═══════════════════════════════════════════════════════════════════════════════

data class TierSettings(
    val enableVoiceGuidance: Boolean = false,
    val showColorfulVisuals: Boolean = false,
    val enableHints: Boolean = true,
    val showDetailedExplanations: Boolean = true,
    val enableTimedChallenges: Boolean = false,
    val requireTopicMastery: Boolean = false
) {
    companion object {
        fun forTier(tier: LearningTier) = when (tier) {
            LearningTier.FOUNDATION -> TierSettings(
                enableVoiceGuidance = true,
                showColorfulVisuals = true,
                enableHints = true,
                showDetailedExplanations = true,
                enableTimedChallenges = false,
                requireTopicMastery = false
            )
            LearningTier.ADVANCED -> TierSettings(
                enableVoiceGuidance = false,
                showColorfulVisuals = false,
                enableHints = true,
                showDetailedExplanations = true,
                enableTimedChallenges = true,
                requireTopicMastery = true
            )
            LearningTier.PROFESSIONAL -> TierSettings(
                enableVoiceGuidance = false,
                showColorfulVisuals = false,
                enableHints = false,
                showDetailedExplanations = true,
                enableTimedChallenges = true,
                requireTopicMastery = true
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  EXTENDED GAME PROGRESS (NON-BREAKING)
// ═══════════════════════════════════════════════════════════════════════════════

data class ExtendedGameProgress(
    // Original progress (preserved)
    val coreProgress: GameProgress = GameProgress(),
    
    // New tier system
    val selectedTier: LearningTier = LearningTier.FOUNDATION,
    val tierSettings: TierSettings = TierSettings.forTier(LearningTier.FOUNDATION),
    
    // Learning Hub
    val learningProgress: LearningProgress = LearningProgress(),
    
    // Audio preferences
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.8f
)
