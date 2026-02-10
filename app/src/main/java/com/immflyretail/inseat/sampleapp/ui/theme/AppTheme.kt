package com.immflyretail.inseat.sampleapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * App color schemes for Light and Dark themes.
 *
 * Theme colors:
 *  - primary: Main buttons and key icons
 *  - onPrimary: Text/icons displayed on primary color
 *  - surface: TopBar, general surfaces, cards
 *  - onSurface: Main text on surfaces
 *  - onSurfaceVariant: Secondary text on surfaces
 *  - background: App general background
 *  - onBackground: Text on background
 *  - primaryContainer: Modal containers, photo containers
 *  - secondaryContainer: Info cards, highlighted sections
 *  - tertiaryContainer: Add/remove icons or minor elements
 *  - surfaceContainer: Item info in promotions, incomplete steps, product descriptions
 *  - surfaceVariant: Labels and secondary elements
 *  - outlineVariant: Borders for TextField, Stepper, decorative lines
 */

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFDD083A),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF666666),
    background = Color(0xFFF8F8F8),
    onBackground = Color(0xFF333333),
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xB3575555),
    secondaryContainer = Color(0xFFEBEEF6),
    tertiaryContainer = Color(0xFFE2E2E2),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFFF8F8F8),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFDD083A),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF999999),
    background = Color(0xFF111111),
    onBackground = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF222222),
    onPrimaryContainer = Color(0xB3575555),
    secondaryContainer = Color(0xFF252932),
    tertiaryContainer = Color(0xFF333333),
    surfaceContainer = Color(0xFF222222),
    surfaceVariant = Color(0xFF2C2C2C),
    outlineVariant = Color(0xFF49454F)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content
    )
}