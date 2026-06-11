package com.frollot.mobile.auth

import androidx.compose.runtime.Composable
import kotlinx.browser.localStorage

/**
 * Implémentation WebAssembly JS de AuthDataStore utilisant localStorage.
 */
class WasmJsAuthDataStore : AuthDataStore {

    companion object {
        private const val ACCESS_TOKEN_KEY = "frollot_access_token"
        private const val REFRESH_TOKEN_KEY = "frollot_refresh_token"
    }

    override suspend fun getAccessToken(): String? {
        return try {
            localStorage.getItem(ACCESS_TOKEN_KEY)
        } catch (e: Exception) {
            println("⚠️ AuthDataStore WASM: Erreur lors de la récupération du token d'accès: ${e.message}")
            null
        }
    }

    override suspend fun getRefreshToken(): String? {
        return try {
            localStorage.getItem(REFRESH_TOKEN_KEY)
        } catch (e: Exception) {
            println("⚠️ AuthDataStore WASM: Erreur lors de la récupération du refresh token: ${e.message}")
            null
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        try {
            localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
            localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
            println("✅ AuthDataStore WASM: Tokens sauvegardés")
        } catch (e: Exception) {
            println("⚠️ AuthDataStore WASM: Erreur lors de la sauvegarde des tokens: ${e.message}")
        }
    }

    override suspend fun clearTokens() {
        try {
            localStorage.removeItem(ACCESS_TOKEN_KEY)
            localStorage.removeItem(REFRESH_TOKEN_KEY)
            println("✅ AuthDataStore WASM: Tokens supprimés")
        } catch (e: Exception) {
            println("⚠️ AuthDataStore WASM: Erreur lors de la suppression des tokens: ${e.message}")
        }
    }

    override suspend fun hasTokens(): Boolean {
        return try {
            getAccessToken() != null && getRefreshToken() != null
        } catch (e: Exception) {
            println("⚠️ AuthDataStore WASM: Erreur lors de la vérification des tokens: ${e.message}")
            false
        }
    }
}

/**
 * Factory function pour créer AuthDataStore sur WebAssembly.
 */
@Composable
actual fun createAuthDataStore(): AuthDataStore {
    return WasmJsAuthDataStore()
}