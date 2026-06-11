package com.frollot.mobile.ui.components.forms

import androidx.compose.foundation.layout.*
import com.frollot.mobile.localization.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Champ de Recherche Standard
 *
 * Champ de recherche standardisé conforme à la charte graphique :
 * - Rayon : 12dp (déjà standardisé dans StandardTextField)
 * - Icône de recherche à gauche
 * - Style adapté pour la recherche
 *
 * @param value Valeur du champ
 * @param onValueChange Callback appelé lors du changement de valeur
 * @param modifier Modifier pour personnaliser
 * @param placeholder Placeholder (défaut: "Rechercher...")
 * @param enabled État activé/désactivé (défaut: true)
 * @param onSearch Callback appelé lors de la recherche (ImeAction.Search)
 * @param leadingIcon Icône de début personnalisée (optionnel, par défaut: Search)
 * @param trailingIcon Icône de fin (optionnel, par ex. pour effacer)
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Rechercher...", // Valeur par défaut, peut être remplacée par l'appelant
    enabled: Boolean = true,
    onSearch: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(Strings.Components.SearchTextField.ContentDescription),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    },
    trailingIcon: (@Composable () -> Unit)? = null
) {
    StandardTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch?.invoke()
            }
        )
    )
}

