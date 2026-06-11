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
import com.frollot.mobile.config.FrollotLogger


/**
 * Composant pour afficher une spécialité sélectionnable
 */
@Composable
fun SpecialtyItem(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                    fontSize = 16.sp
                )
            }

            Text(
                text = category.getLocalizedDisplayName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Checkbox personnalisé
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outlineVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
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
fun CreateStaffScreen(
    salonId: String,
    onBack: () -> Unit,
    onStaffCreated: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedSpecialties by remember { mutableStateOf<Set<ServiceCategory>>(emptySet()) }

    var isCreating by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }

    val scrollState = rememberScrollState()

    val isFormValid by remember(firstName, lastName, email) {
        derivedStateOf {
            firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && email.contains("@")
        }
    }

    fun createStaff() {
        scope.launch {
            try {
                if (firstName.isBlank()) {
                    errorMessage = "Le prénom est obligatoire"
                    return@launch
                }

                if (lastName.isBlank()) {
                    errorMessage = "Le nom est obligatoire"
                    return@launch
                }

                if (email.isBlank() || !email.contains("@")) {
                    errorMessage = "Un email valide est obligatoire"
                    return@launch
                }

                isCreating = true
                errorMessage = null

                var userId: String? = null
                try {
                    // Étape 1: Chercher si l'utilisateur existe déjà par email
                    val searchResults = api.searchUsers(email.trim())
                    val existingUser = searchResults.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }

                    if (existingUser != null) {
                        // L'utilisateur existe déjà
                        if (existingUser.userType != UserType.hairstylist) {
                            errorMessage = "Cet utilisateur existe mais n'est pas de type 'coiffeur'. Contactez l'administrateur."
                            isCreating = false
                            return@launch
                        }
                        userId = existingUser.id
                        FrollotLogger.debug("CreateStaff", "Utilisateur existant trouvé: ${existingUser.id}")
                    } else {
                        // Étape 2: Créer un nouveau compte hairstylist
                        val defaultPassword = "TempPass123!"
                        val registerRequest = RegisterRequest(
                            email = email.trim(),
                            password = defaultPassword,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            userType = UserType.hairstylist
                        )
                        val response = api.register(registerRequest)
                        userId = response.userId
                        FrollotLogger.debug("CreateStaff", "Nouvel utilisateur créé: $userId")
                    }
                } catch (e: Exception) {
                    // Gérer le cas où l'email existe déjà (erreur 409)
                    if (e.message?.contains("409") == true || 
                        e.message?.contains("Conflict") == true ||
                        e.message?.contains("email", ignoreCase = true) == true) {
                        // Réessayer de chercher l'utilisateur
                        try {
                            val searchResults = api.searchUsers(email.trim())
                            val existingUser = searchResults.firstOrNull { 
                                it.email.equals(email.trim(), ignoreCase = true) && 
                                it.userType == UserType.hairstylist 
                            }
                            if (existingUser != null) {
                                userId = existingUser.id
                                FrollotLogger.debug("CreateStaff", "Utilisateur existant trouvé après conflit: ${existingUser.id}")
                            } else {
                                errorMessage = "Un utilisateur avec cet email existe mais n'est pas coiffeur."
                                isCreating = false
                                return@launch
                            }
                        } catch (searchError: Exception) {
                            errorMessage = "Erreur lors de la recherche de l'utilisateur: ${searchError.message}"
                            isCreating = false
                            return@launch
                        }
                    } else {
                        errorMessage = "Erreur lors de la création du compte: ${e.message}"
                        isCreating = false
                        return@launch
                    }
                }

                if (userId == null) {
                    errorMessage = "Impossible d'obtenir l'ID utilisateur"
                    isCreating = false
                    return@launch
                }

                val request = CreateStaffRequest(
                    salonId = salonId,
                    userId = userId,
                    specialties = selectedSpecialties.toList(),
                    isActive = true
                )

                api.addStaffMember(request)
                successMessage = "✨ ${firstName} ${lastName} ajouté avec succès !"

                kotlinx.coroutines.delay(1500)
                onStaffCreated()
            } catch (e: Exception) {
                FrollotLogger.error("API", "❌ Erreur création staff: ${e.message}")
                errorMessage = when {
                    e.message?.contains("403") == true || 
                    e.message?.contains("Forbidden") == true ||
                    e.message?.contains("autorisé") == true ->
                        "Vous n'êtes pas autorisé à ajouter du staff à ce salon. Seul le propriétaire peut le faire."
                    e.message?.contains("401") == true ||
                    e.message?.contains("Unauthorized") == true ->
                        "Votre session a expiré. Veuillez vous reconnecter."
                    e.message?.contains("existe déjà") == true ||
                    e.message?.contains("fait déjà partie") == true ->
                        "Ce coiffeur fait déjà partie de l'équipe de ce salon."
                    e.message?.contains("hairstylist") == true ->
                        "Seuls les utilisateurs de type 'coiffeur' peuvent être ajoutés au staff."
                    else -> e.message ?: "Erreur lors de l'ajout au staff"
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
                title = stringResource(Strings.CreateStaff.Title),
                showAvatar = false
            )
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
                                    Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(Strings.CreateStaff.NewCollaborator),
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

                    // Section Informations personnelles
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.CreateStaff.PersonalInfo),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Prénom
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StandardTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text(stringResource(Strings.CreateStaff.FirstName)) },
                                placeholder = { Text(stringResource(Strings.CreateStaff.FirstNamePlaceholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isCreating,
                                trailingIcon = if (firstName.isNotBlank()) {
                                    {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }

                        // Nom
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StandardTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text(stringResource(Strings.CreateStaff.LastName)) },
                                placeholder = { Text(stringResource(Strings.CreateStaff.LastNamePlaceholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isCreating,
                                trailingIcon = if (lastName.isNotBlank()) {
                                    {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }

                        // Email
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StandardTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(stringResource(Strings.CreateStaff.Email)) },
                                placeholder = { Text("jean.dupont@example.com") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                                ),
                                singleLine = true,
                                enabled = !isCreating,
                                trailingIcon = if (email.isNotBlank() && email.contains("@")) {
                                    {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }

                        // Info box
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
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Un compte utilisateur sera créé automatiquement si l'email n'existe pas encore.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Section Spécialités
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.CreateStaff.Specialties),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            stringResource(Strings.CreateStaff.SpecialtiesHint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
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
                                        SpecialtyItem(
                                            category = category,
                                            isSelected = selectedSpecialties.contains(category),
                                            onClick = {
                                                selectedSpecialties = if (selectedSpecialties.contains(category)) {
                                                    selectedSpecialties - category
                                                } else {
                                                    selectedSpecialties + category
                                                }
                                            },
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
                        text = if (isCreating) "Ajout en cours..." else "Ajouter au staff",
                        onClick = { createStaff() },
                        enabled = isFormValid && !isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        icon = if (!isCreating) {
                            {
                                Icon(
                                    Icons.Default.PersonAdd,
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
                        text = stringResource(Strings.CreateStaff.EmailInfo),
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
}}
