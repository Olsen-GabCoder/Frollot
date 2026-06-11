package com.frollot.mobile.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ============================================
// REQUEST DTOs
// ============================================

/**
 * DTO pour créer un nouvel avis.
 *
 * IMPORTANT : Correspond au CreateReviewRequest du backend.
 */
@Serializable
data class CreateReviewRequest(
    val salonId: String,
    val bookingId: String,
    val rating: Int,
    val title: String? = null,
    val content: String? = null
)

// ============================================
// RESPONSE DTOs
// ============================================

/**
 * DTO représentant un avis reçu du backend.
 */
@Serializable
data class Review(
    val id: String,
    val salonId: String,
    val salonName: String,
    val staffId: String? = null,
    val staffName: String? = null,
    val clientId: String,
    val clientName: String,
    val clientEmail: String,
    val bookingId: String? = null,
    val rating: Int,
    val title: String? = null,
    val content: String? = null,
    val responseSalon: String? = null,
    val responseAt: String? = null,
    val isVerified: Boolean,
    val isVisible: Boolean,
    val createdAt: String?
)

/**
 * DTO représentant les statistiques d'avis d'un salon.
 */
@Serializable
data class SalonReviewStats(
    val salonId: String,
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<String, Long> // Map<rating, count> - String keys for JSON
)

