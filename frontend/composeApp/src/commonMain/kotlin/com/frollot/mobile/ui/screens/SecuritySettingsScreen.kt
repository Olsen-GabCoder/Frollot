package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.time.currentTimeMillis
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.buttons.ButtonSize
import com.frollot.mobile.ui.components.dialogs.StandardDialog
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Écran des paramètres de sécurité - Design élégant et moderne.
 * 
 * Fonctionnalités :
 * - Changement de mot de passe avec indicateur de force
 * - Gestion des sessions actives en temps réel
 * - Déconnexion de tous les appareils
 * - Rafraîchissement automatique des sessions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // État du changement de mot de passe
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var isPasswordSectionExpanded by remember { mutableStateOf(false) }
    
    // État des sessions
    var sessions by remember { mutableStateOf<List<SessionInfo>>(emptyList()) }
    var isLoadingSessions by remember { mutableStateOf(true) }
    var showRevokeAllDialog by remember { mutableStateOf(false) }
    var sessionToRevoke by remember { mutableStateOf<SessionInfo?>(null) }
    var lastRefreshTime by remember { mutableStateOf<Long>(currentTimeMillis()) }
    
    val scope = rememberCoroutineScope()
    
    // Capturer les chaînes localisées au niveau composable
    val strAllFieldsRequired = stringResource(Strings.Security.AllFieldsRequired)
    val strPasswordsDoNotMatch = stringResource(Strings.Security.PasswordsDoNotMatch)
    val strPasswordTooShort = stringResource(Strings.Security.PasswordTooShort)
    val strChangePasswordError = stringResource(Strings.Security.ChangePasswordError)
    val strSessionRevoked = stringResource(Strings.Security.SessionRevoked)
    val strRevokeSessionError = stringResource(Strings.Security.RevokeSessionError)
    val strAllSessionsRevoked = stringResource(Strings.Security.AllSessionsRevoked)
    val strRevokeAllSessionsError = stringResource(Strings.Security.RevokeAllSessionsError)
    
    // Fonction pour charger les sessions
    suspend fun loadSessions() {
        try {
            val response = api.getActiveSessions()
            sessions = response.sessions
            lastRefreshTime = currentTimeMillis()
        } catch (e: Exception) {
            FrollotLogger.error("SecuritySettings", "Erreur chargement sessions: ${e.message}")
        } finally {
            isLoadingSessions = false
        }
    }
    
    // Charger les sessions au démarrage
    LaunchedEffect(Unit) {
        loadSessions()
    }
    
    // Rafraîchissement automatique toutes les 30 secondes
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000) // 30 secondes
            loadSessions()
        }
    }
    
    // Fonction pour changer le mot de passe
    fun changePassword() {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            errorMessage = strAllFieldsRequired
            return
        }
        
        if (newPassword != confirmPassword) {
            errorMessage = strPasswordsDoNotMatch
            return
        }
        
        if (newPassword.length < 8) {
            errorMessage = strPasswordTooShort
            return
        }
        
        scope.launch {
            isChangingPassword = true
            errorMessage = null
            
            try {
                val request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
                val response = api.changePassword(request)
                
                if (response.success) {
                    successMessage = response.message
                    // Réinitialiser les champs
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    isPasswordSectionExpanded = false
                    // Déconnecter l'utilisateur après changement de mot de passe
                    delay(1500)
                    onLogout()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: strChangePasswordError
            } finally {
                isChangingPassword = false
            }
        }
    }
    
    // Fonction pour révoquer une session
    fun revokeSession(session: SessionInfo) {
        scope.launch {
            isLoading = true
            try {
                val response = api.revokeSession(session.id)
                if (response.success) {
                    sessions = sessions.filter { it.id != session.id }
                    successMessage = strSessionRevoked
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: strRevokeSessionError
            } finally {
                isLoading = false
                sessionToRevoke = null
            }
        }
    }
    
    // Fonction pour révoquer toutes les sessions
    fun revokeAllSessions() {
        scope.launch {
            isLoading = true
            try {
                val response = api.revokeAllOtherSessions()
                if (response.success) {
                    // Recharger les sessions
                    loadSessions()
                    successMessage = strAllSessionsRevoked
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: strRevokeAllSessionsError
            } finally {
                isLoading = false
                showRevokeAllDialog = false
            }
        }
    }
    
    // Dismiss messages automatiquement
    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            delay(4000)
            successMessage = null
            errorMessage = null
        }
    }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.Security.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Messages de notification
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                NotificationBanner(
                    message = successMessage ?: "",
                    type = NotificationType.SUCCESS
                )
            }
            
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                NotificationBanner(
                    message = errorMessage ?: "",
                    type = NotificationType.ERROR
                )
            }
            
            // En-tête de sécurité avec statut
            SecurityStatusCard(
                sessionsCount = sessions.size,
                lastPasswordChange = currentUser?.createdAt ?: "N/A"
            )
            
            // Section : Changement de mot de passe
            ExpandableSecuritySection(
                title = stringResource(Strings.Security.ChangePassword),
                subtitle = stringResource(Strings.Security.PasswordRequirements),
                icon = Icons.Outlined.Lock,
                isExpanded = isPasswordSectionExpanded,
                onToggle = { isPasswordSectionExpanded = !isPasswordSectionExpanded }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mot de passe actuel
                    ElegantPasswordField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = stringResource(Strings.Security.CurrentPassword),
                        showPassword = showCurrentPassword,
                        onToggleVisibility = { showCurrentPassword = !showCurrentPassword },
                        icon = Icons.Outlined.Lock
                    )
                    
                    // Nouveau mot de passe avec indicateur de force
                    ElegantPasswordField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = stringResource(Strings.Security.NewPassword),
                        showPassword = showNewPassword,
                        onToggleVisibility = { showNewPassword = !showNewPassword },
                        icon = Icons.Outlined.LockReset,
                        showStrengthIndicator = true
                    )
                    
                    // Confirmer le mot de passe
                    ElegantPasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = stringResource(Strings.Security.ConfirmPassword),
                        showPassword = showConfirmPassword,
                        onToggleVisibility = { showConfirmPassword = !showConfirmPassword },
                        icon = Icons.Outlined.LockReset,
                        isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                        errorMessage = if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) 
                            stringResource(Strings.Security.PasswordsDoNotMatch) else null
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    PrimaryButton(
                        text = if (isChangingPassword) 
                            stringResource(Strings.Security.Changing) 
                        else 
                            stringResource(Strings.Security.ChangePasswordButton),
                        onClick = { changePassword() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChangingPassword && 
                            currentPassword.isNotBlank() && 
                            newPassword.isNotBlank() && 
                            confirmPassword.isNotBlank() &&
                            newPassword == confirmPassword,
                        size = ButtonSize.Large,
                        icon = if (isChangingPassword) {
                            {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            }
                        } else null
                    )
                }
            }
            
            // Section : Sessions actives
            ElegantSessionsSection(
                sessions = sessions,
                isLoading = isLoadingSessions,
                lastRefreshTime = lastRefreshTime,
                onRefresh = { 
                    scope.launch { 
                        isLoadingSessions = true
                        loadSessions() 
                    }
                },
                onRevokeSession = { sessionToRevoke = it },
                onRevokeAll = { showRevokeAllDialog = true }
            )
            
            // Conseils de sécurité
            SecurityTipsCard()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Dialog de confirmation de révocation d'une session
    sessionToRevoke?.let { session ->
        StandardDialog(
            title = stringResource(Strings.Security.RevokeSessionTitle),
            text = stringResource(Strings.Security.RevokeSessionMessage),
            onDismissRequest = { sessionToRevoke = null },
            confirmButton = {
                Button(
                    onClick = { revokeSession(session) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(Strings.Security.Revoke))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRevoke = null }) {
                    Text(stringResource(Strings.Common.Cancel))
                }
            }
        )
    }
    
    // Dialog de confirmation de révocation de toutes les sessions
    if (showRevokeAllDialog) {
        StandardDialog(
            title = stringResource(Strings.Security.RevokeAllSessionsTitle),
            text = stringResource(Strings.Security.RevokeAllSessionsMessage),
            onDismissRequest = { showRevokeAllDialog = false },
            confirmButton = {
                Button(
                    onClick = { revokeAllSessions() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(Strings.Security.RevokeAll))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeAllDialog = false }) {
                    Text(stringResource(Strings.Common.Cancel))
                }
            }
        )
    }
}

// ========== COMPOSANTS ÉLÉGANTS ==========

/**
 * Type de notification.
 */
private enum class NotificationType {
    SUCCESS, ERROR, INFO
}

/**
 * Bannière de notification élégante.
 */
@Composable
private fun NotificationBanner(
    message: String,
    type: NotificationType
) {
    val (backgroundColor, iconColor, icon) = when (type) {
        NotificationType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            Icons.Filled.CheckCircle
        )
        NotificationType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            Icons.Filled.Error
        )
        NotificationType.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary,
            Icons.Filled.Info
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Carte de statut de sécurité.
 */
@Composable
private fun SecurityStatusCard(
    sessionsCount: Int,
    lastPasswordChange: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(Strings.Security.SecurityStatus),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.Security.AccountProtected),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecurityStatItem(
                    icon = Icons.Outlined.Devices,
                    value = sessionsCount.toString(),
                    label = stringResource(Strings.Security.ActiveDevices)
                )
                
                SecurityStatItem(
                    icon = Icons.Outlined.Shield,
                    value = "✓",
                    label = stringResource(Strings.Security.PasswordSet)
                )
            }
        }
    }
}

/**
 * Item de statistique de sécurité.
 */
@Composable
private fun SecurityStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Section de sécurité expansible.
 */
@Composable
private fun ExpandableSecuritySection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // En-tête cliquable
            Surface(
                onClick = onToggle,
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Contenu expansible
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    content()
                }
            }
        }
    }
}

/**
 * Champ de mot de passe élégant avec indicateur de force optionnel.
 */
@Composable
private fun ElegantPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit,
    icon: ImageVector,
    showStrengthIndicator: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) 
                VisualTransformation.None 
            else 
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff 
                        else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingIcon = {
                Icon(
                    icon, 
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = isError,
            supportingText = if (errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        
        // Indicateur de force du mot de passe
        if (showStrengthIndicator && value.isNotEmpty()) {
            PasswordStrengthIndicator(password = value)
        }
    }
}

/**
 * Indicateur de force du mot de passe.
 */
@Composable
private fun PasswordStrengthIndicator(password: String) {
    val strength = calculatePasswordStrength(password)
    
    val (color, label) = when {
        strength < 0.25f -> Pair(Color(0xFFE53935), stringResource(Strings.Security.PasswordWeak))
        strength < 0.5f -> Pair(Color(0xFFFF9800), stringResource(Strings.Security.PasswordFair))
        strength < 0.75f -> Pair(Color(0xFFFFC107), stringResource(Strings.Security.PasswordGood))
        else -> Pair(Color(0xFF4CAF50), stringResource(Strings.Security.PasswordStrong))
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Strings.Security.PasswordStrength),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Calcule la force d'un mot de passe (0.0 à 1.0).
 */
private fun calculatePasswordStrength(password: String): Float {
    if (password.isEmpty()) return 0f
    
    var score = 0f
    
    // Longueur
    score += (password.length.coerceAtMost(12) / 12f) * 0.3f
    
    // Contient des minuscules
    if (password.any { it.isLowerCase() }) score += 0.15f
    
    // Contient des majuscules
    if (password.any { it.isUpperCase() }) score += 0.2f
    
    // Contient des chiffres
    if (password.any { it.isDigit() }) score += 0.15f
    
    // Contient des caractères spéciaux
    if (password.any { !it.isLetterOrDigit() }) score += 0.2f
    
    return score.coerceIn(0f, 1f)
}

/**
 * Section des sessions actives élégante.
 */
@Composable
private fun ElegantSessionsSection(
    sessions: List<SessionInfo>,
    isLoading: Boolean,
    lastRefreshTime: Long,
    onRefresh: () -> Unit,
    onRevokeSession: (SessionInfo) -> Unit,
    onRevokeAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // En-tête
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = stringResource(Strings.Security.ActiveSessions),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Indicateur de rafraîchissement en temps réel
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Point pulsant pour indiquer l'activité en temps réel
                            PulsingDot(color = Color(0xFF4CAF50))
                            Text(
                                text = stringResource(Strings.Security.RealTimeUpdates),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Bouton de rafraîchissement
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            // Liste des sessions
            if (isLoading && sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DevicesOther,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(Strings.Security.NoActiveSessions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column {
                    sessions.forEachIndexed { index, session ->
                        ElegantSessionItem(
                            session = session,
                            onRevoke = { onRevokeSession(session) }
                        )
                        
                        if (index < sessions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                    
                    // Bouton pour révoquer toutes les sessions
                    val otherSessions = sessions.filter { !it.isCurrent }
                    if (otherSessions.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRevokeAll,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        )
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Strings.Security.RevokeAllSessions),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Point pulsant pour indiquer l'activité en temps réel.
 */
@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val dotSize = 8f * scale
    Box(
        modifier = Modifier
            .size(dotSize.dp)
            .background(
                color = color.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}

/**
 * Item de session élégant.
 */
@Composable
private fun ElegantSessionItem(
    session: SessionInfo,
    onRevoke: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône de l'appareil
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (session.isCurrent) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(14.dp)
                )
                .then(
                    if (session.isCurrent) 
                        Modifier.border(
                            2.dp, 
                            MaterialTheme.colorScheme.primary, 
                            RoundedCornerShape(14.dp)
                        )
                    else 
                        Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when (session.deviceType) {
                    "mobile" -> Icons.Filled.PhoneAndroid
                    "tablet" -> Icons.Filled.Tablet
                    "desktop" -> Icons.Filled.Computer
                    else -> Icons.Outlined.Devices
                },
                contentDescription = null,
                tint = if (session.isCurrent) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Infos de la session
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (session.isCurrent) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.Security.CurrentBadge),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Text(
                text = session.shortDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                session.ipAddress?.let { ip ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Language,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = ip,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                session.lastUsedAt?.let { lastUsed ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatSessionTime(lastUsed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Bouton de révocation (pas pour la session courante)
        if (!session.isCurrent) {
            IconButton(
                onClick = onRevoke,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(Strings.Security.Revoke),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Formate l'heure de la session pour l'affichage.
 */
private fun formatSessionTime(dateTime: String): String {
    return try {
        // Format ISO: 2024-01-15T10:30:00
        val parts = dateTime.split("T")
        if (parts.size >= 2) {
            val datePart = parts[0] // 2024-01-15
            val timePart = parts[1].take(5) // 10:30
            "$datePart $timePart"
        } else {
            dateTime.take(16)
        }
    } catch (e: Exception) {
        dateTime.take(16)
    }
}

/**
 * Carte de conseils de sécurité.
 */
@Composable
private fun SecurityTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(Strings.Security.SecurityTipsTitle),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SecurityTipItem(text = stringResource(Strings.Security.Tip1))
                SecurityTipItem(text = stringResource(Strings.Security.Tip2))
                SecurityTipItem(text = stringResource(Strings.Security.Tip3))
            }
        }
    }
}

/**
 * Item de conseil de sécurité.
 */
@Composable
private fun SecurityTipItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
