package com.frollot.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design System Frollot v2 — Editorial premium chaleureux
 *
 * Palette: Prune #6B4E78 + Rose poudre #A4677F + Champagne #A98750
 * Fonts: Cormorant Garamond (display) + Manrope (UI)
 * Framework: Material 3, mobile-first
 */

// ========================================
// COULEURS NOMMEES — Reference directe
// ========================================

// Primary — Prune raffinee
val FrollotPrimary = Color(0xFF6B4E78)
val FrollotOnPrimary = Color(0xFFFFFFFF)
val FrollotPrimaryContainer = Color(0xFFEEE2F1)
val FrollotOnPrimaryContainer = Color(0xFF281733)

// Secondary — Rose poudre
val FrollotSecondary = Color(0xFFA4677F)
val FrollotOnSecondary = Color(0xFFFFFFFF)
val FrollotSecondaryContainer = Color(0xFFF7E2EA)
val FrollotOnSecondaryContainer = Color(0xFF38202A)

// Tertiary — Champagne / or doux
val FrollotTertiary = Color(0xFFA98750)
val FrollotOnTertiary = Color(0xFFFFFFFF)
val FrollotTertiaryContainer = Color(0xFFF4E9D4)
val FrollotOnTertiaryContainer = Color(0xFF362814)

// Surfaces — blancs tiedis (sous-ton prune)
val FrollotSurface = Color(0xFFFFFFFF)
val FrollotOnSurface = Color(0xFF221A26)
val FrollotSurfaceVariant = Color(0xFFECE3EA)
val FrollotOnSurfaceVariant = Color(0xFF6C5F6E)

val FrollotSurfaceContainerHighest = Color(0xFFEAE1E8)
val FrollotSurfaceContainerHigh = Color(0xFFF0E9EE)
val FrollotSurfaceContainer = Color(0xFFF6F0F4)
val FrollotSurfaceContainerLow = Color(0xFFFBF6F9)

// States
val FrollotError = Color(0xFFB3261E)
val FrollotOnError = Color(0xFFFFFFFF)
val FrollotErrorContainer = Color(0xFFF9DEDC)
val FrollotOnErrorContainer = Color(0xFF410E0B)

val FrollotSuccess = Color(0xFF4C7A57)
val FrollotOnSuccess = Color(0xFFFFFFFF)
val FrollotSuccessContainer = Color(0xFFD8EDDD)
val FrollotOnSuccessContainer = Color(0xFF14301B)

val FrollotWarning = Color(0xFF946A1A)
val FrollotOnWarning = Color(0xFFFFFFFF)
val FrollotWarningContainer = Color(0xFFF6E8CA)
val FrollotOnWarningContainer = Color(0xFF2F2106)

val FrollotInfo = Color(0xFF3C6A8A)
val FrollotOnInfo = Color(0xFFFFFFFF)
val FrollotInfoContainer = Color(0xFFD8E8F2)
val FrollotOnInfoContainer = Color(0xFF0E2533)

// Outline & borders
val FrollotOutline = Color(0xFF897C8A)
val FrollotOutlineVariant = Color(0xFFDACFD9)
val FrollotScrim = Color(0xFF000000)

// Background
val FrollotBackground = Color(0xFFFBF7F9)
val FrollotInverseSurface = Color(0xFF372E3A)

// Accents beaute (compatibilite avec code existant)
val FrollotGold = FrollotTertiary
val FrollotRose = FrollotSecondary
val FrollotEmerald = FrollotSuccess

// ========================================
// COLOR SCHEME FROLLOT
// ========================================

val FrollotLightColorScheme = lightColorScheme(
    primary = FrollotPrimary,
    onPrimary = FrollotOnPrimary,
    primaryContainer = FrollotPrimaryContainer,
    onPrimaryContainer = FrollotOnPrimaryContainer,
    secondary = FrollotSecondary,
    onSecondary = FrollotOnSecondary,
    secondaryContainer = FrollotSecondaryContainer,
    onSecondaryContainer = FrollotOnSecondaryContainer,
    tertiary = FrollotTertiary,
    onTertiary = FrollotOnTertiary,
    tertiaryContainer = FrollotTertiaryContainer,
    onTertiaryContainer = FrollotOnTertiaryContainer,
    error = FrollotError,
    onError = FrollotOnError,
    errorContainer = FrollotErrorContainer,
    onErrorContainer = FrollotOnErrorContainer,
    surface = FrollotSurface,
    onSurface = FrollotOnSurface,
    surfaceVariant = FrollotSurfaceVariant,
    onSurfaceVariant = FrollotOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = FrollotSurfaceContainerLow,
    surfaceContainer = FrollotSurfaceContainer,
    surfaceContainerHigh = FrollotSurfaceContainerHigh,
    surfaceContainerHighest = FrollotSurfaceContainerHighest,
    background = FrollotBackground,
    onBackground = FrollotOnSurface,
    outline = FrollotOutline,
    outlineVariant = FrollotOutlineVariant,
    scrim = FrollotScrim,
    inverseSurface = FrollotInverseSurface,
    inverseOnSurface = Color(0xFFF6EEF3),
    inversePrimary = Color(0xFFD9BBE2)
)

// ========================================
// TYPOGRAPHIE — Material 3 type scale
// Display & Headline = Cormorant Garamond (serif, caractere)
// Title / Body / Label = Manrope (sans, lisibilite UI)
//
// Note: Les fontFamily Cormorant Garamond et Manrope seront
// chargees via Google Fonts (Android) ou @font-face (Web).
// En attendant, on utilise les poids/tailles corrects avec
// les polices systeme par defaut. La migration vers les
// polices custom se fera sans changer la structure.
// ========================================

val FrollotTypography = Typography(
    // Display — Grands titres editoriaux (Cormorant Garamond)
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 60.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 50.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    // Headlines — Titres d'ecran (Cormorant Garamond)
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    // Titles — Titres de sections (Manrope)
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    ),

    // Body — Contenu principal (Manrope)
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.3.sp
    ),

    // Labels — Boutons, tags, navigation (Manrope)
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
)

// ========================================
// FORMES — Material 3 shape scale
// ========================================

val FrollotShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // radius-xs: badges
    small = RoundedCornerShape(8.dp),        // radius-sm: inputs, petites cartes
    medium = RoundedCornerShape(12.dp),      // radius-md: cartes standard
    large = RoundedCornerShape(16.dp),       // radius-lg: grandes cartes, categories
    extraLarge = RoundedCornerShape(28.dp)   // radius-xl: bandeaux, FAB
)

// ========================================
// ELEVATION — M3 levels (ombres chaudes)
// ========================================

object FrollotElevation {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 3.dp
    val Level3 = 6.dp
    val Level4 = 8.dp
    val Level5 = 12.dp
}

// ========================================
// SPACING — base 4dp grid
// ========================================

object FrollotSpacing {
    val xs = 4.dp    // sp-1
    val sm = 8.dp    // sp-2
    val md = 12.dp   // sp-3
    val base = 16.dp // sp-4
    val lg = 20.dp   // sp-5
    val xl = 24.dp   // sp-6
    val xxl = 32.dp  // sp-8
    val xxxl = 40.dp // sp-10
}

// ========================================
// THEME COMPOSABLE
// ========================================

@Composable
fun FrollotTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FrollotTypography,
        shapes = FrollotShapes,
        content = content
    )
}
