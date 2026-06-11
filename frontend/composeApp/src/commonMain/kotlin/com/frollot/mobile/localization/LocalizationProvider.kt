package com.frollot.mobile.localization

import androidx.compose.runtime.*
import androidx.compose.ui.unit.LayoutDirection
import com.frollot.mobile.localization.resources.StringsBundle
import com.frollot.mobile.config.FrollotLogger


/**
 * CompositionLocal pour la langue courante.
 * 
 * Expose la langue actuellement sélectionnée dans le tree Compose.
 */
val LocalLanguage = compositionLocalOf<String> {
    error("LocalLanguage not provided")
}

/**
 * CompositionLocal pour le bundle de strings.
 * 
 * Expose le bundle de strings correspondant à la langue courante.
 */
val LocalStrings = compositionLocalOf<StringsBundle> {
    error("LocalStrings not provided")
}

/**
 * CompositionLocal pour la direction de layout (LTR/RTL).
 * 
 * Expose la direction de layout selon la langue (RTL pour l'arabe).
 * 
 * Conforme à l'ADR-001 - DÉCISION 10 : Support RTL.
 */
val LocalLayoutDirection = compositionLocalOf<LayoutDirection> {
    error("LocalLayoutDirection not provided")
}

/**
 * Provider de localisation global pour l'application.
 * 
 * Ce provider :
 * - Charge le bundle de strings pour la langue spécifiée
 * - Expose la langue, les strings et la direction de layout via CompositionLocal
 * - Gère le caching des bundles pour performance
 * - Applique automatiquement la direction RTL pour les langues concernées
 * 
 * Conforme à l'ADR-001 - DÉCISIONS 8, 9, 10.
 * 
 * @param language Code de la langue (ex: "fr", "en", "ar")
 * @param content Contenu Compose à envelopper
 */
@Composable
fun LocalizationProvider(
    language: String,
    content: @Composable () -> Unit
) {
    FrollotLogger.warning("Debug", "🟡 [DEBUG] LocalizationProvider appelé avec language: $language")
    // Déterminer la langue supportée (avec fallback)
    // Utiliser language directement comme clé pour garantir la mise à jour
    val supportedLanguage = remember(language) {
        val lang = SupportedLanguage.fromCode(language)
        FrollotLogger.warning("Debug", "🟡 [DEBUG] supportedLanguage déterminé: ${lang.code}")
        lang
    }
    
    // Charger le bundle de strings (avec cache via StringsLoader)
    // Utiliser language directement comme clé pour forcer le rechargement
    val stringsBundle = remember(language) {
        val bundle = StringsLoader.load(supportedLanguage.code)
        FrollotLogger.warning("Debug", "🟡 [DEBUG] stringsBundle chargé pour ${supportedLanguage.code}, taille: ${bundle.size()}")
        bundle
    }
    
    // Déterminer la direction de layout (RTL pour l'arabe)
    // Utiliser language directement comme clé pour garantir la mise à jour
    val layoutDirection = remember(language) {
        if (supportedLanguage.isRTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }
    
    // Fournir les valeurs via CompositionLocal
    CompositionLocalProvider(
        LocalLanguage provides supportedLanguage.code,
        LocalStrings provides stringsBundle,
        LocalLayoutDirection provides layoutDirection
    ) {
        content()
    }
}

/**
 * Helper composable pour récupérer une string localisée.
 * 
 * Cette fonction :
 * - Récupère le bundle de strings depuis CompositionLocal
 * - Utilise `remember` pour éviter les recalculs lors des recompositions
 * - Retourne la string correspondant à la clé, ou la clé elle-même si non trouvée
 * 
 * Conforme à l'ADR-001 - DÉCISION 9 : Performance avec remember.
 * 
 * @param key Clé type-safe de la string à récupérer
 * @return String localisée, ou la clé entre crochets si non trouvée
 * 
 * Exemple d'utilisation :
 * ```
 * Text(stringResource(Strings.Login.Title))
 * ```
 */
@Composable
fun stringResource(key: StringKey): String {
    val strings = LocalStrings.current
    val language = LocalLanguage.current
    
    // Utiliser remember avec language et key pour forcer la mise à jour
    // quand la langue change. Le language change quand on sélectionne une nouvelle langue.
    return remember(language, key.key) {
        val result = strings.get(key)
        // Log seulement pour quelques clés importantes pour éviter le spam
        if (key.key.contains("login.title") || key.key.contains("settings.title")) {
            FrollotLogger.debug("Localization", "🟣 [DEBUG] stringResource(${key.key}) avec language=$language -> $result")
        }
        result
    }
}

