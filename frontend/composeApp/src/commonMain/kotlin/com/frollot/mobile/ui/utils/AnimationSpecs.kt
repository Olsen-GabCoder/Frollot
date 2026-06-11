package com.frollot.mobile.ui.utils

import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

/**
 * Spécifications d'animation standardisées selon la charte graphique Frollot.
 * 
 * Durées standard :
 * - Micro-interactions : 150ms (hover, press)
 * - Transitions d'état : 300ms (changement de couleur, taille)
 * - Navigation : 300ms (transitions d'écran)
 * - Animations complexes : 500ms (apparitions, disparitions)
 * 
 * Easing standard :
 * - Standard : FastOutSlowInEasing (Material)
 * - Entrées : LinearOutSlowInEasing (décélération)
 * - Sorties : FastOutLinearInEasing (accélération)
 * - Bounce : Spring avec DampingRatioMediumBouncy (pour les likes, réactions)
 */
object AnimationSpecs {
    
    // Durées standardisées
    const val MICRO_INTERACTION_DURATION = 150
    const val STATE_TRANSITION_DURATION = 300
    const val NAVIGATION_DURATION = 300
    const val COMPLEX_ANIMATION_DURATION = 500
    
    // Easing standardisé
    val StandardEasing: Easing = FastOutSlowInEasing
    val EnterEasing: Easing = LinearOutSlowInEasing
    val ExitEasing: Easing = FastOutLinearInEasing
    
    // Spécifications d'animation standardisées
    
    /**
     * Micro-interaction (hover, press) : 150ms
     */
    val MicroInteraction: FiniteAnimationSpec<Float> = tween(
        durationMillis = MICRO_INTERACTION_DURATION,
        easing = StandardEasing
    )
    
    /**
     * Transition d'état (changement de couleur, taille) : 300ms
     */
    val StateTransition: FiniteAnimationSpec<Float> = tween(
        durationMillis = STATE_TRANSITION_DURATION,
        easing = StandardEasing
    )
    
    /**
     * Navigation (transitions d'écran) : 300ms
     */
    val Navigation: FiniteAnimationSpec<Float> = tween(
        durationMillis = NAVIGATION_DURATION,
        easing = StandardEasing
    )
    
    /**
     * Animation complexe (apparitions, disparitions) : 500ms
     */
    val ComplexAnimation: FiniteAnimationSpec<Float> = tween(
        durationMillis = COMPLEX_ANIMATION_DURATION,
        easing = StandardEasing
    )
    
    /**
     * Animation spring pour likes/réactions (Float) : bounce
     */
    val LikeReaction: SpringSpec<Float> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = StiffnessMedium
    )
    
    /**
     * Animation spring pour slide (IntOffset) : bounce
     */
    val SlideSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = StiffnessMedium
    )
    
    /**
     * Animation spring pour interactions tactiles : bounce subtil (StiffnessMedium)
     */
    val TouchInteraction: SpringSpec<Float> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = StiffnessMedium
    )
    
    /**
     * Animation spring pour interactions tactiles douces : bounce très subtil (StiffnessLow)
     */
    val SoftTouchInteraction: SpringSpec<Float> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = StiffnessLow
    )
    
    /**
     * Animation spring pour interactions tactiles douces (Int) : bounce très subtil (StiffnessLow)
     */
    val SoftTouchInteractionInt: SpringSpec<Int> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = StiffnessLow
    )
    
    /**
     * Animation spring pour interactions tactiles douces (Color) : bounce très subtil (StiffnessLow)
     */
    val SoftTouchInteractionColor: SpringSpec<Color> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = StiffnessLow
    )
    
    /**
     * Animation d'entrée (fade in) : 300ms avec LinearOutSlowInEasing (décélération)
     */
    val FadeIn: FiniteAnimationSpec<Float> = tween(
        durationMillis = STATE_TRANSITION_DURATION,
        easing = EnterEasing
    )
    
    /**
     * Animation de sortie (fade out) : 300ms avec FastOutLinearInEasing (accélération)
     */
    val FadeOut: FiniteAnimationSpec<Float> = tween(
        durationMillis = STATE_TRANSITION_DURATION,
        easing = ExitEasing
    )
    
    /**
     * Animation d'apparition de carte : fade in + slide up (300ms)
     */
    val CardAppearance: FiniteAnimationSpec<Float> = tween(
        durationMillis = STATE_TRANSITION_DURATION,
        easing = EnterEasing
    )
    
    /**
     * Animation de slide pour IntOffset : 300ms
     */
    val SlideAnimation: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = STATE_TRANSITION_DURATION,
        easing = EnterEasing
    )
}

