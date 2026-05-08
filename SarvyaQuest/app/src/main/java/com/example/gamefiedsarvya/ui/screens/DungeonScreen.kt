package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.gamefiedsarvya.data.repository.QuestionRepository
import com.example.gamefiedsarvya.engine.AdaptiveEngine
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DungeonScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit
) {
    val progress by gameViewModel.progress.collectAsState()
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var answered by remember { mutableIntStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }
    var lastCorrect by remember { mutableStateOf<Boolean?>(null) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var isRunning by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var lives by remember { mutableIntStateOf(3) }
    val usedIds = remember { mutableSetOf<String>() }

    val coroutineScope = rememberCoroutineScope()

    fun loadNextQuestion() {
        val q = AdaptiveEngine.selectNextQuestion(progress.digitalTwin, usedIds, "zone_dungeon")
            ?: QuestionRepository.getAdaptiveQuestions("", progress.digitalTwin.preferredDifficulty, 5)
                .filter { it.id !in usedIds }.firstOrNull()
            ?: run { usedIds.clear(); QuestionRepository.getAdaptiveQuestions("", Difficulty.EASY, 5).firstOrNull() }
        q?.let {
            usedIds.add(it.id)
            currentQuestion = it
            timeLeft = AdaptiveEngine.adjustedTimeLimit(it.timeLimitSeconds, progress.digitalTwin)
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (isRunning && !gameOver) {
            delay(1000L)
            if (!showFeedback) {
                timeLeft--
                if (timeLeft <= 0) {
                    lives--
                    streak = 0
                    showFeedback = true
                    lastCorrect = false
                    if (lives <= 0) { gameOver = true; isRunning = false }
                    else {
                        delay(1500L)
                        showFeedback = false
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0A1A), DeepVoid)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Exit", onClick = onBack, color = TextSecondary)
                Text("DUNGEON",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = NeonOrange, letterSpacing = 3.sp
                    ))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Score: $score", style = MaterialTheme.typography.labelLarge.copy(color = NeonGold))
                    Text("Streak: $streak", style = MaterialTheme.typography.labelSmall.copy(color = NeonOrange))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Lives
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Text(if (i < lives) "+" else "-", fontSize = 20.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("Q${answered + 1}", style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary))
            }

            Spacer(Modifier.height(16.dp))

            if (!isRunning && !gameOver) {
                // Start screen
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚡", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("DUNGEON MODE",
                            style = MaterialTheme.typography.displayMedium.copy(color = NeonOrange))
                        Spacer(Modifier.height(8.dp))
                        Text("Fast-paced questions. 3 lives. High XP rewards.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextSecondary, textAlign = TextAlign.Center
                            ))
                        Spacer(Modifier.height(8.dp))
                        Text("Difficulty adapts to your performance.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = NeonPurple))
                        Spacer(Modifier.height(32.dp))
                        NeonButton("⚡ ENTER DUNGEON", onClick = {
                            loadNextQuestion()
                            isRunning = true
                        }, color = NeonOrange)
                    }
                }
            } else if (gameOver) {
                // Game over
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💀 DUNGEON CLEARED",
                            style = MaterialTheme.typography.displayMedium.copy(color = NeonRed))
                        Spacer(Modifier.height(16.dp))
                        Text("Final Score: $score",
                            style = MaterialTheme.typography.headlineLarge.copy(color = NeonGold))
                        Text("Questions: $answered  •  Best Streak: $streak",
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NeonButton("Play Again", onClick = {
                                score = 0; streak = 0; answered = 0; lives = 3
                                gameOver = false; usedIds.clear()
                                loadNextQuestion(); isRunning = true
                            }, color = NeonOrange)
                            NeonButton("Exit", onClick = onBack, color = TextSecondary)
                        }
                    }
                }
            } else {
                // Active dungeon
                currentQuestion?.let { q ->
                    // Timer
                    TimerBarDungeon(timeLeft, q.timeLimitSeconds)
                    Spacer(Modifier.height(12.dp))

                    // Question
                    GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonOrange.copy(alpha = 0.4f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DifficultyBadge(q.difficulty.name, NeonOrange)
                                DifficultyBadge(q.topic, NeonCyan)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(q.text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Options
                    q.options.forEachIndexed { idx, opt ->
                        val isCorrect = idx == q.correctIndex
                        val color = when {
                            !showFeedback -> NeonOrange
                            isCorrect     -> NeonGreen
                            else          -> NeonRed
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .clickable(enabled = !showFeedback) {
                                    val correct = idx == q.correctIndex
                                    lastCorrect = correct
                                    showFeedback = true
                                    answered++
                                    if (correct) {
                                        streak++
                                        score += 10 + streak * 2
                                    } else {
                                        streak = 0
                                        lives--
                                        if (lives <= 0) { gameOver = true; isRunning = false }
                                    }
                                    if (!gameOver) {
                                        coroutineScope.launch {
                                            delay(1500L)
                                            showFeedback = false
                                            loadNextQuestion()
                                        }
                                    }
                                }
                                .padding(14.dp)
                        ) {
                            Text(opt, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    // Feedback
                    AnimatedVisibility(visible = showFeedback) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(
                                    if (lastCorrect == true) NeonGreen.copy(alpha = 0.1f)
                                    else NeonRed.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                if (lastCorrect == true) "✓ Correct! +${10 + streak * 2} pts"
                                else "✗ Wrong! ${q.explanation}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (lastCorrect == true) NeonGreen else NeonRed
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerBarDungeon(remaining: Int, total: Int) {
    val fraction = (remaining.toFloat() / total).coerceIn(0f, 1f)
    val color = when {
        fraction > 0.5f  -> NeonGreen
        fraction > 0.25f -> NeonOrange
        else             -> NeonRed
    }
    val animFraction by animateFloatAsState(targetValue = fraction, animationSpec = tween(800), label = "dt")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⏱", fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier.weight(1f).height(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(CardBorder)
        ) {
            Box(modifier = Modifier.fillMaxWidth(animFraction).fillMaxHeight()
                .clip(RoundedCornerShape(3.dp)).background(color))
        }
        Spacer(Modifier.width(6.dp))
        Text("${remaining}s", style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}
