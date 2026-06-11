package com.frollot.dto

import com.frollot.model.User
import com.frollot.model.UserType

/**
 * DTO unifié pour les réponses d'authentification.
 *
 * Utilisé par :
 * - POST /api/users/login
 * - POST /api/users/register
 * - POST /api/users/refresh (à implémenter)
 *
 * Format plat (tous les champs à plat, pas d'objet user imbriqué) pour faciliter
 * la désérialisation côté frontend et réduire la complexité.
 *
 * Structure JSON :
 * ```json
 * {
 *   "accessToken": "eyJhbGc...",
 *   "refreshToken": "uuid-refresh-token",
 *   "userId": "507f1f77bcf86cd799439011",
 *   "email": "user@example.com",
 *   "userType": "client",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "isVerified": false,
 *   "isActive": true,
 *   "avatarUrl": "https://example.com/avatar.jpg",
 *   "message": "Connexion réussie"
 * }
 * ```
 *
 * Tokens :
 * - Access token : JWT, durée 15 minutes (configurable), utilisé dans header Authorization: Bearer
 * - Refresh token : UUID, durée 7 jours, stocké en BDD, utilisé pour renouveler l'access token
 *
 * Note importante :
 * Le backend fait de la ROTATION des refresh tokens.
 * À chaque refresh, un nouveau refresh token est généré et l'ancien est révoqué.
 *
 * @param accessToken Token JWT d'accès (15 min par défaut)
 * @param refreshToken Token UUID de refresh (7 jours, rotation activée)
 * @param userId Identifiant unique de l'utilisateur
 * @param email Email de l'utilisateur
 * @param userType Type d'utilisateur (client, hairstylist, salon_owner, admin)
 * @param firstName Prénom (optionnel)
 * @param lastName Nom (optionnel)
 * @param isVerified Si l'email est vérifié
 * @param isActive Si le compte est actif
 * @param avatarUrl URL de l'avatar (optionnel)
 * @param message Message de confirmation (optionnel)
 * @param emailSendStatus Statut d'envoi de l'email ("success", "dev_mode", "dev_redirect", "disabled", "failed")
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val userType: UserType,
    val firstName: String? = null,
    val lastName: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val avatarUrl: String? = null,
    val message: String? = null,
    val emailSendStatus: String? = null
) {
    companion object {
        /**
         * Crée un AuthResponse à partir d'un User et des tokens.
         *
         * @param user L'utilisateur authentifié
         * @param accessToken Le token JWT d'accès
         * @param refreshToken Le token UUID de refresh
         * @param message Message optionnel de confirmation
         * @return Un AuthResponse avec toutes les informations de l'utilisateur
         */
        fun fromUser(
            user: User,
            accessToken: String,
            refreshToken: String,
            message: String? = null,
            emailSendStatus: String? = null
        ): AuthResponse {
            // LOGS CRITIQUES POUR DEBUGGER LE PROBLÈME DE REDIRECTION
            println("🔧 [AuthResponse.fromUser] Construction de la réponse:")
            println("🔧 [AuthResponse.fromUser] user.emailVerified = ${user.emailVerified}")
            println("🔧 [AuthResponse.fromUser] user.isVerified = ${user.isVerified}")
            println("🔧 [AuthResponse.fromUser] user.email = ${user.email}")

            val response = AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = user.id!!,
                email = user.email,
                userType = user.userType,
                firstName = user.firstName,
                lastName = user.lastName,
                isVerified = user.emailVerified, // CORRECTION: Utiliser emailVerified au lieu de isVerified
                isActive = user.isActive,
                avatarUrl = user.avatarUrl,
                message = message,
                emailSendStatus = emailSendStatus
            )

            println("🔧 [AuthResponse.fromUser] response.isVerified = ${response.isVerified}")
            return response
        }

        /**
         * Crée un AuthResponse pour une pré-inscription (avant vérification email).
         *
         * Utilisé pour le nouveau système de pré-inscription où l'utilisateur doit
         * d'abord vérifier son email avant que le compte soit créé.
         *
         * @param pendingId ID de la pré-inscription
         * @param email Email de l'utilisateur
         * @param message Message d'information
         * @param emailSendStatus Statut d'envoi de l'email
         * @return Un AuthResponse adapté pour la pré-inscription
         */
        fun preRegistration(
            pendingId: String,
            email: String,
            message: String,
            emailSendStatus: String? = null
        ): AuthResponse {
            return AuthResponse(
                accessToken = "", // Pas de token tant que non vérifié
                refreshToken = "", // Pas de token tant que non vérifié
                userId = pendingId, // ID de la pré-inscription
                email = email,
                userType = UserType.client, // Valeur par défaut
                firstName = null,
                lastName = null,
                isVerified = false, // Email non vérifié
                isActive = false, // Compte pas encore actif
                avatarUrl = null,
                message = message,
                emailSendStatus = emailSendStatus
            )
        }

        /**
         * Crée un AuthResponse pour une erreur d'authentification.
         *
         * Utilisé pour les cas d'erreur où on veut retourner un format cohérent
         * mais avec des valeurs nulles/vides pour indiquer l'échec.
         *
         * @param message Message d'erreur
         * @return Un AuthResponse avec des valeurs par défaut et le message d'erreur
         */
        fun error(message: String): AuthResponse {
            return AuthResponse(
                accessToken = "",
                refreshToken = "",
                userId = "",
                email = "",
                userType = UserType.client, // Valeur par défaut, ne sera pas utilisée
                message = message
            )
        }
    }
}

