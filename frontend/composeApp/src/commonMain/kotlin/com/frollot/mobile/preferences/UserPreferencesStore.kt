package com.frollot.mobile.preferences

import androidx.compose.runtime.Composable

/**
 * Factory pour créer une instance de UserPreferencesStore selon la plateforme.
 * 
 * Cette interface abstrait le stockage des préférences utilisateur (dark mode, etc.),
 * permettant différentes implémentations selon la plateforme :
 * - Android : DataStore Preferences
 * - Web : localStorage
 * 
 * NOTE : Sur Android, cette fonction nécessite un Context et doit être appelée
 * depuis un composable pour accéder à LocalContext.
 */
@Composable
expect fun createUserPreferencesStore(): UserPreferencesStore

/**
 * Interface pour la persistance des préférences utilisateur.
 * 
 * Gère les préférences utilisateur non liées à l'authentification :
 * - Dark mode (thème sombre)
 * - Autres préférences futures (taille de police, etc.)
 */
interface UserPreferencesStore {
    /**
     * Récupère la préférence de dark mode.
     * 
     * @return true si le dark mode est activé, false sinon, null si jamais défini
     */
    suspend fun getDarkMode(): Boolean?
    
    /**
     * Sauvegarde la préférence de dark mode.
     * 
     * @param enabled true pour activer le dark mode, false pour le désactiver
     */
    suspend fun setDarkMode(enabled: Boolean)
    
    /**
     * Supprime toutes les préférences utilisateur stockées.
     */
    suspend fun clear()
}

