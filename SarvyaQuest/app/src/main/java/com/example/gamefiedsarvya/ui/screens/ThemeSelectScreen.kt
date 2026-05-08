package com.example.gamefiedsarvya.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.gamefiedsarvya.ui.theme.*
import com.example.gamefiedsarvya.ui.components.NeonButton

@Composable
fun ThemeSelectScreen(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("Back", onClick = onBack, color = TextSecondary)
                Text("THEMES",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = NeonCyan, letterSpacing = 3.sp, fontWeight = FontWeight.Black
                    ))
                Spacer(Modifier.width(72.dp))
            }

            Text("Choose your visual style",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            AppTheme.values().forEach { theme ->
                ThemeCard(
                    theme      = theme,
                    isSelected = currentTheme == theme,
                    onSelect   = { onThemeSelected(theme) }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeCard(theme: AppTheme, isSelected: Boolean, onSelect: () -> Unit) {
    val scale by animateFloatAsState(
        if (isSelected) 1.02f else 1f,
        spring(Spring.DampingRatioMediumBouncy), label = "theme_s"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.primary else theme.border,
                shape = RoundedCornerShape(14.dp)
            )
            .background(theme.background, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colour preview swatches
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(theme.primary, theme.secondary, theme.accent).forEach { color ->
                    Box(modifier = Modifier
                        .size(28.dp)
                        .background(color, RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(theme.icon, fontSize = 20.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(theme.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(color = theme.primary))
                }
                Spacer(Modifier.height(4.dp))
                // Mini preview bar
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(theme.primary, theme.secondary, theme.accent)),
                        RoundedCornerShape(2.dp)
                    )
                )
            }

            Spacer(Modifier.width(12.dp))

            if (isSelected) {
                Text("✓", style = MaterialTheme.typography.headlineMedium.copy(color = theme.primary))
            }
        }
    }
}
