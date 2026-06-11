package com.frollot.mobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.frollot.mobile.model.ReactionType
import com.frollot.mobile.localization.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Menu de réactions flottant (style Instagram/Facebook).
 * 
 * Conforme à la charte graphique :
 * - Rayon : 24dp
 * - Élévation : 8dp
 * - Bordure fine : 0.5dp
 * - Animations fluides
 * - Positionnement optimisé pour éviter la troncature
 * - Toutes les réactions visibles sans débris
 * - Tooltip avec nom de la réaction au survol
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionsMenu(
    onReactionSelected: (ReactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation d'apparition avec effet de rebond
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "reactions_menu_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(150),
        label = "reactions_menu_alpha"
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // État pour l'animation de chaque réaction au survol
    val hoveredReaction = remember { mutableStateOf<ReactionType?>(null) }
    
    Surface(
        modifier = modifier
            .zIndex(2000f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(24.dp), // Conforme à la charte (24dp pour dialogues)
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp, // Conforme à la charte (8dp pour dialogues)
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ) // Bordure fine conforme à la charte
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp), // Padding augmenté pour un meilleur espacement
            horizontalArrangement = Arrangement.spacedBy(0.dp), // Pas d'espacement entre les réactions (géré par le Box)
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toutes les réactions disponibles avec animations
            ReactionType.entries.forEachIndexed { index, reactionType ->
                val isHovered = hoveredReaction.value == reactionType
                val reactionScale by animateFloatAsState(
                    targetValue = if (isHovered) 1.4f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "reaction_scale_${reactionType.name}"
                )
                
                // Animation d'apparition séquentielle pour chaque réaction
                val delay = index * 30L
                var showReaction by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(delay)
                    showReaction = true
                }
                
                val reactionAlpha by animateFloatAsState(
                    targetValue = if (showReaction) 1f else 0f,
                    animationSpec = tween(200),
                    label = "reaction_alpha_${reactionType.name}"
                )
                
                // Tooltip avec le nom de la réaction
                val tooltipState = rememberTooltipState()
                var showTooltip by remember { mutableStateOf(false) }
                
                // Gérer l'affichage du tooltip avec LaunchedEffect
                LaunchedEffect(showTooltip) {
                    if (showTooltip) {
                        tooltipState.show()
                        // Fermer le tooltip après 2 secondes
                        kotlinx.coroutines.delay(2000)
                        tooltipState.dismiss()
                        showTooltip = false
                        hoveredReaction.value = null
                    }
                }
                
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(
                                text = reactionType.getLocalizedDisplayName(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    state = tooltipState,
                    modifier = Modifier
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp) // Taille réduite pour des emojis plus petits
                            .pointerInput(reactionType) {
                                // Détecter le touch long pour afficher le tooltip
                                detectTapGestures(
                                    onLongPress = {
                                        // Au touch long, déclencher l'affichage du tooltip
                                        hoveredReaction.value = reactionType
                                        showTooltip = true
                                    },
                                    onTap = {
                                        // Au clic, sélectionner la réaction
                                        onReactionSelected(reactionType)
                                    }
                                )
                            }
                            .clickable {
                                onReactionSelected(reactionType)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reactionType.getEmoji(),
                            fontSize = 30.sp, // Taille réduite pour des emojis plus petits
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = reactionScale
                                    scaleY = reactionScale
                                    this.alpha = reactionAlpha
                                }
                        )
                    }
                }
            }
        }
    }
}

