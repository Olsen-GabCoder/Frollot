package com.frollot.dto

import com.frollot.model.Review
import java.math.BigDecimal
import java.time.LocalDateTime

// ============================================
// REQUEST DTOs (Client → Serveur)
// ============================================

/**
 * DTO pour créer un nouvel avis.
 *
 * @property salonId ID du salon évalué
 * @property bookingId ID de la réservation associée (obligatoire pour vérification)
 * @property rating Note de 1 à 5 étoiles
 * @property title Titre de l'avis (optionnel)
 * @property content Commentaire de l'avis (optionnel mais recommandé)
 */
data class CreateReviewRequest(
    val salonId: String,
    val bookingId: String,
    val rating: Int,
    val title: String? = null,
    val content: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        require(rating in 1..5) { "La note doit être entre 1 et 5" }
        require(salonId.isNotBlank()) { "L'ID du salon est obligatoire" }
        require(bookingId.isNotBlank()) { "L'ID de la réservation est obligatoire" }
    }
}

/**
 * DTO pour répondre à un avis (owner/manager).
 */
/**
 * DTO pour creer un avis-salon (sans reservation).
 */
data class CreateSalonReviewRequest(
    val salonId: String,
    val rating: Int,
    val title: String? = null,
    val content: String? = null
) {
    fun validate() {
        require(rating in 1..5) { "La note doit être entre 1 et 5" }
        require(salonId.isNotBlank()) { "L'ID du salon est obligatoire" }
        require((content?.length ?: 0) <= 2000) { "Le contenu ne peut pas dépasser 2000 caractères" }
    }
}

data class ReplyToReviewRequest(
    val reply: String
) {
    fun validate() {
        require(reply.isNotBlank()) { "La réponse ne peut pas être vide" }
        require(reply.length <= 2000) { "La réponse ne peut pas dépasser 2000 caractères" }
    }
}

// ============================================
// RESPONSE DTOs (Serveur → Client)
// ============================================

/**
 * DTO de réponse pour un avis complet.
 *
 * Contient toutes les informations nécessaires à l'affichage côté client.
 */
data class ReviewResponse(
    val id: String,
    val salonId: String,
    val salonName: String,
    val staffId: String?,
    val staffName: String?,
    val clientId: String,
    val clientName: String,
    val clientEmail: String,
    val bookingId: String?,
    val rating: Int,
    val title: String?,
    val content: String?,
    val responseSalon: String?,
    val responseAt: LocalDateTime?,
    val responseByName: String? = null,
    val isVerified: Boolean,
    val isVisible: Boolean,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité Review en ReviewResponse.
         */
        fun fromEntity(review: Review, responseByName: String? = null): ReviewResponse {
            val salon = review.salon!!
            val client = review.client!!
            val staff = review.staff

            return ReviewResponse(
                id = review.id!!,
                salonId = salon.id!!,
                salonName = salon.name,
                staffId = staff?.id,
                staffName = staff?.user?.let { user ->
                    "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                        .ifBlank { user.email }
                },
                clientId = client.id!!,
                clientName = "${client.firstName ?: ""} ${client.lastName ?: ""}".trim()
                    .ifBlank { client.email },
                clientEmail = client.email,
                bookingId = review.booking?.id,
                rating = review.rating,
                title = review.title,
                content = review.content,
                responseSalon = review.responseSalon,
                responseAt = review.responseAt,
                responseByName = responseByName,
                isVerified = review.isVerified,
                isVisible = review.isVisible,
                createdAt = review.createdAt
            )
        }
    }
}

/**
 * DTO pour les statistiques d'avis d'un salon.
 */
data class SalonReviewStats(
    val salonId: String,
    val averageRating: BigDecimal,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Long>,
    val verifiedAverage: BigDecimal = BigDecimal.ZERO,
    val verifiedCount: Int = 0,
    val generalAverage: BigDecimal = BigDecimal.ZERO,
    val generalCount: Int = 0
) {
    companion object {
        fun fromSalonAndDistribution(
            salonId: String,
            averageRating: BigDecimal,
            totalReviews: Int,
            distribution: Map<Int, Long>,
            verifiedAverage: BigDecimal = BigDecimal.ZERO,
            verifiedCount: Int = 0,
            generalAverage: BigDecimal = BigDecimal.ZERO,
            generalCount: Int = 0
        ): SalonReviewStats {
            return SalonReviewStats(
                salonId = salonId,
                averageRating = averageRating,
                totalReviews = totalReviews,
                ratingDistribution = distribution,
                verifiedAverage = verifiedAverage,
                verifiedCount = verifiedCount,
                generalAverage = generalAverage,
                generalCount = generalCount
            )
        }
    }
}

