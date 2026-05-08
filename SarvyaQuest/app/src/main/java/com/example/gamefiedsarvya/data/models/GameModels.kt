package com.example.gamefiedsarvya.data.models

// ─── Zone / World ────────────────────────────────────────────────────────────

enum class ZoneType(val displayName: String, val description: String) {
    FOREST("Verdant Forest", "Basic knowledge – where every hero begins"),
    RUINS("Ancient Ruins", "Intermediate challenges await the brave"),
    FORTRESS("Shadow Fortress", "Advanced mastery – only the worthy survive")
}

data class Zone(
    val id: String,
    val type: ZoneType,
    val isUnlocked: Boolean = false,
    val completionPercent: Float = 0f,
    val enemies: List<Enemy> = emptyList()
)

// ─── Player ──────────────────────────────────────────────────────────────────

data class Player(
    val name: String = "Sarvya",
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val maxHp: Int = 100,
    val currentHp: Int = 100,
    val maxEnergy: Int = 50,
    val currentEnergy: Int = 50,
    val abilities: List<Ability> = listOf(Ability.HINT_STRIKE),
    val equippedAbility: Ability? = null,
    val streakCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0
) {
    val accuracy: Float get() = if (totalAnswered == 0) 0f else totalCorrect.toFloat() / totalAnswered
    val hpPercent: Float get() = currentHp.toFloat() / maxHp
    val xpPercent: Float get() = xp.toFloat() / xpToNextLevel
    val energyPercent: Float get() = currentEnergy.toFloat() / maxEnergy
}

// ─── Abilities ───────────────────────────────────────────────────────────────

enum class Ability(
    val displayName: String,
    val description: String,
    val energyCost: Int,
    val unlockLevel: Int
) {
    HINT_STRIKE("Hint Strike", "Reveals a hint before answering", 10, 1),
    TIME_FREEZE("Time Freeze", "Grants +10 seconds on the timer", 15, 3),
    DOUBLE_DAMAGE("Double Damage", "Next correct answer deals 2× damage", 20, 5),
    SHIELD_BLOCK("Shield Block", "Blocks next wrong-answer damage", 25, 7),
    MIND_SURGE("Mind Surge", "Skips current question without penalty", 30, 10)
}

// ─── Enemy / Boss ─────────────────────────────────────────────────────────────

enum class EnemyType { MINION, ELITE, BOSS }

data class Enemy(
    val id: String,
    val name: String,
    val type: EnemyType,
    val topic: String,
    val maxHp: Int,
    val currentHp: Int = maxHp,
    val attackPower: Int = 15,
    val xpReward: Int = 30,
    val phase: Int = 1,          // bosses have multiple phases
    val totalPhases: Int = 1,
    val spriteKey: String = "enemy_default"
) {
    val hpPercent: Float get() = currentHp.toFloat() / maxHp
    val isBoss: Boolean get() = type == EnemyType.BOSS
}

// ─── Questions ───────────────────────────────────────────────────────────────

enum class Difficulty { EASY, MEDIUM, HARD }

data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val hint: String = "",
    val explanation: String = "",
    val topic: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val timeLimitSeconds: Int = 20
) {
    val correctAnswer: String get() = options[correctIndex]
}

// ─── Combat State ─────────────────────────────────────────────────────────────

enum class CombatResult { ONGOING, PLAYER_WIN, PLAYER_LOSE }

data class CombatState(
    val player: Player,
    val enemy: Enemy,
    val currentQuestion: Question?,
    val questionIndex: Int = 0,
    val consecutiveCorrect: Int = 0,
    val result: CombatResult = CombatResult.ONGOING,
    val lastAnswerCorrect: Boolean? = null,
    val damageDealt: Int = 0,
    val damageTaken: Int = 0,
    val isShowingFeedback: Boolean = false,
    val activeAbility: Ability? = null,
    val hintVisible: Boolean = false,
    val timeRemainingSeconds: Int = 20,
    val isCriticalHit: Boolean = false
)

// ─── Skill Tree ───────────────────────────────────────────────────────────────

data class SkillNode(
    val id: String,
    val ability: Ability,
    val isUnlocked: Boolean = false,
    val requiredLevel: Int,
    val prerequisites: List<String> = emptyList(),
    val positionX: Float = 0f,
    val positionY: Float = 0f
)

// ─── AI / Adaptive Engine ─────────────────────────────────────────────────────

data class DigitalTwin(
    val playerId: String = "local_player",
    val knowledgeScores: Map<String, Float> = emptyMap(),   // topic → 0..1
    val preferredDifficulty: Difficulty = Difficulty.EASY,
    val averageResponseTimeMs: Long = 5000L,
    val recentAccuracy: Float = 0.5f,
    val focusScore: Float = 0.7f,                           // hardware placeholder
    val engagementScore: Float = 0.7f                       // hardware placeholder
)

// ─── Game Mode ────────────────────────────────────────────────────────────────

enum class GameMode { STORY, DUNGEON, PRACTICE }

// ─── Narrative / NPC ─────────────────────────────────────────────────────────

data class NpcDialogue(
    val npcName: String,
    val lines: List<String>,
    val portraitKey: String = "npc_default"
)

// ─── Save / Progress ─────────────────────────────────────────────────────────

data class GameProgress(
    val player: Player = Player(),
    val unlockedZones: Set<String> = setOf("zone_forest"),
    val completedEnemyIds: Set<String> = emptySet(),
    val digitalTwin: DigitalTwin = DigitalTwin(),
    val selectedLanguage: String = "en",
    val currentZoneId: String = "zone_forest",
    val gameMode: GameMode = GameMode.STORY
)
