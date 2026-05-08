package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.VoiceSettings
import com.example.gamefiedsarvya.voice.VoiceManager
import com.example.gamefiedsarvya.voice.VoiceState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VoiceUiState(
    val voiceState: VoiceState = VoiceState.IDLE,
    val recognisedText: String = "",
    val settings: VoiceSettings = VoiceSettings(),
    val isTtsReady: Boolean = false,
    val lastSpokenText: String = "",
    val errorMessage: String = ""
)

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    init {
        VoiceManager.init(application)

        viewModelScope.launch {
            VoiceManager.voiceState.collect { vs ->
                _state.update { it.copy(voiceState = vs) }
            }
        }
        viewModelScope.launch {
            VoiceManager.recognisedText.collect { text ->
                _state.update { it.copy(recognisedText = text) }
            }
        }
        viewModelScope.launch {
            VoiceManager.isTtsReady.collect { ready ->
                _state.update { it.copy(isTtsReady = ready) }
            }
        }
    }

    // ── STT ───────────────────────────────────────────────────────────────────

    fun startListening(context: Context, onResult: (String) -> Unit) {
        if (!_state.value.settings.sttEnabled) return
        VoiceManager.startListening(
            context  = context,
            onResult = { text ->
                _state.update { it.copy(recognisedText = text) }
                onResult(text)
            },
            onError  = { msg ->
                _state.update { it.copy(errorMessage = msg) }
            }
        )
    }

    fun stopListening() = VoiceManager.stopListening()

    // ── TTS ───────────────────────────────────────────────────────────────────

    fun speak(text: String) {
        if (!_state.value.settings.ttsEnabled) return
        _state.update { it.copy(lastSpokenText = text) }
        VoiceManager.speak(text)
    }

    fun speakIfAuto(text: String) {
        if (_state.value.settings.autoNarrate) speak(text)
    }

    fun stopSpeaking() = VoiceManager.stopSpeaking()

    // ── Settings ──────────────────────────────────────────────────────────────

    fun updateSettings(settings: VoiceSettings) {
        _state.update { it.copy(settings = settings) }
        VoiceManager.setLanguage(settings.language)
    }

    fun toggleStt(enabled: Boolean) {
        val s = _state.value.settings.copy(sttEnabled = enabled)
        updateSettings(s)
    }

    fun toggleTts(enabled: Boolean) {
        val s = _state.value.settings.copy(ttsEnabled = enabled)
        updateSettings(s)
    }

    fun toggleAutoNarrate(enabled: Boolean) {
        val s = _state.value.settings.copy(autoNarrate = enabled)
        updateSettings(s)
    }

    fun setLanguage(code: String) {
        val s = _state.value.settings.copy(language = code)
        updateSettings(s)
    }

    fun clearError() { _state.update { it.copy(errorMessage = "") } }

    override fun onCleared() {
        super.onCleared()
        VoiceManager.release()
    }
}
