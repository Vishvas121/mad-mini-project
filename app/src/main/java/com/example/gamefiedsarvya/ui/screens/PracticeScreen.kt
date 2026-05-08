package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
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
import com.example.gamefiedsarvya.data.repository.QuestionRepository
import com.example.gamefiedsarvya.engine.AdaptiveEngine
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel

@Composable
fun PracticeScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit
) {
    val progress by gameViewModel.progress.collectAsState()
    val topics   = remember { QuestionRepository.getAllTopics() }

    var selectedTopic by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var lastCorrect by remember { mutableStateOf<Boolean?>(null) }
    var hintVisible by remember { mutableStateOf(false) }
    var totalAnswered by remember { mutableIntStateOf(0) }
    var totalCorrect by remember { mutableIntStateOf(0) }
    val usedIds = remember { mutableSetOf<String>() }

    fun loadQuestion() {
        val q = QuestionRepository.getAdaptiveQuestions(selectedTopic, selectedDifficulty, 10)
            .filter { it.id !in usedIds }.firstOrNull()
            ?: run { usedIds.clear()
                QuestionRepository.getAdaptiveQuestions(selectedTopic, selectedDifficulty, 5).firstOrNull() }
        q?.let { usedIds.add(it.id); currentQuestion = it }
        showFeedback = false; lastCorrect = null; hintVisible = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepVoid)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Text("PRACTICE",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = NeonGreen, letterSpacing = 3.sp
                    ))
                Spacer(Modifier.width(80.dp))
            }

            Spacer(Modifier.height(8.dp))

            // AI Tutor banner
            GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGreen.copy(alpha = 0.3f)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    PulsingGlowDot(NeonGreen)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("AI Tutor Active", style = MaterialTheme.typography.labelLarge.copy(color = NeonGreen))
                        Text("No time pressure. Hints available. Learn at your pace.",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPill("Answered", "$totalAnswered", NeonCyan)
                StatPill("Correct", "$totalCorrect", NeonGreen)
                StatPill("Accuracy",
                    if (totalAnswered > 0) "${(totalCorrect * 100 / totalAnswered)}%" else "—",
                    NeonGold)
            }

            Spacer(Modifier.height(16.dp))

            // Topic selector
            Text("Topic", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopicChip("All", selectedTopic == "") { selectedTopic = ""; currentQuestion = null }
                topics.forEach { topic ->
                    TopicChip(topic, selectedTopic == topic) { selectedTopic = topic; currentQuestion = null }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Difficulty selector
            Text("Difficulty", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.values().forEach { diff ->
                    val color = when (diff) {
                        Difficulty.EASY   -> NeonGreen
                        Difficulty.MEDIUM -> NeonOrange
                        Difficulty.HARD   -> NeonRed
                    }
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (selectedDifficulty == diff) color else color.copy(alpha = 0.3f),
                                RoundedCornerShape(6.dp)
                            )
                            .background(
                                if (selectedDifficulty == diff) color.copy(alpha = 0.2f)
                                else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedDifficulty = diff; currentQuestion = null }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(diff.name, style = MaterialTheme.typography.labelLarge.copy(color = color))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (currentQuestion == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NeonButton("Start Practice", onClick = { loadQuestion() }, color = NeonGreen)
                }
            } else {
                val q = currentQuestion!!

                // Question
                GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGreen.copy(alpha = 0.3f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DifficultyBadge(q.difficulty.name, NeonGreen)
                            DifficultyBadge(q.topic, NeonCyan)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(q.text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        if (hintVisible && q.hint.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Hint: ${q.hint}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = NeonGold))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Hint button
                if (!hintVisible && !showFeedback) {
                    NeonButton("💡 Show Hint", onClick = { hintVisible = true }, color = NeonGold)
                    Spacer(Modifier.height(8.dp))
                }

                // Options
                q.options.forEachIndexed { idx, opt ->
                    val isCorrect = idx == q.correctIndex
                    val color = when {
                        !showFeedback -> NeonGreen
                        isCorrect     -> NeonGreen
                        else          -> NeonRed
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = if (showFeedback && isCorrect) 0.2f else 0.06f),
                                RoundedCornerShape(8.dp))
                            .clickable(enabled = !showFeedback) {
                                val correct = idx == q.correctIndex
                                lastCorrect = correct
                                showFeedback = true
                                totalAnswered++
                                if (correct) totalCorrect++
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${('A' + idx)}. ", style = MaterialTheme.typography.labelLarge.copy(color = color))
                            Text(opt, style = MaterialTheme.typography.bodyLarge)
                            if (showFeedback && isCorrect) {
                                Spacer(Modifier.weight(1f))
                                Text("OK", style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen))
                            }
                        }
                    }
                }

                // Feedback + explanation
                AnimatedVisibility(visible = showFeedback) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (lastCorrect == true) NeonGreen.copy(alpha = 0.1f)
                                    else NeonRed.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                if (lastCorrect == true) "✓ Correct!" else "✗ Not quite.",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (lastCorrect == true) NeonGreen else NeonRed
                                )
                            )
                        }
                        if (q.explanation.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NeonGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .background(NeonGold.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text("${q.explanation}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = NeonGold))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        NeonButton("Next Question →", onClick = { loadQuestion() }, color = NeonGreen,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TopicChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(
                1.dp,
                if (selected) NeonCyan else CardBorder,
                RoundedCornerShape(20.dp)
            )
            .background(
                if (selected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(
            color = if (selected) NeonCyan else TextSecondary
        ))
    }
}
