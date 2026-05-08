package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.engine.AdaptiveEngine
import com.example.gamefiedsarvya.ui.assets.NeonXpBar
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel

@Composable
fun AdaptiveDashboardScreen(
    gameViewModel: GameViewModel,
    hubViewModel:  LearningHubViewModel,
    onBack:        () -> Unit,
    onOpenHub:     () -> Unit,
    onOpenMap:     () -> Unit
) {
    val progress by gameViewModel.progress.collectAsState()
    val hubState by hubViewModel.uiState.collectAsState()
    val twin     = progress.digitalTwin
    val tier     = hubState.selectedTier
    val theme    = TierThemes.forTier(tier)

    val recommendations = remember(twin, tier) { hubViewModel.getRecommendedMaterials() }

    Box(modifier = Modifier.fillMaxSize().background(theme.background)) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 60f
            var x = 0f; while (x <= size.width)  { drawLine(theme.primary.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height)); x += step }
            var y = 0f; while (y <= size.height) { drawLine(theme.primary.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y)); y += step }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Text("AI DASHBOARD",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = theme.primary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                    ))
                Spacer(Modifier.width(72.dp))
            }

            Spacer(Modifier.height(14.dp))

            // Player overview
            DashCard(theme = theme, borderColor = theme.primary.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(progress.player.name,
                            style = MaterialTheme.typography.titleLarge.copy(color = theme.primary),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${tier.displayName}  ·  Level ${progress.player.level}",
                            style = MaterialTheme.typography.labelSmall.copy(color = theme.secondary),
                            maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${progress.player.totalCorrect}/${progress.player.totalAnswered}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = theme.accent, fontWeight = FontWeight.Bold
                            ))
                        Text("correct",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("XP Progress",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Spacer(Modifier.height(4.dp))
                NeonXpBar(progress.player.xp, progress.player.xpToNextLevel,
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(3.dp))
                Text("${progress.player.xp} / ${progress.player.xpToNextLevel} XP",
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.accent))
            }

            Spacer(Modifier.height(10.dp))

            // AI Engine status
            DashCard(theme = theme, borderColor = NeonPurple.copy(alpha = 0.4f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingGlowDot(NeonPurple)
                    Spacer(Modifier.width(8.dp))
                    Text("AI Adaptive Engine",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonPurple),
                        maxLines = 1)
                }
                Spacer(Modifier.height(10.dp))

                val nextDiff = AdaptiveEngine.computeNextDifficulty(twin)
                val diffColor = when (nextDiff) {
                    Difficulty.EASY   -> NeonGreen
                    Difficulty.MEDIUM -> NeonOrange
                    Difficulty.HARD   -> NeonRed
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricBox("Accuracy", "${(twin.recentAccuracy * 100).toInt()}%",
                        if (twin.recentAccuracy > 0.7f) NeonGreen else NeonOrange)
                    MetricBox("Avg Time", "${twin.averageResponseTimeMs / 1000}s",
                        if (twin.averageResponseTimeMs < 8000) NeonGreen else NeonOrange)
                    MetricBox("Difficulty", nextDiff.name, diffColor)
                    MetricBox("Focus", "${(twin.focusScore * 100).toInt()}%", NeonCyan)
                }

                Spacer(Modifier.height(10.dp))

                val message = when {
                    twin.recentAccuracy > 0.80f && twin.averageResponseTimeMs < 8000 ->
                        "Performing well. Difficulty increasing to keep you challenged."
                    twin.recentAccuracy < 0.50f ->
                        "Difficulty reduced. Try the Learning Hub to strengthen weak areas."
                    twin.averageResponseTimeMs > 15000 ->
                        "Taking more time than usual. Hints are available."
                    else ->
                        "Performing well. AI is fine-tuning your experience."
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Knowledge map
            DashCard(theme = theme, borderColor = NeonOrange.copy(alpha = 0.3f)) {
                Text("Knowledge Map",
                    style = MaterialTheme.typography.labelLarge.copy(color = NeonOrange))
                Spacer(Modifier.height(10.dp))

                if (twin.knowledgeScores.isEmpty()) {
                    Text("Play battles to build your knowledge profile.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                } else {
                    twin.knowledgeScores.entries.sortedBy { it.value }.forEach { (topic, score) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(topic,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                modifier = Modifier.width(90.dp),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f).height(6.dp)
                                .background(CardBorder, RoundedCornerShape(3.dp))
                            ) {
                                val barColor = when {
                                    score > 0.7f -> NeonGreen
                                    score > 0.4f -> NeonOrange
                                    else         -> NeonRed
                                }
                                Box(modifier = Modifier
                                    .fillMaxWidth(score).fillMaxHeight()
                                    .background(barColor, RoundedCornerShape(3.dp)))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${(score * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when {
                                        score > 0.7f -> NeonGreen
                                        score > 0.4f -> NeonOrange
                                        else         -> NeonRed
                                    }
                                ),
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.End)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Stats row
            DashCard(theme = theme, borderColor = NeonGold.copy(alpha = 0.3f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox("Streak",  "${progress.player.streakCount}", NeonOrange)
                    StatBox("Correct", "${progress.player.totalCorrect}", NeonGreen)
                    StatBox("Total",   "${progress.player.totalAnswered}", NeonCyan)
                    StatBox("Rate",
                        if (progress.player.totalAnswered > 0)
                            "${(progress.player.accuracy * 100).toInt()}%"
                        else "—",
                        NeonGold)
                }
            }

            // Recommendations
            if (recommendations.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                DashCard(theme = theme, borderColor = theme.secondary.copy(alpha = 0.3f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recommended Study",
                            style = MaterialTheme.typography.labelLarge.copy(color = theme.secondary))
                        NeonButton("View All", onClick = onOpenHub, color = theme.secondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    recommendations.take(3).forEach { mat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(theme.surface, RoundedCornerShape(6.dp))
                                .clickable { onOpenHub() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mat.title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${mat.topic}  ·  ${mat.estimatedMinutes} min",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                                    maxLines = 1)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("+${mat.xpReward} XP",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeonGold))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeonButton("World Map", onClick = onOpenMap,
                    modifier = Modifier.weight(1f), color = theme.primary)
                NeonButton("Study Hub", onClick = onOpenHub,
                    modifier = Modifier.weight(1f), color = theme.secondary)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
private fun DashCard(
    theme: TierTheme,
    borderColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(theme.surface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        content = content
    )
}

@Composable
private fun MetricBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value,
            style = MaterialTheme.typography.titleLarge.copy(
                color = color, fontWeight = FontWeight.Bold
            ))
        Text(label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = color, fontWeight = FontWeight.Bold
            ))
        Text(label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
    }
}
