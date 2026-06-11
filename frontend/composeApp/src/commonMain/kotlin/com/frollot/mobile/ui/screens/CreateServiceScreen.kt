package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Composant pour afficher une catégorie de service sélectionnable
 */
@Composable
fun CategoryItem(
    category: ServiceCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.dp else 0.5.dp,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.getEmoji(),
                    fontSize = 18.sp
                )
            }
            Text(
                text = category.getLocalizedDisplayName(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    salonId: String,
    onBack: () -> Unit,
    onServiceCreated: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("30") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ServiceCategory.COUPE) }

    var isCreating by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    val scrollState = rememberScrollState()

    // Validation du formulaire
    val isFormValid by remember(name, durationMinutes, price) {
        derivedStateOf {
            val nameValid = name.isNotBlank()
            val durationValid = durationMinutes.toIntOrNull()?.let { it in 1..480 } ?: false
            val priceValid = price.toDoubleOrNull()?.let { it in 0.0..10000.0 } ?: false
            nameValid && durationValid && priceValid
        }
    }

    // Fonction de création de service
    fun createService() {
        scope.launch {
            try {
                // Validation
                if (name.isBlank()) {
                    errorMessage = "Le nom est obligatoire"
                    return@launch
                }

                val duration = durationMinutes.toIntOrNull()
                if (duration == null || duration <= 0 || duration > 480) {
                    errorMessage = "La durée doit être entre 1 et 480 minutes"
                    return@launch
                }

                val priceValue = price.toDoubleOrNull()
                if (priceValue == null || priceValue < 0 || priceValue > 10000) {
                    errorMessage = "Le prix doit être entre 0 et 10 000€"
                    return@launch
                }

                isCreating = true
                errorMessage = null

                val request = CreateServiceRequest(
                    salonId = salonId,
                    name = name.trim(),
                    description = description.trim().takeIf { it.isNotBlank() },
                    durationMinutes = duration,
                    price = priceValue.toString(),
                    category = selectedCategory
                )

                api.createSalonService(request)
                successMessage = "✨ $name créé avec succès !"

                kotlinx.coroutines.delay(1500)
                onServiceCreated()
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("403") == true || 
                    e.message?.contains("Forbidden") == true ||
                    e.message?.contains("autorisé") == true ->
                        "Vous n'êtes pas autorisé à créer des services pour ce salon. Seul le propriétaire peut le faire."
                    e.message?.contains("401") == true ||
                    e.message?.contains("Unauthorized") == true ->
                        "Votre session a expiré. Veuillez vous reconnecter."
                    e.message?.contains("existe déjà") == true ->
                        "Un service avec ce nom existe déjà dans ce salon."
                    else -> e.message ?: "Erreur lors de la création du service"
                }
            } finally {
                isCreating = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = null,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = "Nouvelle prestation",
                showAvatar = false
            )
        },
        bottomBar = {
            // Bouton fixe en bas (optionnel, décommenter si besoin)
            /*
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { createService() },
                        enabled = isFormValid && !isCreating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isCreating) {
                            StandardLoadingIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Création en cours...",
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Créer le service",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            */
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Espace pour compenser le header
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Hero section
                    StandardCardNoPadding(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = stringResource(Strings.CreateService.PremiumService),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Messages feedback
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let { message ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = successMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        successMessage?.let { message ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    // Section Informations
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.CreateService.ServiceInfo),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Nom du service
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StandardTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(Strings.CreateService.Name)) },
                                placeholder = { Text(stringResource(Strings.CreateService.NamePlaceholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isCreating
                            )
                        }

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StandardTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text(stringResource(Strings.CreateService.Description)) },
                                placeholder = { Text("Décrivez votre prestation...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                maxLines = 3,
                                enabled = !isCreating
                            )
                        }

                        // Durée et Prix
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Durée
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StandardTextField(
                                    value = durationMinutes,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) {
                                            durationMinutes = newValue
                                        }
                                    },
                                    label = { Text(stringResource(Strings.CreateService.Duration)) },
                                    placeholder = { Text("30") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    enabled = !isCreating
                                )
                            }

                            // Prix
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StandardTextField(
                                    value = price,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            price = newValue
                                        }
                                    },
                                    label = { Text(stringResource(Strings.CreateService.Price)) },
                                    placeholder = { Text("25.50") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                    ),
                                    enabled = !isCreating
                                )
                            }
                        }

                        // Catégories - Design moderne
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = stringResource(Strings.CreateService.Category),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            StandardCardNoPadding(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                ServiceCategory.entries.chunked(2).forEach { rowCategories ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowCategories.forEach { category ->
                                            CategoryItem(
                                                category = category,
                                                isSelected = selectedCategory == category,
                                                onClick = { selectedCategory = category },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        // Si seulement 1 élément dans la dernière ligne, ajouter un espace vide
                                        if (rowCategories.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // Indicateur de validation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFormValid) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isFormValid) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.tertiary
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFormValid) "✓" else "!",
                                        color = if (isFormValid) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onTertiary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isFormValid) "Formulaire validé" else "Champs requis manquants",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isFormValid) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = if (isFormValid) "Tous les champs obligatoires sont correctement renseignés."
                                        else "Veuillez compléter tous les champs obligatoires (*).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isFormValid) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        // Bouton de création
                        PrimaryButton(
                            text = if (isCreating) "Création en cours..." else "Créer le service",
                            onClick = { createService() },
                            enabled = isFormValid && !isCreating,
                            modifier = Modifier.fillMaxWidth(),
                            icon = if (!isCreating) {
                                {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        )

                        // Footer info
                        Text(
                            text = stringResource(Strings.CreateService.PriceAdjustmentInfo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Espace final pour permettre un scroll fluide
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}}
