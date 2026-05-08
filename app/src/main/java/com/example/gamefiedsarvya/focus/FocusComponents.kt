package com.example.gamefiedsarvya.focus

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamefiedsarvya.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// FocusMonitor
//
// Drop this into any screen to get live focus detection.
// Handles camera permission, starts/stops the detector with the lifecycle,
// and exposes the FocusViewModel to the content lambda.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FocusMonitor(
    enabled: Boolean = true,
    content: @Composable (focusVm: FocusViewModel) -> Unit
) {
    val focusVm: FocusViewModel = viewModel()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // Request permission and start detector
    LaunchedEffect(enabled, hasPermission) {
        if (!enabled) { focusVm.stopDetection(); return@LaunchedEffect }
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            focusVm.startDetection(lifecycle)
        }
    }

    DisposableEffect(Unit) {
        onDispose { focusVm.stopDetection() }
    }

    content(focusVm)
}

// ─────────────────────────────────────────────────────────────────────────────
// FocusIndicator
//
// Compact status bar shown at the top of the practice screen.
// Shows: focus score ring + alert state + session focus %.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FocusIndicator(
    focusVm: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val state by focusVm.state.collectAsState()
    if (!state.isActive) return

    val inf = rememberInfiniteTransition(label = "fi")
    val pulse by inf.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fp"
    )

    val ringColor = when (state.alertState) {
        AlertState.FOCUSED      -> NeonGreen
        AlertState.LOW_FOCUS    -> NeonOrange
        AlertState.DROWSY       -> NeonRed
        AlertState.DISTRACTED   -> NeonOrange
        AlertState.LOOKING_AWAY -> NeonOrange
        AlertState.NO_FACE      -> NeonRed
        AlertState.UNAVAILABLE  -> TextMuted
    }

    val animFocus by animateFloatAsState(state.smoothFocus, tween(600), label = "af")

    Row(
        modifier = modifier
            .background(CardSurface.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .border(1.dp, ringColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Focus ring
        Box(
            modifier = Modifier
                .size(28.dp)
                .scale(if (state.alertState == AlertState.FOCUSED) 1f else pulse),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress    = { animFocus },
                modifier    = Modifier.fillMaxSize(),
                color       = ringColor,
                trackColor  = ringColor.copy(alpha = 0.15f),
                strokeWidth = 3.dp
            )
            Text(
                state.alertState.emoji,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            )
        }

        Column {
            Text(
                state.alertState.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ringColor, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            )
            Text(
                "Focus: ${(state.smoothFocus * 100).toInt()}%  •  Session: ${state.focusPercent}%",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
            )
        }

        // Eye indicators
        if (state.latest.faceDetected) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                EyeDot(state.latest.leftEyeOpen)
                EyeDot(state.latest.rightEyeOpen)
            }
        }
    }
}

@Composable
private fun EyeDot(openProb: Float) {
    val color = if (openProb > 0.5f) NeonGreen else NeonRed
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color.copy(alpha = 0.8f), CircleShape)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// FocusAlertBanner
//
// Full-width dismissible banner that appears when focus drops.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FocusAlertBanner(
    focusVm: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val state by focusVm.state.collectAsState()

    AnimatedVisibility(
        visible = state.showAlert,
        enter   = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(250)),
        exit    = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        val (bgColor, message) = when (state.alertState) {
            AlertState.DROWSY       -> NeonRed    to "😴 You look drowsy — take a breath and refocus!"
            AlertState.DISTRACTED   -> NeonOrange to "👀 Head turned — stay focused on the screen!"
            AlertState.LOOKING_AWAY -> NeonOrange to "↩️ Look back at the screen to continue!"
            AlertState.NO_FACE      -> NeonRed    to "❓ No face detected — are you still there?"
            AlertState.LOW_FOCUS    -> NeonOrange to "😐 Focus dropping — you've got this!"
            else                    -> NeonGreen  to ""
        }

        if (message.isBlank()) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, bgColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clickable { focusVm.dismissAlert() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = bgColor),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "✕",
                    style = MaterialTheme.typography.labelMedium.copy(color = bgColor.copy(alpha = 0.7f))
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FocusStatsCard
//
// Detailed stats panel — shown in settings or end-of-session summary.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FocusStatsCard(
    focusVm: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val state by focusVm.state.collectAsState()
    if (!state.isActive && state.totalFrames == 0) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .background(CardSurface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🧠", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Text(
                "Focus Analytics",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = NeonCyan, fontWeight = FontWeight.Bold)
            )
        }

        FocusStatRow("Session Focus",   "${state.focusPercent}%",       NeonGreen)
        FocusStatRow("Current Focus",   "${(state.smoothFocus * 100).toInt()}%", NeonCyan)
        FocusStatRow("Engagement",      "${(state.smoothEngagement * 100).toInt()}%", NeonPurple)

        if (state.latest.faceDetected) {
            HorizontalDivider(color = CardBorder)
            FocusStatRow("Left Eye",  "${(state.latest.leftEyeOpen  * 100).toInt()}% open", NeonGold)
            FocusStatRow("Right Eye", "${(state.latest.rightEyeOpen * 100).toInt()}% open", NeonGold)
            FocusStatRow("Smile",     "${(state.latest.smileProbability * 100).toInt()}%",  NeonOrange)
            FocusStatRow("Head Turn", "${state.latest.headTurnDeg.toInt()}°",               TextSecondary)
        }
    }
}

@Composable
private fun FocusStatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(value, style = MaterialTheme.typography.bodySmall.copy(color = color, fontWeight = FontWeight.SemiBold))
    }
}
