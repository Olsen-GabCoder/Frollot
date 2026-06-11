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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun PortfoliosListScreen(
    ownerId: String,
    ownerType: String, // "coiffeur" ou "salon"
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit = {}
) {
    var portfolios by remember { mutableStateOf<List<PortfolioResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val portfolioOwnerType = when (ownerType) {
        "salon" -> PortfolioOwnerType.salon
        "coiffeur" -> PortfolioOwnerType.coiffeur
        else -> PortfolioOwnerType.coiffeur
    }

    fun loadPortfolios() {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                // Inclure les portfolios privés si l'utilisateur est le propriétaire
                val includePrivate = currentUser?.id == ownerId
                portfolios = api.getPortfoliosByOwner(ownerId, portfolioOwnerType, includePrivate)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement des portfolios: ${e.message}"
                hasError = true
                isLoading = false
            }
        }
    }

    LaunchedEffect(ownerId, ownerType) {
        loadPortfolios()
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            // Afficher le FAB uniquement si l'utilisateur est le propriétaire
            if (currentUser?.id == ownerId) {
                FloatingActionButton(
                    onClick = {
                        currentUser.id?.let { userId ->
                            onNavigate(Route.CreatePortfolio(userId, ownerType))
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nouveau portfolio")
                }
            }
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
                    message = errorMessage ?: "Erreur inconnue",
                    onRetry = { loadPortfolios() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            portfolios.isEmpty() -> {
                ListEmptyState(
                    title = "Aucun portfolio",
                    message = if (currentUser?.id == ownerId) {
                        "Créez votre premier portfolio pour organiser vos créations"
                    } else {
                        "Cet utilisateur n'a pas encore de portfolio"
                    },
                    icon = {
                        Icon(
                            Icons.Default.Collections,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    action = if (currentUser?.id == ownerId) {
                        {
                            PrimaryButton(
                                text = stringResource(Strings.PortfoliosList.CreatePortfolio),
                                onClick = {
                                    currentUser.id?.let { userId ->
                                        onNavigate(Route.CreatePortfolio(userId, ownerType))
                                    }
                                },
                                icon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            )
                        }
                    } else null,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = portfolios,
                        key = { it.id }
                    ) { portfolio ->
                        StandardCardNoPadding(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigate(Route.PortfolioDetail(portfolio.id))
                                }
                        ) {
                            Column {
                                // Image de couverture
                                if (portfolio.coverImageUrl != null) {
                                    AsyncImage(
                                        model = portfolio.coverImageUrl,
                                        contentDescription = portfolio.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                // Informations
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = portfolio.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (!portfolio.isPublic) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Privé",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (portfolio.description != null) {
                                        Text(
                                            text = portfolio.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Image,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${portfolio.postsCount} post${if (portfolio.postsCount > 1) "s" else ""}",
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
            }
        }
    }
}

