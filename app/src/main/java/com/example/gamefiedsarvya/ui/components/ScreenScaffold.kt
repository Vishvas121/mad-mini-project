package com.example.gamefiedsarvya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gamefiedsarvya.ui.theme.DeepVoid

/**
 * Wraps every screen with correct status-bar and navigation-bar insets.
 * Prevents content from drawing behind the system UI.
 *
 * Usage:
 *   ScreenScaffold { Column { ... } }
 */
@Composable
fun ScreenScaffold(
    background: Color = DeepVoid,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = content
    )
}

/**
 * Lightweight version — only applies status bar padding (no nav bar).
 * Use for full-bleed screens that handle nav bar themselves.
 */
@Composable
fun ScreenWithStatusBar(
    background: Color = DeepVoid,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding(),
        content = content
    )
}
