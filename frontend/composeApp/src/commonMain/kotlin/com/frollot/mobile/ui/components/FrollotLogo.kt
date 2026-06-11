package com.frollot.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Couleur officielle du logo Frollot — Design System v2
 * Prune raffinee (#6B4E78) remplace l'ancien violet (#6B46C1)
 */
val FrollotPurple = Color(0xFF6B4E78)
val FrollotPurpleLight = Color(0xFFD9BBE2)
val FrollotPurpleDark = Color(0xFF281733)

/**
 * Logo Frollot stylisé - Simple F majuscule épais.
 * Conforme a la charte graphique Frollot v2 (#6B4E78).
 *
 * @param size Taille du logo
 * @param animated Active les animations de pulsation
 * @param showGlow Affiche un effet de lueur
 * @param variant Style du logo (Outline, Filled, Gradient)
 * @param useOfficialColor Utilise la couleur officielle violet Frollot
 */
@Composable
fun FrollotLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true,
    showGlow: Boolean = true,
    variant: LogoVariant = LogoVariant.Outline,
    useOfficialColor: Boolean = true
) {
    val primaryColor = if (useOfficialColor) FrollotPurple else MaterialTheme.colorScheme.primary
    val secondaryColor = if (useOfficialColor) FrollotPurpleLight else MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animated) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (animated) 0.7f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(size * 1.4f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = glowAlpha
                    }
                    // .blur(35.dp) // API expérimentale non disponible
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.6f),
                                secondaryColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        val textColor = when (variant) {
            LogoVariant.Gradient -> Brush.linearGradient(
                colors = listOf(primaryColor, secondaryColor)
            )
            else -> SolidColor(primaryColor)
        }

        Text(
            text = "F",
            fontSize = (size.value * 0.75f).sp,
            fontWeight = FontWeight.Black,
            color = primaryColor,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
    }
}

/**
 * Logo Frollot dans un cercle - Version élégante pour les headers.
 * Parfait pour les écrans de connexion/inscription.
 * ANIMATION ULTRA VISIBLE ET PUISSANTE.
 */
@Composable
fun FrollotLogoCircle(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.15f),
    showShadow: Boolean = true,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circle_animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (animated) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle_scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "circle_rotation"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (animated) 1f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (animated) 0.9f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 2f)
                .graphicsLayer {
                    scaleX = scale * 1.2f
                    scaleY = scale * 1.2f
                    alpha = glowPulse * 0.8f
                    rotationZ = rotation * 0.5f
                }
                // .blur(50.dp) // API expérimentale non disponible
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FrollotPurple.copy(alpha = 0.9f),
                            FrollotPurpleLight.copy(alpha = 0.6f),
                            FrollotPurpleDark.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(size * 1.6f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = glowPulse * 0.5f
                    rotationZ = -rotation * 0.3f
                }
                // .blur(35.dp) // API expérimentale non disponible
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FrollotPurpleLight.copy(alpha = 0.7f),
                            FrollotPurple.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale * 0.9f
                    scaleY = scale * 0.9f
                    rotationZ = rotation
                    alpha = shadowAlpha
                }
                .then(
                    if (showShadow) {
                        Modifier.shadow(
                            elevation = 40.dp,
                            shape = CircleShape,
                            spotColor = FrollotPurple.copy(alpha = shadowAlpha),
                            ambientColor = FrollotPurpleLight.copy(alpha = shadowAlpha * 0.6f)
                        )
                    } else Modifier
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.35f),
                            backgroundColor.copy(alpha = 0.18f),
                            backgroundColor.copy(alpha = 0.08f)
                        ),
                        center = Offset(0.4f, 0.4f)
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(size * 0.88f)
                .graphicsLayer {
                    rotationZ = -rotation * 0.7f
                    scaleX = 1f + (scale - 1f) * 0.3f
                    scaleY = 1f + (scale - 1f) * 0.3f
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.25f),
                            backgroundColor.copy(alpha = 0.12f)
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    shape = CircleShape
                )
        )

        FrollotLogo(
            size = size * 0.5f,
            variant = LogoVariant.Filled,
            animated = false,
            showGlow = false,
            useOfficialColor = true
        )
    }
}

/**
 * Logo Frollot compact - Version simplifiée avec juste le "F".
 * Idéal pour les espaces restreints comme les barres de navigation.
 */
@Composable
fun FrollotLogoCompact(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = FrollotPurple,
    letterColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(size * 0.25f),
                spotColor = backgroundColor.copy(alpha = 0.5f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        FrollotPurpleDark
                    )
                ),
                shape = RoundedCornerShape(size * 0.25f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "F",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = letterColor,
            fontSize = (size.value * 0.5f).sp
        )
    }
}

/**
 * Logo Frollot avec texte - Version complète avec le nom "Frollot".
 */
@Composable
fun FrollotLogoWithText(
    modifier: Modifier = Modifier,
    logoSize: Dp = 80.dp,
    showTagline: Boolean = false,
    tagline: String = "Beauty at your fingertips",
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FrollotLogo(
            size = logoSize,
            variant = LogoVariant.Gradient,
            animated = true,
            showGlow = true
        )

        Text(
            text = "Frollot",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = textColor,
            letterSpacing = (-1).sp
        )

        if (showTagline) {
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Variantes du logo Frollot.
 */
enum class LogoVariant {
    Outline,
    Filled,
    Gradient
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
