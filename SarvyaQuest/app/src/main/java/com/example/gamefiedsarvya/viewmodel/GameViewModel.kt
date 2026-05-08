package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.remote.SessionRepository
import com.example.gamefiedsarvya.data.repository.GameRepository
import com.example.gamefiedsarvya.engine.AdaptiveEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorldUiState(
    val zones: List<Zone> = emptyList(),
    val player: Player = Player(),
    val currentZoneId: String = "zone_forest",
    val npcDialogue: NpcDialogue? = null,
    val isNpcVisible: Boolean = false,
    val isLoading: Boolean = true,
    val justLevelledUp: Boolean = false,
    val newlyUnlockedAbility: Ability? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _uiState = MutableStateFlow(WorldUiState())
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(GameProgress())
    val progress: StateFlow<GameProgress> = _progress.asStateFlow()

    init {
        viewModelScope.launch {
            repository.gameProgressFlow.collect { savedProgress ->
                _progress.value = savedProgress
                refreshWorldState(savedProgress)
            }
        }
    }

    private fun refreshWorldState(prog: GameProgress) {
        val zones = repository.getWorldZones(prog.unlockedZones)
        _uiState.update {
            it.copy(zones = zones, player = prog.player,
                currentZoneId = prog.currentZoneId, isLoading = false)
        }
    }

    fun selectZone(zoneId: String) {
        viewModelScope.launch {
            val prog = _progress.value.copy(currentZoneId = zoneId)
            _progress.value = prog
            repository.saveProgress(prog)
            _uiState.update { it.copy(currentZoneId = zoneId) }
        }
    }

    fun showNpcDialogue(zoneId: String) {
        val dialogue = repository.getZoneNpcDialogue(zoneId)
        _uiState.update { it.copy(npcDialogue = dialogue, isNpcVisible = true) }
    }

    fun dismissNpcDialogue() { _uiState.update { it.copy(isNpcVisible = false) } }

    /**
     * Called after every combat win.
     * Awards XP → levels up → unlocks skill tree nodes → syncs to Supabase.
     */
    fun markEnemyDefeated(enemyId: String, xpGained: Int) {
        viewModelScope.launch {
            val prog = _progress.value
            val prevLevel = prog.player.level
            val updatedPlayer = com.example.gamefiedsarvya.engine.CombatEngine
                .awardXp(prog.player, xpGained)

            // Zone unlocking
            val completedIds    = prog.completedEnemyIds + enemyId
            val updatedUnlocked = prog.unlockedZones.toMutableSet()
            if (completedIds.count { it in forestEnemyIds() } >= 2) updatedUnlocked.add("zone_ruins")
            if (completedIds.count { it in ruinsEnemyIds()  } >= 2) updatedUnlocked.add("zone_fortress")

            // Skill tree: unlock abilities by level
            val newAbilities = updatedPlayer.abilities.toMutableList()
            var newlyUnlocked: Ability? = null
            Ability.values().forEach { ability ->
                if (updatedPlayer.level >= ability.unlockLevel && ability !in newAbilities) {
                    newAbilities.add(ability)
                    newlyUnlocked = ability   // show the most recently unlocked
                }
            }

            val didLevelUp = updatedPlayer.level > prevLevel

            val newProg = prog.copy(
                player            = updatedPlayer.copy(abilities = newAbilities),
                completedEnemyIds = completedIds,
                unlockedZones     = updatedUnlocked
            )
            _progress.value = newProg
            repository.saveProgress(newProg)
            refreshWorldState(newProg)

            _uiState.update { it.copy(
                justLevelledUp       = didLevelUp,
                newlyUnlockedAbility = newlyUnlocked
            )}

            // ── Supabase sync ─────────────────────────────────────────────────
            SessionRepository.syncUserProfile(
                userName           = updatedPlayer.name,
                tier               = prog.gameMode.name,
                level              = updatedPlayer.level,
                totalXp            = updatedPlayer.xp,
                accuracy           = updatedPlayer.accuracy,
                unlockedAbilities  = newAbilities.map { it.name }
            )
            SessionRepository.syncSkillProgress(
                userName       = updatedPlayer.name,
                unlockedNodes  = newAbilities.map { it.name },
                xpTotal        = updatedPlayer.xp,
                level          = updatedPlayer.level
            )
        }
    }

    fun clearLevelUpFlag() {
        _uiState.update { it.copy(justLevelledUp = false, newlyUnlockedAbility = null) }
    }

    fun updateDigitalTwin(twin: DigitalTwin) {
        viewModelScope.launch {
            val prog = _progress.value.copy(digitalTwin = twin)
            _progress.value = prog
            repository.saveProgress(prog)
        }
    }

    fun setGameMode(mode: GameMode) {
        viewModelScope.launch {
            val prog = _progress.value.copy(gameMode = mode)
            _progress.value = prog
            repository.saveProgress(prog)
        }
    }

    fun resetGame() { viewModelScope.launch { repository.resetProgress() } }

    private fun forestEnemyIds() = setOf("e_goblin", "e_sprite", "e_wolf", "e_treant")
    private fun ruinsEnemyIds()  = setOf("e_skeleton", "e_golem", "e_wraith", "e_lich")

    fun getSkillTree()                  = repository.getSkillTree()
    fun getNpcDialogue(zoneId: String)  = repository.getZoneNpcDialogue(zoneId)
    fun getDigitalTwin(): DigitalTwin   = _progress.value.digitalTwin

    fun getEnemiesForZone(zoneId: String): List<Enemy> =
        repository.getWorldZones(_progress.value.unlockedZones)
            .find { it.id == zoneId }?.enemies ?: emptyList()

    fun getEnemiesForCurrentZone(): List<Enemy> =
        getEnemiesForZone(_uiState.value.currentZoneId)
}
