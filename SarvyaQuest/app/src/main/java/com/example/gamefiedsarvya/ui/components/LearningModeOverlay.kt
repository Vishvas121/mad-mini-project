package com.example.gamefiedsarvya.ui.components

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.theme.*

/**
 * Learning Mode Overlay.
 *
 * Appears over the combat screen when a wrong answer is given.
 * Phases:
 *   TRIGGERED       → brief pause, entering learning mode
 *   EXPLANATION     → full explanation + mind map + example
 *   SIMPLIFIED      → even simpler explanation for repeated failures
 *   RETRY_QUESTION  → similar question to confirm understanding
 *   MASTERY_CELEBRATION → concept mastered animation
 */
@Composable
fun LearningModeOverlay(
    state: LearningModeState,
    onTryAgain: () -> Unit,
    onExplainSimpler: () -> Unit,
    onRetryAnswer: (Int) -> Unit,
    onContinueCombat: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isActive,
        enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(300)),
        exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
        ) {
            when (state.phase) {
                LearningModePhase.TRIGGERED -> TriggeredPhase()

                LearningModePhase.EXPLANATION,
                LearningModePhase.SIMPLIFIED -> ExplanationPhase(
                    state            = state,
                    onTryAgain       = onTryAgain,
                    onExplainSimpler = onExplainSimpler
                )

                LearningModePhase.RETRY_QUESTION -> RetryPhase(
                    state          = state,
                    onRetryAnswer  = onRetryAnswer
                )

                LearningModePhase.MASTERY_CELEBRATION -> MasteryCelebration(
                    state           = state,
                    onContinue      = onContinueCombat
                )

                else -> {}
            }
        }
    }
}

// ── Triggered phase — brief entry animation ───────────────────────────────────

@Composable
private fun TriggeredPhase() {
    val inf = rememberInfiniteTransition(label = "trigger")
    val pulse by inf.animateFloat(0.8f, 1f,
        infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse), label = "p")

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulse)
                    .background(NeonOrange.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, NeonOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("!", style = MaterialTheme.typography.displayLarge.copy(
                    color = NeonOrange, fontWeight = FontWeight.Black
                ))
            }
            Spacer(Modifier.height(16.dp))
            Text("Learning Mode",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = NeonOrange, fontWeight = FontWeight.Bold
                ))
            Spacer(Modifier.height(8.dp))
            Text("Let's understand this concept",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
        }
    }
}

// ── Explanation phase ─────────────────────────────────────────────────────────

@Composable
private fun ExplanationPhase(
    state: LearningModeState,
    onTryAgain: () -> Unit,
    onExplainSimpler: () -> Unit
) {
    val isSimplified = state.phase == LearningModePhase.SIMPLIFIED
    val accentColor  = if (isSimplified) NeonGold else NeonCyan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isSimplified) "S" else "L",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = accentColor, fontWeight = FontWeight.Bold
                    ))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    if (isSimplified) "Simplified Explanation" else "Learning Mode",
                    style = MaterialTheme.typography.titleLarge.copy(color = accentColor)
                )
                Text(
                    "Topic: ${state.triggerQuestion?.topic ?: ""}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Wrong answer indicator
        state.triggerQuestion?.let { q ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Question you got wrong:",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonRed))
                    Spacer(Modifier.height(4.dp))
                    Text(q.text,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    Spacer(Modifier.height(6.dp))
                    Text("Correct answer: ${q.correctAnswer}",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonGreen))
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Loading state
        if (state.isLoadingExplanation) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.height(10.dp))
                    Text("AI is preparing your explanation...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                }
            }
        } else {
            // Main explanation
            if (state.aiExplanation.isNotBlank()) {
                ExplanationCard(
                    title   = "Explanation",
                    content = state.aiExplanation,
                    color   = accentColor
                )
                Spacer(Modifier.height(10.dp))
            }

            // Example
            if (state.conceptExample.isNotBlank()) {
                ExplanationCard(
                    title   = "Example",
                    content = state.conceptExample,
                    color   = NeonPurple
                )
                Spacer(Modifier.height(10.dp))
            }

            // Mind map
            if (state.mindMapPoints.isNotEmpty()) {
                MindMapCard(points = state.mindMapPoints, color = NeonGold)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary: Try Again
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = !state.isLoadingExplanation) { onTryAgain() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Try Again",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF050508), fontWeight = FontWeight.Bold
                    ))
            }

            // Secondary: Explain Simpler (only if not already simplified)
            if (!isSimplified) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .background(NeonGold.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable { onExplainSimpler() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Explain Simpler",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonGold))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Retry question phase ──────────────────────────────────────────────────────

@Composable
private fun RetryPhase(
    state: LearningModeState,
    onRetryAnswer: (Int) -> Unit
) {
    val retryQ = state.retryQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Text("Now let's test your understanding",
                    style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen))
                Text("Answer this similar question to continue",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Retry attempt counter
        if (state.totalRetries > 1) {
            Text("Attempt ${state.totalRetries}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End)
            Spacer(Modifier.height(6.dp))
        }

        // Question
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .background(CardSurface, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                DifficultyBadge(
                    retryQ.difficulty.name,
                    when (retryQ.difficulty) {
                        Difficulty.EASY   -> NeonGreen
                        Difficulty.MEDIUM -> NeonOrange
                        Difficulty.HARD   -> NeonRed
                    }
                )
                Spacer(Modifier.height(10.dp))
                Text(retryQ.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Answer options
        retryQ.options.forEachIndexed { idx, opt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .background(NeonCyan.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                    .clickable { onRetryAnswer(idx) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${('A' + idx)}. ",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonCyan),
                        modifier = Modifier.width(28.dp))
                    Text(opt, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Mastery celebration ───────────────────────────────────────────────────────

@Composable
private fun MasteryCelebration(
    state: LearningModeState,
    onContinue: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "mastery")
    val scale by inf.animateFloat(1f, 1.08f,
        infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse), label = "s")

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(NeonGreen.copy(alpha = 0.2f), CircleShape)
                    .border(3.dp, NeonGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("OK",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = NeonGreen, fontWeight = FontWeight.Black
                    ))
            }

            Spacer(Modifier.height(20.dp))

            Text("Concept Understood!",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = NeonGreen, fontWeight = FontWeight.Black
                ))

            Spacer(Modifier.height(8.dp))

            Text("You got it right after learning.",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))

            if (state.xpEarnedFromLearning > 0) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(NeonGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("+${state.xpEarnedFromLearning} Learning XP",
                        style = MaterialTheme.typography.titleLarge.copy(color = NeonGold))
                }
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonGreen, RoundedCornerShape(10.dp))
                    .clickable { onContinue() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Continue Battle",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF050508), fontWeight = FontWeight.Bold
                    ))
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun ExplanationCard(title: String, content: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(title,
            style = MaterialTheme.typography.labelLarge.copy(color = color))
        Spacer(Modifier.height(6.dp))
        Text(content,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp))
    }
}

@Composable
private fun MindMapCard(points: List<String>, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text("Key Points",
            style = MaterialTheme.typography.labelLarge.copy(color = color))
        Spacer(Modifier.height(8.dp))
        points.forEach { point ->
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .offset(y = 6.dp)
                        .background(color, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(point,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ── Concept Mastered Banner (shown in combat) ─────────────────────────────────

@Composable
fun ConceptMasteredBanner(
    conceptName: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit    = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(NeonGreen.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, NeonGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", style = MaterialTheme.typography.labelLarge.copy(
                        color = NeonGreen, fontWeight = FontWeight.Bold
                    ))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Concept Mastered",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonGreen))
                    Text(conceptName,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                }
                Spacer(Modifier.weight(1f))
                Text("+30 XP",
                    style = MaterialTheme.typography.labelLarge.copy(color = NeonGold))
            }
        }
    }
}


// ── Learning Streak Banner ────────────────────────────────────────────────────

@Composable
fun LearningStreakBanner(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    if (streakCount < 2) return
    val inf = rememberInfiniteTransition(label = "streak")
    val alpha by inf.animateFloat(0.7f, 1f,
        infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse), label = "a")

    Box(
        modifier = modifier
            .background(NeonOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, NeonOrange.copy(alpha = alpha * 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(NeonOrange.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$streakCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonOrange, fontWeight = FontWeight.Bold
                    ))
            }
            Spacer(Modifier.width(6.dp))
            Text("Learning Streak",
                style = MaterialTheme.typography.labelSmall.copy(color = NeonOrange))
        }
    }
}
