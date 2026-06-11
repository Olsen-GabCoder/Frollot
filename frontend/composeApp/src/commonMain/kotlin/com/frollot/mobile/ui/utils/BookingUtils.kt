package com.frollot.mobile.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.BookingStatus
import com.frollot.mobile.localization.formatLocalizedDateTime
import com.frollot.mobile.localization.*

/**
 * Formate une date ISO-8601 en format localisé.
 * 
 * Phase 4 - Fonctionnalité Langue : Utilise le formatage localisé.
 * 
 * @param isoDateTime Date-heure au format ISO-8601
 * @return Date-heure formatée selon la langue courante
 */
@Composable
fun formatBookingDateTime(isoDateTime: String): String {
    return formatLocalizedDateTime(isoDateTime)
}

/**
 * Badge de statut pour les réservations.
 */
@Composable
fun StatusBadge(status: BookingStatus) {
    val (containerColor, contentColor, emoji) = when (status) {
        BookingStatus.CONFIRMED -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.primary,
            "✅"
        )
        BookingStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "⏳"
        )
        BookingStatus.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "⚡"
        )
        BookingStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "✅"
        )
        BookingStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "❌"
        )
        BookingStatus.NO_SHOW -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            "👻"
        )
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.getLocalizedDisplayName(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

