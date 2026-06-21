package com.frollot.dto

import com.frollot.model.VerificationType
import java.time.LocalDateTime
import java.math.BigDecimal

/**
 * DTO pour représenter un salon dans les réponses API.
 * Phase H.2 - Vérification Salons/Coiffeurs (ajout isVerified et verificationType)
 */
data class SalonResponse(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val description: String?,
    val slug: String,
    val ownerId: String,
    val coverPhotoUrl: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val websiteUrl: String? = null,
    val isVerified: Boolean = false, // Phase H.2 - Vérification Salons/Coiffeurs
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val averageRating: BigDecimal = BigDecimal.ZERO,
    val reviewCount: Int = 0,
    val isFollowedByCurrentUser: Boolean? = null,
    val followersCount: Long? = null,
    val openingHours: Map<String, List<Map<String, String>>>? = null,
    val timezone: String = "Africa/Libreville",
    val createdAt: LocalDateTime?
)