package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.rememberImagePicker
import com.frollot.mobile.ui.components.toImageBitmap
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.AnnotatedTextField
import com.frollot.mobile.ui.components.MentionSuggestion
import com.frollot.mobile.ui.components.MentionType
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.utils.extractHashtags
import com.frollot.mobile.ui.utils.extractMentions
import com.frollot.mobile.ui.utils.AnimationSpecs
import com.frollot.mobile.localization.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.frollot.mobile.config.FrollotLogger


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    currentUser: User,
    api: FrollotApi,
    onBack: () -> Unit,
    onPostCreated: () -> Unit = {},
    salonId: String? = null // ID du salon pour pré-remplir le tag (posts de salon)
) {
    var content by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(PostType.GENERAL) }
    var selectedVisibility by remember { mutableStateOf(PostVisibility.PUBLIC) } // Phase F.3 - Visibilité des Posts
    
    // Structure pour gérer plusieurs images avec leurs types
    data class MediaItem(
        val id: String = Random.nextLong().toString(),
        val bytes: ByteArray,
        val preview: ImageBitmap,
        var mediaType: PostMediaType? = null,
        val orderIndex: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MediaItem) return false
            return id == other.id
        }
        override fun hashCode(): Int = id.hashCode()
    }
    
    var selectedMedia by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    
    // Rétrocompatibilité : garder les anciennes variables pour les posts simples
    // Note: Ces variables sont utilisées pour la rétrocompatibilité avec les posts GENERAL
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }
    
    var isUploading by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Suggestions pour les mentions @
    var mentionQuery by remember { mutableStateOf("") }
    var mentionSuggestions by remember { mutableStateOf<List<MentionSuggestion>>(emptyList()) }
    var showMentionSuggestions by remember { mutableStateOf(false) }
    var isSearchingMentions by remember { mutableStateOf(false) }
    
    // Suggestions pour les hashtags #
    var hashtagQuery by remember { mutableStateOf("") }
    var hashtagSuggestions by remember { mutableStateOf<List<HairHashtagResponse>>(emptyList()) }
    var showHashtagSuggestions by remember { mutableStateOf(false) }
    var isSearchingHashtags by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            val preview = bytes.toImageBitmap()
            if (preview == null) {
                errorMessage = "Erreur lors du chargement de l'image"
            } else {
                // Déterminer le type de média par défaut selon le type de post
                val defaultMediaType = when (selectedPostType) {
                    PostType.AVANT_APRES -> {
                        // Pour AVANT_APRES : before pour la première, after pour la deuxième, puis detail
                        when (selectedMedia.size) {
                            0 -> PostMediaType.before
                            1 -> PostMediaType.after
                            else -> PostMediaType.detail
                        }
                    }
                    else -> PostMediaType.detail // Pour les autres types, par défaut "detail"
                }
                
                // Ajouter à la liste des médias
                val newMediaItem = MediaItem(
                    bytes = bytes,
                    preview = preview,
                    mediaType = defaultMediaType,
                    orderIndex = selectedMedia.size
                )
                selectedMedia = selectedMedia + newMediaItem
                
                // Rétrocompatibilité : pour les posts simples (GENERAL), garder aussi les anciennes variables
                if (selectedPostType == PostType.GENERAL && selectedMedia.size == 1) {
                    selectedImageBytes = bytes
                    imagePreview = preview
                }
            }
        }
    }

    val animatedCharCount by animateIntAsState(
        targetValue = content.length,
        animationSpec = AnimationSpecs.SoftTouchInteractionInt
    )

    // Recherche pour les mentions @ (utilisateurs et salons)
    LaunchedEffect(mentionQuery) {
        if (mentionQuery.isNotBlank() && mentionQuery.length >= 2) {
            delay(300) // Debounce
            isSearchingMentions = true
            showMentionSuggestions = true
            try {
                val users = api.searchUsers(mentionQuery)
                val salons = api.getSalons(query = mentionQuery)
                
                val suggestions = mutableListOf<MentionSuggestion>()
                
                // Ajouter les utilisateurs
                users.take(5).forEach { user ->
                    suggestions.add(
                        MentionSuggestion(
                            id = user.id!!,
                            displayName = user.firstName ?: user.email,
                            subtitle = user.email,
                            type = MentionType.USER
                        )
                    )
                }
                
                // Ajouter les salons
                salons.take(5).forEach { salon ->
                    suggestions.add(
                        MentionSuggestion(
                            id = salon.id,
                            displayName = salon.name,
                            subtitle = "${salon.city}, ${salon.address}",
                            type = MentionType.SALON
                        )
                    )
                }
                
                mentionSuggestions = suggestions
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur recherche mentions: ${e.message}")
                mentionSuggestions = emptyList()
            } finally {
                isSearchingMentions = false
            }
        } else {
            mentionSuggestions = emptyList()
            showMentionSuggestions = false
        }
    }

    // Effect pour rechercher des hashtags
    LaunchedEffect(hashtagQuery) {
        if (hashtagQuery.isNotBlank() && hashtagQuery.length >= 1) {
            delay(300) // Debounce
            try {
                isSearchingHashtags = true
                val suggestions = api.suggestHashtags(hashtagQuery, limit = 10)
                hashtagSuggestions = suggestions
                showHashtagSuggestions = suggestions.isNotEmpty()
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur recherche hashtags: ${e.message}")
                hashtagSuggestions = emptyList()
                showHashtagSuggestions = false
            } finally {
                isSearchingHashtags = false
            }
        } else {
            hashtagSuggestions = emptyList()
            showHashtagSuggestions = false
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = "Créer un post",
                showAvatar = false,
                actions = {
                    PrimaryButton(
                        text = if (isPosting || isUploading) {
                            if (isUploading) "Upload..." else "Publication..."
                        } else {
                            "Publier"
                        },
                        onClick = {
                            scope.launch(Dispatchers.Default) {
                                try {
                                    withContext(Dispatchers.Main) {
                                        isPosting = true
                                        errorMessage = null
                                    }

                                    val currentToken = api.getAuthToken()
                                    if (currentToken == null) {
                                        withContext(Dispatchers.Main) {
                                            errorMessage = "Non authentifié - Token JWT manquant"
                                            isPosting = false
                                        }
                                        return@launch
                                    }

                                    if (content.isBlank()) {
                                        withContext(Dispatchers.Main) {
                                            errorMessage = "Le contenu ne peut pas être vide"
                                            isPosting = false
                                        }
                                        return@launch
                                    }
                                    
                                    // Validation spécifique pour AVANT_APRES : au moins 2 images (before + after)
                                    if (selectedPostType == PostType.AVANT_APRES) {
                                        if (selectedMedia.size < 2) {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "Un post Avant/Après nécessite au moins 2 images (une 'Avant' et une 'Après')"
                                                isPosting = false
                                            }
                                            return@launch
                                        }
                                        
                                        // Vérifier qu'il y a au moins une image "before" et une "after"
                                        val hasBefore = selectedMedia.any { it.mediaType == PostMediaType.before }
                                        val hasAfter = selectedMedia.any { it.mediaType == PostMediaType.after }
                                        
                                        if (!hasBefore || !hasAfter) {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "Un post Avant/Après doit contenir au moins une image 'Avant' et une image 'Après'"
                                                isPosting = false
                                            }
                                            return@launch
                                        }
                                    }

                                    // Upload des images (multi-images support)
                                    var uploadedImageUrl: String? = null // Rétrocompatibilité : première image
                                    val uploadedMedia = mutableListOf<CreatePostMediaRequest>()
                                    
                                    if (selectedMedia.isNotEmpty()) {
                                        try {
                                            withContext(Dispatchers.Main) { isUploading = true }
                                            
                                            // Uploader toutes les images séquentiellement
                                            selectedMedia.forEachIndexed { index, mediaItem ->
                                                val fileName = "post_${Random.nextLong()}_${index}.jpg"
                                                val mediaUrl = api.uploadImage(mediaItem.bytes, fileName)
                                                
                                                // Rétrocompatibilité : première image dans imageUrl
                                                if (index == 0) {
                                                    uploadedImageUrl = mediaUrl
                                                }
                                                
                                                // Créer le CreatePostMediaRequest
                                                val mediaType = mediaItem.mediaType ?: PostMediaType.detail
                                                uploadedMedia.add(
                                                    CreatePostMediaRequest(
                                                        mediaUrl = mediaUrl,
                                                        mediaType = mediaType,
                                                        orderIndex = mediaItem.orderIndex
                                                    )
                                                )
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "Erreur lors de l'upload des images: ${e.message}"
                                                isPosting = false
                                                isUploading = false
                                            }
                                            return@launch
                                        } finally {
                                            withContext(Dispatchers.Main) { isUploading = false }
                                        }
                                    } else {
                                        // Rétrocompatibilité : upload d'une seule image (ancien système)
                                        selectedImageBytes?.let { bytes ->
                                            try {
                                                withContext(Dispatchers.Main) { isUploading = true }
                                                val fileName = "post_${Random.nextLong()}.jpg"
                                                uploadedImageUrl = api.uploadImage(bytes, fileName)
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    errorMessage = "Erreur lors de l'upload de l'image: ${e.message}"
                                                    isPosting = false
                                                    isUploading = false
                                                }
                                                return@launch
                                            } finally {
                                                withContext(Dispatchers.Main) { isUploading = false }
                                            }
                                        }
                                    }

                                    // Extraire les mentions (@) du texte et les convertir en tags
                                    val mentions = extractMentions(content)
                                    val extractedTags = mutableListOf<CreateTagRequest>()
                                    
                                    // Si salonId est fourni, ajouter automatiquement le tag salon (post de salon)
                                    if (salonId != null) {
                                        extractedTags.add(
                                            CreateTagRequest(
                                                taggedType = TaggedType.salon,
                                                taggedId = salonId
                                            )
                                        )
                                    }
                                    
                                    // Pour chaque mention, essayer de trouver l'utilisateur ou le salon correspondant
                                    mentions.forEach { (mentionText, _) ->
                                        // Rechercher dans les suggestions récentes ou faire une recherche
                                        val suggestion = mentionSuggestions.find { 
                                            it.displayName.contains(mentionText, ignoreCase = true) ||
                                            it.id.contains(mentionText, ignoreCase = true)
                                        }
                                        
                                        if (suggestion != null) {
                                            extractedTags.add(
                                                CreateTagRequest(
                                                    taggedType = if (suggestion.type == MentionType.SALON) TaggedType.salon else TaggedType.user,
                                                    taggedId = suggestion.id
                                                )
                                            )
                                        }
                                    }

                                    val request = com.frollot.mobile.model.CreatePostRequest(
                                        authorId = currentUser.id!!,
                                        content = content.trim(),
                                        imageUrl = uploadedImageUrl, // Rétrocompatibilité
                                        postType = selectedPostType,
                                        visibility = selectedVisibility, // Phase F.3 - Visibilité des Posts
                                        tags = extractedTags.distinctBy { "${it.taggedType}_${it.taggedId}" },
                                        media = uploadedMedia // Médias multiples (Phase B.5)
                                    )

                                    api.createPost(request)
                                    withContext(Dispatchers.Main) { onPostCreated() }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        errorMessage = e.message ?: "Erreur lors de la création du post"
                                        isPosting = false
                                    }
                                }
                            }
                        },
                        enabled = !isPosting && !isUploading && content.isNotBlank(),
                        modifier = Modifier.height(40.dp),
                        icon = if (!isPosting && !isUploading) {
                            {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StandardCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser.firstName?.firstOrNull() ?: currentUser.email.firstOrNull())?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = currentUser.firstName ?: currentUser.email,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Strings.CreatePost.Public),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    // Sélecteur de type de post
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.CreatePost.PostType),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PostType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedPostType == type,
                                    onClick = { selectedPostType = type },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = type.getEmoji(),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = type.getLocalizedDisplayName(),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Phase F.3 - Sélecteur de visibilité
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.CreatePost.Visibility),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PostVisibility.entries.forEach { visibility ->
                                FilterChip(
                                    selected = selectedVisibility == visibility,
                                    onClick = { selectedVisibility = visibility },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = visibility.getEmoji(),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = visibility.getLocalizedDisplayName(),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Champ de texte avec détection # et @
                    AnnotatedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        placeholder = "Quoi de neuf ? Utilisez # pour les hashtags et @ pour mentionner...",
                        singleLine = false,
                        minLines = 6,
                        onMentionQuery = { query ->
                            mentionQuery = query
                        },
                        suggestions = mentionSuggestions,
                        onMentionSelected = { suggestion ->
                            // Remplacer la mention en cours par le nom complet
                            val textBeforeCursor = content.take(content.length)
                            val mentionMatch = Regex("@(\\w*)$").find(textBeforeCursor)
                            if (mentionMatch != null) {
                                val start = mentionMatch.range.first
                                val end = mentionMatch.range.last + 1
                                content = content.substring(0, start) + "@${suggestion.displayName} " + content.substring(end)
                            }
                            showMentionSuggestions = false
                            mentionQuery = ""
                        },
                        showSuggestions = showMentionSuggestions && mentionQuery.isNotBlank(),
                        onHashtagQuery = { query ->
                            hashtagQuery = query
                        },
                        hashtagSuggestions = hashtagSuggestions,
                        onHashtagSelected = { hashtag ->
                            // Remplacer le hashtag en cours par le nom complet
                            val textBeforeCursor = content.take(content.length)
                            val hashtagMatch = Regex("#(\\w*)$").find(textBeforeCursor)
                            if (hashtagMatch != null) {
                                val start = hashtagMatch.range.first
                                val end = hashtagMatch.range.last + 1
                                content = content.substring(0, start) + "#${hashtag.name} " + content.substring(end)
                            }
                            showHashtagSuggestions = false
                            hashtagQuery = ""
                        },
                        showHashtagSuggestions = showHashtagSuggestions && hashtagQuery.isNotBlank()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AnimatedContent(
                            targetState = animatedCharCount,
                            transitionSpec = {
                                fadeIn() + scaleIn(initialScale = 0.8f) togetherWith fadeOut() + scaleOut()
                            }
                        ) { count ->
                            Text(
                                text = "$count caractères",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (count > 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            StandardCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = if (selectedMedia.isEmpty()) "Ajouter des images" else "${selectedMedia.size} image${if (selectedMedia.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                // Aide contextuelle pour AVANT_APRES
                                if (selectedPostType == PostType.AVANT_APRES && selectedMedia.isEmpty()) {
                                    Text(
                                        text = stringResource(Strings.CreatePost.AtLeastTwoImages),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        if (selectedMedia.size < 6) {
                            FilledTonalButton(
                                onClick = { imagePicker.launch() },
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isUploading && !isPosting
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ajouter")
                            }
                        }
                    }

                    // Grille horizontale des images sélectionnées
                    if (selectedMedia.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                selectedMedia.forEachIndexed { index, mediaItem ->
                                    Column(
                                        modifier = Modifier.width(120.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        ) {
                                            Image(
                                                bitmap = mediaItem.preview,
                                                contentDescription = "Aperçu ${index + 1}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            
                                            // Badge du type de média (pour AVANT_APRES)
                                            if (selectedPostType == PostType.AVANT_APRES && mediaItem.mediaType != null) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                                    shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 12.dp),
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                ) {
                                                    Text(
                                                        text = "${mediaItem.mediaType!!.getEmoji()} ${mediaItem.mediaType!!.getLocalizedDisplayName()}",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                            
                                            // Bouton de suppression
                                            FilledIconButton(
                                                onClick = {
                                                    selectedMedia = selectedMedia.filter { it.id != mediaItem.id }
                                                        .mapIndexed { idx, item -> item.copy(orderIndex = idx) }
                                                    // Rétrocompatibilité : mettre à jour les anciennes variables si nécessaire
                                                    if (selectedPostType == PostType.GENERAL) {
                                                        if (selectedMedia.isEmpty()) {
                                                            selectedImageBytes = null
                                                            imagePreview = null
                                                        } else if (selectedMedia.size == 1) {
                                                            selectedImageBytes = selectedMedia[0].bytes
                                                            imagePreview = selectedMedia[0].preview
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(32.dp)
                                                    .padding(4.dp),
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = Color.White.copy(alpha = 0.9f),
                                                    contentColor = Color.Black
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Supprimer",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        
                                        // Sélecteur de type de média (uniquement pour AVANT_APRES)
                                        if (selectedPostType == PostType.AVANT_APRES) {
                                            var expanded by remember { mutableStateOf(false) }
                                            Box {
                                                FilledTonalButton(
                                                    onClick = { expanded = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = mediaItem.mediaType?.let { "${it.getEmoji()} ${it.getLocalizedDisplayName()}" } ?: "Type",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                                
                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    PostMediaType.entries.forEach { type ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Text(text = type.getEmoji(), fontSize = 14.sp)
                                                                    Text(text = type.getLocalizedDisplayName())
                                                                }
                                                            },
                                                            onClick = {
                                                                selectedMedia = selectedMedia.map { item ->
                                                                    if (item.id == mediaItem.id) {
                                                                        item.copy(mediaType = type)
                                                                    } else {
                                                                        item
                                                                    }
                                                                }
                                                                expanded = false
                                                            }
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

                    // Zone de drop/upload (affichée si moins de 6 images)
                    AnimatedVisibility(
                        visible = selectedMedia.isEmpty() || selectedMedia.size < 6,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clickable { imagePicker.launch() },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(Strings.CreatePost.ClickToSelectPhoto),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(Strings.CreatePost.PhotoFormatHint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }


            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Strings.CreatePost.PostWillBeVisible),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
