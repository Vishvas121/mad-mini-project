package com.example.gamefiedsarvya.ui.assets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════════════════════
//  HEALTH BAR  – glowing red/blue futuristic style
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FuturisticHealthBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    height: Dp = 18.dp,
    isEnemy: Boolean = false
) {
    val fraction = (current.toFloat() / max).coerceIn(0f, 1f)
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "hp_bar"
    )

    val inf = rememberInfiniteTransition(label = "hp_glow")
    val glowPulse by inf.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "hp_pulse"
    )

    // Colour shifts red when low
    val barColor = when {
        fraction > 0.6f -> if (isEnemy) NeonRed else Color(0xFF00BFFF)
        fraction > 0.3f -> NeonOrange
        else            -> NeonRed
    }

    Canvas(modifier = modifier.height(height).fillMaxWidth()) {
        val w = size.width; val h = size.height
        val barW = w * animFraction
        val cornerR = h / 2

        // ── Outer shell ───────────────────────────────────────────────────────
        drawRoundRect(Color(0xFF0A0A18), size = Size(w, h), cornerRadius = CornerRadius(cornerR))
        drawRoundRect(
            barColor.copy(alpha = 0.2f),
            size = Size(w, h), cornerRadius = CornerRadius(cornerR),
            style = Stroke(1.5f)
        )

        // ── Fill gradient ─────────────────────────────────────────────────────
        if (barW > 0f) {
            drawRoundRect(
                Brush.horizontalGradient(
                    listOf(barColor.copy(alpha = 0.7f), barColor, barColor.copy(alpha = 0.9f)),
                    startX = 0f, endX = barW
                ),
                size = Size(barW, h), cornerRadius = CornerRadius(cornerR)
            )

            // Shine strip
            drawRoundRect(
                Brush.horizontalGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                    startX = 0f, endX = barW * 0.4f
                ),
                topLeft = Offset(0f, 1f),
                size = Size(barW, h * 0.4f),
                cornerRadius = CornerRadius(cornerR)
            )

            // Glow edge at fill tip
            drawCircle(
                barColor.copy(alpha = 0.6f * glowPulse),
                radius = h * 0.8f,
                center = Offset(barW, h / 2)
            )
        }

        // ── Segment dividers ──────────────────────────────────────────────────
        val segments = 10
        for (i in 1 until segments) {
            val sx = w * i / segments
            drawLine(Color(0xFF0A0A18).copy(alpha = 0.8f), Offset(sx, 2f), Offset(sx, h - 2f), 1.5f)
        }

        // ── Outer glow ────────────────────────────────────────────────────────
        drawRoundRect(
            barColor.copy(alpha = 0.15f * glowPulse),
            topLeft = Offset(-3f, -3f),
            size = Size(w + 6f, h + 6f),
            cornerRadius = CornerRadius(cornerR + 3f),
            style = Stroke(3f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  XP BAR  – neon gradient fill with animated shimmer
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun NeonXpBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp
) {
    val fraction = (current.toFloat() / max).coerceIn(0f, 1f)
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "xp_bar"
    )

    val inf = rememberInfiniteTransition(label = "xp_shimmer")
    val shimmerX by inf.animateFloat(
        -1f, 2f,
        infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "shimmer"
    )

    Canvas(modifier = modifier.height(height).fillMaxWidth()) {
        val w = size.width; val h = size.height
        val barW = w * animFraction
        val cornerR = h / 2

        // Background
        drawRoundRect(Color(0xFF0A0A18), size = Size(w, h), cornerRadius = CornerRadius(cornerR))
        drawRoundRect(
            NeonGold.copy(alpha = 0.2f),
            size = Size(w, h), cornerRadius = CornerRadius(cornerR),
            style = Stroke(1f)
        )

        if (barW > 0f) {
            // Main gradient fill
            drawRoundRect(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFF6B00), NeonGold, Color(0xFFFFFF00), NeonGold),
                    startX = 0f, endX = barW
                ),
                size = Size(barW, h), cornerRadius = CornerRadius(cornerR)
            )

            // Shimmer sweep
            val shimmerStart = barW * (shimmerX - 0.3f)
            val shimmerEnd   = barW * (shimmerX + 0.3f)
            if (shimmerEnd > 0f && shimmerStart < barW) {
                drawRoundRect(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                        startX = shimmerStart.coerceAtLeast(0f),
                        endX   = shimmerEnd.coerceAtMost(barW)
                    ),
                    size = Size(barW, h), cornerRadius = CornerRadius(cornerR)
                )
            }

            // Star particles at tip
            for (i in 0..2) {
                val px = barW - i * 4f
                val py = h / 2 + (i - 1) * 2f
                drawCircle(NeonGold.copy(alpha = 0.8f - i * 0.25f), 1.5f, Offset(px, py))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  SKILL TREE NODES  – circular nodes with glowing connections
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillTreeCanvas(
    modifier: Modifier = Modifier,
    nodes: List<SkillNodeData>,
    connections: List<Pair<Int, Int>>   // index pairs
) {
    val inf = rememberInfiniteTransition(label = "skill_tree")
    val pulse by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "node_pulse"
    )
    val flowOffset by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "flow"
    )

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // ── Draw connections first ────────────────────────────────────────────
        connections.forEach { (fromIdx, toIdx) ->
            val from = nodes.getOrNull(fromIdx) ?: return@forEach
            val to   = nodes.getOrNull(toIdx)   ?: return@forEach
            val fx = from.x * w; val fy = from.y * h
            val tx = to.x * w;   val ty = to.y * h

            val bothUnlocked = from.isUnlocked && to.isUnlocked
            val lineColor = if (bothUnlocked) NeonPurple else TextMuted.copy(alpha = 0.3f)

            // Base line
            drawLine(lineColor.copy(alpha = 0.4f), Offset(fx, fy), Offset(tx, ty), 2f)

            // Animated energy flow along unlocked connections
            if (bothUnlocked) {
                val flowPos = flowOffset
                val dotX = fx + (tx - fx) * flowPos
                val dotY = fy + (ty - fy) * flowPos
                drawCircle(NeonPurple.copy(alpha = 0.9f), 4f, Offset(dotX, dotY))
                drawCircle(NeonPurple.copy(alpha = 0.3f), 8f, Offset(dotX, dotY))
            }
        }

        // ── Draw nodes ────────────────────────────────────────────────────────
        nodes.forEach { node ->
            val nx = node.x * w; val ny = node.y * h
            drawSkillNode(nx, ny, node, pulse)
        }
    }
}

data class SkillNodeData(
    val x: Float, val y: Float,
    val icon: String,
    val isUnlocked: Boolean,
    val canUnlock: Boolean,
    val label: String
)

private fun DrawScope.drawSkillNode(
    x: Float, y: Float,
    node: SkillNodeData,
    pulse: Float
) {
    val r = 24f
    val color = when {
        node.isUnlocked -> NeonPurple
        node.canUnlock  -> NeonGold
        else            -> TextMuted
    }

    // Outer glow ring (unlocked only)
    if (node.isUnlocked) {
        val glowR = r + 8f + pulse * 4f
        drawCircle(
            Brush.radialGradient(
                listOf(color.copy(alpha = 0.3f * (0.5f + pulse * 0.5f)), Color.Transparent),
                center = Offset(x, y), radius = glowR
            ),
            radius = glowR, center = Offset(x, y)
        )
    }

    // Outer ring
    drawCircle(color.copy(alpha = if (node.isUnlocked) 0.8f else 0.3f), r, Offset(x, y), style = Stroke(2f))

    // Fill
    drawCircle(
        Brush.radialGradient(
            listOf(color.copy(alpha = if (node.isUnlocked) 0.4f else 0.1f), Color(0xFF0A0A18)),
            center = Offset(x, y), radius = r
        ),
        radius = r, center = Offset(x, y)
    )

    // Inner ring
    drawCircle(color.copy(alpha = 0.3f), r * 0.65f, Offset(x, y), style = Stroke(1f))

    // Center dot
    drawCircle(color.copy(alpha = if (node.isUnlocked) 1f else 0.4f), 4f, Offset(x, y))

    // Can-unlock indicator (pulsing outer ring)
    if (node.canUnlock && !node.isUnlocked) {
        drawCircle(
            NeonGold.copy(alpha = 0.4f + pulse * 0.4f),
            r + 4f, Offset(x, y), style = Stroke(1.5f)
        )
    }
}
