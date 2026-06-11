package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran de recherche unifiée.
 * Phase C.1 - Recherche spécialisée coiffure
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    currentUser: User?,
    initialQuery: String = "",
    onBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToSalon: (String) -> Unit = {},
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToHashtag: (String) -> Unit = {},
    onNavigateToAuthor: ((String, UserType?) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedType by remember { mutableStateOf(SearchType.ALL) }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    
    // Filtres avancés (pour les posts)
    var selectedPostType by remember { mutableStateOf<PostType?>(null) }
    var selectedServiceId by remember { mutableStateOf<String?>(null) }
    var selectedSalonId by remember { mutableStateOf<String?>(null) }
    var selectedHashtagName by remember { mutableStateOf<String?>(null) }
    var selectedAuthorId by remember { mutableStateOf<String?>(null) }
    
    // Résultats de recherche
    var searchResults by remember { mutableStateOf<SearchResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    
    // Posts paginés (pour le type POSTS uniquement)
    var postsPage by remember { mutableStateOf<PageResponse<PostResponse>?>(null) }
    
    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }
    val scrollState = rememberLazyListState()
    
    // Fonction pour effectuer la recherche
    fun performSearch(showLoading: Boolean = true) {
        scope.launch {
            try {
                if (showLoading) isLoading = true
                hasError = false
                errorMessage = null
                
                val filters = SearchFilters(
                    postType = selectedPostType,
                    serviceId = selectedServiceId,
                    salonId = selectedSalonId,
                    hashtagName = selectedHashtagName,
                    authorId = selectedAuthorId
                )
                
                when (selectedType) {
                    SearchType.POSTS -> {
                        // Recherche paginée pour les posts
                        val page = if (searchQuery.isBlank() && !filters.hasAnyFilter()) {
                            // Si pas de query ni de filtre, on ne fait rien
                            null
                        } else {
                            api.searchPostsWithFilters(
                                query = searchQuery.takeIf { it.isNotBlank() },
                                postType = filters.postType,
                                serviceId = filters.serviceId,
                                salonId = filters.salonId,
                                hashtagName = filters.hashtagName,
                                authorId = filters.authorId,
                                page = currentPage,
                                size = 20
                            )
                        }
                        postsPage = page
                        if (page != null) {
                            hasMore = !page.last
                        }
                    }
                    else -> {
                        // Recherche unifiée pour les autres types
                        val results = api.unifiedSearch(
                            query = searchQuery.takeIf { it.isNotBlank() },
                            type = selectedType,
                            postType = filters.postType,
                            serviceId = filters.serviceId,
                            salonId = filters.salonId,
                            hashtagName = filters.hashtagName,
                            authorId = filters.authorId,
                            page = currentPage,
                            size = 20
                        )
                        searchResults = results
                    }
                }
                
                isLoading = false
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur recherche: ${e.message}")
                errorMessage = e.message
                hasError = true
                isLoading = false
            }
        }
    }
    
    // Debounce pour la recherche
    LaunchedEffect(searchQuery, selectedType, selectedPostType, selectedServiceId, selectedSalonId, selectedHashtagName, selectedAuthorId) {
        if (searchQuery.isNotBlank() || selectedType == SearchType.POSTS) {
            delay(500)
            currentPage = 0
            performSearch()
        } else {
            searchResults = null
            postsPage = null
        }
    }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = onNavigateToProfile,
                title = stringResource(Strings.Search.Title),
                showSearchBar = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                actions = {
                    // Bouton filtres avancés (uniquement pour les posts)
                    if (selectedType == SearchType.POSTS || selectedType == SearchType.ALL) {
                        IconButton(
                            onClick = { showAdvancedFilters = !showAdvancedFilters }
                        ) {
                            Icon(
                                if (showAdvancedFilters) Icons.Default.Tune else Icons.Outlined.Tune,
                                contentDescription = "Filtres avancés",
                                tint = if (showAdvancedFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Onglets pour les types de recherche
            ScrollableTabRow(
                selectedTabIndex = SearchType.entries.indexOf(selectedType),
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
            ) {
                SearchType.entries.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            currentPage = 0
                            searchResults = null
                            postsPage = null
                        },
                        text = {
                            Text(
                                text = when (type) {
                                    SearchType.POSTS -> "Posts"
                                    SearchType.SALONS -> "Salons"
                                    SearchType.USERS -> "Utilisateurs"
                                    SearchType.HASHTAGS -> "Hashtags"
                                    SearchType.ALL -> "Tout"
                                },
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Filtres avancés (pour les posts)
            if (showAdvancedFilters && (selectedType == SearchType.POSTS || selectedType == SearchType.ALL)) {
                StandardCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.Search.AdvancedFilters),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Filtre par type de post
                        Column {
                            Text(
                                text = stringResource(Strings.Search.PostType),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedPostType == null,
                                    onClick = { selectedPostType = null },
                                    label = { Text(stringResource(Strings.Search.All)) }
                                )
                                PostType.entries.forEach { type ->
                                    FilterChip(
                                        selected = selectedPostType == type,
                                        onClick = { selectedPostType = if (selectedPostType == type) null else type },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(type.getEmoji(), fontSize = 14.sp)
                                                Text(type.getLocalizedDisplayName())
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Note : Les autres filtres (service, salon, hashtag, auteur) nécessiteraient
                        // des sélecteurs plus complexes (dialogs, dropdowns) qui seront ajoutés dans une future version
                    }
                }
            }
            
            // Contenu des résultats
            when {
                isLoading && searchResults == null && postsPage == null -> {
                    ListLoadingState(
                        message = "Recherche en cours...",
                        modifier = Modifier.weight(1f)
                    )
                }
                hasError -> {
                    ListErrorState(
                        message = errorMessage ?: "Erreur lors de la recherche",
                        onRetry = { performSearch() },
                        modifier = Modifier.weight(1f)
                    )
                }
                searchQuery.isBlank() && selectedPostType == null && selectedType == SearchType.POSTS -> {
                    // Aucune recherche, afficher un message d'accueil
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = stringResource(Strings.Search.SearchPlaceholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    // Afficher les résultats selon le type
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedType) {
                            SearchType.POSTS -> {
                                val posts = postsPage?.content ?: emptyList()
                                if (posts.isEmpty() && !isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(Strings.Search.NoPostsFound),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = posts,
                                        key = { it.id }
                                    ) { post ->
                                        // Utiliser le même composant que SocialFeedScreen
                                        // Pour l'instant, un placeholder simple
                                        SearchPostCard(
                                            post = post,
                                            currentUser = currentUser,
                                            api = api,
                                            onClick = { onNavigateToPost(post.id) },
                                            onAuthorClick = onNavigateToAuthor
                                        )
                                    }
                                    
                                    // Bouton charger plus
                                    if (hasMore && !isLoading) {
                                        item {
                                            Button(
                                                onClick = {
                                                    currentPage++
                                                    performSearch(showLoading = false)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(stringResource(Strings.Search.LoadMore))
                                            }
                                        }
                                    }
                                }
                            }
                            SearchType.SALONS -> {
                                val salons = searchResults?.salons ?: emptyList()
                                if (salons.isEmpty() && !isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(Strings.Search.NoSalonsFound),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = salons,
                                        key = { it.id }
                                    ) { salon ->
                                        SearchSalonCard(
                                            salon = salon,
                                            onClick = { onNavigateToSalon(salon.id) }
                                        )
                                    }
                                }
                            }
                            SearchType.USERS -> {
                                val users = searchResults?.users ?: emptyList()
                                if (users.isEmpty() && !isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(Strings.Search.NoUsersFound),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = users,
                                        key = { it.id }
                                    ) { user ->
                                        SearchUserCard(
                                            user = user,
                                            onClick = { onNavigateToUser(user.id) }
                                        )
                                    }
                                }
                            }
                            SearchType.HASHTAGS -> {
                                val hashtags = searchResults?.hashtags ?: emptyList()
                                if (hashtags.isEmpty() && !isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(Strings.Search.NoHashtagsFound),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = hashtags,
                                        key = { it.id }
                                    ) { hashtag ->
                                        SearchHashtagCard(
                                            hashtag = hashtag,
                                            onClick = { onNavigateToHashtag(hashtag.name) }
                                        )
                                    }
                                }
                            }
                            SearchType.ALL -> {
                                // Afficher tous les résultats groupés
                                val results = searchResults
                                if (results == null || (results.posts.isEmpty() && results.salons.isEmpty() && results.users.isEmpty() && results.hashtags.isEmpty())) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(Strings.Search.NoResultsFound),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    // Posts
                                    if (results.posts.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = stringResource(Strings.Search.Posts).replace("{count}", results.totalPosts.toString()),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        items(
                                            items = results.posts,
                                            key = { it.id }
                                        ) { post ->
                                            SearchPostCard(
                                                post = post,
                                                currentUser = currentUser,
                                                api = api,
                                                onClick = { onNavigateToPost(post.id) }
                                            )
                                        }
                                    }
                                    
                                    // Salons
                                    if (results.salons.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = stringResource(Strings.Search.Salons).replace("{count}", results.totalSalons.toString()),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        items(
                                            items = results.salons,
                                            key = { it.id }
                                        ) { salon ->
                                            SearchSalonCard(
                                                salon = salon,
                                                onClick = { onNavigateToSalon(salon.id) }
                                            )
                                        }
                                    }
                                    
                                    // Utilisateurs
                                    if (results.users.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = stringResource(Strings.Search.Users).replace("{count}", results.totalUsers.toString()),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        items(
                                            items = results.users,
                                            key = { it.id }
                                        ) { user ->
                                            SearchUserCard(
                                                user = user,
                                                onClick = { onNavigateToUser(user.id) }
                                            )
                                        }
                                    }
                                    
                                    // Hashtags
                                    if (results.hashtags.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = stringResource(Strings.Search.Hashtags).replace("{count}", results.totalHashtags.toString()),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        items(
                                            items = results.hashtags,
                                            key = { it.id }
                                        ) { hashtag ->
                                            SearchHashtagCard(
                                                hashtag = hashtag,
                                                onClick = { onNavigateToHashtag(hashtag.name) }
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
}

// ============================================
// COMPOSANTS DE CARTES DE RECHERCHE
// ============================================

@Composable
fun SearchPostCard(
    post: PostResponse,
    currentUser: User?,
    api: FrollotApi,
    onClick: () -> Unit,
    onAuthorClick: ((String, UserType?) -> Unit)? = null
) {
    // currentUser et api sont réservés pour de futures fonctionnalités (like, comment, etc.)
    StandardCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Auteur et date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.clickable(enabled = onAuthorClick != null) {
                            onAuthorClick?.invoke(post.authorId, post.authorUserType)
                        }
                    ) {
                        UserAvatar(
                            user = User(
                                id = post.authorId,
                                email = post.authorEmail,
                                userType = post.authorUserType ?: UserType.client,
                                firstName = post.authorName.split(" ").firstOrNull(),
                                lastName = post.authorName.split(" ").drop(1).joinToString(" ").takeIf { it.isNotBlank() }
                            ),
                            size = 40.dp
                        )
                    }
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(enabled = onAuthorClick != null) {
                                onAuthorClick?.invoke(post.authorId, post.authorUserType)
                            }
                        )
                        Text(
                            text = post.formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Type de post
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${post.postType.getEmoji()} ${post.postType.getLocalizedDisplayName()}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Contenu
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
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
}

@Composable
fun SearchSalonCard(
    salon: Salon,
    onClick: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image de couverture ou placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (salon.coverPhotoUrl != null) {
                    AsyncImage(
                        model = salon.coverPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = salon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${salon.address}, ${salon.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (salon.description != null) {
                    Text(
                        text = salon.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SearchUserCard(
    user: User,
    onClick: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user, size = 56.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifBlank { user.email },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = user.userType.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchHashtagCard(
    hashtag: HairHashtagResponse,
    onClick: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#${hashtag.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${hashtag.categoryEmoji} ${hashtag.categoryLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "${hashtag.usageCount} utilisations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

