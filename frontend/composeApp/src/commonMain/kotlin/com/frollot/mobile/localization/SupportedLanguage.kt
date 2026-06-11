package com.frollot.mobile.localization

/**
 * Énumération des langues supportées par l'application.
 * 
 * Chaque langue contient :
 * - code : Code ISO 639-1 (ex: "fr", "en", "ar")
 * - displayName : Nom d'affichage dans la langue elle-même
 * - isRTL : Indique si la langue s'écrit de droite à gauche
 * 
 * Conforme à l'ADR-001 - DÉCISION 3 et DÉCISION 10.
 */
enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val isRTL: Boolean = false
) {
    FRENCH("fr", "Français"),
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    GERMAN("de", "Deutsch"),
    ARABIC("ar", "العربية", isRTL = true);
    
    companion object {
        /**
         * Retourne la langue correspondant au code, ou FRENCH par défaut.
         * 
         * Conforme à l'ADR-001 - DÉCISION 3 : Français comme langue par défaut.
         */
        fun fromCode(code: String): SupportedLanguage {
            return values().find { it.code == code } ?: FRENCH
        }
        
        /**
         * Retourne la langue par défaut (français).
         */
        fun default(): SupportedLanguage = FRENCH
    }
}

