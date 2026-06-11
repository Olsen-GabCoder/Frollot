package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil3.compose.AsyncImage
import kotlinx.datetime.Clock
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.rememberImagePicker
import com.frollot.mobile.ui.components.toImageBitmap
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.time.currentTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePortfolioScreen(
    currentUser: User,
    ownerId: String,
    ownerType: String, // "coiffeur" ou "salon"
    api: FrollotApi,
    onBack: () -> Unit,
    onPortfolioCreated: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imagePreviewUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Pour les salons, charger la liste des salons du propriétaire
    var ownerSalons by remember { mutableStateOf<List<Salon>>(emptyList()) }
    var selectedSalonId by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            selectedImageBytes = bytes
        }
    }

    // Charger les salons si ownerType est "salon"
    LaunchedEffect(ownerType, ownerId) {
        if (ownerType == "salon") {
            scope.launch {
                try {
                    ownerSalons = api.getSalonsByOwner(ownerId)
                    selectedSalonId = ownerSalons.firstOrNull()?.id
                } catch (e: Exception) {
                    errorMessage = "Erreur lors du chargement des salons"
                }
            }
        }
    }

    fun createPortfolio() {
        if (name.isBlank()) {
            errorMessage = "Le nom du portfolio est requis"
            return
        }

        scope.launch {
            try {
                isCreating = true
                errorMessage = null

                // Upload de l'image si présente
                var coverImageUrl: String? = null
                selectedImageBytes?.let { bytes ->
                    isUploading = true
                    try {
                        coverImageUrl = api.uploadImage(bytes, "portfolio_cover_${currentTimeMillis()}.jpg")
                    } catch (e: Exception) {
                        errorMessage = "Erreur lors de l'upload de l'image: ${e.message}"
                        isCreating = false
                        isUploading = false
                        return@launch
                    } finally {
                        isUploading = false
                    }
                }

                // Déterminer le type de propriétaire
                val portfolioOwnerType = when (ownerType) {
                    "salon" -> PortfolioOwnerType.salon
                    "coiffeur" -> PortfolioOwnerType.coiffeur
                    else -> PortfolioOwnerType.coiffeur
                }

                // Déterminer l'ID du propriétaire
                val finalOwnerId = when (portfolioOwnerType) {
                    PortfolioOwnerType.salon -> selectedSalonId ?: ownerId
                    PortfolioOwnerType.coiffeur -> currentUser.id!!
                }

                if (portfolioOwnerType == PortfolioOwnerType.salon && finalOwnerId == null) {
                    errorMessage = "Veuillez sélectionner un salon"
                    isCreating = false
                    return@launch
                }

                // Créer le portfolio
                val request = CreatePortfolioRequest(
                    ownerType = portfolioOwnerType,
                    ownerId = finalOwnerId,
                    name = name.trim(),
                    description = description.trim().takeIf { it.isNotBlank() },
                    coverImageUrl = coverImageUrl,
                    isPublic = isPublic
                )

                api.createPortfolio(request)
                
                // Succès
                onPortfolioCreated()
            } catch (e: Exception) {
                errorMessage = "Erreur lors de la création du portfolio: ${e.message}"
                isCreating = false
            } finally {
                isCreating = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Titre
            Text(
                text = stringResource(Strings.CreatePortfolio.Title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Carte principale
            StandardCardNoPadding(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type de propriétaire (affichage seulement)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (ownerType == "salon") Icons.Default.Store else Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (ownerType == "salon") "Portfolio Salon" else "Portfolio Coiffeur",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider()

                    // Sélection du salon (si ownerType est "salon")
                    if (ownerType == "salon") {
                        if (ownerSalons.isEmpty()) {
                            Text(
                                text = stringResource(Strings.CreatePortfolio.NoSalonFound),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = stringResource(Strings.CreatePortfolio.Salon),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            ownerSalons.forEach { salon ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSalonId = salon.id }
                                        .padding(12.dp)
                                        .background(
                                            if (selectedSalonId == salon.id)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else
                                                Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedSalonId == salon.id,
                                        onClick = { selectedSalonId = salon.id }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = salon.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${salon.city}, ${salon.address}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Divider()
                    }

                    // Nom du portfolio
                    StandardTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Strings.CreatePortfolio.Name)) },
                        placeholder = { Text(stringResource(Strings.CreatePortfolio.NamePlaceholder)) },
                        singleLine = true
                    )

                    // Description
                    StandardTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = { Text(stringResource(Strings.CreatePortfolio.Description)) },
                        placeholder = { Text("Décrivez votre portfolio...") },
                        maxLines = 5
                    )

                    // Image de couverture
                    Text(
                        text = stringResource(Strings.CreatePortfolio.CoverImage),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )

                    // Aperçu de l'image
                    if (imagePreviewUrl != null || selectedImageBytes != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            if (imagePreviewUrl != null) {
                                AsyncImage(
                                    model = imagePreviewUrl,
                                    contentDescription = "Aperçu",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (selectedImageBytes != null) {
                                // TODO: Afficher l'aperçu depuis bytes
                            }
                            
                            // Bouton pour changer l'image
                            IconButton(
                                onClick = { imagePicker.launch() },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Changer l'image",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        SecondaryButton(
                            text = stringResource(Strings.CreatePortfolio.AddCoverImage),
                            onClick = { imagePicker.launch() },
                            modifier = Modifier.fillMaxWidth(),
                            icon = {
                                Icon(Icons.Default.Image, contentDescription = null)
                            }
                        )
                    }

                    // Visibilité
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Strings.CreatePortfolio.PublicPortfolio),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = isPublic,
                            onCheckedChange = { isPublic = it }
                        )
                    }

                    // Message d'erreur
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Bouton de création
                    PrimaryButton(
                        text = if (isUploading) "Upload en cours..." else if (isCreating) "Création..." else "Créer le portfolio",
                        onClick = { createPortfolio() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating && !isUploading && name.isNotBlank() && (ownerType != "salon" || selectedSalonId != null),
                        icon = if (!isCreating && !isUploading) {
                            {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        } else if (isCreating || isUploading) {
                            {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

