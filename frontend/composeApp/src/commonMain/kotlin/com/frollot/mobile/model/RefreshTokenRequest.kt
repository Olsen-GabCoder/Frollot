package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * Requête pour renouveler l'access token via le refresh token.
 *
 * Endpoint : POST /api/users/auth/refresh
 * Body : { "refreshToken": "eyJhbGc..." }
 *
 * Réponse attendue :
 * - 200 OK : AuthResponse avec nouveaux tokens (rotation activée)
 * - 401 Unauthorized : Refresh token invalide/expiré/révoqué
 *
 * @param refreshToken Le refresh token actuellement valide (durée : 30 jours)
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)
