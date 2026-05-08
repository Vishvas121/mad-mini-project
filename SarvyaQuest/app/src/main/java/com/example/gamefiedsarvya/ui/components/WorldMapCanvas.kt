package com.example.gamefiedsarvya.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.WorldMapRepository
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.*

/**
 * Interactive, animated world map canvas.
 * Renders nodes + connections for any TierWorldMap.
 * Tap a node to select it; callback fires on confirmed tap.
 */
@Composable
fun WorldMapCanvas(
    worldMap: TierWorldMap,
    completedNodeIds: Set<String>,
    selectedNodeId: String?,
    onNodeTap: (MapNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "map_anim")
    val pulse by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse), label = "pulse")
    val flowOffset by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "flow")

    // Resolve node states
    val resolvedNodes = remember(worldMap, completedNodeIds) {
        WorldMapRepository.getUnlockedNodes(worldMap.tier, completedNodeIds)
    }

    BoxWithConstraints(modifier = modifier) {
        val canvasW = constraints.maxWidth.toFloat()
        val canvasH = constraints.maxHeight.toFloat()

        // ── Connection lines ──────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            worldMap.connections.forEach { (fromId, toId) ->
                val from = resolvedNodes.find { it.id == fromId } ?: return@forEach
                val to   = resolvedNodes.find { it.id == toId }   ?: return@forEach
                val fx = from.posX * canvasW; val fy = from.posY * canvasH
                val tx = to.posX * canvasW;   val ty = to.posY * canvasH

                val bothUnlocked = from.isUnlocked && to.isUnlocked
                val lineColor = when {
                    from.isCompleted && to.isCompleted -> nodeColor(worldMap.tier, MapNodeType.COMPLETED)
                    bothUnlocked -> nodeColor(worldMap.tier, to.type).copy(alpha = 0.5f)
                    else -> Color.White.copy(alpha = 0.1f)
                }

                // Base line
                drawLine(lineColor, Offset(fx, fy), Offset(tx, ty), 2f,
                    pathEffect = if (!bothUnlocked) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null)

                // Animated energy dot on unlocked connections
                if (bothUnlocked && !from.isCompleted) {
                    val dotX = fx + (tx - fx) * flowOffset
                    val dotY = fy + (ty - fy) * flowOffset
                    drawCircle(lineColor.copy(alpha = 0.9f), 4f, Offset(dotX, dotY))
                    drawCircle(lineColor.copy(alpha = 0.3f), 9f, Offset(dotX, dotY))
                }
            }
        }

        // ── Tap detection overlay ─────────────────────────────────────────────
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(resolvedNodes) {
                detectTapGestures { tapOffset ->
                    val nodeRadius = 32f
                    resolvedNodes.forEach { node ->
                        if (!node.isUnlocked) return@forEach
                        val nx = node.posX * canvasW
                        val ny = node.posY * canvasH
                        val dist = sqrt((tapOffset.x - nx).pow(2) + (tapOffset.y - ny).pow(2))
                        if (dist <= nodeRadius * 1.5f) {
                            onNodeTap(node)
                            return@detectTapGestures
                        }
                    }
                }
            }
        )

        // ── Node composables ──────────────────────────────────────────────────
        // Use absoluteOffset with pixel values — canvasW/H are already in pixels
        val density = LocalDensity.current
        resolvedNodes.forEach { node ->
            val nxPx = node.posX * canvasW
            val nyPx = node.posY * canvasH
            val isSelected = node.id == selectedNodeId

            MapNodeComposable(
                node       = node,
                tier       = worldMap.tier,
                isSelected = isSelected,
                pulse      = pulse,
                modifier   = Modifier
                    .absoluteOffset(
                        x = with(density) { (nxPx - 28.dp.toPx()).toDp() },
                        y = with(density) { (nyPx - 28.dp.toPx()).toDp() }
                    )
                    .size(56.dp)
            )
        }
    }
}

@Composable
private fun MapNodeComposable(
    node: MapNode,
    tier: LearningTier,
    isSelected: Boolean,
    pulse: Float,
    modifier: Modifier = Modifier
) {
    val color = nodeColor(tier, if (node.isCompleted) MapNodeType.COMPLETED else node.type)
    val scale by animateFloatAsState(
        targetValue = when {
            isSelected       -> 1.2f
            node.isUnlocked  -> 1f + pulse * 0.05f
            else             -> 0.85f
        },
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "node_scale"
    )
    val alpha = if (node.isUnlocked) 1f else 0.35f

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        if (node.isUnlocked && !node.isCompleted) {
            Box(modifier = Modifier
                .size(56.dp)
                .background(
                    Brush.radialGradient(listOf(color.copy(alpha = 0.25f * (0.5f + pulse * 0.5f)), Color.Transparent)),
                    CircleShape
                )
            )
        }

        // Node circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (node.isCompleted)
                        Brush.radialGradient(listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.2f)))
                    else
                        Brush.radialGradient(listOf(color.copy(alpha = 0.3f), Color(0xFF0A0A14))),
                    CircleShape
                )
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else color.copy(alpha = 0.8f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    node.isCompleted -> "+"
                    !node.isUnlocked -> "X"
                    else             -> node.icon.take(2)
                },
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }

        // Boss indicator ring
        if (node.type == MapNodeType.BOSS && node.isUnlocked) {
            Box(modifier = Modifier
                .size(52.dp)
                .border(1.dp, color.copy(alpha = 0.5f + pulse * 0.4f), CircleShape)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun nodeColor(tier: LearningTier, type: MapNodeType): Color = when (type) {
    MapNodeType.LESSON    -> when (tier) {
        LearningTier.FOUNDATION   -> Color(0xFF00C896)
        LearningTier.ADVANCED     -> Color(0xFF2979FF)
        LearningTier.PROFESSIONAL -> Color(0xFFBF00FF)
    }
    MapNodeType.QUIZ      -> Color(0xFFFFB300)
    MapNodeType.BOSS      -> Color(0xFFFF073A)
    MapNodeType.HUB       -> Color(0xFF00F5FF)
    MapNodeType.COMPLETED -> Color(0xFF4CAF50)
    MapNodeType.LOCKED    -> Color(0xFF505070)
}
