package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.ui.components.HeroDirection
import com.example.gamefiedsarvya.ui.components.HeroSprite
import com.example.gamefiedsarvya.ui.assets.SarvyaQuestLogo
import com.example.gamefiedsarvya.ui.assets.LogoSize
import com.example.gamefiedsarvya.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        visible = true
        delay(600)
        subtitleVisible = true
        delay(500)
        taglineVisible = true
        delay(1800)
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_bg")
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "bg_shift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ShadowPurple.copy(alpha = 0.8f + bgShift * 0.2f),
                        DeepVoid
                    ),
                    center = Offset(0.5f, 0.4f),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 60f
            for (x in 0..(size.width.toInt()) step step.toInt()) {
                drawLine(
                    color = NeonCyan.copy(alpha = 0.04f),
                    start = Offset(x.toFloat(), 0f),
                    end   = Offset(x.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height.toInt()) step step.toInt()) {
                drawLine(
                    color = NeonPurple.copy(alpha = 0.04f),
                    start = Offset(0f, y.toFloat()),
                    end   = Offset(size.width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game title
            AnimatedVisibility(
                visible = visible,
                enter   = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Hero sprite above title
                    HeroSprite(
                        direction = HeroDirection.SOUTH,
                        size      = 96.dp,
                        glowColor = NeonCyan,
                        showGlow  = true,
                        floatAnim = true
                    )
                    Spacer(Modifier.height(20.dp))
                    // Neon logo
                    SarvyaQuestLogo(size = LogoSize.LARGE)
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = subtitleVisible,
                enter   = fadeIn(tween(600)) + slideInVertically()
            ) {
                Text(
                    "THE KNOWLEDGE WARRIOR",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color        = NeonGold,
                        letterSpacing = 4.sp
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(
                visible = taglineVisible,
                enter   = fadeIn(tween(800))
            ) {
                Text(
                    "Learn. Fight. Evolve.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color     = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(Modifier.height(48.dp))

            AnimatedVisibility(visible = taglineVisible, enter = fadeIn(tween(1000))) {
                CircularProgressIndicator(
                    color     = NeonCyan.copy(alpha = 0.6f),
                    modifier  = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
