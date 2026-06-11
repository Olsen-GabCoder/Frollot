package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frollot.mobile.localization.Strings
import com.frollot.mobile.localization.stringResource
import com.frollot.mobile.ui.components.forms.PasswordTextField
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.model.ResetPasswordRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    api: com.frollot.mobile.network.FrollotApi,
    token: String?,
    onPasswordResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Définir tous les messages localisés EN DEHORS des coroutines
    val tokenMissingMsg = stringResource(Strings.ResetPassword.TokenMissingError)
    val passwordsDoNotMatchMsg = stringResource(Strings.ResetPassword.PasswordsDoNotMatchError)
    val passwordFieldsRequiredMsg = stringResource(Strings.ResetPassword.PasswordFieldsRequiredError)
    val genericErrorMsg = "An error occurred while resetting your password. Please try again."

    // Vérifier le token au chargement
    LaunchedEffect(token) {
        if (token == null) {
            errorMessage = tokenMissingMsg
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Strings.ResetPassword.Title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Strings.ResetPassword.BackButtonDescription)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (token == null) {
                Text(
                    text = tokenMissingMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Text(
                    text = stringResource(Strings.ResetPassword.Instructions),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                PasswordTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(Strings.ResetPassword.NewPasswordLabel)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(Strings.ResetPassword.ConfirmPasswordLabel)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                successMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                PrimaryButton(
                    text = if (isLoading)
                        stringResource(Strings.ResetPassword.ResettingPasswordButton)
                    else
                        stringResource(Strings.ResetPassword.ResetPasswordButton),
                    onClick = {
                        if (newPassword.isNotBlank() && confirmPassword.isNotBlank()) {
                            if (newPassword == confirmPassword) {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null

                                    try {
                                        val request = ResetPasswordRequest(
                                            token = token,
                                            newPassword = newPassword,
                                            confirmPassword = confirmPassword
                                        )
                                        val response = api.resetPassword(request)

                                        if (response.success) {
                                            successMessage = response.message
                                            kotlinx.coroutines.delay(2000)
                                            onPasswordResetSuccess()
                                        } else {
                                            errorMessage = response.message
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = genericErrorMsg
                                        println("Erreur lors de la réinitialisation du mot de passe: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = passwordsDoNotMatchMsg
                            }
                        } else {
                            errorMessage = passwordFieldsRequiredMsg
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}