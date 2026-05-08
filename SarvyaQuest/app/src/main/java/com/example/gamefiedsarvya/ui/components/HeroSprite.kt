package com.example.gamefiedsarvya.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gamefiedsarvya.R
import com.example.gamefiedsarvya.ui.theme.*

// ── Direction enum ────────────────────────────────────────────────────────────

enum class HeroDirection {
    SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST;

    @DrawableRes
    fun drawable(): Int = when (this) {
        SOUTH      -> R.drawable.hero_south
        SOUTH_EAST -> R.drawable.hero_south_east
        EAST       -> R.drawable.hero_east
        NORTH_EAST -> R.drawable.hero_north_east
        NORTH      -> R.drawable.hero_north
        NORTH_WEST -> R.drawable.hero_north_west
        WEST       -> R.drawable.hero_west
        SOUTH_WEST -> R.drawable.hero_south_west
    }
}

// ── Hero Sprite composable ────────────────────────────────────────────────────

/**
 * Renders the cyber-hero sprite with an optional neon glow ring and idle float.
 *
 * @param direction   Which of the 8 directional frames to show
 * @param size        Rendered size (the source PNG is 68×68 px)
 * @param glowColor   Colour of the pulsing glow ring; null = no ring
 * @param floatAnim   Whether to apply the idle floating animation
 * @param modifier    Standard Compose modifier
 */
@Composable
fun HeroSprite(
    direction: HeroDirection = HeroDirection.SOUTH,
    size: Dp = 68.dp,
    glowColor: Color = NeonCyan,
    showGlow: Boolean = true,
    floatAnim: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")

    // Idle float
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue  =  4f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    // Glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue  = 0.85f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow"
    )

    Box(
        modifier = modifier
            .size(size + if (showGlow) 16.dp else 0.dp)
            .offset(y = if (floatAnim) floatOffset.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow ring
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(size + 12.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                glowColor.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        // Sprite
        Image(
            painter            = painterResource(id = direction.drawable()),
            contentDescription = "Hero – ${direction.name}",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.size(size)
        )
    }
}

// ── Combat-sized hero (larger, attack flash) ──────────────────────────────────

/**
 * Larger hero display used in the combat screen player panel.
 * Flashes white briefly on a correct answer.
 */
@Composable
fun CombatHeroSprite(
    direction: HeroDirection = HeroDirection.SOUTH,
    flashCorrect: Boolean = false,
    flashWrong: Boolean = false,
    modifier: Modifier = Modifier
) {
    val flashAlpha by animateFloatAsState(
        targetValue = if (flashCorrect || flashWrong) 0.6f else 0f,
        animationSpec = tween(200),
        label = "flash"
    )
    val flashColor = if (flashCorrect) NeonGreen else NeonRed

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        HeroSprite(
            direction  = direction,
            size       = 80.dp,
            glowColor  = if (flashCorrect) NeonGreen else if (flashWrong) NeonRed else NeonCyan,
            showGlow   = true,
            floatAnim  = true
        )
        // Flash overlay
        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(flashColor.copy(alpha = flashAlpha), CircleShape)
            )
        }
    }
}
