package com.example.gamefiedsarvya.ui.assets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════════════════════
//  SARVYA QUEST  – futuristic neon glowing game logo
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SarvyaQuestLogo(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.LARGE
) {
    val inf = rememberInfiniteTransition(label = "logo")

    // Neon flicker
    val flicker by inf.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker"
    )

    // Slow colour cycle
    val colorCycle by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "color_cycle"
    )

    // Outer glow pulse
    val glowPulse by inf.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow_pulse"
    )

    // Scan line
    val scanLine by inf.animateFloat(
        -0.1f, 1.1f,
        infiniteRepeatable(tween(2500, easing = LinearEasing)),
        label = "scan"
    )

    val (canvasW, canvasH) = when (size) {
        LogoSize.SMALL  -> Pair(180.dp, 70.dp)
        LogoSize.MEDIUM -> Pair(280.dp, 100.dp)
        LogoSize.LARGE  -> Pair(320.dp, 130.dp)
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.size(canvasW, canvasH)) {
        val w = this.size.width
        val h = this.size.height

        // ── Background panel ──────────────────────────────────────────────────
        drawLogoBackground(w, h, glowPulse, colorCycle)

        // ── Decorative frame ──────────────────────────────────────────────────
        drawLogoFrame(w, h, flicker, colorCycle)

        // ── "SARVYA" text ─────────────────────────────────────────────────────
        val sarvyaFontSize = when (size) {
            LogoSize.SMALL  -> 28.sp
            LogoSize.MEDIUM -> 40.sp
            LogoSize.LARGE  -> 52.sp
        }
        val questFontSize = when (size) {
            LogoSize.SMALL  -> 14.sp
            LogoSize.MEDIUM -> 18.sp
            LogoSize.LARGE  -> 22.sp
        }

        // Glow layers for SARVYA
        val sarvyaStyle = TextStyle(
            fontSize     = sarvyaFontSize,
            fontWeight   = FontWeight.Black,
            letterSpacing = 6.sp,
            color        = NeonCyan
        )
        val sarvyaLayout = textMeasurer.measure("SARVYA", sarvyaStyle)
        val sarvyaX = (w - sarvyaLayout.size.width) / 2
        val sarvyaY = h * 0.12f

        // Glow shadow layers
        for (blur in listOf(16f, 10f, 6f)) {
            val glowAlpha = (0.15f + glowPulse * 0.1f) * (16f / blur) * flicker
            drawText(
                textMeasurer,
                "SARVYA",
                topLeft = Offset(sarvyaX + blur * 0.3f, sarvyaY + blur * 0.3f),
                style = sarvyaStyle.copy(color = NeonCyan.copy(alpha = glowAlpha))
            )
        }
        // Main text
        drawText(
            textMeasurer, "SARVYA",
            topLeft = Offset(sarvyaX, sarvyaY),
            style = sarvyaStyle.copy(color = NeonCyan.copy(alpha = flicker))
        )

        // ── "QUEST" text ──────────────────────────────────────────────────────
        val questStyle = TextStyle(
            fontSize     = questFontSize,
            fontWeight   = FontWeight.Bold,
            letterSpacing = 10.sp,
            color        = NeonPurple
        )
        val questLayout = textMeasurer.measure("QUEST", questStyle)
        val questX = (w - questLayout.size.width) / 2
        val questY = sarvyaY + sarvyaLayout.size.height + 2f

        // Glow layers for QUEST
        for (blur in listOf(12f, 7f)) {
            val glowAlpha = 0.2f * (12f / blur) * glowPulse * flicker
            drawText(
                textMeasurer, "QUEST",
                topLeft = Offset(questX + blur * 0.2f, questY + blur * 0.2f),
                style = questStyle.copy(color = NeonPurple.copy(alpha = glowAlpha))
            )
        }
        drawText(
            textMeasurer, "QUEST",
            topLeft = Offset(questX, questY),
            style = questStyle.copy(color = NeonPurple.copy(alpha = flicker))
        )

        // ── Scan line sweep ───────────────────────────────────────────────────
        val scanY = scanLine * h
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color.White.copy(alpha = 0.06f), Color.Transparent),
                startY = scanY - 8f, endY = scanY + 8f
            ),
            topLeft = Offset(0f, (scanY - 8f).coerceAtLeast(0f)),
            size = Size(w, 16f)
        )

        // ── Corner accent marks ───────────────────────────────────────────────
        drawLogoCorners(w, h, NeonCyan, flicker)

        // ── Subtitle tagline ──────────────────────────────────────────────────
        val tagStyle = TextStyle(
            fontSize     = 9.sp,
            fontWeight   = FontWeight.Medium,
            letterSpacing = 3.sp,
            color        = NeonGold.copy(alpha = 0.7f * flicker)
        )
        val tagLayout = textMeasurer.measure("AI-POWERED LEARNING RPG", tagStyle)
        drawText(
            textMeasurer, "AI-POWERED LEARNING RPG",
            topLeft = Offset((w - tagLayout.size.width) / 2, h - tagLayout.size.height - 6f),
            style = tagStyle
        )
    }
}

private fun DrawScope.drawLogoBackground(w: Float, h: Float, glow: Float, cycle: Float) {
    // Dark panel
    drawRoundRect(
        Color(0xFF050510),
        size = Size(w, h), cornerRadius = CornerRadius(12f)
    )
    // Gradient overlay
    drawRoundRect(
        Brush.linearGradient(
            listOf(
                NeonPurple.copy(alpha = 0.08f + glow * 0.04f),
                NeonCyan.copy(alpha = 0.06f + glow * 0.03f),
                NeonPurple.copy(alpha = 0.08f + glow * 0.04f)
            ),
            start = Offset(0f, 0f), end = Offset(w, h)
        ),
        size = Size(w, h), cornerRadius = CornerRadius(12f)
    )
    // Outer glow border
    drawRoundRect(
        Brush.linearGradient(
            listOf(NeonCyan.copy(alpha = 0.6f * glow), NeonPurple.copy(alpha = 0.6f * glow), NeonCyan.copy(alpha = 0.6f * glow)),
            start = Offset(0f, 0f), end = Offset(w, h)
        ),
        size = Size(w, h), cornerRadius = CornerRadius(12f),
        style = Stroke(1.5f)
    )
    // Double border
    drawRoundRect(
        NeonCyan.copy(alpha = 0.15f * glow),
        topLeft = Offset(3f, 3f),
        size = Size(w - 6f, h - 6f),
        cornerRadius = CornerRadius(9f),
        style = Stroke(0.5f)
    )
}

private fun DrawScope.drawLogoFrame(w: Float, h: Float, flicker: Float, cycle: Float) {
    // Horizontal divider lines
    val midY = h * 0.72f
    drawLine(NeonCyan.copy(alpha = 0.3f * flicker), Offset(16f, midY), Offset(w - 16f, midY), 0.5f)

    // Side accent bars
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent, NeonCyan.copy(alpha = 0.4f * flicker), Color.Transparent)
        ),
        topLeft = Offset(8f, h * 0.1f), size = Size(2f, h * 0.8f)
    )
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent, NeonPurple.copy(alpha = 0.4f * flicker), Color.Transparent)
        ),
        topLeft = Offset(w - 10f, h * 0.1f), size = Size(2f, h * 0.8f)
    )

    // Diamond accents
    val diamonds = listOf(Offset(w * 0.5f, 8f), Offset(w * 0.5f, h - 8f))
    diamonds.forEach { d ->
        val dp = Path().apply {
            moveTo(d.x, d.y - 5f); lineTo(d.x + 5f, d.y)
            lineTo(d.x, d.y + 5f); lineTo(d.x - 5f, d.y); close()
        }
        drawPath(dp, NeonGold.copy(alpha = 0.7f * flicker))
    }
}

private fun DrawScope.drawLogoCorners(w: Float, h: Float, color: Color, flicker: Float) {
    val len = 16f; val thick = 2f
    val corners = listOf(
        Pair(Offset(4f, 4f),     Pair(Offset(4f + len, 4f),     Offset(4f, 4f + len))),
        Pair(Offset(w - 4f, 4f), Pair(Offset(w - 4f - len, 4f), Offset(w - 4f, 4f + len))),
        Pair(Offset(4f, h - 4f), Pair(Offset(4f + len, h - 4f), Offset(4f, h - 4f - len))),
        Pair(Offset(w - 4f, h - 4f), Pair(Offset(w - 4f - len, h - 4f), Offset(w - 4f, h - 4f - len)))
    )
    corners.forEach { (corner, lines) ->
        drawLine(color.copy(alpha = flicker), corner, lines.first, thick)
        drawLine(color.copy(alpha = flicker), corner, lines.second, thick)
        drawCircle(color.copy(alpha = flicker), 2f, corner)
    }
}

enum class LogoSize { SMALL, MEDIUM, LARGE }
