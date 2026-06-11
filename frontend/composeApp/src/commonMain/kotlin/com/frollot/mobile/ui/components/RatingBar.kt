package com.frollot.mobile.ui.components
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.foundation.clickable
import com.frollot.mobile.localization.formatLocalizedRating
import com.frollot.mobile.localization.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Barre de notation avec 5 étoiles.
 *
 * @param rating Note actuelle (0-5)
 * @param onRatingChange Callback appelé quand l'utilisateur clique sur une étoile (null si read-only)
 * @param modifier Modifier pour personnaliser l'apparence
 * @param starSize Taille des étoiles
 * @param starColor Couleur des étoiles remplies
 * @param starBorderColor Couleur des étoiles vides
 * @param readOnly Si true, les étoiles ne sont pas cliquables
 */
@Composable
fun RatingBar(
    rating: Int,
    onRatingChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 24.dp,
    starColor: Color = MaterialTheme.colorScheme.tertiary, // Or Élégant de la charte
    starBorderColor: Color = MaterialTheme.colorScheme.outlineVariant, // Gris clair
    readOnly: Boolean = onRatingChange == null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (i <= rating) stringResource(Strings.Components.RatingBar.StarFilled).replace("{number}", i.toString()) else stringResource(Strings.Components.RatingBar.StarEmpty).replace("{number}", i.toString()),
                tint = if (i <= rating) starColor else starBorderColor,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (!readOnly && onRatingChange != null) {
                            Modifier.clickable { onRatingChange(i) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

/**
 * Affiche une note avec le nombre d'étoiles et le texte associé.
 */
@Composable
fun RatingDisplay(
    rating: Double,
    totalReviews: Int? = null,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RatingBar(
            rating = rating.toInt(),
            readOnly = true,
            starSize = 20.dp
        )

        if (showText) {
            Text(
                text = formatLocalizedRating(rating),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        totalReviews?.let { count ->
                androidx.compose.material3.Text(
                    text = stringResource(Strings.Components.RatingBar.ReviewsCount).replace("{count}", count.toString()),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }


