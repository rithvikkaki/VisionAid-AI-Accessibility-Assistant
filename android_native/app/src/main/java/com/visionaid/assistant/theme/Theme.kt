package com.visionaid.assistant.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryYellow,
    secondary = White90,
    tertiary = White40,
    background = BgCharcoal,
    surface = SurfaceDark,
    onPrimary = BgCharcoal,
    onSecondary = BgCharcoal,
    onTertiary = White90,
    onBackground = White90,
    onSurface = White90
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    secondary = BgCharcoal,
    tertiary = DarkTextMuted,
    background = LightBgMain,
    surface = LightSurface,
    onPrimary = BgCharcoal,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BgCharcoal,
    onSurface = BgCharcoal
)

@Composable
fun VisionAidTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
