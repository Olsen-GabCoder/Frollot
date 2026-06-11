package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.CommentResponse
import com.frollot.mobile.model.PostResponse
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran dédié pour afficher et gérer les commentaires d'un post.
 * 
 * Fonctionnalités :
 * - Affichage de tous les commentaires avec pagination
 * - Ajout de nouveaux commentaires
 * - Suppression de ses propres commentaires
 * - Design premium cohérent avec le reste de l'application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: String,
    currentUser: User?,
    onBack: () -> Unit,
    onCommentAdded: () -> Unit = {},
    onNavigateToAuthor: ((String, UserType?) -> Unit)? = null
) {
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }
    var isSubmittingComment by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }

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
                FrollotLogger.error("API", "❌ Erreur chargement commentaires: ${e.message}")
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
                FrollotLogger.error("API", "❌ Erreur chargement commentaires supplémentaires: ${e.message}")
                isLoadingMore = false
            }
        }
    }

    // Ajouter un nouveau commentaire
    fun submitComment() {
        if (newCommentText.isBlank() || currentUser == null || isSubmittingComment) return

        scope.launch {
            try {
                isSubmittingComment = true
                
                val request = com.frollot.mobile.model.CreateCommentRequest(
                    postId = postId,
                    authorId = currentUser.id,
                    content = newCommentText.trim()
                )
                
                val newComment = api.createComment(postId, request)
                
                // Ajouter le nouveau commentaire en haut de la liste
                comments = listOf(newComment) + comments
                
                // Réinitialiser le champ
                newCommentText = ""
                
                // Mettre à jour le compteur de commentaires du post
                post = post?.copy(commentsCount = (post?.commentsCount ?: 0) + 1)
                
                // Notifier le parent
                onCommentAdded()
                
                // Scroller vers le haut pour voir le nouveau commentaire
                listState.animateScrollToItem(0)
                
                isSubmittingComment = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur création commentaire: ${e.message}")
                errorMessage = "Erreur lors de l'ajout du commentaire: ${e.message}"
                isSubmittingComment = false
            }
        }
    }

    // Supprimer un commentaire
    fun deleteComment(commentId: String) {
        scope.launch {
            try {
                api.deleteComment(commentId)
                
                // Retirer le commentaire de la liste
                comments = comments.filter { it.id != commentId }
                
                // Mettre à jour le compteur
                post = post?.copy(commentsCount = (post?.commentsCount ?: 0) - 1)
                
                // Notifier le parent
                onCommentAdded()
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur suppression commentaire: ${e.message}")
                errorMessage = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    LaunchedEffect(postId) {
        loadPostAndComments()
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.Comments.Title),
                showAvatar = false
            )
        },
        bottomBar = {
            // Barre de saisie de commentaire en bas
            if (currentUser != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar de l'utilisateur
                        UserAvatar(
                            user = currentUser,
                            size = 40.dp
                        )
                        
                        // Champ de texte
                        StandardTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(Strings.Comments.AddComment)) },
                            maxLines = 4,
                            enabled = !isSubmittingComment
                        )
                        
                        // Bouton d'envoi
                        IconButton(
                            onClick = { submitComment() },
                            enabled = newCommentText.isNotBlank() && !isSubmittingComment,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = if (newCommentText.isNotBlank() && !isSubmittingComment) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                )
                        ) {
                            if (isSubmittingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Envoyer",
                                    tint = if (newCommentText.isNotBlank()) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                // État de chargement
                ListLoadingState(
                    message = "Chargement des commentaires...",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            hasError -> {
                // État d'erreur
                ListErrorState(
                    message = errorMessage ?: "Impossible de charger les commentaires",
                    onRetry = { loadPostAndComments() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            else -> {
                // Affichage des commentaires
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Aperçu du post en haut
                    post?.let { postData ->
                        item {
                            PostPreviewCard(
                                post = postData,
                                onAuthorClick = { authorId ->
                                    val authorUserType = postData.authorUserType
                                    onNavigateToAuthor?.invoke(authorId, authorUserType)
                                }
                            )
                        }
                        
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                    
                    // Liste des commentaires
                    if (comments.isEmpty()) {
                        item {
                            ListEmptyState(
                                icon = {
                                    Icon(
                                        Icons.Outlined.Comment,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                title = "Aucun commentaire",
                                message = "Soyez le premier à commenter !"
                            )
                        }
                    } else {
                        items(
                            items = comments,
                            key = { it.id }
                        ) { comment ->
                            CommentCard(
                                comment = comment,
                                currentUserId = currentUser?.id,
                                onDelete = { deleteComment(comment.id) },
                                onAuthorClick = { authorId ->
                                    // Pour les commentaires, on n'a pas le type d'utilisateur
                                    onNavigateToAuthor?.invoke(authorId, null)
                                }
                            )
                        }
                        
                        // Charger plus si nécessaire
                        if (hasMorePages) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    } else {
                                        TextButton(onClick = { loadMoreComments() }) {
                                            Text(stringResource(Strings.Comments.LoadMore))
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
}

/**
 * Carte d'aperçu du post en haut de l'écran de commentaires.
 */
@Composable
fun PostPreviewCard(
    post: PostResponse,
    onAuthorClick: ((String) -> Unit)? = null
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar de l'auteur
            Box(
                modifier = Modifier.clickable(enabled = onAuthorClick != null) {
                    onAuthorClick?.invoke(post.authorId)
                }
            ) {
                UserAvatar(
                    user = User(
                        id = post.authorId,
                        email = post.authorEmail,
                        userType = UserType.client, // Valeur par défaut pour l'affichage
                        firstName = post.authorName.split(" ").firstOrNull(),
                        lastName = post.authorName.split(" ").drop(1).joinToString(" ")
                    ),
                    size = 40.dp
                )
            }
            
            // Contenu du post
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    post.authorName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = onAuthorClick != null) {
                        onAuthorClick?.invoke(post.authorId)
                    }
                )
                Text(
                    post.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Carte d'affichage d'un commentaire.
 */
@Composable
fun CommentCard(
    comment: CommentResponse,
    currentUserId: String?,
    onDelete: () -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val isOwnComment = comment.authorId == currentUserId
    
    StandardCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar de l'auteur
            Box(
                modifier = Modifier.clickable(enabled = onAuthorClick != null) {
                    onAuthorClick?.invoke(comment.authorId)
                }
            ) {
                UserAvatar(
                    user = User(
                        id = comment.authorId,
                        email = comment.authorEmail,
                        userType = UserType.client, // Valeur par défaut pour l'affichage
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        comment.authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = onAuthorClick != null) {
                            onAuthorClick?.invoke(comment.authorId)
                        }
                    )
                    Text(
                        comment.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Bouton de suppression (si c'est notre commentaire)
            if (isOwnComment) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Supprimer",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

