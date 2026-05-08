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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*

/**
 * NEW FEATURE: Tier selection screen.
 * Shown once on first launch or accessible from Settings.
 * Does NOT affect existing progress.
 */
@Composable
fun TierSelectScreen(
    currentTier: LearningTier,
    onTierSelected: (LearningTier) -> Unit,
    onBack: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(currentTier) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepVoid)
    ) {
        // Background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NeonPurple.copy(alpha = 0.12f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.3f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.3f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            NeonButton("Back", onClick = onBack, color = TextSecondary,
                modifier = Modifier.align(Alignment.Start))

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = visible, enter = fadeIn(tween(600)) + slideInVertically()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CHOOSE YOUR PATH",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = NeonCyan, letterSpacing = 4.sp, fontWeight = FontWeight.Black
                        ))
                    Spacer(Modifier.height(4.dp))
                    Text("Select your learning tier. You can change this anytime.",
                        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center))
                }
            }

            Spacer(Modifier.height(28.dp))

            LearningTier.values().forEach { tier ->
                TierCard(
                    tier       = tier,
                    isSelected = selectedTier == tier,
                    onSelect   = { selectedTier = tier }
                )
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(8.dp))

            NeonButton(
                "Confirm: ${selectedTier.displayName}",
                onClick = { onTierSelected(selectedTier) },
                modifier = Modifier.fillMaxWidth(),
                color = NeonGold
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TierCard(
    tier: LearningTier,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val (accentColor, bgColor) = when (tier) {
        LearningTier.FOUNDATION   -> Pair(NeonGreen,  NeonGreen.copy(alpha = 0.08f))
        LearningTier.ADVANCED     -> Pair(NeonOrange, NeonOrange.copy(alpha = 0.08f))
        LearningTier.PROFESSIONAL -> Pair(NeonPurple, NeonPurple.copy(alpha = 0.08f))
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "tier_scale"
    )

    val inf = rememberInfiniteTransition(label = "tier_glow")
    val glowAlpha by inf.animateFloat(
        0.4f, 0.9f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor.copy(alpha = glowAlpha) else accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.icon, fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(tier.displayName,
                            style = MaterialTheme.typography.headlineMedium.copy(color = accentColor))
                        Text(tier.ageRange,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .border(1.dp, accentColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OK", style = MaterialTheme.typography.labelLarge.copy(color = accentColor))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(tier.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            // Feature chips
            val features = when (tier) {
                LearningTier.FOUNDATION -> listOf("Colorful UI", "Voice Guide", "Relaxed Timer", "Hints Always On")
                LearningTier.ADVANCED   -> listOf("Timed Battles", "Concept Focus", "Boss Fights", "📈 ${(tier.getXpMultiplier() * 100).toInt()}% XP")
                LearningTier.PROFESSIONAL -> listOf("Complex Problems", "Fast Timer", "Skill Tree", "🚀 ${(tier.getXpMultiplier() * 100).toInt()}% XP")
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEach { f ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(f, style = MaterialTheme.typography.labelSmall.copy(color = accentColor))
                    }
                }
            }
        }
    }
}
