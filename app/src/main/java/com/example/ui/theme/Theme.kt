package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RoseGold,
    onPrimary = Color.Black,
    primaryContainer = LightVelvet,
    onPrimaryContainer = NeutralCream,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = RoseGoldLight,
    onTertiary = Color.Black,
    background = MatteCharcoal,
    onBackground = NeutralCream,
    surface = DarkVelvet,
    onSurface = NeutralCream,
    surfaceVariant = LightVelvet,
    onSurfaceVariant = NeutralMuted,
    outline = GoldAccent
)

private val LightColorScheme = lightColorScheme(
    primary = GoldAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF9F6F0),
    onPrimaryContainer = Color.Black,
    secondary = RoseGold,
    onSecondary = Color.White,
    tertiary = Color(0xFFBF9B30),
    background = Color(0xFFFDFBF7),
    onBackground = Color(0xFF1C1A17),
    surface = Color.White,
    onSurface = Color(0xFF1C1A17),
    surfaceVariant = Color(0xFFF3EDE2),
    onSurfaceVariant = Color(0xFF534F46),
    outline = GoldAccent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
