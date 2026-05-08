package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.LearningHubRepository
import com.example.gamefiedsarvya.ui.theme.AppTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.hubDataStore by preferencesDataStore(name = "sarvya_hub")

data class HubUiState(
    val selectedTier: LearningTier = LearningTier.FOUNDATION,
    val selectedTheme: AppTheme = AppTheme.DARK_FANTASY,
    val learningProgress: LearningProgress = LearningProgress(),
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.8f,
    val showTierSelect: Boolean = false
)

class LearningHubViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val TIER_KEY     = stringPreferencesKey("selected_tier")
    private val PROGRESS_KEY = stringPreferencesKey("learning_progress")
    private val MUSIC_KEY    = booleanPreferencesKey("music_enabled")
    private val SFX_KEY      = booleanPreferencesKey("sfx_enabled")
    private val MUSIC_VOL    = floatPreferencesKey("music_volume")
    private val SFX_VOL      = floatPreferencesKey("sfx_volume")
    private val THEME_KEY    = stringPreferencesKey("selected_theme")

    private val _uiState = MutableStateFlow(HubUiState())
    val uiState: StateFlow<HubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            application.hubDataStore.data.collect { prefs ->
                val tier = try {
                    LearningTier.valueOf(prefs[TIER_KEY] ?: LearningTier.FOUNDATION.name)
                } catch (e: Exception) { LearningTier.FOUNDATION }

                val progress = try {
                    prefs[PROGRESS_KEY]?.let { gson.fromJson(it, LearningProgress::class.java) }
                        ?: LearningProgress()
                } catch (e: Exception) { LearningProgress() }

                _uiState.update {
                    it.copy(
                        selectedTier     = tier,
                        selectedTheme    = try {
                            AppTheme.valueOf(prefs[THEME_KEY] ?: AppTheme.DARK_FANTASY.name)
                        } catch (e: Exception) { AppTheme.DARK_FANTASY },
                        learningProgress = progress,
                        musicEnabled     = prefs[MUSIC_KEY] ?: true,
                        sfxEnabled       = prefs[SFX_KEY]   ?: true,
                        musicVolume      = prefs[MUSIC_VOL] ?: 0.7f,
                        sfxVolume        = prefs[SFX_VOL]   ?: 0.8f
                    )
                }
            }
        }
    }

    fun setTier(tier: LearningTier) {
        viewModelScope.launch {
            getApplication<Application>().hubDataStore.edit { prefs ->
                prefs[TIER_KEY] = tier.name
            }
            _uiState.update { it.copy(selectedTier = tier, showTierSelect = false) }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            getApplication<Application>().hubDataStore.edit { prefs ->
                prefs[THEME_KEY] = theme.name
            }
            _uiState.update { it.copy(selectedTheme = theme) }
        }
    }

    fun markMaterialStudied(materialId: String, xpReward: Int) {
        viewModelScope.launch {
            val current  = _uiState.value.learningProgress
            val material = LearningHubRepository.getMaterialById(materialId) ?: return@launch
            val newProgress = current.copy(
                studiedMaterialIds = current.studiedMaterialIds + materialId,
                totalStudyXp       = current.totalStudyXp + xpReward,
                studyTimeMinutes   = current.studyTimeMinutes + material.estimatedMinutes,
                preparedTopics     = current.preparedTopics + material.topic
            )
            _uiState.update { it.copy(learningProgress = newProgress) }
            getApplication<Application>().hubDataStore.edit { prefs ->
                prefs[PROGRESS_KEY] = gson.toJson(newProgress)
            }
            // Sync to Supabase
            val userName = _uiState.value.selectedTier.name  // placeholder — wire real name
            com.example.gamefiedsarvya.data.remote.SessionRepository.syncHubProgress(
                userName       = userName,
                studiedIds     = newProgress.studiedMaterialIds.toList(),
                totalStudyXp   = newProgress.totalStudyXp,
                studyMinutes   = newProgress.studyTimeMinutes,
                preparedTopics = newProgress.preparedTopics.toList()
            )
        }
    }

    fun isTopicPrepared(topic: String): Boolean =
        _uiState.value.learningProgress.preparedTopics.contains(topic)

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            getApplication<Application>().hubDataStore.edit { it[MUSIC_KEY] = enabled }
            _uiState.update { s -> s.copy(musicEnabled = enabled) }
            // Apply immediately to the running player
            com.example.gamefiedsarvya.audio.AudioManager.applyMusicEnabled(enabled)
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        viewModelScope.launch {
            getApplication<Application>().hubDataStore.edit { it[SFX_KEY] = enabled }
            _uiState.update { s -> s.copy(sfxEnabled = enabled) }
            com.example.gamefiedsarvya.audio.AudioManager.sfxEnabled = enabled
        }
    }

    fun setMusicVolume(vol: Float) {
        viewModelScope.launch {
            getApplication<Application>().hubDataStore.edit { it[MUSIC_VOL] = vol }
            _uiState.update { s -> s.copy(musicVolume = vol) }
            com.example.gamefiedsarvya.audio.AudioManager.applyMusicVolume(vol)
        }
    }

    fun setSfxVolume(vol: Float) {
        viewModelScope.launch {
            getApplication<Application>().hubDataStore.edit { it[SFX_VOL] = vol }
            _uiState.update { s -> s.copy(sfxVolume = vol) }
            com.example.gamefiedsarvya.audio.AudioManager.sfxVolume = vol
        }
    }

    fun showTierSelect() { _uiState.update { it.copy(showTierSelect = true) } }
    fun hideTierSelect() { _uiState.update { it.copy(showTierSelect = false) } }

    fun getRecommendedMaterials(): List<com.example.gamefiedsarvya.data.models.StudyMaterial> {
        val state = _uiState.value
        return LearningHubRepository.getRecommendedMaterials(
            tier        = state.selectedTier,
            weakTopics  = emptyList(),
            studiedIds  = state.learningProgress.studiedMaterialIds
        )
    }
}
