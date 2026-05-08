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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.assets.SarvyaQuestLogo
import com.example.gamefiedsarvya.ui.assets.LogoSize
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel
import com.example.gamefiedsarvya.viewmodel.UserProfileViewModel

/**
 * First-run onboarding: name entry → tier selection → language → done.
 * Skipped on subsequent launches.
 */
@Composable
fun OnboardingScreen(
    profileViewModel: UserProfileViewModel,
    hubViewModel: LearningHubViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf(LearningTier.FOUNDATION) }
    var selectedLang by remember { mutableStateOf("en") }
    var selectedDiff by remember { mutableStateOf(DifficultyPreference.ADAPTIVE) }

    val totalSteps = 4

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        // Animated background
        val inf = rememberInfiniteTransition(label = "ob_bg")
        val glow by inf.animateFloat(0.3f, 0.7f,
            infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "glow")
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(
                listOf(NeonCyan.copy(alpha = glow * 0.15f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.2f), radius = 400f
            ), 400f, Offset(size.width * 0.2f, size.height * 0.2f))
            drawCircle(Brush.radialGradient(
                listOf(NeonPurple.copy(alpha = glow * 0.12f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.8f), radius = 350f
            ), 350f, Offset(size.width * 0.8f, size.height * 0.8f))
        }

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { i ->
                    Box(modifier = Modifier
                        .size(if (i == step) 24.dp else 8.dp, 8.dp)
                        .background(
                            if (i <= step) NeonCyan else CardBorder,
                            RoundedCornerShape(4.dp)
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "ob_step"
            ) { currentStep ->
                when (currentStep) {
                    0 -> WelcomeStep(onNext = { step = 1 })
                    1 -> NameStep(name = name, onNameChange = { name = it }, onNext = { if (name.isNotBlank()) step = 2 })
                    2 -> TierStep(selected = selectedTier, onSelect = { selectedTier = it }, onNext = { step = 3 })
                    3 -> PreferencesStep(
                        selectedLang = selectedLang,
                        selectedDiff = selectedDiff,
                        onLangChange = { selectedLang = it },
                        onDiffChange = { selectedDiff = it },
                        onComplete   = {
                            val profile = UserProfile(
                                name                  = name.trim(),
                                preferredLanguage     = selectedLang,
                                difficultyPreference  = selectedDiff,
                                onboardingComplete    = true
                            )
                            profileViewModel.completeOnboarding(profile)
                            hubViewModel.setTier(selectedTier)
                            onComplete()
                        }
                    )
                }
            }
        }
    }
}

// ── Step 0: Welcome ───────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        SarvyaQuestLogo(size = LogoSize.LARGE)
        Spacer(Modifier.height(32.dp))
        Text("Your AI-Powered\nLearning Adventure",
            style = MaterialTheme.typography.displayMedium.copy(
                color = TextPrimary, fontWeight = FontWeight.Black, textAlign = TextAlign.Center
            ))
        Spacer(Modifier.height(16.dp))
        Text("Adaptive learning that feels like a game.\nPersonalised just for you.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary, textAlign = TextAlign.Center
            ))
        Spacer(Modifier.height(48.dp))
        NeonButton("Let's Begin", onClick = onNext,
            modifier = Modifier.fillMaxWidth(), color = NeonCyan)
    }
}

// ── Step 1: Name ──────────────────────────────────────────────────────────────

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("👋", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("What's your name?",
            style = MaterialTheme.typography.displayMedium.copy(
                color = NeonCyan, fontWeight = FontWeight.Black
            ))
        Spacer(Modifier.height(8.dp))
        Text("We'll personalise everything just for you.",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value         = name,
            onValueChange = onNameChange,
            placeholder   = { Text("Enter your name", color = TextMuted) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = NeonCyan,
                unfocusedBorderColor = CardBorder,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = NeonCyan
            ),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary, textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(32.dp))
        NeonButton(
            text     = "Continue →",
            onClick  = onNext,
            enabled  = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            color    = NeonCyan
        )
    }
}

// ── Step 2: Tier ──────────────────────────────────────────────────────────────

@Composable
private fun TierStep(
    selected: LearningTier,
    onSelect: (LearningTier) -> Unit,
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        Text("Choose Your Path",
            style = MaterialTheme.typography.displayMedium.copy(
                color = NeonGold, fontWeight = FontWeight.Black
            ))
        Spacer(Modifier.height(8.dp))
        Text("You can change this anytime.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Spacer(Modifier.height(24.dp))

        LearningTier.values().forEach { tier ->
            val (color, bg) = when (tier) {
                LearningTier.FOUNDATION   -> Pair(Color(0xFF00C896), Color(0xFF00C896).copy(alpha = 0.1f))
                LearningTier.ADVANCED     -> Pair(Color(0xFF2979FF), Color(0xFF2979FF).copy(alpha = 0.1f))
                LearningTier.PROFESSIONAL -> Pair(NeonPurple, NeonPurple.copy(alpha = 0.1f))
            }
            val isSelected = selected == tier
            val scale by animateFloatAsState(if (isSelected) 1.02f else 1f,
                spring(Spring.DampingRatioMediumBouncy), label = "tier_s")

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .scale(scale)
                .border(if (isSelected) 2.dp else 1.dp,
                    if (isSelected) color else color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(bg, RoundedCornerShape(16.dp))
                .clickable { onSelect(tier) }
                .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.icon, fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tier.displayName, style = MaterialTheme.typography.titleLarge.copy(color = color))
                        Text(tier.ageRange, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    }
                    if (isSelected) Text("✓", style = MaterialTheme.typography.titleLarge.copy(color = color))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        NeonButton("Continue →", onClick = onNext,
            modifier = Modifier.fillMaxWidth(), color = NeonGold)
    }
}

// ── Step 3: Preferences ───────────────────────────────────────────────────────

@Composable
private fun PreferencesStep(
    selectedLang: String,
    selectedDiff: DifficultyPreference,
    onLangChange: (String) -> Unit,
    onDiffChange: (DifficultyPreference) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        Text("Almost Ready!",
            style = MaterialTheme.typography.displayMedium.copy(
                color = NeonGreen, fontWeight = FontWeight.Black
            ))
        Spacer(Modifier.height(8.dp))
        Text("Set your preferences",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
        Spacer(Modifier.height(24.dp))

        // Language
        Text("Language", style = MaterialTheme.typography.titleLarge.copy(color = NeonCyan))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SUPPORTED_LANGUAGES.take(6).forEach { lang ->
                val isSelected = selectedLang == lang.code
                Box(modifier = Modifier
                    .border(1.dp, if (isSelected) NeonCyan else CardBorder, RoundedCornerShape(8.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onLangChange(lang.code) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("${lang.flag} ${lang.nativeName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) NeonCyan else TextSecondary
                        ))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Difficulty
        Text("Difficulty Mode", style = MaterialTheme.typography.titleLarge.copy(color = NeonOrange))
        Spacer(Modifier.height(8.dp))
        DifficultyPreference.values().forEach { diff ->
            val isSelected = selectedDiff == diff
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, if (isSelected) NeonOrange else CardBorder, RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonOrange.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                .clickable { onDiffChange(diff) }
                .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(diff.label, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f))
                if (isSelected) Text("✓", style = MaterialTheme.typography.titleLarge.copy(color = NeonOrange))
            }
        }

        Spacer(Modifier.height(32.dp))
        NeonButton("🚀 Start My Journey!", onClick = onComplete,
            modifier = Modifier.fillMaxWidth(), color = NeonGreen)
    }
}
