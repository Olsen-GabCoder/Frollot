package com.frollot.mobile.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implémentation Android de AuthDataStore utilisant DataStore.
 * 
 * Les tokens sont stockés de manière persistante dans DataStore.
 * 
 * NOTE SÉCURITÉ :
 * Pour une sécurité maximale en production, il faudrait utiliser EncryptedDataStore
 * ou EncryptedSharedPreferences pour chiffrer les tokens au repos.
 * Pour l'instant, nous utilisons DataStore standard. Le chiffrement sera ajouté
 * dans une phase ultérieure si nécessaire.
 * 
 * Sécurité actuelle :
 * - Les tokens sont stockés dans le répertoire privé de l'application
 * - Accessibles uniquement par l'application elle-même
 * - Non chiffrés au repos (à améliorer en production)
 */
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

class AndroidAuthDataStore(
    private val context: Context
) : AuthDataStore {
    
    override suspend fun getAccessToken(): String? {
        return context.authDataStore.data
            .map { preferences -> preferences[ACCESS_TOKEN_KEY] }
            .first()
    }
    
    override suspend fun getRefreshToken(): String? {
        return context.authDataStore.data
            .map { preferences -> preferences[REFRESH_TOKEN_KEY] }
            .first()
    }
    
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }
    
    override suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
    
    override suspend fun hasTokens(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }
}

/**
 * Factory function pour créer AuthDataStore sur Android.
 * 
 * Nécessite un Context, donc doit être appelée depuis un composable.
 */
@Composable
actual fun createAuthDataStore(): AuthDataStore {
    val context = LocalContext.current
    return remember(context) {
        AndroidAuthDataStore(context)
    }
}

