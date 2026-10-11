package com.visionaid.assistant.theme

import androidx.compose.ui.graphics.Color

// Premium Colors from HTML
val PrimaryYellow = Color(0xFFFFD84D)
val AccentYellow = PrimaryYellow
val AccentBlue = Color(0xFF4D9FFF)
val AccentPink = Color(0xFFFF4D9F)
val DangerRed = Color(0xFFFF4D4D)

// Dark Theme Colors
val BgCharcoal = Color(0xFF0F0F0F)
val Charcoal = BgCharcoal
val BgDark = Color(0xFF141414)
val SurfaceDark = Color(0xFF1A1A1A)
val SurfaceLight = Color(0xFF242424)

// Light Theme Colors (Inferred from HTML .light-mode)
val LightBgMain = Color(0xFFF2F2F7)
val LightBgDark = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)

// Opacity Helpers
val White90 = Color(0xFFFFFFFF).copy(alpha = 0.9f)
val White40 = Color(0xFFFFFFFF).copy(alpha = 0.4f)
val White20 = Color(0xFFFFFFFF).copy(alpha = 0.2f)
val White10 = Color(0xFFFFFFFF).copy(alpha = 0.1f)
val White05 = Color(0xFFFFFFFF).copy(alpha = 0.05f)

val DarkTextMuted = Color(0xFF3C3C43).copy(alpha = 0.6f)
val DarkTextGhost = Color(0xFF3C3C43).copy(alpha = 0.3f)
