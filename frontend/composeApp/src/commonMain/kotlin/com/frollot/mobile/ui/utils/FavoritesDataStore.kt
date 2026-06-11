package com.frollot.mobile.ui.utils

import com.frollot.mobile.model.PostResponse

/**
 * Phase I.4 - Mode Offline Basique
 * Interface pour le stockage local des favoris
 */
expect class FavoritesDataStore {
    /**
     * Sauvegarde la liste des favoris dans le cache local.
     */
    suspend fun saveFavorites(userId: String, favorites: List<PostResponse>)
    
    /**
     * Charge la liste des favoris depuis le cache local.
     */
    suspend fun loadFavorites(userId: String): List<PostResponse>?
    
    /**
     * Vide le cache des favoris pour un utilisateur.
     */
    suspend fun clearFavorites(userId: String)
    
    /**
     * Vérifie si des favoris sont en cache pour un utilisateur.
     */
    suspend fun hasCachedFavorites(userId: String): Boolean
}

/**
 * Factory function pour créer une instance de FavoritesDataStore.
 */
expect fun createFavoritesDataStore(): FavoritesDataStore

/**
 * Helper function pour créer FavoritesDataStore depuis un composable.
 * Phase I.4 - Mode Offline Basique
 */
@androidx.compose.runtime.Composable
expect fun rememberFavoritesDataStore(): FavoritesDataStore

