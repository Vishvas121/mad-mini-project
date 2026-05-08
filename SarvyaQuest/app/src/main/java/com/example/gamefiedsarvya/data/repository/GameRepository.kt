package com.example.gamefiedsarvya.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gamefiedsarvya.data.models.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sarvya_thozan_save")

class GameRepository(private val context: Context) {

    private val gson = Gson()
    private val PROGRESS_KEY = stringPreferencesKey("game_progress")

    val gameProgressFlow: Flow<GameProgress> = context.dataStore.data.map { prefs ->
        val json = prefs[PROGRESS_KEY]
        if (json != null) {
            try { gson.fromJson(json, GameProgress::class.java) }
            catch (e: Exception) { defaultProgress() }
        } else {
            defaultProgress()
        }
    }

    suspend fun saveProgress(progress: GameProgress) {
        context.dataStore.edit { prefs ->
            prefs[PROGRESS_KEY] = gson.toJson(progress)
        }
    }

    suspend fun resetProgress() {
        context.dataStore.edit { prefs ->
            prefs[PROGRESS_KEY] = gson.toJson(defaultProgress())
        }
    }

    private fun defaultProgress() = GameProgress(
        player = Player(name = "Sarvya"),
        unlockedZones = setOf("zone_forest"),
        completedEnemyIds = emptySet(),
        digitalTwin = DigitalTwin(),
        selectedLanguage = "en",
        currentZoneId = "zone_forest",
        gameMode = GameMode.STORY
    )

    // ── World Data ────────────────────────────────────────────────────────────

    fun getWorldZones(unlockedZones: Set<String>): List<Zone> = listOf(
        Zone(
            id = "zone_forest",
            type = ZoneType.FOREST,
            isUnlocked = "zone_forest" in unlockedZones,
            enemies = forestEnemies()
        ),
        Zone(
            id = "zone_ruins",
            type = ZoneType.RUINS,
            isUnlocked = "zone_ruins" in unlockedZones,
            enemies = ruinsEnemies()
        ),
        Zone(
            id = "zone_fortress",
            type = ZoneType.FORTRESS,
            isUnlocked = "zone_fortress" in unlockedZones,
            enemies = fortressEnemies()
        )
    )

    private fun forestEnemies() = listOf(
        Enemy("e_goblin",    "Knowledge Goblin",  EnemyType.MINION, "Technology", 60,  attackPower = 10, xpReward = 25),
        Enemy("e_sprite",    "Riddle Sprite",     EnemyType.MINION, "Science",    50,  attackPower = 8,  xpReward = 20),
        Enemy("e_wolf",      "Logic Wolf",        EnemyType.ELITE,  "Math",       100, attackPower = 18, xpReward = 50),
        Enemy("e_treant",    "Ancient Treant",    EnemyType.BOSS,   "Mixed",      200, attackPower = 25, xpReward = 120, totalPhases = 2)
    )

    private fun ruinsEnemies() = listOf(
        Enemy("e_skeleton",  "Data Skeleton",     EnemyType.MINION, "Technology", 80,  attackPower = 15, xpReward = 40),
        Enemy("e_golem",     "Stone Golem",       EnemyType.ELITE,  "Science",    130, attackPower = 22, xpReward = 70),
        Enemy("e_wraith",    "Memory Wraith",     EnemyType.ELITE,  "Math",       110, attackPower = 20, xpReward = 60),
        Enemy("e_lich",      "The Lich King",     EnemyType.BOSS,   "Mixed",      300, attackPower = 35, xpReward = 200, totalPhases = 3)
    )

    private fun fortressEnemies() = listOf(
        Enemy("e_knight",    "Dark Knight",       EnemyType.ELITE,  "Technology", 150, attackPower = 28, xpReward = 90),
        Enemy("e_mage",      "Chaos Mage",        EnemyType.ELITE,  "Science",    140, attackPower = 30, xpReward = 85),
        Enemy("e_dragon",    "Algorithm Dragon",  EnemyType.BOSS,   "Mixed",      500, attackPower = 50, xpReward = 400, totalPhases = 4)
    )

    // ── Skill Tree ────────────────────────────────────────────────────────────

    fun getSkillTree(): List<SkillNode> = listOf(
        SkillNode("sn_hint",   Ability.HINT_STRIKE,   true,  1,  emptyList(),       0.5f, 0.1f),
        SkillNode("sn_freeze", Ability.TIME_FREEZE,   false, 3,  listOf("sn_hint"), 0.2f, 0.35f),
        SkillNode("sn_double", Ability.DOUBLE_DAMAGE, false, 5,  listOf("sn_hint"), 0.8f, 0.35f),
        SkillNode("sn_shield", Ability.SHIELD_BLOCK,  false, 7,  listOf("sn_freeze","sn_double"), 0.35f, 0.6f),
        SkillNode("sn_surge",  Ability.MIND_SURGE,    false, 10, listOf("sn_shield"), 0.5f, 0.85f)
    )

    // ── NPC Dialogues ─────────────────────────────────────────────────────────

    fun getZoneNpcDialogue(zoneId: String): NpcDialogue = when (zoneId) {
        "zone_forest" -> NpcDialogue(
            "Elder Mira",
            listOf(
                "Welcome, young Sarvya. The Forest holds the seeds of knowledge.",
                "Defeat the creatures here and your mind will grow stronger.",
                "Remember: every correct answer is a strike against ignorance!"
            )
        )
        "zone_ruins" -> NpcDialogue(
            "Scholar Dren",
            listOf(
                "These ruins hold the echoes of forgotten wisdom.",
                "The enemies here are tougher – they demand deeper understanding.",
                "Use your abilities wisely. Time is your greatest enemy."
            )
        )
        "zone_fortress" -> NpcDialogue(
            "Shadow Oracle",
            listOf(
                "Few dare enter the Fortress. Fewer leave unchanged.",
                "The Dragon guards the ultimate knowledge. Are you ready?",
                "Master every topic. Leave no question unanswered."
            )
        )
        else -> NpcDialogue("Guide", listOf("Seek knowledge. Gain power."))
    }
}
