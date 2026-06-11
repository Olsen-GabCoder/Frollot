package com.frollot.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.frollot.mobile.localization.*
import com.frollot.mobile.model.User

/**
 * Composant réutilisable pour afficher l'avatar d'un utilisateur.
 * Charge automatiquement la photo depuis l'URL si disponible, sinon affiche les initiales.
 * 
 * @param user Utilisateur dont on veut afficher l'avatar
 * @param size Taille de l'avatar (défaut: 40.dp)
 * @param showBorder Afficher une bordure gradient (défaut: false)
 * @param borderWidth Largeur de la bordure si showBorder = true (défaut: 2.dp)
 */
@Composable
fun UserAvatar(
    user: User?,
    size: Dp = 40.dp,
    showBorder: Boolean = false,
    borderWidth: Dp = 2.dp
) {
    val displayName = user?.let { u ->
        buildString {
            u.firstName?.let { append(it.firstOrNull()?.uppercase() ?: "U") }
            u.lastName?.let { append(it.firstOrNull()?.uppercase() ?: "") }
            if (isEmpty()) append(u.email.firstOrNull()?.uppercase() ?: "U")
        }
    } ?: "U"

    if (showBorder) {
        // Avatar avec bordure gradient
        Box(modifier = Modifier.size(size)) {
            // Bordure gradient externe
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Espace blanc
                Box(
                    modifier = Modifier
                        .size(size - borderWidth * 2)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar intérieur
                    UserAvatarContent(
                        user = user,
                        size = size - borderWidth * 4,
                        displayName = displayName
                    )
                }
            }
        }
    } else {
        // Avatar simple
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            UserAvatarContent(
                user = user,
                size = size,
                displayName = displayName
            )
        }
    }
}

@Composable
private fun UserAvatarContent(
    user: User?,
    size: Dp,
    displayName: String
) {
    user?.avatarUrl?.let { avatarUrl ->
        // Charger l'image depuis l'URL
        AsyncImage(
            model = avatarUrl,
            contentDescription = stringResource(Strings.Components.UserAvatar.ContentDescription).replace("{name}", user.firstName ?: user.email),
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } ?: run {
        // Afficher les initiales
        androidx.compose.material3.Text(
            text = displayName.take(2),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = (size.value * 0.4).sp
        )
    }
}

