package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeConfiguration(
    val themePreset: AppThemePreset = AppThemePreset.ECO_GREEN,
    val backgroundPreset: AppBackgroundPreset = AppBackgroundPreset.MISTY_BOTANICAL
)

val LocalAppTheme = compositionLocalOf { AppThemeConfiguration() }

@Composable
fun Project90Theme(
    themePreset: AppThemePreset = DynamicThemeState.activePreset,
    backgroundPreset: AppBackgroundPreset = DynamicThemeState.activeBackground,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep global reactive state synchronized so any Composable reading ForestGreen/LeafGreen/etc. recomposes immediately
    SideEffect {
        DynamicThemeState.activePreset = themePreset
        DynamicThemeState.activeBackground = backgroundPreset
    }

    val dynamicColorScheme = lightColorScheme(
        primary = themePreset.primary,
        onPrimary = Color.White,
        primaryContainer = themePreset.containerLight,
        onPrimaryContainer = themePreset.primaryDark,
        secondary = GoldenStreak,
        onSecondary = Color.White,
        secondaryContainer = GoldenStreakLight,
        onSecondaryContainer = themePreset.primary,
        tertiary = MarineAqua,
        onTertiary = Color.White,
        tertiaryContainer = AquaLight,
        onTertiaryContainer = themePreset.primaryDark,
        background = themePreset.backgroundBase,
        onBackground = TextPrimary,
        surface = CardWhite,
        onSurface = TextPrimary,
        surfaceVariant = themePreset.surfaceTint,
        onSurfaceVariant = TextSecondary,
        outline = themePreset.borderTint,
        error = DangerCrimson,
        onError = Color.White
    )

    val appThemeConfig = AppThemeConfiguration(
        themePreset = themePreset,
        backgroundPreset = backgroundPreset
    )

    CompositionLocalProvider(LocalAppTheme provides appThemeConfig) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
