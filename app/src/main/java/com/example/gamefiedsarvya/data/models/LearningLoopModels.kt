package com.example.gamefiedsarvya.data.models

/**
 * Models for the Intelligent Learning Loop.
 * Additive — does not modify existing GameModels.
 */

// ── Concept mastery tracking ──────────────────────────────────────────────────

data class ConceptMastery(
    val topic: String,
    val correctAttempts: Int = 0,
    val totalAttempts: Int = 0,
    val lastSeenDifficulty: Difficulty = Difficulty.EASY,
    val consecutiveCorrect: Int = 0,
    val isMastered: Boolean = false,
    val needsReinforcement: Boolean = false
) {
    val masteryScore: Float
        get() = if (totalAttempts == 0) 0f
                else (correctAttempts.toFloat() / totalAttempts).coerceIn(0f, 1f)

    val masteryLevel: MasteryLevel
        get() = when {
            isMastered                -> MasteryLevel.MASTERED
            masteryScore >= 0.8f      -> MasteryLevel.PROFICIENT
            masteryScore >= 0.5f      -> MasteryLevel.DEVELOPING
            else                      -> MasteryLevel.STRUGGLING
        }
}

enum class MasteryLevel(val label: String, val xpMultiplier: Float) {
    STRUGGLING("Struggling", 0.5f),
    DEVELOPING("Developing", 1.0f),
    PROFICIENT("Proficient", 1.5f),
    MASTERED("Mastered",    2.0f)
}

// ── Learning mode state ───────────────────────────────────────────────────────

enum class LearningModePhase {
    NONE,               // normal combat
    TRIGGERED,          // wrong answer — entering learning mode
    EXPLANATION,        // showing micro-explanation
    SIMPLIFIED,         // repeated failures — simplified mode
    RETRY_QUESTION,     // asking similar question after learning
    MASTERY_CELEBRATION // concept just mastered
}

data class LearningModeState(
    val phase: LearningModePhase = LearningModePhase.NONE,
    val triggerQuestion: Question? = null,
    val retryQuestion: Question? = null,
    val wrongAttempts: Int = 0,          // consecutive wrong on same concept
    val totalRetries: Int = 0,
    val aiExplanation: String = "",
    val isLoadingExplanation: Boolean = false,
    val simplifiedExplanation: String = "",
    val conceptExample: String = "",
    val mindMapPoints: List<String> = emptyList(),
    val xpEarnedFromLearning: Int = 0,
    val showMasteryAnimation: Boolean = false
) {
    val isActive: Boolean get() = phase != LearningModePhase.NONE
    val needsSimplified: Boolean get() = wrongAttempts >= 2
}

// ── Learning event (for analytics + Supabase) ─────────────────────────────────

data class LearningEvent(
    val topic: String,
    val wasCorrect: Boolean,
    val enteredLearningMode: Boolean,
    val retriedSuccessfully: Boolean,
    val timeSpentLearningMs: Long,
    val difficulty: Difficulty,
    val masteryAfter: Float
)

// ── Reinforcement schedule ────────────────────────────────────────────────────

data class ReinforcementSchedule(
    val topicQueue: List<String> = emptyList(),   // topics due for re-test
    val questionsSinceLastReinforcement: Int = 0
) {
    val shouldReinforce: Boolean
        get() = topicQueue.isNotEmpty() && questionsSinceLastReinforcement >= 2
}
