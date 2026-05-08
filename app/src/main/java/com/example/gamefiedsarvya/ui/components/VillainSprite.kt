package com.example.gamefiedsarvya.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gamefiedsarvya.R
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.sin

enum class VillainDirection {
    SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST;

    @DrawableRes
    fun drawable(): Int = when (this) {
        SOUTH      -> R.drawable.villain_south
        SOUTH_EAST -> R.drawable.villain_south_east
        EAST       -> R.drawable.villain_east
        NORTH_EAST -> R.drawable.villain_north_east
        NORTH      -> R.drawable.villain_north
        NORTH_WEST -> R.drawable.villain_north_west
        WEST       -> R.drawable.villain_west
        SOUTH_WEST -> R.drawable.villain_south_west
    }
}

/**
 * Renders the genius villain sprite with a menacing red/purple glow.
 * Used in combat as the enemy visual for boss encounters.
 */
@Composable
fun VillainSprite(
    direction: VillainDirection = VillainDirection.SOUTH,
    size: Dp = 80.dp,
    glowColor: Color = NeonRed,
    showGlow: Boolean = true,
    floatAnim: Boolean = true,
    isBoss: Boolean = false,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "villain")

    val floatOffset by inf.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "villain_float"
    )

    val glowAlpha by inf.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "villain_glow"
    )

    // Boss gets extra menacing scale pulse
    val bossScale by inf.animateFloat(
        initialValue = 1f, targetValue = if (isBoss) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "boss_scale"
    )

    Box(
        modifier = modifier
            .size(size + if (showGlow) 20.dp else 0.dp)
            .offset(y = if (floatAnim) floatOffset.dp else 0.dp)
            .scale(bossScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(size + 16.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                glowColor.copy(alpha = glowAlpha * 0.5f),
                                NeonPurple.copy(alpha = glowAlpha * 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        // Boss gets a second outer ring
        if (isBoss && showGlow) {
            Box(
                modifier = Modifier
                    .size(size + 28.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                NeonRed.copy(alpha = glowAlpha * 0.2f),
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
            contentDescription = "Villain – ${direction.name}",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.size(size)
        )
    }
}

/**
 * Combat-sized villain with hit flash feedback.
 */
@Composable
fun CombatVillainSprite(
    direction: VillainDirection = VillainDirection.SOUTH,
    isBoss: Boolean = false,
    flashHit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val flashAlpha by animateFloatAsState(
        targetValue = if (flashHit) 0.7f else 0f,
        animationSpec = tween(150),
        label = "villain_flash"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        VillainSprite(
            direction = direction,
            size      = if (isBoss) 96.dp else 72.dp,
            glowColor = if (isBoss) NeonRed else NeonOrange,
            showGlow  = true,
            floatAnim = true,
            isBoss    = isBoss
        )
        // White flash on hit
        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(if (isBoss) 112.dp else 88.dp)
                    .background(Color.White.copy(alpha = flashAlpha), CircleShape)
            )
        }
    }
}
