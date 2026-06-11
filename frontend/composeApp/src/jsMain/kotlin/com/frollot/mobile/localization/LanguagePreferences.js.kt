package com.frollot.mobile.localization

import androidx.compose.runtime.Composable

/**
 * Implémentation JavaScript/Web de LanguagePreferences utilisant localStorage.
 * 
 * Conforme à l'ADR-001 - DÉCISION 2 : Persistance locale via localStorage pour Web.
 */
class JsLanguagePreferences : LanguagePreferences {
    
    private val STORAGE_KEY = "frollot_preferred_language"
    
    override suspend fun getLanguage(): String? {
        return try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.getItem")(STORAGE_KEY) as? String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun setLanguage(languageCode: String) {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.setItem")(STORAGE_KEY, languageCode)
            }
        } catch (e: Exception) {
            // Ignorer les erreurs de localStorage (mode privé, quota, etc.)
        }
    }
    
    override suspend fun clear() {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.removeItem")(STORAGE_KEY)
            }
        } catch (e: Exception) {
            // Ignorer les erreurs
        }
    }
}

@Composable
actual fun createLanguagePreferences(): LanguagePreferences {
    return JsLanguagePreferences()
}

