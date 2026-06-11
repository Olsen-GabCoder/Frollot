package com.frollot.mobile.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Dialog Standard
 * 
 * Dialog standardisé conforme à la charte graphique :
 * - Rayon : 24dp
 * - Élévation : 8dp
 * - Padding : 24dp
 * 
 * @param title Titre du dialog (optionnel)
 * @param text Texte du dialog (optionnel)
 * @param onDismissRequest Callback appelé pour fermer le dialog
 * @param confirmButton Bouton de confirmation (optionnel)
 * @param dismissButton Bouton d'annulation (optionnel)
 * @param content Contenu personnalisé (optionnel, remplace text si fourni)
 * @param modifier Modifier pour personnaliser
 */
@Composable
fun StandardDialog(
    title: String? = null,
    text: String? = null,
    onDismissRequest: () -> Unit,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val titleComposable: (@Composable () -> Unit)? = if (title != null) {
        {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        null
    }
    
    val textComposable: (@Composable () -> Unit)? = if (text != null && content == null) {
        {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        null
    }
    
    val contentComposable: (@Composable ColumnScope.() -> Unit)? = content

    val textContent: (@Composable () -> Unit)? = if (contentComposable != null) {
        {
            Column(content = contentComposable)
        }
    } else {
        textComposable
    }

    // Afficher le dialogue même s'il n'y a pas de confirmButton
    // (nécessaire pour les dialogues avec seulement dismissButton)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 8.dp,
        title = titleComposable ?: {},
        text = textContent ?: {},
        confirmButton = confirmButton ?: {},
        dismissButton = dismissButton
    )
}

