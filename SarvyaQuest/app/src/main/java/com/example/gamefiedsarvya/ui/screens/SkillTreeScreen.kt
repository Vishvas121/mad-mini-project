package com.example.gamefiedsarvya.ui.screens

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
import com.example.gamefiedsarvya.ui.assets.NeonXpBar
import com.example.gamefiedsarvya.ui.assets.SkillTreeCanvas
import com.example.gamefiedsarvya.ui.assets.SkillNodeData
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel

@Composable
fun SkillTreeScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit
) {
    val progress  by gameViewModel.progress.collectAsState()
    val skillTree = remember { gameViewModel.getSkillTree() }
    val player    = progress.player

    var selectedNode by remember { mutableStateOf<SkillNode?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {

        // Subtle grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 50f
            var x = 0f; while (x <= size.width)  { drawLine(NeonPurple.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height)); x += step }
            var y = 0f; while (y <= size.height) { drawLine(NeonPurple.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y)); y += step }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Text("SKILL TREE",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = NeonPurple, letterSpacing = 3.sp, fontWeight = FontWeight.Black
                    ))
                Spacer(Modifier.width(72.dp))
            }

            Text("Level ${player.level}  •  Tap a node to see details",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary, textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            // ── Skill nodes as scrollable list (more readable than canvas on mobile) ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                skillTree.forEach { node ->
                    val isUnlocked = node.ability in player.abilities
                    val canUnlock  = player.level >= node.requiredLevel &&
                        node.prerequisites.all { prereqId ->
                            skillTree.find { it.id == prereqId }?.ability in player.abilities
                        }
                    val isSelected = selectedNode?.id == node.id

                    SkillNodeCard(
                        node       = node,
                        isUnlocked = isUnlocked,
                        canUnlock  = canUnlock,
                        isSelected = isSelected,
                        onClick    = { selectedNode = if (isSelected) null else node }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Selected node detail ──────────────────────────────────────────
            selectedNode?.let { node ->
                val isUnlocked = node.ability in player.abilities
                Spacer(Modifier.height(8.dp))
                GameCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPurple.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(node.ability.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(color = NeonPurple))
                        Spacer(Modifier.height(6.dp))
                        Text(node.ability.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Unlock Level: ${node.requiredLevel}",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeonGold))
                            Text("Energy Cost: ${node.ability.energyCost}⚡",
                                style = MaterialTheme.typography.labelSmall.copy(color = EnergyBlue))
                        }
                        if (isUnlocked) {
                            Spacer(Modifier.height(8.dp))
                            Text("✓ UNLOCKED",
                                style = MaterialTheme.typography.labelLarge.copy(color = NeonGreen))
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (player.level < node.requiredLevel)
                                    "Reach Level ${node.requiredLevel} to unlock"
                                else "Prerequisites not met",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillNodeCard(
    node: SkillNode,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = when {
        isUnlocked -> NeonPurple
        canUnlock  -> NeonGold
        else       -> TextMuted
    }
    val icon = when (node.ability) {
        Ability.HINT_STRIKE   -> "💡"
        Ability.TIME_FREEZE   -> "❄️"
        Ability.DOUBLE_DAMAGE -> "⚡"
        Ability.SHIELD_BLOCK  -> "🛡️"
        Ability.MIND_SURGE    -> "🌀"
    }

    val inf = rememberInfiniteTransition(label = "node")
    val pulse by inf.animateFloat(0.8f, 1f,
        infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse), label = "p")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                if (isUnlocked) color.copy(alpha = 0.1f) else CardSurface,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(if (isUnlocked) pulse else 1f)
                .background(color.copy(alpha = if (isUnlocked) 0.25f else 0.1f), CircleShape)
                .border(1.5.dp, color.copy(alpha = if (isUnlocked) 0.8f else 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 22.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(node.ability.displayName,
                style = MaterialTheme.typography.titleLarge.copy(color = color))
            Spacer(Modifier.height(2.dp))
            Text(node.ability.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Lv ${node.requiredLevel}",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonGold))
                Text("${node.ability.energyCost}⚡",
                    style = MaterialTheme.typography.labelSmall.copy(color = EnergyBlue))
            }
        }

        // Status badge
        when {
            isUnlocked -> Text("✓", style = MaterialTheme.typography.headlineMedium.copy(color = NeonGreen))
            canUnlock  -> Text("!", style = MaterialTheme.typography.headlineMedium.copy(color = NeonGold))
            else       -> Text("🔒", fontSize = 18.sp)
        }
    }
}
