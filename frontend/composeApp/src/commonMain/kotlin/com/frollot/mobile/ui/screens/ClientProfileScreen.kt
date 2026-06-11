@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.VerificationBadge
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import kotlinx.datetime.Clock
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.rememberImagePicker
import com.frollot.mobile.ui.components.toImageBitmap
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import androidx.compose.ui.graphics.ImageBitmap
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.ui.components.profile.*
import com.frollot.mobile.model.UserType
import com.frollot.mobile.time.currentTimeMillis

/**
 * Écran de profil enrichi d'un client.
 * Phase E.4 - Profil Client
 * 
 * Affiche :
 * - Bio
 * - Statistiques (posts, likes, followers, collections, réservations)
 * - Collections
 * - Posts récents
 * - Badges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    clientId: String,
    currentUser: User? = null,
    onBack: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToCollection: (String) -> Unit = {},
    onNavigateToClient: (String) -> Unit = {},
    onFollowClick: ((String) -> Unit)? = null
) {
    var profile by remember { mutableStateOf<ClientProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // État local pour les posts récents (permet la mise à jour)
    var recentPosts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    
    // États pour les collections (sauvegarde de post)
    var showCollectionDialog by remember { mutableStateOf(false) }
    var selectedPostIdForCollection by remember { mutableStateOf<String?>(null) }
    var userCollections by remember { mutableStateOf<List<CollectionResponse>>(emptyList()) }
    var isLoadingCollections by remember { mutableStateOf(false) }

    // États pour la photo de couverture
    var selectedCoverImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var coverImagePreview by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isUploadingCoverImage by remember { mutableStateOf(false) }
    var coverImageErrorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    
    // Image picker pour la photo de couverture
    val coverImagePicker = com.frollot.mobile.ui.components.rememberImagePicker { bytes ->
        if (bytes != null) {
            selectedCoverImageBytes = bytes
            coverImagePreview = bytes.toImageBitmap()
            if (coverImagePreview == null) {
                coverImageErrorMessage = "Erreur lors du chargement de l'image"
            }
        }
    }
    
    // Fonction utilitaire pour mettre à jour un post dans la liste
    fun updatePostInList(posts: List<PostResponse>, updatedPost: PostResponse): List<PostResponse> {
        return posts.map { if (it.id == updatedPost.id) updatedPost else it }
    }

    // Charger le profil
    LaunchedEffect(clientId) {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                val loadedProfile = api.getClientProfile(clientId)
                profile = loadedProfile
                recentPosts = loadedProfile.recentPosts
            } catch (e: Exception) {
                hasError = true
                errorMessage = e.message ?: "Erreur lors du chargement du profil"
                FrollotLogger.error("API", "❌ Erreur chargement profil client: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = stringResource(Strings.ClientProfile.Title),
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                ListLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            hasError -> {
                ListErrorState(
                    message = errorMessage ?: "Impossible de charger le profil",
                    onRetry = {
                        scope.launch {
                            try {
                                isLoading = true
                                hasError = false
                                profile = api.getClientProfile(clientId)
                            } catch (e: Exception) {
                                hasError = true
                                errorMessage = e.message
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            profile != null -> {
                val p = profile!!
                val isOwner = currentUser?.id == clientId
                
                // Dialogue de sélection de collection
                if (showCollectionDialog) {
                    CollectionSelectionDialog(
                        collections = userCollections,
                        isLoading = isLoadingCollections,
                        onCollectionSelected = { collectionId ->
                            scope.launch {
                                try {
                                    selectedPostIdForCollection?.let { postId ->
                                        api.addPostToCollection(collectionId, postId)
                                        FrollotLogger.debug("ClientProfile", "Post ajouté à la collection")
                                    }
                                } catch (e: Exception) {
                                    FrollotLogger.debug("ClientProfile", "Erreur ajout à collection: ${e.message}")
                                } finally {
                                    showCollectionDialog = false
                                    selectedPostIdForCollection = null
                                }
                            }
                        },
                        onDismiss = {
                            showCollectionDialog = false
                            selectedPostIdForCollection = null
                        }
                    )
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header avec photo de couverture, avatar et nom
                    item {
                        ProfileHeaderCard(
                            config = ProfileHeaderConfig(
                                id = p.id,
                                email = p.email,
                                firstName = p.firstName,
                                lastName = p.lastName,
                                avatarUrl = p.avatarUrl,
                                coverImageUrl = p.coverImageUrl,
                                isVerified = p.isVerified,
                                userType = UserType.client
                            ),
                            isOwner = isOwner,
                            isFollowed = p.isFollowedByCurrentUser ?: false,
                            onFollowClick = onFollowClick?.let { { it(clientId) } },
                            coverImagePreview = coverImagePreview,
                            isUploadingCoverImage = isUploadingCoverImage,
                            coverImageErrorMessage = coverImageErrorMessage,
                            onSelectCoverImage = { coverImagePicker.launch() },
                            onSaveCoverImage = {
                                selectedCoverImageBytes?.let { bytes ->
                                    scope.launch {
                                        try {
                                            isUploadingCoverImage = true
                                            coverImageErrorMessage = null
                                            val fileName = "cover_${clientId}_${currentTimeMillis()}.jpg"
                                            val coverImageUrl = api.uploadImage(bytes, fileName)
                                            api.updateUserCoverImage(clientId, coverImageUrl)
                                            profile = api.getClientProfile(clientId)
                                            selectedCoverImageBytes = null
                                            coverImagePreview = null
                                            isUploadingCoverImage = false
                                        } catch (e: Exception) {
                                            coverImageErrorMessage = "Erreur lors de l'upload: ${e.message}"
                                            isUploadingCoverImage = false
                                        }
                                    }
                                }
                            },
                            onCancelCoverImage = {
                                selectedCoverImageBytes = null
                                coverImagePreview = null
                                coverImageErrorMessage = null
                            },
                            stats = listOf(
                                ProfileStatConfig(
                                    value = p.statistics.postsCount.toString(),
                                    label = stringResource(Strings.ClientProfile.Posts)
                                ),
                                ProfileStatConfig(
                                    value = formatProfileNumber(p.statistics.totalLikes.toInt()),
                                    label = stringResource(Strings.ClientProfile.Likes)
                                ),
                                ProfileStatConfig(
                                    value = p.statistics.followersCount.toString(),
                                    label = stringResource(Strings.ClientProfile.Followers)
                                ),
                                ProfileStatConfig(
                                    value = p.statistics.followingCount.toString(),
                                    label = stringResource(Strings.ClientProfile.Following)
                                ),
                                ProfileStatConfig(
                                    value = p.statistics.collectionsCount.toString(),
                                    label = stringResource(Strings.ClientProfile.CollectionsCount)
                                ),
                                ProfileStatConfig(
                                    value = p.statistics.bookingsCount.toString(),
                                    label = "Réservations"
                                )
                            ),
                            followButtonText = FollowButtonText(
                                follow = stringResource(Strings.ClientProfile.Follow),
                                unfollow = stringResource(Strings.ClientProfile.Unfollow)
                            )
                        )
                    }

                    // Bio
                    if (p.bio != null) {
                        item {
                            ClientBioSection(bio = p.bio)
                        }
                    }

                    // Badges
                    if (p.badges.isNotEmpty()) {
                        item {
                            ProfileBadgesSection(
                                badges = p.badges,
                                title = stringResource(Strings.ClientProfile.Badges),
                                displayMode = BadgeDisplayMode.Compact
                            )
                        }
                    }

                    // Collections
                    if (p.collections.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Strings.ClientProfile.Collections),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(p.collections) { collection ->
                            CollectionCard(
                                collection = collection,
                                onClick = { onNavigateToCollection(collection.id) }
                            )
                        }
                    }

                    // Posts récents
                    if (recentPosts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Strings.ClientProfile.RecentPosts),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(recentPosts) { post ->
                            UltraPremiumPostCard(
                                post = post,
                                comments = emptyList(),
                                currentUser = currentUser,
                                onLikeClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.toggleLike(postId)
                                            recentPosts = updatePostInList(recentPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("ClientProfile", "Erreur toggle like: ${e.message}")
                                        }
                                    }
                                },
                                onReactionClick = { postId: String, reactionType: ReactionType ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.addReaction(postId, reactionType)
                                            recentPosts = updatePostInList(recentPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("ClientProfile", "Erreur réaction: ${e.message}")
                                        }
                                    }
                                },
                                onCommentClick = { postId: String ->
                                    onNavigateToComments(postId)
                                },
                                onShareClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            api.sharePost(postId)
                                            FrollotLogger.debug("ClientProfile", "Post partagé avec succès")
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("ClientProfile", "Erreur partage: ${e.message}")
                                        }
                                    }
                                },
                                onAuthorClick = { authorId: String ->
                                    onNavigateToClient(authorId)
                                },
                                onArchiveClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.archivePost(postId)
                                            recentPosts = updatePostInList(recentPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("ClientProfile", "Erreur archive: ${e.message}")
                                        }
                                    }
                                },
                                onSaveToCollection = { postId: String ->
                                    selectedPostIdForCollection = postId
                                    scope.launch {
                                        try {
                                            isLoadingCollections = true
                                            currentUser?.id?.let { userId ->
                                                userCollections = api.getCollectionsByUser(userId, includePrivate = true)
                                            }
                                            showCollectionDialog = true
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("ClientProfile", "Erreur chargement collections: ${e.message}")
                                        } finally {
                                            isLoadingCollections = false
                                        }
                                    }
                                },
                                onPostClick = { postId ->
                                    onNavigateToPost(postId)
                                },
                                onTagClick = { taggedType, taggedId ->
                                    when (taggedType) {
                                        com.frollot.mobile.model.TaggedType.salon -> {
                                            // TODO: Navigate to salon profile
                                        }
                                        com.frollot.mobile.model.TaggedType.user -> {
                                            onNavigateToClient(taggedId)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * Section bio du client.
 */
@Composable
private fun ClientBioSection(bio: String) {
    StandardCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Strings.ClientProfile.About),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



/**
 * Carte de collection.
 */
@Composable
internal fun CollectionCard(
    collection: CollectionResponse,
    onClick: () -> Unit
) {
    StandardCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Collections,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${collection.postsCount} ${if (collection.postsCount > 1) "posts" else "post"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

