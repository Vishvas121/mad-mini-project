package com.example.gamefiedsarvya.ui.assets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════════════════════
//  FOREST WORLD  – glowing plants, soft lighting, tile-based
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ForestWorldBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "forest")
    val glowPulse by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )
    val sway by inf.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "sway"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ── Sky gradient ──────────────────────────────────────────────────────
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFF0A1A0A), Color(0xFF0D2B0D), Color(0xFF061206))
            )
        )

        // ── Tile grid (soft) ──────────────────────────────────────────────────
        val tileSize = 48f
        val cols = (w / tileSize).toInt() + 1
        val rows = (h / tileSize).toInt() + 1
        for (c in 0..cols) for (r in 0..rows) {
            drawRect(
                color = Color(0xFF0F2A0F).copy(alpha = 0.6f),
                topLeft = Offset(c * tileSize, r * tileSize),
                size = Size(tileSize - 1f, tileSize - 1f)
            )
        }

        // ── Ground moss patches ───────────────────────────────────────────────
        val mossCenters = listOf(
            Offset(w * 0.15f, h * 0.85f), Offset(w * 0.45f, h * 0.90f),
            Offset(w * 0.75f, h * 0.82f), Offset(w * 0.30f, h * 0.78f),
            Offset(w * 0.60f, h * 0.88f), Offset(w * 0.88f, h * 0.92f)
        )
        mossCenters.forEach { c ->
            drawOval(
                Brush.radialGradient(
                    listOf(ForestAccent.copy(alpha = 0.25f), Color.Transparent),
                    center = c, radius = 55f
                ),
                topLeft = Offset(c.x - 55f, c.y - 20f),
                size = Size(110f, 40f)
            )
        }

        // ── Glowing plants ────────────────────────────────────────────────────
        val plants = listOf(
            Triple(w * 0.10f, h * 0.75f, 0.8f),
            Triple(w * 0.25f, h * 0.70f, 1.0f),
            Triple(w * 0.50f, h * 0.72f, 0.6f),
            Triple(w * 0.68f, h * 0.68f, 0.9f),
            Triple(w * 0.85f, h * 0.74f, 0.7f),
            Triple(w * 0.38f, h * 0.80f, 1.0f),
            Triple(w * 0.92f, h * 0.65f, 0.5f)
        )
        plants.forEach { (px, py, phase) ->
            val alpha = 0.5f + 0.5f * sin((glowPulse + phase) * PI.toFloat())
            val swayX = sway * 3f * phase.toFloat()
            drawGlowingPlant(px + swayX, py, alpha, ForestAccent)
        }

        // ── Firefly particles ─────────────────────────────────────────────────
        val fireflies = listOf(
            Pair(w * 0.20f, h * 0.55f), Pair(w * 0.55f, h * 0.50f),
            Pair(w * 0.78f, h * 0.58f), Pair(w * 0.35f, h * 0.62f),
            Pair(w * 0.90f, h * 0.48f)
        )
        fireflies.forEachIndexed { i, (fx, fy) ->
            val a = 0.4f + 0.6f * sin((glowPulse * 2f + i * 0.7f) * PI.toFloat())
            drawCircle(NeonGreen.copy(alpha = a.toFloat()), 3f, Offset(fx, fy))
            drawCircle(NeonGreen.copy(alpha = a.toFloat() * 0.3f), 8f, Offset(fx, fy))
        }

        // ── Soft ambient light rays ───────────────────────────────────────────
        val rayAlpha = 0.04f + glowPulse * 0.03f
        for (i in 0..4) {
            val rx = w * (0.1f + i * 0.2f)
            drawRect(
                Brush.verticalGradient(
                    listOf(NeonGreen.copy(alpha = rayAlpha), Color.Transparent),
                    startY = 0f, endY = h * 0.6f
                ),
                topLeft = Offset(rx - 15f, 0f),
                size = Size(30f, h * 0.6f)
            )
        }
    }
}

private fun DrawScope.drawGlowingPlant(x: Float, y: Float, alpha: Float, color: Color) {
    // Stem
    drawLine(color.copy(alpha = alpha * 0.8f), Offset(x, y), Offset(x, y - 35f), strokeWidth = 2.5f)
    // Leaves
    for (i in -1..1 step 2) {
        val lx = x + i * 14f
        val ly = y - 20f
        drawOval(
            color.copy(alpha = alpha * 0.7f),
            topLeft = Offset(lx - 8f, ly - 6f), size = Size(16f, 12f)
        )
    }
    // Glow tip
    drawCircle(color.copy(alpha = alpha), 5f, Offset(x, y - 38f))
    drawCircle(color.copy(alpha = alpha * 0.3f), 12f, Offset(x, y - 38f))
}

// ═══════════════════════════════════════════════════════════════════════════════
//  RUINS WORLD  – broken pillars, glowing symbols, dark fantasy
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun RuinsWorldBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "ruins")
    val glowPulse by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ruins_glow"
    )
    val symbolRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "sym_rot"
    )

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // ── Dark sky ──────────────────────────────────────────────────────────
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0A0010), Color(0xFF150520), Color(0xFF0D0018))))

        // ── Stone floor tiles ─────────────────────────────────────────────────
        val tileW = 56f; val tileH = 28f
        val rows = (h / tileH).toInt() + 1
        val cols = (w / tileW).toInt() + 2
        for (r in (rows / 2)..rows) for (c in 0..cols) {
            val tx = (c - r % 2 * 0.5f) * tileW
            val ty = r * tileH
            drawRect(
                Color(0xFF1A1020).copy(alpha = 0.9f),
                topLeft = Offset(tx, ty), size = Size(tileW - 2f, tileH - 2f)
            )
            drawRect(
                Color(0xFF2A1A30).copy(alpha = 0.4f),
                topLeft = Offset(tx, ty), size = Size(tileW - 2f, 2f)
            )
        }

        // ── Broken pillars ────────────────────────────────────────────────────
        val pillars = listOf(
            Pair(w * 0.08f, h * 0.3f), Pair(w * 0.22f, h * 0.25f),
            Pair(w * 0.72f, h * 0.28f), Pair(w * 0.88f, h * 0.32f),
            Pair(w * 0.50f, h * 0.20f)
        )
        pillars.forEach { (px, py) ->
            drawBrokenPillar(px, py, h, RuinsAccent, glowPulse)
        }

        // ── Glowing rune symbols ──────────────────────────────────────────────
        val runes = listOf(
            Triple(w * 0.30f, h * 0.55f, 1.0f),
            Triple(w * 0.60f, h * 0.50f, 0.7f),
            Triple(w * 0.15f, h * 0.65f, 0.5f),
            Triple(w * 0.80f, h * 0.60f, 0.9f)
        )
        runes.forEach { (rx, ry, phase) ->
            val a = 0.4f + 0.6f * sin((glowPulse + phase) * PI.toFloat())
            drawRuneSymbol(rx, ry, a.toFloat(), RuinsAccent, symbolRot)
        }

        // ── Mist / fog at ground ──────────────────────────────────────────────
        val mistAlpha = 0.12f + glowPulse * 0.06f
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, RuinsAccent.copy(alpha = mistAlpha)),
                startY = h * 0.7f, endY = h
            )
        )

        // ── Distant purple glow ───────────────────────────────────────────────
        drawCircle(
            Brush.radialGradient(
                listOf(NeonPurple.copy(alpha = 0.08f + glowPulse * 0.04f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.3f), radius = w * 0.5f
            ),
            radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.3f)
        )
    }
}

private fun DrawScope.drawBrokenPillar(x: Float, baseY: Float, h: Float, color: Color, glow: Float) {
    val pillarH = h * 0.45f
    val pillarW = 22f
    // Main shaft
    drawRect(Color(0xFF2A2030), topLeft = Offset(x - pillarW / 2, baseY), size = Size(pillarW, pillarH))
    // Highlight edge
    drawRect(color.copy(alpha = 0.3f), topLeft = Offset(x - pillarW / 2, baseY), size = Size(3f, pillarH))
    // Broken top (jagged)
    val path = Path().apply {
        moveTo(x - pillarW / 2, baseY)
        lineTo(x - pillarW / 2 + 5f, baseY - 12f)
        lineTo(x, baseY - 6f)
        lineTo(x + 8f, baseY - 18f)
        lineTo(x + pillarW / 2, baseY - 8f)
        lineTo(x + pillarW / 2, baseY)
        close()
    }
    drawPath(path, Color(0xFF3A2A40))
    // Glow crack
    val crackAlpha = 0.3f + glow * 0.5f
    drawLine(color.copy(alpha = crackAlpha), Offset(x - 3f, baseY + 20f), Offset(x + 5f, baseY + 60f), 1.5f)
    drawLine(color.copy(alpha = crackAlpha * 0.6f), Offset(x + 5f, baseY + 60f), Offset(x - 2f, baseY + 90f), 1f)
}

private fun DrawScope.drawRuneSymbol(x: Float, y: Float, alpha: Float, color: Color, rot: Float) {
    val r = 18f
    // Outer ring
    drawCircle(color.copy(alpha = alpha * 0.6f), r, Offset(x, y), style = Stroke(1.5f))
    drawCircle(color.copy(alpha = alpha * 0.15f), r + 6f, Offset(x, y))
    // Inner star (6-pointed)
    val path = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((rot + i * 60.0)).toFloat()
        val px = x + cos(angle) * r * 0.7f
        val py = y + sin(angle) * r * 0.7f
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color.copy(alpha = alpha * 0.5f), style = Stroke(1f))
    drawCircle(color.copy(alpha = alpha), 3f, Offset(x, y))
}

// ═══════════════════════════════════════════════════════════════════════════════
//  FORTRESS WORLD  – dark futuristic metallic + neon lights
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FortressWorldBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "fortress")
    val neonFlicker by inf.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker"
    )
    val scanLine by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "scan"
    )

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // ── Deep black base ───────────────────────────────────────────────────
        drawRect(Brush.verticalGradient(listOf(Color(0xFF050508), Color(0xFF0A0A12), Color(0xFF060610))))

        // ── Metal floor panels ────────────────────────────────────────────────
        val panelH = 32f
        val panelRows = (h / panelH).toInt() + 1
        for (r in (panelRows / 2)..panelRows) {
            val ty = r * panelH
            drawRect(Color(0xFF0E0E18), topLeft = Offset(0f, ty), size = Size(w, panelH - 2f))
            drawLine(Color(0xFF1A1A2A), Offset(0f, ty), Offset(w, ty), 1f)
            // Neon edge strip every 3 rows
            if (r % 3 == 0) {
                drawLine(NeonCyan.copy(alpha = 0.15f * neonFlicker), Offset(0f, ty), Offset(w, ty), 1.5f)
            }
        }

        // ── Vertical metal columns ────────────────────────────────────────────
        val colPositions = listOf(0.05f, 0.25f, 0.50f, 0.75f, 0.95f)
        colPositions.forEach { cx ->
            val x = w * cx
            drawRect(Color(0xFF0C0C18), topLeft = Offset(x - 12f, 0f), size = Size(24f, h))
            drawLine(NeonCyan.copy(alpha = 0.25f * neonFlicker), Offset(x - 12f, 0f), Offset(x - 12f, h), 1f)
            drawLine(NeonCyan.copy(alpha = 0.25f * neonFlicker), Offset(x + 12f, 0f), Offset(x + 12f, h), 1f)
            // Neon strip on column
            drawRect(
                Brush.verticalGradient(
                    listOf(NeonCyan.copy(alpha = 0.4f * neonFlicker), NeonPurple.copy(alpha = 0.2f * neonFlicker)),
                    startY = 0f, endY = h
                ),
                topLeft = Offset(x - 2f, 0f), size = Size(4f, h)
            )
        }

        // ── Neon wall panels ──────────────────────────────────────────────────
        val panels = listOf(
            Pair(w * 0.15f, h * 0.15f), Pair(w * 0.60f, h * 0.12f),
            Pair(w * 0.38f, h * 0.20f), Pair(w * 0.82f, h * 0.18f)
        )
        panels.forEach { (px, py) ->
            drawFortressPanel(px, py, NeonCyan, NeonPurple, neonFlicker)
        }

        // ── Scan line effect ──────────────────────────────────────────────────
        val scanY = scanLine * h
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, NeonCyan.copy(alpha = 0.06f), Color.Transparent),
                startY = scanY - 20f, endY = scanY + 20f
            ),
            topLeft = Offset(0f, scanY - 20f), size = Size(w, 40f)
        )

        // ── Corner warning triangles ──────────────────────────────────────────
        drawFortressWarning(20f, 20f, NeonRed, neonFlicker)
        drawFortressWarning(w - 20f, 20f, NeonRed, neonFlicker)

        // ── Ambient red glow (danger zone) ────────────────────────────────────
        drawCircle(
            Brush.radialGradient(
                listOf(NeonRed.copy(alpha = 0.06f * neonFlicker), Color.Transparent),
                center = Offset(w * 0.5f, h), radius = w * 0.7f
            ),
            radius = w * 0.7f, center = Offset(w * 0.5f, h)
        )
    }
}

private fun DrawScope.drawFortressPanel(x: Float, y: Float, c1: Color, c2: Color, flicker: Float) {
    val pw = 80f; val ph = 40f
    drawRect(Color(0xFF0A0A18), topLeft = Offset(x - pw / 2, y), size = Size(pw, ph))
    drawRect(c1.copy(alpha = 0.4f * flicker), topLeft = Offset(x - pw / 2, y), size = Size(pw, 2f))
    drawRect(c2.copy(alpha = 0.3f * flicker), topLeft = Offset(x - pw / 2, y + ph - 2f), size = Size(pw, 2f))
    // Inner grid
    for (i in 1..3) {
        drawLine(c1.copy(alpha = 0.1f * flicker),
            Offset(x - pw / 2 + i * pw / 4, y), Offset(x - pw / 2 + i * pw / 4, y + ph), 0.5f)
    }
    drawCircle(c1.copy(alpha = 0.6f * flicker), 4f, Offset(x, y + ph / 2))
}

private fun DrawScope.drawFortressWarning(x: Float, y: Float, color: Color, flicker: Float) {
    val path = Path().apply {
        moveTo(x, y - 14f); lineTo(x + 12f, y + 8f); lineTo(x - 12f, y + 8f); close()
    }
    drawPath(path, color.copy(alpha = 0.5f * flicker), style = Stroke(1.5f))
    drawCircle(color.copy(alpha = 0.8f * flicker), 2f, Offset(x, y + 2f))
}
