package com.frollot.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.utils.formatBookingDateTime
import com.frollot.mobile.ui.utils.StatusBadge
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran de gestion des rendez-vous pour les propriétaires de salons.
 * Permet de voir, filtrer et gérer toutes les réservations d'un salon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingsManagementScreen(
    salon: Salon,
    currentUser: User,
    onBack: () -> Unit,
    onBookingClick: (String) -> Unit = {}
) {
    var bookings by remember { mutableStateOf<List<BookingResponse>>(emptyList()) }
    var filteredBookings by remember { mutableStateOf<List<BookingResponse>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf(OwnerBookingFilter.ALL) }
    var statistics by remember { mutableStateOf<BookingStatistics?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var actionInProgress by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    // Fonction pour appliquer le filtre
    fun applyFilter(filter: OwnerBookingFilter, bookingsList: List<BookingResponse>) {
        filteredBookings = when (filter) {
            OwnerBookingFilter.ALL -> bookingsList
            OwnerBookingFilter.PENDING -> bookingsList.filter { it.status == BookingStatus.PENDING }
            OwnerBookingFilter.CONFIRMED -> bookingsList.filter { it.status == BookingStatus.CONFIRMED }
            OwnerBookingFilter.IN_PROGRESS -> bookingsList.filter { it.status == BookingStatus.IN_PROGRESS }
            OwnerBookingFilter.COMPLETED -> bookingsList.filter { it.status == BookingStatus.COMPLETED }
            OwnerBookingFilter.CANCELLED -> bookingsList.filter { it.status == BookingStatus.CANCELLED }
            OwnerBookingFilter.NO_SHOW -> bookingsList.filter { it.status == BookingStatus.NO_SHOW }
        }
    }

    // Fonction de chargement des réservations
    fun loadBookings(showLoading: Boolean = true) {
        scope.launch {
            try {
                if (showLoading) isLoading = true
                isRefreshing = !showLoading
                errorMessage = null

                bookings = api.getSalonBookings(salon.id)
                
                // Charger les statistiques
                try {
                    statistics = api.getBookingStatistics(salon.id)
                } catch (e: Exception) {
                    // Statistiques optionnelles, ne pas bloquer si erreur
                    // Log l'erreur si nécessaire
                }

                // Appliquer le filtre
                applyFilter(selectedFilter, bookings)

                isLoading = false
                isRefreshing = false
            } catch (e: Exception) {
                errorMessage = "Impossible de charger les réservations"
                isLoading = false
                isRefreshing = false
            }
        }
    }

    // Charger au démarrage
    LaunchedEffect(salon.id) {
        loadBookings()
    }

    // Recharger quand le filtre change
    LaunchedEffect(selectedFilter) {
        applyFilter(selectedFilter, bookings)
    }

    // Auto-refresh toutes les 30 secondes
    LaunchedEffect(salon.id) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            if (!isLoading && !isRefreshing) {
                loadBookings(showLoading = false)
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.OwnerBookingsManagement.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Statistiques en en-tête
            statistics?.let { stats ->
                OwnerBookingStatsCard(stats)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filtres
            OwnerBookingFilters(
                selectedFilter = selectedFilter,
                onFilterSelected = { 
                    selectedFilter = it
                    applyFilter(it, bookings)
                },
                counts = mapOf(
                    OwnerBookingFilter.ALL to bookings.size,
                    OwnerBookingFilter.PENDING to bookings.count { it.status == BookingStatus.PENDING },
                    OwnerBookingFilter.CONFIRMED to bookings.count { it.status == BookingStatus.CONFIRMED },
                    OwnerBookingFilter.IN_PROGRESS to bookings.count { it.status == BookingStatus.IN_PROGRESS },
                    OwnerBookingFilter.COMPLETED to bookings.count { it.status == BookingStatus.COMPLETED },
                    OwnerBookingFilter.CANCELLED to bookings.count { it.status == BookingStatus.CANCELLED },
                    OwnerBookingFilter.NO_SHOW to bookings.count { it.status == BookingStatus.NO_SHOW }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        ListLoadingState()
                    }

                    errorMessage != null -> {
                        ListErrorState(
                            message = errorMessage!!,
                            onRetry = { loadBookings() }
                        )
                    }

                    filteredBookings.isEmpty() -> {
                        ListEmptyState(
                            title = when (selectedFilter) {
                                OwnerBookingFilter.ALL -> "Aucune réservation"
                                OwnerBookingFilter.PENDING -> "Aucune réservation en attente"
                                OwnerBookingFilter.CONFIRMED -> "Aucune réservation confirmée"
                                OwnerBookingFilter.IN_PROGRESS -> "Aucune réservation en cours"
                                OwnerBookingFilter.COMPLETED -> "Aucune réservation terminée"
                                OwnerBookingFilter.CANCELLED -> "Aucune réservation annulée"
                                OwnerBookingFilter.NO_SHOW -> "Aucune absence"
                            },
                            message = "Les réservations apparaîtront ici une fois créées.",
                            icon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }

                    else -> {
                        // Liste des réservations
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 32.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredBookings) { booking ->
                                OwnerBookingCard(
                                    booking = booking,
                                    onClick = { onBookingClick(booking.id) },
                                    onAction = { action, bookingId ->
                                        scope.launch {
                                            try {
                                                actionInProgress = bookingId
                                                val newStatus = when (action) {
                                                    OwnerBookingAction.CONFIRM -> BookingStatus.CONFIRMED
                                                    OwnerBookingAction.START -> BookingStatus.IN_PROGRESS
                                                    OwnerBookingAction.COMPLETE -> BookingStatus.COMPLETED
                                                    OwnerBookingAction.CANCEL -> BookingStatus.CANCELLED
                                                    OwnerBookingAction.MARK_NO_SHOW -> BookingStatus.NO_SHOW
                                                }
                                                api.updateBookingStatus(
                                                    bookingId,
                                                    UpdateBookingStatusRequest(newStatus)
                                                )
                                                loadBookings(showLoading = false)
                                            } catch (e: Exception) {
                                                errorMessage = "Impossible de mettre à jour la réservation"
                                            } finally {
                                                actionInProgress = null
                                            }
                                        }
                                    },
                                    actionInProgress = actionInProgress == booking.id
                                )
                            }
                        }
                    }
                }

                // Indicateur de rafraîchissement
                if (isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

enum class OwnerBookingFilter(val label: String) {
    ALL("Toutes"),
    PENDING("En attente"),
    CONFIRMED("Confirmées"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminées"),
    CANCELLED("Annulées"),
    NO_SHOW("Absences")
}

enum class OwnerBookingAction {
    CONFIRM,
    START,
    COMPLETE,
    CANCEL,
    MARK_NO_SHOW
}

@Composable
fun OwnerBookingStatsCard(statistics: BookingStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Statistiques",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = statistics.totalBookings.toString(),
                    label = "Total",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    value = statistics.pendingBookings.toString(),
                    label = "En attente",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    value = statistics.confirmedBookings.toString(),
                    label = "Confirmées",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    value = statistics.completedBookings.toString(),
                    label = "Terminées",
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OwnerBookingFilters(
    selectedFilter: OwnerBookingFilter,
    onFilterSelected: (OwnerBookingFilter) -> Unit,
    counts: Map<OwnerBookingFilter, Int>
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OwnerBookingFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "${counts[filter] ?: 0}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                filter.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    modifier = Modifier.widthIn(min = 80.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun OwnerBookingCard(
    booking: BookingResponse,
    onClick: () -> Unit,
    onAction: (OwnerBookingAction, String) -> Unit,
    actionInProgress: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // En-tête : Client + Statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = booking.clientName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = booking.serviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Détails
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date & Heure
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatBookingDateTime(booking.bookingDatetime),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Coiffeur
                    booking.staffName?.let { staffName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = staffName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Prix
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = booking.formattedPrice ?: "N/A",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = booking.paymentBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions rapides selon le statut
            when (booking.status) {
                BookingStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAction(OwnerBookingAction.CONFIRM, booking.id) },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (actionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Strings.OwnerBookingsManagement.Confirm), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        OutlinedButton(
                            onClick = { onAction(OwnerBookingAction.CANCEL, booking.id) },
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Strings.Common.Cancel), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                BookingStatus.CONFIRMED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAction(OwnerBookingAction.START, booking.id) },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (actionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Strings.OwnerBookingsManagement.Start), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        OutlinedButton(
                            onClick = { onAction(OwnerBookingAction.MARK_NO_SHOW, booking.id) },
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Strings.OwnerBookingsManagement.Absent), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                BookingStatus.IN_PROGRESS -> {
                    Button(
                        onClick = { onAction(OwnerBookingAction.COMPLETE, booking.id) },
                        enabled = !actionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (actionInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Strings.OwnerBookingsManagement.Finish), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                else -> {
                    // Statuts finaux : pas d'actions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = when (booking.status) {
                                BookingStatus.COMPLETED -> "✅ Prestation terminée"
                                BookingStatus.CANCELLED -> "❌ Réservation annulée"
                                BookingStatus.NO_SHOW -> "👻 Client absent"
                                else -> ""
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


