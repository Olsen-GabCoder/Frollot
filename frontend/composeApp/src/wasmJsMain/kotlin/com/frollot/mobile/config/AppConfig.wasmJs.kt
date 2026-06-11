package com.frollot.mobile.config

/**
 * Configuration WASM JS de l'application.
 * Utilise une URL par défaut pour le développement web.
 */
actual object AppConfig {
    actual val baseUrl: String = "http://localhost:9090/api"
}