package com.frollot.mobile.localization

/**
 * Implémentation WebAssembly JS de SystemLanguageDetector.
 *
 * NOTE: Implémentation temporaire - navigator n'est pas encore supporté en WASM.
 * Cette implémentation retourne une valeur par défaut pour permettre la compilation.
 *
 * TODO: Implémenter avec des bindings JavaScript externes appropriés pour WASM.
 */
class WasmJsSystemLanguageDetector : SystemLanguageDetector {
    override fun detectSystemLanguage(): String? {
        // TODO: Implémenter avec navigator via bindings JS externes
        println("⚠️ SystemLanguageDetector WASM: detectSystemLanguage non implémenté - retourne 'en'")
        return "en" // Valeur par défaut
    }
}

actual fun createSystemLanguageDetector(): SystemLanguageDetector {
    return WasmJsSystemLanguageDetector()
}