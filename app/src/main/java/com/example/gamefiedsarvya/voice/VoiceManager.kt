package com.example.gamefiedsarvya.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Manages STT (SpeechRecognizer) and TTS (TextToSpeech).
 * Uses Android built-in engines — no external API needed.
 * Call init(context) once, release() on destroy.
 */
object VoiceManager {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState

    private val _recognisedText = MutableStateFlow("")
    val recognisedText: StateFlow<String> = _recognisedText

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var currentLanguage: Locale = Locale.ENGLISH

    // ── Init ──────────────────────────────────────────────────────────────────

    fun init(context: Context) {
        initTts(context)
    }

    private fun initTts(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = currentLanguage
                _isTtsReady.value = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?)  { _voiceState.value = VoiceState.SPEAKING }
                    override fun onDone(utteranceId: String?)   { _voiceState.value = VoiceState.IDLE }
                    override fun onError(utteranceId: String?)  { _voiceState.value = VoiceState.ERROR }
                })
            }
        }
    }

    // ── Language ──────────────────────────────────────────────────────────────

    fun setLanguage(langCode: String) {
        currentLanguage = when (langCode) {
            "ta" -> Locale("ta", "IN")
            "hi" -> Locale("hi", "IN")
            "te" -> Locale("te", "IN")
            "ml" -> Locale("ml", "IN")
            "kn" -> Locale("kn", "IN")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "es" -> Locale("es", "ES")
            "ja" -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }
        tts?.language = currentLanguage
    }

    // ── STT ───────────────────────────────────────────────────────────────────

    fun startListening(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }

        stopListening()
        _voiceState.value = VoiceState.LISTENING
        _recognisedText.value = ""

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { _voiceState.value = VoiceState.PROCESSING }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    _recognisedText.value = text
                    _voiceState.value = VoiceState.IDLE
                    onResult(text)
                }

                override fun onError(error: Int) {
                    _voiceState.value = VoiceState.ERROR
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH       -> "No speech detected"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                        SpeechRecognizer.ERROR_AUDIO          -> "Audio error"
                        SpeechRecognizer.ERROR_NETWORK        -> "Network error"
                        else                                  -> "Recognition error ($error)"
                    }
                    onError(msg)
                    _voiceState.value = VoiceState.IDLE
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    _recognisedText.value = partial
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            startListening(intent)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        if (_voiceState.value == VoiceState.LISTENING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    // ── TTS ───────────────────────────────────────────────────────────────────

    fun speak(text: String, utteranceId: String = "sarvya_tts") {
        if (!_isTtsReady.value) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _voiceState.value = VoiceState.IDLE
    }

    val isSpeaking: Boolean get() = tts?.isSpeaking == true

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun release() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isTtsReady.value = false
        _voiceState.value = VoiceState.IDLE
    }
}

// Re-export VoiceState here so it's in the same package
enum class VoiceState { IDLE, LISTENING, PROCESSING, SPEAKING, ERROR }
