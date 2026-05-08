package com.example.gamefiedsarvya.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.ui.components.NeonButton
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.viewmodel.SessionViewModel
import com.example.gamefiedsarvya.viewmodel.UserProfileViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StreamFeedScreen(
    sessionViewModel: SessionViewModel,
    profileViewModel: UserProfileViewModel,
    onReplay: (LearningSession) -> Unit,
    onBack: () -> Unit
) {
    val state        by sessionViewModel.state.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val context      = LocalContext.current

    var activeTab     by remember { mutableIntStateOf(0) }
    var showCodeSheet by remember { mutableStateOf(false) }
    var replayCode    by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sessionViewModel.loadPublicStream()
        sessionViewModel.loadUserSessions(profileState.profile.displayName)
        sessionViewModel.loadTopPerformers()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080B10))) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Back button — left
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .border(1.dp, Color(0xFF2A2D3E), RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Back",
                        style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary))
                }

                // Title — center
                Text(
                    "Activity",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Replay button — right
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .clickable { showCodeSheet = !showCodeSheet }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Enter Code",
                        style = MaterialTheme.typography.labelLarge.copy(color = NeonCyan))
                }
            }

            // ── Code input sheet ──────────────────────────────────────────────
            if (showCodeSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = replayCode,
                            onValueChange = { replayCode = it.uppercase().take(8) },
                            placeholder   = { Text("Share code  e.g. AB3X7K2M", color = TextMuted) },
                            singleLine    = true,
                            modifier      = Modifier.weight(1f),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = NeonCyan,
                                unfocusedBorderColor = Color(0xFF2A2D3E),
                                focusedTextColor     = TextPrimary,
                                unfocusedTextColor   = TextPrimary,
                                cursorColor          = NeonCyan
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = TextPrimary, letterSpacing = 3.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (replayCode.length >= 6) NeonCyan else Color(0xFF2A2D3E),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = replayCode.length >= 6) {
                                    sessionViewModel.loadSessionByCode(replayCode)
                                    showCodeSheet = false
                                    replayCode = ""
                                }
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text("Load",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (replayCode.length >= 6) Color(0xFF080B10) else TextMuted
                                ))
                        }
                    }
                }
                Divider(color = Color(0xFF1A1D24), thickness = 1.dp)
            }

            // ── Tab bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
            ) {
                listOf("Feed", "My Activity", "Leaderboard").forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = i }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (activeTab == i) TextPrimary else TextMuted,
                                    fontWeight = if (activeTab == i) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(32.dp)
                                    .background(
                                        if (activeTab == i) NeonCyan else Color.Transparent,
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Divider(color = Color(0xFF1A1D24), thickness = 1.dp)

            // ── Content ───────────────────────────────────────────────────────
            if (state.isLoadingStream) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                ) {
                    when (activeTab) {
                        0 -> {
                            if (state.publicStream.isEmpty()) {
                                item { EmptyFeed("No activity yet. Complete a session to appear here.") }
                            } else {
                                items(state.publicStream, key = { it.id }) { card ->
                                    ActivityCard(
                                        card    = card,
                                        onShare = { shareSession(context, card.shareCode, card.userName) },
                                        onCopy  = { copyCode(context, card.shareCode) },
                                        onReplay = { sessionViewModel.loadSessionByCode(card.shareCode) }
                                    )
                                    Divider(
                                        color = Color(0xFF1A1D24),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                        1 -> {
                            if (state.userSessions.isEmpty()) {
                                item { EmptyFeed("No sessions recorded yet.\nFinish a practice to see your activity.") }
                            } else {
                                items(state.userSessions, key = { it.id }) { session ->
                                    MySessionCard(
                                        session  = session,
                                        onShare  = { shareSession(context, session.shareCode, session.userName) },
                                        onCopy   = { copyCode(context, session.shareCode) },
                                        onReplay = { onReplay(session) }
                                    )
                                    Divider(
                                        color = Color(0xFF1A1D24),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                        2 -> {
                            if (state.topPerformers.isEmpty()) {
                                item { EmptyFeed("Leaderboard is empty.\nBe the first to set a score.") }
                            } else {
                                item {
                                    Text(
                                        "Top performers by accuracy",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted, letterSpacing = 1.sp
                                        ),
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp, vertical = 10.dp
                                        )
                                    )
                                }
                                itemsIndexed(
                                    state.topPerformers,
                                    key = { _, c -> c.id }
                                ) { idx, card ->
                                    LeaderboardRow(rank = idx + 1, card = card)
                                    Divider(
                                        color = Color(0xFF1A1D24),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error toast
        if (state.errorMessage.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color(0xFF1A0808), RoundedCornerShape(8.dp))
                    .border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = NeonRed))
            }
            LaunchedEffect(state.errorMessage) {
                delay(3000)
                sessionViewModel.clearError()
            }
        }
    }
}

// ── Activity Card (public feed — Strava style) ────────────────────────────────

@Composable
private fun ActivityCard(
    card: StreamCard,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onReplay: () -> Unit
) {
    val tierLabel = card.tier.lowercase().replaceFirstChar { it.uppercase() }
    val diffColor = when {
        card.accuracyPct >= 80 -> Color(0xFF2ECC71)
        card.accuracyPct >= 50 -> Color(0xFFE67E22)
        else                   -> Color(0xFFE74C3C)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF080B10))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Row 1: Avatar + name + time
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    card.userName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = NeonCyan, fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(card.userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary, fontWeight = FontWeight.SemiBold
                    ))
                Text("$tierLabel  ·  ${card.topic}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
            }

            Text(
                formatRelativeTime(card.createdAt),
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }

        Spacer(Modifier.height(14.dp))

        // Row 2: Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            StatCell(label = "Accuracy", value = "${card.accuracyPct}%", color = diffColor,
                modifier = Modifier.weight(1f))
            StatDivider()
            StatCell(label = "Score", value = "${card.correctAnswers}/${card.totalQuestions}",
                color = TextPrimary, modifier = Modifier.weight(1f))
            StatDivider()
            StatCell(label = "XP", value = "+${card.xpEarned}",
                color = Color(0xFFF39C12), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        // Row 3: Share code + actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Share code pill
            Box(
                modifier = Modifier
                    .background(Color(0xFF0D1117), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF2A2D3E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(card.shareCode,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextSecondary, letterSpacing = 2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Replay", NeonCyan, onReplay)
                ActionButton("Share", Color(0xFF2ECC71), onShare)
                ActionButton("Copy", TextSecondary, onCopy)
            }
        }
    }
}

// ── My Session Card ───────────────────────────────────────────────────────────

@Composable
private fun MySessionCard(
    session: LearningSession,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onReplay: () -> Unit
) {
    val diffColor = when {
        session.accuracyPct >= 80 -> Color(0xFF2ECC71)
        session.accuracyPct >= 50 -> Color(0xFFE67E22)
        else                      -> Color(0xFFE74C3C)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF080B10))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.topic.ifBlank { "Mixed Topics" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary, fontWeight = FontWeight.SemiBold
                    ))
                Spacer(Modifier.height(2.dp))
                Text(
                    "${session.tier.lowercase().replaceFirstChar { it.uppercase() }}  ·  ${session.language.uppercase()}${if (session.voiceMode) "  ·  Voice" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
            Text(
                formatRelativeTime(session.createdAt),
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell("Accuracy", "${session.accuracyPct}%", diffColor, Modifier.weight(1f))
            StatDivider()
            StatCell("Score", "${session.correctAnswers}/${session.totalQuestions}", TextPrimary, Modifier.weight(1f))
            StatDivider()
            StatCell("XP", "+${session.xpEarned}", Color(0xFFF39C12), Modifier.weight(1f))
            StatDivider()
            StatCell("Time", formatDuration(session.totalTimeMs), TextSecondary, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF0D1117), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF2A2D3E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(session.shareCode.ifBlank { "—" },
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextSecondary, letterSpacing = 2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Replay", NeonCyan, onReplay)
                ActionButton("Share", Color(0xFF2ECC71), onShare)
                ActionButton("Copy Code", TextSecondary, onCopy)
            }
        }
    }
}

// ── Leaderboard Row ───────────────────────────────────────────────────────────

@Composable
private fun LeaderboardRow(rank: Int, card: StreamCard) {
    val rankColor = when (rank) {
        1    -> Color(0xFFFFD700)
        2    -> Color(0xFFBDC3C7)
        3    -> Color(0xFFCD7F32)
        else -> TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF080B10))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            rank.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = rankColor, fontWeight = FontWeight.Black
            ),
            modifier = Modifier.width(36.dp)
        )

        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(rankColor.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, rankColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                card.userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = rankColor, fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(card.userName,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary, fontWeight = FontWeight.SemiBold
                ))
            Text("${card.tier.lowercase().replaceFirstChar { it.uppercase() }}  ·  ${card.topic}",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${card.accuracyPct}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = when {
                        card.accuracyPct >= 80 -> Color(0xFF2ECC71)
                        card.accuracyPct >= 50 -> Color(0xFFE67E22)
                        else                   -> Color(0xFFE74C3C)
                    },
                    fontWeight = FontWeight.Bold
                ))
            Text("+${card.xpEarned} XP",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFF39C12)))
        }
    }
}

// ── Reusable sub-components ───────────────────────────────────────────────────

@Composable
private fun StatCell(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = color, fontWeight = FontWeight.Bold
            ))
        Spacer(Modifier.height(2.dp))
        Text(label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted, letterSpacing = 0.5.sp
            ))
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color(0xFF1A1D24))
    )
}

@Composable
private fun ActionButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label,
            style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

@Composable
private fun EmptyFeed(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No activity",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextSecondary, fontWeight = FontWeight.SemiBold
                ))
            Spacer(Modifier.height(8.dp))
            Text(message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted, textAlign = TextAlign.Center
                ))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatRelativeTime(isoDate: String): String {
    return try {
        val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoDate) ?: return ""
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < 60_000L          -> "just now"
            diff < 3_600_000L       -> "${diff / 60_000}m ago"
            diff < 86_400_000L      -> "${diff / 3_600_000}h ago"
            diff < 604_800_000L     -> "${diff / 86_400_000}d ago"
            else                    -> SimpleDateFormat("MMM d", Locale.US).format(date)
        }
    } catch (e: Exception) { "" }
}

private fun formatDuration(ms: Long): String {
    val s = ms / 1000
    return if (s < 60) "${s}s" else "${s / 60}m ${s % 60}s"
}

private fun copyCode(context: Context, code: String) {
    if (code.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Share Code", code))
}

private fun shareSession(context: Context, code: String, userName: String) {
    if (code.isBlank()) return
    val text = "$userName completed a session on Sarvya Quest.\nReplay it with code: $code\nhttps://sarvyaquest.app/replay/$code"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share session"))
}
