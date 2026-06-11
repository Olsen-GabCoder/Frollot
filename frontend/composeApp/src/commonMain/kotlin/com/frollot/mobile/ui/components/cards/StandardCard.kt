package com.frollot.mobile.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Carte Standard
 * 
 * Carte de base conforme à la charte graphique :
 * - Élévation : 1dp (default), 2dp (pressed)
 * - Bordure : 0.5dp, outlineVariant avec alpha 0.3
 * - Rayon : 16dp
 * - Padding : 16dp (interne)
 * 
 * @param modifier Modifier pour personnaliser
 * @param onClick Callback optionnel pour rendre la carte cliquable
 * @param content Contenu de la carte (avec padding interne de 16dp appliqué automatiquement)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Variante de StandardCard sans padding automatique
 * Utilisée quand on veut gérer manuellement le padding interne.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardCardNoPadding(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            onClick = onClick
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            content()
        }
    }
}

