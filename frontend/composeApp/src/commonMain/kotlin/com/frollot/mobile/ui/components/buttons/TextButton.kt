package com.frollot.mobile.ui.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Bouton Texte
 * 
 * Bouton texte sans bordure ni fond, conforme à la charte graphique :
 * - Élévation : 0dp
 * - Pas de bordure ni fond
 * - Couleur : Primary ou OnSurface selon le contexte
 * 
 * @param text Texte du bouton
 * @param onClick Callback appelé au clic
 * @param modifier Modifier pour personnaliser
 * @param enabled État activé/désactivé (défaut: true)
 * @param colorStyle Style de couleur : Primary ou OnSurface
 * @param icon Icône optionnelle à afficher avant le texte
 */
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorStyle: TextButtonColorStyle = TextButtonColorStyle.Primary,
    icon: (@Composable () -> Unit)? = null
) {
    val contentColor = when (colorStyle) {
        TextButtonColorStyle.Primary -> MaterialTheme.colorScheme.primary
        TextButtonColorStyle.OnSurface -> MaterialTheme.colorScheme.onSurface
    }
    
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
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

/**
 * Style de couleur pour TextButton
 */
enum class TextButtonColorStyle {
    Primary,
    OnSurface
}

