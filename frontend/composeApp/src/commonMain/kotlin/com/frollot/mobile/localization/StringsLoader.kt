package com.frollot.mobile.localization

import com.frollot.mobile.localization.resources.StringsBundle
import com.frollot.mobile.localization.resources.createFrenchStrings
import com.frollot.mobile.localization.resources.createEnglishStrings
import com.frollot.mobile.localization.resources.createSpanishStrings
import com.frollot.mobile.localization.resources.createGermanStrings
import com.frollot.mobile.localization.resources.createArabicStrings

/**
 * Chargeur de bundles de strings selon la langue.
 * 
 * Cette classe centralise le chargement des bundles de strings
 * et garantit qu'un bundle est toujours retourné (fallback vers français).
 * 
 * Conforme à l'ADR-001 - DÉCISION 3 : Français comme langue par défaut.
 */
object StringsLoader {
    /**
     * Cache des bundles chargés pour éviter les rechargements.
     * 
     * Conforme à l'ADR-001 - DÉCISION 9 : Performance et caching.
     */
    private val cache = mutableMapOf<String, StringsBundle>()
    
    /**
     * Charge le bundle de strings pour la langue spécifiée.
     * 
     * Si la langue n'est pas supportée ou si le bundle n'existe pas,
     * retourne le bundle français (langue de référence).
     * 
     * @param languageCode Code de la langue (ex: "fr", "en", "ar")
     * @return Bundle de strings pour la langue, ou bundle français en fallback
     */
    fun load(languageCode: String): StringsBundle {
        // Vérifier le cache d'abord
        cache[languageCode]?.let { 
            // Si le bundle en cache est vide, fallback vers français
            if (it.size() == 0 && languageCode != SupportedLanguage.FRENCH.code) {
                return load(SupportedLanguage.FRENCH.code)
            }
            return it 
        }
        
        // Charger le bundle selon la langue
        val bundle = when (languageCode) {
            SupportedLanguage.FRENCH.code -> createFrenchStrings()
            SupportedLanguage.ENGLISH.code -> createEnglishStrings()
            SupportedLanguage.SPANISH.code -> createSpanishStrings()
            SupportedLanguage.GERMAN.code -> createGermanStrings()
            SupportedLanguage.ARABIC.code -> createArabicStrings()
            else -> {
                // Fallback vers français si langue non supportée
                createFrenchStrings()
            }
        }
        
        // Si le bundle est vide (pas encore traduit), fallback vers français
        if (bundle.size() == 0 && languageCode != SupportedLanguage.FRENCH.code) {
            return load(SupportedLanguage.FRENCH.code)
        }
        
        // Mettre en cache
        cache[languageCode] = bundle
        
        return bundle
    }
    
    /**
     * Vide le cache (utile pour les tests ou rechargement forcé).
     */
    fun clearCache() {
        cache.clear()
    }
}

