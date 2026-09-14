package com.sl.videodownloader.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A65),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF2EA),
    onPrimaryContainer = Color(0xFF00201E),
    secondary = Color(0xFF4D635F),
    background = Color(0xFFF7FAF8),
    surface = Color(0xFFF7FAF8),
    surfaceVariant = Color(0xFFDCE5E1),
    onSurface = Color(0xFF161D1C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80DBD3),
    primaryContainer = Color(0xFF00504C),
    secondary = Color(0xFFB1CCC7)
)

@Composable
fun SLVideoDownloaderTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val isDark = when (mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> LocalConfiguration.current.uiMode and 0x30 == 0x20
    }
    MaterialTheme(colorScheme = if (isDark) DarkColors else LightColors, content = content)
}