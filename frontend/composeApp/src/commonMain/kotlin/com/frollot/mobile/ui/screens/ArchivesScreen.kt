package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.outlined.Archive
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
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.navigation.Route
import com.frollot.mobile.ui.utils.rememberExternalShare
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran affichant les posts archivés d'un utilisateur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivesScreen(
    userId: String,
    currentUser: User,
    api: FrollotApi,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    var archives by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
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

    // Phase I.1 - Pull-to-refresh et Infinite Scroll
    fun loadArchives(reset: Boolean = false) {
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
                val page = if (reset) 0 else currentPage
                val response = api.getArchivedPosts(userId, page = page, size = 20)
                if (reset) {
                    archives = response.content
                } else {
                    archives = archives + response.content
                }
                hasMore = !response.last && response.content.isNotEmpty()
                if (!reset && response.content.isNotEmpty()) {
                    currentPage = currentPage + 1
                }
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement des archives: ${e.message}"
            } finally {
                isLoading = false
                isRefreshing = false
                isLoadingMore = false
            }
        }
    }

    fun unarchivePost(postId: String) {
        scope.launch {
            try {
                api.unarchivePost(postId)
                // Retirer le post de la liste locale
                archives = archives.filter { it.id != postId }
            } catch (e: Exception) {
                errorMessage = "Erreur lors du désarchivage: ${e.message}"
            }
        }
    }

    LaunchedEffect(userId) {
        loadArchives(reset = true)
    }
    
    // Phase I.1 - Détecter le scroll pour charger plus (infinite scroll)
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= archives.size - 3 && 
                    hasMore && 
                    !isLoadingMore && 
                    !isLoading &&
                    !isRefreshing) {
                    loadArchives(reset = false)
                }
            }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.Archives.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && archives.isEmpty()) {
                ListLoadingState(
                    message = "Chargement de vos archives..."
                )
            } else if (errorMessage != null && archives.isEmpty()) {
                ListErrorState(
                    message = errorMessage!!,
                    onRetry = { loadArchives(reset = true) }
                )
            } else if (archives.isEmpty()) {
                ListEmptyState(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    title = "Aucune archive",
                    message = "Les posts que vous archivez apparaîtront ici"
                )
            } else {
                // Phase I.1 - Pull-to-refresh et Infinite Scroll
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { loadArchives(reset = true) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(archives, key = { it.id }) { post ->
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
                                            archives = archives.map { p ->
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
                                            archives = archives.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur lors du like: ${e.message}")
                                            // En cas d'erreur, rafraîchir
                                            loadArchives(reset = false)
                                        }
                                    }
                                },
                                // Phase D.4 - Réactions Spécialisées
                                onReactionClick = { postId: String, reactionType: ReactionType ->
                                    val currentPost = archives.find { it.id == postId }
                                    val currentReaction = currentPost?.currentUserReaction
                                    
                                    scope.launch {
                                        try {
                                            if (currentReaction == reactionType) {
                                                // Si l'utilisateur clique sur sa réaction actuelle, la supprimer
                                                val updatedPost = api.removeReaction(postId)
                                                archives = archives.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            } else {
                                                // Ajouter/modifier la réaction
                                                val updatedPost = api.addReaction(postId, reactionType)
                                                archives = archives.map { p ->
                                                    if (p.id == postId) updatedPost else p
                                                }
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur lors de la réaction: ${e.message}")
                                            loadArchives(reset = false)
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
                                            val post = archives.find { it.id == postId }
                                            if (post != null && !post.isSharedByCurrentUser) {
                                                // Optimistic update
                                                archives = archives.map { p ->
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
                                                archives = archives.map { p ->
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
                                    // Phase I.3 - Désarchiver depuis les archives
                                    unarchivePost(postId)
                                },
                                onSaveToCollection = { postId: String ->
                                    // Phase F.1 - Pour l'instant, ne rien faire (optionnel)
                                },
                                // Phase F.2 - Posts Épinglés
                                onPinClick = { postId: String ->
                                    val post = archives.find { it.id == postId }
                                    if (currentUser.id == post?.authorId) {
                                        scope.launch {
                                            try {
                                                val updatedPost = if (post.isPinned) {
                                                    api.unpinPost(postId)
                                                } else {
                                                    api.pinPost(postId)
                                                }
                                                archives = archives.map { p ->
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

