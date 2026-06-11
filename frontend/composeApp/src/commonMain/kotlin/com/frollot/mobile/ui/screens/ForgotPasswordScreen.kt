package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.frollot.mobile.localization.Strings
import com.frollot.mobile.localization.stringResource
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.model.ForgotPasswordRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    api: com.frollot.mobile.network.FrollotApi,
    onNavigateBack: () -> Unit,
    onResetRequestSent: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Capturer les strings dans le contexte @Composable
    val emailRequiredError = stringResource(Strings.ForgotPassword.EmailRequiredError)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Strings.ForgotPassword.Title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Strings.ForgotPassword.BackButtonDescription)
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
            Text(
                text = stringResource(Strings.ForgotPassword.Instructions),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            StandardTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Strings.ForgotPassword.EmailLabel)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
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
                    stringResource(Strings.ForgotPassword.SendingEmailButton)
                else
                    stringResource(Strings.ForgotPassword.SendResetLinkButton),
                onClick = {
                    if (email.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            try {
                                val request = ForgotPasswordRequest(email = email)
                                val response = api.forgotPassword(request)

                                if (response.success) {
                                    successMessage = response.message
                                    kotlinx.coroutines.delay(2000)
                                    onResetRequestSent(email)
                                } else {
                                    errorMessage = response.message
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erreur lors de l'envoi de l'email. Veuillez réessayer."
                                println("Erreur lors de la demande de réinitialisation: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = emailRequiredError
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}