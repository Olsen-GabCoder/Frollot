package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.frollot.mobile.model.PostResponse

/**
 * Phase I.4 - Mode Offline Basique
 * Implémentation par défaut pour wasmJs (non supporté pour l'instant)
 */
actual class FavoritesDataStore {
    actual suspend fun saveFavorites(userId: String, favorites: List<PostResponse>) {
        // Non supporté sur wasmJs pour l'instant
    }
    
    actual suspend fun loadFavorites(userId: String): List<PostResponse>? {
        return null
    }
    
    actual suspend fun clearFavorites(userId: String) {
        // Non supporté sur wasmJs pour l'instant
    }
    
    actual suspend fun hasCachedFavorites(userId: String): Boolean {
        return false
    }
}

actual fun createFavoritesDataStore(): FavoritesDataStore {
    return FavoritesDataStore()
}

/**
 * Helper function pour créer FavoritesDataStore depuis un composable wasmJs.
 */
@Composable
actual fun rememberFavoritesDataStore(): FavoritesDataStore {
    return remember {
        FavoritesDataStore()
    }
}

