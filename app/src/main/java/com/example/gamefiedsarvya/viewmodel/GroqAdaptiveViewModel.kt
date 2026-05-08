package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.QuestionRepository
import com.example.gamefiedsarvya.engine.AdaptiveEngine
import com.example.gamefiedsarvya.network.GeneratedQuestion
import com.example.gamefiedsarvya.network.GroqService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GroqAdaptiveState(
    val currentQuestion: Question? = null,
    val aiGeneratedQuestion: GeneratedQuestion? = null,
    val isLoadingQuestion: Boolean = false,
    val aiFeedback: String = "",
    val aiHint: String = "",
    val isLoadingFeedback: Boolean = false,
    val isLoadingHint: Boolean = false,
    val aiTopicSummary: String = "",
    val isLoadingSummary: Boolean = false,
    val useAiQuestion: Boolean = false,
    val errorMessage: String = ""
)

class GroqAdaptiveViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GroqAdaptiveState())
    val state: StateFlow<GroqAdaptiveState> = _state.asStateFlow()

    // Track used question texts to prevent repeats
    private val usedQuestionTexts = mutableSetOf<String>()
    private val usedOfflineIds    = mutableSetOf<String>()

    // ── Generate question (Groq with no-repeat + hard mode) ───────────────────

    fun generateQuestion(
        topic: String,
        twin: DigitalTwin,
        tier: LearningTier,
        playerName: String,
        zoneId: String
    ) {
        val difficulty = AdaptiveEngine.computeNextDifficulty(twin)
        _state.update { it.copy(isLoadingQuestion = true, errorMessage = "") }

        viewModelScope.launch {
            val aiQ = GroqService.generateAdaptiveQuestion(
                topic              = topic,
                difficulty         = difficulty,
                tier               = tier,
                playerName         = playerName,
                recentAccuracy     = twin.recentAccuracy,
                usedQuestionTexts  = usedQuestionTexts
            )

            if (aiQ != null && aiQ.text !in usedQuestionTexts) {
                usedQuestionTexts.add(aiQ.text)
                _state.update { it.copy(
                    aiGeneratedQuestion = aiQ,
                    useAiQuestion       = true,
                    isLoadingQuestion   = false
                )}
            } else {
                // Offline fallback — no repeats
                val offlineQ = QuestionRepository.getAdaptiveQuestions(topic, difficulty, 20)
                    .filter { it.id !in usedOfflineIds }
                    .shuffled()
                    .firstOrNull()
                    ?: run {
                        // All used — reset and reshuffle
                        usedOfflineIds.clear()
                        QuestionRepository.getQuestionsForZone(zoneId, difficulty)
                            .shuffled().firstOrNull()
                    }

                offlineQ?.let { usedOfflineIds.add(it.id) }
                _state.update { it.copy(
                    currentQuestion   = offlineQ,
                    useAiQuestion     = false,
                    isLoadingQuestion = false
                )}
            }
        }
    }

    fun resetUsedQuestions() {
        usedQuestionTexts.clear()
        usedOfflineIds.clear()
    }

    // ── Hint ──────────────────────────────────────────────────────────────────

    fun requestHint(
        questionText: String,
        topic: String,
        tier: LearningTier,
        playerName: String,
        fallbackHint: String
    ) {
        _state.update { it.copy(isLoadingHint = true, aiHint = "") }
        viewModelScope.launch {
            val hint = GroqService.getTutorHint(questionText, topic, tier, playerName)
                ?: fallbackHint
            _state.update { it.copy(aiHint = hint, isLoadingHint = false) }
        }
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    fun requestFeedback(
        playerName: String,
        wasCorrect: Boolean,
        topic: String,
        explanation: String,
        tier: LearningTier,
        streakCount: Int
    ) {
        _state.update { it.copy(isLoadingFeedback = true, aiFeedback = "") }
        viewModelScope.launch {
            val feedback = GroqService.getPersonalisedFeedback(
                playerName, wasCorrect, topic, explanation, tier, streakCount
            ) ?: if (wasCorrect) "Great job, $playerName!" else "Keep going, $playerName!"
            _state.update { it.copy(aiFeedback = feedback, isLoadingFeedback = false) }
        }
    }

    // ── Topic summary ─────────────────────────────────────────────────────────

    fun requestTopicSummary(topic: String, tier: LearningTier, playerName: String) {
        _state.update { it.copy(isLoadingSummary = true, aiTopicSummary = "") }
        viewModelScope.launch {
            val summary = GroqService.summariseTopic(topic, tier, playerName)
                ?: "Study $topic to prepare for your next challenge!"
            _state.update { it.copy(aiTopicSummary = summary, isLoadingSummary = false) }
        }
    }

    fun clearFeedback() { _state.update { it.copy(aiFeedback = "", aiHint = "") } }
}
