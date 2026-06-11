package com.frollot.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.RatingBar
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.forms.StandardTextField
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran de création d'un avis pour une réservation terminée.
 *
 * Permet au client de :
 * - Noter le salon (1-5 étoiles)
 * - Ajouter un titre (optionnel)
 * - Ajouter un commentaire (optionnel)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewScreen(
    salon: Salon,
    booking: BookingResponse,
    onBack: () -> Unit,
    onReviewCreated: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { FrollotApi() }


    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = null,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = stringResource(Strings.CreateReview.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Espace en haut
            Spacer(modifier = Modifier.height(16.dp))

            // Carte d'information sur la réservation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Strings.CreateReview.YourBooking),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = booking.serviceName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Strings.CreateReview.Salon).replace("{salonName}", booking.salonName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    booking.bookingDatetime?.let {
                        Text(
                            text = stringResource(Strings.CreateReview.Date).replace("{date}", it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section de notation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(Strings.CreateReview.RatingQuestion),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    RatingBar(
                        rating = rating,
                        onRatingChange = { rating = it },
                        starSize = 40.dp,
                        readOnly = false
                    )

                    if (rating > 0) {
                        Text(
                            text = when (rating) {
                                1 -> "Très déçu"
                                2 -> "Déçu"
                                3 -> "Moyen"
                                4 -> "Bien"
                                5 -> "Excellent"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Section titre (optionnel)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StandardTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(Strings.CreateReview.TitleLabel)) },
                    placeholder = { Text(stringResource(Strings.CreateReview.TitlePlaceholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Section commentaire (optionnel)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StandardTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(Strings.CreateReview.CommentLabel)) },
                    placeholder = { Text(stringResource(Strings.CreateReview.CommentPlaceholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
            }

            // Message d'erreur
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Bouton de soumission
            PrimaryButton(
                text = if (isSubmitting) "Publication..." else "Publier l'avis",
                onClick = {
                    if (rating == 0) {
                        errorMessage = "Veuillez sélectionner une note"
                        return@PrimaryButton
                    }

                    scope.launch {
                        try {
                            isSubmitting = true
                            errorMessage = null

                            // Vérifier que nous avons les IDs nécessaires
                            if (salon.id.isBlank() || booking.id.isBlank()) {
                                errorMessage = "Informations incomplètes. Veuillez réessayer."
                                isSubmitting = false
                                return@launch
                            }

                            FrollotLogger.debug("API", "📤 Création d'avis pour la réservation: ${booking.id}")
                            FrollotLogger.debug("API", "📤 Salon: ${salon.id}")
                            FrollotLogger.debug("API", "📤 Note: $rating")

                            val request = CreateReviewRequest(
                                salonId = salon.id,
                                bookingId = booking.id,
                                rating = rating,
                                title = title.takeIf { it.isNotBlank() }?.trim(),
                                content = content.takeIf { it.isNotBlank() }?.trim()
                            )

                            // ✅ APPEL CORRIGÉ : plus besoin de passer currentUser.id
                            // Le token JWT est envoyé automatiquement via l'intercepteur
                            val review = api.createReview(request)

                            FrollotLogger.success("API", "✅ Avis créé avec succès: ${review.id}")
                            onReviewCreated()

                        } catch (e: IllegalArgumentException) {
                            FrollotLogger.error("API", "❌ Erreur de validation: ${e.message}")
                            errorMessage = "Données invalides: ${e.message}"
                        } catch (e: io.ktor.client.plugins.ClientRequestException) {
                            FrollotLogger.error("API", "❌ Erreur client (4xx): ${e.message}")
                            when (e.response.status.value) {
                                400 -> errorMessage = "Les données sont invalides. Vérifiez vos informations."
                                401 -> errorMessage = "Vous devez être connecté pour créer un avis."
                                403 -> errorMessage = "Vous n'êtes pas autorisé à créer un avis pour cette réservation."
                                404 -> errorMessage = "La réservation ou le salon n'existe plus."
                                409 -> errorMessage = "Un avis existe déjà pour cette réservation."
                                else -> errorMessage = "Erreur de requête: ${e.message}"
                            }
                        } catch (e: io.ktor.client.plugins.ServerResponseException) {
                            FrollotLogger.error("API", "❌ Erreur serveur (5xx): ${e.message}")
                            errorMessage = "Erreur serveur. Veuillez réessayer plus tard."
                        } catch (e: Exception) {
                            FrollotLogger.error("API", "❌ Erreur création avis: ${e.message}")
                            errorMessage = "Impossible de publier l'avis: ${e.message ?: "Erreur inconnue"}"
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                enabled = !isSubmitting && rating > 0,
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

            // Espace en bas
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
