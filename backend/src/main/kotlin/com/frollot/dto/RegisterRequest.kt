package com.frollot.dto

import com.frollot.model.UserType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO pour la requête d'inscription.
 * Tous les champs obligatoires sont validés avec Jakarta Validation.
 */
data class RegisterRequest(
    @field:NotBlank(message = "L'email est obligatoire")
    @field:Email(message = "Format d'email invalide")
    val email: String = "",
    
    @field:NotBlank(message = "Le mot de passe est obligatoire")
    @field:Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    val password: String = "",
    
    @field:NotBlank(message = "Le prénom est obligatoire")
    val firstName: String = "",
    
    @field:NotBlank(message = "Le nom est obligatoire")
    val lastName: String = "",
    
    val userType: UserType = UserType.client
)