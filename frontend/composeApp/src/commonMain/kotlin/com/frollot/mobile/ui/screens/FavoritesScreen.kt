package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.CommentResponse
import com.frollot.mobile.model.PostResponse
import com.frollot.mobile.model.ReactionType
import com.frollot.mobile.model.TaggedType
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.ExternalShareDialog
import com.frollot.mobile.ui.components.PullToRefreshBox
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.navigation.Route
import com.frollot.mobile.ui.utils.rememberExternalShare
import com.frollot.mobile.ui.utils.rememberFavoritesDataStore
import com.frollot.mobile.ui.utils.rememberNetworkMonitor
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran affichant les posts favoris d'un utilisateur.
 */
@Composable
fun FavoritesScreen(
    userId: String,
    currentUser: User,
    api: FrollotApi,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    var favorites by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    
    // Phase I.2 - Partage Externe
    val externalShare = rememberExternalShare()
    var showExternalShareDialog by remember { mutableStateOf(false) }
    var postToShareExternally by remember { mutableStateOf<PostResponse?>(null) }
    
    // Phase I.4 - Mode Offline Basique
    val favoritesDataStore = rememberFavoritesDataStore()
    val networkMonitor = rememberNetworkMonitor()
    var networkState by remember { mutableStateOf(com.frollot.mobile.model.NetworkState(isOnline = true)) }
    var isOfflineMode by remember { mutableStateOf(false) }
    
    // Phase I.1 - Pull-to-refresh et Infinite Scroll
    // Phase I.4 - Mode Offline Basique : Charge depuis le cache d'abord, puis depuis l'API si online
    fun loadFavorites(reset: Boolean = false, forceFromApi: Boolean = false) {
        scope.launch {
            try {
                if (reset) {
                    currentPage = 0
                    hasMore = true
                    isLoading = true
                    isRefreshing = true
                } else {
                    isLoadingMore = true
                }
                errorMessage = null
                
                // Phase I.4 - Charger depuis le cache d'abord (seulement si reset et pas forceFromApi)
                if (reset && !forceFromApi) {
                    val cachedFavorites = favoritesDataStore.loadFavorites(userId)
                    if (cachedFavorites != null && cachedFavorites.isNotEmpty()) {
                        favorites = cachedFavorites
                        isLoading = false
                        isRefreshing = false
                    }
                }
                
                // Si online, charger depuis l'API
                val isOnline = networkMonitor?.getNetworkState()?.isOnline ?: true
                if (isOnline) {
                    val page = if (reset) 0 else currentPage
                    val response = api.getFavoritesByUser(userId, page = page, size = 20)
                    if (reset) {
                        favorites = response.content
                        // Phase I.4 - Sauvegarder dans le cache
                        favoritesDataStore.saveFavorites(userId, response.content)
                    } else {
                        favorites = favorites + response.content
                        // Phase I.4 - Sauvegarder toutes les favoris dans le cache (limiter à 100)
                        val allFavorites = favorites.take(100)
                        favoritesDataStore.saveFavorites(userId, allFavorites)
                    }
                    hasMore = !response.last && response.content.isNotEmpty()
                    if (!reset && response.content.isNotEmpty()) {
                        currentPage = currentPage + 1
                    }
                    isOfflineMode = false
                } else {
                    // Si offline et pas de cache, afficher un message d'erreur
                    if (favorites.isEmpty() && reset) {
                        errorMessage = "Mode hors ligne. Aucune donnée en cache disponible."
                        isOfflineMode = true
                    }
                }
            } catch (e: Exception) {
                // Si erreur et qu'on a des données en cache, continuer avec le cache
                if (favorites.isEmpty()) {
                    val cachedFavorites = favoritesDataStore.loadFavorites(userId)
                    if (cachedFavorites != null && cachedFavorites.isNotEmpty()) {
                        favorites = cachedFavorites
                        isOfflineMode = true
                        errorMessage = "Mode hors ligne. Données en cache affichées."
                    } else {
                        errorMessage = "Erreur lors du chargement des favoris: ${e.message}"
                        isOfflineMode = true
                    }
                } else {
                    errorMessage = "Erreur lors de la synchronisation: ${e.message}"
                }
            } finally {
                isLoading = false
                isRefreshing = false
                isLoadingMore = false
            }
        }
    }
    
    // Observer les changements de connectivité
    LaunchedEffect(networkMonitor) {
        networkMonitor?.observeNetworkState()?.collect { state ->
            networkState = state
            isOfflineMode = !state.isOnline
            
            // Si on revient en ligne et qu'on a des données en cache, synchroniser
            if (state.isOnline && favorites.isNotEmpty()) {
                loadFavorites(reset = true, forceFromApi = true)
            }
        } ?: run {
            // Si networkMonitor est null, supposer qu'on est online
            networkState = com.frollot.mobile.model.NetworkState(isOnline = true)
        }
    }
    
    // Phase I.3 - Retirer un favori
    // Phase I.4 - Mode Offline Basique : Mettre à jour le cache après modification
    fun unfavoritePost(postId: String) {
        scope.launch {
            try {
                // Optimistic update : retirer de la liste locale immédiatement
                favorites = favorites.filter { it.id != postId }
                // Phase I.4 - Mettre à jour le cache
                favoritesDataStore.saveFavorites(userId, favorites)
                
                // Si online, appeler l'API pour retirer le favori
                val isOnline = networkMonitor?.getNetworkState()?.isOnline ?: true
                if (isOnline) {
                    api.toggleFavorite(postId)
                } else {
                    // Si offline, marquer comme nécessitant une synchronisation
                    errorMessage = "Modification enregistrée localement. Synchronisation au retour en ligne."
                }
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur lors du retrait du favori: ${e.message}")
                // En cas d'erreur, rafraîchir la liste
                errorMessage = "Erreur lors du retrait du favori"
                loadFavorites(reset = false)
            }
        }
    }

    LaunchedEffect(userId) {
        loadFavorites(reset = true)
    }
    
    // Phase I.1 - Détecter le scroll pour charger plus (infinite scroll)
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= favorites.size - 3 && 
                    hasMore && 
                    !isLoadingMore && 
                    !isLoading &&
                    !isRefreshing) {
                    loadFavorites(reset = false)
                }
            }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.Favorites.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Phase I.4 - Indicateur de mode offline
            if (isOfflineMode) {
                StandardCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalWifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Strings.Favorites.OfflineMode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (isLoading && favorites.isEmpty()) {
                ListLoadingState(
                    message = "Chargement de vos favoris..."
                )
            } else if (errorMessage != null && favorites.isEmpty()) {
                ListErrorState(
                    message = errorMessage!!,
                    onRetry = { loadFavorites(reset = true) }
                )
            } else if (favorites.isEmpty()) {
                ListEmptyState(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    title = "Aucun favori",
                    message = "Les posts que vous mettez en favori apparaîtront ici"
                )
            } else {
                // Phase I.1 - Pull-to-refresh et Infinite Scroll
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { loadFavorites(reset = true) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(favorites, key = { it.id }) { post ->
                            UltraPremiumPostCard(
                                post = post,
                                comments = emptyList<CommentResponse>(), // Phase I.3 - Commentaires non chargés pour simplifier
                                currentUser = currentUser,
                                onTagClick = { taggedType: TaggedType, taggedId: String ->
                                    // Phase E - Navigation vers les profils selon le type
                                    when (taggedType) {
                                        TaggedType.salon -> {
                                            onNavigate(Route.SalonSocialProfile(salonId = taggedId))
                                        }
                                        TaggedType.user -> {
                                            // On suppose que c'est un coiffeur si tagué
                                            onNavigate(Route.CoiffeurProfile(coiffeurId = taggedId))
                                        }
                                    }
                                },
                                onLikeClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            // Optimistic update
                                            favorites = favorites.map { p ->
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
                                            val updatedPost = api.toggleLike(postId)
                                            // Mettre à jour avec les données du serveur
                                            favorites = favorites.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur lors du like: ${e.message}")
                                            // En cas d'erreur, rafraîchir
                                            loadFavorites(reset = false)
                                        }
                                    }
                                },
                                // Phase D.4 - Réactions Spécialisées
                                onReactionClick = { postId: String, reactionType: ReactionType ->
                                    val currentPost = favorites.find { it.id == postId }
                                    val currentReaction = currentPost?.currentUserReaction
                                    
                                    scope.launch {
                                        try {
                                            if (currentReaction == reactionType) {
                                                // Si l'utilisateur clique sur sa réaction actuelle, la supprimer
                                                val updatedPost = api.removeReaction(postId)
                                                favorites = favorites.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            } else {
                                                // Ajouter/modifier la réaction
                                                val updatedPost = api.addReaction(postId, reactionType)
                                                favorites = favorites.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur lors de la réaction: ${e.message}")
                                            loadFavorites(reset = false)
                                        }
                                    }
                                },
                                onCommentClick = { postId: String ->
                                    onNavigate(Route.Comments(postId = postId))
                                },
                                onShareClick = { postId: String ->
                                    // Phase D.3 - Partage de Posts (simplifié, pas de dialog)
                                    scope.launch {
                                        try {
                                            val post = favorites.find { it.id == postId }
                                            if (post != null && !post.isSharedByCurrentUser) {
                                                // Optimistic update
                                                favorites = favorites.map { p ->
                                                    if (p.id == postId) {
                                                        p.copy(
                                                            isSharedByCurrentUser = true,
                                                            sharesCount = p.sharesCount + 1
                                                        )
                                                    } else {
                                                        p
                                                    }
                                                }
                                                val updatedPost = api.sharePost(postId, null)
                                                favorites = favorites.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur lors du partage: ${e.message}")
                                        }
                                    }
                                },
                                onAuthorClick = { authorId: String ->
                                    // Phase E - Navigation selon le type d'utilisateur
                                    val authorUserType = post.authorUserType
                                    when (authorUserType) {
                                        UserType.hairstylist -> {
                                            onNavigate(Route.CoiffeurProfile(coiffeurId = authorId))
                                        }
                                        UserType.salon_owner -> {
                                            // Pour l'instant, ne rien faire
                                        }
                                        UserType.client -> {
                                            // Pour l'instant, ne rien faire
                                        }
                                        null -> {
                                            // Type inconnu : essayer le profil coiffeur par défaut
                                            onNavigate(Route.CoiffeurProfile(coiffeurId = authorId))
                                        }
                                        else -> {
                                            // Autres types : ne rien faire
                                        }
                                    }
                                },
                                onArchiveClick = { postId: String ->
                                    // Phase I.3 - Archiver depuis les favoris
                                    scope.launch {
                                        try {
                                            api.archivePost(postId)
                                            // Retirer le post de la liste locale
                                            favorites = favorites.filter { it.id != postId }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur lors de l'archivage: ${e.message}")
                                        }
                                    }
                                },
                                onSaveToCollection = { postId: String ->
                                    // Phase I.3 - Retirer le favori si déjà favori
                                    unfavoritePost(postId)
                                },
                                // Phase F.2 - Posts Épinglés
                                onPinClick = { postId: String ->
                                    val post = favorites.find { it.id == postId }
                                    if (currentUser.id == post?.authorId) {
                                        scope.launch {
                                            try {
                                                val updatedPost = if (post.isPinned) {
                                                    api.unpinPost(postId)
                                                } else {
                                                    api.pinPost(postId)
                                                }
                                                favorites = favorites.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            } catch (e: Exception) {
                                                FrollotLogger.error("API", "❌ Erreur lors de l'épinglage: ${e.message}")
                                            }
                                        }
                                    }
                                },
                                // Phase H.1 - Signalement de Contenu (pour l'instant, ne rien faire)
                                onReportClick = null,
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
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Phase I.2 - Dialog de partage externe
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
}

