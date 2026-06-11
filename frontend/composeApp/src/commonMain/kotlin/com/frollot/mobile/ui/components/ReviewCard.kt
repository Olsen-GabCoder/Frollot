package com.frollot.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.Review
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Carte d'affichage d'un avis client.
 *
 * Affiche :
 * - Avatar du client (initiale)
 * - Nom du client
 * - Note (étoiles)
 * - Titre et contenu de l'avis
 * - Date de publication
 * - Réponse du salon (si présente)
 */
@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header avec avatar et nom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.clientName.firstOrNull()?.uppercase() ?: "?",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Nom et date
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = review.clientName,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    review.createdAt?.let { dateStr ->
                        Text(
                            text = formatReviewDate(dateStr),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Badge vérifié (si applicable)
                if (review.isVerified) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "✓ Vérifié",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Note (étoiles)
            RatingBar(
                rating = review.rating,
                readOnly = true,
                starSize = 20.dp
            )

            // Titre (si présent)
            review.title?.let { title ->
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Contenu
            review.content?.let { content ->
                if (content.isNotBlank()) {
                    Text(
                        text = content,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = androidx.compose.material3.MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )
                }
            }

            // Réponse du salon (si présente)
            review.responseSalon?.let { response ->
                if (response.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Réponse du salon",
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                            Text(
                                text = response,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formate une date d'avis en format lisible.
 */
private fun formatReviewDate(dateStr: String): String {
    return try {
        // Format ISO simplifié : "2024-01-15T10:30:00" -> "15 janv. 2024"
        val date = dateStr.split("T")[0]
        val parts = date.split("-")
        if (parts.size == 3) {
            val day = parts[2].toInt()
            val month = parts[1].toInt()
            val year = parts[0].toInt()
            
            val monthNames = listOf(
                "janv.", "févr.", "mars", "avr.", "mai", "juin",
                "juil.", "août", "sept.", "oct.", "nov.", "déc."
            )
            
            "$day ${monthNames.getOrNull(month - 1) ?: month} $year"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

