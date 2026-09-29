package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpotificColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueSubtle,
    onPrimaryContainer = ElectricBlueLight,
    secondary = ElectricBlueLight,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextGray,
    outline = SurfaceCardBorder,
    outlineVariant = Color(0x33FFFFFF)
)

@Composable
fun SpotificTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpotificColorScheme,
        typography = Typography,
        content = content
    )
}
