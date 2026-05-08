package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.SUPPORTED_LANGUAGES
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.VoiceViewModel

@Composable
fun VoiceSettingsScreen(
    voiceViewModel: VoiceViewModel,
    onBack: () -> Unit
) {
    val state by voiceViewModel.state.collectAsState()
    val settings = state.settings

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
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
                Text("VOICE & LANGUAGE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = NeonCyan, letterSpacing = 2.sp
                    ))
                Spacer(Modifier.width(80.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Voice Input (STT) ─────────────────────────────────────────────
            VoiceSection("Voice Input (Speech-to-Text)", NeonRed) {
                VoiceToggleRow(
                    label   = "Enable Microphone Input",
                    checked = settings.sttEnabled,
                    color   = NeonRed,
                    onToggle = { voiceViewModel.toggleStt(it) }
                )
                if (settings.sttEnabled) {
                    Spacer(Modifier.height(8.dp))
                    InfoBox("Tap the mic button during practice to answer by voice.", NeonRed)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Voice Output (TTS) ────────────────────────────────────────────
            VoiceSection("Voice Output (Text-to-Speech)", NeonGreen) {
                VoiceToggleRow(
                    label   = "Enable Audio Playback",
                    checked = settings.ttsEnabled,
                    color   = NeonGreen,
                    onToggle = { voiceViewModel.toggleTts(it) }
                )
                if (settings.ttsEnabled) {
                    Spacer(Modifier.height(8.dp))
                    VoiceToggleRow(
                        label   = "Auto-narrate questions",
                        checked = settings.autoNarrate,
                        color   = NeonGreen,
                        onToggle = { voiceViewModel.toggleAutoNarrate(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                    InfoBox("Questions and feedback will be read aloud automatically.", NeonGreen)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Language ──────────────────────────────────────────────────────
            VoiceSection("Language", NeonCyan) {
                Text("Select your preferred language for voice and AI responses.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                Spacer(Modifier.height(12.dp))

                SUPPORTED_LANGUAGES.forEach { lang ->
                    val isSelected = settings.language == lang.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else CardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) NeonCyan.copy(alpha = 0.12f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { voiceViewModel.setLanguage(lang.code) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(lang.flag, fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(lang.nativeName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (isSelected) NeonCyan else TextPrimary
                                    ))
                                Text(lang.englishName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                            }
                        }
                        if (isSelected) {
                            Text("✓", style = MaterialTheme.typography.titleLarge.copy(color = NeonCyan))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── TTS Status ────────────────────────────────────────────────────
            VoiceSection("Status", NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusChip("TTS Engine",
                        if (state.isTtsReady) "Ready ✓" else "Not ready",
                        if (state.isTtsReady) NeonGreen else NeonOrange)
                    StatusChip("STT",
                        if (settings.sttEnabled) "Enabled" else "Disabled",
                        if (settings.sttEnabled) NeonGreen else TextMuted)
                    StatusChip("Language",
                        settings.language.uppercase(),
                        NeonCyan)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VoiceSection(
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(CardSurface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge.copy(color = color))
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun VoiceToggleRow(
    label: String,
    checked: Boolean,
    color: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor  = color,
                checkedTrackColor  = color.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun InfoBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(color = color))
    }
}

@Composable
private fun StatusChip(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(value, style = MaterialTheme.typography.labelLarge.copy(color = color))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
    }
}
