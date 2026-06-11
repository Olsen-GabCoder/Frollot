package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.ButtonSize
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran de modification du numéro de téléphone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneScreen(
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit,
    onPhoneChanged: (String?) -> Unit
) {
    var newPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Chaînes localisées (capturées avant les fonctions locales)
    val strPasswordRequired = stringResource(Strings.ChangePhone.PasswordRequired)
    val strPhoneError = stringResource(Strings.ChangePhone.Error)
    
    fun changePhone() {
        if (password.isBlank()) {
            errorMessage = strPasswordRequired
            return
        }
        
        scope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                val request = ChangePhoneRequest(
                    newPhone = newPhone.trim().ifBlank { null },
                    password = password
                )
                val response = api.changePhone(request)
                
                if (response.success) {
                    successMessage = response.message
                    onPhoneChanged(response.newPhone)
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                FrollotLogger.error("ChangePhone", "Erreur: ${e.message}")
                errorMessage = e.message ?: strPhoneError
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.ChangePhone.Title),
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Message de succès
            successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Message d'erreur
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Téléphone actuel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(Strings.ChangePhone.CurrentPhone),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentUser?.phoneNumber ?: stringResource(Strings.ChangePhone.NotDefined),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Formulaire
            SettingsSection(title = stringResource(Strings.ChangePhone.NewPhoneSection)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nouveau téléphone
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text(stringResource(Strings.ChangePhone.NewPhone)) },
                        placeholder = { Text(stringResource(Strings.ChangePhone.PhonePlaceholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, contentDescription = null)
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        supportingText = {
                            Text(stringResource(Strings.ChangePhone.LeaveBlankToRemove))
                        }
                    )
                    
                    // Mot de passe pour confirmation
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(Strings.ChangePhone.ConfirmWithPassword)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPassword) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Outlined.VisibilityOff 
                                    else Icons.Outlined.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        }
                    )
                    
                    PrimaryButton(
                        text = if (isLoading) 
                            stringResource(Strings.ChangePhone.Saving) 
                        else 
                            stringResource(Strings.ChangePhone.Save),
                        onClick = { changePhone() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && password.isNotBlank(),
                        size = ButtonSize.Large,
                        icon = if (isLoading) {
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
        }
    }
}

