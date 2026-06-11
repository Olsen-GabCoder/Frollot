@file:OptIn(ExperimentalFoundationApi::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.frollot.mobile.model.CommentResponse
import com.frollot.mobile.model.PostMediaResponse
import com.frollot.mobile.model.PostMediaType
import com.frollot.mobile.model.PostResponse
import com.frollot.mobile.model.PostVisibility
import com.frollot.mobile.ui.utils.AnimationSpecs
import com.frollot.mobile.model.ReactionType
import com.frollot.mobile.model.TaggedType
import com.frollot.mobile.model.User
import com.frollot.mobile.ui.components.menus.PostOptionsMenu
import com.frollot.mobile.ui.components.menus.PostOptionItem
import com.frollot.mobile.ui.components.menus.PostOptionsDivider
import com.frollot.mobile.ui.components.ReactionsMenu
import com.frollot.mobile.localization.*
import com.frollot.mobile.localization.pluralizedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt
import com.frollot.mobile.config.FrollotLogger


@Composable
fun CollectionSelectionItem(
    collection: com.frollot.mobile.model.CollectionResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône ou image de couverture
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (collection.coverImageUrl != null) {
                    coil3.compose.AsyncImage(
                        model = collection.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleSmall,
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

/**
 * Fonction utilitaire pour formater les nombres (style réseaux sociaux)
 */
fun formatNumber(count: Int): String {
    return when {
        count >= 1_000_000 -> {
            val value = (count / 1_000_000.0 * 10).roundToInt() / 10.0
            "${if (value % 1 == 0.0) value.toInt() else value}M"
        }

        count >= 1_000 -> {
            val value = (count / 1_000.0 * 10).roundToInt() / 10.0
            "${if (value % 1 == 0.0) value.toInt() else value}K"
        }

        else -> count.toString()
    }
}

/**
 * 🎭 Skeleton loading ultra premium avec shimmer avancé
 */
@Composable
fun UltraPremiumLoadingSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                                    CircleShape
                                )
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(16.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }
    }
}

/**
 * 💔 Carte d'erreur ultra premium
 */
@Composable
fun UltraPremiumErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icône d'erreur avec effet glow
            Box(
                modifier = Modifier.size(120.dp)
            ) {
                // Effet de glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.2f)
                        // .blur(40.dp) // API expérimentale non disponible
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                            CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.errorContainer,
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Strings.UltraPremiumPostCard.Oops),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Bouton retry premium
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Strings.UltraPremiumPostCard.Retry),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 🎨 Carte feed vide ultra premium
 */
@Composable
fun UltraPremiumEmptyCard(
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            // Icône flottante avec effet 3D
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = float.dp)
            ) {
                // Ombre portée
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = 20.dp)
                        // .blur(30.dp) // API expérimentale non disponible
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        )
                )

                // Cercle principal avec gradient animé
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Strings.UltraPremiumPostCard.NoPostsYet),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(Strings.UltraPremiumPostCard.BeFirstToShare),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Bouton créer premium avec gradient
            Button(
                onClick = onCreatePost,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            "Créer mon premier post",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Slider simple pour afficher plusieurs médias.
 */
@Composable
fun MediaSlider(
    media: List<PostMediaResponse>,
    modifier: Modifier = Modifier
) {
    if (media.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { media.size }, initialPage = 0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = media[page].mediaUrl,
                contentDescription = "${media[page].mediaType.getLocalizedDisplayName()} - Image ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Indicateurs de page
        if (media.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                media.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (pagerState.currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
        }

        // Badge du type de média supprimé pour un design plus épuré
    }
}

/**
 * Vue de comparaison avant/après avec slider interactif.
 */
@Composable
fun BeforeAfterComparisonView(
    beforeImages: List<PostMediaResponse>,
    afterImages: List<PostMediaResponse>,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0.5f) } // 0.0 = avant, 1.0 = après

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Image "après" (fond)
        if (afterImages.isNotEmpty()) {
            AsyncImage(
                model = afterImages[0].mediaUrl,
                contentDescription = "Après",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Image "avant" avec masque selon la position du slider
        if (beforeImages.isNotEmpty()) {
            var containerWidth by remember { mutableStateOf(0) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { containerWidth = it.width }
                    .clip(RoundedCornerShape(0.dp))
                    .graphicsLayer {
                        clip = true
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((containerWidth * sliderPosition).coerceAtLeast(0f).dp)
                ) {
                    AsyncImage(
                        model = beforeImages[0].mediaUrl,
                        contentDescription = "Avant",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Slider vertical pour ajuster la comparaison
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val newPosition = (change.position.x / size.width).coerceIn(0f, 1f)
                        sliderPosition = newPosition
                    }
                }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Indicateur de slider
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = "Glisser pour comparer",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Labels "Avant" et "Après"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "📸 Avant",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✨ Après",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

/**
 * Composant pour afficher les médias avant/après avec un slider interactif.
 *
 * Affiche les images "before" et "after" côte à côte avec possibilité de glisser
 * pour comparer, ou en mode slider pour les posts avec plusieurs images.
 */
@Composable
fun BeforeAfterMediaViewer(
    media: List<PostMediaResponse>,
    modifier: Modifier = Modifier
) {
    // Séparer les médias par type
    val beforeMedia =
        media.filter { it.mediaType == PostMediaType.before }.sortedBy { it.orderIndex }
    val afterMedia =
        media.filter { it.mediaType == PostMediaType.after }.sortedBy { it.orderIndex }

    // Si on a des images avant et après, afficher en mode comparaison
    if (beforeMedia.isNotEmpty() && afterMedia.isNotEmpty()) {
        BeforeAfterComparisonView(
            beforeImages = beforeMedia,
            afterImages = afterMedia,
            modifier = modifier
        )
    } else {
        // Sinon, afficher en mode slider simple
        MediaSlider(
            media = media.sortedBy { it.orderIndex },
            modifier = modifier
        )
    }
}

/**
 * 🎨 Carte de post ULTRA PREMIUM
 * - Glassmorphism
 * - Animations sophistiquées
 * - Gradients dynamiques
 * - Micro-interactions
 * - Section commentaires améliorée
 * - Fonctionnalité d'archivage
 */
@Composable
fun UltraPremiumPostCard(
    post: PostResponse,
    comments: List<CommentResponse> = emptyList(),
    currentUser: User? = null, // Phase F.2 - Pour vérifier si l'utilisateur est l'auteur
    onLikeClick: (String) -> Unit,
    onReactionClick: ((String, ReactionType) -> Unit)? = null, // Phase D.4 - Réactions Spécialisées
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onSaveToCollection: (String) -> Unit,
    onTagClick: ((TaggedType, String) -> Unit)? = null, // Phase E - Navigation vers profils
    onPinClick: ((String) -> Unit)? = null, // Phase F.2 - Posts Épinglés
    onReportClick: ((String) -> Unit)? = null, // Phase H.1 - Signalement de Contenu
    onExternalShareClick: ((PostResponse) -> Unit)? = null, // Phase I.2 - Partage Externe
    onPostClick: ((String) -> Unit)? = null, // Callback pour ouvrir le post en détail
    postIdWithOpenMenu: String? = null, // État global pour l'overlay
    onPostIdWithOpenMenuChange: ((String?) -> Unit)? = null, // Callback pour mettre à jour l'état
    onMenuButtonPositionChange: ((Offset?) -> Unit)? = null, // Callback pour mettre à jour la position du bouton
    modifier: Modifier = Modifier
) {
    var isLikeAnimating by remember { mutableStateOf(false) }
    var showLikeExplosion by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(post.isFavoritedByCurrentUser) }
    var showCommentsPreview by remember { mutableStateOf(true) }
    var showMoreComments by remember { mutableStateOf(false) }
    
    // Phase D.4 - Menu de réactions
    var showReactionsMenu by remember { mutableStateOf(false) }
    var reactionButtonPosition by remember { mutableStateOf<Offset?>(null) }
    
    // Fermer le menu automatiquement après 3 secondes ou quand les réactions changent
    LaunchedEffect(showReactionsMenu, post.reactions, post.currentUserReaction) {
        if (showReactionsMenu) {
            // Fermer automatiquement après 3 secondes
            delay(3000)
            showReactionsMenu = false
        }
    }

    // Synchroniser isBookmarked avec post.isFavoritedByCurrentUser
    LaunchedEffect(post.isFavoritedByCurrentUser) {
        isBookmarked = post.isFavoritedByCurrentUser
    }
    
    // Scope pour les coroutines
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isLikeAnimating) 1.4f else 1f,
        animationSpec = AnimationSpecs.LikeReaction
    )

    val heartRotation by animateFloatAsState(
        targetValue = if (isLikeAnimating) 360f else 0f,
        animationSpec = AnimationSpecs.SoftTouchInteraction
    )

    LaunchedEffect(isLikeAnimating) {
        if (isLikeAnimating) {
            showLikeExplosion = true
            delay(300)
            isLikeAnimating = false
            delay(500)
            showLikeExplosion = false
        }
    }

    // Menu déroulant pour les options du post
    var showOptionsMenu by remember { mutableStateOf(false) }
    
    // Logs de diagnostic
    LaunchedEffect(showOptionsMenu) {
        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] showOptionsMenu changed for post ${post.id}: $showOptionsMenu")
        onPostIdWithOpenMenuChange?.invoke(if (showOptionsMenu) post.id else null)
        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Called onPostIdWithOpenMenuChange with: ${if (showOptionsMenu) post.id else null}")
    }
    
    // Fermer le menu si l'overlay est fermé ou si un autre post ouvre son menu
    LaunchedEffect(postIdWithOpenMenu) {
        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] postIdWithOpenMenu changed: $postIdWithOpenMenu (current post: ${post.id}, showOptionsMenu: $showOptionsMenu)")
        if (postIdWithOpenMenu == null && showOptionsMenu) {
            // L'overlay a été fermé (clic extérieur), fermer le menu local
            FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Closing menu because overlay was closed")
            showOptionsMenu = false
        } else if (postIdWithOpenMenu != null && postIdWithOpenMenu != post.id && showOptionsMenu) {
            // Un autre post a ouvert son menu, fermer celui-ci
            FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Closing menu because another post opened its menu")
            showOptionsMenu = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        // Box pour positionner le menu en overlay absolu par rapport à la Card
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 🎭 Header avec avatar et infos premium
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onAuthorClick(post.authorId) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    // Avatar avec double bordure gradient
                    Box(
                        modifier = Modifier.size(52.dp)
                    ) {
                        // Bordure gradient externe
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Espace blanc
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Avatar intérieur
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = post.authorName.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            // Badge vérifié premium
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            // Badge "Épinglé" - Phase F.2
                            if (post.isPinned) {
                                Surface(
                                    modifier = Modifier.size(18.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = stringResource(Strings.UltraPremiumPostCard.Pinned),
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            // Badge de visibilité - Phase F.3
                            if (post.visibility != PostVisibility.PUBLIC) {
                                Surface(
                                    modifier = Modifier.size(18.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (post.visibility) {
                                        PostVisibility.FOLLOWERS -> MaterialTheme.colorScheme.secondaryContainer
                                        PostVisibility.PRIVATE -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = post.visibility.getEmoji(),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = post.formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            )
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Menu avec design premium et options
                // IconButton dans le Row (prend seulement l'espace nécessaire)
                // zIndex élevé pour être au-dessus de l'overlay (qui a zIndex 0.1f)
                IconButton(
                    onClick = { 
                        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] IconButton CLICKED for post ${post.id}")
                        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Current showOptionsMenu: $showOptionsMenu")
                        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Current postIdWithOpenMenu: $postIdWithOpenMenu")
                        // Ouvrir le menu et notifier que le clic a été intercepté
                        showOptionsMenu = true
                        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Set showOptionsMenu to true")
                        onPostIdWithOpenMenuChange?.invoke(post.id)
                        FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Called onPostIdWithOpenMenuChange with post.id: ${post.id}")
                    },
                    modifier = Modifier
                        .zIndex(100f) // zIndex élevé pour être au-dessus de l'overlay
                        .onGloballyPositioned { coordinates ->
                            // Obtenir la position globale du bouton pour positionner le menu
                            // Convertir les coordonnées locales (0,0) en coordonnées globales (fenêtre)
                            val localOffset = Offset.Zero
                            val globalOffset = coordinates.localToWindow(localOffset)
                            FrollotLogger.debug("API", "🔍 [UltraPremiumPostCard] Menu button position: $globalOffset")
                            onMenuButtonPositionChange?.invoke(globalOffset)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 📝 Contenu avec expansion élégante
            var isExpanded by remember { mutableStateOf(false) }
            val maxLines = if (isExpanded) Int.MAX_VALUE else 4

            AnimatedVisibility(
                visible = post.content.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isExpanded = !isExpanded
                        }
                ) {
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (post.content.length > 150 && !isExpanded) {
                        Text(
                            text = "... voir plus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🏷️ Tags/Mentions
            if (post.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        post.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    // Phase E - Navigation vers les profils
                                    onTagClick?.invoke(tag.taggedType, tag.taggedId)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (tag.taggedType == TaggedType.salon) "🏢" else "👤",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = tag.taggedId,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // #️⃣ Hashtags Coiffure
            if (post.hashtags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        post.hashtags.forEach { hashtag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    // TODO: Navigation vers les posts du hashtag
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = hashtag.categoryEmoji,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "#${hashtag.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 🖼️ Image avec effet premium - Support multi-médias (Phase B.5)
            when {
                // Si le post a des médias (Phase B.5)
                post.media.isNotEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .clickable(enabled = onPostClick != null) {
                                onPostClick?.invoke(post.id)
                            }
                    ) {
                        BeforeAfterMediaViewer(
                            media = post.media,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                // Sinon, utiliser l'image principale (rétrocompatibilité)
                post.imageUrl != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = onPostClick != null) {
                                onPostClick?.invoke(post.id)
                            }
                    ) {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "Photo du post",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay subtil en bas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 💫 Barre d'actions premium avec animations ET COMPTEURS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    // Phase D.4 - Bouton de réactions avec menu au long-press
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        // Clic simple : toggle réaction ou like classique
                                        if (onReactionClick != null) {
                                            // Phase D.4 : Si l'utilisateur a déjà une réaction, la supprimer
                                            // Sinon, ajouter un like
                                            val currentReaction = post.currentUserReaction
                                            if (currentReaction != null) {
                                                // Supprimer la réaction (sera géré par le parent)
                                                onReactionClick(post.id, currentReaction)
                                            } else {
                                                // Ajouter un like
                                                isLikeAnimating = true
                                                onReactionClick(post.id, ReactionType.LIKE)
                                            }
                                        } else {
                                            // Rétrocompatibilité : like classique
                                            isLikeAnimating = true
                                            onLikeClick(post.id)
                                        }
                                    },
                                    onLongClick = {
                                        // Long-press : afficher le menu de réactions
                                        if (onReactionClick != null) {
                                            showReactionsMenu = true
                                        }
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .onGloballyPositioned { coordinates ->
                                    // Capturer la position du bouton pour positionner le menu
                                    val localOffset = Offset.Zero
                                    val globalOffset = coordinates.localToWindow(localOffset)
                                    reactionButtonPosition = globalOffset
                                }
                        ) {
                            // Afficher l'icône en fonction de la réaction de l'utilisateur
                            val (icon, tint) = when {
                                post.currentUserReaction != null -> {
                                    // Afficher l'emoji de la réaction de l'utilisateur
                                    val emoji = post.currentUserReaction.getEmoji()
                                    null to null // On utilisera Text avec emoji
                                }
                                post.isLikedByCurrentUser -> {
                                    // Rétrocompatibilité : like classique
                                    Icons.Default.Favorite to MaterialTheme.colorScheme.primary
                                }
                                else -> {
                                    Icons.Outlined.FavoriteBorder to MaterialTheme.colorScheme.onSurface
                                }
                            }
                            
                            // Calculer les réactions populaires à afficher
                            val popularReactions = remember(post.reactions, post.currentUserReaction) {
                                // Convertir Map<String, Int> en liste de paires (ReactionType, count)
                                val reactionsList = post.reactions.mapNotNull { (key, count) ->
                                    try {
                                        val reactionType = ReactionType.valueOf(key.uppercase())
                                        reactionType to count
                                    } catch (e: IllegalArgumentException) {
                                        null
                                    }
                                }.filter { it.second > 0 }
                                
                                // Trier par nombre décroissant
                                val sorted = reactionsList.sortedByDescending { it.second }
                                
                                // Prendre les 2-3 premières réactions populaires
                                val topReactions = sorted.take(3)
                                
                                // Si l'utilisateur a une réaction, la mettre en premier si elle n'est pas déjà dans le top
                                val userReaction = post.currentUserReaction
                                if (userReaction != null) {
                                    val userReactionCount = post.reactions[userReaction.name.lowercase()] ?: 0
                                    if (userReactionCount > 0) {
                                        val userReactionPair = userReaction to userReactionCount
                                        if (userReactionPair in topReactions) {
                                            // Si déjà dans le top, la mettre en premier
                                            listOf(userReactionPair) + topReactions.filter { it.first != userReaction }
                                        } else {
                                            // Sinon, l'ajouter en premier et garder les 2 autres
                                            listOf(userReactionPair) + topReactions.take(2)
                                        }
                                    } else {
                                        topReactions
                                    }
                                } else {
                                    topReactions
                                }
                            }
                            
                            // Afficher les réactions populaires (style Instagram/Facebook)
                            val totalReactions = post.reactions.values.sum()
                            val displayCount = if (totalReactions > 0) totalReactions else post.likesCount
                            
                            if (popularReactions.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    popularReactions.forEachIndexed { index, (reactionType, count) ->
                                        // Grouper emoji et compteur ensemble
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(end = if (index < popularReactions.size - 1) 0.dp else 0.dp)
                                        ) {
                                            // Emoji de la réaction
                                            Text(
                                                text = reactionType.getEmoji(),
                                                fontSize = if (index == 0 && post.currentUserReaction == reactionType) 22.sp else 18.sp,
                                                modifier = Modifier
                                                    .then(
                                                        if (index == 0 && post.currentUserReaction == reactionType) {
                                                            Modifier
                                                                .scale(scale)
                                                                .rotate(heartRotation)
                                                        } else {
                                                            Modifier
                                                        }
                                                    )
                                            )
                                            
                                            // Compteur - TOUJOURS affiché pour toutes les réactions
                                            Text(
                                                text = formatNumber(count),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            )
                                        }
                                        
                                        // Séparateur visuel (sauf pour la dernière)
                                        if (index < popularReactions.size - 1) {
                                            Text(
                                                text = "·",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                    
                                    // Afficher le total si différent de la somme des réactions affichées
                                    val displayedSum = popularReactions.sumOf { it.second }
                                    if (displayedSum < displayCount && displayCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "·",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = formatNumber(displayCount),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                // Fallback : afficher l'icône par défaut si aucune réaction
                                if (post.currentUserReaction != null) {
                                    // Afficher l'emoji de la réaction de l'utilisateur
                                    Text(
                                        text = post.currentUserReaction.getEmoji(),
                                        fontSize = 26.sp,
                                        modifier = Modifier
                                            .scale(scale)
                                            .rotate(heartRotation)
                                    )
                                } else {
                                    Icon(
                                        imageVector = icon ?: Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Réaction",
                                        tint = tint ?: MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .scale(scale)
                                            .rotate(heartRotation)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Afficher le total des réactions (somme de toutes les réactions)
                                val totalReactions = post.reactions.values.sum()
                                val displayCount = if (totalReactions > 0) totalReactions else post.likesCount
                                
                                Text(
                                    text = formatNumber(displayCount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Effet d'explosion de particules
                        if (showLikeExplosion) {
                            repeat(8) { index ->
                                val angle = (index * 45f)
                                val offsetAnimation by animateFloatAsState(
                                    targetValue = 30f,
                                    animationSpec = AnimationSpecs.StateTransition
                                )

                                // Conversion degrés -> radians
                                val radians = angle * (kotlin.math.PI / 180f)

                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .offset(
                                            x = (offsetAnimation * kotlin.math.cos(radians)
                                                .toFloat()).dp,
                                            y = (offsetAnimation * kotlin.math.sin(radians)
                                                .toFloat()).dp
                                        )
                                        .align(Alignment.Center)
                                )
                            }
                        }
                        
                        // Phase D.4 - Menu de réactions (conforme à la charte graphique)
                        if (showReactionsMenu && onReactionClick != null) {
                            val density = LocalDensity.current
                            // Le menu fait environ 6 réactions × 42dp + padding 12dp = 264dp
                            val menuWidth = 264.dp
                            val menuWidthPx = with(density) { menuWidth.toPx() }
                            
                            // Calculer l'offset pour centrer le menu par rapport au bouton
                            val offsetX = remember(reactionButtonPosition) {
                                if (reactionButtonPosition != null) {
                                    // Position du bouton en pixels
                                    val buttonX = reactionButtonPosition!!.x
                                    // Largeur du bouton (approximativement 40dp)
                                    val buttonWidthPx = with(density) { 40.dp.toPx() }
                                    // Centre du bouton
                                    val buttonCenterX = buttonX + buttonWidthPx / 2
                                    // Positionner le menu centré sur le bouton, mais s'assurer qu'il ne dépasse pas à gauche
                                    val menuCenterX = buttonCenterX - menuWidthPx / 2
                                    // Si le menu dépasse à gauche (menuCenterX < 0), le positionner à 16dp du bord gauche
                                    val minMargin = with(density) { 16.dp.toPx() }
                                    val finalX = if (menuCenterX < minMargin) {
                                        minMargin - buttonX
                                    } else {
                                        -menuWidthPx / 2 + buttonWidthPx / 2
                                    }
                                    with(density) { finalX.toDp() }
                                } else {
                                    // Fallback : position par défaut centrée (ajustée pour menu plus petit)
                                    (-120).dp
                                }
                            }
                            
                            ReactionsMenu(
                                onReactionSelected = { reactionType ->
                                    onReactionClick(post.id, reactionType)
                                    showReactionsMenu = false
                                },
                                modifier = Modifier
                                    .offset(x = offsetX, y = (-70).dp) // Position dynamique centrée par rapport au bouton
                                    .zIndex(2000f) // zIndex élevé pour être au-dessus de tout
                            )
                        }
                    }

                    // Comment button avec compteur
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onCommentClick(post.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Commenter",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatNumber(post.commentsCount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,

                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share button avec compteur - Phase D.3
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onShareClick(post.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSharedByCurrentUser)
                                Icons.Default.Share
                            else
                                Icons.Outlined.IosShare,
                            contentDescription = "Partager",
                            tint = if (post.isSharedByCurrentUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatNumber(post.sharesCount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,

                            color = if (post.isSharedByCurrentUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                // Bookmark avec animation (sans compteur)
                IconButton(
                    onClick = {
                        onSaveToCollection(post.id)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (post.isFavoritedByCurrentUser)
                            Icons.Default.Bookmark
                        else
                            Icons.Outlined.BookmarkBorder,
                        contentDescription = "Enregistrer",
                        tint = if (post.isFavoritedByCurrentUser)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 📊 Section commentaires améliorée
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section d'aperçu des commentaires (style Instagram/Facebook)
                if (post.commentsCount > 0 && showCommentsPreview && comments.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Afficher les 2 premiers commentaires réels
                        val commentsToShow =
                            if (showMoreComments) comments else comments.take(2)
                        commentsToShow.forEach { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCommentClick(post.id) },
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Nom de l'utilisateur + commentaire (style compact)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = comment.authorName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,

                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                            }
                        }

                        // Bouton pour voir plus/moins de commentaires
                        if (comments.size > 2) {
                            Text(
                                text = if (showMoreComments) {
                                    "Masquer les commentaires"
                                } else {
                                    "Voir les ${post.commentsCount} commentaires"
                                },
                                modifier = Modifier
                                    .clickable { showMoreComments = !showMoreComments }
                                    .padding(top = 2.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                } else if (post.commentsCount > 0) {
                    // Version minimaliste si pas d'aperçu
                    Text(
                        text = stringResource(Strings.UltraPremiumPostCard.ViewComments).replace("{count}", post.commentsCount.toString()),
                        modifier = Modifier
                            .clickable { onCommentClick(post.id) }
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Champ de commentaire (style Instagram)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                    onClick = { onCommentClick(post.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Strings.UltraPremiumPostCard.AddComment),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "😊",

                                modifier = Modifier.clickable {
                                    // TODO: Ajouter émotion
                                }
                            )
                            Text(
                                text = "❤️",

                                modifier = Modifier.clickable {
                                    // TODO: Ajouter émotion
                                }
                            )
                        }
                    }
                }
            } // Fin de la Column commentaires
            
            // Menu déroulant standardisé - NE PLUS RENDRE ICI
            // Le menu est maintenant rendu au niveau du Scaffold pour être au-dessus de l'overlay
            // Le menu est complètement géré au niveau du Scaffold dans SocialFeedScreen
        } // Fin de la Column fillMaxWidth
        } // Fin du Box fillMaxSize
    } // Fin de la Card
}
