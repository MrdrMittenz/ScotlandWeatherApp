package com.scotlandweather.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = OceanBlue.copy(alpha = 0.15f),
    onPrimaryContainer = OceanBlue,
    secondary = OceanTeal,
    onSecondary = Color.Black,
    tertiary = SunsetOrange,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnBackground,
    outline = DarkSurfaceVariant,
    error = WarningRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = OceanBlue.copy(alpha = 0.12f),
    onPrimaryContainer = OceanBlue,
    secondary = OceanTeal,
    onSecondary = Color.Black,
    tertiary = SunsetOrange,
    onTertiary = Color.Black,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnBackground,
    outline = LightSurfaceVariant,
    error = WarningRed,
    onError = Color.White
)

@Composable
fun ScotlandWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
