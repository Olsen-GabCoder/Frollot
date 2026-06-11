@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
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
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.ui.navigation.Route
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran affichant les collections d'un utilisateur.
 * Phase F.1 - Collections Thématiques
 */
@Composable
fun CollectionsScreen(
    userId: String,
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    var collections by remember { mutableStateOf<List<CollectionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadCollections() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val isOwner = currentUser?.id == userId
                collections = api.getCollectionsByUser(userId, includePrivate = isOwner)
            } catch (e: Exception) {
                errorMessage = "Erreur lors du chargement des collections: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        loadCollections()
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.Collections.Title),
                showAvatar = false
            )
        },
        floatingActionButton = {
            // Afficher le FAB uniquement si c'est l'utilisateur actuel
            if (currentUser?.id == userId) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Créer une collection")
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading && collections.isEmpty() -> {
                ListLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            errorMessage != null && collections.isEmpty() -> {
                ListErrorState(
                    message = errorMessage!!,
                    onRetry = { loadCollections() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            collections.isEmpty() -> {
                ListEmptyState(
                    title = "Aucune collection",
                    message = if (currentUser?.id == userId) {
                        "Créez votre première collection pour organiser vos posts favoris"
                    } else {
                        "Cet utilisateur n'a pas encore de collections"
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(collections, key = { it.id }) { collection ->
                        CollectionCard(
                            collection = collection,
                            isOwner = currentUser?.id == userId,
                            api = api,
                            onCollectionClick = {
                                onNavigate(Route.CollectionDetail(collection.id))
                            },
                            onEdit = {
                                // TODO: Ouvrir dialog d'édition
                            },
                            onDelete = {
                                scope.launch {
                                    try {
                                        api.deleteCollection(collection.id)
                                        loadCollections()
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur lors de la suppression: ${e.message}"
                                    }
                                }
                            },
                            onCollectionUpdated = {
                                loadCollections()
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog de création de collection
    if (showCreateDialog) {
        CreateCollectionDialog(
            api = api,
            onDismiss = { showCreateDialog = false },
            onCollectionCreated = {
                showCreateDialog = false
                loadCollections()
            }
        )
    }
}

@Composable
fun CollectionCard(
    collection: CollectionResponse,
    isOwner: Boolean,
    api: FrollotApi,
    onCollectionClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCollectionUpdated: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCollectionClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image de couverture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (collection.coverImageUrl != null) {
                    AsyncImage(
                        model = collection.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Contenu
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isOwner) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }
                    }
                }

                // Catégorie
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = collection.category)
                    if (!collection.isPublic) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Privé",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Description
                if (collection.description != null) {
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Nombre de posts
                Text(
                    text = "${collection.postsCount} ${if (collection.postsCount > 1) stringResource(Strings.Collections.Posts) else stringResource(Strings.Collections.Post)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Menu d'options
    if (showMenu) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Strings.Collections.Edit)) },
                onClick = {
                    showMenu = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Strings.Collections.Delete)) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun CategoryBadge(category: CollectionCategory) {
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

@Composable
private fun CreateCollectionDialog(
    api: FrollotApi,
    onDismiss: () -> Unit,
    onCollectionCreated: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coverImageUrl by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf(CollectionCategory.INSPIRATION) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Strings.Collections.NewCollection)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StandardTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Strings.Collections.Name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                StandardTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Strings.Collections.Description)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                StandardTextField(
                    value = coverImageUrl,
                    onValueChange = { coverImageUrl = it },
                    label = { Text("URL image de couverture (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Catégorie
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = when (category) {
                            CollectionCategory.INSPIRATION -> "Inspiration"
                            CollectionCategory.PORTFOLIO -> "Portfolio"
                            CollectionCategory.TENDANCE -> "Tendance"
                            CollectionCategory.PERSONNEL -> "Personnel"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Strings.Collections.Category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CollectionCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (cat) {
                                            CollectionCategory.INSPIRATION -> "Inspiration"
                                            CollectionCategory.PORTFOLIO -> "Portfolio"
                                            CollectionCategory.TENDANCE -> "Tendance"
                                            CollectionCategory.PERSONNEL -> "Personnel"
                                        }
                                    )
                                },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Visibilité
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Strings.Collections.PublicCollection))
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (isLoading) "Création..." else "Créer",
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Le nom est requis"
                        return@PrimaryButton
                    }
                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null
                            api.createCollection(
                                CreateCollectionRequest(
                                    name = name.trim(),
                                    description = description.takeIf { it.isNotBlank() },
                                    coverImageUrl = coverImageUrl.takeIf { it.isNotBlank() },
                                    isPublic = isPublic,
                                    category = category
                                )
                            )
                            onCollectionCreated()
                        } catch (e: Exception) {
                            errorMessage = "Erreur: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && name.isNotBlank(),
                icon = if (isLoading) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else null
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Strings.Common.Cancel))
            }
        }
    )
}

