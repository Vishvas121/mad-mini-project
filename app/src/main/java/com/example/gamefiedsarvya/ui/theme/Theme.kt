package com.example.gamefiedsarvya.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── CompositionLocal so any composable can read the live AppTheme ─────────────

val LocalAppTheme = staticCompositionLocalOf<AppTheme> { AppTheme.DARK_FANTASY }

/** Shortcut: read the current AppTheme from anywhere in the composition tree. */
val currentAppTheme: AppTheme
    @Composable @ReadOnlyComposable get() = LocalAppTheme.current

/**
 * Builds a MaterialTheme ColorScheme from an AppTheme.
 * primary/secondary/tertiary/background/surface/outline all come from the
 * selected theme, so every composable that uses MaterialTheme.colorScheme.*
 * automatically reflects the user's choice.
 */
private fun colorSchemeFor(theme: AppTheme): ColorScheme = darkColorScheme(
    primary          = theme.primary,
    onPrimary        = Color(0xFF050508),
    primaryContainer = theme.primary.copy(alpha = 0.15f),
    secondary        = theme.secondary,
    onSecondary      = Color(0xFFE8E8FF),
    secondaryContainer = theme.secondary.copy(alpha = 0.15f),
    tertiary         = theme.accent,
    onTertiary       = Color(0xFF050508),
    tertiaryContainer = theme.accent.copy(alpha = 0.15f),
    background       = theme.background,
    surface          = theme.surface,
    surfaceVariant   = theme.surface,
    onBackground     = Color(0xFFE8E8FF),
    onSurface        = Color(0xFFE8E8FF),
    onSurfaceVariant = Color(0xFF9090B0),
    error            = NeonRed,
    outline          = theme.border,
    outlineVariant   = theme.border.copy(alpha = 0.4f)
)

@Composable
fun GamefiedSarvyaTheme(
    appTheme: AppTheme = AppTheme.DARK_FANTASY,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorSchemeFor(appTheme),
            typography  = SarvyaTypography,
            content     = content
        )
    }
}
