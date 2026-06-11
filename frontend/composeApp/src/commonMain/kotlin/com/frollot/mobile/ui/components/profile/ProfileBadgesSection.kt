package com.frollot.mobile.ui.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frollot.mobile.model.UserBadgeResponse
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.cards.StandardCardNoPadding

/**
 * Section de badges générique pour les profils.
 * 
 * Ce composant unifie l'affichage des badges pour tous les types de profils
 * en éliminant la duplication de code.
 * 
 * @param badges Liste des badges à afficher
 * @param title Titre de la section (optionnel)
 * @param modifier Modifier pour personnaliser le layout
 * @param displayMode Mode d'affichage : Compact (icônes simples) ou Card (cartes détaillées)
 */
@Composable
fun ProfileBadgesSection(
    badges: List<UserBadgeResponse>,
    title: String? = null,
    modifier: Modifier = Modifier,
    displayMode: BadgeDisplayMode = BadgeDisplayMode.Compact
) {
    if (badges.isEmpty()) return
    
    when (displayMode) {
        BadgeDisplayMode.Compact -> {
            StandardCard(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        badges.forEach { badge ->
                            CompactBadgeItem(badge = badge)
                        }
                    }
                }
            }
        }
        BadgeDisplayMode.Card -> {
            StandardCardNoPadding(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        badges.chunked(4).forEach { rowBadges ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowBadges.forEach { userBadge ->
                                    BadgeCard(badge = userBadge.badge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item de badge compact (affichage simple avec icône et nom).
 */
@Composable
private fun CompactBadgeItem(badge: UserBadgeResponse) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        if (badge.badge.iconUrl != null) {
            AsyncImage(
                model = badge.badge.iconUrl,
                contentDescription = badge.badge.name,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = badge.badge.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2
        )
    }
}

/**
 * Carte d'affichage d'un badge (affichage détaillé avec carte).
 */
@Composable
private fun BadgeCard(badge: com.frollot.mobile.model.BadgeResponse) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icône du badge
            badge.iconUrl?.let { iconUrl ->
                AsyncImage(
                    model = iconUrl,
                    contentDescription = badge.name,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            } ?: run {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Mode d'affichage des badges.
 */
enum class BadgeDisplayMode {
    /** Affichage compact avec icônes simples */
    Compact,
    /** Affichage détaillé avec cartes */
    Card
}

