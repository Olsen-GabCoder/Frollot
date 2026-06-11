package com.frollot.mobile.preferences

import androidx.compose.runtime.Composable

/**
 * Implémentation JavaScript/Web de UserPreferencesStore utilisant localStorage.
 */
class JsUserPreferencesStore : UserPreferencesStore {
    
    private val DARK_MODE_KEY = "frollot_dark_mode"
    
    override suspend fun getDarkMode(): Boolean? {
        return try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                val value = js("window.localStorage.getItem")(DARK_MODE_KEY) as? String
                value?.toBooleanStrictOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun setDarkMode(enabled: Boolean) {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.setItem")(DARK_MODE_KEY, enabled.toString())
            }
        } catch (e: Exception) {
            // Ignorer les erreurs de localStorage (mode privé, quota, etc.)
        }
    }
    
    override suspend fun clear() {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.removeItem")(DARK_MODE_KEY)
            }
        } catch (e: Exception) {
            // Ignorer les erreurs
        }
    }
}

@Composable
actual fun createUserPreferencesStore(): UserPreferencesStore {
    return JsUserPreferencesStore()
}

