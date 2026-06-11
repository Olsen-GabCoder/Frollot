package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.CommentResponse
import com.frollot.mobile.model.CreateCommentRequest
import com.frollot.mobile.model.PostResponse
import com.frollot.mobile.model.ReactionType
import com.frollot.mobile.model.ReportedEntityType
import com.frollot.mobile.model.TaggedType
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.localization.*
import com.frollot.mobile.localization.pluralizedString
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.FullScreenImageViewer
import com.frollot.mobile.model.PostMediaResponse
import com.frollot.mobile.ui.utils.AnimationSpecs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran de détail d'un post en plein écran.
 * 
 * Affiche :
 * - Le post complet avec toutes ses informations
 * - Les images en grand format avec swipe
 * - Tous les commentaires
 * - Les interactions (like, comment, share)
 * - Design premium cohérent avec le reste de l'application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    currentUser: User?,
    onBack: () -> Unit,
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToAuthor: (String, UserType?) -> Unit = { _, _ -> },
    onNavigateToCoiffeur: ((String) -> Unit)? = null,
    onNavigateToSalon: ((String) -> Unit)? = null,
    onNavigateToReport: ((ReportedEntityType, String) -> Unit)? = null
) {
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }
    var isSubmittingComment by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    
    // Menu de réactions
    var showReactionsMenu by remember { mutableStateOf(false) }
    var isLikeAnimating by remember { mutableStateOf(false) }
    var showLikeExplosion by remember { mutableStateOf(false) }
    
    // Viewer d'images en plein écran
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    val listState = rememberLazyListState()

    // Charger le post et les commentaires
    fun loadPostAndComments() {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                
                // Charger le post
                post = api.getPostById(postId)
                
                // Charger les commentaires (première page)
                val commentsPage = api.getCommentsByPost(postId, page = 0, size = 50)
                comments = commentsPage.content
                currentPage = 0
                hasMorePages = !commentsPage.last
                
                isLoading = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement post détail: ${e.message}")
                errorMessage = e.message
                hasError = true
                isLoading = false
            }
        }
    }

    // Charger plus de commentaires
    fun loadMoreComments() {
        if (isLoadingMore || !hasMorePages) return
        
        scope.launch {
            try {
                isLoadingMore = true
                val nextPage = currentPage + 1
                val commentsPage = api.getCommentsByPost(postId, page = nextPage, size = 50)
                comments = comments + commentsPage.content
                currentPage = nextPage
                hasMorePages = !commentsPage.last
                isLoadingMore = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement plus de commentaires: ${e.message}")
                isLoadingMore = false
            }
        }
    }

    // Ajouter un commentaire
    fun submitComment() {
        if (newCommentText.isBlank() || currentUser == null || isSubmittingComment) return
        
        scope.launch {
            try {
                isSubmittingComment = true
                val request = CreateCommentRequest(
                    postId = postId,
                    authorId = currentUser.id,
                    content = newCommentText.trim()
                )
                val newComment = api.createComment(postId, request)
                comments = listOf(newComment) + comments
                newCommentText = ""
                
                // Mettre à jour le compteur de commentaires
                post = post?.copy(commentsCount = (post?.commentsCount ?: 0) + 1)
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur ajout commentaire: ${e.message}")
            } finally {
                isSubmittingComment = false
            }
        }
    }

    // Charger au démarrage
    LaunchedEffect(postId) {
        loadPostAndComments()
    }

    // Fermer le menu de réactions après 3 secondes
    LaunchedEffect(showReactionsMenu) {
        if (showReactionsMenu) {
            delay(3000)
            showReactionsMenu = false
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = "Publication",
                onBackClick = onBack,
                onNavigateToProfile = {},
                showAvatar = false
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ListLoadingState()
                }
            }
            hasError -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ListErrorState(
                        message = errorMessage ?: "Erreur lors du chargement",
                        onRetry = { loadPostAndComments() }
                    )
                }
            }
            post == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ListEmptyState(
                        title = "Publication introuvable",
                        message = "Cette publication n'existe pas ou a été supprimée"
                    )
                }
            }
            else -> {
                val currentPost = post!!
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Post principal
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Utiliser UltraPremiumPostCard pour afficher le post
                            // Mais en mode détail (sans padding horizontal, avec toutes les interactions)
                            UltraPremiumPostCard(
                                post = currentPost,
                                comments = comments.take(3), // Afficher les 3 premiers commentaires
                                currentUser = currentUser,
                                onPostClick = { postId ->
                                    // Ouvrir le viewer d'images si le post a des médias
                                    val postMedia = currentPost.media
                                    if (postMedia.isNotEmpty()) {
                                        imageViewerInitialIndex = 0
                                        showImageViewer = true
                                    } else if (currentPost.imageUrl != null) {
                                        // Pour les posts avec imageUrl uniquement, créer une liste de médias temporaire
                                        imageViewerInitialIndex = 0
                                        showImageViewer = true
                                    }
                                },
                                onLikeClick = { postId ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.toggleLike(postId)
                                            post = updatedPost
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur like: ${e.message}")
                                        }
                                    }
                                },
                                onReactionClick = { postId, reactionType ->
                                    scope.launch {
                                        try {
                                            val currentReaction = currentPost.currentUserReaction
                                            val updatedPost = if (currentReaction == reactionType) {
                                                api.removeReaction(postId)
                                            } else {
                                                api.addReaction(postId, reactionType)
                                            }
                                            post = updatedPost
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur réaction: ${e.message}")
                                        }
                                    }
                                },
                                onCommentClick = { postId ->
                                    onNavigateToComments(postId)
                                },
                                onShareClick = { postId ->
                                    scope.launch {
                                        try {
                                            val postToShare = post
                                            if (postToShare != null) {
                                                if (postToShare.isSharedByCurrentUser) {
                                                    api.unsharePost(postId)
                                                } else {
                                                    api.sharePost(postId, "")
                                                }
                                                post = api.getPostById(postId)
                                            }
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur partage: ${e.message}")
                                        }
                                    }
                                },
                                onAuthorClick = { authorId ->
                                    val authorUserType = post?.authorUserType
                                    onNavigateToAuthor(authorId, authorUserType)
                                },
                                onArchiveClick = { postId ->
                                    scope.launch {
                                        try {
                                            api.archivePost(postId)
                                            onBack() // Retourner après archivage
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur archivage: ${e.message}")
                                        }
                                    }
                                },
                                onSaveToCollection = { postId ->
                                    // TODO: Ouvrir le dialogue de sélection de collection
                                },
                                onTagClick = { taggedType, taggedId ->
                                    when (taggedType) {
                                        TaggedType.salon -> {
                                            onNavigateToSalon?.invoke(taggedId)
                                        }
                                        TaggedType.user -> {
                                            onNavigateToCoiffeur?.invoke(taggedId)
                                        }
                                    }
                                },
                                onReportClick = { postId ->
                                    onNavigateToReport?.invoke(ReportedEntityType.POST, postId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Séparateur
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }

                    // Section commentaires
                    item {
                        Text(
                            text = pluralizedString(
                                Strings.PostDetail.Comments,
                                currentPost.commentsCount,
                                replaceCount = true
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Liste des commentaires
                    if (comments.isEmpty()) {
                        item {
                            ListEmptyState(
                                title = "Aucun commentaire",
                                message = "Soyez le premier à commenter !",
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        items(
                            items = comments,
                            key = { it.id }
                        ) { comment ->
                            CommentItem(
                                comment = comment,
                                currentUser = currentUser,
                                onAuthorClick = { authorId, _ ->
                                    // Pour les commentaires, on n'a pas le type d'utilisateur
                                    onNavigateToAuthor(authorId, null)
                                },
                                onDeleteClick = { commentId ->
                                    scope.launch {
                                        try {
                                            api.deleteComment(commentId)
                                            comments = comments.filter { it.id != commentId }
                                            post = post?.copy(commentsCount = (post?.commentsCount ?: 1) - 1)
                                        } catch (e: Exception) {
                                            FrollotLogger.error("API", "❌ Erreur suppression commentaire: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Charger plus de commentaires
                    if (hasMorePages && !isLoadingMore) {
                        item {
                            LaunchedEffect(Unit) {
                                loadMoreComments()
                            }
                        }
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    // Champ de commentaire en bas
                    if (currentUser != null) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StandardTextField(
                                        value = newCommentText,
                                        onValueChange = { newCommentText = it },
                                        placeholder = { Text(stringResource(Strings.UltraPremiumPostCard.AddComment)) },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isSubmittingComment
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    IconButton(
                                        onClick = { submitComment() },
                                        enabled = newCommentText.isNotBlank() && !isSubmittingComment
                                    ) {
                                        if (isSubmittingComment) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Envoyer",
                                                tint = if (newCommentText.isNotBlank()) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                }
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
        
    }
    
    // Viewer d'images en plein écran - RENDU EN DEHORS DU SCAFFOLD pour couvrir tout l'écran
    val currentPostForViewer = post
    if (showImageViewer && currentPostForViewer != null) {
        val imagesToShow = if (currentPostForViewer.media.isNotEmpty()) {
            currentPostForViewer.media.sortedBy { it.orderIndex }
        } else {
            val imageUrl = currentPostForViewer.imageUrl
            if (imageUrl != null) {
                // Créer un PostMediaResponse temporaire pour l'image principale
                listOf(
                    PostMediaResponse(
                        id = currentPostForViewer.id,
                        mediaUrl = imageUrl,
                        mediaType = com.frollot.mobile.model.PostMediaType.after,
                        orderIndex = 0,
                        mediaTypeLabel = "Image"
                    )
                )
            } else {
                emptyList()
            }
        }
        
        if (imagesToShow.isNotEmpty()) {
            FullScreenImageViewer(
                images = imagesToShow,
                initialIndex = imageViewerInitialIndex,
                onDismiss = { showImageViewer = false }
            )
        }
    }
}

/**
 * Composant pour afficher un commentaire dans la liste
 */
@Composable
fun CommentItem(
    comment: CommentResponse,
    currentUser: User?,
    onAuthorClick: (String, UserType?) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier.clickable { onAuthorClick(comment.authorId, null) }
        ) {
            UserAvatar(
                user = User(
                    id = comment.authorId,
                    email = comment.authorEmail ?: "",
                    userType = com.frollot.mobile.model.UserType.client,
                    firstName = comment.authorName.split(" ").firstOrNull(),
                    lastName = comment.authorName.split(" ").drop(1).joinToString(" ")
                ),
                size = 40.dp
            )
        }

        // Contenu du commentaire
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onAuthorClick(comment.authorId, null) }
                )
                
                Text(
                    text = comment.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bouton supprimer (si l'utilisateur est l'auteur)
        if (currentUser != null && comment.authorId == currentUser.id) {
            IconButton(
                onClick = { onDeleteClick(comment.id) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

