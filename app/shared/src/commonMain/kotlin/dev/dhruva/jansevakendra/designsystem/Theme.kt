package dev.dhruva.jansevakendra.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = JskColors.SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = JskColors.SaffronContainer,
    onPrimaryContainer = JskColors.OnSaffronContainer,
    secondary = JskColors.NavyPrimary,
    onSecondary = Color.White,
    secondaryContainer = JskColors.NavyContainer,
    onSecondaryContainer = JskColors.OnNavyContainer,
    tertiary = JskColors.ForestGreen,
    onTertiary = Color.White,
    tertiaryContainer = JskColors.GreenContainer,
    onTertiaryContainer = Color(0xFF002204),
    background = JskColors.BackgroundLight,
    onBackground = JskColors.TextPrimaryLight,
    surface = JskColors.SurfaceLight,
    onSurface = JskColors.TextPrimaryLight,
    surfaceVariant = JskColors.SurfaceCardLight,
    onSurfaceVariant = JskColors.TextSecondaryLight,
    outline = JskColors.SurfaceBorderLight,
    error = JskColors.ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = JskColors.SaffronLight,
    onPrimary = Color(0xFF380D00),
    primaryContainer = JskColors.SaffronDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF86B4E5),
    onSecondary = Color(0xFF003258),
    secondaryContainer = JskColors.NavyPrimary,
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF00390B),
    tertiaryContainer = Color(0xFF1B5E20),
    onTertiaryContainer = Color(0xFFA5D6A7),
    background = JskColors.BackgroundDark,
    onBackground = JskColors.TextPrimaryDark,
    surface = JskColors.SurfaceDark,
    onSurface = JskColors.TextPrimaryDark,
    surfaceVariant = JskColors.SurfaceCardDark,
    onSurfaceVariant = JskColors.TextSecondaryDark,
    outline = JskColors.SurfaceBorderDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun JanSevaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JskTypography,
        content = content
    )
}
