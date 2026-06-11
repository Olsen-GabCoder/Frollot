@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.localization.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran pour demander une vérification (utilisateur ou salon).
 * Phase H.2 - Vérification Salons/Coiffeurs
 * 
 * @param entityType Type d'entité ('user' ou 'salon')
 * @param entityId ID de l'entité à vérifier
 * @param api Instance de l'API
 * @param currentUser Utilisateur authentifié
 * @param onBack Callback pour revenir en arrière
 * @param onVerificationRequested Callback appelé après une demande réussie
 */
@Composable
fun RequestVerificationScreen(
    entityType: String,
    entityId: String,
    api: FrollotApi,
    currentUser: User?,
    onBack: () -> Unit,
    onVerificationRequested: () -> Unit
) {
    var selectedVerificationType by remember { mutableStateOf<VerificationType?>(null) }
    var additionalInfo by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                title = stringResource(Strings.RequestVerification.Title),
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(Strings.RequestVerification.HeaderTitle),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(Strings.RequestVerification.Description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Sélection du type de vérification
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Strings.RequestVerification.VerificationType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                VerificationType.entries.forEach { type ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedVerificationType == type),
                                onClick = { selectedVerificationType = type }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (selectedVerificationType == type) 2.dp else 1.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedVerificationType == type) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (selectedVerificationType == type) 1.dp else 0.5.dp,
                            color = if (selectedVerificationType == type) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = (selectedVerificationType == type),
                                onClick = { selectedVerificationType = type }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = type.getEmoji(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = type.getLocalizedDisplayName(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = type.getLocalizedDescription(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Informations supplémentaires
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StandardTextField(
                    value = additionalInfo,
                    onValueChange = {
                        if (it.length <= 2000) {
                            additionalInfo = it
                        }
                    },
                    label = { Text(stringResource(Strings.RequestVerification.AdditionalInfo)) },
                    placeholder = { Text(stringResource(Strings.RequestVerification.AdditionalInfoPlaceholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    minLines = 4,
                    enabled = !isSubmitting
                )
                Text(
                    text = "${additionalInfo.length}/2000",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Messages d'erreur et de succès
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            successMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Bouton de soumission
            PrimaryButton(
                text = if (isSubmitting) "Envoi en cours..." else "Envoyer la demande",
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        errorMessage = null
                        successMessage = null
                        try {
                            if (selectedVerificationType == null) {
                                errorMessage = "Veuillez sélectionner un type de vérification."
                                return@launch
                            }
                            val request = RequestVerificationRequest(
                                verificationType = selectedVerificationType!!,
                                additionalInfo = additionalInfo.takeIf { it.isNotBlank() }
                            )
                            val response = api.requestVerification(entityType, entityId, request)
                            successMessage = response["message"] ?: "Votre demande a été enregistrée avec succès !"
                            // Délai pour que l'utilisateur puisse lire le message de succès
                            kotlinx.coroutines.delay(2000)
                            onVerificationRequested()
                        } catch (e: Exception) {
                            errorMessage = "Erreur lors de l'envoi de la demande: ${e.message}"
                            FrollotLogger.error("API", "❌ Erreur demande vérification: ${e.message}")
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedVerificationType != null && !isSubmitting,
                size = com.frollot.mobile.ui.components.buttons.ButtonSize.Large,
                icon = if (isSubmitting) {
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

