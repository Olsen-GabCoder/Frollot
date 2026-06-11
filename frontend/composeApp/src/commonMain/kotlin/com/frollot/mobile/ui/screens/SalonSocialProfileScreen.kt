@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.material3.FloatingActionButton
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
import com.frollot.mobile.ui.components.VerificationBadge
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
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


/**
 * Écran de profil social enrichi d'un salon.
 * Phase E.2 - Profil Salon Social
 * 
 * Affiche :
 * - Cover image et description sociale
 * - Posts mis en avant
 * - Statistiques (posts, likes, followers)
 * - Portfolios
 * - Posts récents
 * - Équipe (coiffeurs)
 * - Services proposés
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonSocialProfileScreen(
    salonId: String,
    currentUser: User? = null,
    onBack: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToPortfolio: (String) -> Unit = {},
    onNavigateToCoiffeur: (String) -> Unit = {},
    onNavigateToSalon: (String) -> Unit = {},
    onFollowClick: ((String) -> Unit)? = null
) {
    var profile by remember { mutableStateOf<SalonSocialProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    // Charger le profil
    LaunchedEffect(salonId) {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                profile = api.getSalonSocialProfile(salonId)
            } catch (e: Exception) {
                hasError = true
                errorMessage = e.message ?: "Erreur lors du chargement du profil"
                FrollotLogger.error("API", "❌ Erreur chargement profil salon: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = "Profil Salon",
                onBackClick = onBack
            )
        },
        // Pas de FAB ici - les posts de salon sont créés depuis SalonPostsScreen ("Voir les posts")
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
                                profile = api.getSalonSocialProfile(salonId)
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
                val isOwner = currentUser?.userType == UserType.salon_owner
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Cover image sociale
                    p.socialCoverImage?.let { coverImage ->
                        item {
                            SalonCoverImageSection(coverImageUrl = coverImage)
                        }
                    }

                    // Header avec nom et adresse
                    item {
                        SalonProfileHeader(
                            profile = p,
                            isFollowed = p.isFollowedByCurrentUser ?: false,
                            onFollowClick = onFollowClick?.let { { it(salonId) } }
                        )
                    }

                    // Description sociale
                    p.socialDescription?.let { description ->
                        item {
                            SalonDescriptionSection(description = description)
                        }
                    }

                    // Statistiques
                    item {
                        ProfileStatsSection(
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
                                )
                            )
                        )
                    }

                    // Posts mis en avant
                    if (p.highlightedPosts.isNotEmpty()) {
                        item {
                            SalonHighlightedPostsSection(
                                posts = p.highlightedPosts,
                                onNavigateToPost = onNavigateToPost
                            )
                        }
                    }

                    // Portfolios
                    if (p.portfolios.isNotEmpty()) {
                        item {
                            SalonPortfoliosSection(
                                portfolios = p.portfolios,
                                onNavigateToPortfolio = { portfolioId ->
                                    onNavigateToPortfolio(portfolioId)
                                }
                            )
                        }
                    }

                    // Posts récents
                    if (p.recentPosts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Strings.SalonSocialProfile.RecentPosts),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(p.recentPosts) { post ->
                            UltraPremiumPostCard(
                                post = post,
                                comments = emptyList<CommentResponse>(),
                                onLikeClick = { postId: String ->
                                    // TODO: Implémenter toggle like
                                },
                                onReactionClick = { postId: String, reactionType: ReactionType ->
                                    // TODO: Implémenter réaction
                                },
                                onCommentClick = { postId: String ->
                                    onNavigateToComments(postId)
                                },
                                onShareClick = { postId: String ->
                                    // TODO: Implémenter partage
                                },
                                onAuthorClick = { authorId: String ->
                                    onNavigateToCoiffeur(authorId)
                                },
                                onArchiveClick = { postId: String ->
                                    // TODO: Implémenter archive
                                },
                                onSaveToCollection = { postId: String ->
                                    // TODO: Implémenter sauvegarde collection
                                }
                            )
                        }
                    }

                    // Équipe
                    if (p.team.isNotEmpty()) {
                        item {
                            SalonTeamSection(
                                team = p.team,
                                onNavigateToCoiffeur = onNavigateToCoiffeur
                            )
                        }
                    }

                    // Services
                    if (p.services.isNotEmpty()) {
                        item {
                            SalonServicesSection(services = p.services)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section cover image sociale du salon.
 */
@Composable
fun SalonCoverImageSection(coverImageUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        AsyncImage(
            model = coverImageUrl,
            contentDescription = "Image de couverture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Header du profil salon avec nom, adresse et bouton Follow.
 */
@Composable
fun SalonProfileHeader(
    profile: SalonSocialProfileResponse,
    isFollowed: Boolean,
    onFollowClick: (() -> Unit)? = null
) {
    StandardCardNoPadding(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nom du salon avec badge de vérification
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Phase H.2 - Badge de vérification
                VerificationBadge(
                    isVerified = profile.isVerified,
                    verificationType = profile.verificationType
                )
            }

            // Adresse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${profile.address}, ${profile.city} ${profile.postalCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bouton Follow
            if (onFollowClick != null) {
                if (isFollowed) {
                    SecondaryButton(
                        text = stringResource(Strings.SalonSocialProfile.Unfollow),
                        onClick = onFollowClick,
                        icon = {
                            Icon(
                                Icons.Filled.PersonRemove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                } else {
                    PrimaryButton(
                        text = stringResource(Strings.SalonSocialProfile.Follow),
                        onClick = onFollowClick,
                        icon = {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Section description sociale du salon.
 */
@Composable
fun SalonDescriptionSection(description: String) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


/**
 * Section posts mis en avant du salon.
 */
@Composable
fun SalonHighlightedPostsSection(
    posts: List<PostResponse>,
    onNavigateToPost: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Strings.SalonSocialProfile.FeaturedPosts),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        posts.forEach { post ->
            StandardCardNoPadding(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPost(post.id) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image du post
                    post.imageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = post.content,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = post.content.take(100) + if (post.content.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "❤️ ${post.likesCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "💬 ${post.commentsCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section portfolios du salon.
 */
@Composable
fun SalonPortfoliosSection(
    portfolios: List<PortfolioResponse>,
    onNavigateToPortfolio: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Strings.SalonSocialProfile.Portfolios),
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
 * Section équipe du salon.
 */
@Composable
fun SalonTeamSection(
    team: List<UserResponse>,
    onNavigateToCoiffeur: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Strings.SalonDetail.Team),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        team.forEach { member ->
            StandardCardNoPadding(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCoiffeur(member.id) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Créer un User temporaire pour l'avatar
                    val userForAvatar = remember(member.id, member.avatarUrl) {
                        User(
                            id = member.id,
                            email = member.email,
                            userType = member.userType,
                            firstName = member.firstName,
                            lastName = member.lastName,
                            avatarUrl = member.avatarUrl
                        )
                    }
                    UserAvatar(
                        user = userForAvatar,
                        size = 50.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${member.firstName ?: ""} ${member.lastName ?: ""}".trim()
                                .ifBlank { member.email },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (member.isVerified) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Verified,
                                    contentDescription = "Vérifié",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(Strings.SalonSocialProfile.Verified),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section services du salon.
 */
@Composable
fun SalonServicesSection(services: List<SalonService>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Strings.SalonSocialProfile.Services),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        services.take(5).forEach { service ->
            StandardCardNoPadding(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        service.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "⏱️ ${service.formattedDuration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "💰 ${service.price}€",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        if (services.size > 5) {
            Text(
                text = "... et ${services.size - 5} autres services",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

