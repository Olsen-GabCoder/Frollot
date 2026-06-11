package com.frollot.mobile.localization

/**
 * Factory pour créer une instance de SystemLanguageDetector selon la plateforme.
 * 
 * Conforme à l'ADR-001 - DÉCISION 4 : Détection automatique de la langue système.
 * 
 * L'implémentation plateforme-spécifique est fournie via expect/actual.
 */
expect fun createSystemLanguageDetector(): SystemLanguageDetector

/**
 * Interface pour la détection de la langue du système.
 * 
 * Cette interface abstrait la détection de la langue du système d'exploitation,
 * permettant différentes implémentations selon la plateforme.
 */
interface SystemLanguageDetector {
    /**
     * Détecte la langue du système.
     * 
     * @return Code de la langue détectée (ex: "fr", "en"), ou null si non détectable
     */
    fun detectSystemLanguage(): String?
}

