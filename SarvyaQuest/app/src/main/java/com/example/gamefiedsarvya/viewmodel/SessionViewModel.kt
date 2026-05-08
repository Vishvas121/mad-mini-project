package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.remote.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionUiState(
    val activeSession:  LearningSession? = null,
    val isRecording:    Boolean = false,
    val isSaving:       Boolean = false,
    val savedSession:   LearningSession? = null,
    val publicStream:   List<StreamCard> = emptyList(),
    val topPerformers:  List<StreamCard> = emptyList(),
    val userSessions:   List<LearningSession> = emptyList(),
    val replaySession:  LearningSession? = null,
    val replayIndex:    Int = 0,
    val isLoadingStream: Boolean = false,
    val shareCode:      String = "",
    val errorMessage:   String = "",
    val earnedBadges:   List<Badge> = emptyList(),
    val showBadgeToast: Badge? = null
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    // ── Session recording ─────────────────────────────────────────────────────

    fun startSession(
        userName:  String,
        tier:      LearningTier,
        topic:     String,
        language:  String,
        voiceMode: Boolean
    ) {
        _state.update {
            it.copy(
                activeSession = LearningSession(
                    userName  = userName,
                    tier      = tier.name,
                    topic     = topic,
                    language  = language,
                    voiceMode = voiceMode
                ),
                isRecording = true
            )
        }
    }

    fun recordEvent(event: SessionEvent) {
        val current = _state.value.activeSession ?: return
        _state.update {
            it.copy(
                activeSession = current.copy(
                    events         = current.events + event,
                    totalQuestions = current.totalQuestions + 1,
                    correctAnswers = current.correctAnswers + (if (event.wasCorrect) 1 else 0),
                    totalTimeMs    = current.totalTimeMs + event.timeMs
                )
            )
        }
    }

    fun endSession(xpEarned: Int) {
        val current = _state.value.activeSession ?: return
        val final   = current.copy(xpEarned = xpEarned)
        _state.update { it.copy(activeSession = final, isRecording = false) }
        saveSession(final)
    }

    // ── Supabase: save session ────────────────────────────────────────────────

    private fun saveSession(session: LearningSession) {
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            SessionRepository.saveSession(session).fold(
                onSuccess = { saved ->
                    val badges = checkBadges(saved)
                    _state.update {
                        it.copy(
                            isSaving       = false,
                            savedSession   = saved,
                            shareCode      = saved.shareCode,
                            earnedBadges   = it.earnedBadges + badges,
                            showBadgeToast = badges.firstOrNull()
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isSaving = false, errorMessage = "Save failed: ${e.message}") }
                }
            )
        }
    }

    // ── Supabase: load feeds ──────────────────────────────────────────────────

    fun loadPublicStream() {
        _state.update { it.copy(isLoadingStream = true) }
        viewModelScope.launch {
            SessionRepository.fetchPublicStream().fold(
                onSuccess = { cards -> _state.update { it.copy(publicStream = cards, isLoadingStream = false) } },
                onFailure = { _state.update { it.copy(isLoadingStream = false) } }
            )
        }
    }

    fun loadTopPerformers() {
        viewModelScope.launch {
            SessionRepository.fetchTopPerformers().fold(
                onSuccess = { cards -> _state.update { it.copy(topPerformers = cards) } },
                onFailure = {}
            )
        }
    }

    fun loadUserSessions(userName: String) {
        viewModelScope.launch {
            SessionRepository.fetchUserSessions(userName).fold(
                onSuccess = { sessions -> _state.update { it.copy(userSessions = sessions) } },
                onFailure = {}
            )
        }
    }

    // ── Replay ────────────────────────────────────────────────────────────────

    fun startReplay(session: LearningSession) {
        _state.update { it.copy(replaySession = session, replayIndex = 0) }
    }

    fun replayNext() {
        val session = _state.value.replaySession ?: return
        val next    = _state.value.replayIndex + 1
        if (next >= session.events.size) {
            _state.update { it.copy(replaySession = null, replayIndex = 0) }
        } else {
            _state.update { it.copy(replayIndex = next) }
        }
    }

    fun loadSessionByCode(code: String) {
        viewModelScope.launch {
            SessionRepository.fetchSessionByCode(code).fold(
                onSuccess = { session -> session?.let { startReplay(it) } },
                onFailure = { _state.update { it.copy(errorMessage = "Session not found") } }
            )
        }
    }

    // ── Supabase: sync profile + skill tree + hub ─────────────────────────────

    fun syncProfile(
        userName:  String,
        tier:      LearningTier,
        level:     Int,
        totalXp:   Int,
        accuracy:  Float,
        streakDays: Int = 0,
        unlockedAbilities: List<String> = emptyList(),
        badges: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            SessionRepository.syncUserProfile(
                userName           = userName,
                tier               = tier.name,
                level              = level,
                totalXp            = totalXp,
                accuracy           = accuracy,
                streakDays         = streakDays,
                unlockedAbilities  = unlockedAbilities,
                badges             = badges
            )
        }
    }

    fun syncSkillTree(userName: String, unlockedNodes: List<String>, xpTotal: Int, level: Int) {
        viewModelScope.launch {
            SessionRepository.syncSkillProgress(userName, unlockedNodes, xpTotal, level)
        }
    }

    fun syncHubProgress(
        userName: String,
        studiedIds: List<String>,
        totalStudyXp: Int,
        studyMinutes: Int,
        preparedTopics: List<String>
    ) {
        viewModelScope.launch {
            SessionRepository.syncHubProgress(userName, studiedIds, totalStudyXp, studyMinutes, preparedTopics)
        }
    }

    // ── Badge logic ───────────────────────────────────────────────────────────

    private fun checkBadges(session: LearningSession): List<Badge> {
        val earned   = mutableListOf<Badge>()
        val existing = _state.value.earnedBadges.map { it.id }.toSet()

        if ("first_share"   !in existing)
            earned.add(ALL_BADGES.first { it.id == "first_share" })
        if (session.accuracyPct == 100 && "perfect_score" !in existing)
            earned.add(ALL_BADGES.first { it.id == "perfect_score" })
        if (session.voiceMode && "voice_learner" !in existing)
            earned.add(ALL_BADGES.first { it.id == "voice_learner" })
        if (session.accuracyPct >= 90 &&
            session.tier in listOf("ADVANCED", "PROFESSIONAL") &&
            "top_performer" !in existing)
            earned.add(ALL_BADGES.first { it.id == "top_performer" })

        return earned
    }

    fun dismissBadgeToast() { _state.update { it.copy(showBadgeToast = null) } }
    fun clearError()         { _state.update { it.copy(errorMessage = "") } }
    fun clearShareCode()     { _state.update { it.copy(shareCode = "") } }
}
