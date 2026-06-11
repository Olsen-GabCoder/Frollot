package com.frollot.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.ui.utils.ExternalShare
import com.frollot.mobile.ui.utils.generatePostShareText
import com.frollot.mobile.ui.utils.rememberExternalShare
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Phase I.2 - Partage Externe
 * Dialog permettant de partager un post vers des applications externes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalShareDialog(
    postId: String,
    postContent: String,
    authorName: String,
    imageUrl: String? = null,
    externalShare: ExternalShare? = null,
    onDismiss: () -> Unit,
    onShareSuccess: (() -> Unit)? = null
) {
    // Phase I.2 - Obtenir ExternalShare si non fourni
    val defaultShare = rememberExternalShare()
    val shareInstance = externalShare ?: defaultShare
    
    var isSharing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Strings localisées
    val errorSharingText = stringResource(Strings.Components.ExternalShareDialog.ErrorSharing)
    val errorCopyingText = stringResource(Strings.Components.ExternalShareDialog.ErrorCopying)
    
    // Générer le texte de partage
    val shareText = remember(postId, postContent, authorName) {
        generatePostShareText(
            postContent = postContent,
            authorName = authorName,
            postId = postId
        )
    }
    
    com.frollot.mobile.ui.components.dialogs.StandardDialog(
        title = stringResource(Strings.Components.ExternalShareDialog.Title),
        onDismissRequest = onDismiss,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (shareInstance != null) {
                    // Option 1 : Partager via l'application native
                    ShareOptionButton(
                        icon = Icons.Default.Share,
                        title = stringResource(Strings.Components.ExternalShareDialog.ShareViaApp),
                        description = stringResource(Strings.Components.ExternalShareDialog.ShareViaAppDescription),
                        onClick = {
                            isSharing = true
                            scope.launch {
                                shareInstance.share(
                                    text = shareText,
                                    imageUrl = imageUrl,
                                    onSuccess = {
                                        isSharing = false
                                        onShareSuccess?.invoke()
                                        onDismiss()
                                    },
                                    onError = { error ->
                                        isSharing = false
                                        errorMessage = errorSharingText.replace("{error}", error.message ?: "")
                                    }
                                )
                            }
                        },
                        enabled = !isSharing
                    )
                    
                    // Option 2 : Copier le lien
                    ShareOptionButton(
                        icon = Icons.Default.ContentCopy,
                        title = stringResource(Strings.Components.ExternalShareDialog.CopyLink),
                        description = stringResource(Strings.Components.ExternalShareDialog.CopyLinkDescription),
                        onClick = {
                            // Pour l'instant, on utilise le partage pour copier
                            // TODO: Implémenter la copie dans le presse-papiers native
                            scope.launch {
                                shareInstance.share(
                                    text = shareText,
                                    onSuccess = {
                                        onDismiss()
                                    },
                                    onError = { error ->
                                        errorMessage = errorCopyingText.replace("{error}", error.message ?: "")
                                    }
                                )
                            }
                        },
                        enabled = !isSharing
                    )
                } else {
                    Text(
                        text = stringResource(Strings.Components.ExternalShareDialog.NotAvailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        dismissButton = {
            com.frollot.mobile.ui.components.buttons.SecondaryButton(
                text = stringResource(Strings.Common.Cancel),
                onClick = onDismiss
            )
        }
    )
    
    // Indicateur de chargement
    if (isSharing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ShareOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

