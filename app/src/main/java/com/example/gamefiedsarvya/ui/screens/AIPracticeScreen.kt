package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.QuestionRepository
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.*
import com.example.gamefiedsarvya.focus.FocusMonitor
import com.example.gamefiedsarvya.focus.FocusIndicator
import com.example.gamefiedsarvya.focus.FocusAlertBanner

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
    val uiState      by gameViewModel.uiState.collectAsState()
    val hubState     by hubViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val groqState    by groqViewModel.state.collectAsState()
    val sessionState by sessionViewModel.state.collectAsState()
    val voiceState   by voiceViewModel.state.collectAsState()
    val theme        = TierThemes.forTier(hubState.selectedTier)

    val topics          = remember { QuestionRepository.getAllTopics() }
    var selectedTopic   by remember { mutableStateOf(topics.firstOrNull() ?: "Technology") }
    var showFeedback    by remember { mutableStateOf(false) }
    var lastCorrect     by remember { mutableStateOf<Boolean?>(null) }
    var totalAnswered   by remember { mutableIntStateOf(0) }
    var totalCorrect    by remember { mutableIntStateOf(0) }
    var hintVisible     by remember { mutableStateOf(false) }
    var questionStartMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var studyMode       by remember { mutableStateOf(false) }
    var studyExpanded   by remember { mutableStateOf(false) }
    val sessionMastery  = remember { mutableStateMapOf<String, Pair<Int,Int>>() }

    val playerName  = profileState.profile.displayName
    val isVoiceMode = voiceState.settings.sttEnabled || voiceState.settings.ttsEnabled

    LaunchedEffect(Unit) {
        sessionViewModel.startSession(
            userName  = playerName,
            tier      = hubState.selectedTier,
            topic     = selectedTopic,
            language  = voiceState.settings.language,
            voiceMode = isVoiceMode
        )
    }

    LaunchedEffect(selectedTopic, hubState.selectedTier) {
        showFeedback = false; hintVisible = false; studyExpanded = false
        questionStartMs = System.currentTimeMillis()
        groqViewModel.clearFeedback()
        groqViewModel.generateQuestion(
            topic      = selectedTopic,
            twin       = progress.digitalTwin,
            tier       = hubState.selectedTier,
            playerName = playerName,
            zoneId     = "zone_forest"
        )
        if (studyMode) groqViewModel.requestTopicSummary(selectedTopic, hubState.selectedTier, playerName)
    }

    LaunchedEffect(groqState.aiGeneratedQuestion, groqState.currentQuestion) {
        val qText = groqState.aiGeneratedQuestion?.text ?: groqState.currentQuestion?.text
        if (qText != null) voiceViewModel.speakIfAuto(qText)
    }

    LaunchedEffect(voiceState.recognisedText) {
        val text = voiceState.recognisedText.trim().lowercase()
        if (text.isBlank() || showFeedback) return@LaunchedEffect
        val options = groqState.aiGeneratedQuestion?.options
            ?: groqState.currentQuestion?.options ?: return@LaunchedEffect
        val correctIndex = groqState.aiGeneratedQuestion?.correctIndex
            ?: groqState.currentQuestion?.correctIndex ?: 0
        val matchedIdx = when {
            text.startsWith("a") || text == "option a" -> 0
            text.startsWith("b") || text == "option b" -> 1
            text.startsWith("c") || text == "option c" -> 2
            text.startsWith("d") || text == "option d" -> 3
            else -> options.indexOfFirst { it.lowercase().contains(text) }
        }
        if (matchedIdx >= 0) {
            val correct = matchedIdx == correctIndex
            val explanation = groqState.aiGeneratedQuestion?.explanation
                ?: groqState.currentQuestion?.explanation ?: ""
            lastCorrect = correct; showFeedback = true; totalAnswered++
            if (correct) { totalCorrect++; gameViewModel.awardXpFromPractice(10) }
            val prev = sessionMastery[selectedTopic] ?: (0 to 0)
            sessionMastery[selectedTopic] = (prev.first + if (correct) 1 else 0) to (prev.second + 1)
            recordPracticeEvent(sessionViewModel, groqState, correct, matchedIdx,
                selectedTopic, voiceState.settings.language, true, questionStartMs)
            voiceViewModel.speakIfAuto(if (correct) "Correct! $explanation" else "Incorrect. $explanation")
            groqViewModel.requestFeedback(playerName, correct, selectedTopic,
                explanation, hubState.selectedTier, progress.player.streakCount)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(theme.background)) {
        // ── Focus detection wrapper ───────────────────────────────────────────
        FocusMonitor(enabled = true) { focusVm ->
            val focusState by focusVm.state.collectAsState()

            // Feed real focus signals into DigitalTwin on every frame
            LaunchedEffect(focusState.smoothFocus, focusState.smoothEngagement) {
                if (focusState.isActive) {
                    val updatedTwin = com.example.gamefiedsarvya.engine.AdaptiveEngine.updateFocusSignals(
                        twin            = progress.digitalTwin,
                        focusScore      = focusState.smoothFocus,
                        engagementScore = focusState.smoothEngagement
                    )
                    gameViewModel.updateDigitalTwin(updatedTwin)
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            PracticeHeader(
                theme         = theme,
                totalCorrect  = totalCorrect,
                totalAnswered = totalAnswered,
                isVoiceMode   = isVoiceMode,
                studyMode     = studyMode,
                onStudyToggle = {
                    studyMode = !studyMode
                    if (studyMode && groqState.aiTopicSummary.isBlank())
                        groqViewModel.requestTopicSummary(selectedTopic, hubState.selectedTier, playerName)
                },
                onBack = {
                    val xp = totalCorrect * 10
                    sessionViewModel.endSession(xpEarned = xp)
                    gameViewModel.awardXpFromPractice(xp)
                    sessionViewModel.syncProfile(playerName, hubState.selectedTier,
                        progress.player.level, progress.player.xp, progress.player.accuracy)
                    onBack()
                }
            )

            Spacer(Modifier.height(6.dp))

            // ── Focus indicator row ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusIndicator(focusVm = focusVm)
            }

            Spacer(Modifier.height(4.dp))

            // ── Focus alert banner ────────────────────────────────────────────
            FocusAlertBanner(focusVm = focusVm, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(4.dp))

            VoiceStatusBanner(
                voiceState     = voiceState.voiceState,
                recognisedText = voiceState.recognisedText,
                modifier       = Modifier.fillMaxWidth()
            )
            BadgeToast(badge = sessionState.showBadgeToast, onDismiss = { sessionViewModel.dismissBadgeToast() })

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingGlowDot(if (groqState.useAiQuestion) NeonGreen else NeonOrange)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (groqState.useAiQuestion) "AI-generated • ${voiceState.settings.language.uppercase()}"
                    else "Offline bank",
                    style = theme.labelStyle.copy(color = if (groqState.useAiQuestion) NeonGreen else NeonOrange)
                )
            }

            Spacer(Modifier.height(10.dp))

            PracticeTopicSelector(
                topics        = topics,
                selectedTopic = selectedTopic,
                mastery       = sessionMastery,
                theme         = theme,
                onSelect      = { topic ->
                    selectedTopic = topic
                    showFeedback = false; hintVisible = false
                    groqViewModel.clearFeedback()
                    questionStartMs = System.currentTimeMillis()
                }
            )

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(visible = studyMode) {
                PracticeConceptCard(
                    topic     = selectedTopic,
                    summary   = groqState.aiTopicSummary,
                    isLoading = groqState.isLoadingSummary,
                    expanded  = studyExpanded,
                    theme     = theme,
                    onToggle  = { studyExpanded = !studyExpanded }
                )
            }
            if (studyMode) Spacer(Modifier.height(12.dp))

            if (groqState.isLoadingQuestion) {
                PracticeLoadingCard(theme = theme)
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
                val difficulty = progress.digitalTwin.preferredDifficulty

                TopicVideoCard(topic = selectedTopic, questionText = questionText, theme = theme)
                Spacer(Modifier.height(8.dp))

                PracticeQuestionCard(
                    topic         = selectedTopic,
                    questionText  = questionText,
                    difficulty    = difficulty,
                    theme         = theme,
                    isSpeaking    = voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.SPEAKING,
                    ttsEnabled    = voiceState.settings.ttsEnabled,
                    onSpeak       = { voiceViewModel.speak(it) },
                    onStop        = { voiceViewModel.stopSpeaking() },
                    hintVisible   = hintVisible,
                    hint          = groqState.aiHint.ifBlank { fallbackHint },
                    isLoadingHint = groqState.isLoadingHint
                )

                Spacer(Modifier.height(8.dp))

                if (!showFeedback) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!hintVisible) {
                            NeonButton("💡 AI Hint", onClick = {
                                hintVisible = true
                                groqViewModel.requestHint(questionText, selectedTopic,
                                    hubState.selectedTier, playerName, fallbackHint)
                            }, color = NeonGold)
                        } else { Spacer(Modifier.width(1.dp)) }
                        if (voiceState.settings.sttEnabled) {
                            MicButton(
                                voiceState = voiceState.voiceState,
                                enabled    = !showFeedback,
                                onTap      = {
                                    if (voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.LISTENING)
                                        voiceViewModel.stopListening()
                                    else voiceViewModel.startListening(context) {}
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                PracticeAnswerOptions(
                    options      = options,
                    correctIndex = correctIndex,
                    showFeedback = showFeedback,
                    theme        = theme,
                    onSelect     = { idx ->
                        val correct = idx == correctIndex
                        lastCorrect = correct; showFeedback = true; totalAnswered++
                        if (correct) { totalCorrect++; gameViewModel.awardXpFromPractice(10) }
                        val prev = sessionMastery[selectedTopic] ?: (0 to 0)
                        sessionMastery[selectedTopic] =
                            (prev.first + if (correct) 1 else 0) to (prev.second + 1)
                        recordPracticeEvent(sessionViewModel, groqState, correct, idx,
                            selectedTopic, voiceState.settings.language, false, questionStartMs)
                        voiceViewModel.speakIfAuto(if (correct) "Correct! $explanation" else "Incorrect. $explanation")
                        groqViewModel.requestFeedback(playerName, correct, selectedTopic,
                            explanation, hubState.selectedTier, progress.player.streakCount)
                    }
                )

                AnimatedVisibility(visible = showFeedback, enter = expandVertically() + fadeIn(tween(300))) {
                    PracticeFeedbackPanel(
                        wasCorrect  = lastCorrect == true,
                        explanation = explanation,
                        aiFeedback  = groqState.aiFeedback,
                        isLoadingFb = groqState.isLoadingFeedback,
                        topic       = selectedTopic,
                        theme       = theme,
                        isSpeaking  = voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.SPEAKING,
                        ttsEnabled  = voiceState.settings.ttsEnabled,
                        onSpeak     = { voiceViewModel.speak(it) },
                        onStop      = { voiceViewModel.stopSpeaking() },
                        onNext      = {
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
                        }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        if (uiState.justLevelledUp) {
            LevelUpBanner(level = progress.player.level, onDismiss = { gameViewModel.clearLevelUpFlag() })
        }
        } // end FocusMonitor
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun PracticeHeader(
    theme: TierTheme, totalCorrect: Int, totalAnswered: Int,
    isVoiceMode: Boolean, studyMode: Boolean,
    onStudyToggle: () -> Unit, onBack: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        NeonButton("← Back", onClick = onBack, color = TextSecondary)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AI PRACTICE", style = theme.titleStyle.copy(color = theme.primary))
            Text("Powered by Groq", style = theme.labelStyle.copy(color = TextMuted))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$totalCorrect/$totalAnswered",
                style = theme.labelStyle.copy(color = theme.accent, fontWeight = FontWeight.Bold))
            Box(modifier = Modifier
                .border(1.dp, if (studyMode) NeonCyan else CardBorder, RoundedCornerShape(6.dp))
                .background(if (studyMode) NeonCyan.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(6.dp))
                .clickable { onStudyToggle() }
                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(if (studyMode) "📖 Study ON" else "📖 Study",
                    style = theme.labelStyle.copy(color = if (studyMode) NeonCyan else TextSecondary, fontSize = 10.sp))
            }
            if (isVoiceMode) Text("🎙 Voice", style = theme.labelStyle.copy(color = NeonGreen, fontSize = 10.sp))
        }
    }
}

// ── Topic selector ────────────────────────────────────────────────────────────

@Composable
private fun PracticeTopicSelector(
    topics: List<String>, selectedTopic: String,
    mastery: Map<String, Pair<Int, Int>>, theme: TierTheme,
    onSelect: (String) -> Unit
) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        topics.forEach { topic ->
            val isSelected = selectedTopic == topic
            val (correct, total) = mastery[topic] ?: (0 to 0)
            val frac = if (total > 0) correct.toFloat() / total else 0f
            val mColor = when { frac >= 0.8f -> NeonGreen; frac >= 0.5f -> NeonOrange; total > 0 -> NeonRed; else -> CardBorder }
            Column(modifier = Modifier
                .border(1.dp, if (isSelected) theme.primary else CardBorder, RoundedCornerShape(12.dp))
                .background(if (isSelected) theme.primary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(12.dp))
                .clickable { onSelect(topic) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(practiceTopicIcon(topic), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(2.dp))
                Text(topic, style = theme.labelStyle.copy(color = if (isSelected) theme.primary else TextSecondary, fontSize = 10.sp))
                if (total > 0) {
                    Spacer(Modifier.height(4.dp))
                    val animFrac by animateFloatAsState(frac, tween(600), label = "mf")
                    Box(modifier = Modifier.width(48.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(CardBorder)) {
                        Box(modifier = Modifier.fillMaxWidth(animFrac).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(mColor))
                    }
                }
            }
        }
    }
}

// ── Concept card ──────────────────────────────────────────────────────────────

@Composable
private fun PracticeConceptCard(
    topic: String, summary: String, isLoading: Boolean,
    expanded: Boolean, theme: TierTheme, onToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()
        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        .background(Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent)), RoundedCornerShape(14.dp))
        .padding(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() },
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp)
                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, NeonCyan.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    Text(practiceTopicIcon(topic), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Concept Overview", style = theme.labelStyle.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
                    Text(topic, style = theme.labelStyle.copy(color = TextSecondary, fontSize = 10.sp))
                }
            }
            Text(if (expanded) "▲ Collapse" else "▼ Expand", style = theme.labelStyle.copy(color = NeonCyan, fontSize = 10.sp))
        }
        Spacer(Modifier.height(10.dp))
        PracticeMindMap(topic = topic, theme = theme)
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = NeonCyan.copy(alpha = 0.2f))
                Spacer(Modifier.height(10.dp))
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("AI preparing concept summary…", style = theme.bodyStyle.copy(color = TextSecondary))
                    }
                } else if (summary.isNotBlank()) {
                    summary.lines().filter { it.isNotBlank() }.forEach { line ->
                        val isBullet = line.trimStart().startsWith("-") || line.trimStart().startsWith("•") || line.trimStart().startsWith("*")
                        if (isBullet) {
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(6.dp).offset(y = 7.dp).background(NeonCyan, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(line.trimStart('-', '•', '*', ' '), style = theme.bodyStyle.copy(color = TextPrimary))
                            }
                        } else {
                            Text(line, style = theme.bodyStyle.copy(
                                color = if (line.endsWith(":")) NeonCyan else TextPrimary,
                                fontWeight = if (line.endsWith(":")) FontWeight.SemiBold else FontWeight.Normal),
                                modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                } else {
                    practiceOfflineSummary(topic).forEach { point ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 3.dp)) {
                            Box(modifier = Modifier.size(6.dp).offset(y = 7.dp).background(NeonCyan.copy(alpha = 0.7f), CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(point, style = theme.bodyStyle.copy(color = TextPrimary))
                        }
                    }
                }
            }
        }
    }
}

// ── Mind map canvas ───────────────────────────────────────────────────────────

@Composable
private fun PracticeMindMap(topic: String, theme: TierTheme) {
    val keyPoints = practiceTopicKeyPoints(topic)
    Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val cx = size.width / 2f; val cy = size.height / 2f; val r = 28f
        drawCircle(theme.primary.copy(alpha = 0.18f), r + 6f, Offset(cx, cy))
        drawCircle(theme.primary, r, Offset(cx, cy), style = Stroke(2f))
        val count = keyPoints.size.coerceAtMost(5)
        val radius = size.width * 0.36f
        for (i in 0 until count) {
            val angle = (Math.PI * 2 * i / count - Math.PI / 2).toFloat()
            val nx = cx + radius * kotlin.math.cos(angle)
            val ny = cy + radius * kotlin.math.sin(angle)
            drawLine(theme.primary.copy(alpha = 0.35f), Offset(cx, cy), Offset(nx, ny), 1.5f)
            drawCircle(theme.primary.copy(alpha = 0.12f), 22f, Offset(nx, ny))
            drawCircle(theme.primary.copy(alpha = 0.6f), 22f, Offset(nx, ny), style = Stroke(1.5f))
        }
    }
    val kp5 = keyPoints.take(5)
    if (kp5.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            kp5.forEach { kp ->
                Text(kp, style = MaterialTheme.typography.labelSmall.copy(
                    color = theme.primary.copy(alpha = 0.8f), fontSize = 9.sp, textAlign = TextAlign.Center),
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

// ── Question card ─────────────────────────────────────────────────────────────

@Composable
private fun PracticeQuestionCard(
    topic: String, questionText: String, difficulty: Difficulty, theme: TierTheme,
    isSpeaking: Boolean, ttsEnabled: Boolean, onSpeak: (String) -> Unit, onStop: () -> Unit,
    hintVisible: Boolean, hint: String, isLoadingHint: Boolean
) {
    val diffColor = when (difficulty) { Difficulty.EASY -> NeonGreen; Difficulty.MEDIUM -> NeonOrange; Difficulty.HARD -> NeonRed }
    val isCode = questionText.contains("O(") || questionText.contains("->") ||
                 questionText.contains("==") || questionText.contains("def ") ||
                 questionText.contains("int ") || questionText.contains("class ")
    Column(modifier = Modifier.fillMaxWidth()
        .border(1.dp, theme.primary.copy(alpha = 0.45f), RoundedCornerShape(theme.cardRadius.toInt().dp))
        .background(theme.surface, RoundedCornerShape(theme.cardRadius.toInt().dp))
        .padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(theme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("${practiceTopicIcon(topic)} $topic", style = theme.labelStyle.copy(color = theme.primary, fontSize = 10.sp))
                }
                DifficultyBadge(difficulty.name, diffColor)
            }
            SpeakButton(text = questionText, isSpeaking = isSpeaking, enabled = ttsEnabled, onSpeak = onSpeak, onStop = onStop)
        }
        Spacer(Modifier.height(12.dp))
        if (isCode) {
            Box(modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)) {
                Text(questionText, style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace, color = Color(0xFFE6EDF3), lineHeight = 22.sp))
            }
        } else {
            Text(questionText, style = theme.bodyStyle.copy(fontWeight = FontWeight.SemiBold, lineHeight = 22.sp, color = TextPrimary))
        }
        Spacer(Modifier.height(10.dp))
        QuestionVisualAid(questionText = questionText, topic = topic, theme = theme)
        AnimatedVisibility(visible = hintVisible) {
            Column {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = NeonGold.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                if (isLoadingHint) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = NeonGold, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("AI thinking…", style = theme.labelStyle.copy(color = NeonGold))
                    }
                } else if (hint.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()
                        .background(NeonGold.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                        .padding(10.dp), verticalAlignment = Alignment.Top) {
                        Text("💡", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Hint", style = theme.labelStyle.copy(color = NeonGold, fontSize = 10.sp))
                            Text(hint, style = theme.bodyStyle.copy(color = NeonGold))
                        }
                    }
                }
            }
        }
    }
}

// ── Answer options ────────────────────────────────────────────────────────────

@Composable
private fun PracticeAnswerOptions(
    options: List<String>, correctIndex: Int, showFeedback: Boolean,
    theme: TierTheme, onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEachIndexed { idx, opt ->
            val isCorrect = idx == correctIndex
            val optColor = when { !showFeedback -> theme.primary; isCorrect -> NeonGreen; else -> NeonRed.copy(alpha = 0.6f) }
            val bgAlpha  = when { !showFeedback -> 0.05f; isCorrect -> 0.18f; else -> 0.04f }
            val scale by animateFloatAsState(if (showFeedback && isCorrect) 1.02f else 1f,
                spring(Spring.DampingRatioMediumBouncy), label = "os$idx")
            Box(modifier = Modifier.fillMaxWidth().scale(scale)
                .border(if (showFeedback && isCorrect) 2.dp else 1.dp,
                    optColor.copy(alpha = if (showFeedback && isCorrect) 0.9f else 0.45f), RoundedCornerShape(10.dp))
                .background(optColor.copy(alpha = bgAlpha), RoundedCornerShape(10.dp))
                .clickable(enabled = !showFeedback) { onSelect(idx) }
                .padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(28.dp)
                        .background(optColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, optColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text("${('A' + idx)}", style = theme.labelStyle.copy(color = optColor, fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(opt, style = theme.bodyStyle.copy(
                        color = if (showFeedback && isCorrect) TextPrimary else if (showFeedback) TextSecondary else TextPrimary),
                        modifier = Modifier.weight(1f))
                    if (showFeedback && isCorrect) {
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.background(NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("✓ Correct", style = theme.labelStyle.copy(color = NeonGreen))
                        }
                    }
                }
            }
        }
    }
}

// ── Feedback panel ────────────────────────────────────────────────────────────

@Composable
private fun PracticeFeedbackPanel(
    wasCorrect: Boolean, explanation: String, aiFeedback: String, isLoadingFb: Boolean,
    topic: String, theme: TierTheme, isSpeaking: Boolean, ttsEnabled: Boolean,
    onSpeak: (String) -> Unit, onStop: () -> Unit, onNext: () -> Unit
) {
    val resultColor = if (wasCorrect) NeonGreen else NeonOrange
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Box(modifier = Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(resultColor.copy(alpha = 0.15f), Color.Transparent)), RoundedCornerShape(10.dp))
            .border(1.dp, resultColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (wasCorrect) "✓" else "✗", style = MaterialTheme.typography.headlineMedium.copy(color = resultColor))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(if (wasCorrect) "Correct!" else "Not quite", style = theme.titleStyle.copy(color = resultColor))
                        if (isLoadingFb) Text("AI personalising…", style = theme.labelStyle.copy(color = TextMuted, fontSize = 10.sp))
                        else if (aiFeedback.isNotBlank()) Text(aiFeedback, style = theme.bodyStyle.copy(color = TextSecondary))
                    }
                }
                if (aiFeedback.isNotBlank()) SpeakButton(text = aiFeedback, isSpeaking = isSpeaking, enabled = ttsEnabled, onSpeak = onSpeak, onStop = onStop)
            }
        }
        if (explanation.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()
                .border(1.dp, NeonGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .background(NeonGold.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp)
                        .background(NeonGold.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, NeonGold.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center) { Text("📚", style = MaterialTheme.typography.labelMedium) }
                    Spacer(Modifier.width(8.dp))
                    Text("Why this answer?", style = theme.labelStyle.copy(color = NeonGold, fontWeight = FontWeight.Bold))
                }
                Text(explanation, style = theme.bodyStyle.copy(color = TextPrimary, lineHeight = 22.sp))
                HorizontalDivider(color = NeonGold.copy(alpha = 0.15f))
                Row(modifier = Modifier.fillMaxWidth()
                    .background(NeonGold.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(10.dp), verticalAlignment = Alignment.Top) {
                    Text("🔑", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text("Key Takeaway", style = theme.labelStyle.copy(color = NeonGold, fontSize = 10.sp))
                        val sentences = explanation.split(". ").filter { it.length > 20 }
                        Text(sentences.lastOrNull()?.trimEnd('.') ?: "Master the core concept of $topic.",
                            style = theme.bodyStyle.copy(color = NeonGold))
                    }
                }
                val analogy = practiceTopicAnalogy(topic)
                if (analogy.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()
                        .background(NeonPurple.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonPurple.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(10.dp), verticalAlignment = Alignment.Top) {
                        Text("🧠", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Think of it like…", style = theme.labelStyle.copy(color = NeonPurple, fontSize = 10.sp))
                            Text(analogy, style = theme.bodyStyle.copy(color = NeonPurple.copy(alpha = 0.9f)))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().background(theme.primary, RoundedCornerShape(10.dp))
            .clickable { onNext() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Text("Next Question →", style = theme.titleStyle.copy(color = Color(0xFF050508), fontWeight = FontWeight.Bold))
        }
    }
}

// ── Loading card ──────────────────────────────────────────────────────────────

@Composable
private fun PracticeLoadingCard(theme: TierTheme) {
    val inf = rememberInfiniteTransition(label = "ql")
    val alpha by inf.animateFloat(0.3f, 1f, infiniteRepeatable(tween(700, easing = EaseInOutSine), RepeatMode.Reverse), label = "qa")
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)
        .border(1.dp, theme.primary.copy(alpha = alpha * 0.4f), RoundedCornerShape(theme.cardRadius.toInt().dp))
        .background(theme.surface, RoundedCornerShape(theme.cardRadius.toInt().dp)),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = theme.primary, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text("AI crafting your question…", style = theme.bodyStyle.copy(color = TextSecondary))
            Spacer(Modifier.height(4.dp))
            Text("Adapting to your level", style = theme.labelStyle.copy(color = TextMuted, fontSize = 10.sp))
        }
    }
}

// ── Topic metadata helpers ────────────────────────────────────────────────────

private fun practiceTopicIcon(topic: String): String = when (topic.lowercase()) {
    "chemistry" -> "⚗️"; "physics" -> "⚡"; "math", "mathematics" -> "📐"
    "biology" -> "🧬"; "geography" -> "🌍"; "history" -> "📜"; "science" -> "🔬"
    "technology" -> "💻"; "english" -> "📝"; "economics" -> "📊"
    "computer science" -> "🖥️"; "algorithms" -> "🔄"; "data structures" -> "🗂️"
    else -> "📚"
}

private fun practiceTopicKeyPoints(topic: String): List<String> = when (topic.lowercase()) {
    "chemistry" -> listOf("Atoms", "Bonds", "Reactions", "Periodic Table", "Moles")
    "physics" -> listOf("Forces", "Energy", "Waves", "Electricity", "Motion")
    "math", "mathematics" -> listOf("Algebra", "Geometry", "Calculus", "Statistics", "Proofs")
    "biology" -> listOf("Cells", "DNA", "Evolution", "Ecosystems", "Genetics")
    "algorithms" -> listOf("Sorting", "Searching", "Complexity", "Recursion", "Graphs")
    "data structures" -> listOf("Arrays", "Trees", "Graphs", "Hashing", "Queues")
    else -> listOf("Concepts", "Principles", "Examples", "Applications", "Review")
}

private fun practiceOfflineSummary(topic: String): List<String> = when (topic.lowercase()) {
    "chemistry" -> listOf("Matter is made of atoms.", "Chemical bonds hold atoms together.", "Reactions conserve mass.", "The periodic table organises elements.", "Moles link mass to quantity.")
    "physics" -> listOf("Newton's laws describe motion.", "Energy is conserved.", "Waves transfer energy.", "Electricity is charge flow.", "Kinematics links displacement, velocity, acceleration.")
    "algorithms" -> listOf("Big-O measures runtime growth.", "O(n log n) is best average sort.", "Recursion breaks problems down.", "DP avoids redundant computation.", "BFS/DFS explore graphs.")
    else -> listOf("Review core definitions for $topic.", "Understand key principles.", "Practice with examples.", "Look for patterns.", "Test at increasing difficulty.")
}

private fun practiceTopicAnalogy(topic: String): String = when (topic.lowercase()) {
    "chemistry" -> "Reactions are like recipes — combine ingredients in the right amounts to get a product."
    "physics" -> "Forces are invisible hands — they push or pull objects, changing speed or direction."
    "math", "mathematics" -> "Algebra is a balance scale — whatever you do to one side, do to the other."
    "biology" -> "DNA is a recipe book — instructions to build and run a living organism."
    "algorithms" -> "Sorting is organising a bookshelf — different strategies have different speeds."
    "technology" -> "A CPU is like a brain — it processes instructions; RAM is short-term memory."
    else -> ""
}

// ── Session event helper ──────────────────────────────────────────────────────

private fun recordPracticeEvent(
    sessionViewModel: SessionViewModel, groqState: GroqAdaptiveState,
    wasCorrect: Boolean, selectedIdx: Int, topic: String,
    language: String, voiceUsed: Boolean, startMs: Long
) {
    val options = groqState.aiGeneratedQuestion?.options ?: groqState.currentQuestion?.options ?: return
    val correct = groqState.aiGeneratedQuestion?.correctIndex ?: groqState.currentQuestion?.correctIndex ?: 0
    sessionViewModel.recordEvent(SessionEvent(
        index          = 0,
        question       = groqState.aiGeneratedQuestion?.text ?: groqState.currentQuestion?.text ?: "",
        selectedAnswer = options.getOrElse(selectedIdx) { "" },
        correctAnswer  = options.getOrElse(correct) { "" },
        wasCorrect     = wasCorrect,
        timeMs         = System.currentTimeMillis() - startMs,
        hint           = groqState.aiHint,
        explanation    = groqState.aiGeneratedQuestion?.explanation ?: groqState.currentQuestion?.explanation ?: "",
        topic          = topic, language = language, voiceUsed = voiceUsed
    ))
}
