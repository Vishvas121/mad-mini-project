package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.assets.NeonXpBar
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel
import com.example.gamefiedsarvya.viewmodel.UserProfileViewModel

@Composable
fun MainMenuScreen(
    gameViewModel:  GameViewModel,
    hubViewModel:   LearningHubViewModel,
    onStartStory:   () -> Unit,
    onDungeon:      () -> Unit,
    onPractice:     () -> Unit,
    onSkillTree:    () -> Unit,
    onSettings:     () -> Unit,
    onLearningHub:  () -> Unit,
    onTierSelect:   () -> Unit,
    onTierWorldMap: () -> Unit = {},
    onDashboard:    () -> Unit = {},
    onProfile:      () -> Unit = {},
    onStreamFeed:   () -> Unit = {}
) {
    val progress by gameViewModel.progress.collectAsState()
    val hubState by hubViewModel.uiState.collectAsState()
    val appTheme = LocalAppTheme.current   // live theme from CompositionLocal

    var menuVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { menuVisible = true }

    val inf = rememberInfiniteTransition(label = "menu_bg")
    val glowAlpha by inf.animateFloat(0.3f, 0.7f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "glow")

    val tierColor = when (hubState.selectedTier) {
        LearningTier.FOUNDATION   -> Color(0xFF00C896)
        LearningTier.ADVANCED     -> Color(0xFF2979FF)
        LearningTier.PROFESSIONAL -> NeonPurple
    }

    // Use live theme colors for background/primary
    val bgColor      = appTheme.background
    val primaryColor = appTheme.primary
    val accentColor  = appTheme.accent

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {

        // Animated background glows — use theme colors
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(
                listOf(appTheme.secondary.copy(alpha = glowAlpha * 0.25f), Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * 0.25f), radius = 380f
            ), 380f, Offset(size.width * 0.15f, size.height * 0.25f))
            drawCircle(Brush.radialGradient(
                listOf(tierColor.copy(alpha = glowAlpha * 0.18f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.75f), radius = 320f
            ), 320f, Offset(size.width * 0.85f, size.height * 0.75f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Top bar: tier badge + title + profile ─────────────────────────
            AnimatedVisibility(visible = menuVisible, enter = fadeIn(tween(500)) + slideInVertically()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo — plain text, never overflows
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "SARVYA QUEST",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                        )
                        Text(
                            "AI-POWERED LEARNING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentColor,
                                letterSpacing = 3.sp
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Tier + Profile row — below logo, no overlap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tier badge
                        Box(
                            modifier = Modifier
                                .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .background(tierColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { onTierSelect() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                hubState.selectedTier.displayName,
                                style = MaterialTheme.typography.labelLarge.copy(color = tierColor)
                            )
                        }

                        // Profile button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(primaryColor.copy(alpha = 0.12f), CircleShape)
                                .border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape)
                                .clickable { onProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "P",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Player card ───────────────────────────────────────────────────
            AnimatedVisibility(visible = menuVisible, enter = fadeIn(tween(700)) + slideInVertically()) {
                GameCard(modifier = Modifier.fillMaxWidth(), borderColor = primaryColor.copy(alpha = 0.35f)) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(progress.player.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1)
                                Text("Level ${progress.player.level} · ${hubState.selectedTier.displayName}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = tierColor),
                                    maxLines = 1)
                            }
                            HeroSprite(
                                direction = HeroDirection.SOUTH,
                                size      = 44.dp,
                                glowColor = primaryColor,
                                showGlow  = true,
                                floatAnim = true
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        StatBar("HP", progress.player.currentHp, progress.player.maxHp, HealthGreen)
                        Spacer(Modifier.height(4.dp))
                        NeonXpBar(progress.player.xp, progress.player.xpToNextLevel,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(2.dp))
                        Text("${progress.player.xp} / ${progress.player.xpToNextLevel} XP  ·  Streak: ${progress.player.streakCount}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                            maxLines = 1)
                        Spacer(Modifier.height(4.dp))
                        StatBar("Energy", progress.player.currentEnergy, progress.player.maxEnergy, EnergyBlue)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── AI Engine status ──────────────────────────────────────────────
            AnimatedVisibility(visible = menuVisible, enter = fadeIn(tween(850))) {
                GameCard(modifier = Modifier.fillMaxWidth(), borderColor = appTheme.secondary.copy(alpha = 0.35f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PulsingGlowDot(appTheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Adaptive Engine",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = appTheme.secondary
                                ),
                                maxLines = 1
                            )
                            Text(
                                "Difficulty: ${progress.digitalTwin.preferredDifficulty.name}  •  Accuracy: ${(progress.digitalTwin.recentAccuracy * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, appTheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .background(appTheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .clickable { onDashboard() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Dashboard",
                                style = MaterialTheme.typography.labelSmall.copy(color = appTheme.secondary))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Game mode buttons ─────────────────────────────────────────────
            AnimatedVisibility(visible = menuVisible, enter = fadeIn(tween(1000)) + slideInVertically()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    MenuButton("Story", "STORY MODE",
                        "Explore zones, battle enemies, unlock the world", NeonCyan) {
                        gameViewModel.setGameMode(GameMode.STORY); onStartStory()
                    }
                    MenuButton("AI", "AI PRACTICE",
                        "Groq-powered adaptive questions personalised for you", tierColor) {
                        gameViewModel.setGameMode(GameMode.PRACTICE); onPractice()
                    }
                    MenuButton("Dungeon", "DUNGEON MODE",
                        "Fast-paced randomised challenges for high XP", NeonOrange) {
                        gameViewModel.setGameMode(GameMode.DUNGEON); onDungeon()
                    }

                    // Tier world map
                    MenuButton("Map", "TIER WORLD MAP",
                        "Explore your ${hubState.selectedTier.displayName} learning world", tierColor,
                        onClick = onTierWorldMap)

                    // Learning Hub
                    MenuButton("Hub", "LEARNING HUB",
                        "Study materials, notes and guides — earn XP for learning", NeonGreen,
                        onClick = onLearningHub)

                    // Stream Feed
                    MenuButton("Stream", "LEARNING STREAM",
                        "Share sessions, replay journeys, see top performers", NeonCyan,
                        onClick = onStreamFeed)

                    // Bottom row
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeonButton("Skill Tree", onClick = onSkillTree,
                            modifier = Modifier.weight(1f), color = NeonPurple)
                        NeonButton("Profile", onClick = onProfile,
                            modifier = Modifier.weight(1f), color = NeonCyan)
                        NeonButton("Settings", onClick = onSettings,
                            modifier = Modifier.weight(1f), color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))   // bottom padding
        }
    }
}

// ── Menu Button ───────────────────────────────────────────────────────────────

@Composable
private fun MenuButton(
    icon: String,
    title: String,
    desc: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Text badge
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .widthIn(min = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color, fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = color, fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
                Text(
                    desc,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
