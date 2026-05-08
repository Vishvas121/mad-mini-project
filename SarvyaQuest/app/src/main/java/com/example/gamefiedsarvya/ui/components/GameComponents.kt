package com.example.gamefiedsarvya.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.*

// ── Stat Bar (HP / Energy / XP) ───────────────────────────────────────────────

@Composable
fun StatBar(
    label: String,
    current: Int,
    max: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val fraction = (current.toFloat() / max).coerceIn(0f, 1f)
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "stat_bar"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("$current / $max", style = MaterialTheme.typography.labelSmall, color = color)
        }
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))
                    )
            )
        }
    }
}

// ── Neon Button ───────────────────────────────────────────────────────────────

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan,
    enabled: Boolean = true
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scale)
            .border(1.dp, if (enabled) color else TextMuted, RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor   = color,
            disabledContainerColor = CardBorder,
            disabledContentColor   = TextMuted
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge.copy(color = if (enabled) color else TextMuted)
        )
    }
}

// ── Game Card ─────────────────────────────────────────────────────────────────

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        content = content
    )
}

// ── Pulsing Glow Dot ──────────────────────────────────────────────────────────

@Composable
fun PulsingGlowDot(color: Color, size: Dp = 12.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// ── Particle Canvas ───────────────────────────────────────────────────────────

data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float, var maxLife: Float,
    val color: Color, val radius: Float
)

@Composable
fun ParticleEffect(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            drawCircle(
                color  = p.color.copy(alpha = alpha),
                radius = p.radius * alpha,
                center = Offset(p.x, p.y)
            )
        }
    }
}

// ── Screen Shake Modifier ─────────────────────────────────────────────────────

@Composable
fun Modifier.screenShake(active: Boolean): Modifier {
    val offsetX by animateFloatAsState(
        targetValue = if (active) 8f else 0f,
        animationSpec = if (active) spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness    = Spring.StiffnessHigh
        ) else snap(),
        label = "shake_x"
    )
    return this.offset(x = offsetX.dp)
}

// ── NPC Dialogue Box ──────────────────────────────────────────────────────────

@Composable
fun NpcDialogueBox(
    npcName: String,
    lines: List<String>,
    onDismiss: () -> Unit
) {
    var lineIndex by remember { mutableIntStateOf(0) }

    AnimatedVisibility(
        visible = true,
        enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                .background(AbyssBlue.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingGlowDot(NeonCyan)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        npcName,
                        style = MaterialTheme.typography.titleLarge.copy(color = NeonCyan)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    lines.getOrElse(lineIndex) { "" },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (lineIndex < lines.size - 1) {
                        NeonButton("Next ▶", onClick = { lineIndex++ }, color = NeonCyan)
                    } else {
                        NeonButton("Close", onClick = onDismiss, color = NeonPurple)
                    }
                }
            }
        }
    }
}

// ── Level Up Banner ───────────────────────────────────────────────────────────

@Composable
fun LevelUpBanner(level: Int, onDismiss: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "lvl_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            Text("✦ LEVEL UP ✦",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = NeonGold,
                    fontWeight = FontWeight.Black
                )
            )
            Spacer(Modifier.height(8.dp))
            Text("Level $level",
                style = MaterialTheme.typography.displayMedium.copy(color = NeonCyan)
            )
            Spacer(Modifier.height(4.dp))
            Text("New powers await!",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(24.dp))
            NeonButton("Continue", onClick = onDismiss, color = NeonGold)
        }
    }
}

// ── Boss Phase Banner ─────────────────────────────────────────────────────────

@Composable
fun BossPhaseBanner(phase: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠ PHASE $phase ⚠",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = NeonRed,
                    fontWeight = FontWeight.Black
                )
            )
            Spacer(Modifier.height(8.dp))
            Text("The enemy grows stronger!",
                style = MaterialTheme.typography.headlineMedium.copy(color = TextSecondary)
            )
        }
    }
}

// ── Difficulty Badge ──────────────────────────────────────────────────────────

@Composable
fun DifficultyBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}
