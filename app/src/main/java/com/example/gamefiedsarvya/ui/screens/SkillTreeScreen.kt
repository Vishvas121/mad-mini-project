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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.assets.NeonXpBar
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// River Progression Map
//
// A vertical scrollable screen showing the player's skill progression as a
// winding river. Each bend in the river holds a skill node. The river glows
// and flows with animation. Locked nodes are dark; unlocked ones pulse.
// ─────────────────────────────────────────────────────────────────────────────

// River waypoints — normalised X (0..1), absolute Y spacing handled by layout
private val RIVER_WAYPOINTS = listOf(
    0.50f, // top — centre
    0.25f, // bend left
    0.75f, // bend right
    0.30f, // bend left
    0.70f, // bend right
    0.50f  // bottom — centre (boss)
)

// Each skill node sits at a waypoint
private data class RiverNode(
    val skillNode: SkillNode,
    val waypointIndex: Int,
    val xpMilestone: Int,       // XP needed to reach this node
    val levelRequired: Int,
    val icon: String,
    val label: String
)

private fun buildRiverNodes(skillTree: List<SkillNode>): List<RiverNode> {
    val icons = mapOf(
        Ability.HINT_STRIKE   to "💡",
        Ability.TIME_FREEZE   to "❄️",
        Ability.DOUBLE_DAMAGE to "⚡",
        Ability.SHIELD_BLOCK  to "🛡️",
        Ability.MIND_SURGE    to "🌀"
    )
    return skillTree.mapIndexed { i, node ->
        RiverNode(
            skillNode     = node,
            waypointIndex = (i + 1).coerceAtMost(RIVER_WAYPOINTS.size - 1),
            xpMilestone   = node.requiredLevel * 80,
            levelRequired = node.requiredLevel,
            icon          = icons[node.ability] ?: "⭐",
            label         = node.ability.displayName
        )
    }
}

@Composable
fun SkillTreeScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit
) {
    val progress  by gameViewModel.progress.collectAsState()
    val skillTree = remember { gameViewModel.getSkillTree() }
    val player    = progress.player
    val nodes     = remember(skillTree) { buildRiverNodes(skillTree) }
    val appTheme  = LocalAppTheme.current

    var selectedNode by remember { mutableStateOf<RiverNode?>(null) }

    // Animated river flow
    val inf = rememberInfiniteTransition(label = "river")
    val flowOffset by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "flow"
    )
    val glowPulse by inf.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize().background(appTheme.background)) {

        // Starfield background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            repeat(80) { i ->
                val x = (i * 137.5f) % w
                val y = (i * 97.3f)  % h
                drawCircle(Color.White.copy(alpha = 0.04f + (i % 3) * 0.02f), 1.2f, Offset(x, y))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("← Back", onClick = onBack, color = TextSecondary)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SKILL RIVER",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = appTheme.primary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                    )
                    Text(
                        "Level ${player.level}  •  ${player.xp}/${player.xpToNextLevel} XP",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
                Spacer(Modifier.width(72.dp))
            }

            // XP bar
            NeonXpBar(
                current  = player.xp,
                max      = player.xpToNextLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // ── River map (scrollable) ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Fixed height canvas for the river path
                val canvasHeightDp = (nodes.size + 1) * 160
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(canvasHeightDp.dp)
                ) {
                    drawRiverPath(
                        nodes       = nodes,
                        player      = player,
                        flowOffset  = flowOffset,
                        glowPulse   = glowPulse,
                        primaryColor = appTheme.primary,
                        accentColor  = appTheme.accent
                    )
                }

                // Overlay: node buttons positioned over the canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(canvasHeightDp.dp)
                ) {
                    nodes.forEachIndexed { i, node ->
                        val isUnlocked = node.skillNode.ability in player.abilities
                        val canUnlock  = player.level >= node.levelRequired &&
                            node.skillNode.prerequisites.all { prereqId ->
                                skillTree.find { it.id == prereqId }?.ability in player.abilities
                            }
                        val isSelected = selectedNode?.skillNode?.id == node.skillNode.id

                        // Position: X from waypoint, Y evenly spaced
                        val xFraction = RIVER_WAYPOINTS.getOrElse(node.waypointIndex) { 0.5f }
                        val yOffset   = ((i + 1) * 160 - 40).dp

                        RiverNodeButton(
                            node       = node,
                            isUnlocked = isUnlocked,
                            canUnlock  = canUnlock,
                            isSelected = isSelected,
                            glowPulse  = glowPulse,
                            appTheme   = appTheme,
                            modifier   = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = 0.dp,  // handled by fillMaxWidth + padding below
                                    y = yOffset
                                )
                                .fillMaxWidth()
                                .wrapContentWidth(
                                    if (xFraction < 0.5f) Alignment.Start else Alignment.End
                                )
                                .padding(
                                    start = if (xFraction < 0.5f) (xFraction * 200f).dp else 0.dp,
                                    end   = if (xFraction >= 0.5f) ((1f - xFraction) * 200f).dp else 0.dp
                                ),
                            onClick = { selectedNode = if (isSelected) null else node }
                        )
                    }
                }
            }

            // ── Selected node detail panel ────────────────────────────────────
            selectedNode?.let { node ->
                val isUnlocked = node.skillNode.ability in player.abilities
                NodeDetailPanel(
                    node       = node,
                    isUnlocked = isUnlocked,
                    player     = player,
                    appTheme   = appTheme,
                    onDismiss  = { selectedNode = null }
                )
            }
        }
    }
}

// ── Canvas river path drawing ─────────────────────────────────────────────────

private fun DrawScope.drawRiverPath(
    nodes: List<RiverNode>,
    player: Player,
    flowOffset: Float,
    glowPulse: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val nodeSpacingPx = 160.dp.toPx()
    val nodeRadius    = 36.dp.toPx()

    // Capture DrawScope size before entering buildList lambda
    val canvasWidth  = size.width
    val canvasHeight = size.height

    // Build river control points
    val points = buildList<Offset> {
        add(Offset(canvasWidth / 2f, 40f))  // source
        nodes.forEachIndexed { i, node ->
            val x = RIVER_WAYPOINTS.getOrElse(node.waypointIndex) { 0.5f } * canvasWidth
            val y = (i + 1) * nodeSpacingPx
            add(Offset(x, y))
        }
        add(Offset(canvasWidth / 2f, nodes.size * nodeSpacingPx + nodeSpacingPx * 0.5f))
    }

    // Draw river glow (wide, soft)
    for (glow in 3 downTo 1) {
        val path = buildCatmullRomPath(points)
        drawPath(
            path  = path,
            color = primaryColor.copy(alpha = 0.04f * glow * glowPulse),
            style = Stroke(width = (nodeRadius * 1.2f * glow), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Draw river body
    val riverPath = buildCatmullRomPath(points)
    drawPath(
        path  = riverPath,
        color = primaryColor.copy(alpha = 0.25f),
        style = Stroke(width = nodeRadius * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Draw river edge lines (banks)
    drawPath(
        path  = riverPath,
        color = primaryColor.copy(alpha = 0.5f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f),
                phase = flowOffset * 20f))
    )

    // Animated flow dots along the river
    val totalDots = 8
    for (d in 0 until totalDots) {
        val t = ((flowOffset + d.toFloat() / totalDots) % 1f)
        val pos = interpolatePath(points, t)
        drawCircle(
            color  = accentColor.copy(alpha = 0.7f * (1f - abs(t - 0.5f) * 2f).coerceAtLeast(0.2f)),
            radius = 5f,
            center = pos
        )
    }

    // Draw node circles on the canvas (background rings)
    nodes.forEachIndexed { i, node ->
        val x = RIVER_WAYPOINTS.getOrElse(node.waypointIndex) { 0.5f } * canvasWidth
        val y = (i + 1) * nodeSpacingPx
        val isUnlocked = node.skillNode.ability in player.abilities
        val nodeColor  = if (isUnlocked) primaryColor else primaryColor.copy(alpha = 0.3f)

        // Outer glow ring
        if (isUnlocked) {
            drawCircle(nodeColor.copy(alpha = 0.15f * glowPulse), nodeRadius * 1.6f, Offset(x, y))
        }
        // Ring
        drawCircle(nodeColor.copy(alpha = if (isUnlocked) 0.8f else 0.3f),
            nodeRadius, Offset(x, y), style = Stroke(3f))
        // Fill
        drawCircle(
            color  = if (isUnlocked) nodeColor.copy(alpha = 0.2f) else Color(0xFF0A0A14).copy(alpha = 0.8f),
            radius = nodeRadius - 3f,
            center = Offset(x, y)
        )

        // XP milestone label line
        val labelX = if (x < canvasWidth / 2f) x + nodeRadius + 8f else x - nodeRadius - 8f
        drawLine(
            color       = nodeColor.copy(alpha = 0.4f),
            start       = Offset(x + (if (x < canvasWidth / 2f) nodeRadius else -nodeRadius), y),
            end         = Offset(labelX + (if (x < canvasWidth / 2f) 60f else -60f), y),
            strokeWidth = 1f
        )
    }

    // Source spring at top
    drawCircle(primaryColor.copy(alpha = 0.6f * glowPulse), 14f, Offset(canvasWidth / 2f, 40f))
    drawCircle(primaryColor.copy(alpha = 0.3f), 22f, Offset(canvasWidth / 2f, 40f), style = Stroke(2f))
}

// ── Catmull-Rom spline path builder ──────────────────────────────────────────

private fun buildCatmullRomPath(points: List<Offset>): Path {
    val path = Path()
    if (points.size < 2) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val p0 = points.getOrElse(i - 1) { points[i] }
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points.getOrElse(i + 2) { points[i + 1] }
        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f
        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
    return path
}

// ── Interpolate position along the river path ─────────────────────────────────

private fun interpolatePath(points: List<Offset>, t: Float): Offset {
    if (points.size < 2) return points.firstOrNull() ?: Offset.Zero
    val segments = points.size - 1
    val scaledT  = t * segments
    val seg      = scaledT.toInt().coerceIn(0, segments - 1)
    val localT   = scaledT - seg
    val p1 = points[seg]
    val p2 = points[seg + 1]
    return Offset(
        x = p1.x + (p2.x - p1.x) * localT,
        y = p1.y + (p2.y - p1.y) * localT
    )
}

// ── River node button (Compose overlay) ──────────────────────────────────────

@Composable
private fun RiverNodeButton(
    node: RiverNode,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    isSelected: Boolean,
    glowPulse: Float,
    appTheme: AppTheme,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val nodeColor = when {
        isUnlocked -> appTheme.primary
        canUnlock  -> appTheme.accent
        else       -> TextMuted
    }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else if (isUnlocked) 0.95f + glowPulse * 0.05f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "ns"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Node circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    if (isUnlocked) nodeColor.copy(alpha = 0.2f) else Color(0xFF0A0A14),
                    CircleShape
                )
                .border(
                    width = if (isSelected) 3.dp else 2.dp,
                    color = nodeColor.copy(alpha = if (isSelected) 1f else 0.7f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(node.icon, fontSize = 28.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(4.dp))

        // Label
        Text(
            node.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = nodeColor,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Level badge
        Box(
            modifier = Modifier
                .background(nodeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                "Lv ${node.levelRequired}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = nodeColor, fontSize = 9.sp)
            )
        }
    }
}

// ── Node detail panel ─────────────────────────────────────────────────────────

@Composable
private fun NodeDetailPanel(
    node: RiverNode,
    isUnlocked: Boolean,
    player: Player,
    appTheme: AppTheme,
    onDismiss: () -> Unit
) {
    val nodeColor = if (isUnlocked) appTheme.primary else appTheme.accent
    val canUnlock = player.level >= node.levelRequired

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(appTheme.surface, appTheme.background)
                )
            )
            .border(
                width = 1.dp,
                color = nodeColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(node.icon, fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        node.skillNode.ability.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = nodeColor, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        if (isUnlocked) "✓ Unlocked" else "Locked — Level ${node.levelRequired}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isUnlocked) NeonGreen else TextMuted)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(TextMuted.copy(alpha = 0.15f), CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            node.skillNode.ability.description,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoChip("⚡ ${node.skillNode.ability.energyCost} Energy", EnergyBlue)
            InfoChip("🎯 Level ${node.levelRequired}", NeonGold)
            if (!isUnlocked && canUnlock) InfoChip("Ready to unlock!", NeonGreen)
        }

        if (!isUnlocked) {
            Spacer(Modifier.height(10.dp))
            val xpNeeded = (node.levelRequired - player.level).coerceAtLeast(0) * 80
            val progress = if (xpNeeded == 0) 1f
                           else (player.xp.toFloat() / node.xpMilestone).coerceIn(0f, 1f)
            Text(
                "Progress to unlock",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CardBorder)
            ) {
                val animProg by animateFloatAsState(progress, tween(800), label = "prog")
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProg)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(listOf(nodeColor.copy(alpha = 0.7f), nodeColor))
                        )
                )
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(color = color, fontSize = 10.sp))
    }
}
