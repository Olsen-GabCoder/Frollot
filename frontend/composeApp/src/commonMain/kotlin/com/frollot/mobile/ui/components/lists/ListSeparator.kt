package com.frollot.mobile.ui.components.lists

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Séparateur de Liste Standardisé
 *
 * Séparateur standardisé conforme à la charte graphique :
 * - Padding horizontal : 16dp (grille 8dp)
 * - Épaisseur : 0.5dp
 * - Couleur : outlineVariant avec alpha 0.3
 */
@Composable
fun ListSeparator(
    modifier: Modifier = Modifier,
    paddingHorizontal: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))
        Divider(
            modifier = Modifier.padding(paddingHorizontal),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Séparateur compact sans espacement vertical.
 */
@Composable
fun ListSeparatorCompact(
    modifier: Modifier = Modifier,
    paddingHorizontal: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    Divider(
        modifier = modifier.padding(paddingHorizontal),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

