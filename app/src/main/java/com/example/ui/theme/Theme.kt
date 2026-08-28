package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    onPrimary = PolishOnPrimary,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = PolishSecondary,
    onSecondary = PolishOnSecondary,
    secondaryContainer = PolishSecondaryContainer,
    onSecondaryContainer = PolishOnSecondaryContainer,
    tertiary = PolishTertiary,
    onTertiary = PolishOnTertiary,
    tertiaryContainer = PolishTertiaryContainer,
    onTertiaryContainer = PolishOnTertiaryContainer,
    background = PolishBgLight,
    onBackground = PolishTextPrimaryLight,
    surface = PolishSurfaceLight,
    onSurface = PolishTextPrimaryLight,
    surfaceVariant = PolishSurfaceCardLight,
    onSurfaceVariant = PolishTextSecondaryLight,
    outline = PolishBorderLight,
    outlineVariant = PolishBorderLight,
    error = PolishCoral,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = PolishBgDark,
    onBackground = PolishTextPrimaryDark,
    surface = PolishSurfaceDark,
    onSurface = PolishTextPrimaryDark,
    surfaceVariant = PolishSurfaceCardDark,
    onSurfaceVariant = PolishTextSecondaryDark,
    outline = PolishBorderDark,
    outlineVariant = PolishBorderDark,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

