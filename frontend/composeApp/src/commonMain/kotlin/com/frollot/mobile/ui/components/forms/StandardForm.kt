package com.frollot.mobile.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Formulaire Standard
 *
 * Wrapper de formulaire standardisé conforme à la charte graphique :
 * - Espacement vertical : 16dp entre champs (grille 8dp)
 * - Validation visuelle standardisée
 * - Messages d'erreur standardisés
 *
 * @param modifier Modifier pour personnaliser
 * @param content Contenu du formulaire (champs, boutons, etc.)
 */
@Composable
fun StandardForm(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

/**
 * Wrapper de formulaire avec titre et description optionnels.
 *
 * @param modifier Modifier pour personnaliser
 * @param title Titre du formulaire (optionnel)
 * @param description Description du formulaire (optionnel)
 * @param content Contenu du formulaire
 */
@Composable
fun StandardFormWithHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // En-tête du formulaire
        if (title != null || description != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Contenu du formulaire
        StandardForm {
            content()
        }
    }
}

/**
 * Message d'erreur standardisé pour les formulaires.
 *
 * @param message Message d'erreur à afficher
 * @param modifier Modifier pour personnaliser
 */
@Composable
fun FormErrorText(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(top = 4.dp)
    )
}

/**
 * Message d'aide standardisé pour les formulaires.
 *
 * @param message Message d'aide à afficher
 * @param modifier Modifier pour personnaliser
 */
@Composable
fun FormHelperText(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 4.dp)
    )
}

