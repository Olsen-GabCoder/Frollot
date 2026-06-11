package com.frollot.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank(message = "Le token de réinitialisation est requis")
    val token: String,

    @field:NotBlank(message = "Le nouveau mot de passe est requis")
    @field:Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    val newPassword: String,

    @field:NotBlank(message = "La confirmation du mot de passe est requise")
    val confirmPassword: String
)