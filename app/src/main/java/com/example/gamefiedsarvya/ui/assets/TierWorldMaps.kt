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
//  FOUNDATION MAP BACKGROUND  – colorful, warm, forest/city
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FoundationMapBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "fn_bg")
    val cloudX by inf.animateFloat(-0.1f, 1.1f,
        infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "cloud")
    val sunPulse by inf.animateFloat(0.8f, 1f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "sun")

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // Sky gradient
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1A3A5C), Color(0xFF0D2B1A), Color(0xFF0A1F12))))

        // Sun
        val sunX = w * 0.8f; val sunY = h * 0.12f
        drawCircle(Brush.radialGradient(
            listOf(Color(0xFFFFE066).copy(alpha = sunPulse), Color(0xFFFFB300).copy(alpha = 0.3f * sunPulse), Color.Transparent),
            center = Offset(sunX, sunY), radius = 60f
        ), 60f, Offset(sunX, sunY))
        drawCircle(Color(0xFFFFE066), 22f, Offset(sunX, sunY))

        // Clouds
        listOf(0.15f, 0.45f, 0.72f).forEachIndexed { i, baseX ->
            val cx = ((baseX + cloudX * 0.3f + i * 0.1f) % 1.1f) * w
            val cy = h * (0.08f + i * 0.04f)
            drawCloud(cx, cy, Color.White.copy(alpha = 0.18f))
        }

        // Rolling hills
        val hillPath = Path().apply {
            moveTo(0f, h * 0.55f)
            cubicTo(w * 0.15f, h * 0.42f, w * 0.30f, h * 0.50f, w * 0.45f, h * 0.44f)
            cubicTo(w * 0.60f, h * 0.38f, w * 0.75f, h * 0.48f, w, h * 0.42f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(hillPath, Brush.verticalGradient(
            listOf(Color(0xFF1B5E20), Color(0xFF0D3B14)), startY = h * 0.38f, endY = h
        ))

        // Foreground grass
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)), startY = h * 0.72f, endY = h
        ), topLeft = Offset(0f, h * 0.72f), size = Size(w, h * 0.28f))

        // Path/road
        val roadPath = Path().apply {
            moveTo(w * 0.45f, h)
            cubicTo(w * 0.47f, h * 0.85f, w * 0.50f, h * 0.78f, w * 0.52f, h * 0.72f)
        }
        drawPath(roadPath, Color(0xFFD4A017).copy(alpha = 0.5f), style = Stroke(12f, cap = StrokeCap.Round))

        // Trees
        listOf(0.1f, 0.2f, 0.78f, 0.88f).forEach { tx ->
            drawTree(w * tx, h * 0.68f, Color(0xFF2E7D32), Color(0xFF4CAF50))
        }

        // Stars (small dots in sky)
        repeat(30) { i ->
            val sx = (i * 137.5f) % w
            val sy = (i * 53.7f) % (h * 0.4f)
            drawCircle(Color.White.copy(alpha = 0.15f + (i % 3) * 0.08f), 1.5f, Offset(sx, sy))
        }
    }
}

private fun DrawScope.drawCloud(cx: Float, cy: Float, color: Color) {
    listOf(Offset(cx, cy), Offset(cx - 18f, cy + 8f), Offset(cx + 18f, cy + 8f),
        Offset(cx - 8f, cy + 14f), Offset(cx + 8f, cy + 14f)).forEach {
        drawCircle(color, 16f, it)
    }
}

private fun DrawScope.drawTree(x: Float, y: Float, dark: Color, light: Color) {
    drawRect(Color(0xFF5D4037), topLeft = Offset(x - 4f, y), size = Size(8f, 22f))
    drawCircle(dark, 20f, Offset(x, y - 8f))
    drawCircle(light.copy(alpha = 0.6f), 14f, Offset(x - 5f, y - 14f))
}

// ═══════════════════════════════════════════════════════════════════════════════
//  ADVANCED MAP BACKGROUND  – structured, blueprint/exam hall
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AdvancedMapBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "av_bg")
    val scanY by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "scan")
    val pulse by inf.animateFloat(0.5f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse), label = "pulse")

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // Deep navy base
        drawRect(Brush.verticalGradient(listOf(Color(0xFF040A14), Color(0xFF080E1A), Color(0xFF050C18))))

        // Blueprint grid
        val gridStep = 40f
        val gridColor = Color(0xFF2979FF).copy(alpha = 0.08f)
        var x = 0f
        while (x <= w) { drawLine(gridColor, Offset(x, 0f), Offset(x, h), 0.5f); x += gridStep }
        var y = 0f
        while (y <= h) { drawLine(gridColor, Offset(0f, y), Offset(w, y), 0.5f); y += gridStep }

        // Diagonal accent lines
        for (i in -5..15) {
            val lx = i * 80f
            drawLine(Color(0xFF2979FF).copy(alpha = 0.04f),
                Offset(lx, 0f), Offset(lx + h, h), 1f)
        }

        // Scan line
        val sy = scanY * h
        drawRect(Brush.verticalGradient(
            listOf(Color.Transparent, Color(0xFF2979FF).copy(alpha = 0.07f), Color.Transparent),
            startY = sy - 15f, endY = sy + 15f
        ), topLeft = Offset(0f, (sy - 15f).coerceAtLeast(0f)), size = Size(w, 30f))

        // Chapter divider lines
        listOf(0.25f, 0.50f, 0.75f).forEach { fy ->
            drawLine(Color(0xFF2979FF).copy(alpha = 0.2f * pulse),
                Offset(20f, h * fy), Offset(w - 20f, h * fy), 1f)
            drawLine(Color(0xFF00BCD4).copy(alpha = 0.1f),
                Offset(20f, h * fy + 3f), Offset(w - 20f, h * fy + 3f), 0.5f)
        }

        // Corner brackets
        val bLen = 20f; val bThick = 2f
        val corners = listOf(Offset(10f, 10f), Offset(w - 10f, 10f), Offset(10f, h - 10f), Offset(w - 10f, h - 10f))
        corners.forEach { c ->
            val sx = if (c.x < w / 2) 1f else -1f
            val sy2 = if (c.y < h / 2) 1f else -1f
            drawLine(Color(0xFF2979FF).copy(alpha = 0.6f), c, Offset(c.x + sx * bLen, c.y), bThick)
            drawLine(Color(0xFF2979FF).copy(alpha = 0.6f), c, Offset(c.x, c.y + sy2 * bLen), bThick)
        }

        // Ambient glow
        drawCircle(Brush.radialGradient(
            listOf(Color(0xFF2979FF).copy(alpha = 0.06f), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.3f), radius = w * 0.6f
        ), w * 0.6f, Offset(w * 0.5f, h * 0.3f))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  PROFESSIONAL MAP BACKGROUND  – network/circuit, dark minimal
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProfessionalMapBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "pr_bg")
    val flow by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "flow")
    val flicker by inf.animateFloat(0.85f, 1f,
        infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse), label = "flicker")

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // Near-black base
        drawRect(Color(0xFF050508))

        // Hex grid (approximated with lines)
        val hexSize = 35f
        val rows = (h / hexSize).toInt() + 2
        val cols = (w / hexSize).toInt() + 2
        for (r in 0..rows) for (c in 0..cols) {
            val hx = c * hexSize * 1.5f
            val hy = r * hexSize * 1.73f + (if (c % 2 == 0) 0f else hexSize * 0.87f)
            drawHexOutline(hx, hy, hexSize * 0.48f, Color(0xFFBF00FF).copy(alpha = 0.04f))
        }

        // Circuit traces
        val traces = listOf(
            listOf(Offset(0f, h * 0.3f), Offset(w * 0.2f, h * 0.3f), Offset(w * 0.2f, h * 0.5f), Offset(w * 0.5f, h * 0.5f)),
            listOf(Offset(w, h * 0.6f), Offset(w * 0.8f, h * 0.6f), Offset(w * 0.8f, h * 0.4f), Offset(w * 0.5f, h * 0.4f)),
            listOf(Offset(w * 0.3f, 0f), Offset(w * 0.3f, h * 0.2f), Offset(w * 0.6f, h * 0.2f), Offset(w * 0.6f, h * 0.7f))
        )
        traces.forEach { pts ->
            for (i in 0 until pts.size - 1) {
                drawLine(Color(0xFFBF00FF).copy(alpha = 0.12f * flicker), pts[i], pts[i + 1], 1f)
            }
            // Animated dot flowing along trace
            val totalLen = pts.size - 1
            val segIdx = (flow * totalLen).toInt().coerceIn(0, totalLen - 1)
            val segFrac = (flow * totalLen) - segIdx
            val p1 = pts[segIdx]; val p2 = pts[segIdx + 1]
            val dotX = p1.x + (p2.x - p1.x) * segFrac
            val dotY = p1.y + (p2.y - p1.y) * segFrac
            drawCircle(Color(0xFFBF00FF).copy(alpha = 0.7f), 3f, Offset(dotX, dotY))
            drawCircle(Color(0xFFBF00FF).copy(alpha = 0.2f), 7f, Offset(dotX, dotY))
        }

        // Ambient purple glow
        drawCircle(Brush.radialGradient(
            listOf(Color(0xFFBF00FF).copy(alpha = 0.07f * flicker), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.55f
        ), w * 0.55f, Offset(w * 0.5f, h * 0.5f))
    }
}

private fun DrawScope.drawHexOutline(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    for (i in 0..5) {
        val angle = (i * 60f - 30f) * PI.toFloat() / 180f
        val px = cx + r * cos(angle); val py = cy + r * sin(angle)
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color, style = Stroke(0.5f))
}
