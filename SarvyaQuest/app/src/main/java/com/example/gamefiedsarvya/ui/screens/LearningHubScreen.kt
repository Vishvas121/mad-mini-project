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
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.LearningHubRepository
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel

/**
 * NEW FEATURE: Learning Hub
 * Embedded study material — no external redirects.
 * Gamified: earn XP, unlock boosts, show "Prepared" status.
 */
@Composable
fun LearningHubScreen(
    gameViewModel: GameViewModel,
    selectedTier: LearningTier,
    onBack: () -> Unit
) {
    val progress by gameViewModel.progress.collectAsState()
    var selectedMaterial by remember { mutableStateOf<StudyMaterial?>(null) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val materials = remember(selectedTier) {
        LearningHubRepository.getMaterialsForTier(selectedTier)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepVoid)
    ) {
        if (selectedMaterial == null) {
            // ── Hub Home ──────────────────────────────────────────────────────
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
                    Text(
                        "LEARNING HUB",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = NeonGreen, letterSpacing = 3.sp
                        )
                    )
                    Spacer(Modifier.width(80.dp))
                }

                Spacer(Modifier.height(8.dp))

                // Tier badge
                Box(
                    modifier = Modifier
                        .border(1.dp, NeonGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .background(NeonGold.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${selectedTier.icon} ${selectedTier.displayName} Tier",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonGold)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Study stats
                GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGreen.copy(alpha = 0.3f)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill("Studied", "${0}", NeonGreen)  // TODO: wire to extended progress
                        StatPill("XP Earned", "${0}", XpGold)
                        StatPill("Topics", "${materials.map { it.topic }.distinct().size}", NeonCyan)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Study materials unlock boosts and improve performance",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Material cards
                materials.forEach { material ->
                    MaterialCard(
                        material   = material,
                        isStudied  = false,  // TODO: wire to extended progress
                        onClick    = { selectedMaterial = material }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
            // ── Material Detail ───────────────────────────────────────────────
            MaterialDetailView(
                material  = selectedMaterial!!,
                onBack    = { selectedMaterial = null },
                onComplete = {
                    showCompleteDialog = true
                    // TODO: mark as studied, award XP
                }
            )
        }

        // Complete dialog
        if (showCompleteDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showCompleteDialog = false },
                contentAlignment = Alignment.Center
            ) {
                GameCard(
                    modifier = Modifier.padding(32.dp),
                    borderColor = NeonGreen
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✓ COMPLETED", style = MaterialTheme.typography.headlineMedium.copy(color = NeonGreen))
                        Spacer(Modifier.height(8.dp))
                        Text("+${selectedMaterial?.xpReward ?: 0} XP",
                            style = MaterialTheme.typography.titleLarge.copy(color = XpGold))
                        Spacer(Modifier.height(4.dp))
                        Text("You're now prepared for ${selectedMaterial?.topic} battles!",
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        NeonButton("Continue", onClick = {
                            showCompleteDialog = false
                            selectedMaterial = null
                        }, color = NeonGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialCard(
    material: StudyMaterial,
    isStudied: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isStudied) NeonGreen.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .background(
                if (isStudied) NeonGreen.copy(alpha = 0.08f) else CardSurface,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(material.title, style = MaterialTheme.typography.titleLarge)
                    if (isStudied) {
                        Spacer(Modifier.width(8.dp))
                        Text("✓", style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(material.topic, style = MaterialTheme.typography.bodyMedium.copy(color = NeonCyan))
                Spacer(Modifier.height(4.dp))
                Text("${material.estimatedMinutes} min  •  +${material.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
            Box(
                modifier = Modifier
                    .background(NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isStudied) "Done" else "Read",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isStudied) NeonGreen else NeonCyan
                    )
                )
            }
        }
    }
}

@Composable
private fun MaterialDetailView(
    material: StudyMaterial,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            Text(material.topic, style = MaterialTheme.typography.labelLarge.copy(color = NeonCyan))
        }

        Spacer(Modifier.height(16.dp))

        // Title
        Text(material.title, style = MaterialTheme.typography.displayMedium.copy(color = NeonGreen))

        Spacer(Modifier.height(8.dp))

        Text("${material.estimatedMinutes} min read  •  +${material.xpReward} XP reward",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))

        Spacer(Modifier.height(20.dp))

        // Content
        GameCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(material.content, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Key points
        if (material.keyPoints.isNotEmpty()) {
            Text("Key Points", style = MaterialTheme.typography.titleLarge.copy(color = NeonGold))
            Spacer(Modifier.height(8.dp))
            material.keyPoints.forEach { point ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•", style = MaterialTheme.typography.bodyLarge.copy(color = NeonGold))
                    Spacer(Modifier.width(8.dp))
                    Text(point, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Examples
        if (material.examples.isNotEmpty()) {
            Text("Examples", style = MaterialTheme.typography.titleLarge.copy(color = NeonPurple))
            Spacer(Modifier.height(8.dp))
            material.examples.forEach { example ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .background(NeonPurple.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(example, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Complete button
        NeonButton(
            "Mark as Studied",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            color = NeonGreen
        )

        Spacer(Modifier.height(24.dp))
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
