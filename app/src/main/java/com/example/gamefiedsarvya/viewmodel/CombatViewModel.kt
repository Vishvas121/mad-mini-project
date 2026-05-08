package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.QuestionRepository
import com.example.gamefiedsarvya.engine.AdaptiveEngine
import com.example.gamefiedsarvya.engine.CombatEngine
import com.example.gamefiedsarvya.engine.LearningEngine
import com.example.gamefiedsarvya.network.GroqService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class CombatUiState(
    // Existing fields
    val combatState: CombatState? = null,
    val isLoading: Boolean = true,
    val showLevelUpBanner: Boolean = false,
    val levelUpLevel: Int = 1,
    val showBossPhaseTransition: Boolean = false,
    val bossPhase: Int = 1,
    val showVictoryScreen: Boolean = false,
    val showDefeatScreen: Boolean = false,
    val xpGained: Int = 0,
    val screenShake: Boolean = false,
    // Learning loop fields
    val learningMode: LearningModeState = LearningModeState(),
    val conceptMastery: Map<String, ConceptMastery> = emptyMap(),
    val reinforcementSchedule: ReinforcementSchedule = ReinforcementSchedule(),
    val learningXpEarned: Int = 0,
    val showConceptMasteredBanner: Boolean = false,
    val masteredConceptName: String = ""
)

class CombatViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CombatUiState())
    val uiState: StateFlow<CombatUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTimeMs: Long = 0L
    private val usedQuestionIds = mutableSetOf<String>()
    private var currentDigitalTwin: DigitalTwin = DigitalTwin()
    private var currentZoneId: String = "zone_forest"
    private var currentTier: LearningTier = LearningTier.FOUNDATION
    private var playerName: String = "Sarvya"

    // ── Init ──────────────────────────────────────────────────────────────────

    fun startCombat(
        player: Player,
        enemy: Enemy,
        twin: DigitalTwin,
        zoneId: String,
        tier: LearningTier = LearningTier.FOUNDATION,
        name: String = "Sarvya"
    ) {
        currentDigitalTwin = twin
        currentZoneId = zoneId
        currentTier = tier
        playerName = name
        usedQuestionIds.clear()

        val firstQuestion = AdaptiveEngine.selectNextQuestion(twin, usedQuestionIds, zoneId)
            ?: QuestionRepository.getQuestionsForZone(zoneId, twin.preferredDifficulty).firstOrNull()
            ?: return

        usedQuestionIds.add(firstQuestion.id)
        val adjusted = firstQuestion.copy(
            timeLimitSeconds = AdaptiveEngine.adjustedTimeLimit(firstQuestion.timeLimitSeconds, twin)
        )
        val state = CombatEngine.startCombat(player, enemy, adjusted)
        _uiState.update { it.copy(combatState = state, isLoading = false) }
        questionStartTimeMs = System.currentTimeMillis()
        startTimer()
    }

    // ── Answer handling ───────────────────────────────────────────────────────

    fun submitAnswer(selectedIndex: Int) {
        val state = _uiState.value.combatState ?: return
        if (state.isShowingFeedback || state.result != CombatResult.ONGOING) return
        if (_uiState.value.learningMode.isActive) return

        timerJob?.cancel()
        val responseTimeMs = System.currentTimeMillis() - questionStartTimeMs
        val isCorrect = selectedIndex == state.currentQuestion?.correctIndex
        val topic = state.currentQuestion?.topic ?: ""

        state.currentQuestion?.let { q ->
            currentDigitalTwin = AdaptiveEngine.updateDigitalTwin(
                currentDigitalTwin, q, isCorrect, responseTimeMs
            )
        }

        val updatedMastery = LearningEngine.processAnswer(
            topic, isCorrect, currentDigitalTwin.preferredDifficulty,
            _uiState.value.conceptMastery
        )
        val updatedSchedule = LearningEngine.updateReinforcementSchedule(
            _uiState.value.reinforcementSchedule, topic, isCorrect, updatedMastery
        )

        val newCombatState = CombatEngine.processAnswer(state, selectedIndex, responseTimeMs)

        if (!isCorrect) triggerScreenShake()

        _uiState.update {
            it.copy(
                combatState           = newCombatState,
                conceptMastery        = updatedMastery,
                reinforcementSchedule = updatedSchedule
            )
        }

        val mastery = updatedMastery[topic]
        if (mastery?.isMastered == true && isCorrect) showMasteredBanner(topic)

        if (!isCorrect) {
            viewModelScope.launch {
                delay(800L)
                triggerLearningMode(state.currentQuestion!!, topic)
            }
        } else {
            viewModelScope.launch {
                delay(1500L)
                handlePostFeedback(newCombatState)
            }
        }
    }

    // ── Learning Mode ─────────────────────────────────────────────────────────

    private fun triggerLearningMode(question: Question, topic: String) {
        val newLearning = LearningEngine.enterLearningMode(question, _uiState.value.learningMode)
        _uiState.update { it.copy(learningMode = newLearning) }
        viewModelScope.launch { loadExplanation(question, topic, newLearning.needsSimplified) }
    }

    private suspend fun loadExplanation(question: Question, topic: String, needsSimplified: Boolean) {
        val aiExplanation = try {
            GroqService.getConceptExplanation(
                question      = question.text,
                correctAnswer = question.correctAnswer,
                topic         = topic,
                tier          = currentTier,
                playerName    = playerName,
                simplified    = needsSimplified
            )
        } catch (_: Exception) { null }

        val (offlineExplanation, simplified, example) = LearningEngine.buildOfflineExplanation(question)
        val mindMap = LearningEngine.buildMindMapPoints(question)

        val updated = LearningEngine.setExplanationLoaded(
            state         = _uiState.value.learningMode,
            explanation   = aiExplanation ?: offlineExplanation,
            simplified    = simplified,
            example       = example,
            mindMapPoints = mindMap
        )
        _uiState.update { it.copy(learningMode = updated) }
    }

    fun proceedToRetry() {
        val learning = _uiState.value.learningMode
        val triggerQ = learning.triggerQuestion ?: return
        val topic    = triggerQ.topic
        val retryDifficulty = if (learning.needsSimplified) Difficulty.EASY
                              else currentDigitalTwin.preferredDifficulty
        val retryQ = QuestionRepository.getAdaptiveQuestions(topic, retryDifficulty, 10)
            .filter { it.id !in usedQuestionIds && it.id != triggerQ.id }
            .shuffled()
            .firstOrNull() ?: triggerQ

        _uiState.update { it.copy(learningMode = LearningEngine.proceedToRetry(learning, retryQ)) }
    }

    fun submitRetry(selectedIndex: Int) {
        val learning  = _uiState.value.learningMode
        val retryQ    = learning.retryQuestion ?: return
        val isCorrect = selectedIndex == retryQ.correctIndex
        val topic     = retryQ.topic

        val xp = LearningEngine.calculateLearningXp(
            completedExplanation = true,
            retrySuccessful      = isCorrect,
            conceptMastered      = false,
            wrongAttempts        = learning.wrongAttempts
        )
        val updatedLearning = LearningEngine.handleRetryAnswer(learning, isCorrect, xp)
        val updatedMastery  = LearningEngine.processAnswer(
            topic, isCorrect, currentDigitalTwin.preferredDifficulty, _uiState.value.conceptMastery
        )

        _uiState.update {
            it.copy(
                learningMode     = updatedLearning,
                conceptMastery   = updatedMastery,
                learningXpEarned = it.learningXpEarned + xp
            )
        }

        if (isCorrect) {
            viewModelScope.launch {
                delay(2000L)
                exitLearningModeAndContinue(true)
            }
        }
    }

    fun exitLearningModeAndContinue(retryWasSuccessful: Boolean = false) {
        val combatState = _uiState.value.combatState ?: return
        val newDifficulty = LearningEngine.adaptDifficultyAfterLearning(
            currentDigitalTwin.preferredDifficulty, retryWasSuccessful
        )
        currentDigitalTwin = currentDigitalTwin.copy(preferredDifficulty = newDifficulty)
        _uiState.update { it.copy(learningMode = LearningEngine.exitLearningMode()) }

        val schedule  = _uiState.value.reinforcementSchedule
        val reinforceQ = LearningEngine.getReinforcementQuestion(
            schedule, _uiState.value.conceptMastery, usedQuestionIds, currentZoneId
        )
        if (reinforceQ != null) {
            val updatedSchedule = LearningEngine.resetReinforcementCounter(schedule)
            val newCombatState  = CombatEngine.advanceToNextQuestion(combatState, reinforceQ)
            _uiState.update { it.copy(combatState = newCombatState, reinforcementSchedule = updatedSchedule) }
            questionStartTimeMs = System.currentTimeMillis()
            startTimer()
        } else {
            loadNextQuestion(combatState)
        }
    }

    fun requestSimplifiedExplanation() {
        val learning = _uiState.value.learningMode
        val question = learning.triggerQuestion ?: return
        _uiState.update {
            it.copy(learningMode = learning.copy(phase = LearningModePhase.SIMPLIFIED, isLoadingExplanation = true))
        }
        viewModelScope.launch { loadExplanation(question, question.topic, needsSimplified = true) }
    }

    private fun showMasteredBanner(topic: String) {
        _uiState.update { it.copy(showConceptMasteredBanner = true, masteredConceptName = topic) }
        viewModelScope.launch {
            delay(3000L)
            _uiState.update { it.copy(showConceptMasteredBanner = false) }
        }
    }

    // ── Existing methods ──────────────────────────────────────────────────────

    private fun handlePostFeedback(state: CombatState) {
        when (state.result) {
            CombatResult.PLAYER_WIN  -> handleVictory(state)
            CombatResult.PLAYER_LOSE -> handleDefeat()
            CombatResult.ONGOING     -> {
                if (state.enemy.isBoss && state.enemy.currentHp <= 0 &&
                    state.enemy.phase < state.enemy.totalPhases) {
                    triggerBossPhaseTransition(state)
                } else {
                    loadNextQuestion(state)
                }
            }
        }
    }

    private fun loadNextQuestion(state: CombatState) {
        val nextQ = AdaptiveEngine.selectNextQuestion(currentDigitalTwin, usedQuestionIds, currentZoneId)
            ?: QuestionRepository.getQuestionsForZone(currentZoneId, currentDigitalTwin.preferredDifficulty)
                .filter { it.id !in usedQuestionIds }.firstOrNull()
            ?: QuestionRepository.getQuestionsForZone(currentZoneId, currentDigitalTwin.preferredDifficulty)
                .also { usedQuestionIds.clear() }.firstOrNull()
            ?: return

        usedQuestionIds.add(nextQ.id)
        val adjusted = nextQ.copy(
            timeLimitSeconds = AdaptiveEngine.adjustedTimeLimit(nextQ.timeLimitSeconds, currentDigitalTwin)
        )
        val newState = CombatEngine.advanceToNextQuestion(state, adjusted)
        _uiState.update { it.copy(combatState = newState) }
        questionStartTimeMs = System.currentTimeMillis()
        startTimer()
    }

    fun activateAbility(ability: Ability) {
        val state = _uiState.value.combatState ?: return
        _uiState.update { it.copy(combatState = CombatEngine.activateAbility(state, ability)) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value.combatState ?: break
                if (state.isShowingFeedback || state.result != CombatResult.ONGOING) break
                if (_uiState.value.learningMode.isActive) break
                val ticked = CombatEngine.tickTimer(state)
                _uiState.update { it.copy(combatState = ticked) }
                if (ticked.isShowingFeedback) {
                    delay(2000L)
                    handlePostFeedback(ticked)
                    break
                }
            }
        }
    }

    private fun triggerBossPhaseTransition(state: CombatState) {
        val newState = CombatEngine.advanceBossPhase(state)
        _uiState.update {
            it.copy(combatState = newState, showBossPhaseTransition = true, bossPhase = newState.enemy.phase)
        }
        viewModelScope.launch {
            delay(2500L)
            _uiState.update { it.copy(showBossPhaseTransition = false) }
            loadNextQuestion(newState)
        }
    }

    private fun handleVictory(state: CombatState) {
        val xp = state.enemy.xpReward + _uiState.value.learningXpEarned
        val updatedPlayer = CombatEngine.awardXp(state.player, xp)
        val didLevelUp = updatedPlayer.level > state.player.level
        _uiState.update {
            it.copy(
                combatState       = state.copy(player = updatedPlayer),
                showVictoryScreen = true,
                xpGained          = xp,
                showLevelUpBanner = didLevelUp,
                levelUpLevel      = updatedPlayer.level
            )
        }
        // Sync concept mastery to Supabase
        viewModelScope.launch {
            com.example.gamefiedsarvya.data.remote.SessionRepository.syncConceptMastery(
                userName = playerName,
                mastery  = _uiState.value.conceptMastery
            )
        }
    }

    private fun handleDefeat() {
        _uiState.update { it.copy(showDefeatScreen = true) }
    }

    private fun triggerScreenShake() {
        viewModelScope.launch {
            _uiState.update { it.copy(screenShake = true) }
            delay(500L)
            _uiState.update { it.copy(screenShake = false) }
        }
    }

    fun getUpdatedDigitalTwin(): DigitalTwin = currentDigitalTwin
    fun dismissVictory() { _uiState.update { it.copy(showVictoryScreen = false, showLevelUpBanner = false) } }
    fun dismissDefeat()  { _uiState.update { it.copy(showDefeatScreen = false) } }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
