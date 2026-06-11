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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.ButtonSize
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.dialogs.StandardDialog
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.utils.formatBookingDateTime
import com.frollot.mobile.ui.utils.StatusBadge
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran affichant toutes les réservations d'un client.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    currentUser: User,
    onMenuClick: () -> Unit = {},
    onBookingClick: (String) -> Unit,
    onBack: () -> Unit = {},
    onReview: (BookingResponse, Salon) -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    var salonCache by remember { mutableStateOf<Map<String, Salon>>(emptyMap()) }
    var bookings by remember { mutableStateOf<List<BookingResponse>>(emptyList()) }
    var filteredBookings by remember { mutableStateOf<List<BookingResponse>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf(BookingFilter.ALL) }

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    // Fonction de chargement des réservations
    fun loadBookings(showLoading: Boolean = true) {
        scope.launch {
            try {
                if (showLoading) isLoading = true
                isRefreshing = !showLoading
                errorMessage = null

                FrollotLogger.debug("API", "📥 API: Chargement des réservations pour user ${currentUser.id}")
                bookings = api.getClientBookings(currentUser.id)

                // DEBUG: Log détaillé de toutes les réservations
                FrollotLogger.debug("API", "📊 Réservations chargées: ${bookings.size}")
                bookings.forEachIndexed { index, booking ->
                    FrollotLogger.debug("MyBookingsScreen", "   ${index + 1}. ID: ${booking.id}")
                    FrollotLogger.debug("MyBookingsScreen", "      → Salon: ${booking.salonName}")
                    FrollotLogger.debug("MyBookingsScreen", "      → Service: ${booking.serviceName}")
                    FrollotLogger.debug("MyBookingsScreen", "      → Status: ${booking.status} (label: ${booking.statusLabel})")
                    FrollotLogger.debug("MyBookingsScreen", "      → isPast: ${booking.isPast}")
                    FrollotLogger.debug("MyBookingsScreen", "      → Date: ${booking.bookingDatetime}")
                    FrollotLogger.debug("MyBookingsScreen", "      → Can cancel: ${booking.canBeCancelled}")
                    FrollotLogger.debug("MyBookingsScreen", "      → Completed: ${booking.status == BookingStatus.COMPLETED}")
                }

                // Appliquer le filtre
                filteredBookings = when (selectedFilter) {
                    BookingFilter.ALL -> bookings
                    BookingFilter.UPCOMING -> bookings.filter {
                        !it.isPast &&
                                it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)
                    }
                    BookingFilter.PAST -> bookings.filter {
                        it.isPast ||
                                it.status in listOf(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.NO_SHOW)
                    }
                }

                // DEBUG: Log des réservations filtrées
                FrollotLogger.debug("MyBookingsScreen", "🎯 Réservations filtrées (${selectedFilter.label}): ${filteredBookings.size}")
                val completedCount = filteredBookings.count { it.status == BookingStatus.COMPLETED }
                FrollotLogger.debug("API", "   → Réservations COMPLETED dans le filtre: $completedCount")

                isLoading = false
                isRefreshing = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement réservations: ${e.message}")
                e.printStackTrace()
                errorMessage = "Impossible de charger les réservations"
                isLoading = false
                isRefreshing = false
            }
        }
    }

    // Charger au démarrage
    LaunchedEffect(Unit) {
        loadBookings()
    }

    // Recharger quand le filtre change
    LaunchedEffect(selectedFilter) {
        filteredBookings = when (selectedFilter) {
            BookingFilter.ALL -> bookings
            BookingFilter.UPCOMING -> bookings.filter {
                !it.isPast &&
                        it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)
            }
            BookingFilter.PAST -> bookings.filter {
                it.isPast ||
                        it.status in listOf(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.NO_SHOW)
            }
        }
        FrollotLogger.debug("Refresh", "🔄 Filtre changé: '${selectedFilter.label}' -> ${filteredBookings.size} réservations")
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onMenuClick = onMenuClick,
                onNavigateToProfile = onNavigateToProfile,
                title = "Mes Réservations",
                actions = {
                    // Bouton actualiser
                    IconButton(
                        onClick = { loadBookings(showLoading = false) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRefreshing) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = if (isRefreshing) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Filtres améliorés
            BookingFilters(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                counts = mapOf(
                    BookingFilter.ALL to bookings.size,
                    BookingFilter.UPCOMING to bookings.count {
                        !it.isPast &&
                                it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)
                    },
                    BookingFilter.PAST to bookings.count {
                        it.isPast ||
                                it.status in listOf(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.NO_SHOW)
                    }
                )
            )

            // Contenu
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        // État de chargement
                        ListLoadingState(
                            message = "Nous préparons vos rendez-vous"
                        )
                    }

                    errorMessage != null -> {
                        // État d'erreur
                        ListErrorState(
                            message = errorMessage ?: "Une erreur est survenue",
                            onRetry = { loadBookings() },
                            retryButtonText = "Réessayer"
                        )
                    }

                    filteredBookings.isEmpty() -> {
                        EmptyBookingsView(filter = selectedFilter)
                    }

                    else -> {
                        // Compteur élégant
                        StandardCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${filteredBookings.size} réservation${if (filteredBookings.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    "Trier par: Date ▼",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Liste des réservations avec espacement
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 32.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredBookings) { booking ->
                                var hasReview by remember { mutableStateOf(false) }

                                // Vérifier si un avis existe pour cette réservation
                                LaunchedEffect(booking.id) {
                                    FrollotLogger.debug("MyBookingsScreen", "🔄 Vérification avis pour booking: ${booking.id}")
                                    FrollotLogger.debug("API", "   → Salon: ${booking.salonName}")
                                    FrollotLogger.debug("API", "   → Service: ${booking.serviceName}")
                                    FrollotLogger.debug("API", "   → Status: ${booking.status} (${booking.statusLabel})")
                                    FrollotLogger.debug("API", "   → isPast: ${booking.isPast}")
                                    FrollotLogger.debug("API", "   → Client ID: ${currentUser.id}")

                                    if (booking.status == BookingStatus.COMPLETED) {
                                        FrollotLogger.debug("MyBookingsScreen", "   ✅ Réservation COMPLETED - Vérification d'avis...")
                                        try {
                                            val exists = api.hasReviewForBooking(booking.id)
                                            hasReview = exists
                                            FrollotLogger.debug("MyBookingsScreen", "   📊 Avis existe? $exists")
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("MyBookingsScreen", "   ❌ Erreur vérification avis: ${e.message}")
                                            hasReview = false
                                        }
                                    } else {
                                        FrollotLogger.debug("MyBookingsScreen", "   ⚠️ Réservation NON COMPLETED (status: ${booking.status}) - Pas de vérification d'avis")
                                    }
                                }

                                BookingCard(
                                    booking = booking,
                                    onClick = { onBookingClick(booking.id) },
                                    onCancel = {
                                        scope.launch {
                                            try {
                                                api.cancelBooking(booking.id)
                                                loadBookings(showLoading = false)
                                            } catch (e: Exception) {
                                                errorMessage = "Impossible d'annuler la réservation"
                                            }
                                        }
                                    },
                                    onReview = if (booking.status == BookingStatus.COMPLETED) {
                                        { bookingResponse ->
                                            FrollotLogger.debug("MyBookingsScreen", "🎯 Clic sur 'Laisser un avis' pour booking ${bookingResponse.id}")
                                            // Charger le salon si nécessaire
                                            scope.launch {
                                                try {
                                                    val salon = salonCache[bookingResponse.salonId]
                                                        ?: api.getSalonById(bookingResponse.salonId).also {
                                                            salonCache = salonCache + (bookingResponse.salonId to it)
                                                        }
                                                    FrollotLogger.debug("API", "   → Salon chargé: ${salon.name}")
                                                    onReview(bookingResponse, salon)
                                                } catch (e: Exception) {
                                                    errorMessage = "Impossible de charger les informations du salon"
                                                }
                                            }
                                        }
                                    } else null,
                                    hasReview = hasReview
                                )
                            }
                        }
                    }
                }

                // Indicateur de rafraîchissement amélioré
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
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
        }
    }
}

enum class BookingFilter(val label: String) {
    ALL("Toutes"),
    UPCOMING("À venir"),
    PAST("Passées")
}

@Composable
fun BookingFilters(
    selectedFilter: BookingFilter,
    onFilterSelected: (BookingFilter) -> Unit,
    counts: Map<BookingFilter, Int>
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookingFilter.entries.forEach { filter ->
                Card(
                    onClick = { onFilterSelected(filter) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (filter == selectedFilter) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (filter == selectedFilter) 1.dp else 0.dp,
                        pressedElevation = 2.dp
                    ),
                    border = BorderStroke(
                        0.5.dp,
                        if (filter == selectedFilter) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${counts[filter] ?: 0}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (filter == selectedFilter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            filter.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (filter == selectedFilter) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (filter == selectedFilter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: BookingResponse,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    onReview: ((BookingResponse) -> Unit)? = null,
    hasReview: Boolean = false
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    // DEBUG: Log pour voir ce qui se passe dans BookingCard
    FrollotLogger.debug("MyBookingsScreen", "🎴 BookingCard: ${booking.id}")
    FrollotLogger.debug("API", "   → Status: ${booking.status} (${booking.statusLabel})")
    FrollotLogger.debug("API", "   → onReview non null? ${onReview != null}")
    FrollotLogger.debug("API", "   → COMPLETED? ${booking.status == BookingStatus.COMPLETED}")
    FrollotLogger.debug("API", "   → hasReview? $hasReview")
    FrollotLogger.debug("API", "   → Bouton visible? ${booking.status == BookingStatus.COMPLETED && onReview != null}")

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // En-tête : Salon + Statut
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
                        text = booking.salonName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Note ou info supplémentaire
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "4.8 ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Service avec icône colorée
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.MyBookings.Service),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = booking.serviceName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Détails sur 2 colonnes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Colonne gauche : Date et Heure
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(Strings.MyBookings.DateTime),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatBookingDateTime(booking.bookingDatetime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Coiffeur
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(Strings.MyBookings.Hairstylist),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = booking.staffInfo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Colonne droite : Prix
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(Strings.MyBookings.Amount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = booking.formattedPrice ?: "Prix non défini",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // IMPORTANT: Condition corrigée pour afficher le bouton d'avis
                    when {
                        booking.status == BookingStatus.COMPLETED && onReview != null -> {
                            FrollotLogger.debug("MyBookingsScreen", "   🎯 BookingCard: Affichage bouton avis pour ${booking.id}")
                            FrollotLogger.debug("MyBookingsScreen", "      → hasReview: $hasReview")

                            if (hasReview) {
                                // Avis déjà laissé
                                SecondaryButton(
                                    text = stringResource(Strings.MyBookings.ReviewLeft),
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            } else {
                                // Bouton pour laisser un avis
                                PrimaryButton(
                                    text = stringResource(Strings.MyBookings.LeaveReview),
                                    onClick = {
                                        FrollotLogger.debug("MyBookingsScreen", "   🎯 Clic sur 'Laisser un avis' dans BookingCard pour ${booking.id}")
                                        onReview(booking)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                        booking.canBeCancelled -> {
                            SecondaryButton(
                                text = stringResource(Strings.MyBookings.Cancel),
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Séparateur et informations complémentaires
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Note ou message selon le statut
            Text(
                text = when (booking.status) {
                    BookingStatus.CONFIRMED -> "✅ Votre réservation est confirmée"
                    BookingStatus.PENDING -> "⏳ En attente de confirmation"
                    BookingStatus.COMPLETED -> "✨ Prestation terminée"
                    BookingStatus.CANCELLED -> "❌ Réservation annulée"
                    BookingStatus.IN_PROGRESS -> "⚡ Prestation en cours"
                    BookingStatus.NO_SHOW -> "👻 Client absent"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when (booking.status) {
                    BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.tertiary
                    BookingStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                    BookingStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
                    BookingStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    BookingStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                    BookingStatus.NO_SHOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        }
    }

    // Dialog de confirmation d'annulation
    if (showCancelDialog) {
        StandardDialog(
            onDismissRequest = { showCancelDialog = false },
            title = "Annuler la réservation ?",
            text = "Êtes-vous sûr de vouloir annuler cette réservation ? Cette action est irréversible.",
            confirmButton = {
                PrimaryButton(
                    text = stringResource(Strings.MyBookings.CancelConfirm),
                    onClick = {
                        onCancel()
                        showCancelDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            },
            dismissButton = {
                SecondaryButton(
                    text = stringResource(Strings.MyBookings.CancelKeep),
                    onClick = { showCancelDialog = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}


@Composable
fun EmptyBookingsView(filter: BookingFilter) {
    ListEmptyState(
        icon = {
            Icon(
                imageVector = when (filter) {
                    BookingFilter.ALL -> Icons.Outlined.CalendarToday
                    BookingFilter.UPCOMING -> Icons.Outlined.Schedule
                    BookingFilter.PAST -> Icons.Outlined.History
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        title = when (filter) {
            BookingFilter.ALL -> "Aucune réservation"
            BookingFilter.UPCOMING -> "Aucune réservation à venir"
            BookingFilter.PAST -> "Aucune réservation passée"
        },
        message = when (filter) {
            BookingFilter.ALL -> "Réservez votre première prestation !"
            BookingFilter.UPCOMING -> "Prenez rendez-vous pour vos prochains soins"
            BookingFilter.PAST -> "Vos réservations passées apparaîtront ici"
        },
        action = if (filter == BookingFilter.ALL) {
            {
                StandardCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Parcourez les salons pour réserver",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else null
    )
}
