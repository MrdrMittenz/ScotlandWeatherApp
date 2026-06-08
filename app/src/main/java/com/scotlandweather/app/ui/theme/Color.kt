package com.scotlandweather.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark theme
val DarkBackground = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkSurfaceVariant = Color(0xFF21262D)
val DarkOnBackground = Color(0xFFC9D1D9)
val DarkOnSurface = Color(0xFFC9D1D9)

// Light theme
val LightBackground = Color(0xFFF6F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEBEDF0)
val LightOnBackground = Color(0xFF1F2328)
val LightOnSurface = Color(0xFF1F2328)

// Accent colors
val OceanBlue = Color(0xFF58A6FF)
val OceanTeal = Color(0xFF56D4C8)
val SunsetOrange = Color(0xFFF0883E)
val FishingGreen = Color(0xFF3FB950)
val WarningRed = Color(0xFFDA3633)
val GoldYellow = Color(0xFFD29922)

// Temperature gradient
val TempCold = Color(0xFF79C0FF)
val TempCool = Color(0xFF58A6FF)
val TempMild = Color(0xFF3FB950)
val TempWarm = Color(0xFFD29922)
val TempHot = Color(0xFFF0883E)
val TempExtreme = Color(0xFFDA3633)

// Solunar
val MajorPeriod = Color(0xFF56D4C8)
val MinorPeriod = Color(0xFF58A6FF)
val ExcellentRating = Color(0xFF3FB950)
val GoodRating = Color(0xFF56D4C8)
val AverageRating = Color(0xFFD29922)
val PoorRating = Color(0xFFDA3633)

// Glassmorphism - more transparent for visible glass effect
private val GlassWhite = Color(0x99FFFFFF)
private val GlassWhiteDim = Color(0x66FFFFFF)
private val GlassBorderWhite = Color(0x55FFFFFF)
private val GlassBlack = Color(0x99000000)
private val GlassBlackDim = Color(0x66000000)
private val GlassBorderBlack = Color(0x44FFFFFF)

fun glassBackground(isDark: Boolean): Color = if (isDark) GlassBlack else GlassWhite
fun glassBackgroundDim(isDark: Boolean): Color = if (isDark) GlassBlackDim else GlassWhiteDim
fun glassBorder(isDark: Boolean): Color = if (isDark) GlassBorderBlack else GlassBorderWhite

// App bar gradient - rich ocean palette
val AppBarGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0D4F6E), Color(0xFF0F6B5E))
)
val AppBarGradientLight = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1976D2), Color(0xFF26A69A))
)

// Glow effect
fun glowColor(baseColor: Color): Color = baseColor.copy(alpha = 0.3f)
