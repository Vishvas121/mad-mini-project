package com.example.gamefiedsarvya.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.components.HeroDirection
import com.example.gamefiedsarvya.ui.components.HeroSprite
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel

@Composable
fun SettingsScreen(
    gameViewModel: GameViewModel,
    hubViewModel: LearningHubViewModel,
    onBack: () -> Unit,
    onTierSelect: () -> Unit,
    onVoiceSettings: () -> Unit = {},
    onThemeSelect: () -> Unit = {}
) {
    val progress  by gameViewModel.progress.collectAsState()
    val hubState  by hubViewModel.uiState.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(progress.selectedLanguage) }

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
                Text("SETTINGS",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary, letterSpacing = 3.sp
                    ))
                Spacer(Modifier.width(80.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Player info
            SectionHeader("Player Profile")
            GameCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        HeroSprite(
                            direction = HeroDirection.SOUTH,
                            size      = 64.dp,
                            glowColor = NeonCyan,
                            showGlow  = true,
                            floatAnim = true
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(progress.player.name,
                                style = MaterialTheme.typography.titleLarge)
                            Text("Level ${progress.player.level} Cyber Warrior",
                                style = MaterialTheme.typography.bodyMedium.copy(color = NeonCyan))
                        }
                    }
                    InfoRow("Name", progress.player.name)
                    InfoRow("Level", "${progress.player.level}")
                    InfoRow("Total XP Earned", "${progress.player.xp + (progress.player.level - 1) * 100}")
                    InfoRow("Accuracy", "${(progress.player.accuracy * 100).toInt()}%")
                    InfoRow("Questions Answered", "${progress.player.totalAnswered}")
                }
            }

            Spacer(Modifier.height(20.dp))

            // AI Engine status
            SectionHeader("AI Adaptive Engine")
            GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPurple.copy(alpha = 0.3f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingGlowDot(NeonPurple)
                        Spacer(Modifier.width(8.dp))
                        Text("Active", style = MaterialTheme.typography.labelLarge.copy(color = NeonPurple))
                    }
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Current Difficulty", progress.digitalTwin.preferredDifficulty.name)
                    InfoRow("Recent Accuracy", "${(progress.digitalTwin.recentAccuracy * 100).toInt()}%")
                    InfoRow("Avg Response Time", "${progress.digitalTwin.averageResponseTimeMs / 1000}s")
                    Spacer(Modifier.height(8.dp))
                    Text("Knowledge Scores", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    progress.digitalTwin.knowledgeScores.forEach { (topic, score) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(topic, style = MaterialTheme.typography.bodyMedium)
                            Text("${(score * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when {
                                        score > 0.7f -> NeonGreen
                                        score > 0.4f -> NeonOrange
                                        else         -> NeonRed
                                    }
                                ))
                        }
                    }
                    if (progress.digitalTwin.knowledgeScores.isEmpty()) {
                        Text("Play to build your knowledge profile.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Learning Tier
            SectionHeader("Learning Tier")
            GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGold.copy(alpha = 0.3f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${hubState.selectedTier.icon} ${hubState.selectedTier.displayName}",
                                style = MaterialTheme.typography.titleLarge.copy(color = NeonGold))
                            Text(hubState.selectedTier.ageRange,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        NeonButton("Change", onClick = onTierSelect, color = NeonGold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Audio Settings
            SectionHeader("Audio")
            GameCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Music toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Background Music", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = hubState.musicEnabled,
                            onCheckedChange = { hubViewModel.setMusicEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.3f))
                        )
                    }
                    if (hubState.musicEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text("Volume", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = hubState.musicVolume,
                            onValueChange = { hubViewModel.setMusicVolume(it) },
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // SFX toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = hubState.sfxEnabled,
                            onCheckedChange = { hubViewModel.setSfxEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple, checkedTrackColor = NeonPurple.copy(alpha = 0.3f))
                        )
                    }
                    if (hubState.sfxEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text("Volume", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = hubState.sfxVolume,
                            onValueChange = { hubViewModel.setSfxVolume(it) },
                            colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Language
            SectionHeader("Language")
            GameCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val languages = listOf("en" to "English", "ta" to "Tamil", "hi" to "Hindi")
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = code }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyLarge)
                            if (selectedLanguage == code) {
                                Text("OK", style = MaterialTheme.typography.titleLarge.copy(color = NeonCyan))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Voice & Language
            SectionHeader("Voice & Language")
            NeonButton(
                "Voice Settings",
                onClick  = onVoiceSettings,
                modifier = Modifier.fillMaxWidth(),
                color    = NeonCyan
            )

            Spacer(Modifier.height(12.dp))

            // Theme
            SectionHeader("Visual Theme")
            NeonButton(
                "Change Theme",
                onClick  = onThemeSelect,
                modifier = Modifier.fillMaxWidth(),
                color    = NeonPurple
            )

            Spacer(Modifier.height(20.dp))

            // Hardware integration placeholder
            SectionHeader("Hardware Integration (Coming Soon)")
            GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGold.copy(alpha = 0.2f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Focus Sensor", style = MaterialTheme.typography.bodyLarge)
                    Text("Connect an EEG device to dynamically adjust difficulty based on your focus level.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                    Spacer(Modifier.height(8.dp))
                    Text("Eye Tracking", style = MaterialTheme.typography.bodyLarge)
                    Text("Engagement signals will reduce difficulty when distraction is detected.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .border(1.dp, NeonGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .background(NeonGold.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text("Simulated values active for demo",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonGold))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Danger zone
            SectionHeader("Danger Zone")
            if (!showResetConfirm) {
                NeonButton(
                    "🗑 Reset All Progress",
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonRed
                )
            } else {
                GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonRed.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Are you sure? This cannot be undone.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = NeonRed))
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NeonButton("Yes, Reset", onClick = {
                                gameViewModel.resetGame()
                                showResetConfirm = false
                            }, modifier = Modifier.weight(1f), color = NeonRed)
                            NeonButton("Cancel", onClick = { showResetConfirm = false },
                                modifier = Modifier.weight(1f), color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Version
            Text("Sarvya Thozan v1.0  •  AI-Powered Learning RPG",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge.copy(color = NeonCyan, letterSpacing = 2.sp),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary, fontWeight = FontWeight.SemiBold
        ))
    }
}
