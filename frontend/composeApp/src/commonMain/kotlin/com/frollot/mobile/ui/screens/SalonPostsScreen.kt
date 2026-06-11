package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.ExternalShareDialog
import com.frollot.mobile.ui.components.PullToRefreshBox
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.utils.rememberExternalShare
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran affichant tous les posts d'un salon.
 * Phase C.2 - Feed par Salon
 * 
 * Permet de :
 * - Voir tous les posts tagués avec le salon
 * - Filtrer par type de post
 * - Filtrer par service
 * - Trier par récence ou popularité
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonPostsScreen(
    salon: Salon,
    currentUser: User? = null,
    onBack: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToSalon: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onCreatePost: (() -> Unit)? = null // Callback pour créer un post de salon
) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var services by remember { mutableStateOf<List<SalonService>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Filtres
    var selectedPostType by remember { mutableStateOf<PostType?>(null) }
    var selectedServiceId by remember { mutableStateOf<String?>(null) }
    var selectedSortBy by remember { mutableStateOf(SortBy.RECENT) }
    
    // Pagination
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    val scrollState = rememberLazyListState()
    
    // Phase I.2 - Partage Externe
    val externalShare = rememberExternalShare()
    var showExternalShareDialog by remember { mutableStateOf(false) }
    var postToShareExternally by remember { mutableStateOf<PostResponse?>(null) }
    
    // Charger les services du salon
    LaunchedEffect(salon.id) {
        scope.launch {
            try {
                services = api.getSalonServices(salon.id)
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement services: ${e.message}")
            }
        }
    }
    
    // Fonction pour charger les posts
    // Phase I.1 - Pull-to-refresh et Infinite Scroll
    fun loadPosts(reset: Boolean = false) {
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
                hasError = false
                errorMessage = null
                
                val page = if (reset) 0 else currentPage
                val pageResponse = api.getPostsBySalon(
                    salonId = salon.id,
                    postType = selectedPostType,
                    serviceId = selectedServiceId,
                    sortBy = selectedSortBy,
                    page = page,
                    size = 20
                )
                
                if (reset) {
                    posts = pageResponse.content
                } else {
                    posts = posts + pageResponse.content
                }
                
                hasMore = pageResponse.content.size == 20 && (page + 1) * 20 < pageResponse.totalElements
                currentPage = page
                
                isLoading = false
                isLoadingMore = false
                isRefreshing = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur chargement posts: ${e.message}")
                errorMessage = "Erreur lors du chargement des posts"
                hasError = true
                isLoading = false
                isLoadingMore = false
                isRefreshing = false
            }
        }
    }
    
    // Charger les posts au démarrage et quand les filtres changent
    LaunchedEffect(salon.id, selectedPostType, selectedServiceId, selectedSortBy) {
        loadPosts(reset = true)
    }
    
    // Détecter le scroll pour charger plus
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= posts.size - 3 && 
                    hasMore && 
                    !isLoadingMore && 
                    !isLoading) {
                    currentPage++
                    loadPosts(reset = false)
                }
            }
    }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = stringResource(Strings.SalonPosts.Title).replace("{salonName}", salon.name),
                onBackClick = onBack,
                showAvatar = false
            )
        },
        floatingActionButton = {
            // Bouton pour créer un post de salon (visible uniquement pour le propriétaire)
            val isOwner = currentUser != null && currentUser.id == salon.ownerId
            if (isOwner) {
                FloatingActionButton(
                    onClick = {
                        onCreatePost?.invoke()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer un post pour ce salon"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header avec image de couverture et nom du salon
            SalonPostsHeader(salon = salon)
            
            // Filtres
            SalonPostsFilters(
                selectedPostType = selectedPostType,
                onPostTypeSelected = { selectedPostType = it },
                services = services,
                selectedServiceId = selectedServiceId,
                onServiceSelected = { selectedServiceId = it },
                selectedSortBy = selectedSortBy,
                onSortBySelected = { selectedSortBy = it }
            )
            
            // Liste des posts
            when {
                isLoading -> {
                    ListLoadingState()
                }
                hasError -> {
                    ListErrorState(
                        message = errorMessage ?: "Une erreur est survenue",
                        onRetry = { loadPosts(reset = true) }
                    )
                }
                posts.isEmpty() -> {
                    ListEmptyState(
                        title = "Aucun post pour ce salon",
                        message = "Les posts tagués avec ce salon apparaîtront ici",
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
                    // Phase I.1 - Pull-to-refresh et Infinite Scroll
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { loadPosts(reset = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                        items(
                            items = posts,
                            key = { it.id }
                        ) { post ->
                            // Réutiliser UltraPremiumPostCard depuis SocialFeedScreen
                            UltraPremiumPostCard(
                                post = post,
                                comments = emptyList<CommentResponse>(), // Les commentaires peuvent être chargés si nécessaire
                                onLikeClick = { postId: String ->
                                    // Optimistic update
                                    posts = posts.map { p ->
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
                                    // Appel API
                                    scope.launch {
                                        try {
                                            val updatedPost = api.toggleLike(postId)
                                            posts = posts.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        } catch (_: Exception) {
                                            // En cas d'erreur, recharger les posts
                                            loadPosts(reset = true)
                                        }
                                    }
                                },
                                onCommentClick = { postId: String -> onNavigateToComments(postId) },
                                onShareClick = { postId: String ->
                                    // Phase I.2 - Partage Externe
                                    postToShareExternally = posts.find { it.id == postId }
                                    showExternalShareDialog = true
                                },
                                onAuthorClick = { authorId: String -> onNavigateToProfile(authorId) },
                                onArchiveClick = { postId: String ->
                                    scope.launch {
                                        try {
                                            api.archivePost(postId)
                                            // Retirer le post de la liste locale
                                            posts = posts.filter { it.id != postId }
                                        } catch (_: Exception) {
                                            // En cas d'erreur, recharger les posts
                                            loadPosts(reset = true)
                                        }
                                    }
                                },
                                onSaveToCollection = { postId: String ->
                                    // Optimistic update
                                    posts = posts.map { p ->
                                        if (p.id == postId) {
                                            p.copy(isFavoritedByCurrentUser = !p.isFavoritedByCurrentUser)
                                        } else {
                                            p
                                        }
                                    }
                                    // Appel API
                                    scope.launch {
                                        try {
                                            val updatedPost = api.toggleFavorite(postId)
                                            posts = posts.map { p ->
                                                if (p.id == postId) updatedPost else p
                                            }
                                        } catch (_: Exception) {
                                            // En cas d'erreur, recharger les posts
                                            loadPosts(reset = true)
                                        }
                                    }
                                }
                            )
                        }
                        
                            // Phase I.1 - Indicateur de chargement pour la pagination
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

/**
 * Header avec image de couverture et nom du salon.
 */
@Composable
private fun SalonPostsHeader(salon: Salon) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Image de couverture
        if (salon.coverPhotoUrl != null) {
            AsyncImage(
                model = salon.coverPhotoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            )
        }
        
        // Overlay avec gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.scrim
                        )
                    )
                )
        )
        
        // Nom du salon
        Text(
            text = salon.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}

/**
 * Filtres pour les posts du salon.
 */
@Composable
private fun SalonPostsFilters(
    selectedPostType: PostType?,
    onPostTypeSelected: (PostType?) -> Unit,
    services: List<SalonService>,
    selectedServiceId: String?,
    onServiceSelected: (String?) -> Unit,
    selectedSortBy: SortBy,
    onSortBySelected: (SortBy) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filtre par type de post
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedPostType == null,
                onClick = { onPostTypeSelected(null) },
                label = { Text(stringResource(Strings.SalonPosts.All)) }
            )
            PostType.entries.forEach { postType ->
                FilterChip(
                    selected = selectedPostType == postType,
                    onClick = { onPostTypeSelected(postType) },
                    label = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(postType.getEmoji(), fontSize = 14.sp)
                            Text(postType.getLocalizedDisplayName())
                        }
                    }
                )
            }
        }
        
        // Filtre par service et tri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dropdown pour les services
            var expanded by remember { mutableStateOf(false) }
            Box {
                FilterChip(
                    selected = selectedServiceId != null,
                    onClick = { expanded = true },
                    label = { 
                        Text(
                            selectedServiceId?.let { serviceId ->
                                services.find { it.id == serviceId }?.name ?: stringResource(Strings.Common.Service)
                            } ?: stringResource(Strings.SalonPosts.AllServices)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Strings.SalonPosts.AllServices)) },
                        onClick = {
                            onServiceSelected(null)
                            expanded = false
                        }
                    )
                    services.forEach { service ->
                        DropdownMenuItem(
                            text = { Text(service.name) },
                            onClick = {
                                onServiceSelected(service.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Tri
            FilterChip(
                selected = selectedSortBy == SortBy.RECENT,
                onClick = { onSortBySelected(SortBy.RECENT) },
                label = { Text("Récents") }
            )
            FilterChip(
                selected = selectedSortBy == SortBy.POPULAR,
                onClick = { onSortBySelected(SortBy.POPULAR) },
                label = { Text(stringResource(Strings.SalonPosts.Popular)) }
            )
        }
    }
}


