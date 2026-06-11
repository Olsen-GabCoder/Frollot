package com.frollot.mobile.localization

import androidx.compose.runtime.Composable
import kotlinx.browser.localStorage

/**
 * Implémentation WebAssembly JS de LanguagePreferences utilisant localStorage.
 */
class WasmJsLanguagePreferences : LanguagePreferences {

    companion object {
        private const val LANGUAGE_KEY = "frollot_language"
    }

    override suspend fun getLanguage(): String? {
        return try {
            localStorage.getItem(LANGUAGE_KEY)
        } catch (e: Exception) {
            println("⚠️ LanguagePreferences WASM: Erreur lors de la récupération de la langue: ${e.message}")
            null
        }
    }

    override suspend fun setLanguage(languageCode: String) {
        try {
            localStorage.setItem(LANGUAGE_KEY, languageCode)
            println("✅ LanguagePreferences WASM: Langue sauvegardée: $languageCode")
        } catch (e: Exception) {
            println("⚠️ LanguagePreferences WASM: Erreur lors de la sauvegarde de la langue: ${e.message}")
        }
    }

    override suspend fun clear() {
        try {
            localStorage.removeItem(LANGUAGE_KEY)
            println("✅ LanguagePreferences WASM: Langue supprimée")
        } catch (e: Exception) {
            println("⚠️ LanguagePreferences WASM: Erreur lors de la suppression de la langue: ${e.message}")
        }
    }
}

@Composable
actual fun createLanguagePreferences(): LanguagePreferences {
    return WasmJsLanguagePreferences()
}