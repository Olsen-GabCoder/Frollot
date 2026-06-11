package com.frollot.mobile.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.frollot.mobile.model.PostResponse
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Phase I.4 - Mode Offline Basique
 * Implémentation Android utilisant DataStore Preferences
 */

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites_cache")

actual class FavoritesDataStore(private val context: Context) {
    private val dataStore = context.dataStore
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private fun favoritesKey(userId: String) = stringPreferencesKey("favorites_$userId")
    
    actual suspend fun saveFavorites(userId: String, favorites: List<PostResponse>) {
        try {
            dataStore.edit { preferences ->
                val jsonString = json.encodeToString(favorites)
                preferences[favoritesKey(userId)] = jsonString
            }
        } catch (e: Exception) {
            println("❌ Erreur lors de la sauvegarde des favoris: ${e.message}")
        }
    }
    
    actual suspend fun loadFavorites(userId: String): List<PostResponse>? {
        return try {
            val jsonString = dataStore.data.first()[favoritesKey(userId)] ?: return null
            json.decodeFromString<List<PostResponse>>(jsonString)
        } catch (e: Exception) {
            println("❌ Erreur lors du chargement des favoris: ${e.message}")
            null
        }
    }
    
    actual suspend fun clearFavorites(userId: String) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(favoritesKey(userId))
            }
        } catch (e: Exception) {
            println("❌ Erreur lors de la suppression des favoris: ${e.message}")
        }
    }
    
    actual suspend fun hasCachedFavorites(userId: String): Boolean {
        return try {
            dataStore.data.first().contains(favoritesKey(userId))
        } catch (e: Exception) {
            false
        }
    }
}

actual fun createFavoritesDataStore(): FavoritesDataStore {
    // Cette fonction nécessite un Context, on la gère via rememberFavoritesDataStore
    throw UnsupportedOperationException("Utilisez rememberFavoritesDataStore() dans les composables")
}

/**
 * Helper function pour créer FavoritesDataStore depuis un composable Android.
 */
@Composable
actual fun rememberFavoritesDataStore(): FavoritesDataStore {
    val context = LocalContext.current
    return remember(context) {
        FavoritesDataStore(context)
    }
}

