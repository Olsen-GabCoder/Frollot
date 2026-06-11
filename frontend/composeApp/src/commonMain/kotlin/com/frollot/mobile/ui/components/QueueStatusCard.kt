package com.frollot.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.QueueEntryResponse
import com.frollot.mobile.localization.*
import kotlinx.datetime.Clock
import kotlin.math.max
import com.frollot.mobile.time.currentTimeMillis

@Composable
fun QueueStatusCard(
    entry: QueueEntryResponse,
    totalClients: Int,
    onLeaveQueue: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    lastUpdateTimestamp: Long? = null,
    currentTimeMillis: Long = currentTimeMillis(),
    hasNetworkError: Boolean = false
) {
    val progress = if (totalClients <= 1) {
        1f
    } else {
        val ratio = 1f - (entry.position - 1).toFloat() / max(totalClients - 1, 1)
        ratio.coerceIn(0f, 1f)
    }

    // Calculer l'âge des données (en minutes)
    val dataAgeMinutes = if (lastUpdateTimestamp != null) {
        ((currentTimeMillis - lastUpdateTimestamp) / 60_000).toInt()
    } else {
        null
    }

    // Déterminer si les données sont obsolètes (> 2 minutes sans mise à jour)
    val isDataStale = dataAgeMinutes != null && dataAgeMinutes > 2

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDataStale || hasNetworkError) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🆕 Badge d'alerte réseau (si erreur ou données obsolètes)
            AnimatedVisibility(
                visible = hasNetworkError || isDataStale,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasNetworkError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                    border = BorderStroke(
                        0.5.dp,
                        if (hasNetworkError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasNetworkError) Icons.Default.CloudOff else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasNetworkError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasNetworkError) {
                                    stringResource(Strings.Components.QueueStatusCard.ConnectionLost)
                                } else {
                                    stringResource(Strings.Components.QueueStatusCard.DataStale)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (hasNetworkError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                            dataAgeMinutes?.let {
                                Text(
                                    text = stringResource(Strings.Components.QueueStatusCard.LastUpdate).replace("{minutes}", it.toString()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(Strings.Components.QueueStatusCard.YourProgress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Strings.Components.QueueStatusCard.CurrentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatOrdinal(entry.position),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(Strings.Components.QueueStatusCard.EstimatedTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "~${entry.estimatedWaitMinutes} min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 🆕 Badge de statut dynamique
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            hasNetworkError -> stringResource(Strings.Components.QueueStatusCard.StatusOffline)
                            isDataStale -> stringResource(Strings.Components.QueueStatusCard.StatusPending)
                            else -> stringResource(Strings.Components.QueueStatusCard.StatusAutoRefresh)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            hasNetworkError -> MaterialTheme.colorScheme.error
                            isDataStale -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Medium
                    )

                    // Timestamp si disponible
                    if (!hasNetworkError && dataAgeMinutes != null && dataAgeMinutes <= 2) {
                        Text(
                            text = if (dataAgeMinutes == 0) stringResource(Strings.Components.QueueStatusCard.JustNow) else stringResource(Strings.Components.QueueStatusCard.MinutesAgo).replace("{minutes}", dataAgeMinutes.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Button(
                onClick = onLeaveQueue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Strings.Components.QueueStatusCard.LeaveQueue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (hasNetworkError) {
                    stringResource(Strings.Components.QueueStatusCard.Reconnecting)
                } else {
                    stringResource(Strings.Components.QueueStatusCard.KeepAppOpen)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatOrdinal(position: Int): String {
    return when (position) {
        1 -> "1er"
        2 -> "2e"
        3 -> "3e"
        else -> "${position}e"
    }
}
