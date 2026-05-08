package com.example.gamefiedsarvya.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.gamefiedsarvya.data.models.MapNodeType
import com.example.gamefiedsarvya.focus.FocusViewModel
import com.example.gamefiedsarvya.ui.screens.*
import com.example.gamefiedsarvya.ui.theme.GamefiedSarvyaTheme
import com.example.gamefiedsarvya.viewmodel.*

// ── Route constants ───────────────────────────────────────────────────────────

object Routes {
    const val SPLASH             = "splash"
    const val ONBOARDING         = "onboarding"
    const val MAIN_MENU          = "main_menu"
    const val WORLD_MAP          = "world_map"
    const val ZONE_DETAIL        = "zone_detail/{zoneId}"
    const val COMBAT             = "combat/{enemyId}/{zoneId}"
    const val SKILL_TREE         = "skill_tree"
    const val SETTINGS           = "settings"
    const val DUNGEON            = "dungeon"
    const val PRACTICE           = "practice"
    const val LEARNING_HUB       = "learning_hub"
    const val TIER_SELECT        = "tier_select"
    const val TIER_WORLD_MAP     = "tier_world_map"
    const val ADAPTIVE_DASHBOARD = "adaptive_dashboard"
    const val PROFILE            = "profile"
    const val AI_PRACTICE        = "ai_practice"
    const val STREAM_FEED        = "stream_feed"
    const val SESSION_REPLAY     = "session_replay"
    const val VOICE_SETTINGS     = "voice_settings"
    const val THEME_SELECT       = "theme_select"
    const val LEADERBOARD        = "leaderboard"

    fun zoneDetail(zoneId: String) = "zone_detail/$zoneId"
    fun combat(enemyId: String, zoneId: String) = "combat/$enemyId/$zoneId"
}

// ── Single nav graph ──────────────────────────────────────────────────────────

@Composable
fun SarvyaNavGraph() {
    val navController     = rememberNavController()
    val gameViewModel:    GameViewModel          = viewModel()
    val hubViewModel:     LearningHubViewModel   = viewModel()
    val profileViewModel: UserProfileViewModel   = viewModel()
    val groqViewModel:    GroqAdaptiveViewModel  = viewModel()
    val sessionViewModel: SessionViewModel       = viewModel()
    val voiceViewModel:   VoiceViewModel         = viewModel()

    // Observe selected theme — drives MaterialTheme for the entire app
    val hubState by hubViewModel.uiState.collectAsState()

    GamefiedSarvyaTheme(appTheme = hubState.selectedTheme) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                val dest = if (!profileViewModel.isOnboardingComplete)
                    Routes.ONBOARDING else Routes.MAIN_MENU
                navController.navigate(dest) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        // ── Onboarding (first-run only) ───────────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                profileViewModel = profileViewModel,
                hubViewModel     = hubViewModel,
                onComplete       = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Main Menu ─────────────────────────────────────────────────────────
        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                gameViewModel  = gameViewModel,
                hubViewModel   = hubViewModel,
                onStartStory   = { navController.navigate(Routes.WORLD_MAP) },
                onDungeon      = { navController.navigate(Routes.DUNGEON) },
                onPractice     = { navController.navigate(Routes.AI_PRACTICE) },
                onSkillTree    = { navController.navigate(Routes.SKILL_TREE) },
                onSettings     = { navController.navigate(Routes.SETTINGS) },
                onLearningHub  = { navController.navigate(Routes.LEARNING_HUB) },
                onTierSelect   = { navController.navigate(Routes.TIER_SELECT) },
                onTierWorldMap = { navController.navigate(Routes.TIER_WORLD_MAP) },
                onDashboard    = { navController.navigate(Routes.ADAPTIVE_DASHBOARD) },
                onProfile      = { navController.navigate(Routes.PROFILE) },
                onStreamFeed   = { navController.navigate(Routes.STREAM_FEED) }
            )
        }

        // ── World Map (original zone-based) ───────────────────────────────────
        composable(Routes.WORLD_MAP) {
            WorldMapScreen(
                gameViewModel  = gameViewModel,
                onZoneSelected = { zoneId -> navController.navigate(Routes.zoneDetail(zoneId)) },
                onBack         = { navController.popBackStack() }
            )
        }

        // ── Zone Detail ───────────────────────────────────────────────────────
        composable(
            route     = Routes.ZONE_DETAIL,
            arguments = listOf(navArgument("zoneId") { type = NavType.StringType })
        ) { back ->
            val zoneId = back.arguments?.getString("zoneId") ?: "zone_forest"
            ZoneDetailScreen(
                zoneId          = zoneId,
                gameViewModel   = gameViewModel,
                onEnemySelected = { enemyId ->
                    navController.navigate(Routes.combat(enemyId, zoneId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Combat ────────────────────────────────────────────────────────────
        composable(
            route     = Routes.COMBAT,
            arguments = listOf(
                navArgument("enemyId") { type = NavType.StringType },
                navArgument("zoneId")  { type = NavType.StringType }
            )
        ) { back ->
            val enemyId         = back.arguments?.getString("enemyId") ?: ""
            val zoneId          = back.arguments?.getString("zoneId")  ?: "zone_forest"
            val combatViewModel: CombatViewModel = viewModel()
            CombatScreen(
                enemyId         = enemyId,
                zoneId          = zoneId,
                gameViewModel   = gameViewModel,
                combatViewModel = combatViewModel,
                onCombatEnd     = { defeatedId, xpGained ->
                    if (defeatedId != null) gameViewModel.markEnemyDefeated(defeatedId, xpGained)
                    gameViewModel.updateDigitalTwin(combatViewModel.getUpdatedDigitalTwin())
                    navController.popBackStack()
                }
            )
        }

        // ── Skill Tree ────────────────────────────────────────────────────────
        composable(Routes.SKILL_TREE) {
            SkillTreeScreen(
                gameViewModel = gameViewModel,
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Dungeon ───────────────────────────────────────────────────────────
        composable(Routes.DUNGEON) {
            DungeonScreen(
                gameViewModel = gameViewModel,
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Practice (original offline) ───────────────────────────────────────
        composable(Routes.PRACTICE) {
            PracticeScreen(
                gameViewModel = gameViewModel,
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(Routes.SETTINGS) {
            SettingsScreen(
                gameViewModel    = gameViewModel,
                hubViewModel     = hubViewModel,
                onBack           = { navController.popBackStack() },
                onTierSelect     = { navController.navigate(Routes.TIER_SELECT) },
                onVoiceSettings  = { navController.navigate(Routes.VOICE_SETTINGS) },
                onThemeSelect    = { navController.navigate(Routes.THEME_SELECT) }
            )
        }

        // ── Learning Hub ──────────────────────────────────────────────────────
        composable(Routes.LEARNING_HUB) {
            val hubState by hubViewModel.uiState.collectAsState()
            LearningHubScreen(
                gameViewModel = gameViewModel,
                selectedTier  = hubState.selectedTier,
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Tier Select ───────────────────────────────────────────────────────
        composable(Routes.TIER_SELECT) {
            val hubState by hubViewModel.uiState.collectAsState()
            TierSelectScreen(
                currentTier    = hubState.selectedTier,
                onTierSelected = { tier ->
                    hubViewModel.setTier(tier)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Tier World Map ────────────────────────────────────────────────────
        composable(Routes.TIER_WORLD_MAP) {
            val hubState by hubViewModel.uiState.collectAsState()
            TierWorldMapScreen(
                tier           = hubState.selectedTier,
                gameViewModel  = gameViewModel,
                hubViewModel   = hubViewModel,
                onNodeSelected = { node ->
                    when (node.type) {
                        MapNodeType.HUB  -> navController.navigate(Routes.LEARNING_HUB)
                        MapNodeType.BOSS -> navController.navigate(Routes.DUNGEON)
                        else             -> navController.navigate(Routes.AI_PRACTICE)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Adaptive Dashboard ────────────────────────────────────────────────
        composable(Routes.ADAPTIVE_DASHBOARD) {
            AdaptiveDashboardScreen(
                gameViewModel = gameViewModel,
                hubViewModel  = hubViewModel,
                onBack        = { navController.popBackStack() },
                onOpenHub     = { navController.navigate(Routes.LEARNING_HUB) },
                onOpenMap     = { navController.navigate(Routes.TIER_WORLD_MAP) }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                gameViewModel    = gameViewModel,
                hubViewModel     = hubViewModel,
                onBack           = { navController.popBackStack() }
            )
        }

        // ── AI Practice (Groq-powered + Voice + Session recording) ───────────
        composable(Routes.AI_PRACTICE) {
            AIPracticeScreen(
                gameViewModel    = gameViewModel,
                hubViewModel     = hubViewModel,
                profileViewModel = profileViewModel,
                groqViewModel    = groqViewModel,
                sessionViewModel = sessionViewModel,
                voiceViewModel   = voiceViewModel,
                onBack           = { navController.popBackStack() }
            )
        }

        // ── Stream Feed ───────────────────────────────────────────────────────
        composable(Routes.STREAM_FEED) {
            StreamFeedScreen(
                sessionViewModel = sessionViewModel,
                profileViewModel = profileViewModel,
                onReplay         = { session ->
                    sessionViewModel.startReplay(session)
                    navController.navigate(Routes.SESSION_REPLAY)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Session Replay ────────────────────────────────────────────────────
        composable(Routes.SESSION_REPLAY) {
            SessionReplayScreen(
                sessionViewModel = sessionViewModel,
                voiceViewModel   = voiceViewModel,
                onBack           = { navController.popBackStack() }
            )
        }

        // ── Voice Settings ────────────────────────────────────────────────────
        composable(Routes.VOICE_SETTINGS) {
            VoiceSettingsScreen(
                voiceViewModel = voiceViewModel,
                onBack         = { navController.popBackStack() }
            )
        }

        // ── Theme Select ──────────────────────────────────────────────────────
        composable(Routes.THEME_SELECT) {
            val themeHubState by hubViewModel.uiState.collectAsState()
            ThemeSelectScreen(
                currentTheme    = themeHubState.selectedTheme,
                onThemeSelected = { theme ->
                    hubViewModel.setTheme(theme)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

    } // end NavHost
    } // end GamefiedSarvyaTheme
}
