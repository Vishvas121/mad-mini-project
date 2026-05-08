package com.example.gamefiedsarvya.engine

import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.QuestionRepository

/**
 * Intelligent Learning Loop Engine.
 *
 * Handles:
 * - Wrong answer → Learning Mode trigger
 * - Concept mastery tracking
 * - Reinforcement scheduling (re-test after 2-3 questions)
 * - Simplified mode after repeated failures
 * - Mastery celebration
 *
 * Pure functions — no side effects, no UI dependencies.
 */
object LearningEngine {

    // ── Mastery thresholds ────────────────────────────────────────────────────

    private const val MASTERY_CONSECUTIVE_REQUIRED = 3
    private const val MASTERY_MIN_ATTEMPTS         = 3
    private const val MASTERY_MIN_SCORE            = 0.8f
    private const val SIMPLIFIED_TRIGGER_THRESHOLD = 2   // wrong answers before simplified
    private const val REINFORCE_AFTER_N_QUESTIONS  = 2

    // ── Process answer → update mastery ──────────────────────────────────────

    fun processAnswer(
        topic: String,
        wasCorrect: Boolean,
        difficulty: Difficulty,
        currentMastery: Map<String, ConceptMastery>
    ): Map<String, ConceptMastery> {
        val existing = currentMastery[topic] ?: ConceptMastery(topic = topic)
        val updated  = existing.copy(
            totalAttempts      = existing.totalAttempts + 1,
            correctAttempts    = existing.correctAttempts + (if (wasCorrect) 1 else 0),
            consecutiveCorrect = if (wasCorrect) existing.consecutiveCorrect + 1 else 0,
            lastSeenDifficulty = difficulty,
            needsReinforcement = !wasCorrect,
            isMastered         = checkMastered(existing, wasCorrect)
        )
        return currentMastery + (topic to updated)
    }

    private fun checkMastered(mastery: ConceptMastery, wasCorrect: Boolean): Boolean {
        if (!wasCorrect) return false
        val newConsecutive = mastery.consecutiveCorrect + 1
        val newTotal       = mastery.totalAttempts + 1
        val newCorrect     = mastery.correctAttempts + 1
        val newScore       = newCorrect.toFloat() / newTotal
        return newConsecutive >= MASTERY_CONSECUTIVE_REQUIRED &&
               newTotal >= MASTERY_MIN_ATTEMPTS &&
               newScore >= MASTERY_MIN_SCORE
    }

    // ── Trigger learning mode ─────────────────────────────────────────────────

    fun shouldTriggerLearningMode(
        wasCorrect: Boolean,
        currentLearningState: LearningModeState
    ): Boolean = !wasCorrect && currentLearningState.phase == LearningModePhase.NONE

    fun enterLearningMode(
        question: Question,
        currentState: LearningModeState
    ): LearningModeState {
        val newWrongAttempts = currentState.wrongAttempts + 1
        return currentState.copy(
            phase           = LearningModePhase.TRIGGERED,
            triggerQuestion = question,
            wrongAttempts   = newWrongAttempts,
            isLoadingExplanation = true
        )
    }

    fun setExplanationLoaded(
        state: LearningModeState,
        explanation: String,
        simplified: String,
        example: String,
        mindMapPoints: List<String>
    ): LearningModeState = state.copy(
        phase                  = if (state.needsSimplified) LearningModePhase.SIMPLIFIED
                                 else LearningModePhase.EXPLANATION,
        aiExplanation          = explanation,
        simplifiedExplanation  = simplified,
        conceptExample         = example,
        mindMapPoints          = mindMapPoints,
        isLoadingExplanation   = false
    )

    fun proceedToRetry(
        state: LearningModeState,
        retryQuestion: Question
    ): LearningModeState = state.copy(
        phase         = LearningModePhase.RETRY_QUESTION,
        retryQuestion = retryQuestion,
        totalRetries  = state.totalRetries + 1
    )

    fun handleRetryAnswer(
        state: LearningModeState,
        wasCorrect: Boolean,
        xpForLearning: Int = 15
    ): LearningModeState = if (wasCorrect) {
        state.copy(
            phase                = LearningModePhase.MASTERY_CELEBRATION,
            xpEarnedFromLearning = state.xpEarnedFromLearning + xpForLearning,
            showMasteryAnimation = true
        )
    } else {
        // Still wrong — go back to explanation (simplified this time)
        state.copy(
            phase         = LearningModePhase.SIMPLIFIED,
            wrongAttempts = state.wrongAttempts + 1
        )
    }

    fun exitLearningMode(): LearningModeState = LearningModeState()

    // ── Reinforcement scheduling ──────────────────────────────────────────────

    fun updateReinforcementSchedule(
        schedule: ReinforcementSchedule,
        topic: String,
        wasCorrect: Boolean,
        mastery: Map<String, ConceptMastery>
    ): ReinforcementSchedule {
        val newQueue = schedule.topicQueue.toMutableList()

        // Add to reinforce queue if wrong and not already queued
        if (!wasCorrect && topic !in newQueue) {
            newQueue.add(topic)
        }

        // Remove from queue if mastered
        val topicMastery = mastery[topic]
        if (topicMastery?.isMastered == true) {
            newQueue.remove(topic)
        }

        val newCount = schedule.questionsSinceLastReinforcement + 1

        return schedule.copy(
            topicQueue = newQueue,
            questionsSinceLastReinforcement = newCount
        )
    }

    fun getReinforcementQuestion(
        schedule: ReinforcementSchedule,
        mastery: Map<String, ConceptMastery>,
        usedIds: Set<String>,
        zoneId: String
    ): Question? {
        if (!schedule.shouldReinforce) return null
        val topic = schedule.topicQueue.firstOrNull() ?: return null
        val topicMastery = mastery[topic]
        val difficulty = when (topicMastery?.masteryLevel) {
            MasteryLevel.STRUGGLING -> Difficulty.EASY
            MasteryLevel.DEVELOPING -> Difficulty.EASY
            else                    -> Difficulty.MEDIUM
        }
        return QuestionRepository.getAdaptiveQuestions(topic, difficulty, 10)
            .filter { it.id !in usedIds }
            .shuffled()
            .firstOrNull()
    }

    fun resetReinforcementCounter(schedule: ReinforcementSchedule): ReinforcementSchedule =
        schedule.copy(questionsSinceLastReinforcement = 0)

    // ── Difficulty adaptation after learning ──────────────────────────────────

    fun adaptDifficultyAfterLearning(
        currentDifficulty: Difficulty,
        wasRetrySuccessful: Boolean
    ): Difficulty = when {
        wasRetrySuccessful && currentDifficulty == Difficulty.HARD   -> Difficulty.HARD
        wasRetrySuccessful && currentDifficulty == Difficulty.MEDIUM -> Difficulty.HARD
        wasRetrySuccessful                                            -> Difficulty.MEDIUM
        currentDifficulty == Difficulty.HARD                         -> Difficulty.MEDIUM
        else                                                          -> Difficulty.EASY
    }

    // ── XP rewards for learning ───────────────────────────────────────────────

    fun calculateLearningXp(
        completedExplanation: Boolean,
        retrySuccessful: Boolean,
        conceptMastered: Boolean,
        wrongAttempts: Int
    ): Int {
        var xp = 0
        if (completedExplanation) xp += 10
        if (retrySuccessful)      xp += 15
        if (conceptMastered)      xp += 30
        // Bonus for persisting through difficulty
        if (wrongAttempts >= 2 && retrySuccessful) xp += 20
        return xp
    }

    // ── Build fallback explanation (offline) ──────────────────────────────────

    fun buildOfflineExplanation(question: Question): Triple<String, String, String> {
        val explanation = question.explanation.ifBlank {
            "The correct answer is: ${question.correctAnswer}. Review this concept and try again."
        }
        val simplified = "Let's break it down simply:\n\n" +
            "Question: ${question.text}\n\n" +
            "Answer: ${question.correctAnswer}\n\n" +
            if (question.hint.isNotBlank()) "Hint: ${question.hint}" else ""

        val example = if (question.explanation.isNotBlank())
            "Remember: ${question.explanation}"
        else
            "The key concept here is: ${question.topic}"

        return Triple(explanation, simplified, example)
    }

    fun buildMindMapPoints(question: Question): List<String> {
        val points = mutableListOf<String>()
        points.add("Topic: ${question.topic}")
        if (question.hint.isNotBlank()) points.add("Key hint: ${question.hint}")
        points.add("Correct answer: ${question.correctAnswer}")
        if (question.explanation.isNotBlank()) points.add(question.explanation)
        return points
    }
}
