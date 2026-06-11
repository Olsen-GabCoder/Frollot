package com.frollot.dto

/**
 * DTO pour la requête de déconnexion.
 */
data class LogoutRequest(
    val refreshToken: String
)

