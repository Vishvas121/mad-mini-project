package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.components.CombatHeroSprite
import com.example.gamefiedsarvya.ui.components.HeroDirection
import com.example.gamefiedsarvya.ui.components.HeroSprite
import com.example.gamefiedsarvya.ui.components.CombatVillainSprite
import com.example.gamefiedsarvya.ui.components.VillainDirection
import com.example.gamefiedsarvya.ui.components.LearningModeOverlay
import com.example.gamefiedsarvya.ui.components.ConceptMasteredBanner
import com.example.gamefiedsarvya.ui.assets.CombatEffectOverlay
import com.example.gamefiedsarvya.ui.assets.StreakFireEffect
import com.example.gamefiedsarvya.ui.assets.FuturisticHealthBar
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.CombatViewModel
import com.example.gamefiedsarvya.viewmodel.GameViewModel

@Composable
fun CombatScreen(
    enemyId: String,
    zoneId: String,
    gameViewModel: GameViewModel,
    combatViewModel: CombatViewModel,
    onCombatEnd: (String?, Int) -> Unit
) {
    val uiState  by combatViewModel.uiState.collectAsState()
    val progress by gameViewModel.progress.collectAsState()

    // Initialise combat once
    LaunchedEffect(enemyId) {
        val allZones = gameViewModel.uiState.value.zones
        val enemy    = allZones.flatMap { it.enemies }.find { it.id == enemyId } ?: return@LaunchedEffect
        combatViewModel.startCombat(
            player = progress.player,
            enemy  = enemy,
            twin   = progress.digitalTwin,
            zoneId = zoneId,
            name   = progress.player.name
        )
    }

    val combatState = uiState.combatState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(AbyssBlue, DeepVoid))
            )
            .screenShake(uiState.screenShake)
    ) {
        if (combatState == null || uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // ── Top: Enemy + Player bars ──────────────────────────────────────
            CombatHeader(combatState = combatState)

            Spacer(Modifier.height(12.dp))

            // ── Enemy visual ──────────────────────────────────────────────────
            EnemyVisual(
                enemy         = combatState.enemy,
                lastCorrect   = combatState.lastAnswerCorrect,
                isCritical    = combatState.isCriticalHit,
                damageDealt   = combatState.damageDealt,
                damageTaken   = combatState.damageTaken,
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Timer bar ─────────────────────────────────────────────────────
            TimerBar(
                remaining = combatState.timeRemainingSeconds,
                total     = combatState.currentQuestion?.timeLimitSeconds ?: 20
            )

            Spacer(Modifier.height(12.dp))

            // ── Question card ─────────────────────────────────────────────────
            combatState.currentQuestion?.let { question ->
                QuestionCard(
                    question    = question,
                    hintVisible = combatState.hintVisible,
                    isShowingFeedback = combatState.isShowingFeedback,
                    lastCorrect = combatState.lastAnswerCorrect,
                    onAnswer    = { idx -> combatViewModel.submitAnswer(idx) },
                    modifier    = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Abilities bar ─────────────────────────────────────────────────
            AbilitiesBar(
                abilities      = combatState.player.abilities,
                playerEnergy   = combatState.player.currentEnergy,
                activeAbility  = combatState.activeAbility,
                onActivate     = { ability -> combatViewModel.activateAbility(ability) }
            )
        }

        // ── Overlays ──────────────────────────────────────────────────────────

        if (uiState.showBossPhaseTransition) {
            BossPhaseBanner(phase = uiState.bossPhase)
        }

        if (uiState.showLevelUpBanner) {
            LevelUpBanner(level = uiState.levelUpLevel) {
                combatViewModel.dismissVictory()
            }
        }

        if (uiState.showVictoryScreen && !uiState.showLevelUpBanner) {
            VictoryOverlay(
                xpGained = uiState.xpGained,
                enemy    = combatState.enemy,
                onContinue = {
                    combatViewModel.dismissVictory()
                    onCombatEnd(combatState.enemy.id, uiState.xpGained)
                }
            )
        }

        if (uiState.showDefeatScreen) {
            DefeatOverlay(
                onRetry = {
                    combatViewModel.dismissDefeat()
                    onCombatEnd(null, 0)
                }
            )
        }

        // ── NEW: Learning Mode Overlay ────────────────────────────────────────
        LearningModeOverlay(
            state            = uiState.learningMode,
            onTryAgain       = { combatViewModel.proceedToRetry() },
            onExplainSimpler = { combatViewModel.requestSimplifiedExplanation() },
            onRetryAnswer    = { idx -> combatViewModel.submitRetry(idx) },
            onContinueCombat = { combatViewModel.exitLearningModeAndContinue(true) },
            modifier         = Modifier.fillMaxSize()
        )

        // ── NEW: Concept Mastered Banner ──────────────────────────────────────
        ConceptMasteredBanner(
            conceptName = uiState.masteredConceptName,
            visible     = uiState.showConceptMasteredBanner,
            modifier    = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
        )
    }
}

// ── Combat Header ─────────────────────────────────────────────────────────────

@Composable
private fun CombatHeader(combatState: CombatState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player stats + sprite
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CombatHeroSprite(
                direction    = HeroDirection.EAST,
                flashCorrect = combatState.lastAnswerCorrect == true,
                flashWrong   = combatState.lastAnswerCorrect == false,
                modifier     = Modifier.size(72.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    combatState.player.name,
                    style = MaterialTheme.typography.labelLarge.copy(color = NeonCyan)
                )
                FuturisticHealthBar(
                    combatState.player.currentHp, combatState.player.maxHp,
                    modifier = Modifier.fillMaxWidth(), isEnemy = false
                )
                Spacer(Modifier.height(4.dp))
                StatBar("EN", combatState.player.currentEnergy, combatState.player.maxEnergy, EnergyBlue,
                    modifier = Modifier.fillMaxWidth())
                // Streak fire
                if (combatState.consecutiveCorrect >= 3) {
                    StreakFireEffect(
                        streakCount = combatState.consecutiveCorrect,
                        modifier    = Modifier.height(28.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // VS
        Text(
            "VS",
            style = MaterialTheme.typography.titleLarge.copy(color = TextMuted),
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Spacer(Modifier.width(12.dp))

        // Enemy stats
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(
                combatState.enemy.name,
                style = MaterialTheme.typography.labelLarge.copy(color = NeonRed),
                textAlign = TextAlign.End
            )
            FuturisticHealthBar(
                combatState.enemy.currentHp, combatState.enemy.maxHp,
                modifier = Modifier.fillMaxWidth(), isEnemy = true
            )
            if (combatState.enemy.isBoss) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Phase ${combatState.enemy.phase}/${combatState.enemy.totalPhases}",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonRed)
                )
            }
        }
    }
}

// ── Enemy Visual ──────────────────────────────────────────────────────────────

@Composable
private fun EnemyVisual(
    enemy: Enemy,
    lastCorrect: Boolean?,
    isCritical: Boolean,
    damageDealt: Int,
    damageTaken: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "enemy_float")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        // ── Combat effects layer ──────────────────────────────────────────────
        CombatEffectOverlay(
            showAttack  = lastCorrect == true,
            showHit     = lastCorrect == true,
            isCritical  = isCritical,
            isPlayerHit = false,
            modifier    = Modifier.fillMaxSize()
        )

        // ── Villain sprite ────────────────────────────────────────────────────
        CombatVillainSprite(
            direction = VillainDirection.SOUTH,
            isBoss    = enemy.isBoss,
            flashHit  = lastCorrect == true,
            modifier  = Modifier.align(Alignment.Center)
        )

        // ── Damage / hit indicators ───────────────────────────────────────────
        AnimatedVisibility(
            visible = lastCorrect != null,
            enter   = fadeIn() + scaleIn(),
            exit    = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            when {
                lastCorrect == true && isCritical ->
                    Text("💥 CRITICAL! -$damageDealt",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NeonGold, fontWeight = FontWeight.Black
                        ))
                lastCorrect == true ->
                    Text("⚔ -$damageDealt",
                        style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen))
                lastCorrect == false ->
                    Text("💔 -$damageTaken",
                        style = MaterialTheme.typography.titleLarge.copy(color = NeonRed))
                else -> {}
            }
        }
    }
}

// ── Timer Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun TimerBar(remaining: Int, total: Int) {
    val fraction = (remaining.toFloat() / total).coerceIn(0f, 1f)
    val color = when {
        fraction > 0.5f -> NeonGreen
        fraction > 0.25f -> NeonOrange
        else -> NeonRed
    }
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(900),
        label = "timer"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⏱", fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text("${remaining}s", style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

// ── Question Card ─────────────────────────────────────────────────────────────

@Composable
private fun QuestionCard(
    question: Question,
    hintVisible: Boolean,
    isShowingFeedback: Boolean,
    lastCorrect: Boolean?,
    onAnswer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // Difficulty + topic
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DifficultyBadge(
                question.difficulty.name,
                when (question.difficulty) {
                    Difficulty.EASY   -> NeonGreen
                    Difficulty.MEDIUM -> NeonOrange
                    Difficulty.HARD   -> NeonRed
                }
            )
            DifficultyBadge(question.topic, NeonCyan)
        }

        Spacer(Modifier.height(10.dp))

        // Question text
        GameCard(
            modifier    = Modifier.fillMaxWidth(),
            borderColor = NeonCyan.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    question.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                if (hintVisible && question.hint.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "💡 ${question.hint}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = NeonGold)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Answer options
        question.options.forEachIndexed { index, option ->
            val isCorrect = index == question.correctIndex
            val optionColor = when {
                !isShowingFeedback -> NeonCyan
                isCorrect          -> NeonGreen
                else               -> NeonRed
            }
            val bgAlpha = when {
                !isShowingFeedback -> 0.05f
                isCorrect          -> 0.2f
                else               -> 0.1f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, optionColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .background(optionColor.copy(alpha = bgAlpha), RoundedCornerShape(8.dp))
                    .clickable(enabled = !isShowingFeedback) { onAnswer(index) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${('A' + index)}.",
                        style = MaterialTheme.typography.labelLarge.copy(color = optionColor),
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isShowingFeedback && isCorrect) {
                        Spacer(Modifier.weight(1f))
                        Text("✓", style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen))
                    }
                }
            }
        }

        // Explanation after feedback
        if (isShowingFeedback && question.explanation.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .background(NeonGold.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "📖 ${question.explanation}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = NeonGold)
                )
            }
        }
    }
}

// ── Abilities Bar ─────────────────────────────────────────────────────────────

@Composable
private fun AbilitiesBar(
    abilities: List<Ability>,
    playerEnergy: Int,
    activeAbility: Ability?,
    onActivate: (Ability) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        abilities.forEach { ability ->
            val canAfford = playerEnergy >= ability.energyCost
            val isActive  = activeAbility == ability
            val color     = when (ability) {
                Ability.HINT_STRIKE  -> NeonGold
                Ability.TIME_FREEZE  -> NeonCyan
                Ability.DOUBLE_DAMAGE -> NeonRed
                Ability.SHIELD_BLOCK -> EnergyBlue
                Ability.MIND_SURGE   -> NeonPurple
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        if (isActive) color else color.copy(alpha = if (canAfford) 0.5f else 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        if (isActive) color.copy(alpha = 0.25f)
                        else color.copy(alpha = if (canAfford) 0.08f else 0.03f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = canAfford && !isActive) { onActivate(ability) }
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = when (ability) {
                    Ability.HINT_STRIKE   -> "💡"
                    Ability.TIME_FREEZE   -> "❄"
                    Ability.DOUBLE_DAMAGE -> "⚡"
                    Ability.SHIELD_BLOCK  -> "🛡"
                    Ability.MIND_SURGE    -> "🌀"
                }
                Text(icon, fontSize = 18.sp)
                Text(
                    "${ability.energyCost}⚡",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (canAfford) color else TextMuted
                    )
                )
            }
        }
    }
}

// ── Victory Overlay ───────────────────────────────────────────────────────────

@Composable
private fun VictoryOverlay(xpGained: Int, enemy: Enemy, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Hero celebrating – face south-east (triumphant pose)
            HeroSprite(
                direction = HeroDirection.SOUTH_EAST,
                size      = 96.dp,
                glowColor = NeonGold,
                showGlow  = true,
                floatAnim = true
            )
            Spacer(Modifier.height(12.dp))
            Text("⚔ VICTORY ⚔",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = NeonGold, fontWeight = FontWeight.Black
                ))
            Spacer(Modifier.height(8.dp))
            Text("${enemy.name} defeated!",
                style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary))
            Spacer(Modifier.height(8.dp))
            Text("+$xpGained XP",
                style = MaterialTheme.typography.headlineLarge.copy(color = XpGold))
            Spacer(Modifier.height(24.dp))
            NeonButton("Continue →", onClick = onContinue, color = NeonGold)
        }
    }
}

// ── Defeat Overlay ────────────────────────────────────────────────────────────

@Composable
private fun DefeatOverlay(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Hero retreating – face west
            HeroSprite(
                direction = HeroDirection.WEST,
                size      = 80.dp,
                glowColor = NeonRed,
                showGlow  = true,
                floatAnim = false
            )
            Spacer(Modifier.height(12.dp))
            Text("💀 DEFEATED",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = NeonRed, fontWeight = FontWeight.Black
                ))
            Spacer(Modifier.height(12.dp))
            Text("Knowledge is power.\nStudy and return stronger.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextSecondary, textAlign = TextAlign.Center
                ))
            Spacer(Modifier.height(24.dp))
            NeonButton("← Retreat", onClick = onRetry, color = NeonRed)
        }
    }
}
