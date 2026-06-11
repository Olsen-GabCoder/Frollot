package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * Ce fichier contenait les anciens modèles LoginResponse et RegisterResponse.
 *
 * Ces modèles ont été remplacés par AuthResponse (défini dans AuthResponse.kt)
 * pour unifier les réponses d'authentification entre backend et frontend.
 *
 * Voir AuthResponse.kt pour le modèle unifié utilisé par :
 * - POST /api/users/login
 * - POST /api/users/register
 * - POST /api/users/refresh
 */

/**
 * Demande de réinitialisation de mot de passe.
 *
 * Utilisée par POST /api/users/forgot-password
 */
@Serializable
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Réponse de demande de réinitialisation de mot de passe.
 *
 * Utilisée par POST /api/users/forgot-password
 */
@Serializable
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String,
    val email: String? = null
)

/**
 * Demande de réinitialisation de mot de passe avec nouveau mot de passe.
 *
 * Utilisée par POST /api/users/reset-password
 */
@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

/**
 * Réponse de réinitialisation de mot de passe.
 *
 * Utilisée par POST /api/users/reset-password
 */
@Serializable
data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)