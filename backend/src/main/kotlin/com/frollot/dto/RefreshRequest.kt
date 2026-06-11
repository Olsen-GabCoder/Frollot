package com.frollot.dto

/**
 * DTO pour la requête de rafraîchissement de token.
 */
data class RefreshRequest(
    val refreshToken: String
)

