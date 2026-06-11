package com.frollot.mobile.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.dialogs.StandardDialog
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.utils.formatBookingDateTime
import com.frollot.mobile.ui.utils.AnimationSpecs
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran de détail d'une réservation avec design épuré et clair
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    onSalonClick: (String) -> Unit = {}
) {
    var booking by remember { mutableStateOf<BookingResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isCancelling by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    // Charger la réservation
    LaunchedEffect(bookingId) {
        scope.launch {
            try {
                isLoading = true
                booking = api.getBookingById(bookingId)
                isLoading = false
            } catch (_: Exception) {
                errorMessage = "Impossible de charger la réservation"
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                StandardAppHeader(
                    currentUser = null,
                    onBackClick = onBack,
                    onNavigateToProfile = {},
                    title = "Détail Réservation",
                    showAvatar = false
                )
            }
        ) { padding ->
            when {
                isLoading -> {
                    ListLoadingState()
                }

                errorMessage != null -> {
                    ListErrorState(
                        message = errorMessage!!,
                        onRetry = onBack
                    )
                }

                booking != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Carte de statut
                        item {
                            PremiumStatusCard(booking!!)
                        }

                        // Carte de salon
                        item {
                            PremiumSalonCard(
                                booking = booking!!,
                                onSalonClick = { onSalonClick(booking!!.salonId) }
                            )
                        }

                        // Section Services
                        item {
                            PremiumServiceCard(booking!!)
                        }

                        // Section Réservation
                        item {
                            PremiumBookingCard(booking!!)
                        }

                        // Section Paiement
                        item {
                            PremiumPaymentCard(booking!!)
                        }

                        // Notes du client
                        if (!booking!!.notesClient.isNullOrBlank()) {
                            item {
                                PremiumNotesCard(
                                    title = "Vos Notes",
                                    content = booking!!.notesClient!!,
                                    icon = Icons.Outlined.EditNote,
                                    gradientColors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        // Notes du salon
                        if (!booking!!.notesSalon.isNullOrBlank()) {
                            item {
                                PremiumNotesCard(
                                    title = "Notes du Salon",
                                    content = booking!!.notesSalon!!,
                                    icon = Icons.Outlined.Info,
                                    gradientColors = listOf(
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                )
                            }
                        }

                        // Bouton d'annulation
                        if (booking!!.canBeCancelled) {
                            item {
                                PremiumCancelButton(
                                    isCancelling = isCancelling,
                                    onClick = { showCancelDialog = true }
                                )
                            }
                        }

                        // Espace final
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmation d'annulation
    if (showCancelDialog) {
        StandardDialog(
            title = stringResource(Strings.BookingDetail.CancelBookingTitle),
            text = stringResource(Strings.BookingDetail.CancelConfirmMessage),
            onDismissRequest = { showCancelDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isCancelling = true
                                api.cancelBooking(bookingId)
                                booking = api.getBookingById(bookingId)
                                showCancelDialog = false
                            } catch (_: Exception) {
                                errorMessage = "Impossible d'annuler la réservation"
                            } finally {
                                isCancelling = false
                            }
                        }
                    },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (!isCancelling) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isCancelling) "Annulation..." else "Oui, annuler")
                }
            },
            dismissButton = {
                SecondaryButton(
                    text = stringResource(Strings.BookingDetail.CancelKeep),
                    onClick = { showCancelDialog = false },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}


@Composable
fun PremiumStatusCard(booking: BookingResponse) {
    val statusColor = Color(booking.status.getColor())
    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = AnimationSpecs.SoftTouchInteractionColor
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = animatedColor.copy(alpha = 0.05f)
                )
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emoji simple
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = animatedColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = booking.status.getEmoji(),
                        fontSize = 48.sp
                    )
                }

                // Statut textuel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = booking.status.getLocalizedDisplayName(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor
                    )
                    Text(
                        text = stringResource(Strings.BookingDetail.BookingStatus),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumSalonCard(
    booking: BookingResponse,
    onSalonClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        onClick = {
            onSalonClick()
            isPressed = true
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar du salon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Spa,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Informations
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Strings.BookingDetail.HairSalon),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = booking.salonName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Strings.BookingDetail.ClickForDetails),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Flèche
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumServiceCard(booking: BookingResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header avec icône
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = stringResource(Strings.BookingDetail.Service),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.BookingDetail.ServiceDetails),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informations du service
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumInfoRow(
                    icon = Icons.Outlined.ContentCut,
                    label = "PRESTATION",
                    value = booking.serviceName,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                PremiumInfoRow(
                    icon = Icons.Outlined.Category,
                    label = "CATÉGORIE",
                    value = booking.serviceCategory,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                PremiumInfoRow(
                    icon = Icons.Outlined.Schedule,
                    label = "DURÉE",
                    value = booking.formattedDuration,
                    iconColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun PremiumBookingCard(booking: BookingResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Column {
                    Text(
                        text = stringResource(Strings.BookingDetail.Booking),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.BookingDetail.BookingDetails),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informations
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumInfoRow(
                    icon = Icons.Outlined.CalendarToday,
                    label = "DATE & HEURE",
                    value = formatBookingDateTime(booking.bookingDatetime),
                    iconColor = MaterialTheme.colorScheme.tertiary
                )
                PremiumInfoRow(
                    icon = Icons.Outlined.Person,
                    label = "COIFFEUR",
                    value = booking.staffInfo,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                PremiumInfoRow(
                    icon = Icons.Outlined.AccessTime,
                    label = "DURÉE TOTALE",
                    value = booking.formattedDuration,
                    iconColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun PremiumPaymentCard(booking: BookingResponse) {
    val paymentStatusColor = when (booking.paymentStatus) {
        PaymentStatus.PAID -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.secondary
        PaymentStatus.REFUNDED -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = paymentStatusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Payments,
                        null,
                        modifier = Modifier.size(22.dp),
                        tint = paymentStatusColor
                    )
                }
                Column {
                    Text(
                        text = stringResource(Strings.BookingDetail.Payment),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.BookingDetail.PaymentDetails),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Montant et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Strings.BookingDetail.Amount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Euro,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = booking.formattedPrice ?: "Non défini",
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Badge de statut
                Surface(
                    color = paymentStatusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, paymentStatusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = booking.paymentBadge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = paymentStatusColor
                    )
                }
            }

            // Méthode de paiement
            booking.paymentMethod?.let { method ->
                Spacer(modifier = Modifier.height(20.dp))
                PremiumInfoRow(
                    icon = Icons.Outlined.CreditCard,
                    label = "MÉTHODE",
                    value = method,
                    iconColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PremiumNotesCard(
    title: String,
    content: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = gradientColors[0].copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.size(22.dp),
                        tint = gradientColors[0]
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenu
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = content,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun PremiumInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icône dans un cercle
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, iconColor.copy(alpha = 0.2f)),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }
        }

        // Texte
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PremiumCancelButton(
    isCancelling: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isCancelling,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (!isCancelling) {
            Icon(Icons.Outlined.Cancel, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(if (isCancelling) "Annulation en cours..." else "Annuler la réservation")
    }
}
