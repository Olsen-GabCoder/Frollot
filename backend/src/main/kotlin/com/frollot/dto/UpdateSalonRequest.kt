package com.frollot.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class UpdateSalonRequest(
    @field:NotBlank(message = "Le nom est obligatoire")
    val name: String = "",

    @field:NotBlank(message = "L'adresse est obligatoire")
    val address: String = "",

    @field:NotBlank(message = "La ville est obligatoire")
    val city: String = "",

    @field:NotBlank(message = "Le code postal est obligatoire")
    val postalCode: String = "",

    val description: String? = null,

    val phoneNumber: String? = null,

    @field:Email(message = "Format d'email invalide")
    val email: String? = null,

    @field:URL(message = "Format d'URL invalide")
    val websiteUrl: String? = null
)
