package com.example.gamefiedsarvya.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Builds a MaterialTheme ColorScheme from an AppTheme.
 * This is what makes theme changes propagate to every screen.
 */
private fun colorSchemeFor(theme: AppTheme): ColorScheme = darkColorScheme(
    primary          = theme.primary,
    onPrimary        = Color(0xFF050508),
    primaryContainer = theme.background,
    secondary        = theme.secondary,
    onSecondary      = Color(0xFFE8E8FF),
    tertiary         = theme.accent,
    background       = theme.background,
    surface          = theme.surface,
    onBackground     = Color(0xFFE8E8FF),
    onSurface        = Color(0xFFE8E8FF),
    error            = NeonRed,
    outline          = theme.border
)

@Composable
fun GamefiedSarvyaTheme(
    appTheme: AppTheme = AppTheme.DARK_FANTASY,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(appTheme),
        typography  = SarvyaTypography,
        content     = content
    )
}
