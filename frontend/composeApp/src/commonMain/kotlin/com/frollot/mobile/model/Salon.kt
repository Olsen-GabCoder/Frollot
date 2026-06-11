package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * DTO représentant un salon reçu du backend.
 *
 * CORRECTION : Harmonisation avec backend/src/main/kotlin/com/frollot/dto/SalonResponse.kt
 * - Ajout du champ slug manquant
 * - Correction du type createdAt (LocalDateTime sérialisé en String)
 */
@Serializable
data class Salon(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val description: String?,
    val slug: String, // CORRECTION : Champ manquant ajouté
    val ownerId: String,
    val createdAt: String?, // CORRECTION : LocalDateTime backend sérialisé en String
    val coverPhotoUrl: String? = null,
    val latitude: Double? = null, // CORRECTION : BigDecimal backend sérialisé en Double
    val longitude: Double? = null, // CORRECTION : BigDecimal backend sérialisé en Double
    val isVerified: Boolean = false, // Phase H.2 - Vérification Salons/Coiffeurs
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val isFollowedByCurrentUser: Boolean? = null,
    val followersCount: Long? = null
)
