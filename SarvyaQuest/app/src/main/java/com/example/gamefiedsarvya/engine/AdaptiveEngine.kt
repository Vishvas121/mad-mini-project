package com.example.gamefiedsarvya.engine

import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.QuestionRepository

/**
 * AI-driven adaptive learning engine.
 *
 * Analyses the player's DigitalTwin (accuracy, response time, topic scores,
 * focus/engagement placeholders) and returns the optimal next question,
 * adjusted difficulty, and tuned combat parameters.
 */
object AdaptiveEngine {

    // ── Difficulty Tuning ─────────────────────────────────────────────────────

    /**
     * Determines the next difficulty level based on recent performance.
     * - accuracy > 0.80 AND fast responses → step up
     * - accuracy < 0.50 OR very slow       → step down
     * - otherwise                          → maintain
     */
    fun computeNextDifficulty(twin: DigitalTwin): Difficulty {
        val acc  = twin.recentAccuracy
        val fast = twin.averageResponseTimeMs < 8_000L   // under 8 s = fast
        val slow = twin.averageResponseTimeMs > 15_000L  // over 15 s = slow

        return when {
            acc > 0.80f && fast -> when (twin.preferredDifficulty) {
                Difficulty.EASY   -> Difficulty.MEDIUM
                Difficulty.MEDIUM -> Difficulty.HARD
                Difficulty.HARD   -> Difficulty.HARD
            }
            acc < 0.50f || slow -> when (twin.preferredDifficulty) {
                Difficulty.HARD   -> Difficulty.MEDIUM
                Difficulty.MEDIUM -> Difficulty.EASY
                Difficulty.EASY   -> Difficulty.EASY
            }
            else -> twin.preferredDifficulty
        }
    }

    // ── Question Selection ────────────────────────────────────────────────────

    /**
     * Picks the next question, prioritising weak topics from the DigitalTwin.
     */
    fun selectNextQuestion(
        twin: DigitalTwin,
        usedIds: Set<String>,
        zoneId: String
    ): Question? {
        val difficulty = computeNextDifficulty(twin)

        // Find the topic the player is weakest in
        val weakTopic = twin.knowledgeScores
            .filter { it.value < 0.6f }
            .minByOrNull { it.value }?.key ?: ""

        val candidates = QuestionRepository.getAdaptiveQuestions(weakTopic, difficulty, 10)
            .filter { it.id !in usedIds }

        return candidates.firstOrNull()
            ?: QuestionRepository.getQuestionsForZone(zoneId, difficulty)
                .filter { it.id !in usedIds }
                .firstOrNull()
    }

    // ── Time Limit Tuning ─────────────────────────────────────────────────────

    /**
     * Adjusts the question time limit based on focus/engagement signals.
     * High focus → tighter timer (more challenge).
     * Low engagement → more generous timer.
     */
    fun adjustedTimeLimit(baseSeconds: Int, twin: DigitalTwin): Int {
        val factor = when {
            twin.focusScore > 0.8f  -> 0.85f   // focused → harder
            twin.focusScore < 0.4f  -> 1.25f   // distracted → easier
            else                    -> 1.0f
        }
        return (baseSeconds * factor).toInt().coerceIn(8, 40)
    }

    // ── Damage Calculation ────────────────────────────────────────────────────

    /**
     * Calculates damage dealt to enemy on a correct answer.
     * Fast answers → critical hit multiplier.
     * Streak bonus applies on 3+ consecutive correct.
     */
    fun calculatePlayerDamage(
        responseTimeMs: Long,
        timeLimitMs: Long,
        streakCount: Int,
        activeAbility: Ability?
    ): Pair<Int, Boolean> {   // damage, isCritical
        val speedRatio = 1f - (responseTimeMs.toFloat() / timeLimitMs).coerceIn(0f, 1f)
        val isCritical = speedRatio > 0.6f   // answered in top 40% of time

        var base = 25
        if (isCritical) base = (base * 1.5f).toInt()
        if (streakCount >= 3) base = (base * 1.3f).toInt()
        if (activeAbility == Ability.DOUBLE_DAMAGE) base *= 2

        return Pair(base, isCritical)
    }

    /**
     * Calculates damage taken by player on a wrong answer.
     * Shield ability blocks it entirely.
     */
    fun calculateEnemyDamage(
        enemyAttack: Int,
        activeAbility: Ability?
    ): Int {
        if (activeAbility == Ability.SHIELD_BLOCK) return 0
        return enemyAttack
    }

    // ── Digital Twin Update ───────────────────────────────────────────────────

    /**
     * Updates the DigitalTwin after each answer.
     */
    fun updateDigitalTwin(
        twin: DigitalTwin,
        question: Question,
        wasCorrect: Boolean,
        responseTimeMs: Long
    ): DigitalTwin {
        // Update topic knowledge score (exponential moving average)
        val alpha = 0.3f
        val currentScore = twin.knowledgeScores[question.topic] ?: 0.5f
        val newScore = if (wasCorrect)
            (currentScore + alpha * (1f - currentScore)).coerceIn(0f, 1f)
        else
            (currentScore - alpha * currentScore).coerceIn(0f, 1f)

        val updatedScores = twin.knowledgeScores.toMutableMap()
        updatedScores[question.topic] = newScore

        // Update accuracy (EMA)
        val newAccuracy = twin.recentAccuracy * 0.7f + (if (wasCorrect) 1f else 0f) * 0.3f

        // Update response time (EMA)
        val newAvgTime = (twin.averageResponseTimeMs * 0.7f + responseTimeMs * 0.3f).toLong()

        val newDifficulty = computeNextDifficulty(
            twin.copy(recentAccuracy = newAccuracy, averageResponseTimeMs = newAvgTime)
        )

        return twin.copy(
            knowledgeScores = updatedScores,
            recentAccuracy = newAccuracy,
            averageResponseTimeMs = newAvgTime,
            preferredDifficulty = newDifficulty
        )
    }

    // ── Hardware Placeholder ──────────────────────────────────────────────────

    /**
     * Placeholder for future hardware integration (EEG / eye-tracking).
     * Currently returns simulated values.
     */
    fun readHardwareSignals(): Pair<Float, Float> {
        // TODO: integrate real sensor SDK here
        val simulatedFocus      = (0.5f + Math.random().toFloat() * 0.4f).coerceIn(0f, 1f)
        val simulatedEngagement = (0.5f + Math.random().toFloat() * 0.4f).coerceIn(0f, 1f)
        return Pair(simulatedFocus, simulatedEngagement)
    }
}
