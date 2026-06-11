package com.frollot.mobile.config

import com.frollot.mobile.BuildConfig

/**
 * Configuration Android avec Build Flavors.
 * 
 * Les valeurs sont définies dans build.gradle.kts selon le flavor actif :
 * - dev: http://10.0.2.2:9090/api (émulateur Android)
 * - staging: https://staging-api.frollot.com/api
 * - prod: https://api.frollot.com/api
 */
actual object AppConfig {
    /**
     * URL de base de l'API backend.
     * Définie par le BuildConfig selon le flavor sélectionné.
     */
    actual val baseUrl: String = BuildConfig.API_BASE_URL
    
    /**
     * Environnement actuel (dev, staging, prod).
     */
    val environment: String = BuildConfig.ENVIRONMENT
    
    /**
     * Active ou désactive les logs de debug.
     * Désactivé en production pour des raisons de sécurité et performance.
     */
    val enableLogging: Boolean = BuildConfig.ENABLE_LOGGING
    
    /**
     * Vérifie si l'application est en mode production.
     */
    val isProduction: Boolean = environment == "prod"
    
    /**
     * Vérifie si l'application est en mode développement.
     */
    val isDevelopment: Boolean = environment == "dev"
}
