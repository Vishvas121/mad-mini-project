package com.example.gamefiedsarvya.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * FocusViewModel
 *
 * Manages the FocusDetector lifecycle and exposes smoothed focus/engagement
 * scores to the UI. Also feeds real values into the DigitalTwin via
 * GameViewModel.updateFocusSignals().
 *
 * Smoothing: exponential moving average (alpha=0.25) to avoid jitter.
 */
class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    private var detector: FocusDetector? = null
    private var isRunning = false

    // EMA smoothing
    private var smoothFocus      = 0.5f
    private var smoothEngagement = 0.5f
    private val ALPHA = 0.25f

    // Alert history for debouncing (only alert after 3 consecutive bad frames)
    private var consecutiveBadFrames = 0
    private val BAD_FRAME_THRESHOLD  = 3

    fun startDetection(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        if (isRunning) return
        isRunning = true
        val det = FocusDetector(getApplication())
        detector = det
        det.start(lifecycleOwner) { result ->
            viewModelScope.launch { processResult(result) }
        }
        _state.update { it.copy(isActive = true) }
    }

    fun stopDetection() {
        detector?.stop()
        detector = null
        isRunning = false
        _state.update { it.copy(isActive = false) }
    }

    private fun processResult(result: FocusResult) {
        // Smooth the scores
        smoothFocus      = smoothFocus      * (1f - ALPHA) + result.focusScore      * ALPHA
        smoothEngagement = smoothEngagement * (1f - ALPHA) + result.engagementScore * ALPHA

        // Debounce alerts
        val isBad = result.alertState != AlertState.FOCUSED &&
                    result.alertState != AlertState.UNAVAILABLE
        if (isBad) consecutiveBadFrames++ else consecutiveBadFrames = 0
        val showAlert = consecutiveBadFrames >= BAD_FRAME_THRESHOLD

        _state.update {
            it.copy(
                latest          = result,
                smoothFocus     = smoothFocus,
                smoothEngagement = smoothEngagement,
                showAlert       = showAlert,
                alertState      = if (showAlert) result.alertState else AlertState.FOCUSED,
                // Running stats
                totalFrames     = it.totalFrames + 1,
                focusedFrames   = it.focusedFrames + if (result.alertState == AlertState.FOCUSED) 1 else 0
            )
        }
    }

    fun dismissAlert() {
        consecutiveBadFrames = 0
        _state.update { it.copy(showAlert = false) }
    }

    /** Returns smoothed values suitable for DigitalTwin injection */
    fun getFocusSignals(): Pair<Float, Float> = Pair(smoothFocus, smoothEngagement)

    override fun onCleared() {
        super.onCleared()
        stopDetection()
    }
}

data class FocusUiState(
    val isActive:         Boolean     = false,
    val latest:           FocusResult = FocusResult.unavailable(),
    val smoothFocus:      Float       = 0.5f,
    val smoothEngagement: Float       = 0.5f,
    val showAlert:        Boolean     = false,
    val alertState:       AlertState  = AlertState.UNAVAILABLE,
    val totalFrames:      Int         = 0,
    val focusedFrames:    Int         = 0
) {
    val focusPercent: Int get() =
        if (totalFrames == 0) 0 else (focusedFrames * 100 / totalFrames)
}
