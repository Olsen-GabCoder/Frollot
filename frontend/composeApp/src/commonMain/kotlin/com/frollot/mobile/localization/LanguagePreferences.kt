package com.frollot.mobile.localization

import androidx.compose.runtime.Composable

/**
 * Factory pour créer une instance de LanguagePreferences selon la plateforme.
 * 
 * Conforme à l'ADR-001 - DÉCISION 2 : Persistance locale de la langue.
 * 
 * L'implémentation plateforme-spécifique est fournie via expect/actual.
 * 
 * NOTE : Sur Android, cette fonction nécessite un Context et doit être appelée
 * depuis un composable pour accéder à LocalContext.
 */
@Composable
expect fun createLanguagePreferences(): LanguagePreferences

/**
 * Interface pour la persistance de la préférence de langue.
 * 
 * Cette interface abstrait le stockage de la langue préférée de l'utilisateur,
 * permettant différentes implémentations selon la plateforme (Android DataStore,
 * Web localStorage, etc.).
 */
interface LanguagePreferences {
    /**
     * Récupère la langue préférée de l'utilisateur.
     * 
     * @return Code de la langue (ex: "fr", "en"), ou null si aucune préférence n'est stockée
     */
    suspend fun getLanguage(): String?
    
    /**
     * Sauvegarde la langue préférée de l'utilisateur.
     * 
     * @param languageCode Code de la langue à sauvegarder (ex: "fr", "en")
     */
    suspend fun setLanguage(languageCode: String)
    
    /**
     * Supprime la préférence de langue stockée.
     */
    suspend fun clear()
}

