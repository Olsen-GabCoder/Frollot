package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.frollot.mobile.model.PostResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Phase I.4 - Mode Offline Basique
 * Implémentation JavaScript utilisant localStorage
 */
actual class FavoritesDataStore {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private fun storageKey(userId: String) = "favorites_cache_$userId"
    
    actual suspend fun saveFavorites(userId: String, favorites: List<PostResponse>) {
        try {
            val jsonString = json.encodeToString(favorites)
            // Utiliser localStorage de manière synchrone (acceptable pour de petites données)
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.setItem")(storageKey(userId), jsonString)
            }
        } catch (e: Exception) {
            println("❌ Erreur lors de la sauvegarde des favoris: ${e.message}")
        }
    }
    
    actual suspend fun loadFavorites(userId: String): List<PostResponse>? {
        return try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                val jsonString = js("window.localStorage.getItem")(storageKey(userId)) as? String
                jsonString?.let { json.decodeFromString<List<PostResponse>>(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ Erreur lors du chargement des favoris: ${e.message}")
            null
        }
    }
    
    actual suspend fun clearFavorites(userId: String) {
        try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                js("window.localStorage.removeItem")(storageKey(userId))
            }
        } catch (e: Exception) {
            println("❌ Erreur lors de la suppression des favoris: ${e.message}")
        }
    }
    
    actual suspend fun hasCachedFavorites(userId: String): Boolean {
        return try {
            if (js("typeof window !== 'undefined' && window.localStorage")) {
                val jsonString = js("window.localStorage.getItem")(storageKey(userId)) as? String
                jsonString != null
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

actual fun createFavoritesDataStore(): FavoritesDataStore {
    return FavoritesDataStore()
}

/**
 * Helper function pour créer FavoritesDataStore depuis un composable JavaScript.
 */
@Composable
actual fun rememberFavoritesDataStore(): FavoritesDataStore {
    return remember {
        FavoritesDataStore()
    }
}

