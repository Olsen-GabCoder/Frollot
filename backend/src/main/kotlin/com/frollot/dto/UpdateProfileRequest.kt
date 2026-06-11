package com.frollot.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * DTO pour la mise à jour du profil utilisateur.
 * 
 * Seuls les champs non-null seront mis à jour (update partiel).
 */
data class UpdateProfileRequest(
    @field:Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    val firstName: String? = null,
    
    @field:Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    val lastName: String? = null,
    
    @field:Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Numéro de téléphone invalide"
    )
    val phoneNumber: String? = null,
    
    @field:Size(max = 500, message = "La bio ne peut pas dépasser 500 caractères")
    val bio: String? = null,
    
    val avatarUrl: String? = null,
    
    @field:Pattern(
        regexp = "^(fr|en|es|de|ar)$",
        message = "Langue non supportée. Langues valides : fr, en, es, de, ar"
    )
    val preferredLanguage: String? = null,
    
    @field:Size(max = 100, message = "Le handle Instagram ne peut pas dépasser 100 caractères")
    val instagramHandle: String? = null,
    
    val yearsExperience: Int? = null
)

