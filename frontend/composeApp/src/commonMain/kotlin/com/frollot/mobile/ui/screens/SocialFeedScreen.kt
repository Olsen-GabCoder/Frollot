package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.frollot.mobile.model.CommentResponse
import com.frollot.mobile.model.PostMediaResponse
import com.frollot.mobile.model.PostMediaType
import com.frollot.mobile.model.PostResponse
import com.frollot.mobile.model.PostType
import com.frollot.mobile.model.PostVisibility
import com.frollot.mobile.ui.utils.AnimationSpecs
import com.frollot.mobile.model.ReactionType
import com.frollot.mobile.model.ReportedEntityType
import com.frollot.mobile.model.TaggedType
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.ExternalShareDialog
import com.frollot.mobile.ui.components.FullScreenImageViewer
import com.frollot.mobile.ui.components.PullToRefreshBox
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.menus.PostOptionsMenu
import com.frollot.mobile.ui.components.menus.PostOptionItem
import com.frollot.mobile.ui.components.menus.PostOptionsDivider
import com.frollot.mobile.ui.utils.rememberExternalShare
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.time.currentTimeMillis


/**
 * 🎨 ULTRA PREMIUM Social Feed Screen
 *
 * Features de luxe:
 * - Animations sophistiquées et fluides
 * - Glassmorphism effects
 * - Gradients dynamiques
 * - Micro-interactions premium
 * - Design inspiré des meilleures apps (Instagram, Twitter X, Threads)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    currentUser: User?,
    onMenuClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onCreatePost: () -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToArchives: () -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToCoiffeur: ((String) -> Unit)? = null, // Phase E.1 - Profil Coiffeur
    onNavigateToClient: ((String) -> Unit)? = null, // Phase E.4 - Profil Client
    onNavigateToOwner: ((String) -> Unit)? = null, // Phase E.5 - Profil Propriétaire
    onNavigateToSalon: ((String) -> Unit)? = null, // Phase E.2 - Profil Salon Social
    onNavigateToReport: ((ReportedEntityType, String) -> Unit)? = null, // Navigation vers l'écran de signalement
    onNavigateToPost: ((String) -> Unit)? = null // Navigation vers l'écran de détail du post
) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Phase I.1 - Pull-to-refresh et Infinite Scroll
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    // Filtre par type de post
    var selectedPostTypeFilter by remember { mutableStateOf<PostType?>(null) }
    
    // Filtre "Près de moi" (Phase C.4)
    var isNearbyFilterActive by remember { mutableStateOf(false) }
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    
    // Filtre "Mes suivis" (Phase D.2)
    var isFollowingFilterActive by remember { mutableStateOf(false) }

    // Stocker les commentaires par post ID
    var commentsByPostId by remember { mutableStateOf<Map<String, List<CommentResponse>>>(emptyMap()) }
    var loadingCommentsForPost by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Dialog de partage - Phase D.3
    var showShareDialog by remember { mutableStateOf(false) }
    var postToShare by remember { mutableStateOf<String?>(null) }
    var shareComment by remember { mutableStateOf("") }

    // Dialog de sélection de collection - Phase F.1
    var showCollectionDialog by remember { mutableStateOf(false) }
    var postToAddToCollection by remember { mutableStateOf<String?>(null) }
    var userCollections by remember { mutableStateOf<List<com.frollot.mobile.model.CollectionResponse>>(emptyList()) }
    var isLoadingCollections by remember { mutableStateOf(false) }

    
    // Phase I.2 - Partage Externe
    var showExternalShareDialog by remember { mutableStateOf(false) }
    var postToShareExternally by remember { mutableStateOf<PostResponse?>(null) }
    
    // Viewer d'images en plein écran
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerImages by remember { mutableStateOf<List<PostMediaResponse>>(emptyList()) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }
    
    // État global pour le menu d'options (pour l'overlay)
    var postIdWithOpenMenu by remember { mutableStateOf<String?>(null) }
    
    // Timestamp de l'ouverture du menu pour empêcher la fermeture immédiate
    var menuOpenTime by remember { mutableStateOf(0L) }
    
    // État pour stocker les informations du post avec le menu ouvert
    var postWithOpenMenu by remember { mutableStateOf<PostResponse?>(null) }
    
    // État pour stocker la position du bouton menu (pour positionner le menu au niveau du Scaffold)
    var menuButtonPosition by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
    
    // Logs de diagnostic pour l'état global
    LaunchedEffect(postIdWithOpenMenu) {
        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] postIdWithOpenMenu changed: $postIdWithOpenMenu")
        if (postIdWithOpenMenu != null) {
            menuOpenTime = currentTimeMillis()
            FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Menu opened at: $menuOpenTime")
        }
    }

    // Suggestions de recherche unifiées (Phase C.1)
    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<com.frollot.mobile.model.SearchResponse?>(null) }
    var isSearchLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    
    // Phase I.2 - Partage Externe
    val externalShare = rememberExternalShare()

    // Animation pour le FAB
    val fabScale by animateFloatAsState(
        targetValue = if (posts.isEmpty()) 1.1f else 1f,
        animationSpec = AnimationSpecs.SoftTouchInteraction
    )

    // Fonction pour charger les commentaires d'un post
    fun loadCommentsForPost(postId: String) {
        scope.launch {
            try {
                loadingCommentsForPost = loadingCommentsForPost + postId
                val commentsPage = api.getCommentsByPost(postId, page = 0, size = 2)
                commentsByPostId = commentsByPostId + (postId to commentsPage.content)
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement commentaires pour post $postId: ${e.message}")
                // En cas d'erreur, on laisse la liste vide plutôt que de bloquer l'affichage
                commentsByPostId = commentsByPostId + (postId to emptyList())
            } finally {
                loadingCommentsForPost = loadingCommentsForPost - postId
            }
        }
    }

    // Phase I.1 - Pull-to-refresh et Infinite Scroll
    fun loadFeed(reset: Boolean = false, showLoading: Boolean = true) {
        scope.launch {
            try {
                if (reset) {
                    currentPage = 0
                    hasMore = true
                    if (showLoading) isLoading = true
                    isRefreshing = true
                } else {
                    isLoadingMore = true
                }
                hasError = false
                
                val page = if (reset) 0 else currentPage
                val feed = when {
                    isFollowingFilterActive && currentUser != null -> {
                        // Utiliser le feed Following
                        api.getFollowingFeed(page = page, size = 20)
                    }
                    isNearbyFilterActive && userLatitude != null && userLongitude != null -> {
                        // Utiliser la recherche par proximité
                        api.getPostsNearby(
                            latitude = userLatitude!!,
                            longitude = userLongitude!!,
                            radiusKm = 10.0,
                            page = page,
                            size = 20
                        )
                    }
                    else -> {
                        // Utiliser le feed classique
                        api.getFeed(page = page, size = 20)
                    }
                }

                // Filtrer par type si un filtre est sélectionné
                val filteredPosts = if (selectedPostTypeFilter != null) {
                    feed.content.filter { it.postType == selectedPostTypeFilter }
                } else {
                    feed.content
                }

                // Phase I.1 - Gérer la pagination (ajouter ou remplacer)
                if (reset) {
                    posts = filteredPosts
                } else {
                    posts = posts + filteredPosts
                }

                // Phase I.1 - Déterminer s'il y a plus de posts à charger
                hasMore = !feed.last && feed.content.isNotEmpty()

                // Charger les commentaires pour les posts qui en ont
                val postsWithComments = filteredPosts.filter { it.commentsCount > 0 }
                postsWithComments.forEach { post ->
                    if (!commentsByPostId.containsKey(post.id) && !loadingCommentsForPost.contains(post.id)) {
                        loadCommentsForPost(post.id)
                    }
                }

                currentPage = page
                isLoading = false
                isRefreshing = false
                isLoadingMore = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement feed: ${e.message}")
                errorMessage = e.message
                hasError = true
                isLoading = false
                isRefreshing = false
                isLoadingMore = false
            }
        }
    }

    // Initialiser avec une position par défaut (Paris) pour le filtre "Près de moi"
    LaunchedEffect(Unit) {
        if (userLatitude == null && userLongitude == null) {
            // Position par défaut : Paris (pour les tests)
            // En production, cela devrait être remplacé par la géolocalisation réelle
            userLatitude = 48.8566
            userLongitude = 2.3522
        }
        loadFeed(reset = true)
    }
    
    // Recharger le feed quand les filtres changent
    LaunchedEffect(selectedPostTypeFilter, isNearbyFilterActive, isFollowingFilterActive, userLatitude, userLongitude) {
        loadFeed(reset = true)
    }
    
    // Phase I.1 - Détecter le scroll pour charger plus (infinite scroll)
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= posts.size - 3 && 
                    hasMore && 
                    !isLoadingMore && 
                    !isLoading &&
                    !isRefreshing) {
                    currentPage++
                    loadFeed(reset = false, showLoading = false)
                }
            }
    }

    // Recherche unifiée avec debounce pour les suggestions (Phase C.1)
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && searchQuery.length >= 2) {
            delay(300) // Debounce de 300ms
            isSearchLoading = true
            try {
                val results = api.unifiedSearch(
                    query = searchQuery,
                    type = com.frollot.mobile.model.SearchType.ALL,
                    page = 0,
                    size = 3 // Limiter à 3 résultats par type pour les suggestions
                )
                searchSuggestions = results
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur suggestions: ${e.message}")
                searchSuggestions = null
            } finally {
                isSearchLoading = false
            }
        } else {
            searchSuggestions = null
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onMenuClick = onMenuClick,
                onNavigateToProfile = onNavigateToProfile,
                title = "Frollot",
                showSearchBar = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClick = { onNavigateToSearch(searchQuery) },
                onNavigateToArchives = onNavigateToArchives,
                onNotificationsClick = { /* TODO: Notifications */ },
                searchSuggestions = searchSuggestions,
                isSearchLoading = isSearchLoading,
                onPostSuggestionClick = { _ ->
                    // Navigation vers le post (à implémenter si nécessaire)
                    onNavigateToSearch("")
                },
                onSalonSuggestionClick = { _ ->
                    // Navigation vers le salon (à implémenter si nécessaire)
                    onNavigateToSearch("")
                },
                onUserSuggestionClick = { _ ->
                    // Navigation vers le profil utilisateur (à implémenter si nécessaire)
                    onNavigateToSearch("")
                },
                onHashtagSuggestionClick = { _ ->
                    // Navigation vers les posts du hashtag (à implémenter si nécessaire)
                    onNavigateToSearch("")
                }
            )
        },
        floatingActionButton = {
            // FAB ultra premium avec gradient et animation
            FloatingActionButton(
                onClick = onCreatePost,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .scale(fabScale)
                    .size(64.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        ambientColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                        Icons.Default.Add,
                        "Créer",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading && posts.isEmpty() -> {
                    UltraPremiumLoadingSkeleton(modifier = Modifier.padding(paddingValues))
                }

                hasError -> {
                    UltraPremiumErrorCard(
                        message = errorMessage ?: "Impossible de charger le feed",
                        onRetry = { loadFeed(reset = true) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                posts.isEmpty() -> {
                    UltraPremiumEmptyCard(
                        onCreatePost = onCreatePost,
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                else -> {
                    // Phase I.1 - Pull-to-refresh et Infinite Scroll
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { loadFeed(reset = true, showLoading = false) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                    // Barre de filtres par type de post
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(Strings.SocialFeed.FilterByType),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Filtre "Mes suivis" (Phase D.2)
                                if (currentUser != null) {
                                    FilterChip(
                                        selected = isFollowingFilterActive,
                                        onClick = {
                                            isFollowingFilterActive = !isFollowingFilterActive
                                            // Désactiver les autres filtres quand "Mes suivis" est actif
                                            if (isFollowingFilterActive) {
                                                selectedPostTypeFilter = null
                                                isNearbyFilterActive = false
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(Strings.SocialFeed.MyFollows),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.PersonAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                                
                                // Filtre "Près de moi"
                                FilterChip(
                                    selected = isNearbyFilterActive,
                                    onClick = {
                                        isNearbyFilterActive = !isNearbyFilterActive
                                        // Désactiver les autres filtres quand "Près de moi" est actif
                                        if (isNearbyFilterActive) {
                                            selectedPostTypeFilter = null
                                            isFollowingFilterActive = false
                                        }
                                    },
                                    enabled = !isFollowingFilterActive,
                                    label = {
                                        Text(
                                            text = stringResource(Strings.SocialFeed.NearMe),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                
                                // Option "Tous"
                                FilterChip(
                                    selected = selectedPostTypeFilter == null && !isNearbyFilterActive && !isFollowingFilterActive,
                                    onClick = { 
                                        selectedPostTypeFilter = null
                                        isNearbyFilterActive = false
                                        isFollowingFilterActive = false
                                    },
                                    enabled = !isNearbyFilterActive && !isFollowingFilterActive,
                                    label = {
                                        Text(
                                            text = stringResource(Strings.SocialFeed.All),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                // Filtres par type
                                PostType.entries.forEach { type ->
                                    FilterChip(
                                        selected = selectedPostTypeFilter == type,
                                        onClick = { selectedPostTypeFilter = type },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = type.getEmoji(),
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = type.getLocalizedDisplayName(),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = posts,
                        key = { it.id }
                    ) { post ->
                        UltraPremiumPostCard(
                            post = post,
                            onPostClick = { postId ->
                                // Ouvrir le viewer d'images si le post a des médias, sinon naviguer vers le détail
                                val clickedPost = posts.find { it.id == postId }
                                if (clickedPost != null) {
                                    if (clickedPost.media.isNotEmpty()) {
                                        // Ouvrir le viewer d'images
                                        imageViewerImages = clickedPost.media.sortedBy { it.orderIndex }
                                        imageViewerInitialIndex = 0
                                        showImageViewer = true
                                    } else {
                                        val imageUrl = clickedPost.imageUrl
                                        if (imageUrl != null) {
                                            // Créer un PostMediaResponse temporaire pour l'image principale
                                            imageViewerImages = listOf(
                                                PostMediaResponse(
                                                    id = clickedPost.id,
                                                    mediaUrl = imageUrl,
                                                    mediaType = PostMediaType.after,
                                                    orderIndex = 0,
                                                    mediaTypeLabel = "Image"
                                                )
                                            )
                                            imageViewerInitialIndex = 0
                                            showImageViewer = true
                                        } else {
                                            // Pas d'image, naviguer vers le détail du post
                                            onNavigateToPost?.invoke(postId)
                                        }
                                    }
                                }
                            },
                            postIdWithOpenMenu = postIdWithOpenMenu,
                            onPostIdWithOpenMenuChange = { newValue: String? -> 
                                FrollotLogger.debug("API", "🔍 [SocialFeedScreen] onPostIdWithOpenMenuChange called with: $newValue")
                                FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Previous postIdWithOpenMenu: $postIdWithOpenMenu")
                                postIdWithOpenMenu = newValue
                                FrollotLogger.debug("API", "🔍 [SocialFeedScreen] New postIdWithOpenMenu: $postIdWithOpenMenu")
                                // Stocker le post avec le menu ouvert
                                postWithOpenMenu = if (newValue != null) post else null
                            },
                            onMenuButtonPositionChange = { position: androidx.compose.ui.geometry.Offset? ->
                                FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Menu button position changed: $position")
                                menuButtonPosition = position
                            },
                            comments = commentsByPostId[post.id] ?: emptyList(),
                            currentUser = currentUser, // Phase F.2 - Pour vérifier si l'utilisateur est l'auteur
                            onTagClick = { taggedType: TaggedType, taggedId: String ->
                                // Phase E - Navigation vers les profils selon le type
                                when (taggedType) {
                                    TaggedType.salon -> {
                                        onNavigateToSalon?.invoke(taggedId)
                                    }
                                    TaggedType.user -> {
                                        // On suppose que c'est un coiffeur si tagué
                                        onNavigateToCoiffeur?.invoke(taggedId)
                                    }
                                }
                            },
                            onLikeClick = { postId: String ->
                                // Rétrocompatibilité : like classique si pas de callback de réaction
                                posts = posts.map { p ->
                                    if (p.id == postId) {
                                        p.copy(
                                            isLikedByCurrentUser = !p.isLikedByCurrentUser,
                                            likesCount = if (p.isLikedByCurrentUser) {
                                                (p.likesCount - 1).coerceAtLeast(0)
                                            } else {
                                                p.likesCount + 1
                                            }
                                        )
                                    } else {
                                        p
                                    }
                                }

                                scope.launch {
                                    try {
                                        val updatedPost = api.toggleLike(postId)
                                        // Mettre à jour le post avec les données du serveur
                                        posts = posts.map { p ->
                                            if (p.id == postId) updatedPost else p
                                        }
                                    } catch (_: Exception) {
                                        // En cas d'erreur, rafraîchir le feed
                                        loadFeed(reset = false, showLoading = false)
                                    }
                                }
                            },
                            // Phase D.4 - Réactions Spécialisées
                            onReactionClick = { postId: String, reactionType: ReactionType ->
                                val currentPost = posts.find { it.id == postId }
                                val currentReaction = currentPost?.currentUserReaction
                                
                                scope.launch {
                                    try {
                                        if (currentReaction == reactionType) {
                                            // Si l'utilisateur clique sur sa réaction actuelle, la supprimer
                                            val updatedPost = api.removeReaction(postId)
                                            posts = posts.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        } else {
                                            // Ajouter/modifier la réaction
                                            val updatedPost = api.addReaction(postId, reactionType)
                                            posts = posts.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        }
                                    } catch (e: Exception) {
                                        FrollotLogger.error("API", "❌ Erreur lors de la réaction: ${e.message}")
                                        // En cas d'erreur, rafraîchir le feed
                                        loadFeed(reset = false, showLoading = false)
                                    }
                                }
                            },
                            onCommentClick = { postId: String ->
                                onNavigateToComments(postId)
                            },
                            onShareClick = { postId: String ->
                                // Phase D.3 - Partage de Posts
                                val post = posts.find { it.id == postId }
                                if (post != null) {
                                    if (post.isSharedByCurrentUser) {
                                        // Si déjà partagé, annuler le partage
                                        scope.launch {
                                            try {
                                                // Optimistic update
                                                posts = posts.map { p ->
                                                    if (p.id == postId) {
                                                        p.copy(
                                                            isSharedByCurrentUser = false,
                                                            sharesCount = (p.sharesCount - 1).coerceAtLeast(0)
                                                        )
                                                    } else {
                                                        p
                                                    }
                                                }
                                                val updatedPost = api.unsharePost(postId)
                                                // Mettre à jour avec les données du serveur
                                                posts = posts.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            } catch (e: Exception) {
                                                FrollotLogger.error("API", "❌ Erreur lors de l'annulation du partage: ${e.message}")
                                                // En cas d'erreur, rafraîchir le feed
                                                loadFeed(showLoading = false)
                                            }
                                        }
                                    } else {
                                        // Ouvrir le dialog de partage
                                        postToShare = postId
                                        shareComment = ""
                                        showShareDialog = true
                                    }
                                }
                            },
                            onAuthorClick = { authorId: String ->
                                // Phase E - Navigation selon le type d'utilisateur
                                val authorUserType = post.authorUserType
                                when (authorUserType) {
                                    UserType.hairstylist -> {
                                        // Coiffeur : naviguer vers le profil coiffeur enrichi
                                        onNavigateToCoiffeur?.invoke(authorId)
                                    }
                                    UserType.salon_owner -> {
                                        // Propriétaire de salon : naviguer vers le profil propriétaire
                                        onNavigateToOwner?.invoke(authorId)
                                    }
                                    UserType.client -> {
                                        // Client : naviguer vers le profil client
                                        onNavigateToClient?.invoke(authorId)
                                    }
                                    null -> {
                                        // Type inconnu : essayer le profil coiffeur par défaut (rétrocompatibilité)
                                        onNavigateToCoiffeur?.invoke(authorId)
                                    }
                                    else -> {
                                        // Autres types (admin, etc.) : ne rien faire
                                    }
                                }
                            },
                            onArchiveClick = { postId: String ->
                                scope.launch {
                                    try {
                                        // Archiver le post
                                        api.archivePost(postId)
                                        // Retirer le post du feed local (il sera masqué du feed)
                                        posts = posts.filter { it.id != postId }
                                    } catch (e: Exception) {
                                        // En cas d'erreur, rafraîchir le feed
                                        FrollotLogger.debug("SocialFeed", "Erreur lors de l'archivage: ${e.message}")
                                        loadFeed(showLoading = false)
                                    }
                                }
                            },
                            onSaveToCollection = { postId: String ->
                                // Phase F.1 - Ouvrir dialog de sélection de collection
                                if (currentUser != null) {
                                    postToAddToCollection = postId
                                    showCollectionDialog = true
                                    // Charger les collections de l'utilisateur
                                    scope.launch {
                                        try {
                                            isLoadingCollections = true
                                            userCollections = api.getCollectionsByUser(currentUser.id, includePrivate = true)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("SocialFeed", "Erreur lors du chargement des collections: ${e.message}")
                                        } finally {
                                            isLoadingCollections = false
                                        }
                                    }
                                }
                            },
                            // Phase F.2 - Posts Épinglés
                            onPinClick = { postId: String ->
                                val post = posts.find { it.id == postId }
                                // Afficher uniquement si l'utilisateur est l'auteur
                                if (currentUser != null && post?.authorId == currentUser.id) {
                                    scope.launch {
                                        try {
                                            val updatedPost = if (post.isPinned) {
                                                api.unpinPost(postId)
                                            } else {
                                                api.pinPost(postId)
                                            }
                                            // Mettre à jour le post dans la liste
                                            posts = posts.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("SocialFeed", "Erreur lors de l'épinglage/désépinglage: ${e.message}")
                                            // En cas d'erreur, rafraîchir le feed
                                            loadFeed(showLoading = false)
                                        }
                                    }
                                }
                            },
                            // Phase H.1 - Signalement de Contenu
                            onReportClick = { postId: String ->
                                onNavigateToReport?.invoke(ReportedEntityType.POST, postId)
                            },
                            // Phase I.2 - Partage Externe
                            onExternalShareClick = { postToShare: PostResponse ->
                                postToShareExternally = postToShare
                                showExternalShareDialog = true
                            }
                        )
                    }

                            // Phase I.1 - Indicateur de chargement pour infinite scroll
                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Spacer final pour le FAB
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        // Dialog de partage - Phase D.3 (standardisé)
        if (showShareDialog && postToShare != null) {
            com.frollot.mobile.ui.components.dialogs.StandardDialog(
                title = "Partager ce post",
                onDismissRequest = {
                    showShareDialog = false
                    postToShare = null
                    shareComment = ""
                },
                content = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.SocialFeed.AddComment),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = shareComment,
                            onValueChange = {
                                if (it.length <= 500) {
                                    shareComment = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(stringResource(Strings.SocialFeed.AddToCollectionExample))
                            },
                            maxLines = 4,
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Text(
                            text = "${shareComment.length}/500",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                },
                confirmButton = {
                    com.frollot.mobile.ui.components.buttons.PrimaryButton(
                        text = stringResource(Strings.SocialFeed.Share),
                        onClick = {
                            val postId = postToShare!!
                            val comment = shareComment.takeIf { it.isNotBlank() }
                            
                            scope.launch {
                                try {
                                    // Optimistic update
                                    posts = posts.map { p ->
                                        if (p.id == postId) {
                                            p.copy(
                                                isSharedByCurrentUser = true,
                                                sharesCount = p.sharesCount + 1
                                            )
                                        } else {
                                            p
                                        }
                                    }
                                    
                                    val updatedPost = api.sharePost(postId, comment)
                                    
                                    // Mettre à jour avec les données du serveur
                                    posts = posts.map { p ->
                                        if (p.id == postId) updatedPost else p
                                    }
                                    
                                    showShareDialog = false
                                    postToShare = null
                                    shareComment = ""
                                } catch (e: Exception) {
                                    FrollotLogger.error("API", "❌ Erreur lors du partage: ${e.message}")
                                    // En cas d'erreur, rafraîchir le feed
                                    loadFeed(showLoading = false)
                                    showShareDialog = false
                                    postToShare = null
                                    shareComment = ""
                                }
                            }
                        },
                        enabled = shareComment.length <= 500
                    )
                },
                dismissButton = {
                    com.frollot.mobile.ui.components.buttons.SecondaryButton(
                        text = stringResource(Strings.SocialFeed.Cancel),
                        onClick = {
                            showShareDialog = false
                            postToShare = null
                            shareComment = ""
                        }
                    )
                }
            )
        }

        // Dialog de sélection de collection - Phase F.1
        if (showCollectionDialog && postToAddToCollection != null && currentUser != null) {
            AlertDialog(
                onDismissRequest = {
                    showCollectionDialog = false
                    postToAddToCollection = null
                },
                title = {
                    Text(stringResource(Strings.SocialFeed.AddToCollection))
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isLoadingCollections) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (userCollections.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(Strings.SocialFeed.NoCollectionYet),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = {
                                        // TODO: Navigate to create collection
                                        // Pour l'instant, on ferme le dialog
                                        showCollectionDialog = false
                                        postToAddToCollection = null
                                    }
                                ) {
                                    Text(stringResource(Strings.SocialFeed.CreateCollection))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(userCollections) { collection ->
                                    CollectionSelectionItem(
                                        collection = collection,
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    api.addPostToCollection(collection.id, postToAddToCollection!!)
                                                    showCollectionDialog = false
                                                    postToAddToCollection = null
                                                    // Optionnel: Rafraîchir le feed pour voir les changements
                                                } catch (e: Exception) {
                                                    FrollotLogger.debug("SocialFeed", "Erreur lors de l'ajout à la collection: ${e.message}")
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    // Pas de bouton de confirmation - la sélection se fait directement
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCollectionDialog = false
                            postToAddToCollection = null
                        }
                    ) {
                        Text(stringResource(Strings.Common.Cancel))
                    }
                }
            )
        }

        // Overlay pour fermer le menu au clic extérieur
        // Placé APRÈS le contenu pour être rendu par-dessus, mais avec zIndex très bas
        // Utilise pointerInput avec detectTapGestures mais seulement si le clic n'a pas été intercepté
        // Les éléments interactifs (menu, boutons, dialogues) avec zIndex élevé recevront les clics en premier
        // IMPORTANT: L'overlay est rendu APRÈS le contenu mais avec zIndex très bas (0.1f) pour être en dessous des éléments interactifs
            if (postIdWithOpenMenu != null) {
            FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering overlay for postIdWithOpenMenu: $postIdWithOpenMenu")
            var shouldCloseMenu by remember(postIdWithOpenMenu) { mutableStateOf(false) }
            
            LaunchedEffect(postIdWithOpenMenu) {
                shouldCloseMenu = false
            }
            
            // Overlay avec pointerInput qui intercepte les clics
            // IMPORTANT: Utiliser pointerInput avec onPointerEvent pour vérifier si le clic est sur le menu
            // Mais pour l'instant, on utilise detectTapGestures avec un délai
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(postIdWithOpenMenu) {
                        detectTapGestures(
                            onTap = { offset ->
                                val currentTime = currentTimeMillis()
                                val timeSinceMenuOpen = currentTime - menuOpenTime
                                FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Overlay tapped at offset: $offset, time since menu open: ${timeSinceMenuOpen}ms")
                                
                                // Ignorer les clics qui arrivent trop tôt après l'ouverture du menu (300ms)
                                // Cela permet aux clics sur les éléments interactifs de se propager
                                if (timeSinceMenuOpen.compareTo(300L) > 0) {
                                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Click after delay, scheduling close")
                                    shouldCloseMenu = true
                } else {
                                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Click too soon after menu open (${timeSinceMenuOpen}ms < 300ms), ignoring")
                                }
                            }
                        )
                    }
                    // zIndex retiré - le menu doit être rendu APRÈS l'overlay pour être au-dessus
            )
            
            // Fermer le menu après un court délai pour permettre aux clics sur les éléments interactifs de se propager
            LaunchedEffect(shouldCloseMenu) {
                if (shouldCloseMenu && postIdWithOpenMenu != null) {
                    delay(50) // Délai très court pour permettre aux clics sur les éléments interactifs de se propager
                    if (postIdWithOpenMenu != null) {
                        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Overlay click confirmed, closing menu")
                        postIdWithOpenMenu = null
                    }
                    shouldCloseMenu = false
                }
            }
        } else {
            FrollotLogger.debug("API", "🔍 [SocialFeedScreen] No overlay (postIdWithOpenMenu is null)")
        }
        
        // Menu rendu au niveau du Scaffold, APRÈS l'overlay, avec zIndex très élevé
        // Cela permet au menu d'être au-dessus de l'overlay et de recevoir les clics
        if (postIdWithOpenMenu != null && postWithOpenMenu != null) {
            val post = postWithOpenMenu!!
            FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering menu at Scaffold level for post: ${post.id}")
            
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                    .zIndex(3000f) // zIndex très élevé pour être au-dessus de l'overlay (0.1f)
            ) {
                // Rendre le menu en haut à droite de l'écran
                // Positionné par rapport au bouton si on a la position, sinon en haut à droite
                // Utiliser padding pour éviter que le menu soit coupé par les bords de l'écran
                PostOptionsMenu(
                    expanded = true,
                    onDismissRequest = {
                        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Menu dismissed at Scaffold level")
                        postIdWithOpenMenu = null
                        postWithOpenMenu = null
                        menuButtonPosition = null
                    },
                    modifier = with(LocalDensity.current) {
                        val buttonPosition = menuButtonPosition // Stocker dans une variable locale pour le smart cast
                        Modifier
                            .then(
                                // Utiliser la position du bouton si disponible, sinon utiliser un positionnement par défaut
                                if (buttonPosition != null) {
                                    // Convertir les pixels en dp et positionner le menu juste en dessous du bouton
                                    val menuWidth = 280.dp.toPx()
                                    val buttonSize = 36.dp.toPx() // Taille du bouton
                                    val offsetX = buttonPosition.x - menuWidth + buttonSize // Aligner à droite du bouton
                                    val offsetY = buttonPosition.y + buttonSize + 8.dp.toPx() // Positionner juste en dessous du bouton
                                    
                                    Modifier
                                        .offset(
                                            x = offsetX.toDp(),
                                            y = offsetY.toDp()
                                        )
                                } else {
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 16.dp, top = 200.dp) // Positionnement par défaut si la position n'est pas disponible
                                }
                            )
                            // Utiliser wrapContentSize pour que le menu s'adapte à son contenu
                            .wrapContentSize(Alignment.TopEnd)
                    }
                ) {
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering menu items for post: ${post.id}")
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] currentUser: ${currentUser?.id}, post.authorId: ${post.authorId}")
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] externalShare: $externalShare")
                    
                        // Option: Épingler/Désépingler - Phase F.2
                        // Afficher uniquement si l'utilisateur est l'auteur du post
                    if (currentUser != null && post.authorId == currentUser.id) {
                        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering Pin option")
                            PostOptionItem(
                                icon = Icons.Outlined.PushPin,
                                text = if (post.isPinned) "Désépingler" else "Épingler",
                                onClick = {
                                scope.launch {
                                    try {
                                        val updatedPost = if (post.isPinned) {
                                            api.unpinPost(post.id)
                                        } else {
                                            api.pinPost(post.id)
                                        }
                                        posts = posts.map { p ->
                                            if (p.id == post.id) updatedPost else p
                                        }
                                        FrollotLogger.success("API", "✅ Post épinglé/désépinglé: ${post.id}")
                                    } catch (e: Exception) {
                                        FrollotLogger.error("API", "❌ Erreur lors de l'épinglage: ${e.message}")
                                    }
                                }
                                postIdWithOpenMenu = null
                                postWithOpenMenu = null
                                menuButtonPosition = null
                                }
                            )
                            PostOptionsDivider()
                    } else {
                        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Pin option not shown (user is not author)")
                        }

                        // Option: Enregistrer dans une collection
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering Save option")
                        PostOptionItem(
                            icon = Icons.Outlined.BookmarkAdd,
                            text = stringResource(Strings.SocialFeed.Save),
                                onClick = {
                            // Ouvrir le dialog de sélection de collection
                            if (currentUser != null) {
                                postToAddToCollection = post.id
                                showCollectionDialog = true
                                scope.launch {
                                    try {
                                        isLoadingCollections = true
                                        userCollections = api.getCollectionsByUser(currentUser.id, includePrivate = true)
                                    } catch (e: Exception) {
                                        FrollotLogger.error("API", "❌ Erreur lors du chargement des collections: ${e.message}")
                                    } finally {
                                        isLoadingCollections = false
                                    }
                                }
                            }
                            postIdWithOpenMenu = null
                            postWithOpenMenu = null
                            menuButtonPosition = null
                                }
                        )

                        // Option: Archiver
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering Archive option")
                        PostOptionItem(
                            icon = Icons.Outlined.Archive,
                            text = stringResource(Strings.SocialFeed.Archive),
                                onClick = {
                            scope.launch {
                                try {
                                    api.archivePost(post.id)
                                    posts = posts.filter { it.id != post.id }
                                    FrollotLogger.success("API", "✅ Post archivé: ${post.id}")
                                } catch (e: Exception) {
                                    FrollotLogger.error("API", "❌ Erreur lors de l'archivage: ${e.message}")
                                }
                            }
                            postIdWithOpenMenu = null
                            postWithOpenMenu = null
                            menuButtonPosition = null
                        }
                    )

                    // Option: Partager dans l'app
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering Share in app option")
                        PostOptionItem(
                            icon = Icons.Outlined.IosShare,
                            text = stringResource(Strings.SocialFeed.ShareInApp),
                                onClick = {
                            scope.launch {
                                try {
                                    val postToShare = posts.find { it.id == post.id }
                                    if (postToShare != null) {
                                        if (postToShare.isSharedByCurrentUser) {
                                            api.unsharePost(post.id)
                                        } else {
                                            api.sharePost(post.id, "")
                                        }
                                        loadFeed(reset = false, showLoading = false)
                                    }
                                } catch (e: Exception) {
                                    FrollotLogger.error("API", "❌ Erreur lors du partage: ${e.message}")
                                }
                            }
                            postIdWithOpenMenu = null
                            postWithOpenMenu = null
                            menuButtonPosition = null
                                }
                        )
                        
                        // Phase I.2 - Partage Externe
                    if (externalShare != null) {
                        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering External Share option")
                            PostOptionItem(
                                icon = Icons.Default.Share,
                                text = stringResource(Strings.SocialFeed.ShareExternal),
                                onClick = {
                                // Fermer le menu et l'overlay AVANT d'ouvrir le dialogue
                                postIdWithOpenMenu = null
                                postWithOpenMenu = null
                                menuButtonPosition = null
                                // Utiliser un délai pour s'assurer que l'overlay est fermé avant d'ouvrir le dialogue
                                scope.launch {
                                    delay(50) // Petit délai pour laisser l'overlay se fermer
                                    postToShareExternally = post
                                    showExternalShareDialog = true
                                }
                            }
                        )
                    } else {
                        FrollotLogger.debug("API", "🔍 [SocialFeedScreen] External Share option not shown (externalShare is null)")
                        }

                        PostOptionsDivider()

                        // Option: Signaler - Phase H.1
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Rendering Report option")
                        PostOptionItem(
                            icon = Icons.Outlined.Report,
                            text = stringResource(Strings.SocialFeed.Report),
                            isDanger = true,
                            onClick = {
                            // Fermer le menu et l'overlay AVANT de naviguer
                            postIdWithOpenMenu = null
                            postWithOpenMenu = null
                            menuButtonPosition = null
                            // Appeler directement - l'overlay devrait se fermer immédiatement
                            onNavigateToReport?.invoke(ReportedEntityType.POST, post.id)
                        }
                    )
                    
                    FrollotLogger.debug("API", "🔍 [SocialFeedScreen] Finished rendering all menu items")
                }
            }
        }

        // Phase I.2 - Dialog de partage externe (AlertDialog s'affiche automatiquement au-dessus de tout)
        // LaunchedEffect pour fermer le menu avant d'ouvrir le dialogue
        LaunchedEffect(showExternalShareDialog) {
            if (showExternalShareDialog) {
                // Fermer le menu et l'overlay avant d'ouvrir le dialogue
                postIdWithOpenMenu = null
            }
        }
        
        if (showExternalShareDialog && postToShareExternally != null) {
            ExternalShareDialog(
                postId = postToShareExternally!!.id,
                postContent = postToShareExternally!!.content,
                authorName = postToShareExternally!!.authorName,
                imageUrl = postToShareExternally!!.imageUrl ?: postToShareExternally!!.media.firstOrNull()?.mediaUrl,
                externalShare = externalShare,
                onDismiss = {
                    showExternalShareDialog = false
                    postToShareExternally = null
                },
                onShareSuccess = {
                    // Optionnel : incrémenter le compteur de partages
                }
            )
        }
        
    }
    
    // Viewer d'images en plein écran - RENDU EN DEHORS DU SCAFFOLD pour couvrir tout l'écran
    if (showImageViewer && imageViewerImages.isNotEmpty()) {
        FullScreenImageViewer(
            images = imageViewerImages,
            initialIndex = imageViewerInitialIndex,
            onDismiss = { 
                showImageViewer = false
                imageViewerImages = emptyList()
            }
        )
    }
}

