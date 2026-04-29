package com.example.trails.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    primaryContainer = Color(0xFF4B3886),
    onPrimaryContainer = Color(0xFFE6E1E5),
    onPrimary = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    background = Color(0xFF1B1B1F),
    surface = Color(0xFF1B1B1F),
    onSecondaryContainer = Color(0xFFD0BCFF),
    secondaryContainer = Color(0xFF4A4458)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = NavySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE3F1),
    onSecondaryContainer = Color(0xFF101C2B),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFF43474E),
    surfaceVariant = Color(0xFFDDE3F1),
    outline = Color(0xFF73777F)
)

@Composable
fun TrailsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
