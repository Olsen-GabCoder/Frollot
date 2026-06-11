package com.frollot.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.User

/**
 * En-tête standardisé pour tous les écrans de l'application.
 * Basé sur le design de SocialFeedScreen avec glassmorphism et gradients.
 * 
 * @param currentUser Utilisateur actuel (pour afficher l'avatar)
 * @param onMenuClick Callback pour ouvrir le menu (si onBackClick n'est pas fourni)
 * @param onBackClick Callback pour le bouton retour (si fourni, remplace le menu)
 * @param onNavigateToProfile Callback pour naviguer vers le profil
 * @param title Titre optionnel à afficher à la place de "Frollot"
 * @param showSearchBar Afficher la barre de recherche (défaut: false)
 * @param searchQuery Requête de recherche (si showSearchBar = true)
 * @param onSearchQueryChange Callback pour changer la requête (si showSearchBar = true)
 * @param onSearchClick Callback pour cliquer sur la barre de recherche
 * @param actions Actions personnalisées à afficher à droite (en plus de l'avatar)
 * @param onNavigateToArchives Callback pour naviguer vers les archives (optionnel)
 * @param onNotificationsClick Callback pour les notifications (optionnel)
 * @param showAvatar Afficher l'avatar utilisateur (défaut: true)
 * @param searchSuggestions Suggestions de recherche unifiées (optionnel, Phase C.1)
 * @param isSearchLoading État de chargement des suggestions (optionnel)
 * @param onPostSuggestionClick Callback pour cliquer sur une suggestion de post (optionnel)
 * @param onSalonSuggestionClick Callback pour cliquer sur une suggestion de salon (optionnel)
 * @param onUserSuggestionClick Callback pour cliquer sur une suggestion d'utilisateur (optionnel)
 * @param onHashtagSuggestionClick Callback pour cliquer sur une suggestion de hashtag (optionnel)
 */
@Composable
fun StandardAppHeader(
    currentUser: User?,
    onMenuClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    onNavigateToProfile: () -> Unit = {},
    title: String = "Frollot",
    showSearchBar: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onNavigateToArchives: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    showAvatar: Boolean = true,
    searchSuggestions: com.frollot.mobile.model.SearchResponse? = null,
    isSearchLoading: Boolean = false,
    onPostSuggestionClick: ((String) -> Unit)? = null,
    onSalonSuggestionClick: ((String) -> Unit)? = null,
    onUserSuggestionClick: ((String) -> Unit)? = null,
    onHashtagSuggestionClick: ((String) -> Unit)? = null
) {
    // TopBar avec effet glassmorphism
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 0.dp,
                spotColor = Color.Transparent
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo / Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton retour ou menu
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Retour",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = onMenuClick) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Actions avec navigation vers le profil
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Actions personnalisées
                    actions()

                    // Bouton Archives (si fourni)
                    onNavigateToArchives?.let { onArchives ->
                        IconButton(onClick = onArchives) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Archive,
                                    contentDescription = "Archives",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Notification badge (si fourni)
                    onNotificationsClick?.let { onNotifications ->
                        Box {
                            IconButton(onClick = onNotifications) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Badge rouge
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    // Avatar utilisateur avec navigation vers profil (si activé)
                    if (showAvatar) {
                        IconButton(onClick = onNavigateToProfile) {
                            UserAvatar(
                                user = currentUser,
                                size = 40.dp,
                                showBorder = true
                            )
                        }
                    }
                }
            }

            // Barre de recherche premium (si activée)
            if (showSearchBar) {
                Column {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        onClick = onSearchClick ?: {}
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Rechercher",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onSearchQueryChange != null) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    ).copy(
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    "Rechercher des posts, des personnes...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            } else {
                                Text(
                                    text = "Rechercher des posts, des personnes...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Suggestions unifiées (Phase C.1)
                    if (searchQuery.isNotBlank() && (searchSuggestions != null || isSearchLoading)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            UnifiedSearchSuggestions(
                                searchResponse = searchSuggestions,
                                isLoading = isSearchLoading,
                                onPostClick = onPostSuggestionClick ?: {},
                                onSalonClick = onSalonSuggestionClick ?: {},
                                onUserClick = onUserSuggestionClick ?: {},
                                onHashtagClick = onHashtagSuggestionClick ?: {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

