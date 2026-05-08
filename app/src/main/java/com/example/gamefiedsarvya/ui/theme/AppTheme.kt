package com.example.gamefiedsarvya.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Selectable app themes.
 * Each theme overrides the base dark-fantasy palette.
 */
enum class AppTheme(
    val displayName: String,
    val icon: String,
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val border: Color
) {
    DARK_FANTASY(
        "Dark Fantasy", "🌑",
        Color(0xFF0A0A14), Color(0xFF1C1F2E),
        NeonCyan, NeonPurple, NeonGold,
        Color(0xFF2A2D3E)
    ),
    NEON_CITY(
        "Neon City", "🌆",
        Color(0xFF050510), Color(0xFF0A0A20),
        Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFFFF00),
        Color(0xFF2A0A2A)
    ),
    FOREST_REALM(
        "Forest Realm", "🌿",
        Color(0xFF061206), Color(0xFF0D2010),
        Color(0xFF52B788), Color(0xFF95D5B2), Color(0xFFFFB300),
        Color(0xFF1B4332)
    ),
    OCEAN_DEEP(
        "Ocean Deep", "🌊",
        Color(0xFF020B18), Color(0xFF051525),
        Color(0xFF0096C7), Color(0xFF48CAE4), Color(0xFFADE8F4),
        Color(0xFF023E8A)
    ),
    CRIMSON_VOID(
        "Crimson Void", "🔴",
        Color(0xFF100505), Color(0xFF1A0808),
        Color(0xFFFF073A), Color(0xFFFF6B6B), Color(0xFFFFD700),
        Color(0xFF3A0A0A)
    ),
    MONOCHROME(
        "Monochrome", "⬛",
        Color(0xFF050505), Color(0xFF111111),
        Color(0xFFFFFFFF), Color(0xFFAAAAAA), Color(0xFF666666),
        Color(0xFF333333)
    );

    companion object {
        fun fromName(name: String): AppTheme =
            values().find { it.name == name } ?: DARK_FANTASY
    }
}
