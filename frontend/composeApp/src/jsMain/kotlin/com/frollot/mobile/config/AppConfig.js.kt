package com.frollot.mobile.config

import kotlinx.browser.window

/**
 * Configuration Web (JS/Wasm).
 * 
 * Lit l'URL de l'API depuis :
 * 1. Une variable globale JavaScript (window.FROLLOT_API_URL)
 * 2. Ou utilise localhost par défaut pour le développement
 * 
 * Pour la production, définir la variable avant le chargement de l'app :
 * <script>window.FROLLOT_API_URL = "https://api.frollot.com/api";</script>
 */
actual object AppConfig {
    actual val baseUrl: String = getApiUrl()
    
    /**
     * Environnement actuel détecté depuis le hostname.
     */
    val environment: String = detectEnvironment()
    
    /**
     * Active les logs uniquement en développement.
     */
    val enableLogging: Boolean = environment != "prod"
    
    /**
     * Vérifie si l'application est en mode production.
     */
    val isProduction: Boolean = environment == "prod"
}

/**
 * Récupère l'URL de l'API depuis la configuration JavaScript ou utilise le défaut.
 */
private fun getApiUrl(): String {
    return try {
        // Essayer de lire depuis une variable globale JavaScript
        val configUrl = js("window.FROLLOT_API_URL") as? String
        if (!configUrl.isNullOrBlank()) {
            configUrl
        } else {
            getDefaultApiUrl()
        }
    } catch (e: Exception) {
        getDefaultApiUrl()
    }
}

/**
 * Détermine l'URL par défaut selon le hostname.
 */
private fun getDefaultApiUrl(): String {
    return try {
        val hostname = window.location.hostname
        when {
            hostname == "localhost" || hostname == "127.0.0.1" -> "http://localhost:9090/api"
            hostname.contains("staging") -> "https://staging-api.frollot.com/api"
            hostname.contains("frollot.com") -> "https://api.frollot.com/api"
            else -> "http://localhost:9090/api"
        }
    } catch (e: Exception) {
        "http://localhost:9090/api"
    }
}

/**
 * Détecte l'environnement depuis le hostname.
 */
private fun detectEnvironment(): String {
    return try {
        val hostname = window.location.hostname
        when {
            hostname == "localhost" || hostname == "127.0.0.1" -> "dev"
            hostname.contains("staging") -> "staging"
            hostname.contains("frollot.com") -> "prod"
            else -> "dev"
        }
    } catch (e: Exception) {
        "dev"
    }
}
