package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.QuestionRepository
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.*

/**
 * AI-enhanced practice mode with:
 * - Groq adaptive questions (offline fallback)
 * - Voice input (STT) and output (TTS)
 * - Session recording → Supabase sync
 * - Interaction tagging
 */
@Composable
fun AIPracticeScreen(
    gameViewModel:    GameViewModel,
    hubViewModel:     LearningHubViewModel,
    profileViewModel: UserProfileViewModel,
    groqViewModel:    GroqAdaptiveViewModel,
    sessionViewModel: SessionViewModel,
    voiceViewModel:   VoiceViewModel,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val progress     by gameViewModel.progress.collectAsState()
    val hubState     by hubViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val groqState    by groqViewModel.state.collectAsState()
    val sessionState by sessionViewModel.state.collectAsState()
    val voiceState   by voiceViewModel.state.collectAsState()
    val theme        = TierThemes.forTier(hubState.selectedTier)

    val topics       = remember { QuestionRepository.getAllTopics() }
    var selectedTopic   by remember { mutableStateOf(topics.firstOrNull() ?: "Technology") }
    var showFeedback    by remember { mutableStateOf(false) }
    var lastCorrect     by remember { mutableStateOf<Boolean?>(null) }
    var totalAnswered   by remember { mutableIntStateOf(0) }
    var totalCorrect    by remember { mutableIntStateOf(0) }
    var hintVisible     by remember { mutableStateOf(false) }
    var questionStartMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val playerName = profileState.profile.displayName
    val isVoiceMode = voiceState.settings.sttEnabled || voiceState.settings.ttsEnabled

    // Start session recording on entry
    LaunchedEffect(Unit) {
        sessionViewModel.startSession(
            userName  = playerName,
            tier      = hubState.selectedTier,
            topic     = selectedTopic,
            language  = voiceState.settings.language,
            voiceMode = isVoiceMode
        )
    }

    // Load first question
    LaunchedEffect(selectedTopic, hubState.selectedTier) {
        questionStartMs = System.currentTimeMillis()
        groqViewModel.generateQuestion(
            topic      = selectedTopic,
            twin       = progress.digitalTwin,
            tier       = hubState.selectedTier,
            playerName = playerName,
            zoneId     = "zone_forest"
        )
    }

    // Auto-narrate question when loaded
    LaunchedEffect(groqState.aiGeneratedQuestion, groqState.currentQuestion) {
        val qText = groqState.aiGeneratedQuestion?.text ?: groqState.currentQuestion?.text
        if (qText != null) voiceViewModel.speakIfAuto(qText)
    }

    // Handle voice-recognised answer
    LaunchedEffect(voiceState.recognisedText) {
        val text = voiceState.recognisedText.trim().lowercase()
        if (text.isBlank() || showFeedback) return@LaunchedEffect
        val options = groqState.aiGeneratedQuestion?.options ?: groqState.currentQuestion?.options ?: return@LaunchedEffect
        // Match spoken text to option A/B/C/D or option content
        val matchedIdx = when {
            text.startsWith("a") || text == "option a" -> 0
            text.startsWith("b") || text == "option b" -> 1
            text.startsWith("c") || text == "option c" -> 2
            text.startsWith("d") || text == "option d" -> 3
            else -> options.indexOfFirst { it.lowercase().contains(text) }
        }
        if (matchedIdx >= 0) submitAnswer(
            idx             = matchedIdx,
            options         = options,
            correctIndex    = groqState.aiGeneratedQuestion?.correctIndex ?: groqState.currentQuestion?.correctIndex ?: 0,
            explanation     = groqState.aiGeneratedQuestion?.explanation ?: groqState.currentQuestion?.explanation ?: "",
            questionText    = groqState.aiGeneratedQuestion?.text ?: groqState.currentQuestion?.text ?: "",
            topic           = selectedTopic,
            voiceUsed       = true,
            questionStartMs = questionStartMs,
            onCorrect       = { totalCorrect++ },
            onResult        = { correct ->
                lastCorrect = correct; showFeedback = true; totalAnswered++
                recordEvent(
                    sessionViewModel, groqState, correct, matchedIdx,
                    selectedTopic, voiceState.settings.language, true, questionStartMs
                )
                voiceViewModel.speakIfAuto(
                    if (correct) "Correct! ${groqState.aiGeneratedQuestion?.explanation ?: ""}"
                    else "Incorrect. ${groqState.aiGeneratedQuestion?.explanation ?: ""}"
                )
                groqViewModel.requestFeedback(
                    playerName, correct, selectedTopic,
                    groqState.aiGeneratedQuestion?.explanation ?: "",
                    hubState.selectedTier, progress.player.streakCount
                )
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(theme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = {
                    // End session and sync before leaving
                    sessionViewModel.endSession(xpEarned = totalCorrect * 10)
                    sessionViewModel.syncProfile(
                        playerName, hubState.selectedTier,
                        progress.player.level, progress.player.xp, progress.player.accuracy
                    )
                    onBack()
                }, color = TextSecondary)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AI PRACTICE", style = theme.titleStyle.copy(color = theme.primary))
                    Text("Powered by Groq", style = theme.labelStyle.copy(color = TextMuted))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("$totalCorrect/$totalAnswered",
                        style = theme.labelStyle.copy(color = theme.accent))
                    if (isVoiceMode) Text("🎙 Voice", style = theme.labelStyle.copy(color = NeonGreen))
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Voice status banner ───────────────────────────────────────────
            VoiceStatusBanner(
                voiceState    = voiceState.voiceState,
                recognisedText = voiceState.recognisedText,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Badge toast ───────────────────────────────────────────────────
            BadgeToast(
                badge     = sessionState.showBadgeToast,
                onDismiss = { sessionViewModel.dismissBadgeToast() }
            )

            Spacer(Modifier.height(10.dp))

            // ── AI status ─────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingGlowDot(if (groqState.useAiQuestion) NeonGreen else NeonOrange)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (groqState.useAiQuestion) "AI-generated • ${voiceState.settings.language.uppercase()}"
                    else "Offline bank",
                    style = theme.labelStyle.copy(
                        color = if (groqState.useAiQuestion) NeonGreen else NeonOrange
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Topic selector ────────────────────────────────────────────────
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach { topic ->
                    Box(
                        modifier = Modifier
                            .border(1.dp,
                                if (selectedTopic == topic) theme.primary else CardBorder,
                                RoundedCornerShape(20.dp))
                            .background(
                                if (selectedTopic == topic) theme.primary.copy(alpha = 0.15f)
                                else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedTopic = topic
                                showFeedback = false; hintVisible = false
                                groqViewModel.clearFeedback()
                                questionStartMs = System.currentTimeMillis()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(topic, style = theme.labelStyle.copy(
                            color = if (selectedTopic == topic) theme.primary else TextSecondary
                        ))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Loading ───────────────────────────────────────────────────────
            if (groqState.isLoadingQuestion) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = theme.primary)
                        Spacer(Modifier.height(10.dp))
                        Text("AI crafting your question…",
                            style = theme.bodyStyle.copy(color = TextSecondary))
                    }
                }
            } else {
                val questionText = groqState.aiGeneratedQuestion?.text
                    ?: groqState.currentQuestion?.text ?: "Loading…"
                val options = groqState.aiGeneratedQuestion?.options
                    ?: groqState.currentQuestion?.options ?: emptyList()
                val correctIndex = groqState.aiGeneratedQuestion?.correctIndex
                    ?: groqState.currentQuestion?.correctIndex ?: 0
                val explanation = groqState.aiGeneratedQuestion?.explanation
                    ?: groqState.currentQuestion?.explanation ?: ""
                val fallbackHint = groqState.aiGeneratedQuestion?.hint
                    ?: groqState.currentQuestion?.hint ?: ""

                // ── Question card ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.primary.copy(alpha = 0.4f),
                            RoundedCornerShape(theme.cardRadius.toInt().dp))
                        .background(theme.surface,
                            RoundedCornerShape(theme.cardRadius.toInt().dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DifficultyBadge(
                                progress.digitalTwin.preferredDifficulty.name,
                                when (progress.digitalTwin.preferredDifficulty) {
                                    Difficulty.EASY   -> NeonGreen
                                    Difficulty.MEDIUM -> NeonOrange
                                    Difficulty.HARD   -> NeonRed
                                }
                            )
                            // TTS play button
                            SpeakButton(
                                text       = questionText,
                                isSpeaking = voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.SPEAKING,
                                enabled    = voiceState.settings.ttsEnabled,
                                onSpeak    = { voiceViewModel.speak(it) },
                                onStop     = { voiceViewModel.stopSpeaking() }
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(questionText,
                            style = theme.bodyStyle.copy(fontWeight = FontWeight.SemiBold))

                        // Hint
                        if (hintVisible) {
                            Spacer(Modifier.height(8.dp))
                            if (groqState.isLoadingHint) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = NeonGold, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("AI thinking…",
                                        style = theme.labelStyle.copy(color = NeonGold))
                                }
                            } else {
                                val hint = groqState.aiHint.ifBlank { fallbackHint }
                                if (hint.isNotBlank()) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NeonGold.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                    ) {
                                        Text("💡 $hint",
                                            style = theme.bodyStyle.copy(color = NeonGold))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Hint + Mic row ────────────────────────────────────────────
                if (!showFeedback) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!hintVisible) {
                            NeonButton("💡 AI Hint", onClick = {
                                hintVisible = true
                                groqViewModel.requestHint(
                                    questionText, selectedTopic,
                                    hubState.selectedTier, playerName, fallbackHint
                                )
                            }, color = NeonGold)
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        // Mic button for voice answer
                        if (voiceState.settings.sttEnabled) {
                            MicButton(
                                voiceState = voiceState.voiceState,
                                enabled    = !showFeedback,
                                onTap      = {
                                    if (voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.LISTENING) {
                                        voiceViewModel.stopListening()
                                    } else {
                                        voiceViewModel.startListening(context) { /* handled via LaunchedEffect */ }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ── Answer options ────────────────────────────────────────────
                options.forEachIndexed { idx, opt ->
                    val isCorrect = idx == correctIndex
                    val optColor = when {
                        !showFeedback -> theme.primary
                        isCorrect     -> NeonGreen
                        else          -> NeonRed
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, optColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .background(
                                if (showFeedback && isCorrect) optColor.copy(alpha = 0.2f)
                                else optColor.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !showFeedback) {
                                val correct = idx == correctIndex
                                lastCorrect = correct; showFeedback = true; totalAnswered++
                                if (correct) totalCorrect++
                                recordEvent(
                                    sessionViewModel, groqState, correct, idx,
                                    selectedTopic, voiceState.settings.language, false, questionStartMs
                                )
                                voiceViewModel.speakIfAuto(
                                    if (correct) "Correct! $explanation" else "Incorrect. $explanation"
                                )
                                groqViewModel.requestFeedback(
                                    playerName, correct, selectedTopic, explanation,
                                    hubState.selectedTier, progress.player.streakCount
                                )
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${('A' + idx)}. ",
                                style = theme.labelStyle.copy(color = optColor),
                                modifier = Modifier.width(28.dp))
                            Text(opt, style = theme.bodyStyle)
                            if (showFeedback && isCorrect) {
                                Spacer(Modifier.weight(1f))
                                Text("✓", style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen))
                            }
                        }
                    }
                }

                // ── Feedback ──────────────────────────────────────────────────
                AnimatedVisibility(visible = showFeedback) {
                    Column {
                        Spacer(Modifier.height(10.dp))
                        if (groqState.isLoadingFeedback) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = theme.primary, strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("AI personalising feedback…",
                                    style = theme.labelStyle.copy(color = TextSecondary))
                            }
                        } else if (groqState.aiFeedback.isNotBlank()) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (lastCorrect == true) NeonGreen.copy(alpha = 0.1f)
                                    else NeonRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(groqState.aiFeedback,
                                        style = theme.bodyStyle.copy(
                                            color = if (lastCorrect == true) NeonGreen else NeonRed
                                        ), modifier = Modifier.weight(1f))
                                    SpeakButton(
                                        text       = groqState.aiFeedback,
                                        isSpeaking = voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.SPEAKING,
                                        enabled    = voiceState.settings.ttsEnabled,
                                        onSpeak    = { voiceViewModel.speak(it) },
                                        onStop     = { voiceViewModel.stopSpeaking() }
                                    )
                                }
                            }
                        }

                        if (explanation.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NeonGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .background(NeonGold.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                            ) {
                                Text("📖 $explanation",
                                    style = theme.bodyStyle.copy(color = NeonGold))
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        NeonButton("Next Question →", onClick = {
                            showFeedback = false; hintVisible = false
                            groqViewModel.clearFeedback()
                            questionStartMs = System.currentTimeMillis()
                            groqViewModel.generateQuestion(
                                topic      = selectedTopic,
                                twin       = progress.digitalTwin,
                                tier       = hubState.selectedTier,
                                playerName = playerName,
                                zoneId     = "zone_forest"
                            )
                        }, modifier = Modifier.fillMaxWidth(), color = theme.primary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun recordEvent(
    sessionViewModel: SessionViewModel,
    groqState: com.example.gamefiedsarvya.viewmodel.GroqAdaptiveState,
    wasCorrect: Boolean,
    selectedIdx: Int,
    topic: String,
    language: String,
    voiceUsed: Boolean,
    startMs: Long
) {
    val q       = groqState.aiGeneratedQuestion ?: groqState.currentQuestion ?: return
    val options = groqState.aiGeneratedQuestion?.options ?: groqState.currentQuestion?.options ?: return
    val correct = groqState.aiGeneratedQuestion?.correctIndex ?: groqState.currentQuestion?.correctIndex ?: 0

    sessionViewModel.recordEvent(
        SessionEvent(
            index          = 0,
            question       = groqState.aiGeneratedQuestion?.text ?: groqState.currentQuestion?.text ?: "",
            selectedAnswer = options.getOrElse(selectedIdx) { "" },
            correctAnswer  = options.getOrElse(correct) { "" },
            wasCorrect     = wasCorrect,
            timeMs         = System.currentTimeMillis() - startMs,
            hint           = groqState.aiHint,
            explanation    = groqState.aiGeneratedQuestion?.explanation ?: groqState.currentQuestion?.explanation ?: "",
            topic          = topic,
            language       = language,
            voiceUsed      = voiceUsed
        )
    )
}

private fun submitAnswer(
    idx: Int,
    options: List<String>,
    correctIndex: Int,
    explanation: String,
    questionText: String,
    topic: String,
    voiceUsed: Boolean,
    questionStartMs: Long,
    onCorrect: () -> Unit,
    onResult: (Boolean) -> Unit
) {
    val correct = idx == correctIndex
    if (correct) onCorrect()
    onResult(correct)
}
