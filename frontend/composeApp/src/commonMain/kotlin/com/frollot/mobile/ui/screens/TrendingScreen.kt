package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran affichant les tendances coiffure.
 * Phase C.3 - Trending Coiffure
 * 
 * Sections :
 * - Posts populaires (avec filtre par période)
 * - Hashtags tendance
 * - Salons en vogue
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    currentUser: User? = null,
    onBack: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToSalon: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToHashtag: (String) -> Unit = {}
) {
    // Section active
    var selectedSection by remember { mutableStateOf(0) } // 0: Posts, 1: Hashtags, 2: Salons
    
    // Période pour les posts trending
    var selectedPeriod by remember { mutableStateOf(TrendPeriod.LAST_7D) }
    
    // Posts trending
    var trendingPosts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isPostsLoading by remember { mutableStateOf(true) }
    var postsError by remember { mutableStateOf<String?>(null) }
    var postsCurrentPage by remember { mutableStateOf(0) }
    var hasMorePosts by remember { mutableStateOf(true) }
    var isLoadingMorePosts by remember { mutableStateOf(false) }
    
    // Hashtags trending
    var trendingHashtags by remember { mutableStateOf<List<HairHashtagResponse>>(emptyList()) }
    var isHashtagsLoading by remember { mutableStateOf(true) }
    var hashtagsError by remember { mutableStateOf<String?>(null) }
    
    // Salons trending
    var trendingSalons by remember { mutableStateOf<List<Salon>>(emptyList()) }
    var isSalonsLoading by remember { mutableStateOf(true) }
    var salonsError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    val scrollState = rememberLazyListState()
    
    // Charger les posts trending
    fun loadTrendingPosts(reset: Boolean = false) {
        scope.launch {
            try {
                if (reset) {
                    postsCurrentPage = 0
                    hasMorePosts = true
                    isPostsLoading = true
                } else {
                    isLoadingMorePosts = true
                }
                postsError = null
                
                val page = if (reset) 0 else postsCurrentPage
                val pageResponse = api.getTrendingPosts(
                    period = selectedPeriod,
                    page = page,
                    size = 20
                )
                
                if (reset) {
                    trendingPosts = pageResponse.content
                } else {
                    trendingPosts = trendingPosts + pageResponse.content
                }
                
                hasMorePosts = !pageResponse.last
                postsCurrentPage = page
                
                isPostsLoading = false
                isLoadingMorePosts = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement posts trending: ${e.message}")
                postsError = e.message ?: "Erreur lors du chargement des posts"
                isPostsLoading = false
                isLoadingMorePosts = false
            }
        }
    }
    
    // Charger les hashtags trending
    fun loadTrendingHashtags() {
        scope.launch {
            try {
                isHashtagsLoading = true
                hashtagsError = null
                trendingHashtags = api.getTrendingHashtags(limit = 20)
                isHashtagsLoading = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement hashtags trending: ${e.message}")
                hashtagsError = e.message ?: "Erreur lors du chargement des hashtags"
                isHashtagsLoading = false
            }
        }
    }
    
    // Charger les salons trending
    fun loadTrendingSalons() {
        scope.launch {
            try {
                isSalonsLoading = true
                salonsError = null
                trendingSalons = api.getTrendingSalons(limit = 10)
                isSalonsLoading = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement salons trending: ${e.message}")
                salonsError = e.message ?: "Erreur lors du chargement des salons"
                isSalonsLoading = false
            }
        }
    }
    
    // Charger les données au démarrage
    LaunchedEffect(Unit) {
        loadTrendingPosts(reset = true)
        loadTrendingHashtags()
        loadTrendingSalons()
    }
    
    // Recharger les posts quand la période change
    LaunchedEffect(selectedPeriod) {
        loadTrendingPosts(reset = true)
    }
    
    // Détecter le scroll pour charger plus de posts
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (selectedSection == 0 && 
                    lastVisibleIndex != null && 
                    lastVisibleIndex >= trendingPosts.size - 3 && 
                    hasMorePosts && 
                    !isLoadingMorePosts && 
                    !isPostsLoading) {
                    postsCurrentPage++
                    loadTrendingPosts(reset = false)
                }
            }
    }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = stringResource(Strings.Trending.Title),
                onBackClick = onBack,
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs pour les sections
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text(stringResource(Strings.Trending.Posts)) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text(stringResource(Strings.Trending.Hashtags)) }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    text = { Text(stringResource(Strings.Trending.Salons)) }
                )
            }
            
            // Contenu selon la section
            when (selectedSection) {
                0 -> TrendingPostsSection(
                    posts = trendingPosts,
                    isLoading = isPostsLoading,
                    error = postsError,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it },
                    onRetry = { loadTrendingPosts(reset = true) },
                    isLoadingMore = isLoadingMorePosts,
                    currentUser = currentUser,
                    onNavigateToPost = onNavigateToPost,
                    onNavigateToComments = onNavigateToComments,
                    onNavigateToProfile = onNavigateToProfile
                )
                1 -> TrendingHashtagsSection(
                    hashtags = trendingHashtags,
                    isLoading = isHashtagsLoading,
                    error = hashtagsError,
                    onRetry = { loadTrendingHashtags() },
                    onHashtagClick = onNavigateToHashtag
                )
                2 -> TrendingSalonsSection(
                    salons = trendingSalons,
                    isLoading = isSalonsLoading,
                    error = salonsError,
                    onRetry = { loadTrendingSalons() },
                    onSalonClick = onNavigateToSalon
                )
            }
        }
    }
}

/**
 * Section Posts Trending.
 */
@Composable
private fun TrendingPostsSection(
    posts: List<PostResponse>,
    isLoading: Boolean,
    error: String?,
    selectedPeriod: TrendPeriod,
    onPeriodSelected: (TrendPeriod) -> Unit,
    onRetry: () -> Unit,
    isLoadingMore: Boolean,
    currentUser: User?,
    onNavigateToPost: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filtre par période
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrendPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = { 
                        Text(
                            when (period) {
                                TrendPeriod.LAST_24H -> "24h"
                                TrendPeriod.LAST_7D -> "7 jours"
                                TrendPeriod.LAST_30D -> "30 jours"
                            }
                        )
                    }
                )
            }
        }
        
        // Liste des posts
        when {
            isLoading -> {
                ListLoadingState()
            }
            error != null -> {
                ListErrorState(
                    message = error,
                    onRetry = onRetry
                )
            }
            posts.isEmpty() -> {
                ListEmptyState(
                    title = "Aucun post trending",
                    message = "Les posts les plus populaires apparaîtront ici",
                    icon = {
                        Icon(
                            Icons.Outlined.Article,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = posts,
                        key = { it.id }
                    ) { post ->
                        UltraPremiumPostCard(
                            post = post,
                            comments = emptyList<CommentResponse>(),
                            onLikeClick = { postId: String ->
                                // Optimistic update
                                // Note: Pour une implémentation complète, il faudrait gérer l'état local
                            },
                            onCommentClick = { postId: String -> onNavigateToComments(postId) },
                            onShareClick = { postId: String -> /* TODO */ },
                            onAuthorClick = { authorId: String -> onNavigateToProfile(authorId) },
                            onArchiveClick = { postId: String -> /* TODO */ },
                            onSaveToCollection = { postId: String -> /* TODO */ }
                        )
                    }
                    
                    // Indicateur de chargement pour la pagination
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section Hashtags Trending.
 */
@Composable
private fun TrendingHashtagsSection(
    hashtags: List<HairHashtagResponse>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onHashtagClick: (String) -> Unit
) {
    when {
        isLoading -> {
            ListLoadingState()
        }
        error != null -> {
            ListErrorState(
                message = error,
                onRetry = onRetry
            )
        }
        hashtags.isEmpty() -> {
            ListEmptyState(
                title = "Aucun hashtag trending",
                message = null,
                icon = {
                    Icon(
                        Icons.Outlined.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hashtags) { hashtag ->
                    TrendingHashtagCard(
                        hashtag = hashtag,
                        onClick = { onHashtagClick(hashtag.name) }
                    )
                }
            }
        }
    }
}

/**
 * Section Salons Trending.
 */
@Composable
private fun TrendingSalonsSection(
    salons: List<Salon>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onSalonClick: (String) -> Unit
) {
    when {
        isLoading -> {
            ListLoadingState()
        }
        error != null -> {
            ListErrorState(
                message = error,
                onRetry = onRetry
            )
        }
        salons.isEmpty() -> {
            ListEmptyState(
                title = "Aucun salon trending",
                message = null,
                icon = {
                    Icon(
                        Icons.Outlined.Store,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(salons) { salon ->
                    TrendingSalonCard(
                        salon = salon,
                        onClick = { onSalonClick(salon.id) }
                    )
                }
            }
        }
    }
}

/**
 * Carte pour un hashtag trending.
 */
@Composable
fun TrendingHashtagCard(
    hashtag: HairHashtagResponse,
    onClick: () -> Unit
) {
    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icône hashtag
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = "#${hashtag.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${hashtag.categoryEmoji} ${hashtag.categoryLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${hashtag.usageCount} utilisations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Carte pour un salon trending.
 */
@Composable
fun TrendingSalonCard(
    salon: Salon,
    onClick: () -> Unit
) {
    StandardCardNoPadding(
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
            // Image ou placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (salon.coverPhotoUrl != null) {
                    coil3.compose.AsyncImage(
                        model = salon.coverPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = salon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${salon.address}, ${salon.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

