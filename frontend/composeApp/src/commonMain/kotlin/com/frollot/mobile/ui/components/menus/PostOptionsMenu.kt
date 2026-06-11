package com.frollot.mobile.ui.components.menus

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.frollot.mobile.ui.utils.AnimationSpecs
import com.frollot.mobile.config.FrollotLogger


/**
 * Menu d'options pour un post, conforme à la charte graphique Frollot.
 * 
 * Améliorations :
 * - Overlay semi-transparent pour fermeture au clic extérieur (via PostOptionsMenuOverlay)
 * - Design plus compact et équilibré
 * - Largeur optimisée (220.dp au lieu de 240.dp)
 * - Espacements réduits pour une meilleure lisibilité
 * - Animations fluides
 * 
 * @param expanded État d'ouverture du menu
 * @param onDismissRequest Callback pour fermer le menu
 * @param modifier Modifier pour positionner le menu (doit inclure offset/align)
 * @param content Contenu du menu (PostOptionItem, PostOptionsDivider)
 */

@Composable
fun PostOptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu positionné en overlay absolu pour ne pas créer de décalage dans le layout
    FrollotLogger.debug("API", "🔍 [PostOptionsMenu] Rendering menu, expanded: $expanded")
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = AnimationSpecs.FadeIn) +
                slideInVertically(
                    initialOffsetY = { -it / 4 },
                    animationSpec = AnimationSpecs.SlideSpring
                ),
        exit = fadeOut(animationSpec = AnimationSpecs.FadeOut) +
               slideOutVertically(
                   targetOffsetY = { -it / 4 },
                   animationSpec = AnimationSpecs.SlideAnimation
               ),
        modifier = modifier
            .wrapContentSize() // Ne prendre que l'espace nécessaire
    ) {
        FrollotLogger.debug("API", "🔍 [PostOptionsMenu] AnimatedVisibility visible=true, rendering Card")
        Card(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight() // S'adapter au contenu
                // zIndex retiré car il ne fonctionne pas entre parents différents
                // Le menu doit être rendu au même niveau que l'overlay pour que le zIndex fonctionne
            ,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp
            ),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            // Utiliser une Column scrollable pour permettre de voir toutes les options
            // Limiter la hauteur maximale pour éviter que le menu dépasse l'écran
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(scrollState)
                    .heightIn(max = 400.dp), // Hauteur maximale pour éviter le débordement
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FrollotLogger.debug("API", "🔍 [PostOptionsMenu] Rendering Column content")
                content()
                FrollotLogger.debug("API", "🔍 [PostOptionsMenu] Finished rendering Column content")
            }
        }
    }
}


/**
 * Item d'option dans le menu, standardisé selon la charte graphique.
 * 
 * @param icon Icône de l'option
 * @param text Texte de l'option
 * @param onClick Action à exécuter au clic
 * @param isDanger Si true, affiche en rouge (pour "Signaler")
 * @param modifier Modifier pour personnaliser
 */
@Composable
fun PostOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDanger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            Color.Transparent,
        animationSpec = AnimationSpecs.SoftTouchInteractionColor
    )
    
    val textColor = if (isDanger)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurface
    
    val iconColor = if (isDanger)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurfaceVariant
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * Séparateur pour le menu d'options, standardisé.
 */
@Composable
fun PostOptionsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

