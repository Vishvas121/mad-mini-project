package com.example.gamefiedsarvya.engine

import com.example.gamefiedsarvya.data.models.*

/**
 * Pure combat logic – no UI dependencies.
 * All state transitions return a new CombatState (immutable).
 */
object CombatEngine {

    fun startCombat(player: Player, enemy: Enemy, firstQuestion: Question): CombatState =
        CombatState(
            player = player,
            enemy = enemy,
            currentQuestion = firstQuestion,
            questionIndex = 0,
            consecutiveCorrect = 0,
            result = CombatResult.ONGOING,
            timeRemainingSeconds = firstQuestion.timeLimitSeconds
        )

    /**
     * Process a player's answer.
     * Returns updated CombatState with feedback flags set.
     */
    fun processAnswer(
        state: CombatState,
        selectedIndex: Int,
        responseTimeMs: Long
    ): CombatState {
        val question = state.currentQuestion ?: return state
        val isCorrect = selectedIndex == question.correctIndex

        return if (isCorrect) {
            handleCorrectAnswer(state, question, responseTimeMs)
        } else {
            handleWrongAnswer(state, question)
        }
    }

    private fun handleCorrectAnswer(
        state: CombatState,
        question: Question,
        responseTimeMs: Long
    ): CombatState {
        val timeLimitMs = question.timeLimitSeconds * 1000L
        val (damage, isCritical) = AdaptiveEngine.calculatePlayerDamage(
            responseTimeMs, timeLimitMs, state.consecutiveCorrect, state.activeAbility
        )

        val newEnemyHp = (state.enemy.currentHp - damage).coerceAtLeast(0)
        val newEnemy   = state.enemy.copy(currentHp = newEnemyHp)
        val newStreak  = state.consecutiveCorrect + 1
        val newPlayer  = state.player.copy(
            streakCount    = newStreak,
            totalCorrect   = state.player.totalCorrect + 1,
            totalAnswered  = state.player.totalAnswered + 1
        )

        val combatResult = when {
            newEnemyHp <= 0 && state.enemy.phase >= state.enemy.totalPhases ->
                CombatResult.PLAYER_WIN
            newEnemyHp <= 0 ->
                CombatResult.ONGOING  // boss phase transition handled by ViewModel
            else ->
                CombatResult.ONGOING
        }

        return state.copy(
            player             = newPlayer,
            enemy              = newEnemy,
            consecutiveCorrect = newStreak,
            result             = combatResult,
            lastAnswerCorrect  = true,
            damageDealt        = damage,
            isShowingFeedback  = true,
            isCriticalHit      = isCritical,
            activeAbility      = if (state.activeAbility == Ability.DOUBLE_DAMAGE) null
                                 else state.activeAbility
        )
    }

    private fun handleWrongAnswer(
        state: CombatState,
        question: Question
    ): CombatState {
        val damage    = AdaptiveEngine.calculateEnemyDamage(state.enemy.attackPower, state.activeAbility)
        val newHp     = (state.player.currentHp - damage).coerceAtLeast(0)
        val newPlayer = state.player.copy(
            currentHp     = newHp,
            streakCount   = 0,
            totalAnswered = state.player.totalAnswered + 1
        )

        val combatResult = if (newHp <= 0) CombatResult.PLAYER_LOSE else CombatResult.ONGOING

        return state.copy(
            player             = newPlayer,
            consecutiveCorrect = 0,
            result             = combatResult,
            lastAnswerCorrect  = false,
            damageTaken        = damage,
            isShowingFeedback  = true,
            isCriticalHit      = false,
            activeAbility      = if (state.activeAbility == Ability.SHIELD_BLOCK) null
                                 else state.activeAbility
        )
    }

    /**
     * Advance to the next question after feedback is dismissed.
     */
    fun advanceToNextQuestion(state: CombatState, nextQuestion: Question?): CombatState =
        state.copy(
            currentQuestion   = nextQuestion,
            questionIndex     = state.questionIndex + 1,
            isShowingFeedback = false,
            lastAnswerCorrect = null,
            damageDealt       = 0,
            damageTaken       = 0,
            hintVisible       = false,
            isCriticalHit     = false,
            timeRemainingSeconds = nextQuestion?.timeLimitSeconds ?: 20
        )

    /**
     * Activate an ability (costs energy).
     */
    fun activateAbility(state: CombatState, ability: Ability): CombatState {
        if (state.player.currentEnergy < ability.energyCost) return state
        val newEnergy = state.player.currentEnergy - ability.energyCost
        val newPlayer = state.player.copy(currentEnergy = newEnergy)
        return when (ability) {
            Ability.HINT_STRIKE  -> state.copy(player = newPlayer, hintVisible = true)
            Ability.TIME_FREEZE  -> state.copy(
                player = newPlayer,
                timeRemainingSeconds = state.timeRemainingSeconds + 10
            )
            else -> state.copy(player = newPlayer, activeAbility = ability)
        }
    }

    /**
     * Tick the timer down by one second. Returns updated state.
     * If time runs out, treat as wrong answer.
     */
    fun tickTimer(state: CombatState): CombatState {
        if (state.isShowingFeedback || state.result != CombatResult.ONGOING) return state
        val newTime = state.timeRemainingSeconds - 1
        return if (newTime <= 0) {
            // Time's up → wrong answer
            handleWrongAnswer(state, state.currentQuestion ?: return state)
        } else {
            state.copy(timeRemainingSeconds = newTime)
        }
    }

    /**
     * Advance boss to next phase when current phase HP hits 0.
     */
    fun advanceBossPhase(state: CombatState): CombatState {
        val boss = state.enemy
        if (!boss.isBoss || boss.phase >= boss.totalPhases) return state
        val newPhase  = boss.phase + 1
        val newMaxHp  = (boss.maxHp * (1f + newPhase * 0.2f)).toInt()
        val newBoss   = boss.copy(
            phase      = newPhase,
            maxHp      = newMaxHp,
            currentHp  = newMaxHp,
            attackPower = (boss.attackPower * 1.25f).toInt()
        )
        return state.copy(enemy = newBoss)
    }

    /**
     * Award XP and level up if threshold reached.
     */
    fun awardXp(player: Player, xpGain: Int): Player {
        val newXp = player.xp + xpGain
        return if (newXp >= player.xpToNextLevel) {
            val overflow    = newXp - player.xpToNextLevel
            val newLevel    = player.level + 1
            val newMaxHp    = player.maxHp + 10
            val newMaxEnergy = player.maxEnergy + 5
            player.copy(
                level         = newLevel,
                xp            = overflow,
                xpToNextLevel = (player.xpToNextLevel * 1.5f).toInt(),
                maxHp         = newMaxHp,
                currentHp     = newMaxHp,
                maxEnergy     = newMaxEnergy,
                currentEnergy = newMaxEnergy
            )
        } else {
            player.copy(xp = newXp)
        }
    }
}
