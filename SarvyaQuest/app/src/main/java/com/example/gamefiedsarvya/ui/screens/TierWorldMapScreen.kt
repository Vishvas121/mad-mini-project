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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.WorldMapRepository
import com.example.gamefiedsarvya.ui.assets.*
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel

/**
 * NEW: Tier-specific interactive world map.
 * Each tier renders its own background, node style, and content.
 */
@Composable
fun TierWorldMapScreen(
    tier: LearningTier,
    gameViewModel: GameViewModel,
    hubViewModel: LearningHubViewModel,
    onNodeSelected: (MapNode) -> Unit,
    onBack: () -> Unit
) {
    val progress  by gameViewModel.progress.collectAsState()
    val hubState  by hubViewModel.uiState.collectAsState()
    val theme     = com.example.gamefiedsarvya.ui.theme.TierThemes.forTier(tier)

    val worldMap  = remember(tier) { WorldMapRepository.getMapForTier(tier) }
    val completedIds = remember { mutableStateOf(setOf<String>()) }  // TODO: wire to persistence

    var selectedNode by remember { mutableStateOf<MapNode?>(null) }
    var showNodeDetail by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Tier-specific background ──────────────────────────────────────────
        when (tier) {
            LearningTier.FOUNDATION   -> FoundationMapBackground(Modifier.fillMaxSize())
            LearningTier.ADVANCED     -> AdvancedMapBackground(Modifier.fillMaxSize())
            LearningTier.PROFESSIONAL -> ProfessionalMapBackground(Modifier.fillMaxSize())
        }

        // Readability overlay
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        worldMap.title,
                        style = theme.titleStyle.copy(color = theme.primary)
                    )
                    Text(
                        "${tier.icon} ${tier.displayName}",
                        style = theme.labelStyle.copy(color = theme.secondary)
                    )
                }

                // Progress indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${completedIds.value.size}/${worldMap.nodes.size}",
                        style = theme.labelStyle.copy(color = theme.accent)
                    )
                    Text("nodes", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }

            // ── Legend ────────────────────────────────────────────────────────
            MapLegend(tier = tier, theme = theme)

            // ── Map canvas ────────────────────────────────────────────────────
            WorldMapCanvas(
                worldMap       = worldMap,
                completedNodeIds = completedIds.value,
                selectedNodeId = selectedNode?.id,
                onNodeTap      = { node ->
                    selectedNode = node
                    showNodeDetail = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // ── Bottom padding ────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
        }

        // ── Node detail panel ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showNodeDetail && selectedNode != null,
            enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedNode?.let { node ->
                NodeDetailPanel(
                    node     = node,
                    tier     = tier,
                    theme    = theme,
                    onStart  = {
                        showNodeDetail = false
                        onNodeSelected(node)
                    },
                    onStudy  = {
                        showNodeDetail = false
                        // Navigate to Learning Hub for this topic
                    },
                    onDismiss = { showNodeDetail = false }
                )
            }
        }
    }
}

// ── Map Legend ────────────────────────────────────────────────────────────────

@Composable
private fun MapLegend(tier: LearningTier, theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LegendItem("Lesson", theme.primary)
        LegendItem("Quiz",   Color(0xFFFFB300))
        LegendItem("Boss",   Color(0xFFFF073A))
        LegendItem("Hub",    Color(0xFF00F5FF))
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

// ── Node Detail Panel ─────────────────────────────────────────────────────────

@Composable
private fun NodeDetailPanel(
    node: MapNode,
    tier: LearningTier,
    theme: com.example.gamefiedsarvya.ui.theme.TierTheme,
    onStart: () -> Unit,
    onStudy: () -> Unit,
    onDismiss: () -> Unit
) {
    val nodeColor = when (node.type) {
        MapNodeType.LESSON    -> theme.primary
        MapNodeType.QUIZ      -> Color(0xFFFFB300)
        MapNodeType.BOSS      -> Color(0xFFFF073A)
        MapNodeType.HUB       -> Color(0xFF00F5FF)
        else                  -> TextSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, theme.background.copy(alpha = 0.97f))),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .border(
                1.dp, nodeColor.copy(alpha = 0.4f),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            // Drag handle
            Box(modifier = Modifier
                .width(40.dp).height(4.dp)
                .background(TextMuted, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(node.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(node.label.replace("\n", " "),
                            style = theme.titleStyle.copy(color = nodeColor))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(node.description, style = theme.bodyStyle.copy(color = TextSecondary))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Topic: ${node.topic}",
                            style = theme.labelStyle.copy(color = theme.secondary))
                        Text("+${node.xpReward} XP",
                            style = theme.labelStyle.copy(color = Color(0xFFFFD700)))
                    }
                }
                NeonButton("✕", onClick = onDismiss, color = TextMuted)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (node.type == MapNodeType.HUB) {
                    NeonButton("📚 Open Hub", onClick = onStudy,
                        modifier = Modifier.weight(1f), color = Color(0xFF00F5FF))
                } else {
                    NeonButton("📚 Study First", onClick = onStudy,
                        modifier = Modifier.weight(1f), color = theme.secondary)
                    NeonButton(
                        text = when (node.type) {
                            MapNodeType.BOSS -> "⚔ Fight Boss"
                            MapNodeType.QUIZ -> "⏱ Start Quiz"
                            else             -> "▶ Start"
                        },
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        color = nodeColor
                    )
                }
            }
        }
    }
}
