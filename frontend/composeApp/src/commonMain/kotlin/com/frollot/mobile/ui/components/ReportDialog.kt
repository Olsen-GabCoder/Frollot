@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.components

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
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Dialog de signalement de contenu.
 * Phase H.1 - Signalement de Contenu
 * 
 * Permet à un utilisateur de signaler du contenu inapproprié avec une raison et des informations supplémentaires.
 */
@Composable
fun ReportDialog(
    reportedEntityType: ReportedEntityType,
    reportedEntityId: String,
    api: FrollotApi,
    onDismiss: () -> Unit,
    onReportSubmitted: () -> Unit
) {
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var additionalInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    
    // Strings localisées
    val errorSelectReasonText = stringResource(Strings.Components.ReportDialog.ErrorSelectReason)
    val errorReportingText = stringResource(Strings.Components.ReportDialog.ErrorReporting)
    val errorUnknownText = stringResource(Strings.Components.ReportDialog.ErrorUnknown)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Strings.Components.ReportDialog.Title).replace("{entity}", reportedEntityType.getLocalizedDisplayName().lowercase()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Message d'information
                Text(
                    text = stringResource(Strings.Components.ReportDialog.InfoMessage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Sélection de la raison
                Text(
                    text = stringResource(Strings.Components.ReportDialog.ReasonLabel),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ReportReason.entries.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reason.getLocalizedDisplayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = reason.getLocalizedDescription(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Informations supplémentaires (optionnel)
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = {
                        if (it.length <= 1000) {
                            additionalInfo = it
                        }
                    },
                    label = { Text(stringResource(Strings.Components.ReportDialog.AdditionalInfoLabel)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(Strings.Components.ReportDialog.AdditionalInfoPlaceholder))
                    },
                    maxLines = 4,
                    singleLine = false,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (additionalInfo.isNotEmpty()) {
                    Text(
                        text = "${additionalInfo.length}/1000",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Message d'erreur
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedReason == null) {
                        errorMessage = errorSelectReasonText
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        try {
                            val request = CreateReportRequest(
                                reportedEntityType = reportedEntityType,
                                reportedEntityId = reportedEntityId,
                                reason = selectedReason!!,
                                additionalInfo = additionalInfo.takeIf { it.isNotBlank() }
                            )

                            api.reportContent(request)
                            
                            onReportSubmitted()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = errorReportingText.replace("{error}", e.message ?: errorUnknownText)
                            isLoading = false
                        }
                    }
                },
                enabled = selectedReason != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Strings.Report.Submit))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(Strings.Common.Cancel))
            }
        }
    )
}

