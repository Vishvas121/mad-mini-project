package com.example.gamefiedsarvya.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.voice.VoiceState

// ── Mic Button ────────────────────────────────────────────────────────────────

@Composable
fun MicButton(
    voiceState: VoiceState,
    enabled: Boolean = true,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "mic")
    val pulseScale by inf.animateFloat(
        1f, 1.25f,
        infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "mic_pulse"
    )
    val ringAlpha by inf.animateFloat(
        0.4f, 0.9f,
        infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ring_alpha"
    )

    val isListening = voiceState == VoiceState.LISTENING
    val isProcessing = voiceState == VoiceState.PROCESSING
    val color = when (voiceState) {
        VoiceState.LISTENING   -> NeonRed
        VoiceState.PROCESSING  -> NeonOrange
        VoiceState.SPEAKING    -> NeonGreen
        VoiceState.ERROR       -> NeonRed.copy(alpha = 0.5f)
        VoiceState.IDLE        -> NeonCyan
    }

    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulse ring when listening
        if (isListening) {
            Box(modifier = Modifier
                .size(56.dp)
                .scale(pulseScale)
                .background(color.copy(alpha = ringAlpha * 0.3f), CircleShape)
            )
        }

        // Main button
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isListening) color.copy(alpha = 0.25f) else color.copy(alpha = 0.15f),
                    CircleShape
                )
                .border(2.dp, color.copy(alpha = if (enabled) 0.8f else 0.3f), CircleShape)
                .clickable(enabled = enabled && voiceState != VoiceState.PROCESSING) { onTap() },
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = color,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "Stop"
                        VoiceState.SPEAKING  -> "Spk"
                        else                 -> "Mic"
                    },
                    fontSize = 20.sp
                )
            }
        }
    }
}

// ── TTS Play Button ───────────────────────────────────────────────────────────

@Composable
fun SpeakButton(
    text: String,
    isSpeaking: Boolean,
    enabled: Boolean = true,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, NeonGreen.copy(alpha = if (enabled) 0.6f else 0.2f), RoundedCornerShape(8.dp))
            .background(NeonGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { if (isSpeaking) onStop() else onSpeak(text) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (isSpeaking) "Stop" else "Spk", fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                if (isSpeaking) "Stop" else "Play",
                style = MaterialTheme.typography.labelSmall.copy(color = NeonGreen)
            )
        }
    }
}

// ── Voice Status Banner ───────────────────────────────────────────────────────

@Composable
fun VoiceStatusBanner(
    voiceState: VoiceState,
    recognisedText: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = voiceState != VoiceState.IDLE,
        enter   = slideInVertically() + fadeIn(),
        exit    = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        val (color, label) = when (voiceState) {
            VoiceState.LISTENING   -> Pair(NeonRed,    "🎙 Listening…")
            VoiceState.PROCESSING  -> Pair(NeonOrange, "⏳ Processing…")
            VoiceState.SPEAKING    -> Pair(NeonGreen,  "🔊 Speaking…")
            VoiceState.ERROR       -> Pair(NeonRed,    "⚠ Voice error")
            VoiceState.IDLE        -> Pair(NeonCyan,   "")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingGlowDot(color)
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelLarge.copy(color = color))
            }
            if (recognisedText.isNotBlank() && voiceState == VoiceState.LISTENING) {
                Spacer(Modifier.height(4.dp))
                Text(recognisedText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
            }
        }
    }
}

// ── Badge Toast ───────────────────────────────────────────────────────────────

@Composable
fun BadgeToast(
    badge: com.example.gamefiedsarvya.data.models.Badge?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = badge != null,
        enter   = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit    = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        badge?.let { b ->
            LaunchedEffect(b) {
                kotlinx.coroutines.delay(3000)
                onDismiss()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.icon, fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("🏅 Badge Unlocked: ${b.title}",
                            style = MaterialTheme.typography.titleLarge.copy(color = NeonGold))
                        Text(b.description,
                            style = MaterialTheme.typography.bodyMedium)
                        Text("+${b.xpBonus} XP",
                            style = MaterialTheme.typography.labelSmall.copy(color = XpGold))
                    }
                }
            }
        }
    }
}
