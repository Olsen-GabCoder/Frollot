package com.frollot.mobile.preferences

import androidx.compose.runtime.Composable
import kotlinx.browser.localStorage

/**
 * Implémentation WebAssembly JS de UserPreferencesStore utilisant localStorage.
 */
class WasmJsUserPreferencesStore : UserPreferencesStore {

    companion object {
        private const val DARK_MODE_KEY = "frollot_dark_mode"
    }

    override suspend fun getDarkMode(): Boolean? {
        return try {
            val value = localStorage.getItem(DARK_MODE_KEY)
            value?.toBoolean()
        } catch (e: Exception) {
            println("⚠️ UserPreferencesStore WASM: Erreur lors de la récupération du mode sombre: ${e.message}")
            null
        }
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        try {
            localStorage.setItem(DARK_MODE_KEY, enabled.toString())
            println("✅ UserPreferencesStore WASM: Mode sombre sauvegardé: $enabled")
        } catch (e: Exception) {
            println("⚠️ UserPreferencesStore WASM: Erreur lors de la sauvegarde du mode sombre: ${e.message}")
        }
    }

    override suspend fun clear() {
        try {
            localStorage.removeItem(DARK_MODE_KEY)
            println("✅ UserPreferencesStore WASM: Préférences supprimées")
        } catch (e: Exception) {
            println("⚠️ UserPreferencesStore WASM: Erreur lors de la suppression des préférences: ${e.message}")
        }
    }
}

@Composable
actual fun createUserPreferencesStore(): UserPreferencesStore {
    return WasmJsUserPreferencesStore()
}