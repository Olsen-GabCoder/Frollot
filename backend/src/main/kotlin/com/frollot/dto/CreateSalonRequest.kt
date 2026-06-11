package com.frollot.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank

@JsonIgnoreProperties(ignoreUnknown = true) // Ignore les champs inconnus
data class CreateSalonRequest(
    val id: String = "",  // Ignoré par le backend, mais accepté depuis le mobile

    @field:NotBlank(message = "Le nom est obligatoire")
    val name: String = "",

    @field:NotBlank(message = "L'adresse est obligatoire")
    val address: String = "",

    @field:NotBlank(message = "La ville est obligatoire")
    val city: String = "",

    @field:NotBlank(message = "Le code postal est obligatoire")
    val postalCode: String = "",

    val description: String? = null,

    @field:NotBlank(message = "L'ownerId est obligatoire")
    val ownerId: String = "",

    val createdAt: String = "",  // Ignoré par le backend, mais accepté depuis le mobile

    val coverPhotoUrl: String? = null,

    val latitude: java.math.BigDecimal? = null,

    val longitude: java.math.BigDecimal? = null
)