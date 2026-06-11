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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.User
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.FrollotLogoCircle
import com.frollot.mobile.ui.components.FrollotPurple
import com.frollot.mobile.ui.components.FrollotPurpleDark
import com.frollot.mobile.ui.components.FrollotToast
import com.frollot.mobile.ui.components.ToastType
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger

/**
 * Écran de vérification d'email.
 * 
 * Permet à l'utilisateur de :
 * - Entrer le code de vérification reçu par email
 * - Renvoyer un email de vérification
 * - Voir le statut de vérification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    api: FrollotApi,
    email: String,
    token: String? = null,
    onVerificationSuccess: (User) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var verificationToken by remember { mutableStateOf(token ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isVerified by remember { mutableStateOf(false) }
    var showDevDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Messages localisés
    val localizedSuccessMessage = stringResource(Strings.EmailVerification.SuccessMessage)
    val localizedInvalidTokenMessage = stringResource(Strings.EmailVerification.Errors.InvalidToken)
    val localizedExpiredTokenMessage = stringResource(Strings.EmailVerification.Errors.ExpiredToken)
    val localizedGenericErrorMessage = stringResource(Strings.EmailVerification.Errors.GenericError)
    val localizedServerErrorMessage = stringResource(Strings.EmailVerification.Errors.ServerError)
    val localizedResendSuccessMessage = stringResource(Strings.EmailVerification.ResendSuccess)

    suspend fun completeRegistration(tokenToVerify: String) {
        isLoading = true
        errorMessage = null
        successMessage = null

        try {
            // Nouveau système : completeRegistration retourne AuthResponse avec tokens
            val response = api.completeRegistration(tokenToVerify)

            // L'API stocke automatiquement les tokens, on récupère juste l'utilisateur
            val user = response.toUser()
            isVerified = true
            successMessage = localizedSuccessMessage
            FrollotLogger.success("EmailVerification", "✅ Inscription finalisée avec succès - tokens stockés")

            delay(2000)
            onVerificationSuccess(user)

        } catch (e: Exception) {
            val errorCode = when {
                e.message?.contains("400") == true ||
                        e.message?.contains("Token de vérification invalide") == true ||
                        e.message?.contains("Token invalide") == true -> "INVALID_TOKEN"
                e.message?.contains("expiré") == true ||
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Token de vérification expiré") == true -> "EXPIRED_TOKEN"
                e.message?.contains("Connection refused") == true -> "SERVER_ERROR"
                else -> "GENERIC_ERROR"
            }
            errorMessage = when (errorCode) {
                "INVALID_TOKEN" -> localizedInvalidTokenMessage
                "EXPIRED_TOKEN" -> localizedExpiredTokenMessage
                "SERVER_ERROR" -> localizedServerErrorMessage
                else -> localizedGenericErrorMessage
            }
            FrollotLogger.error("EmailVerification", "❌ Erreur de finalisation d'inscription : ${e.message}")
        } finally {
            isLoading = false
        }
    }

    suspend fun resendEmail() {
        isResending = true
        errorMessage = null

        try {
            val response = api.resendVerificationEmail()
            if (response["success"] == "true" || response.containsKey("message")) {
                successMessage = localizedResendSuccessMessage
                FrollotLogger.success("EmailVerification", "✅ Email renvoyé avec succès")
            } else {
                errorMessage = response["error"] ?: localizedGenericErrorMessage
            }
        } catch (e: Exception) {
            errorMessage = localizedServerErrorMessage
            FrollotLogger.error("EmailVerification", "❌ Erreur lors du renvoi : ${e.message}")
        } finally {
            isResending = false
        }
    }

    // Si un token est fourni dans l'URL, vérifier automatiquement
    LaunchedEffect(token) {
        if (token != null && token.isNotBlank()) {
            scope.launch {
                completeRegistration(token)
            }
        }
    }

    // Animation d'apparition
    var headerVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(350)
        formVisible = true
    }

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            FrollotPurple,
            FrollotPurpleDark,
            Color(0xFF5B4BC4)
        )
    )


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .background(headerGradient)
        ) {
            // Particules flottantes
            Box(
                modifier = Modifier
                    .offset(x = (-60).dp, y = 20.dp)
                    .size(180.dp)
                    .alpha(0.12f)
                    // .blur(45.dp) // API expérimentale non disponible
                    .background(Color.White, CircleShape)
            )
            
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
                    FrollotLogoCircle(
                        size = 80.dp,
                        backgroundColor = Color.White.copy(alpha = 0.18f),
                        showShadow = true,
                        animated = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(Strings.EmailVerification.Title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom sheet
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
                color = MaterialTheme.colorScheme.surface,
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
                        text = stringResource(Strings.EmailVerification.Subtitle).replace("{email}", email),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FrollotPurple
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))

                    // Instructions
                    Text(
                        text = stringResource(Strings.EmailVerification.Instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Champ de code de vérification
                    OutlinedTextField(
                        value = verificationToken,
                        onValueChange = {
                            verificationToken = it
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text(stringResource(Strings.EmailVerification.TokenLabel)) },
                        placeholder = { Text(stringResource(Strings.EmailVerification.TokenPlaceholder)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = if (verificationToken.isNotBlank()) FrollotPurple 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (verificationToken.isNotBlank() && !isLoading) {
                                    scope.launch {
                                        completeRegistration(verificationToken)
                                    }
                                }
                            }
                        ),
                        enabled = !isLoading && !isVerified
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Messages d'erreur/succès
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let { error ->
                            FrollotToast(
                                message = error,
                                type = ToastType.Error,
                                onDismiss = { errorMessage = null }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = successMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        successMessage?.let { message ->
                            FrollotToast(
                                message = message,
                                type = ToastType.Success,
                                onDismiss = { successMessage = null }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Bouton vérifier
                    Button(
                        onClick = {
                            scope.launch {
                                completeRegistration(verificationToken)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(
                                elevation = if (verificationToken.isNotBlank() && !isLoading) 12.dp else 0.dp,
                                shape = RoundedCornerShape(29.dp),
                                spotColor = FrollotPurple.copy(alpha = 0.4f)
                            ),
                        enabled = verificationToken.isNotBlank() && !isLoading && !isVerified,
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
                                text = stringResource(Strings.EmailVerification.VerifyButton).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton renvoyer
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                resendEmail()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isResending && !isVerified,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isResending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(Strings.EmailVerification.ResendButton),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton Mode DEV (uniquement en développement)
                    val isDevMode = true // TODO: Détecter automatiquement le mode DEV via une configuration
                    if (isDevMode && !isVerified) {
                        OutlinedButton(
                            onClick = { showDevDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9800))
                        ) {
                            Text(
                                text = "🔧 Mode DEV - Saisir token",
                                color = Color(0xFFFF9800),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lien retour connexion
                    TextButton(
                        onClick = onNavigateToLogin,
                        enabled = !isLoading
                    ) {
                        Text(
                            text = stringResource(Strings.Login.CreateAccount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FrollotPurple
                        )
                    }
                }
            }
        }
    }

    // Dialog Mode DEV pour saisir le token manuellement
    if (showDevDialog) {
        AlertDialog(
            onDismissRequest = { showDevDialog = false },
            title = {
                Text(
                    "🔧 Mode Développement",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Saisissez le token de vérification depuis les logs du backend (mode DEV).",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = verificationToken,
                        onValueChange = { verificationToken = it },
                        label = { Text("Token de vérification") },
                        placeholder = { Text("a04ea2e1-cbe5-4dbc-ad4b-9a7d1a69d049") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDevDialog = false
                        if (verificationToken.isNotBlank()) {
                            scope.launch {
                                completeRegistration(verificationToken)
                            }
                        }
                    }
                ) {
                    Text("Vérifier", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

