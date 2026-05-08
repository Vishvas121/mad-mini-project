package com.example.gamefiedsarvya.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.gamefiedsarvya.data.models.LearningTier

/**
 * Per-tier visual identity.
 * Each tier has its own colour palette, typography weight, and UI density.
 * All existing dark-fantasy colours are preserved — these are additive.
 */
data class TierTheme(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val border: Color,
    val titleStyle: TextStyle,
    val bodyStyle: TextStyle,
    val labelStyle: TextStyle,
    val cardRadius: Float,
    val animationSpeed: Int   // ms for standard transitions
)

object TierThemes {

    // ── FOUNDATION – bright, warm, playful ───────────────────────────────────
    val Foundation = TierTheme(
        primary       = Color(0xFF00C896),
        secondary     = Color(0xFFFFB300),
        accent        = Color(0xFFFF6B6B),
        background    = Color(0xFF0D1F1A),
        surface       = Color(0xFF142B22),
        onSurface     = Color(0xFFE8FFF5),
        border        = Color(0xFF00C896).copy(alpha = 0.4f),
        titleStyle    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
        bodyStyle     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
        labelStyle    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
        cardRadius    = 16f,
        animationSpeed = 350
    )

    // ── ADVANCED – structured, exam-focused, cool blue ────────────────────────
    val Advanced = TierTheme(
        primary       = Color(0xFF2979FF),
        secondary     = Color(0xFF00BCD4),
        accent        = Color(0xFFFF9800),
        background    = Color(0xFF080E1A),
        surface       = Color(0xFF0F1829),
        onSurface     = Color(0xFFE3EEFF),
        border        = Color(0xFF2979FF).copy(alpha = 0.4f),
        titleStyle    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp),
        bodyStyle     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
        labelStyle    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
        cardRadius    = 10f,
        animationSpeed = 250
    )

    // ── PROFESSIONAL – minimal, productivity, monochrome + neon ──────────────
    val Professional = TierTheme(
        primary       = Color(0xFFBF00FF),
        secondary     = Color(0xFF00F5FF),
        accent        = Color(0xFFFFD700),
        background    = Color(0xFF050508),
        surface       = Color(0xFF0C0C14),
        onSurface     = Color(0xFFDDDDFF),
        border        = Color(0xFFBF00FF).copy(alpha = 0.35f),
        titleStyle    = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        bodyStyle     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
        labelStyle    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp),
        cardRadius    = 8f,
        animationSpeed = 180
    )

    fun forTier(tier: LearningTier): TierTheme = when (tier) {
        LearningTier.FOUNDATION   -> Foundation
        LearningTier.ADVANCED     -> Advanced
        LearningTier.PROFESSIONAL -> Professional
    }
}
