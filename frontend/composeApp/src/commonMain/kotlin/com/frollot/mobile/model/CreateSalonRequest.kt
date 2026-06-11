package com.frollot.mobile.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateSalonRequest(
    val id: String = "",  // Ajouté : requis par le backend
    val name: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val description: String? = null,
    val ownerId: String,
    val createdAt: String = "",  // Ajouté : requis par le backend
    val coverPhotoUrl: String? = null,
    val latitude: Double? = null,  // CORRECTION : Ajouté pour harmonisation avec backend
    val longitude: Double? = null  // CORRECTION : Ajouté pour harmonisation avec backend
)
