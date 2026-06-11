package com.frollot.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.rememberImagePicker
import com.frollot.mobile.ui.components.toImageBitmap
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.dialogs.StandardDialog
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Écran de profil utilisateur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User,
    onMenuClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onUserUpdated: (User) -> Unit = {}
) {
    // State local synchronisé avec currentUser pour forcer la recomposition
    var localUser by remember(currentUser.id) { mutableStateOf(currentUser) }
    
    // Synchroniser localUser avec currentUser quand il change
    LaunchedEffect(currentUser.id, currentUser.avatarUrl) {
        localUser = currentUser
    }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedAvatarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var avatarPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var avatarErrorMessage by remember { mutableStateOf<String?>(null) }

    // Statistiques dynamiques
    var statsStat1 by remember { mutableStateOf(0) }
    var statsStat2 by remember { mutableStateOf(0) }
    var statsStat3 by remember { mutableStateOf(0) }
    var isLoadingStats by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    // Charger les statistiques
    LaunchedEffect(localUser.id, localUser.userType) {
        scope.launch {
            try {
                isLoadingStats = true
                when (localUser.userType) {
                    UserType.client -> {
                        // Pour un client : Réservations, Avis, Points
                        val bookings = api.getClientBookings(localUser.id)
                        val reviews = api.getClientReviews(localUser.id)
                        statsStat1 = bookings.size
                        statsStat2 = reviews.size
                        // Points = réservations complétées + avis laissés
                        statsStat3 = bookings.count { it.status.name == "completed" } + reviews.size
                    }
                    UserType.salon_owner -> {
                        // Pour un propriétaire : Salons, Services, Points
                        val allSalons = api.getAllSalons()
                        val ownerSalons = allSalons.filter { it.ownerId == localUser.id }
                        var totalServices = 0
                        ownerSalons.forEach { salon ->
                            try {
                                val services = api.getSalonServices(salon.id)
                                totalServices += services.size
                            } catch (e: Exception) {
                                // Ignorer les erreurs pour un salon spécifique
                            }
                        }
                        statsStat1 = ownerSalons.size
                        statsStat2 = totalServices
                        // Points = nombre de salons * 10 + nombre de services
                        statsStat3 = ownerSalons.size * 10 + totalServices
                    }
                    UserType.hairstylist -> {
                        // Pour un coiffeur : Réservations, Services (via staff), Points
                        // Note: Il faudrait récupérer le staffId depuis l'utilisateur
                        // Pour l'instant, on essaie de récupérer les réservations via l'ID utilisateur
                        try {
                            val bookings = api.getStaffBookings(localUser.id)
                            statsStat1 = bookings.size
                        } catch (e: Exception) {
                            statsStat1 = 0
                        }
                        // Services : difficile à calculer sans connaître les salons où il travaille
                        statsStat2 = 0
                        statsStat3 = statsStat1 * 5 // Points basés sur les réservations
                    }
                    else -> {
                        statsStat1 = 0
                        statsStat2 = 0
                        statsStat3 = 0
                    }
                }
            } catch (e: Exception) {
                // En cas d'erreur, garder les valeurs par défaut (0)
                statsStat1 = 0
                statsStat2 = 0
                statsStat3 = 0
            } finally {
                isLoadingStats = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = localUser,
                onMenuClick = onMenuClick,
                onNavigateToProfile = { /* Déjà sur le profil */ },
                title = "Mon Profil",
                actions = {
                    IconButton(onClick = { /* TODO: Paramètres */ }) {
                        Icon(Icons.Default.Settings, "Paramètres")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Avatar et Informations principales
            ProfileHeaderWithPicker(
                user = localUser,
                avatarPreview = avatarPreview,
                isUploading = isUploadingAvatar,
                onAvatarSelected = { bytes ->
                    selectedAvatarBytes = bytes
                    avatarPreview = bytes.toImageBitmap()
                    if (avatarPreview == null) {
                        avatarErrorMessage = "Erreur lors du chargement de l'image"
                    }
                },
                onSaveAvatar = {
                    selectedAvatarBytes?.let { bytes ->
                        scope.launch {
                            try {
                                isUploadingAvatar = true
                                avatarErrorMessage = null
                                val fileName = "avatar_${localUser.id}_${Random.nextLong()}.jpg"
                                val avatarUrl = api.uploadImage(bytes, fileName)
                                val updatedUser = api.updateUserAvatar(localUser.id, avatarUrl)
                                localUser = updatedUser // Mettre à jour le state local immédiatement
                                onUserUpdated(updatedUser)
                                selectedAvatarBytes = null
                                avatarPreview = null
                            } catch (e: Exception) {
                                avatarErrorMessage = "Erreur lors de l'upload: ${e.message}"
                            } finally {
                                isUploadingAvatar = false
                            }
                        }
                    }
                }
            )

            // Message d'erreur avatar
            avatarErrorMessage?.let { error ->
                StandardCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Section Informations de compte
            AccountInfoSection(localUser)

            // Section Type de compte
            AccountTypeSection(localUser)

            // Section Actions
            ActionsSection(
                onEditProfile = { /* TODO: Navigation vers édition */ },
                onChangePassword = { /* TODO: Navigation vers changement de mot de passe */ },
                onLogout = { showLogoutDialog = true }
            )

            // Section Statistiques
            StatsSection(
                user = localUser,
                stat1 = statsStat1,
                stat2 = statsStat2,
                stat3 = statsStat3,
                isLoading = isLoadingStats
            )

            // Version de l'app
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Strings.Profile.Version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Dialog de confirmation de déconnexion
        if (showLogoutDialog) {
            StandardDialog(
                title = stringResource(Strings.Profile.LogoutDialogTitle),
                text = stringResource(Strings.Profile.LogoutDialogText),
                onDismissRequest = { showLogoutDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text(stringResource(Strings.Profile.LogoutConfirm), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(Strings.Profile.Cancel))
                    }
                }
            )
        }
    }
}

/**
 * En-tête du profil avec avatar et picker intégré.
 */
@Composable
fun ProfileHeaderWithPicker(
    user: User,
    avatarPreview: ImageBitmap?,
    isUploading: Boolean,
    onAvatarSelected: (ByteArray) -> Unit,
    onSaveAvatar: () -> Unit
) {
    // Le picker est créé ici, dans le contexte Composable
    val avatarPicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            onAvatarSelected(bytes)
        }
    }

    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar avec possibilité de modification
            Box {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { avatarPicker.launch() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            avatarPreview != null -> {
                                Image(
                                    bitmap = avatarPreview,
                                    contentDescription = stringResource(Strings.Profile.AvatarPreview),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                UserAvatar(
                                    user = user,
                                    size = 100.dp,
                                    showBorder = false
                                )
                            }
                        }
                    }
                }

                // Badge "Modifier" en bas à droite
                if (avatarPreview != null) {
                    FloatingActionButton(
                        onClick = onSaveAvatar,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                stringResource(Strings.Profile.Save),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                } else {
                    // Icône "Modifier" en overlay
                    Surface(
                        onClick = { avatarPicker.launch() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                stringResource(Strings.Profile.EditPhoto),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Nom complet
            Text(
                text = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                    .ifBlank { user.email },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Badge de statut
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (user.isVerified) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (user.isVerified) Icons.Default.Verified else Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (user.isVerified) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = if (user.isVerified) stringResource(Strings.Profile.AccountVerified) else stringResource(Strings.Profile.AccountNotVerified),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Section d'informations du compte.
 */
@Composable
fun AccountInfoSection(user: User) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Strings.Profile.AccountInfo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            // Email
            InfoRow(
                icon = Icons.Default.Email,
                label = stringResource(Strings.Profile.Email),
                value = user.email
            )

            // Téléphone (si disponible)
            user.phoneNumber?.let { phone ->
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = stringResource(Strings.Profile.Phone),
                    value = phone
                )
            }

            // Date de création
            user.createdAt?.let { date ->
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(Strings.Profile.MemberSince),
                    value = formatDate(date)
                )
            }
        }
    }
}

/**
 * Section du type de compte.
 */
@Composable
fun AccountTypeSection(user: User) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (user.userType) {
                    UserType.salon_owner -> Icons.Default.Store
                    UserType.hairstylist -> Icons.Default.ContentCut
                    UserType.admin -> Icons.Default.AdminPanelSettings
                    else -> Icons.Default.Person
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Strings.Profile.AccountType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (user.userType) {
                        UserType.client -> stringResource(Strings.Profile.UserTypes.Client)
                        UserType.salon_owner -> stringResource(Strings.Profile.UserTypes.SalonOwner)
                        UserType.hairstylist -> stringResource(Strings.Profile.UserTypes.Hairstylist)
                        UserType.admin -> stringResource(Strings.Profile.UserTypes.Admin)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Section des actions disponibles.
 */
@Composable
fun ActionsSection(
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            ActionItem(
                icon = Icons.Default.Edit,
                title = stringResource(Strings.Profile.EditProfile),
                subtitle = stringResource(Strings.Profile.EditProfileSubtitle),
                onClick = onEditProfile
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            ActionItem(
                icon = Icons.Default.Lock,
                title = stringResource(Strings.Profile.ChangePassword),
                subtitle = stringResource(Strings.Profile.ChangePasswordSubtitle),
                onClick = onChangePassword
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            ActionItem(
                icon = Icons.Default.Logout,
                title = stringResource(Strings.Profile.Logout),
                subtitle = stringResource(Strings.Profile.LogoutSubtitle),
                onClick = onLogout,
                isDestructive = true
            )
        }
    }
}

/**
 * Section des statistiques.
 */
@Composable
fun StatsSection(
    user: User,
    stat1: Int,
    stat2: Int,
    stat3: Int,
    isLoading: Boolean
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Strings.Profile.Statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = stat1.toString(),
                        label = when (user.userType) {
                            UserType.salon_owner -> stringResource(Strings.Profile.Salons)
                            UserType.hairstylist -> stringResource(Strings.Profile.Bookings)
                            else -> stringResource(Strings.Profile.Bookings)
                        }
                    )
                    StatItem(
                        value = stat2.toString(),
                        label = when (user.userType) {
                            UserType.client -> stringResource(Strings.Profile.Reviews)
                            UserType.salon_owner -> stringResource(Strings.Profile.Services)
                            UserType.hairstylist -> stringResource(Strings.Profile.Services)
                            else -> stringResource(Strings.Profile.Services)
                        }
                    )
                    StatItem(
                        value = stat3.toString(),
                        label = stringResource(Strings.Profile.Points)
                    )
                }
            }
        }
    }
}

/**
 * Composant pour afficher une ligne d'information.
 */
@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Composant pour une action cliquable.
 */
@Composable
fun ActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Composant pour afficher une statistique.
 */
@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Formate une date ISO-8601 en format localisé.
 * 
 * Phase 4 - Fonctionnalité Langue : Utilise le formatage localisé.
 * 
 * @param isoDate Date au format ISO-8601
 * @return Date formatée selon la langue courante
 */
@Composable
fun formatDate(isoDate: String): String {
    return com.frollot.mobile.localization.formatLocalizedDate(isoDate)
}
