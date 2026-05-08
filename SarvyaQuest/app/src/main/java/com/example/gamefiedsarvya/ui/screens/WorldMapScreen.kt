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
import com.example.gamefiedsarvya.viewmodel.GameViewModel

@Composable
fun WorldMapScreen(
    gameViewModel: GameViewModel,
    onZoneSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState  by gameViewModel.uiState.collectAsState()
    val progress by gameViewModel.progress.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {

        // Starfield
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(60) { i ->
                val x = (i * 137.5f) % size.width
                val y = (i * 97.3f)  % size.height
                drawCircle(TextPrimary.copy(alpha = 0.08f + (i % 4) * 0.04f), 1.5f, Offset(x, y))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Text("WORLD MAP",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = NeonCyan, letterSpacing = 3.sp, fontWeight = FontWeight.Black
                    ))
                Spacer(Modifier.width(72.dp))
            }

            // ── Player stats card ─────────────────────────────────────────────
            GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGold.copy(alpha = 0.35f)) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroSprite(
                        direction = HeroDirection.SOUTH,
                        size      = 44.dp,
                        glowColor = NeonCyan,
                        showGlow  = true,
                        floatAnim = true
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(progress.player.name,
                            style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatChip("Lv ${progress.player.level}", NeonGold)
                            StatChip("❤ ${progress.player.currentHp}", HealthGreen)
                            StatChip("⚡ ${progress.player.currentEnergy}", EnergyBlue)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${progress.player.xp} XP",
                            style = MaterialTheme.typography.titleLarge.copy(color = XpGold))
                        Text("to next level",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Choose your destination",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextSecondary, textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            // ── Zone cards ────────────────────────────────────────────────────
            uiState.zones.forEach { zone ->
                ZoneCard(
                    zone      = zone,
                    completed = progress.completedEnemyIds,
                    onSelect  = {
                        if (zone.isUnlocked) {
                            gameViewModel.selectZone(zone.id)
                            onZoneSelected(zone.id)
                        }
                    }
                )
                Spacer(Modifier.height(14.dp))
            }

            // Bottom padding so last card isn't flush with nav bar
            Spacer(Modifier.height(24.dp))
        }

        // NPC dialogue overlay
        if (uiState.isNpcVisible) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                uiState.npcDialogue?.let { dialogue ->
                    NpcDialogueBox(
                        npcName   = dialogue.npcName,
                        lines     = dialogue.lines,
                        onDismiss = { gameViewModel.dismissNpcDialogue() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneCard(zone: Zone, completed: Set<String>, onSelect: () -> Unit) {
    val (bgColor, accentColor, icon) = when (zone.type) {
        ZoneType.FOREST   -> Triple(ForestGreen.copy(alpha = 0.25f),   ForestAccent,   "🌲")
        ZoneType.RUINS    -> Triple(RuinsGray.copy(alpha = 0.25f),     RuinsAccent,    "🏚")
        ZoneType.FORTRESS -> Triple(FortressBlack.copy(alpha = 0.45f), FortressAccent, "🏰")
    }

    val enemiesDefeated = zone.enemies.count { it.id in completed }
    val totalEnemies    = zone.enemies.size
    val pct = if (totalEnemies > 0) enemiesDefeated.toFloat() / totalEnemies else 0f

    val scale by animateFloatAsState(if (zone.isUnlocked) 1f else 0.98f, label = "zone_s")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = 1.5.dp,
                color = if (zone.isUnlocked) accentColor.copy(alpha = 0.7f) else TextMuted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                if (zone.isUnlocked) bgColor else CardSurface.copy(alpha = 0.4f),
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = zone.isUnlocked) { onSelect() }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            // Top row: icon + name + lock/difficulty badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 34.sp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        zone.type.displayName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = if (zone.isUnlocked) accentColor else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        zone.type.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
                if (!zone.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .background(TextMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🔒 LOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                } else {
                    DifficultyBadge(
                        label = when (zone.type) {
                            ZoneType.FOREST   -> "EASY"
                            ZoneType.RUINS    -> "MEDIUM"
                            ZoneType.FORTRESS -> "HARD"
                        },
                        color = when (zone.type) {
                            ZoneType.FOREST   -> NeonGreen
                            ZoneType.RUINS    -> NeonOrange
                            ZoneType.FORTRESS -> NeonRed
                        }
                    )
                }
            }

            // Progress section (unlocked only)
            if (zone.isUnlocked) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$enemiesDefeated / $totalEnemies enemies defeated",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    Text("${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(color = accentColor))
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(CardBorder, RoundedCornerShape(3.dp))
                ) {
                    val animPct by animateFloatAsState(pct, tween(600), label = "zone_pct")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animPct)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor)),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(color = color),
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}
