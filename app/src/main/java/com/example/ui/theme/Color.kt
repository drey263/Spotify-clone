package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Spotific Electric Blue Palette
val ElectricBlue = Color(0xFF1E90FF)
val ElectricBlueLight = Color(0xFF4FACFE)
val ElectricBlueGlow = Color(0x661E90FF)
val ElectricBlueSubtle = Color(0x261E90FF)

// Dark Theme Background & Surfaces
val BackgroundDark = Color(0xFF0A0A0F)
val SurfaceDark = Color(0xFF13141F)
val SurfaceElevated = Color(0xFF1B1D2C)
val SurfaceCard = Color(0x14FFFFFF)
val SurfaceCardBorder = Color(0x1AFFFFFF)
val SurfaceGlass = Color(0x1EFFFFFF)

// Text & Icons
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFFA0A0B0)
val TextMuted = Color(0xFF6E6E82)

// States & Highlights
val HeartRed = Color(0xFFFF4D6D)
val ProgressTrack = Color(0x33FFFFFF)
val ProgressThumb = Color(0xFF1E90FF)

// Electric Blue Gradient
val ElectricBlueGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, ElectricBlueLight)
)

val DarkBackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF141624), BackgroundDark)
)
