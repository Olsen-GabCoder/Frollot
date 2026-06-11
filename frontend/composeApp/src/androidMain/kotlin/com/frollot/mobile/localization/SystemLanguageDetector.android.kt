package com.frollot.mobile.localization

import java.util.Locale

/**
 * Implémentation Android de SystemLanguageDetector.
 * 
 * Utilise Locale.getDefault() pour détecter la langue du système.
 */
class AndroidSystemLanguageDetector : SystemLanguageDetector {
    override fun detectSystemLanguage(): String? {
        return try {
            Locale.getDefault().language.take(2)
        } catch (e: Exception) {
            null
        }
    }
}

actual fun createSystemLanguageDetector(): SystemLanguageDetector {
    return AndroidSystemLanguageDetector()
}

