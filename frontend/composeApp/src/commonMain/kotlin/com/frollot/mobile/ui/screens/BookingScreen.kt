@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.frollot.mobile.ui.utils.AnimationSpecs
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.time.currentInstant
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale


/**
 * Énumération des étapes du wizard de réservation
 */
enum class BookingStep {
    STAFF_SELECTION,
    SLOT_SELECTION,
    SUMMARY,
    SUCCESS
}

/**
 * Écran de réservation d'un service - Design Premium avec Wizard Flow
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BookingScreen(
    salon: Salon,
    service: SalonService,
    currentUser: User,
    onBack: () -> Unit,
    onBookingCreated: (BookingResponse) -> Unit
) {
    var currentStep by remember { mutableStateOf(BookingStep.STAFF_SELECTION) }
    var staffList by remember { mutableStateOf<List<StaffMember>>(emptyList()) }
    var selectedStaff by remember { mutableStateOf<StaffMember?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var availableSlots by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }
    var selectedSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var notes by remember { mutableStateOf("") }
    var createdBooking by remember { mutableStateOf<BookingResponse?>(null) }

    var isLoadingStaff by remember { mutableStateOf(true) }
    var isLoadingSlots by remember { mutableStateOf(false) }
    var isBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    // Charger les coiffeurs disponibles au démarrage
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoadingStaff = true
                staffList = api.getStaffBySpecialty(salon.id, service.category)
                isLoadingStaff = false

                // Présélectionner la date d'aujourd'hui
                selectedDate = currentInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement staff: ${e.message}")
                errorMessage = "Impossible de charger les coiffeurs"
                isLoadingStaff = false
            }
        }
    }

    // Charger les créneaux quand date ou staff change (seulement si on est sur l'écran de sélection de créneaux)
    LaunchedEffect(selectedDate, selectedStaff, currentStep) {
        if (currentStep == BookingStep.SLOT_SELECTION) {
            selectedDate?.let { date ->
                scope.launch {
                    try {
                        isLoadingSlots = true
                        selectedSlot = null

                        val request = AvailableSlotsRequest(
                            salonId = salon.id,
                            serviceId = service.id,
                            staffId = selectedStaff?.id,
                            date = date.atTime(9, 0).toInstant(TimeZone.currentSystemDefault()).toString()
                        )

                        val response = api.getAvailableSlots(salon.id, request)
                        availableSlots = response.slots.filter { it.available }
                        isLoadingSlots = false
                    } catch (e: Exception) {
                        FrollotLogger.error("API", "❌ Erreur chargement créneaux: ${e.message}")
                        errorMessage = "Impossible de charger les créneaux"
                        isLoadingSlots = false
                    }
                }
            }
        }
    }

    // Animation pour le changement d'étape
    val transitionState = remember { MutableTransitionState(currentStep) }
    val transition = updateTransition(transitionState, label = "stepTransition")

    // Couleurs MaterialTheme (plus besoin de couleurs hardcodées)

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = {
                    when (currentStep) {
                        BookingStep.STAFF_SELECTION -> onBack()
                        BookingStep.SLOT_SELECTION -> currentStep = BookingStep.STAFF_SELECTION
                        BookingStep.SUMMARY -> currentStep = BookingStep.SLOT_SELECTION
                        BookingStep.SUCCESS -> onBack()
                    }
                },
                onNavigateToProfile = {},
                title = "Réserver",
                showAvatar = false
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) with
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) { step ->
            when (step) {
                BookingStep.STAFF_SELECTION -> {
                    StaffSelectionScreen(
                        salon = salon,
                        service = service,
                        staffList = staffList,
                        selectedStaff = selectedStaff,
                        isLoading = isLoadingStaff,
                        onStaffSelected = {
                            selectedStaff = it
                            currentStep = BookingStep.SLOT_SELECTION
                        },
                        modifier = Modifier.padding(padding)
                    )
                }

                BookingStep.SLOT_SELECTION -> {
                    SlotSelectionScreen(
                        salon = salon,
                        service = service,
                        selectedStaff = selectedStaff,
                        selectedDate = selectedDate,
                        availableSlots = availableSlots,
                        selectedSlot = selectedSlot,
                        isLoadingSlots = isLoadingSlots,
                        onDateSelected = {
                            selectedDate = it
                        },
                        onSlotSelected = {
                            selectedSlot = it
                            currentStep = BookingStep.SUMMARY
                        },
                        modifier = Modifier.padding(padding)
                    )
                }

                BookingStep.SUMMARY -> {
                    BookingSummaryScreen(
                        salon = salon,
                        service = service,
                        selectedStaff = selectedStaff,
                        selectedDate = selectedDate,
                        selectedSlot = selectedSlot,
                        notes = notes,
                        onNotesChange = { notes = it },
                        isBooking = isBooking,
                        onConfirm = {
                            scope.launch {
                                try {
                                    isBooking = true
                                    val request = CreateBookingRequest(
                                        salonId = salon.id,
                                        clientId = currentUser.id,
                                        staffId = selectedStaff?.id,
                                        serviceId = service.id,
                                        bookingDatetime = selectedSlot!!.datetime,
                                        notesClient = notes.ifBlank { null }
                                    )
                                    val booking = api.createBooking(request)
                                    createdBooking = booking
                                    currentStep = BookingStep.SUCCESS
                                } catch (e: Exception) {
                                    FrollotLogger.error("API", "❌ Erreur création réservation: ${e.message}")
                                    errorMessage = e.message ?: "Erreur lors de la réservation"
                                } finally {
                                    isBooking = false
                                }
                            }
                        },
                        onBack = { currentStep = BookingStep.SLOT_SELECTION },
                        modifier = Modifier.padding(padding)
                    )
                }

                BookingStep.SUCCESS -> {
                    createdBooking?.let { booking ->
                        BookingSuccessScreen(
                            booking = booking,
                            onViewBookings = { onBookingCreated(booking) },
                            onPayNow = {
                                // Navigation vers l'écran de paiement
                                onBookingCreated(booking)
                            },
                            onBack = onBack,
                            modifier = Modifier.padding(padding)
                        )
                    } ?: run {
                        // Fallback si pas de booking créé
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    "Erreur: Réservation non trouvée",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                PrimaryButton(
                                    text = stringResource(Strings.Booking.BackToHome),
                                    onClick = onBack
                                )
                            }
                        }
                    }
                }
            }
        }

        // Message d'erreur global
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                ErrorCard(error) { errorMessage = null }
            }
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Cercle de l'étape
        Box(
            modifier = Modifier
                .size(32.dp)
                .shadow(
                    elevation = if (isActive || isCompleted) 4.dp else 0.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isActive || isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }
        )
    }
}

@Composable
fun ServiceSummaryCard(service: SalonService, salon: Salon) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        // En-tête avec gradient
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge de catégorie
                Box(
                    modifier = Modifier
                        .size(50.dp)
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
                        text = service.categoryEmoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = salon.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Badge de prix
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Text(
                    text = service.formattedPrice,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Informations détaillées
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Durée
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(Strings.Booking.Duration),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = service.formattedDuration,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Catégorie
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(Strings.Booking.Category),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = service.category.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaffSelectionSection(
    staffList: List<StaffMember>,
    selectedStaff: StaffMember?,
    isLoading: Boolean,
    onStaffSelected: (StaffMember?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Titre de section
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Strings.Booking.ChooseExpert),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.Booking.ChooseExpertSubtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when {
            isLoading -> {
                // Loading animation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .shadow(4.dp, CircleShape)
                    ) {
                    }
                    Text(
                        "Chargement des experts...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            staffList.isEmpty() -> {
                EmptyStateCard(
                    icon = Icons.Default.PeopleAlt,
                    title = "Aucun expert disponible",
                    subtitle = "Les coiffeurs seront bientôt de retour"
                )
            }

            else -> {
                // Option "N'importe quel coiffeur"
                AnimatedStaffCard(
                    name = "Laisser le salon choisir",
                    description = "Nous vous attribuerons le meilleur expert disponible",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = selectedStaff == null,
                    onClick = { onStaffSelected(null) }
                )

                // Liste des coiffeurs
                staffList.forEach { staff ->
                    AnimatedStaffCard(
                        name = staff.fullName,
                        description = staff.specialtiesText,
                        avatarUrl = staff.userAvatarUrl,
                        initials = "${staff.userFirstName.firstOrNull()?.uppercase() ?: ""}${staff.userLastName.firstOrNull()?.uppercase() ?: ""}",
                        isSelected = staff == selectedStaff,
                        onClick = { onStaffSelected(staff) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedStaffCard(
    name: String,
    description: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Person,
    avatarUrl: String? = null,
    initials: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = AnimationSpecs.SoftTouchInteraction, label = "staff_scale"
    )

    val borderColor = if (isSelected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.surface

    StandardCardNoPadding(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar avec photo ou initiales
            Box(
                modifier = Modifier
                    .size(60.dp)
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
                if (!avatarUrl.isNullOrBlank()) {
                    // Afficher la photo de profil
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Photo de $name",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!initials.isNullOrBlank()) {
                    // Afficher les initiales
                    Text(
                        text = initials.take(2),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    // Fallback: icône par défaut
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informations
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )

                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Indicateur de sélection
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelectionSection(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Titre de section
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Strings.Booking.SelectDate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.Booking.SelectDateSubtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Grille de dates
        val today = currentInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dates = (0..13).map { today.plus(it, DateTimeUnit.DAY) }

        LazyColumn(
            modifier = Modifier.height(160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates.chunked(7)) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    week.forEach { date ->
                        DateCard(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lun"
        DayOfWeek.TUESDAY -> "Mar"
        DayOfWeek.WEDNESDAY -> "Mer"
        DayOfWeek.THURSDAY -> "Jeu"
        DayOfWeek.FRIDAY -> "Ven"
        DayOfWeek.SATURDAY -> "Sam"
        DayOfWeek.SUNDAY -> "Dim"
        else -> ""
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = AnimationSpecs.SoftTouchInteraction,
        label = "date_scale"
    )

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val dayTextColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isToday && !isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
            // Jour de la semaine
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = dayTextColor
            )

            // Jour du mois
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Indicateur "Aujourd'hui"
            if (isToday && !isSelected) {
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
        }
    }
}

@Composable
fun TimeSlotChip(
    slot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = AnimationSpecs.SoftTouchInteraction,
        label = "timeslot_scale"
    )

    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) 
        MaterialTheme.colorScheme.onPrimary 
    else 
        MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = slot.formattedTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun TimeSlotsGrid(
    availableSlots: List<TimeSlot>,
    selectedSlot: TimeSlot?,
    isLoadingSlots: Boolean,
    onSlotSelected: (TimeSlot) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Titre de section
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Strings.Booking.ChooseTimeSlot),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.Booking.ChooseTimeSlotSubtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when {
            isLoadingSlots -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Recherche des disponibilités...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            availableSlots.isEmpty() -> {
                EmptyStateCard(
                    icon = Icons.Default.EventBusy,
                    title = "Aucun créneau disponible",
                    subtitle = "Veuillez choisir une autre date"
                )
            }

            else -> {
                // Grille de créneaux avec hauteur fixe pour éviter les contraintes infinies
                StandardCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 400.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(availableSlots) { slot ->
                            TimeSlotChip(
                                slot = slot,
                                isSelected = slot == selectedSlot,
                                onClick = { onSlotSelected(slot) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Titre de section
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Strings.Booking.NotesOrSpecialRequests),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.Booking.NotesOrSpecialRequestsSubtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Champ de notes
        StandardCardNoPadding(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Ex: Coupe dégradée, préférence de style...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Strings.Booking.NotesInfo),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConfirmButton(
    isBooking: Boolean,
    onClick: () -> Unit
) {
    PrimaryButton(
        text = if (isBooking) "Réservation en cours..." else "Confirmer la réservation",
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isBooking,
        size = ButtonSize.Large,
        icon = if (!isBooking) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null
    )
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    StandardCardNoPadding(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Oups ! Une erreur est survenue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ========================================
// SCREENS SÉPARÉS DU WIZARD (Design Premium)
// ========================================

/**
 * Écran de sélection du coiffeur (Étape 1) - Design Premium
 */
@Composable
fun StaffSelectionScreen(
    salon: Salon,
    service: SalonService,
    staffList: List<StaffMember>,
    selectedStaff: StaffMember?,
    isLoading: Boolean,
    onStaffSelected: (StaffMember?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ServiceSummaryCard(service, salon)
        }

        item {
            StaffSelectionSection(
                staffList = staffList,
                selectedStaff = selectedStaff,
                isLoading = isLoading,
                onStaffSelected = onStaffSelected
            )
        }

        if (selectedStaff != null || staffList.isEmpty()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedStaff != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = stringResource(Strings.Booking.ExpertSelected),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    PrimaryButton(
                        text = stringResource(Strings.Booking.Continue),
                        onClick = { onStaffSelected(selectedStaff) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        icon = {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Écran de sélection de date et créneau (Étape 2) - Design Premium
 */
@Composable
fun SlotSelectionScreen(
    salon: Salon,
    service: SalonService,
    selectedStaff: StaffMember?,
    selectedDate: LocalDate?,
    availableSlots: List<TimeSlot>,
    selectedSlot: TimeSlot?,
    isLoadingSlots: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onSlotSelected: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ServiceSummaryCard(service, salon)
        }

        // Coiffeur sélectionné (si choisi)
        selectedStaff?.let { staff ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Votre expert",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                staff.fullName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            DateSelectionSection(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }

        if (selectedDate != null) {
            item {
                TimeSlotsGrid(
                    availableSlots = availableSlots,
                    selectedSlot = selectedSlot,
                    isLoadingSlots = isLoadingSlots,
                    onSlotSelected = onSlotSelected
                )
            }
        }

        if (selectedSlot != null) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = stringResource(Strings.Booking.TimeSlotSelected),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Button(
                        onClick = { onSlotSelected(selectedSlot) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Continuer vers la confirmation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Écran de récapitulatif et confirmation (Étape 3) - Design Premium
 */
@Composable
fun BookingSummaryScreen(
    salon: Salon,
    service: SalonService,
    selectedStaff: StaffMember?,
    selectedDate: LocalDate?,
    selectedSlot: TimeSlot?,
    notes: String,
    onNotesChange: (String) -> Unit,
    isBooking: Boolean,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Récapitulatif de votre réservation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Vérifiez les détails avant de confirmer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // En-tête
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Détails de la réservation",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Votre rendez-vous en quelques points",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lignes de détails
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Service
                        BookingSummaryRow(
                            icon = Icons.Default.ContentCut,
                            label = "Service",
                            value = service.name,
                            valueColor = MaterialTheme.colorScheme.primary
                        )

                        // Salon
                        BookingSummaryRow(
                            icon = Icons.Default.Store,
                            label = "Salon",
                            value = salon.name
                        )

                        // Coiffeur
                        BookingSummaryRow(
                            icon = Icons.Default.Person,
                            label = "Coiffeur",
                            value = selectedStaff?.fullName ?: "Laisser le salon choisir"
                        )

                        // Date
                        selectedDate?.let { date ->
                            val dayName = when (date.dayOfWeek) {
                                DayOfWeek.MONDAY -> "Lundi"
                                DayOfWeek.TUESDAY -> "Mardi"
                                DayOfWeek.WEDNESDAY -> "Mercredi"
                                DayOfWeek.THURSDAY -> "Jeudi"
                                DayOfWeek.FRIDAY -> "Vendredi"
                                DayOfWeek.SATURDAY -> "Samedi"
                                DayOfWeek.SUNDAY -> "Dimanche"
                                else -> ""
                            }
                            val monthName = when (date.monthNumber) {
                                1 -> "janvier"
                                2 -> "février"
                                3 -> "mars"
                                4 -> "avril"
                                5 -> "mai"
                                6 -> "juin"
                                7 -> "juillet"
                                8 -> "août"
                                9 -> "septembre"
                                10 -> "octobre"
                                11 -> "novembre"
                                12 -> "décembre"
                                else -> ""
                            }
                            BookingSummaryRow(
                                icon = Icons.Default.CalendarMonth,
                                label = "Date",
                                value = "$dayName ${date.dayOfMonth} $monthName"
                            )
                        }

                        // Heure
                        selectedSlot?.let { slot ->
                            BookingSummaryRow(
                                icon = Icons.Default.Schedule,
                                label = "Heure",
                                value = slot.formattedTime
                            )
                        }

                        // Durée
                        BookingSummaryRow(
                            icon = Icons.Default.Timer,
                            label = "Durée",
                            value = service.formattedDuration
                        )

                        // Prix
                        BookingSummaryRow(
                            icon = Icons.Default.Euro,
                            label = "Prix",
                            value = service.formattedPrice,
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            isBold = true
                        )
                    }
                }
            }
        }

        item {
            NotesSection(
                notes = notes,
                onNotesChange = onNotesChange
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConfirmButton(
                    isBooking = isBooking,
                    onClick = onConfirm
                )

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Retour à la sélection")
                }
            }
        }
    }
}

@Composable
fun BookingSummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Écran de succès après réservation (Étape 4) - Design Premium
 */
@Composable
fun BookingSuccessScreen(
    booking: BookingResponse,
    onViewBookings: () -> Unit,
    onPayNow: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                // Animation de succès
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(16.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Réservation confirmée !",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "Votre rendez-vous est programmé",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "Un email de confirmation vous a été envoyé",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Carte de confirmation
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // En-tête
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Icon(
                                    Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "Votre rendez-vous",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "N°${booking.id.substring(0, 8)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Détails
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BookingSummaryRow(
                                icon = Icons.Default.Store,
                                label = "Salon",
                                value = booking.salonName
                            )
                            BookingSummaryRow(
                                icon = Icons.Default.ContentCut,
                                label = "Service",
                                value = booking.serviceName
                            )
                            BookingSummaryRow(
                                icon = Icons.Default.Schedule,
                                label = "Date & Heure",
                                value = try {
                                    val datetime = booking.bookingDatetime.substring(0, 16).replace("T", " à ")
                                    datetime
                                } catch (e: Exception) {
                                    booking.bookingDatetime
                                }
                            )
                            booking.staffName?.let { staff ->
                                BookingSummaryRow(
                                    icon = Icons.Default.Person,
                                    label = "Coiffeur",
                                    value = staff
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bouton "Payer maintenant" si le paiement n'est pas effectué
                    if (booking.paymentStatus == com.frollot.mobile.model.PaymentStatus.UNPAID) {
                        PrimaryButton(
                            text = stringResource(Strings.Booking.PayNow),
                            onClick = onPayNow,
                            modifier = Modifier.fillMaxWidth(),
                            size = ButtonSize.Large,
                            icon = {
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    // Bouton principal
                    PrimaryButton(
                        text = stringResource(Strings.Booking.ViewMyBookings),
                        onClick = onViewBookings,
                        modifier = Modifier.fillMaxWidth(),
                        size = ButtonSize.Large,
                        icon = {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    // Bouton secondaire
                    SecondaryButton(
                        text = stringResource(Strings.Booking.BackToHome),
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        size = ButtonSize.Large,
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    "Merci de votre confiance ! 💈",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
