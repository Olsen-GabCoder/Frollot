package com.frollot.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.QueueEntryResponse
import com.frollot.mobile.model.QueueEntryStatus
import com.frollot.mobile.model.QueueStatusResponse
import com.frollot.mobile.model.Salon
import com.frollot.mobile.model.User
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.localization.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueManagementScreen(
    salon: Salon,
    currentUser: User,
    api: FrollotApi,
    onBack: () -> Unit
) {

    var queueStatus by remember { mutableStateOf<QueueStatusResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isActionRunning by remember { mutableStateOf(false) }

    // 🔒 MUTEX: Sérialise toutes les actions concurrentes
    val actionMutex = remember { Mutex() }

    val scope = rememberCoroutineScope()

    // 🔄 Fonction de fetch sécurisée (peut être appelée par polling ET actions)
    suspend fun fetchQueueStatus(showSpinner: Boolean) {
        if (showSpinner) {
            isLoading = true
        }
        try {
            queueStatus = api.getQueueStatus(salon.id)
            errorMessage = null
        } catch (e: Exception) {
            if (queueStatus == null) {
                errorMessage = e.message ?: "Impossible de charger la file"
            }
        } finally {
            if (showSpinner) {
                isLoading = false
            }
        }
    }

    // ⏱️ POLLING AUTOMATIQUE (15 secondes - propriétaire) - CORRIGÉ
    LaunchedEffect(salon.id) {
        while (isActive) { // CORRECTION : Utiliser isActive au lieu de true
            try {
                fetchQueueStatus(showSpinner = queueStatus == null)
                delay(15_000)
            } catch (e: CancellationException) {
                // Ne rien faire, c'est normal lors de l'annulation
                throw e // Relancer l'exception d'annulation
            }
        }
    }

    // 🔄 Refresh manuel
    fun refreshNow() {
        scope.launch {
            actionMutex.withLock {
                try {
                    isActionRunning = true
                    fetchQueueStatus(showSpinner = true)
                } finally {
                    isActionRunning = false
                }
            }
        }
    }

    // 🗑️ REMOVE: Protégé par mutex
    fun handleRemove(entry: QueueEntryResponse) {
        scope.launch {
            actionMutex.withLock {
                val previous = queueStatus
                // Optimistic update
                queueStatus = previous?.copy(
                    entries = previous.entries.filterNot { it.entryId == entry.entryId }
                )

                try {
                    isActionRunning = true
                    api.removeQueueEntry(
                        salonId = salon.id,
                        entryId = entry.entryId
                    )
                    // Re-fetch pour synchronisation complète
                    fetchQueueStatus(showSpinner = false)
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Impossible de mettre à jour la file"
                    // Rollback en cas d'erreur
                    queueStatus = previous
                } finally {
                    isActionRunning = false
                }
            }
        }
    }

    // 📣 CALL NEXT: Protégé par mutex
    fun handleCallNext(entry: QueueEntryResponse) {
        scope.launch {
            actionMutex.withLock {
                val previous = queueStatus
                // Optimistic update
                queueStatus = previous?.copy(
                    entries = previous.entries.map {
                        if (it.entryId == entry.entryId) {
                            it.copy(status = QueueEntryStatus.CALLED)
                        } else {
                            it
                        }
                    }
                )

                try {
                    isActionRunning = true
                    api.callNextClient(salon.id)
                    // Re-fetch pour synchronisation complète
                    fetchQueueStatus(showSpinner = false)
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Impossible d'appeler le client"
                    // Rollback en cas d'erreur
                    queueStatus = previous
                } finally {
                    isActionRunning = false
                }
            }
        }
    }

    val entries = queueStatus?.entries ?: emptyList()
    val totalWaiting = entries.count { it.status == QueueEntryStatus.WAITING }
    val calledCount = entries.count { it.status == QueueEntryStatus.CALLED }
    val averageWait = if (entries.isEmpty()) 0 else entries.sumOf { it.estimatedWaitMinutes } / entries.size
    val firstWaitingId = entries.firstOrNull { it.status == QueueEntryStatus.WAITING }?.entryId

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.QueueManagement.Title).replace("{salonName}", salon.name),
                showAvatar = false,
                actions = {
                    IconButton(
                        onClick = { refreshNow() },
                        enabled = !isActionRunning
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QueueDashboardHeader(
                totalWaiting = totalWaiting,
                calledCount = calledCount,
                averageWait = averageWait,
                isLoading = isLoading
            )

            when {
                isLoading && entries.isEmpty() -> {
                    ListLoadingState(
                        modifier = Modifier.weight(1f)
                    )
                }

                errorMessage != null && entries.isEmpty() -> {
                    ListErrorState(
                        message = errorMessage!!,
                        onRetry = { refreshNow() },
                        modifier = Modifier.weight(1f)
                    )
                }

                entries.isEmpty() -> {
                    ListEmptyState(
                        title = "Personne dans la file",
                        message = "Invitez vos clients à rejoindre la file depuis l'application cliente.",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(entries, key = { it.entryId }) { entry ->
                            QueueEntryManagementCard(
                                entry = entry,
                                isFirstWaiting = entry.entryId == firstWaitingId,
                                onCallNext = { handleCallNext(entry) },
                                onRemove = { handleRemove(entry) },
                                actionsEnabled = !isActionRunning
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueDashboardHeader(
    totalWaiting: Int,
    calledCount: Int,
    averageWait: Int,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QueueStatCard(
            title = "En attente",
            value = totalWaiting.toString(),
            icon = Icons.Default.Person,
            background = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            isLoading = isLoading,
            modifier = Modifier.weight(1f)
        )
        QueueStatCard(
            title = "Appelés",
            value = calledCount.toString(),
            icon = Icons.Default.CheckCircle,
            background = MaterialTheme.colorScheme.secondaryContainer,
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            isLoading = isLoading,
            modifier = Modifier.weight(1f)
        )
        QueueStatCard(
            title = "Attente moy.",
            value = "$averageWait min",
            icon = Icons.Default.Timer,
            background = MaterialTheme.colorScheme.tertiaryContainer,
            iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
            isLoading = isLoading,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QueueStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    iconColor: Color,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = iconColor
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QueueEntryManagementCard(
    entry: QueueEntryResponse,
    isFirstWaiting: Boolean,
    onCallNext: () -> Unit,
    onRemove: () -> Unit,
    actionsEnabled: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = entry.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.QueueManagement.Ticket).replace("{position}", entry.position.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (entry.status) {
                        QueueEntryStatus.WAITING -> MaterialTheme.colorScheme.primaryContainer
                        QueueEntryStatus.CALLED -> MaterialTheme.colorScheme.secondaryContainer
                        QueueEntryStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                        QueueEntryStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                ) {
                    Text(
                        text = entry.status.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (entry.status) {
                            QueueEntryStatus.WAITING -> MaterialTheme.colorScheme.onPrimaryContainer
                            QueueEntryStatus.CALLED -> MaterialTheme.colorScheme.onSecondaryContainer
                            QueueEntryStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                            QueueEntryStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Strings.QueueManagement.Arrived),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatQueueDateTime(entry.joinedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Strings.QueueManagement.EstimatedWait),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "~${entry.estimatedWaitMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = stringResource(Strings.QueueManagement.CallNext),
                    onClick = onCallNext,
                    enabled = isFirstWaiting && actionsEnabled && entry.status == QueueEntryStatus.WAITING,
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                    }
                )

                OutlinedButton(
                    onClick = onRemove,
                    enabled = actionsEnabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Strings.QueueManagement.Remove))
                }
            }
        }
    }
}

private fun formatQueueDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val sanitized = raw.replace("T", " ")
    return sanitized.take(minOf(sanitized.length, 16))
}
