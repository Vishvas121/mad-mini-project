package com.example.gamefiedsarvya.data.models

/**
 * User identity and personalisation profile.
 * Stored in DataStore, displayed across the entire app.
 */
data class UserProfile(
    val name: String = "",
    val avatarIndex: Int = 0,           // index into built-in avatar set
    val preferredLanguage: String = "en",
    val difficultyPreference: DifficultyPreference = DifficultyPreference.ADAPTIVE,
    val simpleMode: Boolean = false,    // larger text, fewer animations
    val highContrast: Boolean = false,
    val voiceEnabled: Boolean = false,
    val onboardingComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSetup: Boolean get() = name.isNotBlank() && onboardingComplete
    val displayName: String get() = name.ifBlank { "Warrior" }
    val greeting: String get() = "Welcome back, $displayName!"
}

enum class DifficultyPreference(val label: String) {
    EASY("Easy – I'm just starting"),
    ADAPTIVE("Adaptive – Let AI decide"),
    HARD("Hard – Challenge me!")
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String
)

val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("en", "English",    "English",    "🇬🇧"),
    LanguageOption("ta", "தமிழ்",      "Tamil",      "🇮🇳"),
    LanguageOption("hi", "हिन्दी",     "Hindi",      "🇮🇳"),
    LanguageOption("te", "తెలుగు",     "Telugu",     "🇮🇳"),
    LanguageOption("ml", "മലയാളം",    "Malayalam",  "🇮🇳"),
    LanguageOption("kn", "ಕನ್ನಡ",     "Kannada",    "🇮🇳"),
    LanguageOption("fr", "Français",   "French",     "🇫🇷"),
    LanguageOption("de", "Deutsch",    "German",     "🇩🇪"),
    LanguageOption("es", "Español",    "Spanish",    "🇪🇸"),
    LanguageOption("ja", "日本語",      "Japanese",   "🇯🇵")
)
