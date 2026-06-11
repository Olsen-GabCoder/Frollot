package com.frollot.mobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.localization.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.navigation.Route
import kotlinx.coroutines.launch

/**
 * Drawer Excellence - Ultra compact et raffiné
 *
 * Design:
 * - 240dp de largeur pour un look sleek
 * - Espacement millimétré
 * - Micro-interactions subtiles
 * - Profile card premium en footer
 * - Hiérarchie visuelle parfaite
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    currentUser: User?,
    currentRoute: String?,
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val api = remember { FrollotApi() }
    val scope = rememberCoroutineScope()
    var ownerSalonId by remember { mutableStateOf<String?>(null) }

    // Charger le premier salon du propriétaire
    LaunchedEffect(currentUser?.id) {
        if (currentUser?.userType == UserType.salon_owner) {
            scope.launch {
                try {
                    val salons = api.getSalonsByOwner(currentUser.id)
                    ownerSalonId = salons.firstOrNull()?.id
                } catch (e: Exception) {
                    // Erreur silencieuse, le bouton restera désactivé
                }
            }
        }
    }

    ModalDrawerSheet(
        modifier = modifier.width(240.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header ultra compact
            DrawerHeaderCompact()

            // Navigation scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Navigation principale - Icônes proéminentes
                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Marketplace),
                    icon = Icons.Outlined.Home,
                    route = Route.Home,
                    currentRoute = currentRoute,
                    onClick = { onNavigate(Route.Home) }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Social),
                    icon = Icons.Outlined.Group,
                    route = Route.SocialFeed,
                    currentRoute = currentRoute,
                    onClick = { onNavigate(Route.SocialFeed) }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Appointments),
                    icon = Icons.Outlined.CalendarToday,
                    route = Route.MyBookings,
                    currentRoute = currentRoute,
                    onClick = { onNavigate(Route.MyBookings) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Divider()

                // Compte
                SectionLabel(stringResource(Strings.Components.AppDrawer.Account))

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Profile),
                    icon = Icons.Outlined.Person,
                    route = Route.Profile,
                    currentRoute = currentRoute,
                    onClick = { onNavigate(Route.Profile) }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Notifications),
                    icon = Icons.Outlined.Notifications,
                    enabled = false,
                    onClick = { }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Favorites),
                    icon = Icons.Outlined.FavoriteBorder,
                    enabled = currentUser != null,
                    onClick = {
                        currentUser?.id?.let { userId ->
                            onNavigate(Route.Favorites(userId))
                        }
                    }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Archives),
                    icon = Icons.Outlined.Archive,
                    enabled = currentUser != null,
                    onClick = {
                        currentUser?.id?.let { userId ->
                            onNavigate(Route.Archives(userId))
                        }
                    }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Collections),
                    icon = Icons.Outlined.Collections,
                    enabled = currentUser != null,
                    onClick = {
                        currentUser?.id?.let { userId ->
                            onNavigate(Route.Collections(userId))
                        }
                    }
                )

                // Propriétaires
                if (currentUser?.userType == UserType.salon_owner) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    SectionLabel(stringResource(Strings.Components.AppDrawer.Management))

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.MySalons),
                        icon = Icons.Outlined.Store,
                        enabled = false,
                        onClick = { }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.NewSalon),
                        icon = Icons.Outlined.AddCircleOutline,
                        isPrimary = true,
                        onClick = {
                            currentUser.id.let { ownerId ->
                                onNavigate(Route.CreateSalon(ownerId))
                            }
                        }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.CreatePost),
                        icon = Icons.Outlined.Add,
                        isPrimary = true,
                        onClick = {
                            // Post du réseau social (général, sans salonId)
                            onNavigate(Route.CreatePost(salonId = null))
                        }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.BookingsManagement),
                        icon = Icons.Outlined.EventNote,
                        route = null, // Route dynamique selon le salon
                        enabled = ownerSalonId != null,
                        onClick = {
                            ownerSalonId?.let { salonId ->
                                onNavigate(Route.OwnerBookingsManagement(salonId))
                            }
                        }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.Stats),
                        icon = Icons.Outlined.BarChart,
                        enabled = false,
                        onClick = { }
                    )
                }

                // Coiffeurs
                if (currentUser?.userType == UserType.hairstylist) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    SectionLabel(stringResource(Strings.Components.AppDrawer.Activity))

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.MyPortfolios),
                        icon = Icons.Outlined.Collections,
                        route = null, // Route dynamique
                        enabled = true,
                        onClick = {
                            currentUser.id?.let { userId ->
                                onNavigate(Route.PortfoliosList(userId, "coiffeur"))
                            }
                        }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.NewPortfolio),
                        icon = Icons.Outlined.AddCircleOutline,
                        isPrimary = true,
                        onClick = {
                            currentUser.id?.let { userId ->
                                onNavigate(Route.CreatePortfolio(userId, "coiffeur"))
                            }
                        }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.Services),
                        icon = Icons.Outlined.ContentCut,
                        enabled = false,
                        onClick = { }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.Agenda),
                        icon = Icons.Outlined.Schedule,
                        enabled = false,
                        onClick = { }
                    )
                }

                // Admin
                if (currentUser?.userType == UserType.admin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    SectionLabel(stringResource(Strings.Components.AppDrawer.Admin))

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.Dashboard),
                        icon = Icons.Outlined.Dashboard,
                        enabled = false,
                        onClick = { }
                    )

                    NavItem(
                        label = stringResource(Strings.Components.AppDrawer.Users),
                        icon = Icons.Outlined.SupervisedUserCircle,
                        enabled = false,
                        onClick = { }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Settings),
                    icon = Icons.Outlined.Settings,
                    route = Route.Settings,
                    currentRoute = currentRoute,
                    onClick = { onNavigate(Route.Settings) }
                )

                NavItem(
                    label = stringResource(Strings.Components.AppDrawer.Help),
                    icon = Icons.Outlined.HelpOutline,
                    enabled = false,
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Footer Profile harmonisé
            ProfileFooterCard(
                currentUser = currentUser,
                onLogout = onLogout
            )
        }
    }
}

/**
 * Header minimaliste avec logo
 */
@Composable
fun DrawerHeaderCompact() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Logo gradient
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "F",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Frollot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Beauté",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Label de section ultra discret
 */
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Divider ultra fin
 */
@Composable
fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    )
}

/**
 * Nav Item compact et élégant
 */
@Composable
fun NavItem(
    label: String,
    icon: ImageVector,
    route: Route? = null,
    currentRoute: String? = null,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val isSelected = route?.let { r ->
        when {
            r is Route.Home -> currentRoute?.contains("Home", ignoreCase = true) == true
            r is Route.SocialFeed -> currentRoute?.contains("SocialFeed", ignoreCase = true) == true
            r is Route.Profile -> currentRoute?.contains("Profile", ignoreCase = true) == true
            r is Route.MyBookings -> currentRoute?.contains("MyBookings", ignoreCase = true) == true
            else -> false
        }
    } ?: false

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            isPrimary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else -> Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    isSelected -> MaterialTheme.colorScheme.primary
                    isPrimary -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    isSelected -> MaterialTheme.colorScheme.primary
                    isPrimary -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Profile Footer - Harmonisé avec le reste du drawer
 * Design cohérent avec les NavItem et l'espacement général
 */
@Composable
fun ProfileFooterCard(
    currentUser: User?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        // Divider comme ailleurs dans le drawer
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )

        // Profil - même style que les NavItem
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar avec badge online
                Box {
                    UserAvatar(
                        user = currentUser,
                        size = 38.dp,
                        showBorder = false
                    )

                    // Status online - plus discret
                    Surface(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }

                // Info utilisateur
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = currentUser?.let { user ->
                            val name = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                            name.ifBlank { user.email.substringBefore("@") }
                        } ?: stringResource(Strings.Components.AppDrawer.Guest),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    currentUser?.let { user ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (user.userType) {
                                    UserType.salon_owner -> Icons.Outlined.Store
                                    UserType.hairstylist -> Icons.Outlined.ContentCut
                                    UserType.admin -> Icons.Outlined.Shield
                                    else -> Icons.Outlined.Person
                                },
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = when (user.userType) {
                                    UserType.client -> stringResource(Strings.Components.AppDrawer.Client)
                                    UserType.salon_owner -> stringResource(Strings.Components.AppDrawer.Owner)
                                    UserType.hairstylist -> stringResource(Strings.Components.AppDrawer.Hairstylist)
                                    UserType.admin -> stringResource(Strings.Components.AppDrawer.AdminUser)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Bouton Déconnexion - même taille que les icônes NavItem
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Déconnexion",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Version - même style que les SectionLabel
        Text(
            text = "v1.0.0 © 2025",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp)
        )
    }
}
