@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.RatingDisplay
import com.frollot.mobile.ui.components.ReviewCard
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.rememberImagePicker
import com.frollot.mobile.ui.components.toImageBitmap
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.localization.*
import com.frollot.mobile.localization.pluralizedString
import com.frollot.mobile.localization.formatLocalizedRating
import com.frollot.mobile.localization.*
import com.frollot.mobile.ui.utils.AnimationSpecs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran de détail d'un salon avec son catalogue de services.
 * Design Premium Ultra-Moderne avec animations et micro-interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonDetailScreen(
    salon: Salon,
    currentUser: User? = null,
    onBack: () -> Unit,
    onBookService: (SalonService) -> Unit = {},
    onCreateService: () -> Unit = {},
    onCreateStaff: () -> Unit = {},
    onManageQueue: () -> Unit,
    onManageBookings: () -> Unit = {},
    onViewPosts: () -> Unit = {}, // Phase C.2 - Feed par Salon
) {
    var services by remember { mutableStateOf<List<SalonService>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var staffMembers by remember { mutableStateOf<List<StaffMember>>(emptyList()) }
    var isStaffLoading by remember { mutableStateOf(false) }
    var staffError by remember { mutableStateOf<String?>(null) }

    var queueStatus by remember { mutableStateOf<QueueStatusResponse?>(null) }
    var queueError by remember { mutableStateOf<String?>(null) }
    var isQueueLoading by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var reviewStats by remember { mutableStateOf<SalonReviewStats?>(null) }
    var isReviewsLoading by remember { mutableStateOf(false) }
    var reviewsError by remember { mutableStateOf<String?>(null) }

    // États pour la photo de couverture
    var localSalon by remember { mutableStateOf(salon) }
    var selectedCoverPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var coverPhotoPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isUploadingCoverPhoto by remember { mutableStateOf(false) }
    var coverPhotoErrorMessage by remember { mutableStateOf<String?>(null) }

    // États pour le follow (Phase D.2)
    var isFollowing by remember { mutableStateOf(salon.isFollowedByCurrentUser ?: false) }
    var followersCount by remember { mutableStateOf(salon.followersCount ?: 0L) }
    var isTogglingFollow by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    
    // Synchroniser avec les données du salon
    LaunchedEffect(salon.isFollowedByCurrentUser, salon.followersCount) {
        isFollowing = salon.isFollowedByCurrentUser ?: false
        followersCount = salon.followersCount ?: 0L
    }

    // Image picker pour la photo de couverture
    val coverPhotoPicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            selectedCoverPhotoBytes = bytes
            coverPhotoPreview = bytes.toImageBitmap()
            if (coverPhotoPreview == null) {
                coverPhotoErrorMessage = "Erreur lors du chargement de l'image"
            }
        }
    }

    // Mettre à jour localSalon quand salon change
    LaunchedEffect(salon.id) {
        localSalon = salon
    }

    // Couleurs premium - Utilisation du thème Material Design 3
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    // Navigation par onglets
    var selectedTab by remember { mutableStateOf(SalonDetailTab.Services) }

    // ========== DEBUG OWNERSHIP CHECK ==========
    val isOwner = currentUser != null && currentUser.id == salon.ownerId
    LaunchedEffect(currentUser?.id, salon.ownerId) {
        FrollotLogger.debug("SalonDetail", "=".repeat(60))
        FrollotLogger.debug("API", "🔍 DEBUG OWNERSHIP CHECK - SalonDetailScreen")
        FrollotLogger.debug("SalonDetail", "=".repeat(60))
        FrollotLogger.debug("SalonDetail", "DEBUG: CurrentUser = ${if (currentUser == null) "NULL" else "NOT NULL"}")
        FrollotLogger.debug("SalonDetail", "DEBUG: CurrentUser ID = '${currentUser?.id}'")
        FrollotLogger.debug("SalonDetail", "DEBUG: Salon Owner ID = '${salon.ownerId}'")
        FrollotLogger.debug("SalonDetail", "DEBUG: Direct Match? = ${currentUser?.id == salon.ownerId}")
        FrollotLogger.debug("SalonDetail", "DEBUG: Trimmed Match? = ${currentUser?.id?.trim() == salon.ownerId.trim()}")
        FrollotLogger.debug("SalonDetail", "DEBUG: Lowercase Match? = ${currentUser?.id?.lowercase() == salon.ownerId.lowercase()}")
        FrollotLogger.debug("SalonDetail", "DEBUG: Length Match? = ${currentUser?.id?.length == salon.ownerId.length}")
        FrollotLogger.debug("SalonDetail", "DEBUG: Should Show FAB? = ${currentUser != null && currentUser.id == salon.ownerId}")
        FrollotLogger.debug("SalonDetail", "=".repeat(60))
    }

    // Charger les services au démarrage
    LaunchedEffect(salon.id) {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                FrollotLogger.debug("API", "🔍 Chargement des services pour le salon: ${salon.id}")
                services = api.getSalonServices(salon.id)
                FrollotLogger.success("API", "✅ ${services.size} services chargés")
                isLoading = false
            } catch (_: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement services")
                errorMessage = "Erreur lors du chargement des services"
                hasError = true
                isLoading = false
            }
        }
    }

    // Charger le staff au démarrage (uniquement pour le propriétaire)
    LaunchedEffect(salon.id, isOwner) {
        if (isOwner) {
            scope.launch {
                try {
                    isStaffLoading = true
                    staffError = null
                    FrollotLogger.debug("API", "🔍 Chargement du staff pour le salon: ${salon.id}")
                    staffMembers = api.getSalonStaff(salon.id)
                    FrollotLogger.success("API", "✅ ${staffMembers.size} membres du staff chargés")
                } catch (_: Exception) {
                    FrollotLogger.error("API", "❌ Erreur chargement staff")
                    staffError = "Erreur lors du chargement du staff"
                } finally {
                    isStaffLoading = false
                }
            }
        }
    }

    // Charger le statut de file d'attente
    LaunchedEffect(salon.id) {
        scope.launch {
            try {
                isQueueLoading = true
                queueError = null
                queueStatus = api.getQueueStatus(salon.id)
            } catch (_: Exception) {
                queueError = "Impossible de récupérer la file d'attente"
            } finally {
                isQueueLoading = false
            }
        }
    }

    // Charger les avis et statistiques
    LaunchedEffect(salon.id) {
        scope.launch {
            try {
                isReviewsLoading = true
                reviewsError = null
                reviews = api.getAllSalonReviews(salon.id)
                reviewStats = api.getSalonReviewStats(salon.id)
            } catch (_: Exception) {
                reviewsError = "Impossible de charger les avis"
            } finally {
                isReviewsLoading = false
            }
        }
    }

    // 🔄 POLLING AUTOMATIQUE CÔTÉ CLIENT (30 secondes) - CORRIGÉ
    LaunchedEffect(salon.id, currentUser?.id) {
        // Démarrer le polling seulement si l'utilisateur est un client
        if (currentUser == null || currentUser.userType != UserType.client) {
            return@LaunchedEffect
        }

        while (isActive) { // CORRECTION : Utiliser isActive au lieu de true
            try {
                delay(30_000) // ⏱️ 30 secondes

                // Rafraîchir silencieusement (sans spinner) seulement si déjà en file
                val userEntry = queueStatus?.entries?.firstOrNull { it.clientId == currentUser.id }
                if (userEntry != null) {
                    FrollotLogger.debug("Refresh", "🔄 [CLIENT POLLING] Rafraîchissement automatique position file...")
                    queueStatus = api.getQueueStatus(salon.id)
                    FrollotLogger.success("API", "✅ [CLIENT POLLING] Position mise à jour")
                }
            } catch (e: CancellationException) {
                // Ne rien faire, c'est normal lors de l'annulation
                throw e // Relancer l'exception d'annulation
            } catch (e: Exception) {
                FrollotLogger.warning("API", "⚠️ [CLIENT POLLING] Erreur silencieuse: ${e.message}")
                // Ne pas afficher d'erreur à l'utilisateur pour un polling automatique
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = salon.name,
                showAvatar = false
            )
        },
        floatingActionButton = {
            when {
                currentUser == null -> {
                    // FAB d'information
                    FloatingActionButton(
                        onClick = {
                            FrollotLogger.warning("SalonDetail", "🚨 DEBUG: FAB clicked but user is NULL")
                        },
                        containerColor = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            "Not logged in (DEBUG)",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                isOwner -> {
                    // FAB premium pour propriétaire
                    FloatingActionButton(
                        onClick = {
                            FrollotLogger.success("API", "✅ FAB clicked - Navigating to CreateService")
                            onCreateService()
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            stringResource(Strings.Common.Add),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                else -> {
                    // FAB d'information premium
                    FloatingActionButton(
                        onClick = {
                            FrollotLogger.warning("SalonDetail", "🚨 DEBUG: FAB clicked but user is NOT owner")
                            FrollotLogger.debug("SalonDetailScreen", "   User ID: '${currentUser.id}'")
                            FrollotLogger.debug("SalonDetailScreen", "   Owner ID: '${salon.ownerId}'")
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Not owner (DEBUG)",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER SIMPLIFIÉ (Style Instagram) ---
            item {
                SalonProfileHeader(
                    salon = localSalon,
                    servicesCount = services.size,
                    followersCount = followersCount,
                    reviewStats = reviewStats,
                    isFollowing = isFollowing,
                    isTogglingFollow = isTogglingFollow,
                    isOwner = isOwner,
                    currentUser = currentUser,
                    coverPhotoPreview = coverPhotoPreview,
                    isUploadingCoverPhoto = isUploadingCoverPhoto,
                    coverPhotoErrorMessage = coverPhotoErrorMessage,
                    onCoverPhotoSelected = { coverPhotoPicker.launch() },
                    onSaveCoverPhoto = {
                        selectedCoverPhotoBytes?.let { bytes ->
                            scope.launch {
                                try {
                                    isUploadingCoverPhoto = true
                                    coverPhotoErrorMessage = null
                                    val fileName = "salon_cover_${localSalon.id}_${Random.nextLong()}.jpg"
                                    val coverPhotoUrl = api.uploadImage(bytes, fileName)
                                    val updatedSalon = api.updateSalonCoverPhoto(localSalon.id, coverPhotoUrl)
                                    localSalon = updatedSalon
                                    selectedCoverPhotoBytes = null
                                    coverPhotoPreview = null
                                    isUploadingCoverPhoto = false
                                } catch (e: Exception) {
                                    coverPhotoErrorMessage = "Erreur lors de l'upload: ${e.message}"
                                    isUploadingCoverPhoto = false
                                }
                            }
                        }
                    },
                    onCancelCoverPhoto = {
                        selectedCoverPhotoBytes = null
                        coverPhotoPreview = null
                        coverPhotoErrorMessage = null
                    },
                    onFollowClick = {
                        scope.launch {
                            try {
                                isTogglingFollow = true
                                if (isFollowing) {
                                    api.unfollowSalon(salon.id)
                                    isFollowing = false
                                    followersCount = maxOf(0, followersCount - 1)
                                } else {
                                    api.followSalon(salon.id)
                                    isFollowing = true
                                    followersCount++
                                }
                            } catch (e: Exception) {
                                FrollotLogger.error("API", "❌ Erreur follow/unfollow: ${e.message}")
                            } finally {
                                isTogglingFollow = false
                            }
                        }
                    },
                    onBookClick = {
                        if (services.isNotEmpty()) {
                            onBookService(services.first())
                        }
                    }
                )
            }

            // --- NAVIGATION PAR ONGLETS ---
            item {
                SalonDetailTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    showPosts = true, // TODO: Vérifier si le salon a des posts
                    showStaff = isOwner,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- FILE D'ATTENTE COMPACTE (Info seulement) ---
            item {
                val userEntry = queueStatus?.entries?.firstOrNull { it.clientId == currentUser?.id }
                if (queueStatus != null || userEntry != null) {
                    SalonQueueInfoCompact(
                        queueStatus = queueStatus,
                        userEntry = userEntry,
                        isLoading = isQueueLoading || isJoining,
                        errorMessage = queueError,
                        currentUser = currentUser,
                        onJoin = {
                            if (currentUser == null) {
                                queueError = "Connectez-vous pour rejoindre la file"
                                return@SalonQueueInfoCompact
                            }
                            if (currentUser.userType != UserType.client) {
                                queueError = "Seuls les clients peuvent rejoindre la file d'attente"
                                return@SalonQueueInfoCompact
                            }
                            scope.launch {
                                try {
                                    isJoining = true
                                    queueError = null
                                    val request = JoinQueueRequest(
                                        salonId = salon.id,
                                        userId = currentUser.id,
                                        serviceId = null,
                                        requestedDurationMinutes = null,
                                        notes = null
                                    )
                                    api.joinQueue(request)
                                    queueStatus = api.getQueueStatus(salon.id)
                                } catch (_: Exception) {
                                    queueError = "Impossible de rejoindre la file"
                                } finally {
                                    isJoining = false
                                }
                            }
                        },
                        onLeave = {
                            userEntry ?: return@SalonQueueInfoCompact
                            scope.launch {
                                try {
                                    isJoining = true
                                    queueError = null
                                    val request = LeaveQueueRequest(
                                        entryId = userEntry.entryId,
                                        userId = currentUser?.id
                                    )
                                    api.leaveQueue(request, salon.id)
                                    queueStatus = api.getQueueStatus(salon.id)
                                } catch (_: Exception) {
                                    queueError = "Impossible de quitter la file"
                                } finally {
                                    isJoining = false
                                }
                            }
                        },
                        onRefresh = {
                            scope.launch {
                                try {
                                    isQueueLoading = true
                                    queueError = null
                                    queueStatus = api.getQueueStatus(salon.id)
                                } catch (_: Exception) {
                                    queueError = "Impossible de rafraîchir la file"
                                } finally {
                                    isQueueLoading = false
                                }
                            }
                        }
                    )
                }
            }

            // --- BOUTONS DE GESTION (PROPRIÉTAIRE - Onglet Staff) ---
            // Déplacé dans l'onglet Staff

            // --- CONTENU SELON L'ONGLET ---
            when (selectedTab) {
                SalonDetailTab.Services -> {
                    // Header Services
                    item {
                        Text(
                            text = stringResource(Strings.SalonDetail.Services),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Contenu Services
                    when {
                        isLoading -> {
                            item {
                                ListLoadingState(
                                    message = "Chargement des services...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        hasError -> {
                            item {
                                ListErrorState(
                                    message = errorMessage ?: "Impossible de charger les services",
                                    onRetry = {
                                        scope.launch {
                                            isLoading = true
                                            hasError = false
                                            try {
                                                services = api.getSalonServices(salon.id)
                                                isLoading = false
                                            } catch (_: Exception) {
                                                errorMessage = "Erreur lors du rechargement"
                                                hasError = true
                                                isLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        services.isEmpty() -> {
                            item {
                                ListEmptyState(
                                    title = "Aucun service disponible",
                                    message = if (isOwner) "Créez votre premier service pour commencer" else "Ce salon n'a pas encore de services",
                                    icon = {
                                        Icon(
                                            Icons.Outlined.Spa,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        else -> {
                            items(services, key = { it.id }) { service ->
                                SalonDetailServiceCard(
                                    service = service,
                                    primaryColor = primaryColor,
                                    onBookClick = { onBookService(service) }
                                )
                            }
                        }
                    }
                }

                SalonDetailTab.Posts -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Icon(
                                Icons.Outlined.Article,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(Strings.SalonDetail.ViewPosts),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            PrimaryButton(
                                text = stringResource(Strings.SalonDetail.OpenFeed),
                                onClick = onViewPosts,
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

                SalonDetailTab.Reviews -> {
                    // Stats en haut
                    reviewStats?.let { stats ->
                        item {
                            StandardCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = formatLocalizedRating(stats.averageRating),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${stats.totalReviews} avis",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        RatingDisplay(
                                            rating = stats.averageRating,
                                            totalReviews = stats.totalReviews,
                                            modifier = Modifier.fillMaxWidth(0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Liste des avis
                    when {
                        isReviewsLoading -> {
                            item {
                                ListLoadingState(
                                    message = "Chargement des avis...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        reviewsError != null -> {
                            item {
                                ListErrorState(
                                    message = reviewsError ?: "Erreur inconnue",
                                    onRetry = {
                                        scope.launch {
                                            try {
                                                isReviewsLoading = true
                                                reviewsError = null
                                                reviews = api.getAllSalonReviews(salon.id)
                                                reviewStats = api.getSalonReviewStats(salon.id)
                                            } catch (_: Exception) {
                                                reviewsError = "Erreur lors du rafraîchissement"
                                            } finally {
                                                isReviewsLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        reviews.isEmpty() -> {
                            item {
                                ListEmptyState(
                                    title = "Aucun avis",
                                    message = "Soyez le premier à laisser un avis",
                                    icon = {
                                        Icon(
                                            Icons.Outlined.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        else -> {
                            items(reviews, key = { it.id }) { review ->
                                ReviewCard(
                                    review = review,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                SalonDetailTab.Staff -> {
                    if (isOwner) {
                        // Header avec bouton ajouter
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Strings.SalonDetail.Team),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                FloatingActionButton(
                                    onClick = onCreateStaff,
                                    modifier = Modifier.size(40.dp),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Ajouter un membre",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Liste du staff
                        when {
                            isStaffLoading -> {
                                item {
                                    ListLoadingState(
                                        message = "Chargement de l'équipe...",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            staffError != null -> {
                                item {
                                ListErrorState(
                                    message = staffError ?: "Erreur inconnue",
                                        onRetry = {
                                            scope.launch {
                                                try {
                                                    isStaffLoading = true
                                                    staffError = null
                                                    staffMembers = api.getSalonStaff(salon.id)
                                                } catch (_: Exception) {
                                                    staffError = "Erreur lors du rafraîchissement"
                                                } finally {
                                                    isStaffLoading = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            staffMembers.isEmpty() -> {
                                item {
                                    ListEmptyState(
                                        title = "Aucun membre",
                                        message = "Ajoutez des membres à votre équipe",
                                        icon = {
                                            Icon(
                                                Icons.Outlined.People,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        },
                                        action = {
                                            PrimaryButton(
                                                text = stringResource(Strings.SalonDetail.AddMember),
                                                onClick = onCreateStaff,
                                                icon = {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            else -> {
                                items(staffMembers, key = { it.id }) { staff ->
                                    StandardCard(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = staff.fullName.firstOrNull()?.uppercase() ?: "?",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = staff.fullName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (staff.specialtyLabels.isNotEmpty()) {
                                                    Text(
                                                        text = staff.specialtyLabels.joinToString(", "),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            ListEmptyState(
                                title = "Accès restreint",
                                message = "Seul le propriétaire peut voir les membres du staff",
                                icon = {
                                    Icon(
                                        Icons.Outlined.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // --- ESPACE FINAL ---
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Enum pour les onglets de navigation
 */
enum class SalonDetailTab(val label: String) {
    Services("Services"),
    Posts("Posts"),
    Reviews("Avis"),
    Staff("Équipe")
}

/**
 * Navigation par onglets
 */
@Composable
fun SalonDetailTabs(
    selectedTab: SalonDetailTab,
    onTabSelected: (SalonDetailTab) -> Unit,
    showPosts: Boolean,
    showStaff: Boolean,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        buildList {
            add(SalonDetailTab.Services)
            if (showPosts) add(SalonDetailTab.Posts)
            add(SalonDetailTab.Reviews)
            if (showStaff) add(SalonDetailTab.Staff)
        }
    }

    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                color = MaterialTheme.colorScheme.primary,
                height = 3.dp
            )
        },
        divider = {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

/**
 * Info compacte pour la file d'attente (remplace la grosse carte)
 */
@Composable
fun SalonQueueInfoCompact(
                    queueStatus: QueueStatusResponse?,
                    userEntry: QueueEntryResponse?,
                    isLoading: Boolean,
                    errorMessage: String?,
                    currentUser: User?,
                    onJoin: () -> Unit,
                    onLeave: () -> Unit,
                    onRefresh: () -> Unit
                ) {
                    val isUserInQueue = userEntry != null
                    val queueSize = queueStatus?.entries?.size ?: 0

                    StandardCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Queue,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = stringResource(Strings.SalonDetail.Queue),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (queueSize > 0) "$queueSize personne${if (queueSize > 1) "s" else ""} en attente" else "Aucune personne en attente",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isUserInQueue) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = stringResource(Strings.SalonDetail.Position)
                                                .replace("{position}", userEntry?.position?.toString() ?: "-"),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            if (isUserInQueue) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                                SecondaryButton(
                                    text = stringResource(Strings.SalonDetail.LeaveQueue),
                                    onClick = onLeave,
                                    icon = {
                                        Icon(
                                            Icons.Default.Logout,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else if (currentUser != null && currentUser.userType == UserType.client) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                                PrimaryButton(
                                    text = stringResource(Strings.SalonDetail.JoinQueue),
                                    onClick = onJoin,
                                    icon = {
                                        Icon(
                                            Icons.Default.DirectionsWalk,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else if (currentUser == null) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                                Text(
                                    text = stringResource(Strings.SalonDetail.LoginToJoinQueue),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            errorMessage?.let { error ->
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

/**
 * Header simplifié style Instagram pour SalonDetailScreen
 * Refonte complète - Phase 1
 */
@Composable
fun SalonProfileHeader(
    salon: Salon,
    servicesCount: Int,
    followersCount: Long,
    reviewStats: SalonReviewStats?,
    isFollowing: Boolean,
    isTogglingFollow: Boolean,
    isOwner: Boolean,
    currentUser: User?,
    coverPhotoPreview: ImageBitmap?,
    isUploadingCoverPhoto: Boolean,
    coverPhotoErrorMessage: String?,
    onCoverPhotoSelected: () -> Unit,
    onSaveCoverPhoto: () -> Unit,
    onCancelCoverPhoto: () -> Unit,
    onFollowClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Cover Photo - Pleine largeur (pas de Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            when {
                coverPhotoPreview != null -> {
                    androidx.compose.foundation.Image(
                        bitmap = coverPhotoPreview,
                        contentDescription = "Aperçu photo de couverture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                salon.coverPhotoUrl != null && salon.coverPhotoUrl.isNotBlank() -> {
                    AsyncImage(
                        model = salon.coverPhotoUrl,
                        contentDescription = "Photo de couverture de ${salon.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                    )
                }
            }

            // Bouton pour modifier la photo (propriétaire seulement)
            if (isOwner) {
                if (coverPhotoPreview != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryButton(
                            text = stringResource(Strings.SalonDetail.Cancel),
                            onClick = onCancelCoverPhoto,
                            enabled = !isUploadingCoverPhoto,
                            icon = {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                            }
                        )
                        PrimaryButton(
                            text = if (isUploadingCoverPhoto) "..." else "Enregistrer",
                            onClick = onSaveCoverPhoto,
                            enabled = !isUploadingCoverPhoto,
                            icon = if (!isUploadingCoverPhoto) {
                                {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                }
                            } else null
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = onCoverPhotoSelected,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            "Modifier la photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Contenu principal - Padding horizontal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar + Nom + Vérification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar - Taille réduite pour meilleur équilibre
                    Box(
                        modifier = Modifier
                            .size(64.dp)
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
                            text = salon.name.firstOrNull()?.uppercase() ?: "S",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = salon.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White // Toujours blanc, quel que soit le thème
                            )
                            if (salon.isVerified) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = stringResource(Strings.SalonDetail.SalonVerified),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${salon.city}, ${salon.postalCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats en ligne (Services | Followers | Note)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Services
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$servicesCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.SalonDetail.Services),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Followers
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$followersCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.SalonDetail.Subscribers),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Note
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = reviewStats?.averageRating?.let { formatLocalizedRating(it) } ?: "-",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = reviewStats?.totalReviews?.let { "$it avis" } ?: "Aucun avis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            salon.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Actions principales
            if (currentUser != null && !isOwner) {
                // Client : Follow + Réserver
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        text = if (isTogglingFollow) "..." else if (isFollowing) "Ne plus suivre" else "Suivre",
                        onClick = onFollowClick,
                        enabled = !isTogglingFollow,
                        modifier = Modifier.weight(1f),
                        icon = if (!isTogglingFollow) {
                            {
                                Icon(
                                    if (isFollowing) Icons.Default.Check else Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                    PrimaryButton(
                        text = stringResource(Strings.SalonDetail.Book),
                        onClick = onBookClick,
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            } else if (isOwner) {
                // Propriétaire : Actions de gestion (seront dans les tabs)
                // Pas d'actions ici pour l'instant
            }

            // Message d'erreur cover photo
            coverPhotoErrorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    }
                }
            }
        }
    }
}

/**
 * Header du salon premium pour SalonDetailScreen
 * @deprecated Remplacé par SalonProfileHeader - À supprimer après migration complète
 */
@Composable
fun SalonDetailHeader(
    salon: Salon,
    primaryColor: Color,
    secondaryColor: Color,
    isOwner: Boolean = false,
    coverPhotoPreview: ImageBitmap? = null,
    isUploadingCoverPhoto: Boolean = false,
    onCoverPhotoSelected: () -> Unit = {},
    onSaveCoverPhoto: () -> Unit = {},
    onCancelCoverPhoto: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Photo de couverture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                when {
                    coverPhotoPreview != null -> {
                        // Aperçu de la nouvelle photo
                        androidx.compose.foundation.Image(
                            bitmap = coverPhotoPreview,
                            contentDescription = "Aperçu photo de couverture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    salon.coverPhotoUrl != null && salon.coverPhotoUrl.isNotBlank() -> {
                        // Photo existante - charger l'image depuis l'URL
                        AsyncImage(
                            model = salon.coverPhotoUrl,
                            contentDescription = "Photo de couverture de ${salon.name}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    else -> {
                        // Pas de photo - gradient par défaut
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, secondaryColor)
                                    )
                                )
                        )
                    }
                }

                // Overlay gradient pour la lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                // Bouton pour modifier la photo (propriétaire seulement)
                if (isOwner) {
                    if (coverPhotoPreview != null) {
                        // Mode édition : boutons sauvegarder/annuler
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCancelCoverPhoto,
                                enabled = !isUploadingCoverPhoto,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Strings.Common.Cancel))
                            }
                            Button(
                                onClick = onSaveCoverPhoto,
                                enabled = !isUploadingCoverPhoto,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = primaryColor
                                )
                            ) {
                                if (isUploadingCoverPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = primaryColor
                                    )
                                } else {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(Strings.Common.Save))
                                }
                            }
                        }
                    } else {
                        // Bouton pour sélectionner une photo
                        FloatingActionButton(
                            onClick = onCoverPhotoSelected,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = primaryColor
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                "Modifier la photo",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
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
                        // Logo salon avec gradient
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, secondaryColor)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = salon.name.firstOrNull()?.uppercase() ?: "S",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White // Toujours blanc sur gradient coloré
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = salon.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White // Toujours blanc, quel que soit le thème
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(Strings.SalonDetail.SalonVerified),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Badge premium
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "⭐ 4.8",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Informations détaillées
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Adresse
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = salon.address,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${salon.postalCode} ${salon.city}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Description
                    salon.description?.let { description ->
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Carte de file d'attente premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailQueueCard(
        salon: Salon,
        queueStatus: QueueStatusResponse?,
        userEntry: QueueEntryResponse?,
        isLoading: Boolean,
        errorMessage: String?,
        currentUser: User?,
        primaryColor: Color,
        onRefresh: () -> Unit,
        onJoin: () -> Unit,
        onLeave: () -> Unit
    ) {
        val isUserInQueue = userEntry != null
        val scale by animateFloatAsState(
            targetValue = if (isUserInQueue) 1.02f else 1f,
            animationSpec = AnimationSpecs.SoftTouchInteraction,
            label = "queue_scale"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(
                    elevation = if (isUserInQueue) 12.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (isUserInQueue) primaryColor else MaterialTheme.colorScheme.scrim
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUserInQueue) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                if (isUserInQueue) 2.dp else 1.dp,
                if (isUserInQueue) primaryColor else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🚶",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Strings.SalonDetail.Queue),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isUserInQueue) "Vous êtes en file" else "Statut en temps réel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Bouton refresh
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Rafraîchir",
                                tint = primaryColor
                            )
                        }
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = primaryColor,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Mise à jour en cours...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            OutlinedButton(
                                onClick = onRefresh,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                )
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(Strings.Common.Retry))
                            }
                        }
                    }

                    else -> {
                        val total = queueStatus?.activeSize ?: 0
                        val estimate =
                            userEntry?.estimatedWaitMinutes ?: queueStatus?.estimatedWaitForNew ?: 0

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Clients
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = total.toString(),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = stringResource(Strings.SalonDetail.Clients),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Temps d'attente
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$estimate min",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = stringResource(Strings.SalonDetail.Waiting),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Position utilisateur
                        if (isUserInQueue) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = primaryColor.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userEntry!!.position.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(Strings.SalonDetail.YourPosition),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(Strings.SalonDetail.PositionInQueue)
                                                .replace("{position}", userEntry.position.toString()),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${userEntry.estimatedWaitMinutes} min",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }
                        }

                        // Boutons d'action
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentUser != null && currentUser.userType == UserType.client) {
                                if (!isUserInQueue) {
                                    PrimaryButton(
                                        text = stringResource(Strings.SalonDetail.JoinQueue),
                                        onClick = onJoin,
                                        icon = {
                                            Icon(
                                                Icons.Default.DirectionsWalk,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    SecondaryButton(
                                        text = stringResource(Strings.SalonDetail.LeaveQueue),
                                        onClick = onLeave,
                                        icon = {
                                            Icon(
                                                Icons.Default.Logout,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else if (currentUser != null && currentUser.userType != UserType.client) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "File d'attente réservée aux clients",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Login,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Connectez-vous pour rejoindre la file",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

/**
 * Section staff premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailStaffSection(
        staffMembers: List<StaffMember>,
        isLoading: Boolean,
        error: String?,
        primaryColor: Color,
        onAddStaff: () -> Unit,
        onRefresh: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Strings.SalonDetail.MyTeam),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${staffMembers.size} membre${if (staffMembers.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Rafraîchir",
                                tint = primaryColor
                            )
                        }
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    error != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    staffMembers.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Aucun membre dans l'équipe",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Ajoutez des coiffeurs pour permettre les réservations",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            staffMembers.forEach { member ->
                                SalonDetailStaffCard(member = member, primaryColor = primaryColor)
                            }
                        }
                    }
                }

                // Bouton ajouter
                OutlinedButton(
                    onClick = onAddStaff,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ajouter un membre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

/**
 * Carte staff premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailStaffCard(member: StaffMember, primaryColor: Color) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(primaryColor, MaterialTheme.colorScheme.secondary)
                                )
                            ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.userFirstName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Informations
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = member.specialtiesText.ifEmpty { "Toutes prestations" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Statut
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (member.isActive) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (member.isActive) "✅" else "⏸️",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = if (member.isActive) "Actif" else "Inactif",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (member.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

/**
 * Header services premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailServicesHeader(servicesCount: Int, primaryColor: Color) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Strings.SalonDetail.OurServices),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (servicesCount > 0) {
                        Text(
                            text = pluralizedString(
                                Strings.SalonDetail.ChooseFromServices,
                                servicesCount,
                                replaceCount = true
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (servicesCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = servicesCount.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }

/**
 * État de chargement premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailLoadingState(primaryColor: Color) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = primaryColor
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(Strings.SalonDetail.LoadingServices),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.SalonDetail.PleaseWait),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

/**
 * Carte de service premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailServiceCard(
        service: SalonService,
        primaryColor: Color,
        onBookClick: () -> Unit
    ) {
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = AnimationSpecs.SoftTouchInteraction,
            label = "service_scale"
        )

        Card(
            onClick = onBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(primaryColor, MaterialTheme.colorScheme.secondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = service.categoryEmoji,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = service.categoryLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Prix
                    Text(
                        text = service.formattedPrice,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                // Description
                service.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                        )
                    }
                }

                // Footer avec durée et bouton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Durée
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = service.formattedDuration,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Bouton réserver
                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Réserver",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

/**
 * Carte d'erreur premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailErrorCard(
        message: String,
        primaryColor: Color,
        onRetry: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Strings.SalonDetail.LoadingError),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Réessayer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

/**
 * État vide premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailEmptyState(primaryColor: Color) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Strings.SalonDetail.NoServicesAvailable),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.SalonDetail.NoServicesMessage),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

/**
 * Barre sticky de réservation premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailStickyBookingBar(
        servicesCount: Int,
        onBookClick: () -> Unit,
        primaryColor: Color
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    spotColor = primaryColor
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Strings.SalonDetail.ReadyToBook),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$servicesCount service${if (servicesCount > 1) "s" else ""} disponible${if (servicesCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onBookClick,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Réserver",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

/**
 * Section avis premium pour SalonDetailScreen
 */
@Composable
fun SalonDetailReviewsSection(
        reviews: List<Review>,
        reviewStats: SalonReviewStats?,
        isLoading: Boolean,
        error: String?,
        primaryColor: Color,
        onRefresh: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⭐",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Strings.SalonDetail.ReviewsAndRatings),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            reviewStats?.let { stats ->
                                Text(
                                    text = "${stats.totalReviews} avis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Rafraîchir",
                                tint = primaryColor
                            )
                        }
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    error != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    reviews.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⭐",
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Aucun avis pour le moment",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Soyez le premier à laisser un avis !",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        // Statistiques
                        reviewStats?.let { stats ->
                            RatingDisplay(
                                rating = stats.averageRating,
                                totalReviews = stats.totalReviews,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Liste des avis
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            reviews.take(5).forEach { review ->
                                ReviewCard(
                                    review = review,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
