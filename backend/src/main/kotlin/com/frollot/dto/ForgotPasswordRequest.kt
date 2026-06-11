package com.frollot.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ForgotPasswordRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Format d'email invalide")
    val email: String
)