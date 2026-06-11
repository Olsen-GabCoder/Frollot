package com.frollot.mobile.localization

import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.config.FrollotLogger


/**
 * Gestionnaire central de la langue de l'application.
 * 
 * Cette classe orchestre :
 * - La détection de la langue système
 * - La récupération de la langue stockée (locale)
 * - La synchronisation avec le backend (si utilisateur connecté)
 * - La détermination de la langue initiale
 * - La logique de fallback
 * 
 * Conforme à l'ADR-001 - DÉCISION 2, 4 : Double persistance et comportement au premier lancement.
 */
class LanguageManager(
    private val languagePreferences: LanguagePreferences,
    private val systemLanguageDetector: SystemLanguageDetector,
    private val api: FrollotApi? = null // Optionnel : pour synchronisation backend
) {
    /**
     * Détermine la langue initiale à utiliser au démarrage de l'application.
     * 
     * Ordre de priorité (conforme ADR-001 DÉCISION 2) :
     * 1. Langue du backend (si utilisateur connecté)
     * 2. Langue stockée localement (si existe)
     * 3. Langue du système (si supportée)
     * 4. Français (langue par défaut)
     * 
     * Conforme à l'ADR-001 - DÉCISION 4 : Détection automatique sans dialogue.
     * 
     * @param userPreferredLanguage Langue préférée de l'utilisateur depuis le backend (optionnel)
     * @return Code de la langue à utiliser (ex: "fr", "en")
     */
    suspend fun getInitialLanguage(userPreferredLanguage: String? = null): String {
        // 1. Utiliser la langue du backend si utilisateur connecté (priorité la plus haute)
        userPreferredLanguage?.let { backendLanguage ->
            val supported = SupportedLanguage.fromCode(backendLanguage)
            if (supported.code == backendLanguage) {
                // Synchroniser avec le stockage local
                languagePreferences.setLanguage(backendLanguage)
                return backendLanguage
            }
        }
        
        // 2. Vérifier les préférences stockées localement
        languagePreferences.getLanguage()?.let { storedLanguage ->
            // Valider que la langue stockée est toujours supportée
            val supported = SupportedLanguage.fromCode(storedLanguage)
            if (supported.code == storedLanguage) {
                return storedLanguage
            }
        }
        
        // 3. Détecter la langue du système
        systemLanguageDetector.detectSystemLanguage()?.let { systemLanguage ->
            val supported = SupportedLanguage.fromCode(systemLanguage)
            // Si la langue système est supportée, l'utiliser
            if (supported.code == systemLanguage) {
                return systemLanguage
            }
        }
        
        // 4. Fallback vers français (langue par défaut)
        return SupportedLanguage.default().code
    }
    
    /**
     * Change la langue de l'application et la sauvegarde (locale + backend si connecté).
     * 
     * Conforme à l'ADR-001 - DÉCISION 2 : Double persistance.
     * 
     * @param languageCode Code de la langue à utiliser
     * @param syncToBackend Si true, synchronise avec le backend (défaut: true si API disponible)
     */
    suspend fun setLanguage(languageCode: String, syncToBackend: Boolean = true) {
        // Valider que la langue est supportée
        val supported = SupportedLanguage.fromCode(languageCode)
        val validatedCode = supported.code
        
        // Sauvegarder localement (toujours)
        languagePreferences.setLanguage(validatedCode)
        
        // Synchroniser avec le backend si connecté et demandé
        if (syncToBackend && api != null && api.isAuthenticated()) {
            try {
                api.updateCurrentUserLanguage(validatedCode)
            } catch (e: Exception) {
                // En cas d'erreur backend, on continue avec le stockage local
                // La langue locale est déjà sauvegardée
                FrollotLogger.warning("API", "⚠️ Erreur lors de la synchronisation de la langue avec le backend: ${e.message}")
            }
        }
    }
    
    /**
     * Récupère la langue actuellement stockée localement.
     * 
     * @return Code de la langue stockée, ou null si aucune préférence
     */
    suspend fun getStoredLanguage(): String? {
        return languagePreferences.getLanguage()
    }
    
    /**
     * Récupère la langue depuis le backend (si utilisateur connecté).
     * 
     * @return Code de la langue depuis le backend, ou null si erreur ou non connecté
     */
    suspend fun getBackendLanguage(): String? {
        return if (api != null && api.isAuthenticated()) {
            try {
                api.getCurrentUserLanguage()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}

