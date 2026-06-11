package com.frollot.mobile.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding

/**
 * Section de statistiques générique pour les profils.
 * 
 * Ce composant unifie l'affichage des statistiques pour tous les types de profils
 * en éliminant la duplication de code.
 * 
 * @param stats Liste des statistiques à afficher
 * @param modifier Modifier pour personnaliser le layout
 */
@Composable
fun ProfileStatsSection(
    stats: List<ProfileStatConfig>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) return
    
    StandardCardNoPadding(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.forEach { stat ->
                ProfileStatItem(
                    value = stat.value,
                    label = stat.label
                )
            }
        }
    }
}

