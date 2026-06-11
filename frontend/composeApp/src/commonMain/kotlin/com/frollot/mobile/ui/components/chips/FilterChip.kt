package com.frollot.mobile.ui.components.chips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Chip de Filtre
 * 
 * Chip de filtre conforme à la charte graphique :
 * - Rayon : 20dp (pill shape)
 * - Hauteur : 32dp
 * - Couleurs : MaterialTheme.colorScheme
 * 
 * @param selected État sélectionné/non sélectionné
 * @param onClick Callback appelé au clic
 * @param label Label du chip
 * @param modifier Modifier pour personnaliser
 * @param enabled État activé/désactivé (défaut: true)
 * @param leadingIcon Icône optionnelle à afficher avant le label
 * @param trailingIcon Icône optionnelle à afficher après le label
 */
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(32.dp),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp), // Pill shape
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderWidth = 1.5.dp,
            borderWidth = 1.dp,
            enabled = true,
            selected = selected
        )
    )
}

