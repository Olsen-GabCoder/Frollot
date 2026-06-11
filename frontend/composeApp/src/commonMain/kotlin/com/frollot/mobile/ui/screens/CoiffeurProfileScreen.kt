@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.frollot.mobile.ui.components.VerificationBadge
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ImageBitmap
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.ui.components.rememberImagePicker
import com.frollot.mobile.ui.components.toImageBitmap
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.localization.*
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.ui.components.profile.*
import kotlinx.datetime.Clock
import com.frollot.mobile.time.currentTimeMillis
import com.frollot.mobile.model.UserType


/**
 * Écran de profil enrichi d'un coiffeur.
 * Phase E.1 - Profil Coiffeur Enrichi
 * 
 * Affiche :
 * - Bio et spécialités
 * - Portfolio mis en avant
 * - Statistiques (posts, likes, followers)
 * - Portfolios
 * - Posts récents
 * - Badges et certifications (Phase E.3)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoiffeurProfileScreen(
    coiffeurId: String,
    currentUser: User? = null,
    onBack: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToPortfolio: (String) -> Unit = {},
    onNavigateToCoiffeur: (String) -> Unit = {},
    onFollowClick: ((String) -> Unit)? = null
) {
    var profile by remember { mutableStateOf<CoiffeurProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Phase F.2 - Posts épinglés
    var pinnedPosts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoadingPinnedPosts by remember { mutableStateOf(false) }
    
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

    // Charger le profil et les posts épinglés
    LaunchedEffect(coiffeurId) {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                val loadedProfile = api.getCoiffeurProfile(coiffeurId)
                profile = loadedProfile
                recentPosts = loadedProfile.recentPosts
                
                // Phase F.2 - Charger les posts épinglés
                try {
                    isLoadingPinnedPosts = true
                    pinnedPosts = api.getPinnedPosts(coiffeurId)
                } catch (e: Exception) {
                    FrollotLogger.warning("API", "⚠️ Erreur chargement posts épinglés: ${e.message}")
                    pinnedPosts = emptyList()
                } finally {
                    isLoadingPinnedPosts = false
                }
            } catch (e: Exception) {
                hasError = true
                errorMessage = e.message ?: "Erreur lors du chargement du profil"
                FrollotLogger.error("API", "❌ Erreur chargement profil coiffeur: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = "Profil Coiffeur",
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
                                profile = api.getCoiffeurProfile(coiffeurId)
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
                val isOwner = currentUser?.id == coiffeurId
                
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
                                        FrollotLogger.debug("CoiffeurProfile", "Post ajouté à la collection")
                                    }
                                } catch (e: Exception) {
                                    FrollotLogger.debug("CoiffeurProfile", "Erreur ajout à collection: ${e.message}")
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
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
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
                                verificationType = p.verificationType,
                                userType = UserType.hairstylist
                            ),
                            isOwner = isOwner,
                            isFollowed = p.isFollowedByCurrentUser ?: false,
                            onFollowClick = onFollowClick?.let { { it(coiffeurId) } },
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
                                            val fileName = "cover_${coiffeurId}_${currentTimeMillis()}.jpg"
                                            val coverImageUrl = api.uploadImage(bytes, fileName)
                                            api.updateUserCoverImage(coiffeurId, coverImageUrl)
                                            // Recharger le profil pour obtenir la nouvelle URL
                                            profile = api.getCoiffeurProfile(coiffeurId)
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
                                    label = "Posts"
                                ),
                                ProfileStatConfig(
                                    value = formatProfileNumber(p.statistics.totalLikes.toInt()),
                                    label = "Likes"
                                ),
                                ProfileStatConfig(
                                    value = p.statistics.followersCount.toString(),
                                    label = "Abonnés"
                                ),
                                ProfileStatConfig(
                                    value = p.statistics.followingCount.toString(),
                                    label = "Abonnements"
                                )
                            ),
                            followButtonText = FollowButtonText(
                                follow = stringResource(Strings.CoiffeurProfile.Follow),
                                unfollow = stringResource(Strings.CoiffeurProfile.Unfollow)
                            )
                        )
                    }

                    // Bio et spécialités
                    if (p.bio != null || p.specialties.isNotEmpty()) {
                        item {
                            CoiffeurBioSection(
                                bio = p.bio,
                                specialties = p.specialties,
                                yearsExperience = p.yearsExperience,
                                certifications = p.certifications,
                                instagramHandle = p.instagramHandle
                            )
                        }
                    }

                    // Badges et certifications (Phase E.3)
                    if (p.badges.isNotEmpty()) {
                        item {
                            ProfileBadgesSection(
                                badges = p.badges,
                                title = stringResource(Strings.CoiffeurProfile.BadgesAndCertifications),
                                displayMode = BadgeDisplayMode.Card
                            )
                        }
                    }

                    // Portfolio mis en avant
                    p.portfolioHighlighted?.let { portfolio ->
                        item {
                            CoiffeurHighlightedPortfolioSection(
                                portfolio = portfolio,
                                onNavigateToPortfolio = { onNavigateToPortfolio(portfolio.id) }
                            )
                        }
                    }

                    // Portfolios
                    if (p.portfolios.isNotEmpty()) {
                        item {
                            CoiffeurPortfoliosSection(
                                portfolios = p.portfolios,
                                onNavigateToPortfolio = { portfolioId ->
                                    onNavigateToPortfolio(portfolioId)
                                }
                            )
                        }
                    }

                    // Phase F.2 - Posts épinglés
                    if (pinnedPosts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Strings.CoiffeurProfile.PinnedPosts),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(pinnedPosts) { post ->
                            UltraPremiumPostCard(
                                post = post,
                                comments = emptyList<CommentResponse>(),
                                currentUser = currentUser,
                                onLikeClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.toggleLike(postId)
                                            pinnedPosts = updatePostInList(pinnedPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur toggle like: ${e.message}")
                                        }
                                    }
                                },
                                onReactionClick = { postId: String, reactionType: ReactionType ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.addReaction(postId, reactionType)
                                            pinnedPosts = updatePostInList(pinnedPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur réaction: ${e.message}")
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
                                            FrollotLogger.debug("CoiffeurProfile", "Post partagé avec succès")
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur partage: ${e.message}")
                                        }
                                    }
                                },
                                onAuthorClick = { authorId: String ->
                                    onNavigateToCoiffeur(authorId)
                                },
                                onArchiveClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.archivePost(postId)
                                            pinnedPosts = updatePostInList(pinnedPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur archive: ${e.message}")
                                        }
                                    }
                                },
                                onSaveToCollection = { postId: String ->
                                    // Ouvrir le dialogue de sélection de collection
                                    selectedPostIdForCollection = postId
                                    scope.launch {
                                        try {
                                            isLoadingCollections = true
                                            currentUser?.id?.let { userId ->
                                                userCollections = api.getCollectionsByUser(userId, includePrivate = true)
                                            }
                                            showCollectionDialog = true
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur chargement collections: ${e.message}")
                                        } finally {
                                            isLoadingCollections = false
                                        }
                                    }
                                },
                                onPinClick = { postId: String ->
                                    // Phase F.2 - Épingler/désépingler
                                    scope.launch {
                                        try {
                                            val updatedPost = if (post.isPinned) {
                                                api.unpinPost(postId)
                                            } else {
                                                api.pinPost(postId)
                                            }
                                            // Mettre à jour la liste des posts épinglés
                                            pinnedPosts = pinnedPosts.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }.filter { it.isPinned }
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur lors de l'épinglage/désépinglage: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Posts récents
                    if (recentPosts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Strings.CoiffeurProfile.RecentPosts),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(recentPosts) { post ->
                            UltraPremiumPostCard(
                                post = post,
                                comments = emptyList<CommentResponse>(),
                                currentUser = currentUser,
                                onLikeClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.toggleLike(postId)
                                            recentPosts = updatePostInList(recentPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur toggle like: ${e.message}")
                                        }
                                    }
                                },
                                onReactionClick = { postId: String, reactionType: ReactionType ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.addReaction(postId, reactionType)
                                            recentPosts = updatePostInList(recentPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur réaction: ${e.message}")
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
                                            FrollotLogger.debug("CoiffeurProfile", "Post partagé avec succès")
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur partage: ${e.message}")
                                        }
                                    }
                                },
                                onAuthorClick = { authorId: String ->
                                    onNavigateToCoiffeur(authorId)
                                },
                                onArchiveClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            val updatedPost = api.archivePost(postId)
                                            recentPosts = updatePostInList(recentPosts, updatedPost)
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur archive: ${e.message}")
                                        }
                                    }
                                },
                                onSaveToCollection = { postId: String ->
                                    // Ouvrir le dialogue de sélection de collection
                                    selectedPostIdForCollection = postId
                                    scope.launch {
                                        try {
                                            isLoadingCollections = true
                                            currentUser?.id?.let { userId ->
                                                userCollections = api.getCollectionsByUser(userId, includePrivate = true)
                                            }
                                            showCollectionDialog = true
                                        } catch (e: Exception) {
                                            FrollotLogger.debug("CoiffeurProfile", "Erreur chargement collections: ${e.message}")
                                        } finally {
                                            isLoadingCollections = false
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
 * Section bio et spécialités du profil coiffeur.
 */
@Composable
fun CoiffeurBioSection(
    bio: String?,
    specialties: List<String>,
    yearsExperience: Int?,
    certifications: String?,
    instagramHandle: String?
) {
    StandardCardNoPadding(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bio
            bio?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Spécialités
            if (specialties.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    specialties.chunked(3).forEach { rowSpecialties ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSpecialties.forEach { specialty ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = specialty,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Années d'expérience
            yearsExperience?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Work,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$it ans d'expérience",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Certifications
            certifications?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.School,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Instagram
            instagramHandle?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Link,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


/**
 * Section portfolio mis en avant.
 */
@Composable
fun CoiffeurHighlightedPortfolioSection(
    portfolio: PortfolioResponse,
    onNavigateToPortfolio: () -> Unit
) {
    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPortfolio() }
    ) {
        Column {
            // Image de couverture
            portfolio.coverImageUrl?.let { coverUrl ->
                AsyncImage(
                    model = coverUrl,
                    contentDescription = portfolio.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Mis en avant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Strings.CoiffeurProfile.FeaturedPortfolio),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = portfolio.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                portfolio.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
                Text(
                    text = "${portfolio.postsCount} posts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Section portfolios du profil coiffeur.
 */
@Composable
fun CoiffeurPortfoliosSection(
    portfolios: List<PortfolioResponse>,
    onNavigateToPortfolio: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Strings.CoiffeurProfile.Portfolios),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        portfolios.forEach { portfolio ->
            PortfolioCard(
                portfolio = portfolio,
                onClick = { onNavigateToPortfolio(portfolio.id) }
            )
        }
    }
}

/**
 * Carte d'affichage d'un portfolio.
 */
@Composable
fun PortfolioCard(
    portfolio: PortfolioResponse,
    onClick: () -> Unit
) {
    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image de couverture
            portfolio.coverImageUrl?.let { coverUrl ->
                AsyncImage(
                    model = coverUrl,
                    contentDescription = portfolio.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = portfolio.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${portfolio.postsCount} posts",
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


/**
 * Dialogue de sélection de collection pour sauvegarder un post.
 */
@Composable
fun CollectionSelectionDialog(
    collections: List<CollectionResponse>,
    isLoading: Boolean,
    onCollectionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Strings.Collections.SaveToCollection),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                collections.isEmpty() -> {
                    Text(
                        text = stringResource(Strings.Collections.NoCollections),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(collections) { collection ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCollectionSelected(collection.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Icône collection
                                    Icon(
                                        if (!collection.isPublic) Icons.Outlined.Lock else Icons.Outlined.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = collection.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${collection.postsCount} posts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Strings.Common.Cancel))
            }
        }
    )
}

