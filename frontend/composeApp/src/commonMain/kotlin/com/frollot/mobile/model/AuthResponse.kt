package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * Réponse d'authentification unifiée pour tous les endpoints auth.
 *
 * Utilisée par :
 * - POST /api/users/login
 * - POST /api/users/register
 * - POST /api/users/auth/refresh
 *
 * Structure backend (tous les champs à plat, pas d'objet user imbriqué) :
 * ```json
 * {
 *   "accessToken": "eyJhbGc...",
 *   "refreshToken": "eyJhbGc...",
 *   "userId": "507f1f77bcf86cd799439011",
 *   "email": "user@example.com",
 *   "userType": "client",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "isVerified": false,
 *   "isActive": true,
 *   "avatarUrl": "https://example.com/avatar.jpg",
 *   "message": "Connexion réussie",
 *   "emailSendStatus": "dev_mode|sent|failed"
 * }
 * ```
 *
 * Tokens :
 * - Access token : Durée 2h, utilisé dans header Authorization: Bearer
 * - Refresh token : Durée 7j (backend), utilisé pour renouveler l'access token
 *
 * Statut d'envoi d'email :
 * - "dev_mode" : Mode développement - email non envoyé mais token sauvegardé
 * - "sent" : Email envoyé avec succès en production
 * - "failed" : Échec de l'envoi d'email
 * - null : Pas d'information (anciennes versions)
 *
 * Note importante :
 * Le backend fait de la ROTATION des refresh tokens.
 * À chaque refresh, un nouveau refresh token est généré et l'ancien est révoqué.
 *
 * @param accessToken Token JWT d'accès (2h)
 * @param refreshToken Token UUID de refresh (7j, rotation activée)
 * @param userId Identifiant unique de l'utilisateur
 * @param email Email de l'utilisateur
 * @param userType Type d'utilisateur (client, hairstylist, salon_owner, admin)
 * @param firstName Prénom (optionnel)
 * @param lastName Nom (optionnel)
 * @param isVerified Si l'email est vérifié
 * @param isActive Si le compte est actif
 * @param message Message de confirmation (optionnel)
 * @param emailSendStatus Statut d'envoi de l'email ("success", "dev_mode", "dev_redirect", "disabled", "failed")
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val userType: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val avatarUrl: String? = null,
    val message: String? = null,
    val emailSendStatus: String? = null
) {
    /**
     * Convertit l'AuthResponse en objet User pour compatibilité avec le code existant.
     *
     * @return Un objet User avec les données de l'AuthResponse
     */
    fun toUser(): User {
        return User(
            id = userId,
            email = email,
            userType = try {
                UserType.valueOf(userType)
            } catch (e: IllegalArgumentException) {
                // Fallback en cas de valeur inconnue
                UserType.client
            },
            firstName = firstName,
            lastName = lastName,
            phoneNumber = null, // Non fourni par l'endpoint auth
            isVerified = isVerified,
            isActive = isActive,
            createdAt = null, // Non fourni par l'endpoint auth
            updatedAt = null, // Non fourni par l'endpoint auth
            avatarUrl = avatarUrl // Maintenant fourni par l'endpoint auth
        )
    }
}
