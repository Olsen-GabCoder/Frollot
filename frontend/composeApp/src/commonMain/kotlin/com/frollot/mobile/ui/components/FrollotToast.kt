package com.frollot.mobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Types de toast disponibles selon la charte graphique Frollot
 */
enum class ToastType {
    Success,
    Error,
    Info
}

/**
 * Composant Toast personnalisé conforme à la charte graphique Frollot.
 *
 * Caractéristiques :
 * - Utilise les couleurs de la charte (tertiaryContainer pour succès, errorContainer pour erreur)
 * - Bordures subtiles (0.5dp) comme défini dans la charte
 * - Coins arrondis à 16dp (standard cartes)
 * - Animation slide-in depuis le haut
 * - Auto-dismiss après 3 secondes
 * - Icônes Material Design cohérentes
 */
@Composable
fun FrollotToast(
    message: String,
    type: ToastType = ToastType.Success,
    onDismiss: () -> Unit = {}
) {
    val colors = when (type) {
        ToastType.Success -> ToastColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer
        )
        ToastType.Error -> ToastColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            iconTint = MaterialTheme.colorScheme.error
        )
        ToastType.Info -> ToastColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconTint = MaterialTheme.colorScheme.primary
        )
    }

    val icon = when (type) {
        ToastType.Success -> Icons.Default.CheckCircle
        ToastType.Error -> Icons.Default.Error
        ToastType.Info -> Icons.Default.CheckCircle
    }

    // Animation d'apparition/disparition
    var visible by remember { mutableStateOf(true) }

    // Auto-dismiss après 3 secondes
    LaunchedEffect(Unit) {
        delay(3000)
        visible = false
        delay(300) // Attendre la fin de l'animation
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp)), // 16dp comme défini dans la charte
            color = colors.containerColor,
            shadowElevation = 1.dp, // Élévation minimale comme défini
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp, // Bordure fine comme défini dans la charte
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Padding interne de 16dp comme défini
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.iconTint,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = message,
                    color = colors.contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Couleurs pour le toast selon le type
 */
private data class ToastColors(
    val containerColor: Color,
    val contentColor: Color,
    val iconTint: Color
)
