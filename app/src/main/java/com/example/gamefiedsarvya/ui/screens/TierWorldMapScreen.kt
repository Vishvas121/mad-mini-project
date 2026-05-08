package com.example.gamefiedsarvya.ui.screens

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.WorldMapRepository
import com.example.gamefiedsarvya.ui.assets.*
import com.example.gamefiedsarvya.ui.components.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.GameViewModel
import com.example.gamefiedsarvya.viewmodel.LearningHubViewModel

// ── Per-tier curated learning resources ───────────────────────────────────────

private data class LearningResource(
    val title: String,
    val url: String,
    val type: String,   // "website", "video", "docs"
    val emoji: String
)

private fun resourcesForNode(node: MapNode, tier: LearningTier): List<LearningResource> {
    val topic = node.topic.lowercase()
    return when (tier) {
        LearningTier.FOUNDATION -> foundationResources(topic)
        LearningTier.ADVANCED   -> advancedResources(topic)
        LearningTier.PROFESSIONAL -> professionalResources(topic)
    }
}

private fun foundationResources(topic: String): List<LearningResource> = when {
    "math" in topic || "number" in topic -> listOf(
        LearningResource("Khan Academy – Math", "https://www.khanacademy.org/math", "website", "📐"),
        LearningResource("Math is Fun", "https://www.mathsisfun.com", "website", "🎮"),
        LearningResource("Numberphile", "https://www.youtube.com/c/numberphile", "video", "▶️")
    )
    "science" in topic -> listOf(
        LearningResource("Khan Academy – Science", "https://www.khanacademy.org/science", "website", "🔬"),
        LearningResource("National Geographic Kids", "https://kids.nationalgeographic.com", "website", "🌍"),
        LearningResource("Crash Course Kids", "https://www.youtube.com/user/crashcoursekids", "video", "▶️")
    )
    "language" in topic || "english" in topic -> listOf(
        LearningResource("BBC Learning English", "https://www.bbc.co.uk/learningenglish", "website", "📝"),
        LearningResource("Duolingo", "https://www.duolingo.com", "website", "🦜"),
        LearningResource("Oxford Owl", "https://www.oxfordowl.co.uk", "website", "📚")
    )
    "geography" in topic -> listOf(
        LearningResource("National Geographic", "https://www.nationalgeographic.com/education", "website", "🌍"),
        LearningResource("Geography Games", "https://www.geoguessr.com", "website", "🗺️"),
        LearningResource("Crash Course Geography", "https://www.youtube.com/playlist?list=PL8dPuuaLjXtO85Sl24rSiClEEbmEPRqtb", "video", "▶️")
    )
    "technology" in topic -> listOf(
        LearningResource("Code.org", "https://code.org", "website", "💻"),
        LearningResource("Scratch", "https://scratch.mit.edu", "website", "🐱"),
        LearningResource("CS Unplugged", "https://csunplugged.org", "website", "🎲")
    )
    else -> listOf(
        LearningResource("Khan Academy", "https://www.khanacademy.org", "website", "📚"),
        LearningResource("BBC Bitesize", "https://www.bbc.co.uk/bitesize", "website", "🎓"),
        LearningResource("Crash Course", "https://www.youtube.com/user/crashcourse", "video", "▶️")
    )
}

private fun advancedResources(topic: String): List<LearningResource> = when {
    "math" in topic || "calculus" in topic || "algebra" in topic -> listOf(
        LearningResource("Khan Academy – Calculus", "https://www.khanacademy.org/math/calculus-1", "website", "📐"),
        LearningResource("3Blue1Brown", "https://www.youtube.com/c/3blue1brown", "video", "▶️"),
        LearningResource("Paul's Online Math Notes", "https://tutorial.math.lamar.edu", "website", "📝"),
        LearningResource("Brilliant.org", "https://brilliant.org/courses/calculus", "website", "💡")
    )
    "physics" in topic || "science" in topic -> listOf(
        LearningResource("Khan Academy – Physics", "https://www.khanacademy.org/science/physics", "website", "⚡"),
        LearningResource("Physics Classroom", "https://www.physicsclassroom.com", "website", "🔭"),
        LearningResource("Crash Course Physics", "https://www.youtube.com/playlist?list=PL8dPuuaLjXtN0ge7yDk_UA0ldZJdhwkoV", "video", "▶️"),
        LearningResource("HyperPhysics", "http://hyperphysics.phy-astr.gsu.edu", "website", "🌌")
    )
    "chemistry" in topic -> listOf(
        LearningResource("Khan Academy – Chemistry", "https://www.khanacademy.org/science/chemistry", "website", "⚗️"),
        LearningResource("Chemguide", "https://www.chemguide.co.uk", "website", "🧪"),
        LearningResource("Crash Course Chemistry", "https://www.youtube.com/playlist?list=PL8dPuuaLjXtPHzzYuWy6fYEaX9mQQ8oGr", "video", "▶️")
    )
    "competitive" in topic || "jee" in topic || "neet" in topic -> listOf(
        LearningResource("Unacademy", "https://unacademy.com", "website", "🎯"),
        LearningResource("BYJU'S", "https://byjus.com", "website", "📱"),
        LearningResource("Vedantu", "https://www.vedantu.com", "website", "🏆"),
        LearningResource("PW (Physics Wallah)", "https://www.pw.live", "website", "⚡")
    )
    else -> listOf(
        LearningResource("Khan Academy", "https://www.khanacademy.org", "website", "📚"),
        LearningResource("Coursera", "https://www.coursera.org", "website", "🎓"),
        LearningResource("MIT OpenCourseWare", "https://ocw.mit.edu", "website", "🏛️")
    )
}

private fun professionalResources(topic: String): List<LearningResource> = when {
    "cs" in topic || "algorithm" in topic || "data structure" in topic -> listOf(
        LearningResource("LeetCode", "https://leetcode.com", "website", "💻"),
        LearningResource("GeeksforGeeks", "https://www.geeksforgeeks.org", "website", "🌿"),
        LearningResource("CS50 – Harvard", "https://cs50.harvard.edu", "website", "🏛️"),
        LearningResource("MIT 6.006 Algorithms", "https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-fall-2011", "website", "📐")
    )
    "ai" in topic || "ml" in topic || "machine learning" in topic -> listOf(
        LearningResource("fast.ai", "https://www.fast.ai", "website", "🤖"),
        LearningResource("Hugging Face", "https://huggingface.co/learn", "website", "🤗"),
        LearningResource("Google ML Crash Course", "https://developers.google.com/machine-learning/crash-course", "website", "🧠"),
        LearningResource("Andrej Karpathy – YouTube", "https://www.youtube.com/@AndrejKarpathy", "video", "▶️")
    )
    "engineering" in topic || "system" in topic || "cloud" in topic -> listOf(
        LearningResource("System Design Primer", "https://github.com/donnemartin/system-design-primer", "website", "🏗️"),
        LearningResource("AWS Training", "https://aws.amazon.com/training", "website", "☁️"),
        LearningResource("roadmap.sh", "https://roadmap.sh", "website", "🗺️"),
        LearningResource("The Pragmatic Engineer", "https://newsletter.pragmaticengineer.com", "website", "⚙️")
    )
    "security" in topic -> listOf(
        LearningResource("OWASP", "https://owasp.org", "website", "🔒"),
        LearningResource("TryHackMe", "https://tryhackme.com", "website", "🎯"),
        LearningResource("Cybrary", "https://www.cybrary.it", "website", "🛡️")
    )
    else -> listOf(
        LearningResource("MIT OpenCourseWare", "https://ocw.mit.edu", "website", "🏛️"),
        LearningResource("arXiv", "https://arxiv.org", "website", "📄"),
        LearningResource("GitHub", "https://github.com/explore", "website", "🐙"),
        LearningResource("Stack Overflow", "https://stackoverflow.com", "website", "💬")
    )
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun TierWorldMapScreen(
    tier: LearningTier,
    gameViewModel: GameViewModel,
    hubViewModel: LearningHubViewModel,
    onNodeSelected: (MapNode) -> Unit,
    onBack: () -> Unit
) {
    val progress  by gameViewModel.progress.collectAsState()
    val hubState  by hubViewModel.uiState.collectAsState()
    val theme     = com.example.gamefiedsarvya.ui.theme.TierThemes.forTier(tier)

    val worldMap  = remember(tier) { WorldMapRepository.getMapForTier(tier) }
    val completedIds = remember { mutableStateOf(setOf<String>()) }

    var selectedNode    by remember { mutableStateOf<MapNode?>(null) }
    var showNodeDetail  by remember { mutableStateOf(false) }
    var showResources   by remember { mutableStateOf(false) }
    var openResourceUrl by remember { mutableStateOf<String?>(null) }

    // If a resource URL is open, show the WebView browser
    openResourceUrl?.let { url ->
        EmbeddedWebBrowser(
            url     = url,
            onClose = { openResourceUrl = null }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        when (tier) {
            LearningTier.FOUNDATION   -> FoundationMapBackground(Modifier.fillMaxSize())
            LearningTier.ADVANCED     -> AdvancedMapBackground(Modifier.fillMaxSize())
            LearningTier.PROFESSIONAL -> ProfessionalMapBackground(Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(worldMap.title, style = theme.titleStyle.copy(color = theme.primary))
                    Text("${tier.icon} ${tier.displayName}", style = theme.labelStyle.copy(color = theme.secondary))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${completedIds.value.size}/${worldMap.nodes.size}",
                        style = theme.labelStyle.copy(color = theme.accent))
                    Text("nodes", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }

            MapLegend(tier = tier, theme = theme)

            WorldMapCanvas(
                worldMap         = worldMap,
                completedNodeIds = completedIds.value,
                selectedNodeId   = selectedNode?.id,
                onNodeTap        = { node ->
                    selectedNode   = node
                    showNodeDetail = true
                    showResources  = false
                },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(8.dp))
        }

        // ── Node detail panel ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = showNodeDetail && selectedNode != null,
            enter    = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit     = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedNode?.let { node ->
                NodeDetailPanel(
                    node          = node,
                    tier          = tier,
                    theme         = theme,
                    showResources = showResources,
                    onStart       = {
                        completedIds.value = completedIds.value + node.id
                        showNodeDetail = false
                        onNodeSelected(node)
                    },
                    onToggleResources = { showResources = !showResources },
                    onOpenResource    = { url -> openResourceUrl = url },
                    onDismiss         = { showNodeDetail = false; showResources = false }
                )
            }
        }
    }
}

// ── Embedded web browser ──────────────────────────────────────────────────────

@Composable
private fun EmbeddedWebBrowser(url: String, onClose: () -> Unit) {
    val chromeUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36"

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled   = true
                        domStorageEnabled   = true
                        loadWithOverviewMode = true
                        useWideViewPort     = true
                        userAgentString     = chromeUA
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient   = WebViewClient()
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Close button overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                .border(1.dp, TextSecondary.copy(alpha = 0.5f), CircleShape)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Text("✕", style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary))
        }
    }
}

// ── Map Legend ────────────────────────────────────────────────────────────────

@Composable
private fun MapLegend(tier: LearningTier, theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LegendItem("Lesson", theme.primary)
        LegendItem("Quiz",   Color(0xFFFFB300))
        LegendItem("Boss",   Color(0xFFFF073A))
        LegendItem("Hub",    Color(0xFF00F5FF))
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

// ── Node Detail Panel ─────────────────────────────────────────────────────────

@Composable
private fun NodeDetailPanel(
    node: MapNode,
    tier: LearningTier,
    theme: com.example.gamefiedsarvya.ui.theme.TierTheme,
    showResources: Boolean,
    onStart: () -> Unit,
    onToggleResources: () -> Unit,
    onOpenResource: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val nodeColor = when (node.type) {
        MapNodeType.LESSON -> theme.primary
        MapNodeType.QUIZ   -> Color(0xFFFFB300)
        MapNodeType.BOSS   -> Color(0xFFFF073A)
        MapNodeType.HUB    -> Color(0xFF00F5FF)
        else               -> TextSecondary
    }
    val resources = remember(node.id, tier) { resourcesForNode(node, tier) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, theme.background.copy(alpha = 0.97f))),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .border(1.dp, nodeColor.copy(alpha = 0.4f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
    ) {
        // Drag handle
        Box(modifier = Modifier.width(40.dp).height(4.dp)
            .background(TextMuted, RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))

        // Node info
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(node.icon, fontSize = 24.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(node.label.replace("\n", " "), style = theme.titleStyle.copy(color = nodeColor))
                }
                Spacer(Modifier.height(4.dp))
                Text(node.description, style = theme.bodyStyle.copy(color = TextSecondary))
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Topic: ${node.topic}", style = theme.labelStyle.copy(color = theme.secondary))
                    Text("+${node.xpReward} XP", style = theme.labelStyle.copy(color = Color(0xFFFFD700)))
                }
            }
            NeonButton("✕", onClick = onDismiss, color = TextMuted)
        }

        Spacer(Modifier.height(12.dp))

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Resources toggle
            NeonButton(
                text     = if (showResources) "▲ Resources" else "📚 Resources (${resources.size})",
                onClick  = onToggleResources,
                modifier = Modifier.weight(1f),
                color    = theme.secondary
            )
            // Start button
            NeonButton(
                text = when (node.type) {
                    MapNodeType.BOSS -> "⚔ Fight Boss"
                    MapNodeType.QUIZ -> "⏱ Start Quiz"
                    MapNodeType.HUB  -> "📖 Open Hub"
                    else             -> "▶ Start"
                },
                onClick  = onStart,
                modifier = Modifier.weight(1f),
                color    = nodeColor
            )
        }

        // Resources panel
        AnimatedVisibility(visible = showResources) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HorizontalDivider(color = nodeColor.copy(alpha = 0.2f))
                Spacer(Modifier.height(4.dp))
                Text("Learning Resources", style = theme.labelStyle.copy(color = nodeColor, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                resources.forEach { res ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(nodeColor.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                            .border(1.dp, nodeColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .clickable { onOpenResource(res.url) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(res.emoji, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(res.title, style = theme.bodyStyle.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                            Text(res.type, style = theme.labelStyle.copy(color = TextMuted, fontSize = 9.sp))
                        }
                        Text("→", style = theme.labelStyle.copy(color = nodeColor))
                    }
                }
            }
        }
    }
}

/**
