package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.model.User
import com.frollot.mobile.model.LoginRequest
import com.frollot.mobile.model.UserType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.theme.*
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger

/**
 * Resultat de la tentative de connexion avec gestion des differents statuts.
 */
sealed class LoginResult {
    abstract val user: User?
    abstract val message: String

    data class Success(override val user: User, override val message: String = "Connexion reussie") : LoginResult()
    data class EmailNotVerified(override val user: User?, override val message: String = "Email non verifie", val email: String) : LoginResult()
    data class Error(override val message: String, override val user: User? = null) : LoginResult()
}

/**
 * Gere la logique de connexion avec appel API reel.
 */
private suspend fun handleLogin(
    api: FrollotApi,
    email: String,
    password: String,
    onLoadingChange: (Boolean) -> Unit,
    onResult: (LoginResult) -> Unit
) {
    fun getInvalidCredentialsMessage() = "Email ou mot de passe incorrect"
    fun getAccountDisabledMessage() = "Compte desactive"
    fun getAccountNotFoundMessage() = "Compte introuvable"
    fun getServerUnavailableMessage() = "Serveur indisponible"
    fun getTimeoutMessage() = "Delai d'attente depasse"
    fun getGenericErrorMessage() = "Erreur de connexion"
    onLoadingChange(true)

    try {
        val request = LoginRequest(email = email, password = password)
        val response = api.login(request)

        FrollotLogger.debug("Login", "RAW RESPONSE - accessToken: ${response.accessToken.take(20)}...")
        FrollotLogger.debug("Login", "RAW RESPONSE - userId: ${response.userId}")
        FrollotLogger.debug("Login", "RAW RESPONSE - email: ${response.email}")
        FrollotLogger.debug("Login", "RAW RESPONSE - userType: ${response.userType}")
        FrollotLogger.debug("Login", "RAW RESPONSE - isVerified: ${response.isVerified}")
        FrollotLogger.debug("Login", "RAW RESPONSE - isActive: ${response.isActive}")
        FrollotLogger.debug("Login", "RAW RESPONSE - message: ${response.message}")

        if (response.accessToken.isNotBlank() && response.userId.isNotBlank()) {
            val user = response.toUser()
            FrollotLogger.success("API", "Connexion reussie : ${user.firstName} ${user.lastName}")
            FrollotLogger.debug("Login", "Token JWT recu et stocke automatiquement")

            FrollotLogger.debug("Login", "response.isVerified: ${response.isVerified}")
            FrollotLogger.debug("Login", "user.isVerified: ${user.isVerified}")

            val correctedUser = if (response.isVerified && !user.isVerified) {
                FrollotLogger.warning("Login", "CORRECTION: response.isVerified=${response.isVerified} mais user.isVerified=${user.isVerified}")
                user.copy(isVerified = true)
            } else {
                user
            }

            val result = if (!correctedUser.isVerified) {
                FrollotLogger.warning("Login", "Tentative de connexion avec email non verifie: ${correctedUser.email}")
                LoginResult.EmailNotVerified(correctedUser, "Votre email n'est pas verifie. Verifiez votre boite mail.", correctedUser.email)
            } else {
                FrollotLogger.success("Login", "Email verifie - REDIRECTION VERS HOME")
                LoginResult.Success(correctedUser, response.message ?: "Connexion reussie")
            }

            onResult(result)
        } else {
            val errorMessage = response.message ?: "Erreur de connexion inattendue"
            FrollotLogger.error("API", "Echec de connexion (reponse serveur): $errorMessage")
            onResult(LoginResult.Error(errorMessage))
        }
    } catch (e: Exception) {
        val errorCode = when {
            e.message?.contains("401") == true ||
                    e.message?.contains("Unauthorized") == true -> "INVALID_CREDENTIALS"
            e.message?.contains("403") == true ||
                    e.message?.contains("Forbidden") == true -> {
                if (e.message?.contains("email") == true &&
                    (e.message?.contains("verifie") == true || e.message?.contains("verified") == true)) {
                    val mockUser = User(
                        id = "temp",
                        email = email,
                        userType = UserType.client,
                        firstName = "Utilisateur",
                        lastName = "Temp",
                        phoneNumber = null,
                        isVerified = false,
                        isActive = true
                    )
                    onResult(LoginResult.EmailNotVerified(mockUser, "Votre adresse email n'a pas ete verifiee.", email))
                    return
                } else {
                    "ACCOUNT_DISABLED"
                }
            }
            e.message?.contains("404") == true ||
                    e.message?.contains("Not Found") == true -> "ACCOUNT_NOT_FOUND"
            e.message?.contains("Connection refused") == true ||
                    e.message?.contains("Failed to connect") == true -> "SERVER_UNAVAILABLE"
            e.message?.contains("timeout") == true -> "TIMEOUT"
            else -> "GENERIC_ERROR"
        }

        FrollotLogger.error("API", "Erreur de connexion : ${e.message}")
        e.printStackTrace()

        val errorMessage = when (errorCode) {
            "INVALID_CREDENTIALS" -> getInvalidCredentialsMessage()
            "ACCOUNT_DISABLED" -> getAccountDisabledMessage()
            "ACCOUNT_NOT_FOUND" -> getAccountNotFoundMessage()
            "SERVER_UNAVAILABLE" -> getServerUnavailableMessage()
            "TIMEOUT" -> getTimeoutMessage()
            else -> getGenericErrorMessage()
        }

        onResult(LoginResult.Error(errorMessage))
    } finally {
        onLoadingChange(false)
    }
}

// ========================================
// ECRAN LOGIN — Design System v2
// Editorial premium chaleureux
// ========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    api: FrollotApi,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToEmailVerification: (String) -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onLoginSuccess: (User) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginResult by remember { mutableStateOf<LoginResult?>(null) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val isFormValid = email.isNotBlank() && password.isNotBlank()
    val isEmailValid = email.contains("@") && email.contains(".")

    // Callback de login reutilisable
    val doLogin: () -> Unit = {
        if (isFormValid && !isLoading) {
            scope.launch {
                handleLogin(
                    api = api,
                    email = email,
                    password = password,
                    onLoadingChange = { isLoading = it },
                    onResult = { result ->
                        loginResult = result
                        when (result) {
                            is LoginResult.Success -> {
                                scope.launch {
                                    delay(2000)
                                    onLoginSuccess(result.user!!)
                                }
                            }
                            is LoginResult.EmailNotVerified -> {
                                scope.launch {
                                    delay(3000)
                                    onNavigateToEmailVerification(result.email)
                                }
                            }
                            is LoginResult.Error -> {}
                        }
                    }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ========================================
        // HERO EDITORIAL — Photo salon plein cadre
        // ========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            // Placeholder image (sera remplace par une vraie photo)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FrollotPrimaryContainer,
                                FrollotSurfaceVariant,
                                FrollotPrimaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Gradient overlay pour lisibilite
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFF281733).copy(alpha = 0.42f),
                                0.45f to Color(0xFF281733).copy(alpha = 0.08f),
                                0.7f to MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                1f to MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            // Logo marque en verre depoli
            Row(
                modifier = Modifier
                    .padding(top = 28.dp, start = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icone F en glass (glassmorphism fidele maquette)
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "F",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "Frollot",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // ========================================
        // FORMULAIRE — sous le hero
        // ========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 24.dp)
        ) {
            // Overline — t-overline: sans 11px weight 700 letter-spacing 2px uppercase
            Text(
                text = "BON RETOUR PARMI NOUS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Titre "Bienvenue" — font-display 44px weight 600 lineHeight .98
            Text(
                text = "Bienvenue",
                fontSize = 44.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 43.sp,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = "Connectez-vous pour retrouver vos salons, rendez-vous et inspirations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ========================================
            // CHAMPS DE SAISIE — Style M3
            // ========================================

            // Email
            LoginField(
                value = email,
                onValueChange = {
                    email = it
                    if (loginResult != null) loginResult = null
                },
                label = stringResource(Strings.Login.EmailLabel),
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !isLoading,
                isError = loginResult is LoginResult.Error
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mot de passe
            LoginField(
                value = password,
                onValueChange = {
                    password = it
                    if (loginResult != null) loginResult = null
                },
                label = stringResource(Strings.Login.PasswordLabel),
                leadingIcon = Icons.Outlined.Lock,
                trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingClick = { passwordVisible = !passwordVisible },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        doLogin()
                    }
                ),
                enabled = !isLoading,
                isError = loginResult is LoginResult.Error,
                focused = true
            )

            // "Mot de passe oublie ?" — alignSelf flex-end, marginTop 12
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Strings.Login.ForgotPassword),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.5.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(enabled = !isLoading) { onNavigateToForgotPassword() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )

            // Message de resultat
            AnimatedVisibility(
                visible = loginResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                loginResult?.let { result ->
                    LoginResultCard(result)
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Maquette: marginTop 14 avant bouton
            Spacer(modifier = Modifier.height(14.dp))

            // Bouton "Se connecter" — Btn primary full icon=login, elev-2
            Button(
                onClick = doLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 1.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Strings.Login.SubmitButton),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ========================================
            // SEPARATEUR "OU"
            // ========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "OU",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ========================================
            // BOUTON CREER UN COMPTE — Outline
            // ========================================
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(999.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading),
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(Strings.Login.CreateAccount),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            // Footer legal — body-sm, liens en primary weight 600
            val legalColor = MaterialTheme.colorScheme.onSurfaceVariant
            val linkColor = MaterialTheme.colorScheme.primary
            Text(
                text = buildAnnotatedString {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = legalColor)) {
                        append("En continuant, vous acceptez nos ")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
                        append("Conditions")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = legalColor)) {
                        append(" et notre ")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
                        append("Politique de confidentialite")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = legalColor)) {
                        append(".")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ========================================
// COMPOSANT CHAMP DE SAISIE — Style maquette
// ========================================

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    focused: Boolean = false
) {
    Column {
        // Label au-dessus
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (focused) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.3.sp,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Champ
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = if (trailingIcon != null) {
                {
                    IconButton(onClick = { onTrailingClick?.invoke() }) {
                        Icon(
                            trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            enabled = enabled,
            isError = isError
        )
    }
}

// ========================================
// CARTE RESULTAT CONNEXION
// ========================================

@Composable
private fun LoginResultCard(result: LoginResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = when (result) {
            is LoginResult.Success -> FrollotSuccessContainer
            is LoginResult.EmailNotVerified -> MaterialTheme.colorScheme.secondaryContainer
            is LoginResult.Error -> FrollotErrorContainer
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (result) {
                        is LoginResult.Success -> Icons.Default.CheckCircle
                        is LoginResult.EmailNotVerified -> Icons.Default.Email
                        is LoginResult.Error -> Icons.Default.ErrorOutline
                    },
                    contentDescription = null,
                    tint = when (result) {
                        is LoginResult.Success -> FrollotSuccess
                        is LoginResult.EmailNotVerified -> MaterialTheme.colorScheme.secondary
                        is LoginResult.Error -> FrollotError
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (result) {
                        is LoginResult.Success -> "Connexion reussie"
                        is LoginResult.EmailNotVerified -> "Email non verifie"
                        is LoginResult.Error -> "Erreur de connexion"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = result.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
