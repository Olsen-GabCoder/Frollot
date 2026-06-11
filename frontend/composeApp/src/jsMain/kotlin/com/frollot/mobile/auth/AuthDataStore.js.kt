package com.frollot.mobile.auth

import androidx.compose.runtime.Composable

/**
 * Implémentation JavaScript/Web de AuthDataStore utilisant localStorage.
 * 
 * Les tokens sont stockés dans localStorage du navigateur.
 * 
 * NOTE SÉCURITÉ :
 * localStorage stocke les données en clair. C'est une limitation du web.
 * Pour une sécurité maximale, il faudrait utiliser des cookies HttpOnly,
 * mais cela nécessiterait des modifications côté backend.
 * 
 * Sécurité actuelle :
 * - Les tokens sont stockés dans localStorage (accessible par JavaScript)
 * - Non chiffrés (limitation du web)
 * - Persistants entre les sessions du navigateur
 * 
 * Limitations :
 * - Vulnérable aux attaques XSS (si du code malveillant est injecté)
 * - Accessible par toutes les pages du même domaine
 */
class JsAuthDataStore : AuthDataStore {
    
    private val ACCESS_TOKEN_KEY = "frollot_access_token"
    private val REFRESH_TOKEN_KEY = "frollot_refresh_token"
    
    override suspend fun getAccessToken(): String? {
        return try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.getItem")(ACCESS_TOKEN_KEY) as? String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getRefreshToken(): String? {
        return try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.getItem")(REFRESH_TOKEN_KEY) as? String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.setItem")(ACCESS_TOKEN_KEY, accessToken)
                js("window.localStorage.setItem")(REFRESH_TOKEN_KEY, refreshToken)
            }
        } catch (e: Exception) {
            // Ignorer les erreurs de localStorage (mode privé, quota, etc.)
            println("❌ Erreur lors de la sauvegarde des tokens: ${e.message}")
        }
    }
    
    override suspend fun clearTokens() {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.removeItem")(ACCESS_TOKEN_KEY)
                js("window.localStorage.removeItem")(REFRESH_TOKEN_KEY)
            }
        } catch (e: Exception) {
            // Ignorer les erreurs
            println("❌ Erreur lors de la suppression des tokens: ${e.message}")
        }
    }
    
    override suspend fun hasTokens(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }
}

/**
 * Factory function pour créer AuthDataStore sur Web.
 */
@Composable
actual fun createAuthDataStore(): AuthDataStore {
    return JsAuthDataStore()
}

