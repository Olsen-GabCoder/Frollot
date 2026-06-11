package com.frollot.mobile.localization

/**
 * Implémentation JavaScript/Web de SystemLanguageDetector.
 * 
 * Utilise navigator.language pour détecter la langue du navigateur.
 */
class JsSystemLanguageDetector : SystemLanguageDetector {
    override fun detectSystemLanguage(): String? {
        return try {
            if (js("typeof navigator !== 'undefined' && navigator.language")) {
                val language = js("navigator.language") as String
                language.take(2).lowercase()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

actual fun createSystemLanguageDetector(): SystemLanguageDetector {
    return JsSystemLanguageDetector()
}

