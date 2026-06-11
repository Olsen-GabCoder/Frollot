@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.components.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.model.VerificationType
import com.frollot.mobile.ui.components.UserAvatar
import com.frollot.mobile.ui.components.VerificationBadge
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding
import com.frollot.mobile.localization.stringResource

/**
 * Configuration pour le header de profil.
 */
data class ProfileHeaderConfig(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val coverImageUrl: String?,
    val isVerified: Boolean,
    val verificationType: VerificationType? = null,
    val userType: UserType,
    val displayName: String? = null // Nom personnalisé (pour les salons par exemple)
)

/**
 * Configuration pour les statistiques affichées dans le header.
 */
data class ProfileStatConfig(
    val value: String,
    val label: String
)

/**
 * Composant générique de header de profil avec photo de couverture, avatar, nom et statistiques.
 * 
 * Ce composant unifie l'affichage des headers de profil pour tous les types d'utilisateurs
 * (clients, coiffeurs, propriétaires de salons) en éliminant la duplication de code.
 * 
 * @param config Configuration du profil à afficher
 * @param isOwner Indique si l'utilisateur courant est le propriétaire du profil
 * @param isFollowed Indique si le profil est suivi par l'utilisateur courant
 * @param onFollowClick Callback appelé lors du clic sur le bouton Follow/Unfollow
 * @param coverImagePreview Aperçu de la nouvelle photo de couverture (si en cours de modification)
 * @param isUploadingCoverImage Indique si l'upload de la photo de couverture est en cours
 * @param coverImageErrorMessage Message d'erreur éventuel lors de l'upload
 * @param onSelectCoverImage Callback appelé pour sélectionner une nouvelle photo de couverture
 * @param onSaveCoverImage Callback appelé pour sauvegarder la nouvelle photo de couverture
 * @param onCancelCoverImage Callback appelé pour annuler la modification de la photo de couverture
 * @param stats Liste des statistiques à afficher
 * @param followButtonText Textes pour les boutons Follow/Unfollow (optionnel, utilise les valeurs par défaut si null)
 */
@Composable
fun ProfileHeaderCard(
    config: ProfileHeaderConfig,
    isOwner: Boolean,
    isFollowed: Boolean,
    onFollowClick: (() -> Unit)? = null,
    coverImagePreview: ImageBitmap? = null,
    isUploadingCoverImage: Boolean = false,
    coverImageErrorMessage: String? = null,
    onSelectCoverImage: () -> Unit = {},
    onSaveCoverImage: () -> Unit = {},
    onCancelCoverImage: () -> Unit = {},
    stats: List<ProfileStatConfig> = emptyList(),
    followButtonText: FollowButtonText? = null
) {
    StandardCardNoPadding(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Photo de couverture avec avatar chevauchant (style Facebook)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // Plus grande pour un effet plus professionnel
            ) {
                // Photo de couverture
                ProfileCoverImageContent(
                    coverImageUrl = config.coverImageUrl,
                    coverImagePreview = coverImagePreview
                )
                
                // Avatar qui chevauche la photo de couverture (positionné en bas à gauche)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 16.dp, y = 60.dp) // Chevauchement de 60dp
                ) {
                    // Bordure blanche autour de l'avatar (style Facebook)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        UserAvatar(
                            user = User(
                                id = config.id,
                                email = config.email,
                                firstName = config.firstName,
                                lastName = config.lastName,
                                avatarUrl = config.avatarUrl,
                                isVerified = config.isVerified,
                                userType = config.userType
                            ),
                            size = 112.dp
                        )
                        // Badge de vérification sur l'avatar
                        if (config.isVerified) {
                            VerificationBadge(
                                isVerified = true,
                                verificationType = config.verificationType,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
                
                // Bouton pour modifier la photo de couverture (propriétaire seulement)
                if (isOwner) {
                    ProfileCoverImageEditControls(
                        coverImagePreview = coverImagePreview,
                        isUploading = isUploadingCoverImage,
                        errorMessage = coverImageErrorMessage,
                        onSelectImage = onSelectCoverImage,
                        onSaveImage = onSaveCoverImage,
                        onCancelImage = onCancelCoverImage
                    )
                }
            }
            
            // Contenu sous la photo de couverture (style Facebook)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp) // Espace pour l'avatar qui chevauche
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nom avec badge de vérification (aligné à gauche comme Facebook)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = config.displayName ?: "${config.firstName ?: ""} ${config.lastName ?: ""}".trim()
                            .ifBlank { config.email },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge de vérification à côté du nom
                    if (config.isVerified && config.verificationType != null) {
                        VerificationBadge(
                            isVerified = true,
                            verificationType = config.verificationType
                        )
                    } else if (config.isVerified) {
                        VerificationBadge(
                            isVerified = true,
                            verificationType = null
                        )
                    }
                }
                
                // Bouton Follow (style Facebook - large et centré)
                if (!isOwner && onFollowClick != null) {
                    val followText = followButtonText?.follow
                    val unfollowText = followButtonText?.unfollow
                    
                    if (followText != null && unfollowText != null) {
                        if (isFollowed) {
                            Button(
                                onClick = onFollowClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.PersonRemove,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = unfollowText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Button(
                                onClick = onFollowClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = followText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Statistiques (style Facebook - horizontal avec séparateurs)
                if (stats.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        stats.forEachIndexed { index, stat ->
                            ProfileStatItemFacebookStyle(
                                value = stat.value,
                                label = stat.label
                            )
                            // Séparateur entre les stats (sauf pour le dernier)
                            if (index < stats.size - 1) {
                                VerticalDivider(
                                    modifier = Modifier.height(40.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
 * Contenu de la photo de couverture (sans les contrôles).
 */
@Composable
private fun ProfileCoverImageContent(
    coverImageUrl: String?,
    coverImagePreview: ImageBitmap?
) {
    when {
        coverImagePreview != null -> {
            Image(
                bitmap = coverImagePreview,
                contentDescription = "Aperçu photo de couverture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        coverImageUrl != null && coverImageUrl.isNotBlank() -> {
            AsyncImage(
                model = coverImageUrl,
                contentDescription = "Photo de couverture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Contrôles d'édition de la photo de couverture (style Facebook - discret).
 * Cette fonction doit être appelée dans un BoxScope.
 */
@Composable
private fun BoxScope.ProfileCoverImageEditControls(
    coverImagePreview: ImageBitmap?,
    isUploading: Boolean,
    errorMessage: String?,
    onSelectImage: () -> Unit,
    onSaveImage: () -> Unit,
    onCancelImage: () -> Unit
) {
    if (coverImagePreview != null) {
        // Boutons d'action lors de l'aperçu
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelImage,
                enabled = !isUploading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Annuler", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = onSaveImage,
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enregistrer", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    } else {
        // Bouton discret pour modifier (style Facebook)
        IconButton(
            onClick = onSelectImage,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                "Modifier la photo",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    
    // Message d'erreur
    errorMessage?.let { error ->
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            shadowElevation = 4.dp
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Item de statistique pour le profil (style Facebook).
 */
@Composable
fun ProfileStatItemFacebookStyle(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Item de statistique pour le profil (version classique - pour compatibilité).
 */
@Composable
fun ProfileStatItem(value: String, label: String) {
    ProfileStatItemFacebookStyle(value = value, label = label)
}

/**
 * Textes personnalisés pour les boutons Follow/Unfollow.
 * Les valeurs peuvent être des StringKey de localisation ou des chaînes directes.
 */
data class FollowButtonText(
    val follow: String,
    val unfollow: String
)

