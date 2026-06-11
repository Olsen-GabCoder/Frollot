@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.navigation.Route
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran affichant les détails d'une collection et ses posts.
 * Phase F.1 - Collections Thématiques
 */
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit,
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToCoiffeur: (String) -> Unit = {},
    onNavigateToSalon: (String) -> Unit = {}
) {
    var collection by remember { mutableStateOf<CollectionResponse?>(null) }
    var posts by remember { mutableStateOf<List<CollectionPostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingPosts by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadCollection() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                collection = api.getCollectionById(collectionId)
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement de la collection: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadPosts(page: Int = 0, append: Boolean = false) {
        scope.launch {
            try {
                if (append) {
                    isLoadingPosts = true
                } else {
                    isLoading = true
                }
                errorMessage = null
                val response = api.getCollectionPosts(collectionId, page = page, size = 20)
                if (append) {
                    posts = posts + response.content
                } else {
                    posts = response.content
                }
                hasMore = !response.last
                currentPage = page
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement des posts: ${e.message}"
            } finally {
                isLoading = false
                isLoadingPosts = false
            }
        }
    }

    LaunchedEffect(collectionId) {
        loadCollection()
        loadPosts()
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = collection?.name ?: stringResource(Strings.CollectionDetail.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        when {
            isLoading && collection == null -> {
                ListLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            errorMessage != null && collection == null -> {
                ListErrorState(
                    message = errorMessage!!,
                    onRetry = { loadCollection(); loadPosts() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            collection != null -> {
                val coll = collection!!
                val isOwner = currentUser?.id == coll.userId

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // En-tête de la collection
                    item {
                        CollectionHeader(
                            collection = coll,
                            isOwner = isOwner,
                            api = api,
                            onMenuClick = { showMenu = true },
                            onCollectionUpdated = {
                                loadCollection()
                                loadPosts()
                            },
                            onDelete = {
                                onBack()
                            }
                        )
                    }

                    // Liste des posts
                    if (posts.isEmpty() && !isLoadingPosts) {
                        item {
                            ListEmptyState(
                                title = "Aucun post dans cette collection",
                                message = null,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Collections,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        items(posts, key = { it.id }) { collectionPost ->
                            CollectionPostItem(
                                collectionPost = collectionPost,
                                isOwner = isOwner,
                                api = api,
                                onPostClick = { onNavigateToPost(collectionPost.post.id) },
                                onAuthorClick = {
                                    when (collectionPost.post.authorUserType) {
                                        UserType.hairstylist -> onNavigateToCoiffeur(collectionPost.post.authorId)
                                        else -> {} // Pas de navigation pour les autres types pour l'instant
                                    }
                                },
                                onCommentClick = { onNavigateToComments(collectionPost.post.id) },
                                onRemoveFromCollection = {
                                    scope.launch {
                                        try {
                                            api.removePostFromCollection(collectionId, collectionPost.post.id)
                                            loadPosts()
                                        } catch (e: Exception) {
                                            errorMessage = "Erreur lors de la suppression: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }

                        // Charger plus
                        if (hasMore && !isLoadingPosts) {
                            item {
                                PrimaryButton(
                                    text = stringResource(Strings.CollectionDetail.LoadMore),
                                    onClick = { loadPosts(currentPage + 1, append = true) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }

                        if (isLoadingPosts) {
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
    }

    // Menu d'options (modifier, supprimer)
    if (showMenu && collection != null) {
        val coll = collection!!
        if (currentUser?.id == coll.userId) {
            var showDeleteDialog by remember { mutableStateOf(false) }
            var showEditDialog by remember { mutableStateOf(false) }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Strings.CollectionDetail.Edit)) },
                    onClick = {
                        showMenu = false
                        showEditDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Strings.CollectionDetail.Delete)) },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(Strings.CollectionDetail.DeleteCollection)) },
                    text = { Text(stringResource(Strings.CollectionDetail.DeleteCollectionMessage)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        api.deleteCollection(coll.id)
                                        showDeleteDialog = false
                                        onBack()
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur lors de la suppression: ${e.message}"
                                        showDeleteDialog = false
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(Strings.Common.Delete), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(Strings.Common.Cancel))
                        }
                    }
                )
            }

            // TODO: Dialog d'édition (similaire à CreateCollectionDialog)
        }
    }
}

@Composable
private fun CollectionHeader(
    collection: CollectionResponse,
    isOwner: Boolean,
    api: FrollotApi,
    onMenuClick: () -> Unit,
    onCollectionUpdated: () -> Unit,
    onDelete: () -> Unit
) {
    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image de couverture
            if (collection.coverImageUrl != null) {
                AsyncImage(
                    model = collection.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        CollectionDetailCategoryBadge(category = collection.category)
                        if (!collection.isPublic) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Privé",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "par ${collection.userName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isOwner) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                }
            }

            if (collection.description != null) {
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${collection.postsCount} ${if (collection.postsCount > 1) "posts" else "post"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CollectionPostItem(
    collectionPost: CollectionPostResponse,
    isOwner: Boolean,
    api: FrollotApi,
    onPostClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRemoveFromCollection: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val post = collectionPost.post

    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onPostClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // En-tête avec auteur
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAuthorClick),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        user = User(
                            id = post.authorId,
                            email = "",
                            userType = post.authorUserType ?: UserType.client,
                            firstName = post.authorName.split(" ").firstOrNull(),
                            lastName = post.authorName.split(" ").drop(1).joinToString(" ").takeIf { it.isNotBlank() },
                            avatarUrl = null
                        ),
                        size = 40.dp
                    )
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (post.createdAt != null) {
                            Text(
                                text = formatCollectionDate(post.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isOwner) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Contenu du post
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Image si disponible
            if (post.imageUrl != null) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Actions (likes, commentaires)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.clickable(onClick = onCommentClick),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Menu pour retirer de la collection
    if (showMenu && isOwner) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Strings.CollectionDetail.RemoveFromCollection)) },
                onClick = {
                    showMenu = false
                    onRemoveFromCollection()
                },
                leadingIcon = {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun CollectionDetailCategoryBadge(category: CollectionCategory) {
    val (label, color) = when (category) {
        CollectionCategory.INSPIRATION -> "Inspiration" to MaterialTheme.colorScheme.primary
        CollectionCategory.PORTFOLIO -> "Portfolio" to MaterialTheme.colorScheme.secondary
        CollectionCategory.TENDANCE -> "Tendance" to MaterialTheme.colorScheme.tertiary
        CollectionCategory.PERSONNEL -> "Personnel" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatCollectionDate(dateString: String): String {
    // Format simple pour l'instant - peut être amélioré avec une vraie lib de date
    return try {
        dateString.substring(0, 10) // YYYY-MM-DD
    } catch (e: Exception) {
        dateString
    }
}

