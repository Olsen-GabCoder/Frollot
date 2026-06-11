package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.model.RegisterRequest
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.ui.components.forms.PasswordTextField
import com.frollot.mobile.ui.components.FrollotLogoCircle
import com.frollot.mobile.ui.components.FrollotPurple
import com.frollot.mobile.ui.components.FrollotPurpleDark
import com.frollot.mobile.ui.components.FrollotPurpleLight
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Convertit un UserType en label lisible pour l'interface utilisateur.
 */
@Composable
private fun getUserTypeLabel(userType: UserType): String {
    return when (userType) {
        UserType.client -> stringResource(Strings.Register.UserTypes.Client)
        UserType.hairstylist -> stringResource(Strings.Register.UserTypes.Hairstylist)
        UserType.salon_owner -> stringResource(Strings.Register.UserTypes.SalonOwner)
        UserType.admin -> stringResource(Strings.Register.UserTypes.Admin)
    }
}


/**
 * Gère la logique de pré-inscription avec le nouveau système sécurisé.
 */
private suspend fun handlePreRegister(
    api: FrollotApi,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    selectedType: UserType,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (String, String) -> Unit // (message, email)
) {
    onLoadingChange(true)
    onError("")

    try {
        val request = RegisterRequest(
            email = email.trim(),
            password = password,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            userType = selectedType
        )

        val response = api.preRegister(request)

        // Pré-inscription réussie - préparer le message de succès
        val successMessage = response.message ?: "📧 Un email de vérification a été envoyé à ${email}. Cliquez sur le lien pour activer votre compte."

        FrollotLogger.success("API", "✅ Pré-inscription réussie: $successMessage")
        FrollotLogger.info("Register", "🎯 Pré-inscription créée - ID: ${response.userId}, Email: ${email}")

        onSuccess(successMessage, email)

    } catch (e: Exception) {
        val errorCode = when {
            e.message?.contains("409") == true ||
                    e.message?.contains("Conflict") == true ->
                "EMAIL_ALREADY_USED"

            e.message?.contains("400") == true ||
                    e.message?.contains("Bad Request") == true ->
                "INVALID_DATA"

            e.message?.contains("Connection refused") == true ||
                    e.message?.contains("Failed to connect") == true ->
                "SERVER_UNAVAILABLE"

            e.message?.contains("timeout") == true ->
                "TIMEOUT"

            else -> "GENERIC_ERROR"
        }

        FrollotLogger.error("API", "❌ Erreur de pré-inscription : ${e.message}")
        e.printStackTrace()
        onError(errorCode)

    } finally {
        onLoadingChange(false)
    }
}

/**
 * Écran d'inscription Ultra-Premium
 * Design cohérent avec LoginScreen - header gradient + bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    api: FrollotApi,
    onRegisterSuccess: (User) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToEmailVerification: (email: String, token: String?) -> Unit = { _, _ -> }
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(UserType.client) }
    var userTypeExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val isFormValid = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            password == confirmPassword

    val isEmailValid = email.contains("@") && email.contains(".")
    val passwordMismatch = confirmPassword.isNotBlank() && password != confirmPassword

    // ========================================
    // 🎨 ANIMATIONS ULTRA-PREMIUM
    // ========================================
    val infiniteTransition = rememberInfiniteTransition(label = "premium_bg")
    
    val floatingOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating1"
    )
    
    val floatingOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating2"
    )
    
    var headerVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(350)
        formVisible = true
    }
    
    // Gradient principal Frollot
    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            FrollotPurple,
            FrollotPurpleDark,
            Color(0xFF5B4BC4)
        )
    )
    
    val sheetColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ========================================
        // 🎨 HEADER GRADIENT VIOLET
        // ========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .background(headerGradient)
        ) {
            // Particules flottantes
            val particle1Y = 20f + floatingOffset1
            Box(
                modifier = Modifier
                    .offset(x = (-60).dp, y = particle1Y.dp)
                    .size(180.dp)
                    .alpha(0.12f)
                    // // .blur(45.dp) // API expérimentale non disponible // API expérimentale non disponible
                    .background(Color.White, CircleShape)
            )
            
            val particle2Y = 60f + floatingOffset2
            Box(
                modifier = Modifier
                    .offset(x = 280.dp, y = particle2Y.dp)
                    .size(120.dp)
                    .alpha(0.08f)
                    // // .blur(35.dp) // API expérimentale non disponible // API expérimentale non disponible
                    .background(Color.White, CircleShape)
            )
            
            // Contenu du header
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(700)) + slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Logo Frollot
                    FrollotLogoCircle(
                        size = 80.dp,
                        backgroundColor = Color.White.copy(alpha = 0.18f),
                        showShadow = true,
                        animated = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Titre
                    Text(
                        text = stringResource(Strings.Register.WelcomeTitle),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ========================================
        // 🎨 BOTTOM SHEET (formulaire)
        // ========================================
        AnimatedVisibility(
            visible = formVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(tween(400)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.80f),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = sheetColor,
                shadowElevation = 32.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Handle
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Sous-titre
                    Text(
                        text = stringResource(Strings.Register.WelcomeSubtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))

                    // ========================================
                    // 📝 FORMULAIRE PREMIUM
                    // ========================================
                    
                    // Prénom et Nom sur la même ligne
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                if (errorMessage != null) errorMessage = null
                            },
                            label = { Text(stringResource(Strings.Register.FirstNameLabel)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = if (firstName.isNotBlank()) FrollotPurple 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FrollotPurple,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                focusedLabelColor = FrollotPurple,
                                cursorColor = FrollotPurple
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Right) }
                            ),
                            enabled = !isLoading
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                if (errorMessage != null) errorMessage = null
                            },
                            label = { Text(stringResource(Strings.Register.LastNameLabel)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FrollotPurple,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                focusedLabelColor = FrollotPurple,
                                cursorColor = FrollotPurple
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            enabled = !isLoading
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text(stringResource(Strings.Register.EmailLabel)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                tint = if (email.isNotBlank() && isEmailValid) FrollotPurple 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (email.isNotBlank()) {
                                Icon(
                                    imageVector = if (isEmailValid)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isEmailValid)
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FrollotPurple,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedLabelColor = FrollotPurple,
                            cursorColor = FrollotPurple
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Type de compte - Types disponibles à l'inscription (exclut admin)
                    val availableUserTypes = listOf(
                        UserType.client,
                        UserType.hairstylist,
                        UserType.salon_owner
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = userTypeExpanded,
                        onExpandedChange = { if (!isLoading) userTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = getUserTypeLabel(selectedType),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Strings.Register.AccountTypeLabel)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Badge,
                                    contentDescription = null,
                                    tint = FrollotPurple
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { if (!isLoading) userTypeExpanded = !userTypeExpanded }) {
                                    Icon(
                                        imageVector = if (userTypeExpanded) Icons.Filled.KeyboardArrowUp 
                                                     else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Sélectionner le type de compte",
                                        tint = FrollotPurple
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FrollotPurple,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                focusedLabelColor = FrollotPurple
                            ),
                            enabled = !isLoading
                        )

                        ExposedDropdownMenu(
                            expanded = userTypeExpanded,
                            onDismissRequest = { userTypeExpanded = false }
                        ) {
                            availableUserTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (type) {
                                                    UserType.client -> Icons.Outlined.Person
                                                    UserType.hairstylist -> Icons.Outlined.ContentCut
                                                    UserType.salon_owner -> Icons.Outlined.Store
                                                    UserType.admin -> Icons.Outlined.AdminPanelSettings
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (selectedType == type) FrollotPurple 
                                                       else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = getUserTypeLabel(type),
                                                fontWeight = if (selectedType == type) FontWeight.SemiBold 
                                                            else FontWeight.Normal,
                                                color = if (selectedType == type) FrollotPurple 
                                                        else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedType = type
                                        userTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Mot de passe
                    PasswordTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text(stringResource(Strings.Register.PasswordLabel)) },
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmation mot de passe
                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text(stringResource(Strings.Register.ConfirmPasswordLabel)) },
                        enabled = !isLoading,
                        isError = passwordMismatch,
                        errorText = if (passwordMismatch) {
                            { Text(stringResource(Strings.Register.PasswordMismatch)) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Messages d'erreur/succès
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let { errorCode ->
                            val errorMessageKey = when (errorCode) {
                                "EMAIL_ALREADY_USED" -> Strings.Register.Errors.EmailAlreadyUsed
                                "INVALID_DATA" -> Strings.Register.Errors.InvalidData
                                "SERVER_UNAVAILABLE" -> Strings.Register.Errors.ServerUnavailable
                                "TIMEOUT" -> Strings.Register.Errors.Timeout
                                else -> Strings.Register.Errors.GenericError
                            }
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = stringResource(errorMessageKey),
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
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Bouton inscription premium
                    Button(
                        onClick = {
                            scope.launch {
                                handlePreRegister(
                                    api = api,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    password = password,
                                    selectedType = selectedType,
                                    onLoadingChange = { isLoading = it },
                                    onError = { errorMessage = it },
                                    onSuccess = { message, userEmail ->
                                        successMessage = message
                                        errorMessage = null // S'assurer qu'il n'y a pas de message d'erreur en cas de succès

                                        // Navigation automatique vers la page de vérification OTP
                                        FrollotLogger.info("Register", "Pré-inscription réussie - navigation automatique vers EmailVerificationScreen")
                                        onNavigateToEmailVerification(userEmail, null)
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(
                                elevation = if (isFormValid && !isLoading) 12.dp else 0.dp,
                                shape = RoundedCornerShape(29.dp),
                                spotColor = FrollotPurple.copy(alpha = 0.4f)
                            ),
                        enabled = isFormValid && !isLoading,
                        shape = RoundedCornerShape(29.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FrollotPurple,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = stringResource(Strings.Register.SubmitButton).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = stringResource(Strings.Register.SignUpWith),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Boutons sociaux
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: Google */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            enabled = !isLoading
                        ) {
                            Text(
                                text = stringResource(Strings.Register.GoogleButton),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { /* TODO: Facebook */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            enabled = !isLoading
                        ) {
                            Text(
                                text = stringResource(Strings.Register.FacebookButton),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))

                    // Lien connexion
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Strings.Register.AlreadyRegistered),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = onNavigateToLogin,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = stringResource(Strings.Register.LoginLink),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FrollotPurple
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Badge sécurité
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = FrollotPurple.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = FrollotPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Strings.Register.DataProtected),
                                style = MaterialTheme.typography.labelSmall,
                                color = FrollotPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
