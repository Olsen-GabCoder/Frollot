package com.frollot.mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.Salon
import com.frollot.mobile.model.ServiceCategory
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.theme.*
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger

// ========================================
// ECRAN ACCUEIL — Design System v2
// Editorial premium chaleureux
// ========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User? = null,
    onMenuClick: () -> Unit = {},
    onNavigateToCreateSalon: () -> Unit = {},
    onNavigateToSalonDetail: (String) -> Unit = {},
    onNavigateToMyBookings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var salons by remember { mutableStateOf<List<Salon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var isCityDialogOpen by remember { mutableStateOf(false) }
    var isNearbyFilterActive by remember { mutableStateOf(false) }
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    val scrollState = rememberLazyListState()

    fun refreshSalons() {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                salons = if (isNearbyFilterActive && userLatitude != null && userLongitude != null) {
                    api.getSalonsNearby(
                        latitude = userLatitude!!,
                        longitude = userLongitude!!,
                        radiusKm = 10.0
                    )
                } else {
                    api.getSalons(
                        query = searchQuery.takeIf { it.isNotBlank() },
                        city = selectedCity?.takeIf { it.isNotBlank() },
                        category = selectedCategory
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "Erreur chargement salons: ${e.message}")
                hasError = true
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery, selectedCity, selectedCategory, isNearbyFilterActive, userLatitude, userLongitude) {
        delay(500)
        refreshSalons()
    }

    LaunchedEffect(Unit) {
        refreshSalons()
    }

    val hasActiveFilters by remember {
        derivedStateOf {
            searchQuery.isNotBlank() || !selectedCity.isNullOrBlank() || selectedCategory != null || isNearbyFilterActive
        }
    }

    LaunchedEffect(Unit) {
        if (userLatitude == null && userLongitude == null) {
            userLatitude = 48.8566
            userLongitude = 2.3522
        }
    }

    // Categories avec icones
    val categories = remember {
        listOf(
            Triple(Icons.Default.ContentCut, "Coupe", ServiceCategory.COUPE),
            Triple(Icons.Default.Palette, "Coloration", ServiceCategory.COLORATION),
            Triple(Icons.Default.Spa, "Soin", ServiceCategory.SOIN),
            Triple(Icons.Default.Face, "Barbe", ServiceCategory.BARBE),
            Triple(Icons.Default.AutoAwesome, "Coiffage", ServiceCategory.COIFFAGE)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ========================================
        // HEADER — Brand + search
        // ========================================
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Top bar: menu + brand + notifications + avatar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                    Text(
                        text = "Frollot",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null)
                    }
                    // Avatar avec ring
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            )
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.firstName?.take(1)?.uppercase() ?: "F",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = (36 * 0.4f).sp
                            )
                        }
                    }
                }

                // Barre de recherche M3
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 6.dp, bottom = 14.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isBlank()) {
                            Text(
                                text = "Salon, coiffeur, prestation...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ========================================
        // CONTENU SCROLLABLE
        // ========================================
        when {
            isLoading -> {
                ListLoadingState(
                    message = stringResource(Strings.Home.LoadingMessage),
                    modifier = Modifier.fillMaxSize()
                )
            }
            hasError -> {
                ListErrorState(
                    message = stringResource(Strings.Home.ErrorMessage),
                    onRetry = { refreshSalons() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            salons.isEmpty() && !isLoading -> {
                ListEmptyState(
                    title = if (hasActiveFilters) stringResource(Strings.Home.EmptyStateTitleWithFilters)
                        else stringResource(Strings.Home.EmptyStateTitle),
                    message = if (hasActiveFilters) stringResource(Strings.Home.EmptyStateMessageWithFilters)
                        else stringResource(Strings.Home.EmptyStateMessage),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Store,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    action = if (currentUser?.userType == UserType.salon_owner) {
                        {
                            PrimaryButton(
                                text = stringResource(Strings.Home.CreateMySalon),
                                onClick = onNavigateToCreateSalon,
                                icon = {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Salutation
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Text(
                                text = stringResource(Strings.Home.Title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bonjour ${currentUser?.firstName ?: ""},\nqu'est-ce qui vous ferait plaisir ?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 34.sp
                            )
                        }
                    }

                    // Categories horizontales
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categories.forEachIndexed { index, (icon, label, category) ->
                                val isSelected = selectedCategory == category
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        selectedCategory = if (isSelected) null else category
                                    }
                                ) {
                                    Surface(
                                        modifier = Modifier.size(58.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.surface,
                                        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
                                        shadowElevation = 1.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(26.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                       else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.2.sp,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Section: Salons recents
                    item {
                        HomeSectionHead(
                            title = stringResource(Strings.Home.PremiumSalons),
                            action = "Voir tout"
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 14.dp)
                        ) {
                            items(salons.take(5)) { salon ->
                                HomeSalonCard(
                                    salon = salon,
                                    onClick = { onNavigateToSalonDetail(salon.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Bandeau file d'attente
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                Color(0xFF4F3A5B)
                                            ),
                                            start = Offset.Zero,
                                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                        ),
                                        shape = RoundedCornerShape(28.dp)
                                    )
                                    .padding(22.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "FILE D'ATTENTE EN DIRECT",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f),
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (salons.isNotEmpty()) "${salons.first().name} vous recoit dans ~15 min"
                                               else "Rejoignez la file d'attente",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        lineHeight = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            if (salons.isNotEmpty()) onNavigateToSalonDetail(salons.first().id)
                                        },
                                        shape = RoundedCornerShape(999.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Rejoindre la file",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Tous les salons (liste complete)
                    if (salons.size > 5) {
                        item {
                            HomeSectionHead(
                                title = "Tous les salons",
                                action = "${salons.size} resultats"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(salons.drop(5)) { salon ->
                            PremiumSalonCard(
                                salon = salon,
                                onClick = { onNavigateToSalonDetail(salon.id) },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de selection de ville
    if (isCityDialogOpen) {
        var cityInput by remember { mutableStateOf(selectedCity ?: "") }
        AlertDialog(
            onDismissRequest = { isCityDialogOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedCity = cityInput.trim().ifBlank { null }
                    isCityDialogOpen = false
                }) { Text(stringResource(Strings.Home.Apply)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedCity = null
                    isCityDialogOpen = false
                }) { Text(stringResource(Strings.Home.Reset)) }
            },
            title = { Text(stringResource(Strings.Home.FilterByCity)) },
            text = {
                Column {
                    Text(stringResource(Strings.Home.FilterByCityDescription), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        singleLine = true,
                        placeholder = { Text(stringResource(Strings.Home.CityPlaceholder)) }
                    )
                }
            }
        )
    }
}

// ========================================
// COMPOSANTS HOME
// ========================================

@Composable
private fun HomeSectionHead(title: String, action: String = "Voir tout") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp
        )
        Text(
            text = action,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HomeSalonCard(
    salon: Salon,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
        Column {
            // Photo
            Box(modifier = Modifier.height(132.dp).fillMaxWidth()) {
                if (salon.coverPhotoUrl != null && salon.coverPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = salon.coverPhotoUrl,
                        contentDescription = salon.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Store,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Coeur favori
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = salon.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Place,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${salon.city} ${salon.postalCode ?: ""}".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "4.9",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "(214)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ========================================
// SALON CARD — Liste verticale (reutilise)
// ========================================

@Composable
fun PremiumSalonCard(
    salon: Salon,
    onClick: () -> Unit,
    onBookClick: () -> Unit = onClick,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
        Column {
            // Cover photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (salon.coverPhotoUrl != null && salon.coverPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = salon.coverPhotoUrl,
                        contentDescription = salon.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Store,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Rating badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "4.9",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Info
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = salon.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Outlined.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${salon.city}, ${salon.postalCode ?: ""}".trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!salon.description.isNullOrEmpty()) {
                    Text(
                        text = salon.description!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = stringResource(Strings.Home.Cut),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = stringResource(Strings.Home.Coloring),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.Home.Book),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
