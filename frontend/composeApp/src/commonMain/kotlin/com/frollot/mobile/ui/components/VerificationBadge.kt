package com.frollot.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.VerificationType
import com.frollot.mobile.localization.*

/**
 * Badge de vérification pour afficher le statut de vérification d'un utilisateur ou d'un salon.
 * Phase H.2 - Vérification Salons/Coiffeurs
 * 
 * @param isVerified Indique si l'entité est vérifiée
 * @param verificationType Type de vérification (optionnel)
 * @param modifier Modifier pour personnaliser l'apparence
 */
@Composable
fun VerificationBadge(
    isVerified: Boolean,
    verificationType: VerificationType? = null,
    modifier: Modifier = Modifier
) {
    if (!isVerified) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Verified,
            contentDescription = "Vérifié",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        verificationType?.let { type ->
            Text(
                text = type.getEmoji(),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

/**
 * Badge de vérification avec texte (pour les profils détaillés).
 * Phase H.2 - Vérification Salons/Coiffeurs
 * 
 * @param isVerified Indique si l'entité est vérifiée
 * @param verificationType Type de vérification (optionnel)
 * @param modifier Modifier pour personnaliser l'apparence
 */
@Composable
fun VerificationBadgeWithText(
    isVerified: Boolean,
    verificationType: VerificationType? = null,
    modifier: Modifier = Modifier
) {
    if (!isVerified) return

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Vérifié",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = verificationType?.getLocalizedDisplayName() ?: stringResource(Strings.Common.Verified),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

