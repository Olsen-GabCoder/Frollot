package com.frollot.mobile.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Bouton Secondaire (Outlined)
 * 
 * Bouton secondaire avec bordure, conforme à la charte graphique :
 * - Élévation : 0dp (pas d'ombre)
 * - Rayon : 12dp
 * - Hauteur : 48dp (standard), 56dp (grand)
 * - Bordure : 1.5dp avec couleur primaire
 * - Fond : Transparent
 * 
 * @param text Texte du bouton
 * @param onClick Callback appelé au clic
 * @param modifier Modifier pour personnaliser
 * @param enabled État activé/désactivé (défaut: true)
 * @param size Taille du bouton : Standard (48dp) ou Large (56dp)
 * @param icon Icône optionnelle à afficher avant le texte
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Standard,
    icon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        elevation = null
    ) {
        icon?.invoke()
        if (icon != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

