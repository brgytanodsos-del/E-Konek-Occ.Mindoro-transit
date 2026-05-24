package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MindoroDarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A2540),           // Deep Ocean Blue
    secondary = Color(0xFFFF8C00),         // Vibrant Sunset Orange
    tertiary = Color(0xFF00D4FF),          // Electric Cyan (waves)
    background = Color(0xFF05101F),        // Dark Navy Night
    surface = Color(0xFF0A1F38),
    onPrimary = Color.White,
    onSecondary = Color.Black
)

@Composable
fun MindoroTransitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MindoroDarkColorScheme,
        typography = MindoroTypography,
        content = content
    )
}
