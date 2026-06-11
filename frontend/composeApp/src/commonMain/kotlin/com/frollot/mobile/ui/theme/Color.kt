package com.frollot.mobile.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Palette de couleurs Light — Design System Frollot v2
 *
 * Direction : Editorial premium chaleureux
 * - Primary: Prune raffinee (#6B4E78)
 * - Secondary: Rose poudre (#A4677F)
 * - Tertiary: Champagne / or doux (#A98750)
 * - Surfaces: Blancs tiedis (sous-ton prune)
 */
val LightColorScheme = lightColorScheme(
    // Primary — Prune raffinee
    primary = Color(0xFF6B4E78),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEE2F1),
    onPrimaryContainer = Color(0xFF281733),

    // Secondary — Rose poudre
    secondary = Color(0xFFA4677F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7E2EA),
    onSecondaryContainer = Color(0xFF38202A),

    // Tertiary — Champagne / or doux
    tertiary = Color(0xFFA98750),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF4E9D4),
    onTertiaryContainer = Color(0xFF362814),

    // Error
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    // Surface — blancs tiedis (sous-ton prune)
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF221A26),
    surfaceVariant = Color(0xFFECE3EA),
    onSurfaceVariant = Color(0xFF6C5F6E),

    // Surface Containers
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF6F9),
    surfaceContainer = Color(0xFFF6F0F4),
    surfaceContainerHigh = Color(0xFFF0E9EE),
    surfaceContainerHighest = Color(0xFFEAE1E8),

    // Outline
    outline = Color(0xFF897C8A),
    outlineVariant = Color(0xFFDACFD9),

    // Scrim
    scrim = Color(0xFF000000),

    // Background
    background = Color(0xFFFBF7F9),
    onBackground = Color(0xFF221A26),

    // Inverse
    inverseSurface = Color(0xFF372E3A),
    inverseOnSurface = Color(0xFFF6EEF3),
    inversePrimary = Color(0xFFD9BBE2)
)

/**
 * Palette de couleurs Dark — Design System Frollot v2
 */
val DarkColorScheme = darkColorScheme(
    // Primary
    primary = Color(0xFFD9BBE2),
    onPrimary = Color(0xFF3C2A48),
    primaryContainer = Color(0xFF534060),
    onPrimaryContainer = Color(0xFFF1DCF6),

    // Secondary
    secondary = Color(0xFFE8B6C8),
    onSecondary = Color(0xFF502738),
    secondaryContainer = Color(0xFF6B3D4F),
    onSecondaryContainer = Color(0xFFFBDDE8),

    // Tertiary
    tertiary = Color(0xFFE0C28A),
    onTertiary = Color(0xFF3E2E10),
    tertiaryContainer = Color(0xFF574322),
    onTertiaryContainer = Color(0xFFFBE6BF),

    // Error
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    // Surface
    surface = Color(0xFF15111A),
    onSurface = Color(0xFFE9DFE9),
    surfaceVariant = Color(0xFF4A3F4D),
    onSurfaceVariant = Color(0xFFCDBFCC),

    // Surface Containers
    surfaceContainerLowest = Color(0xFF100B14),
    surfaceContainerLow = Color(0xFF1D1722),
    surfaceContainer = Color(0xFF211B26),
    surfaceContainerHigh = Color(0xFF2C2531),
    surfaceContainerHighest = Color(0xFF37303C),

    // Outline
    outline = Color(0xFF978A98),
    outlineVariant = Color(0xFF4A3F4D),

    // Scrim
    scrim = Color(0xFF000000),

    // Background
    background = Color(0xFF15111A),
    onBackground = Color(0xFFE9DFE9),

    // Inverse
    inverseSurface = Color(0xFFE9DFE9),
    inverseOnSurface = Color(0xFF342B38),
    inversePrimary = Color(0xFF6B4E78)
)
