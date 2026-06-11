package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.navigation.Route
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioDetailScreen(
    portfolioId: String,
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit = {}
) {
    var portfolio by remember { mutableStateOf<PortfolioResponse?>(null) }
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    fun loadPortfolio() {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                portfolio = api.getPortfolioById(portfolioId)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement du portfolio: ${e.message}"
                hasError = true
                isLoading = false
            }
        }
    }

    fun loadPosts(showLoading: Boolean = true) {
        scope.launch {
            try {
                if (showLoading) isLoading = true
                hasError = false
                val pageResponse = api.getPortfolioPosts(portfolioId, page = currentPage, size = 20)
                posts = if (currentPage == 0) {
                    pageResponse.content
                } else {
                    posts + pageResponse.content
                }
                hasMore = !pageResponse.last
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement des posts: ${e.message}"
                hasError = true
                isLoading = false
            }
        }
    }

    LaunchedEffect(portfolioId) {
        loadPortfolio()
        loadPosts()
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        when {
            isLoading && portfolio == null -> {
                ListLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            hasError -> {
                ListErrorState(
                    message = errorMessage ?: "Erreur inconnue",
                    onRetry = { loadPortfolio(); loadPosts() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            portfolio != null -> {
                val portfolioData = portfolio!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Header du portfolio
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Image de couverture
                            if (portfolioData.coverImageUrl != null) {
                                AsyncImage(
                                    model = portfolioData.coverImageUrl,
                                    contentDescription = "Couverture",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Informations du portfolio
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = portfolioData.name,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${portfolioData.postsCount} post${if (portfolioData.postsCount > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!portfolioData.isPublic) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Privé",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (portfolioData.description != null) {
                                    Text(
                                        text = portfolioData.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Divider()
                            }
                        }
                    }

                    // Liste des posts
                    if (posts.isEmpty() && !isLoading) {
                        item {
                            ListEmptyState(
                                title = "Aucun post dans ce portfolio",
                                message = null,
                                icon = {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.padding(40.dp)
                            )
                        }
                    } else {
                        items(
                            items = posts,
                            key = { it.id }
                        ) { post ->
                            // Utiliser UltraPremiumPostCard depuis SocialFeedScreen
                            // Pour l'instant, un placeholder simple
                            StandardCardNoPadding(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        // TODO: Navigation vers le détail du post
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = post.content,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 3
                                    )
                                    if (post.imageUrl != null) {
                                        AsyncImage(
                                            model = post.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
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
                                                modifier = Modifier.size(16.dp)
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

                        // Charger plus
                        if (hasMore && !isLoading) {
                            item {
                                PrimaryButton(
                                    text = stringResource(Strings.PortfolioDetail.LoadMore),
                                    onClick = {
                                        currentPage++
                                        loadPosts(showLoading = false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }

                        if (isLoading) {
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
}

