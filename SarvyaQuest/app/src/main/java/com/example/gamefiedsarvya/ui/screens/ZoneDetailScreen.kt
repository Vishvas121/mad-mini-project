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
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.assets.ForestWorldBackground
import com.example.gamefiedsarvya.ui.assets.RuinsWorldBackground
import com.example.gamefiedsarvya.ui.assets.FortressWorldBackground
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel

@Composable
fun ZoneDetailScreen(
    zoneId: String,
    gameViewModel: GameViewModel,
    onEnemySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val progress by gameViewModel.progress.collectAsState()
    val enemies  = remember(zoneId) { gameViewModel.getEnemiesForZone(zoneId) }

    val (bgColor, accentColor, zoneName, zoneIcon) = when (zoneId) {
        "zone_forest"   -> Quadruple(ForestGreen,   ForestAccent,   "Verdant Forest",  "🌲")
        "zone_ruins"    -> Quadruple(RuinsGray,     RuinsAccent,    "Ancient Ruins",   "🏚")
        "zone_fortress" -> Quadruple(FortressBlack, FortressAccent, "Shadow Fortress", "🏰")
        else            -> Quadruple(DeepVoid,       NeonCyan,       "Unknown Zone",    "❓")
    }

    var npcVisible by remember { mutableStateOf(true) }
    val npcDialogue = remember(zoneId) {
        gameViewModel.getNpcDialogue(zoneId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ── World environment background ──────────────────────────────────────
        when (zoneId) {
            "zone_forest"   -> ForestWorldBackground(modifier = Modifier.fillMaxSize())
            "zone_ruins"    -> RuinsWorldBackground(modifier = Modifier.fillMaxSize())
            "zone_fortress" -> FortressWorldBackground(modifier = Modifier.fillMaxSize())
            else -> Box(modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(AbyssBlue, DeepVoid))))
        }
        // Dark overlay so UI stays readable
        Box(modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(zoneIcon, fontSize = 24.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        zoneName,
                        style = MaterialTheme.typography.headlineMedium.copy(color = accentColor)
                    )
                }
                NeonButton("NPC", onClick = { npcVisible = true }, color = NeonGold)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Choose your enemy",
                style = MaterialTheme.typography.titleLarge.copy(color = TextSecondary)
            )

            Spacer(Modifier.height(12.dp))

            enemies.forEach { enemy ->
                val isDefeated = enemy.id in progress.completedEnemyIds
                EnemyCard(
                    enemy      = enemy,
                    isDefeated = isDefeated,
                    accentColor = accentColor,
                    onFight    = { if (!isDefeated) onEnemySelected(enemy.id) }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))
        }

        // NPC dialogue
        if (npcVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                NpcDialogueBox(
                    npcName   = npcDialogue.npcName,
                    lines     = npcDialogue.lines,
                    onDismiss = { npcVisible = false }
                )
            }
        }
    }
}

@Composable
private fun EnemyCard(
    enemy: Enemy,
    isDefeated: Boolean,
    accentColor: Color,
    onFight: () -> Unit
) {
    val typeColor = when (enemy.type) {
        EnemyType.MINION -> NeonGreen
        EnemyType.ELITE  -> NeonOrange
        EnemyType.BOSS   -> NeonRed
    }
    val typeIcon = when (enemy.type) {
        EnemyType.MINION -> "👾"
        EnemyType.ELITE  -> "⚔"
        EnemyType.BOSS   -> "💀"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDefeated) TextMuted else typeColor.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .background(
                if (isDefeated) CardSurface.copy(alpha = 0.4f)
                else CardSurface,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isDefeated) { onFight() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(typeIcon, fontSize = 28.sp,
                    modifier = Modifier.alpha(if (isDefeated) 0.4f else 1f))
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            enemy.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (isDefeated) TextMuted else TextPrimary
                            )
                        )
                        if (enemy.isBoss) {
                            Spacer(Modifier.width(8.dp))
                            DifficultyBadge("BOSS", NeonRed)
                        }
                    }
                    Text(
                        "Topic: ${enemy.topic}  •  HP: ${enemy.maxHp}  •  ATK: ${enemy.attackPower}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (enemy.isBoss) {
                        Text(
                            "${enemy.totalPhases} phases",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonRed)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (isDefeated) {
                    Text("✓ DEFEATED", style = MaterialTheme.typography.labelSmall.copy(color = NeonGreen))
                } else {
                    Text(
                        "+${enemy.xpReward} XP",
                        style = MaterialTheme.typography.labelLarge.copy(color = XpGold)
                    )
                    Spacer(Modifier.height(4.dp))
                    DifficultyBadge(enemy.type.name, typeColor)
                }
            }
        }
    }
}

// Simple data class to avoid destructuring issues
private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component1() = a
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component2() = b
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component3() = c
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component4() = d
