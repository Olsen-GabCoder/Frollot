package com.frollot.mobile.ui.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Bouton Principal
 *
 * Bouton principal conforme à la charte graphique :
 * - Élévation : 0dp (pas d'ombre)
 * - Rayon : 12dp
 * - Hauteur : 48dp (standard), 56dp (grand)
 * - Couleurs : MaterialTheme.colorScheme.primary
 *
 * @param text Texte du bouton
 * @param onClick Callback appelé au clic
 * @param modifier Modifier pour personnaliser
 * @param enabled État activé/désactivé (défaut: true)
 * @param size Taille du bouton : Standard (48dp) ou Large (56dp)
 * @param icon Icône optionnelle à afficher avant le texte
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,  // ❌ RETIRÉ @Composable ici
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Standard,
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        icon?.let { // ✅ Utilisation de let au lieu de invoke direct
            it()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Taille du bouton
 */
enum class ButtonSize(val height: androidx.compose.ui.unit.Dp) {
    Standard(48.dp),
    Large(56.dp)
}