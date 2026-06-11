package com.frollot.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran dédié au signalement de contenu, conforme à la charte graphique Frollot.
 * 
 * Remplace le dialogue superposé par un écran complet avec :
 * - Contexte visuel du contenu signalé
 * - Sélection de raison claire
 * - Champ de détails supplémentaires
 * - Navigation fluide
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportedEntityType: ReportedEntityType,
    reportedEntityId: String,
    api: FrollotApi,
    currentUser: User,
    onBack: () -> Unit,
    onReportSubmitted: () -> Unit
) {
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var additionalInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reportedPost by remember { mutableStateOf<PostResponse?>(null) }
    var isLoadingPost by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Charger le post si c'est un signalement de post
    LaunchedEffect(reportedEntityId) {
        if (reportedEntityType == ReportedEntityType.POST) {
            isLoadingPost = true
            try {
                reportedPost = api.getPostById(reportedEntityId)
            } catch (e: Exception) {
                // Erreur silencieuse, on continue sans le contexte
            } finally {
                isLoadingPost = false
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header standardisé
        StandardAppHeader(
            currentUser = currentUser,
            title = "Signaler",
            onBackClick = onBack,
            showAvatar = false
        )
        
        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message d'information
            Text(
                text = stringResource(Strings.Report.InfoMessage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Section contexte : Afficher le contenu signalé
            if (reportedEntityType == ReportedEntityType.POST) {
                if (isLoadingPost) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                } else {
                    reportedPost?.let { post ->
                        ReportContextCard(
                            post = post,
                            entityType = reportedEntityType
                        )
                    }
                }
            }
            
            // Section raison du signalement
            Text(
                text = stringResource(Strings.Report.ReasonTitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            ReportReason.entries.forEach { reason ->
                ReportReasonCard(
                    reason = reason,
                    isSelected = selectedReason == reason,
                    onClick = { selectedReason = reason }
                )
            }
            
            // Section informations supplémentaires
            Text(
                text = stringResource(Strings.Report.AdditionalInfo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            StandardTextField(
                value = additionalInfo,
                onValueChange = {
                    if (it.length <= 1000) {
                        additionalInfo = it
                    }
                },
                placeholder = { Text("Décrivez brièvement le problème...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6,
                enabled = !isLoading,
                supportingText = {
                    if (additionalInfo.isNotEmpty()) {
                        Text(
                            text = "${additionalInfo.length}/1000",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            
            // Message d'erreur
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Boutons d'action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = stringResource(Strings.Report.Cancel),
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
                
                Button(
                    onClick = {
                        if (selectedReason == null) {
                            errorMessage = "Veuillez sélectionner une raison"
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
                            } catch (e: Exception) {
                                errorMessage = "Erreur lors du signalement: ${e.message ?: "Erreur inconnue"}"
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = selectedReason != null && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(Strings.Report.Submit),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Carte affichant le contexte du contenu signalé.
 */
@Composable
fun ReportContextCard(
    post: PostResponse,
    entityType: ReportedEntityType
) {
    StandardCardNoPadding(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header avec titre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Strings.Report.ReportedContent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = entityType.getLocalizedDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
            
            // Auteur avec avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar placeholder (PostResponse n'a pas authorAvatarUrl)
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.Report.PostAuthor),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Contenu (tronqué si trop long)
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Image si disponible (afficher le premier média avec une URL)
            post.media.firstOrNull()?.let { media ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = media.mediaUrl,
                        contentDescription = "Image du post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            } ?: post.imageUrl?.let { imageUrl ->
                // Fallback sur imageUrl si media est vide
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Image du post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

/**
 * Carte pour chaque raison de signalement.
 */
@Composable
fun ReportReasonCard(
    reason: ReportReason,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface
    
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    
    val borderWidth = if (isSelected) 1.dp else 0.5.dp
    
    StandardCardNoPadding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reason.getLocalizedDisplayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = reason.getLocalizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

