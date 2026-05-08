package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
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
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.components.HeroDirection
import com.example.gamefiedsarvya.ui.components.HeroSprite
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel
import com.example.gamefiedsarvya.viewmodel.UserProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: UserProfileViewModel,
    gameViewModel: GameViewModel,
    hubViewModel: LearningHubViewModel,
    onBack: () -> Unit
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val progress     by gameViewModel.progress.collectAsState()
    val hubState     by hubViewModel.uiState.collectAsState()
    val theme        = com.example.gamefiedsarvya.ui.theme.TierThemes.forTier(hubState.selectedTier)

    var editingName  by remember { mutableStateOf(false) }
    var nameInput    by remember { mutableStateOf(profileState.profile.name) }
    var showSaved    by remember { mutableStateOf(false) }

    LaunchedEffect(profileState.saveSuccess) {
        if (profileState.saveSuccess) {
            showSaved = true
            kotlinx.coroutines.delay(2000)
            showSaved = false
            profileViewModel.clearSaveSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(theme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Text("MY PROFILE", style = theme.titleStyle.copy(color = theme.primary))
                Spacer(Modifier.width(80.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Avatar + name hero section
            Box(contentAlignment = Alignment.Center) {
                // Glow ring
                Box(modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(listOf(theme.primary.copy(alpha = 0.3f), Color.Transparent)),
                        CircleShape
                    )
                )
                HeroSprite(
                    direction = HeroDirection.SOUTH,
                    size      = 80.dp,
                    glowColor = theme.primary,
                    showGlow  = true,
                    floatAnim = true
                )
            }

            Spacer(Modifier.height(12.dp))

            // Name display / edit
            if (editingName) {
                OutlinedTextField(
                    value         = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(0.7f),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = theme.primary,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        cursorColor          = theme.primary
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary, textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonButton("Save", onClick = {
                        profileViewModel.updateName(nameInput)
                        editingName = false
                    }, color = theme.primary)
                    NeonButton("Cancel", onClick = {
                        nameInput = profileState.profile.name
                        editingName = false
                    }, color = TextSecondary)
                }
            } else {
                Text(
                    profileState.profile.displayName,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = theme.primary, fontWeight = FontWeight.Black
                    )
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${hubState.selectedTier.icon} ${hubState.selectedTier.displayName}",
                        style = MaterialTheme.typography.labelLarge.copy(color = theme.secondary))
                    Spacer(Modifier.width(8.dp))
                    Text("•", color = TextMuted)
                    Spacer(Modifier.width(8.dp))
                    Text("Level ${progress.player.level}",
                        style = MaterialTheme.typography.labelLarge.copy(color = theme.accent))
                }
                Spacer(Modifier.height(8.dp))
                NeonButton("Edit Name", onClick = {
                    nameInput = profileState.profile.name
                    editingName = true
                }, color = theme.primary)
            }

            // Save success toast
            AnimatedVisibility(visible = showSaved) {
                Box(modifier = Modifier
                    .padding(top = 8.dp)
                    .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Profile saved", style = MaterialTheme.typography.labelLarge.copy(color = NeonGreen))
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Stats grid ────────────────────────────────────────────────────
            ProfileSection("📊 Stats", theme.primary) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBox("Level",    "${progress.player.level}",          theme.accent)
                    StatBox("XP",       "${progress.player.xp}",             NeonGold)
                    StatBox("Accuracy", "${(progress.player.accuracy * 100).toInt()}%", NeonGreen)
                    StatBox("Streak",   "${progress.player.streakCount}🔥",  NeonOrange)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Language ──────────────────────────────────────────────────────
            ProfileSection("🌐 Language", theme.secondary) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SUPPORTED_LANGUAGES.take(6).forEach { lang ->
                        val isSelected = profileState.profile.preferredLanguage == lang.code
                        Box(modifier = Modifier
                            .border(1.dp, if (isSelected) theme.primary else CardBorder, RoundedCornerShape(8.dp))
                            .background(if (isSelected) theme.primary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                profileViewModel.saveProfile(
                                    profileState.profile.copy(preferredLanguage = lang.code)
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("${lang.flag} ${lang.nativeName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) theme.primary else TextSecondary
                                ))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Accessibility ─────────────────────────────────────────────────
            ProfileSection("♿ Accessibility", theme.accent) {
                AccessibilityToggle("Simple Mode (larger text)",
                    profileState.profile.simpleMode) {
                    profileViewModel.saveProfile(profileState.profile.copy(simpleMode = it))
                }
                Spacer(Modifier.height(8.dp))
                AccessibilityToggle("High Contrast",
                    profileState.profile.highContrast) {
                    profileViewModel.saveProfile(profileState.profile.copy(highContrast = it))
                }
                Spacer(Modifier.height(8.dp))
                AccessibilityToggle("Voice Narration (TTS)",
                    profileState.profile.voiceEnabled) {
                    profileViewModel.saveProfile(profileState.profile.copy(voiceEnabled = it))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── AI Greeting preview ───────────────────────────────────────────
            if (profileState.personalisedGreeting.isNotBlank()) {
                ProfileSection("🤖 AI Greeting", NeonPurple) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                    ) {
                        Text(profileState.personalisedGreeting,
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier
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
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
    }
}

@Composable
private fun AccessibilityToggle(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor  = NeonCyan,
                checkedTrackColor  = NeonCyan.copy(alpha = 0.3f)
            ))
    }
}
