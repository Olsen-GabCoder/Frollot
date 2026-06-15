package com.frollot.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTOs 2FA TOTP (S9a).
 *
 * RÈGLE DURE : le secret en clair n'apparaît QUE dans TwoFactorSetupResponse
 * (tant que enabled=false). Aucun autre DTO ne doit jamais le transporter.
 */

/**
 * Réponse du POST /api/users/me/2fa/setup.
 *
 * @param secret Secret Base32 en clair (saisie manuelle dans l'app d'authentification)
 * @param otpauthUri URI otpauth://totp/... à encoder en QR code côté client (S9d)
 */
data class TwoFactorSetupResponse(
    val secret: String,
    val otpauthUri: String
)

/**
 * Requête du POST /api/users/me/2fa/confirm.
 */
data class TwoFactorConfirmRequest(
    @field:NotBlank(message = "Le code de vérification est requis")
    val code: String
)

/**
 * Réponse du POST /api/users/me/2fa/confirm.
 *
 * @param recoveryCodes Les 10 codes de récupération EN CLAIR — montrés une
 *        seule fois, stockés hachés (BCrypt). Jamais re-consultables.
 */
data class TwoFactorConfirmResponse(
    val success: Boolean,
    val message: String,
    val recoveryCodes: List<String>
)

/**
 * Réponse du GET /api/users/me/2fa/status. Jamais le secret.
 */
data class TwoFactorStatusResponse(
    val enabled: Boolean
)

/**
 * Requête du POST /api/users/login/2fa (S9b — étape 2 du login).
 *
 * @param twoFactorToken Jeton de défi (JWT type=2fa_pending, 5 min) reçu au login
 * @param code Code TOTP courant (6 chiffres) OU code de récupération (XXXX-XXXX)
 */
data class TwoFactorLoginRequest(
    @field:NotBlank(message = "Le jeton de vérification est requis")
    val twoFactorToken: String,

    @field:NotBlank(message = "Le code de vérification est requis")
    val code: String
)

/**
 * Réponse d'erreur 2FA (refus setup/confirm).
 */
data class TwoFactorErrorResponse(
    val success: Boolean = false,
    val message: String
)

/**
 * Requête du DELETE /api/users/me/2fa (S9c — désactivation).
 *
 * Action sensible (annulation d'une protection) : exige les DEUX preuves —
 * mot de passe ET code (TOTP courant OU code de récupération non utilisé).
 */
data class TwoFactorDisableRequest(
    @field:NotBlank(message = "Le mot de passe est requis")
    val password: String,

    @field:NotBlank(message = "Le code de vérification est requis")
    val code: String
)

/**
 * Réponse du DELETE /api/users/me/2fa.
 */
data class TwoFactorDisableResponse(
    val success: Boolean,
    val message: String
)

/**
 * Requête du POST /api/users/me/2fa/recovery-codes/regenerate (S9c).
 *
 * Même niveau de preuve que la désactivation : mot de passe + code
 * (TOTP courant OU code de récupération — un code consommé ici est de
 * toute façon remplacé par le nouveau lot).
 */
data class TwoFactorRegenerateRequest(
    @field:NotBlank(message = "Le mot de passe est requis")
    val password: String,

    @field:NotBlank(message = "Le code de vérification est requis")
    val code: String
)
