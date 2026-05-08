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
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════════════════
//  ATTACK EFFECT  – glowing energy slash
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun EnergySlashEffect(
    visible: Boolean,
    isCritical: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "slash_progress"
    )

    if (progress == 0f) return

    val slashColor = if (isCritical) NeonGold else NeonCyan

    Canvas(modifier = modifier.size(160.dp, 80.dp)) {
        val w = size.width; val h = size.height
        val alpha = if (progress < 0.7f) progress / 0.7f else (1f - progress) / 0.3f

        // ── Main slash arc ────────────────────────────────────────────────────
        val slashPath = Path().apply {
            val startX = w * 0.05f * progress
            val startY = h * 0.8f
            val ctrlX  = w * 0.5f
            val ctrlY  = h * 0.1f
            val endX   = w * 0.95f * progress
            val endY   = h * 0.3f
            moveTo(startX, startY)
            quadraticTo(ctrlX, ctrlY, endX, endY)
        }

        // Glow layers (wide → narrow)
        for (strokeW in listOf(18f, 10f, 5f, 2f)) {
            val a = alpha * when (strokeW) {
                18f -> 0.15f; 10f -> 0.3f; 5f -> 0.6f; else -> 1f
            }
            drawPath(slashPath, slashColor.copy(alpha = a), style = Stroke(strokeW, cap = StrokeCap.Round))
        }

        // ── Secondary slash (offset) ──────────────────────────────────────────
        val slash2 = Path().apply {
            moveTo(w * 0.1f * progress, h * 0.9f)
            quadraticTo(w * 0.55f, h * 0.2f, w * 0.9f * progress, h * 0.4f)
        }
        drawPath(slash2, slashColor.copy(alpha = alpha * 0.4f), style = Stroke(3f, cap = StrokeCap.Round))

        // ── Spark particles at slash tip ──────────────────────────────────────
        if (progress > 0.3f) {
            val tipX = w * 0.95f * progress
            val tipY = h * 0.3f
            for (i in 0..7) {
                val angle = (i * 45f + progress * 180f) * PI.toFloat() / 180f
                val dist  = 15f + progress * 20f
                val px = tipX + cos(angle) * dist
                val py = tipY + sin(angle) * dist
                drawCircle(slashColor.copy(alpha = alpha * 0.8f), 2.5f, Offset(px, py))
            }
        }

        // ── Critical hit extra ring ───────────────────────────────────────────
        if (isCritical && progress > 0.2f) {
            val cx = w * 0.5f; val cy = h * 0.5f
            val ringR = progress * 50f
            drawCircle(NeonGold.copy(alpha = alpha * 0.5f), ringR, Offset(cx, cy), style = Stroke(3f))
            drawCircle(NeonGold.copy(alpha = alpha * 0.2f), ringR + 8f, Offset(cx, cy), style = Stroke(1.5f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  HIT EFFECT  – spark / explosion impact
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun HitImpactEffect(
    visible: Boolean,
    isPlayerHit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "hit_progress"
    )

    if (progress == 0f) return

    val hitColor = if (isPlayerHit) NeonRed else NeonOrange
    val alpha = if (progress < 0.5f) progress * 2f else (1f - progress) * 2f

    // Stable spark positions (seeded so they don't change per frame)
    val sparks = remember {
        List(12) {
            val angle = (it * 30f) * PI.toFloat() / 180f
            val speed = 30f + (it % 4) * 15f
            Pair(cos(angle) * speed, sin(angle) * speed)
        }
    }

    Canvas(modifier = modifier.size(120.dp)) {
        val cx = size.width / 2; val cy = size.height / 2

        // ── Shockwave ring ────────────────────────────────────────────────────
        val ringR = progress * 55f
        drawCircle(hitColor.copy(alpha = alpha * 0.6f), ringR, Offset(cx, cy), style = Stroke(3f))
        drawCircle(hitColor.copy(alpha = alpha * 0.2f), ringR + 6f, Offset(cx, cy), style = Stroke(1.5f))

        // ── Inner flash ───────────────────────────────────────────────────────
        if (progress < 0.4f) {
            drawCircle(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = alpha * 0.8f), hitColor.copy(alpha = alpha * 0.4f), Color.Transparent),
                    center = Offset(cx, cy), radius = 30f
                ),
                radius = 30f, center = Offset(cx, cy)
            )
        }

        // ── Spark lines ───────────────────────────────────────────────────────
        sparks.forEachIndexed { i, (vx, vy) ->
            val dist = progress
            val sx = cx + vx * dist
            val sy = cy + vy * dist
            val tailX = cx + vx * (dist - 0.15f).coerceAtLeast(0f)
            val tailY = cy + vy * (dist - 0.15f).coerceAtLeast(0f)
            val sparkAlpha = alpha * (0.6f + (i % 3) * 0.13f)
            drawLine(hitColor.copy(alpha = sparkAlpha), Offset(tailX, tailY), Offset(sx, sy), 2f)
            drawCircle(hitColor.copy(alpha = sparkAlpha), 2f, Offset(sx, sy))
        }

        // ── Debris chunks ─────────────────────────────────────────────────────
        for (i in 0..5) {
            val angle = (i * 60f + 15f) * PI.toFloat() / 180f
            val dist  = progress * 35f
            val dx = cx + cos(angle) * dist
            val dy = cy + sin(angle) * dist
            val chunkSize = 4f * (1f - progress * 0.5f)
            drawRect(
                hitColor.copy(alpha = alpha * 0.7f),
                topLeft = Offset(dx - chunkSize / 2, dy - chunkSize / 2),
                size = Size(chunkSize, chunkSize)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  COMBINED COMBAT EFFECT OVERLAY
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun CombatEffectOverlay(
    showAttack: Boolean,
    showHit: Boolean,
    isCritical: Boolean = false,
    isPlayerHit: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (showAttack) {
            EnergySlashEffect(
                visible    = showAttack,
                isCritical = isCritical,
                modifier   = Modifier.align(Alignment.Center)
            )
        }
        if (showHit) {
            HitImpactEffect(
                visible     = showHit,
                isPlayerHit = isPlayerHit,
                modifier    = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  STREAK FIRE EFFECT  – shown on 3+ consecutive correct answers
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun StreakFireEffect(streakCount: Int, modifier: Modifier = Modifier) {
    if (streakCount < 3) return

    val inf = rememberInfiniteTransition(label = "fire")
    val flicker by inf.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker"
    )
    val rise by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(600, easing = EaseInOutSine)),
        label = "rise"
    )

    Canvas(modifier = modifier.size(40.dp, 50.dp)) {
        val cx = size.width / 2
        val baseY = size.height

        // Flame layers
        val flames = listOf(
            Triple(cx, baseY - rise * 30f, NeonRed),
            Triple(cx - 5f, baseY - rise * 22f, NeonOrange),
            Triple(cx + 5f, baseY - rise * 25f, NeonOrange),
            Triple(cx, baseY - rise * 40f, NeonGold)
        )
        flames.forEach { (fx, fy, color) ->
            val flamePath = Path().apply {
                moveTo(fx - 8f, baseY)
                quadraticTo(fx - 12f, fy + 10f, fx, fy)
                quadraticTo(fx + 12f, fy + 10f, fx + 8f, baseY)
                close()
            }
            drawPath(flamePath, color.copy(alpha = flicker * 0.7f))
            drawCircle(color.copy(alpha = flicker * 0.4f), 10f, Offset(fx, fy))
        }

        // Streak count indicator
        if (streakCount >= 5) {
            drawCircle(NeonGold.copy(alpha = flicker), 4f, Offset(cx, baseY - 45f))
        }
    }
}
