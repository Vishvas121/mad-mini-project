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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.SessionViewModel
import com.example.gamefiedsarvya.viewmodel.VoiceViewModel

@Composable
fun SessionReplayScreen(
    sessionViewModel: SessionViewModel,
    voiceViewModel: VoiceViewModel,
    onBack: () -> Unit
) {
    val state      by sessionViewModel.state.collectAsState()
    val voiceState by voiceViewModel.state.collectAsState()

    val session = state.replaySession
    if (session == null) {
        Box(Modifier.fillMaxSize().background(DeepVoid), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No session to replay", style = MaterialTheme.typography.titleLarge.copy(color = TextMuted))
                Spacer(Modifier.height(16.dp))
                NeonButton("Back", onClick = onBack, color = TextSecondary)
            }
        }
        return
    }

    val currentEvent = session.events.getOrNull(state.replayIndex)
    val progress = if (session.events.isEmpty()) 0f
                   else (state.replayIndex + 1).toFloat() / session.events.size

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = {
                    sessionViewModel.startReplay(session.copy()) // reset
                    onBack()
                }, color = TextSecondary)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPLAY", style = MaterialTheme.typography.headlineMedium.copy(color = NeonCyan))
                    Text("${session.userName} • ${session.topic}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                }
                SpeakButton(
                    text      = currentEvent?.question ?: "",
                    isSpeaking = voiceState.voiceState == com.example.gamefiedsarvya.voice.VoiceState.SPEAKING,
                    enabled   = voiceState.settings.ttsEnabled,
                    onSpeak   = { voiceViewModel.speak(it) },
                    onStop    = { voiceViewModel.stopSpeaking() }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Step ${state.replayIndex + 1} of ${session.events.size}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    Text("${session.accuracyPct}% accuracy",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonGreen))
                }
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(6.dp)
                    .background(CardBorder, RoundedCornerShape(3.dp))
                ) {
                    val animProg by animateFloatAsState(progress, tween(400), label = "prog")
                    Box(modifier = Modifier.fillMaxWidth(animProg).fillMaxHeight()
                        .background(NeonCyan, RoundedCornerShape(3.dp)))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Session summary card
            GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan.copy(alpha = 0.3f)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ReplayStat("Questions", "${session.totalQuestions}", NeonCyan)
                    ReplayStat("Correct",   "${session.correctAnswers}", NeonGreen)
                    ReplayStat("XP",        "+${session.xpEarned}",     XpGold)
                    ReplayStat("Language",  session.language.uppercase(), NeonPurple)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Current event
            AnimatedContent(
                targetState = currentEvent,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "replay_event"
            ) { event ->
                if (event != null) {
                    ReplayEventCard(event = event)
                } else {
                    // Session complete
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉 Session Complete!",
                                style = MaterialTheme.typography.displayMedium.copy(color = NeonGold))
                            Spacer(Modifier.height(8.dp))
                            Text("Final score: ${session.accuracyPct}%",
                                style = MaterialTheme.typography.headlineMedium.copy(color = NeonGreen))
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.replayIndex > 0) {
                    NeonButton("← Prev", onClick = {
                        // Go back one step
                        sessionViewModel.startReplay(session)
                        repeat(state.replayIndex - 1) { sessionViewModel.replayNext() }
                    }, modifier = Modifier.weight(1f), color = TextSecondary)
                }
                NeonButton(
                    text = if (currentEvent != null) "Next →" else "Finish",
                    onClick = {
                        if (currentEvent != null) {
                            voiceViewModel.speakIfAuto(
                                "${if (currentEvent.wasCorrect) "Correct!" else "Incorrect."} ${currentEvent.explanation}"
                            )
                            sessionViewModel.replayNext()
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
private fun ReplayEventCard(event: SessionEvent) {
    val color = if (event.wasCorrect) NeonGreen else NeonRed

    GameCard(modifier = Modifier.fillMaxWidth(), borderColor = color.copy(alpha = 0.4f)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Result badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (event.wasCorrect) "✓ CORRECT" else "✗ INCORRECT",
                    style = MaterialTheme.typography.labelLarge.copy(color = color))
                Spacer(Modifier.weight(1f))
                Text("${event.timeMs / 1000}s",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                if (event.voiceUsed) {
                    Spacer(Modifier.width(8.dp))
                    Text("🎙", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Question
            Text(event.question,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))

            Spacer(Modifier.height(8.dp))

            // Answers
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnswerChip("Selected: ${event.selectedAnswer}",
                    if (event.wasCorrect) NeonGreen else NeonRed)
                if (!event.wasCorrect) {
                    AnswerChip("Correct: ${event.correctAnswer}", NeonGreen)
                }
            }

            // Explanation
            if (event.explanation.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonGold.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                ) {
                    Text("📖 ${event.explanation}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = NeonGold))
                }
            }
        }
    }
}

@Composable
private fun AnswerChip(text: String, color: Color) {
    Box(modifier = Modifier
        .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
        .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

@Composable
private fun ReplayStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
    }
}
