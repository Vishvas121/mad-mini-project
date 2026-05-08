package com.example.gamefiedsarvya.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gamefiedsarvya.ui.theme.*
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// TopicVideoCard
//
// Embeds a YouTube video via WebView using the YouTube IFrame API.
// Shows a thumbnail/play button first; loads the WebView only when tapped
// (saves bandwidth and avoids auto-play issues).
// Falls back gracefully when offline.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Maps a topic to a curated YouTube video ID.
 * These are stable, educational, public-domain-friendly videos.
 */
fun topicVideoId(topic: String): String? = when (topic.lowercase()) {
    "chemistry"            -> "FSyAehMdpyI"  // Crash Course Chemistry #1
    "physics"              -> "ZM8ECpBuQYE"  // Crash Course Physics #1
    "math", "mathematics"  -> "WUvTyaaNkzM"  // 3Blue1Brown – Essence of Calculus
    "biology"              -> "QnQe0xW_JY4"  // Crash Course Biology #1
    "geography"            -> "kqIas0eKKEY"  // Crash Course Geography #1
    "history"              -> "Yocja_N5s1I"  // Crash Course World History #1
    "science"              -> "OWXoRSIxyIU"  // Crash Course Science
    "technology"           -> "AkFi90lZmXA"  // How computers work – CS50
    "algorithms"           -> "rL8X2mlNHPM"  // Big-O Notation – CS Dojo
    "data structures"      -> "RBSGKlAvoiM"  // Data Structures – CS Dojo
    "economics"            -> "3ez10ADR_gM"  // Crash Course Economics #1
    "english"              -> "0OApTAbNZoI"  // Crash Course English
    "computer science"     -> "tpIctyqH29Q"  // CS50 Lecture 0
    else                   -> null
}

/**
 * Returns a YouTube video ID relevant to the specific question text.
 * Detects keywords to pick a more targeted video than the generic topic one.
 */
fun questionVideoId(questionText: String, topic: String): String? {
    val q = questionText.lowercase()
    return when {
        // Algorithms / CS
        "quicksort" in q || "quick sort" in q -> "SLauY6PpjW4"   // QuickSort visualised
        "merge sort" in q || "mergesort" in q -> "JSceec-j-n0"   // MergeSort visualised
        "binary search" in q                  -> "P3YID7liBug"   // Binary Search
        "big-o" in q || "time complexity" in q -> "v4cd1O4zkGw"  // Big-O explained
        "linked list" in q                    -> "njTh_OwMljA"   // Linked Lists
        "recursion" in q                      -> "IJDJ0kBx2LM"   // Recursion explained
        "hash" in q || "hashmap" in q         -> "KyUTuwz_b7Q"   // Hash Tables
        "graph" in q && "algorithm" in q      -> "tWVWeAqZ0WU"   // Graph algorithms
        "dynamic programming" in q            -> "oBt53YbR9Kk"   // DP explained
        "stack" in q || "queue" in q          -> "wjI1WNcIntg"   // Stack & Queue
        // Physics
        "newton" in q || "force" in q         -> "kKKM8Y-u7ds"   // Newton's Laws
        "wave" in q || "frequency" in q       -> "VKQVGT7EQNA"   // Waves
        "electric" in q || "circuit" in q     -> "mc979OhitAg"   // Electricity
        "gravity" in q || "gravitational" in q -> "MTY1Kje0yLg"  // Gravity
        "quantum" in q                        -> "p7bzE1E5PMY"   // Quantum mechanics intro
        // Chemistry
        "periodic" in q || "element" in q     -> "0RRVV4Diomg"   // Periodic Table
        "bond" in q || "covalent" in q        -> "QqjcCvzWwww"   // Chemical Bonds
        "acid" in q || "base" in q || "ph" in q -> "LS67vS10O5Y" // Acids & Bases
        "reaction" in q || "equation" in q    -> "AcpIO62x2oo"   // Chemical Reactions
        // Math
        "derivative" in q || "differentiat" in q -> "9vKqVkMQHKk" // Derivatives
        "integral" in q || "integrat" in q    -> "rfG8ce4nNh0"   // Integrals
        "probability" in q                    -> "uzkc-qNVoOk"   // Probability
        "matrix" in q || "matrices" in q      -> "kYB8IZa5AuE"   // Matrices
        "trigonometry" in q || "sin" in q || "cos" in q -> "PUB0TaZ7bhA" // Trig
        // Biology
        "dna" in q || "gene" in q             -> "zwibgNGe4aY"   // DNA explained
        "cell" in q || "mitosis" in q         -> "Q-tTuStEWyE"   // Cell division
        "evolution" in q || "natural selection" in q -> "GhHOjC4oxh8" // Evolution
        // Default: fall back to topic video
        else -> topicVideoId(topic)
    }
}

@Composable
fun TopicVideoCard(
    topic: String,
    questionText: String,
    theme: com.example.gamefiedsarvya.ui.theme.TierTheme,
    modifier: Modifier = Modifier
) {
    val videoId = questionVideoId(questionText, topic) ?: topicVideoId(topic)
    if (videoId == null) return

    var playerVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, theme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1117), RoundedCornerShape(12.dp))
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(theme.primary.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFFF0000).copy(alpha = 0.9f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White, fontSize = 10.sp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Watch: $topic",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Educational video • YouTube",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted, fontSize = 9.sp)
                    )
                }
            }
            // Toggle button
            Box(
                modifier = Modifier
                    .border(1.dp, theme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .background(theme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .clickable { playerVisible = !playerVisible }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (playerVisible) "✕ Close" else "▶ Play",
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.primary)
                )
            }
        }

        // Video player (lazy — only rendered when opened)
        AnimatedVisibility(
            visible = playerVisible,
            enter   = expandVertically(tween(300)) + fadeIn(),
            exit    = shrinkVertically(tween(200)) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            ) {
                YouTubeWebView(videoId = videoId)
            }
        }

        // Thumbnail placeholder when closed
        if (!playerVisible) {
            YouTubeThumbnail(
                videoId = videoId,
                theme   = theme,
                onClick = { playerVisible = true }
            )
        }
    }
}

@Composable
private fun YouTubeWebView(videoId: String) {
    // youtube-nocookie.com embed works in WebView without CSP issues.
    // The IFrame API approach is blocked by YouTube's Content Security Policy.
    // Direct embed URL with autoplay=1 works when mediaPlaybackRequiresUserGesture=false.
    val embedUrl = "https://www.youtube-nocookie.com/embed/$videoId" +
        "?autoplay=1&rel=0&modestbranding=1&playsinline=1&controls=1&fs=1"

    val chromeUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/124.0.6367.82 Mobile Safari/537.36"

    val loadedId = remember { mutableStateOf("") }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled                = true
                    domStorageEnabled                = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode             = true
                    useWideViewPort                  = true
                    allowContentAccess               = true
                    allowFileAccess                  = true
                    userAgentString                  = chromeUA
                    @Suppress("DEPRECATION")
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                webChromeClient = WebChromeClient()
                webViewClient   = WebViewClient()
                loadUrl(embedUrl)
                loadedId.value = videoId
            }
        },
        update = { webView ->
            if (loadedId.value != videoId) {
                webView.loadUrl(embedUrl)
                loadedId.value = videoId
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun YouTubeThumbnail(
    videoId: String,
    theme: com.example.gamefiedsarvya.ui.theme.TierTheme,
    onClick: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "thumb")
    val pulse by inf.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "tp"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(Color(0xFF1A1A2E))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Decorative grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 24f
            var x = 0f
            while (x < size.width) {
                drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color.White.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Play button
            Box(
                modifier = Modifier
                    .size((56 * pulse).dp)
                    .background(Color(0xFFFF0000).copy(alpha = 0.9f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White, fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap to watch video",
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
            )
            Text(
                "youtu.be/$videoId",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted, fontSize = 9.sp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QuestionVisualAid
//
// Detects the question type and renders the appropriate illustration:
//   - Sorting/algorithm → animated bar chart
//   - Graph/tree        → node-edge diagram
//   - Physics/wave      → sine wave
//   - Chemistry/atom    → Bohr atom model
//   - Math/geometry     → geometric shapes
//   - Biology/DNA       → double helix
//   - Generic           → concept diagram
// ─────────────────────────────────────────────────────────────────────────────

enum class VisualAidType {
    SORTING_BARS, GRAPH_NODES, SINE_WAVE, ATOM_MODEL,
    GEOMETRY, DNA_HELIX, BINARY_TREE, FORCE_DIAGRAM, GENERIC
}

fun detectVisualAidType(questionText: String, topic: String): VisualAidType {
    val q = questionText.lowercase()
    val t = topic.lowercase()
    return when {
        "sort" in q || "quicksort" in q || "merge sort" in q || "bubble" in q -> VisualAidType.SORTING_BARS
        "graph" in q || "node" in q || "edge" in q || "bfs" in q || "dfs" in q -> VisualAidType.GRAPH_NODES
        "tree" in q || "binary tree" in q || "bst" in q -> VisualAidType.BINARY_TREE
        "wave" in q || "frequency" in q || "amplitude" in q || "oscillat" in q -> VisualAidType.SINE_WAVE
        "atom" in q || "electron" in q || "orbital" in q || "nucleus" in q -> VisualAidType.ATOM_MODEL
        "force" in q || "newton" in q || "velocity" in q || "acceleration" in q -> VisualAidType.FORCE_DIAGRAM
        "dna" in q || "helix" in q || "gene" in q || "chromosome" in q -> VisualAidType.DNA_HELIX
        "angle" in q || "triangle" in q || "circle" in q || "polygon" in q -> VisualAidType.GEOMETRY
        t == "physics" -> VisualAidType.SINE_WAVE
        t == "chemistry" -> VisualAidType.ATOM_MODEL
        t == "biology" -> VisualAidType.DNA_HELIX
        t == "algorithms" || t == "data structures" -> VisualAidType.SORTING_BARS
        t == "math" || t == "mathematics" -> VisualAidType.GEOMETRY
        else -> VisualAidType.GENERIC
    }
}

@Composable
fun QuestionVisualAid(
    questionText: String,
    topic: String,
    theme: com.example.gamefiedsarvya.ui.theme.TierTheme,
    modifier: Modifier = Modifier
) {
    val type = remember(questionText, topic) { detectVisualAidType(questionText, topic) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, theme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .background(theme.surface.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        when (type) {
            VisualAidType.SORTING_BARS  -> SortingBarsIllustration(theme)
            VisualAidType.GRAPH_NODES   -> GraphNodesIllustration(theme)
            VisualAidType.BINARY_TREE   -> BinaryTreeIllustration(theme)
            VisualAidType.SINE_WAVE     -> SineWaveIllustration(theme)
            VisualAidType.ATOM_MODEL    -> AtomModelIllustration(theme)
            VisualAidType.FORCE_DIAGRAM -> ForceDiagramIllustration(theme)
            VisualAidType.DNA_HELIX     -> DnaHelixIllustration(theme)
            VisualAidType.GEOMETRY      -> GeometryIllustration(theme)
            VisualAidType.GENERIC       -> GenericConceptIllustration(theme, topic)
        }

        // Label overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = theme.primary.copy(alpha = 0.9f), fontSize = 9.sp)
            )
        }
    }
}

// ── Sorting bars (animated) ───────────────────────────────────────────────────

@Composable
private fun SortingBarsIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val heights = remember { listOf(0.9f, 0.4f, 0.75f, 0.3f, 0.6f, 0.85f, 0.5f, 0.65f) }
    val inf = rememberInfiniteTransition(label = "bars")
    val offset by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "bo"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barW = size.width / (heights.size * 1.5f)
        val gap  = barW * 0.5f
        val totalW = heights.size * (barW + gap)
        val startX = (size.width - totalW) / 2f

        heights.forEachIndexed { i, h ->
            val x = startX + i * (barW + gap)
            // Animate a "highlight" sweeping across bars
            val highlight = ((offset * heights.size - i + heights.size) % heights.size) < 1f
            val color = if (highlight) theme.accent else theme.primary.copy(alpha = 0.7f)
            val barH = size.height * 0.75f * h
            drawRoundRect(
                color    = color,
                topLeft  = Offset(x, size.height - barH - 12f),
                size     = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
        }

        // X-axis
        drawLine(
            color = theme.primary.copy(alpha = 0.3f),
            start = Offset(startX - 4f, size.height - 12f),
            end   = Offset(startX + totalW + 4f, size.height - 12f),
            strokeWidth = 1.5f
        )
    }
}

// ── Graph nodes ───────────────────────────────────────────────────────────────

@Composable
private fun GraphNodesIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val inf = rememberInfiniteTransition(label = "graph")
    val pulse by inf.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "gp"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val nodes = listOf(
            Offset(size.width * 0.5f, size.height * 0.2f),
            Offset(size.width * 0.2f, size.height * 0.55f),
            Offset(size.width * 0.8f, size.height * 0.55f),
            Offset(size.width * 0.35f, size.height * 0.85f),
            Offset(size.width * 0.65f, size.height * 0.85f)
        )
        val edges = listOf(0 to 1, 0 to 2, 1 to 3, 2 to 4, 1 to 2, 3 to 4)

        edges.forEach { (a, b) ->
            drawLine(
                color = theme.primary.copy(alpha = 0.35f),
                start = nodes[a], end = nodes[b], strokeWidth = 1.5f
            )
        }
        nodes.forEachIndexed { i, pos ->
            val r = if (i == 0) 18f * pulse else 14f
            drawCircle(color = theme.primary.copy(alpha = 0.15f), radius = r + 4f, center = pos)
            drawCircle(color = theme.primary, radius = r, center = pos, style = Stroke(2f))
            drawCircle(color = theme.primary.copy(alpha = 0.6f), radius = 5f, center = pos)
        }
    }
}

// ── Binary tree ───────────────────────────────────────────────────────────────

@Composable
private fun BinaryTreeIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val root = Offset(size.width / 2f, size.height * 0.15f)
        val l1   = listOf(
            Offset(size.width * 0.28f, size.height * 0.45f),
            Offset(size.width * 0.72f, size.height * 0.45f)
        )
        val l2 = listOf(
            Offset(size.width * 0.14f, size.height * 0.78f),
            Offset(size.width * 0.42f, size.height * 0.78f),
            Offset(size.width * 0.58f, size.height * 0.78f),
            Offset(size.width * 0.86f, size.height * 0.78f)
        )

        // Edges root → l1
        l1.forEach { drawLine(theme.primary.copy(alpha = 0.4f), root, it, 1.5f) }
        // Edges l1 → l2
        drawLine(theme.primary.copy(alpha = 0.4f), l1[0], l2[0], 1.5f)
        drawLine(theme.primary.copy(alpha = 0.4f), l1[0], l2[1], 1.5f)
        drawLine(theme.primary.copy(alpha = 0.4f), l1[1], l2[2], 1.5f)
        drawLine(theme.primary.copy(alpha = 0.4f), l1[1], l2[3], 1.5f)

        // Nodes
        fun node(pos: Offset, r: Float, accent: Boolean) {
            drawCircle(theme.primary.copy(alpha = if (accent) 0.25f else 0.1f), r + 4f, pos)
            drawCircle(theme.primary, r, pos, style = Stroke(2f))
        }
        node(root, 16f, true)
        l1.forEach { node(it, 13f, false) }
        l2.forEach { node(it, 10f, false) }
    }
}

// ── Sine wave (physics) ───────────────────────────────────────────────────────

@Composable
private fun SineWaveIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val inf = rememberInfiniteTransition(label = "wave")
    val phase by inf.animateFloat(
        0f, (2 * Math.PI).toFloat(),
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "wp"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cy = size.height / 2f
        val amp = size.height * 0.32f
        val freq = 2f

        // Draw two waves (different phase/color for interference effect)
        for (wave in 0..1) {
            val wPhase = phase + wave * (Math.PI / 2).toFloat()
            val wColor = if (wave == 0) theme.primary else theme.secondary.copy(alpha = 0.5f)
            val path = Path()
            var first = true
            var x = 0f
            while (x <= size.width) {
                val y = cy - amp * sin((freq * Math.PI * 2 * x / size.width + wPhase).toDouble()).toFloat()
                if (first) { path.moveTo(x, y); first = false } else path.lineTo(x, y)
                x += 2f
            }
            drawPath(path, wColor, style = Stroke(2.5f))
        }

        // Axis
        drawLine(theme.primary.copy(alpha = 0.2f), Offset(0f, cy), Offset(size.width, cy), 1f)

        // Wavelength arrow
        val arrowY = cy + amp + 18f
        drawLine(theme.primary.copy(alpha = 0.5f),
            Offset(size.width * 0.1f, arrowY), Offset(size.width * 0.6f, arrowY), 1.5f)
    }
}

// ── Atom model (Bohr) ─────────────────────────────────────────────────────────

@Composable
private fun AtomModelIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val inf = rememberInfiniteTransition(label = "atom")
    val angle by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "aa"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Nucleus
        drawCircle(theme.accent.copy(alpha = 0.3f), 18f, Offset(cx, cy))
        drawCircle(theme.accent, 12f, Offset(cx, cy), style = Stroke(2.5f))
        drawCircle(theme.accent.copy(alpha = 0.8f), 5f, Offset(cx, cy))

        // Orbits (3 ellipses at different tilts)
        val orbits = listOf(
            Triple(50f, 22f, 0f),
            Triple(50f, 22f, 60f),
            Triple(50f, 22f, 120f)
        )
        orbits.forEachIndexed { i, (rx, ry, tilt) ->
            rotate(tilt, Offset(cx, cy)) {
                drawOval(
                    color    = theme.primary.copy(alpha = 0.35f),
                    topLeft  = Offset(cx - rx, cy - ry),
                    size     = Size(rx * 2, ry * 2),
                    style    = Stroke(1.5f)
                )
                // Electron on this orbit
                val eAngle = Math.toRadians((angle + i * 120.0)).toFloat()
                val ex = cx + rx * cos(eAngle)
                val ey = cy + ry * sin(eAngle)
                drawCircle(theme.primary, 5f, Offset(ex, ey))
                drawCircle(theme.primary.copy(alpha = 0.3f), 9f, Offset(ex, ey))
            }
        }
    }
}

// ── Force diagram (physics) ───────────────────────────────────────────────────

@Composable
private fun ForceDiagramIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val inf = rememberInfiniteTransition(label = "force")
    val arrowLen by inf.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fl"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Object box
        drawRoundRect(
            color    = theme.primary.copy(alpha = 0.2f),
            topLeft  = Offset(cx - 28f, cy - 22f),
            size     = Size(56f, 44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        drawRoundRect(
            color    = theme.primary.copy(alpha = 0.7f),
            topLeft  = Offset(cx - 28f, cy - 22f),
            size     = Size(56f, 44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f),
            style    = Stroke(2f)
        )

        // Force arrows
        fun arrow(from: Offset, to: Offset, color: Color) {
            drawLine(color, from, to, 3f)
            val dx = to.x - from.x; val dy = to.y - from.y
            val len = sqrt(dx * dx + dy * dy)
            val ux = dx / len; val uy = dy / len
            val headLen = 12f
            drawLine(color, to, Offset(to.x - headLen * (ux + uy * 0.5f), to.y - headLen * (uy - ux * 0.5f)), 3f)
            drawLine(color, to, Offset(to.x - headLen * (ux - uy * 0.5f), to.y - headLen * (uy + ux * 0.5f)), 3f)
        }

        val maxLen = size.width * 0.28f * arrowLen
        arrow(Offset(cx + 28f, cy), Offset(cx + 28f + maxLen, cy), NeonGreen)          // right force
        arrow(Offset(cx - 28f, cy), Offset(cx - 28f - maxLen * 0.6f, cy), NeonRed)     // friction
        arrow(Offset(cx, cy - 22f), Offset(cx, cy - 22f - maxLen * 0.5f), NeonCyan)    // normal
        arrow(Offset(cx, cy + 22f), Offset(cx, cy + 22f + maxLen * 0.4f), NeonOrange)  // gravity
    }
}

// ── DNA helix ─────────────────────────────────────────────────────────────────

@Composable
private fun DnaHelixIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val inf = rememberInfiniteTransition(label = "dna")
    val scroll by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "ds"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val amp = size.width * 0.22f
        val steps = 60
        val basePairs = listOf(NeonGreen, NeonCyan, NeonOrange, NeonPurple)

        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val y = size.height * t
            val phase = (t + scroll) * 2 * Math.PI.toFloat()

            val x1 = cx + amp * cos(phase)
            val x2 = cx + amp * cos(phase + Math.PI.toFloat())

            // Backbone dots
            if (i % 3 == 0) {
                drawCircle(theme.primary.copy(alpha = 0.7f), 4f, Offset(x1, y))
                drawCircle(theme.secondary.copy(alpha = 0.7f), 4f, Offset(x2, y))
            }

            // Base pair rungs
            if (i % 6 == 0) {
                val pairColor = basePairs[(i / 6) % basePairs.size]
                drawLine(pairColor.copy(alpha = 0.6f), Offset(x1, y), Offset(x2, y), 2f)
            }
        }
    }
}

// ── Geometry illustration ─────────────────────────────────────────────────────

@Composable
private fun GeometryIllustration(theme: com.example.gamefiedsarvya.ui.theme.TierTheme) {
    val inf = rememberInfiniteTransition(label = "geo")
    val rot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "gr"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Rotating triangle
        rotate(rot, Offset(cx, cy)) {
            val r = size.height * 0.35f
            val pts = (0..2).map { i ->
                val a = Math.toRadians(i * 120.0 - 90.0).toFloat()
                Offset(cx + r * cos(a), cy + r * sin(a))
            }
            val path = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            drawPath(path, theme.primary.copy(alpha = 0.12f))
            drawPath(path, theme.primary.copy(alpha = 0.7f), style = Stroke(2f))
        }

        // Inscribed circle
        drawCircle(theme.accent.copy(alpha = 0.15f), size.height * 0.22f, Offset(cx, cy))
        drawCircle(theme.accent.copy(alpha = 0.5f), size.height * 0.22f, Offset(cx, cy), style = Stroke(1.5f))

        // Right-angle marker
        val sq = 14f
        drawRect(theme.secondary.copy(alpha = 0.5f),
            Offset(cx - sq / 2, cy - sq / 2), Size(sq, sq), style = Stroke(1.5f))
    }
}

// ── Generic concept illustration ──────────────────────────────────────────────

@Composable
private fun GenericConceptIllustration(
    theme: com.example.gamefiedsarvya.ui.theme.TierTheme,
    topic: String
) {
    val inf = rememberInfiniteTransition(label = "gen")
    val pulse by inf.animateFloat(
        0.8f, 1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "gp"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Concentric rings
            for (i in 1..4) {
                drawCircle(
                    color  = theme.primary.copy(alpha = 0.06f * (5 - i)),
                    radius = size.minDimension * 0.12f * i * pulse,
                    center = Offset(cx, cy)
                )
            }
            drawCircle(theme.primary.copy(alpha = 0.3f), 28f * pulse, Offset(cx, cy))
            drawCircle(theme.primary, 28f * pulse, Offset(cx, cy), style = Stroke(2f))
        }
        Text(
            topicIconForCanvas(topic),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp)
        )
    }
}

private fun topicIconForCanvas(topic: String): String = when (topic.lowercase()) {
    "chemistry"   -> "⚗️"
    "physics"     -> "⚡"
    "math", "mathematics" -> "∑"
    "biology"     -> "🧬"
    "geography"   -> "🌍"
    "history"     -> "📜"
    "science"     -> "🔬"
    "technology"  -> "💻"
    "algorithms"  -> "⟳"
    "data structures" -> "🗂"
    else          -> "📚"
}
