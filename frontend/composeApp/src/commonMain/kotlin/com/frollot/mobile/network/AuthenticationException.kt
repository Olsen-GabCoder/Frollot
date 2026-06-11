package com.frollot.mobile.network

/**
 * Exception levée lors d'une erreur d'authentification.
 * 
 * Cette exception est utilisée pour :
 * - Token JWT expiré et refresh impossible
 * - Refresh token invalide ou révoqué
 * - Utilisateur non authentifié tentant d'accéder à une ressource protégée
 * 
 * L'application doit gérer cette exception en :
 * 1. Affichant un message à l'utilisateur
 * 2. Redirigeant vers l'écran de connexion
 * 3. Nettoyant les données de session
 */
class AuthenticationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    companion object {
        /**
         * Code d'erreur pour session expirée.
         */
        const val CODE_SESSION_EXPIRED = "SESSION_EXPIRED"
        
        /**
         * Code d'erreur pour refresh token invalide.
         */
        const val CODE_REFRESH_INVALID = "REFRESH_INVALID"
        
        /**
         * Code d'erreur pour utilisateur non authentifié.
         */
        const val CODE_NOT_AUTHENTICATED = "NOT_AUTHENTICATED"
    }
}
