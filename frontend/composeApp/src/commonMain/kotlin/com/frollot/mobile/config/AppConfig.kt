package com.frollot.mobile.config

/**
 * Configuration de l'application selon la plateforme et l'environnement.
 * 
 * Utilise le pattern expect/actual pour fournir des valeurs spécifiques à chaque plateforme.
 * 
 * Environnements supportés :
 * - dev: Développement local
 * - staging: Environnement de test/recette
 * - prod: Production
 */
expect object AppConfig {
    /**
     * URL de base de l'API backend.
     * 
     * Android (dev): http://10.0.2.2:9090/api (pour l'émulateur Android)
     * Android (staging): https://staging-api.frollot.com/api
     * Android (prod): https://api.frollot.com/api
     * Web: Configurable via variable d'environnement ou défaut
     */
    val baseUrl: String
}

/**
 * Utilitaire de logging conditionnel.
 * 
 * Permet de désactiver les logs en production pour :
 * - Améliorer les performances
 * - Éviter les fuites d'informations sensibles
 * - Réduire la taille des logs
 */
object FrollotLogger {
    
    /**
     * Active ou désactive globalement les logs.
     * À configurer selon l'environnement au démarrage de l'app.
     */
    var isEnabled: Boolean = true
    
    /**
     * Log de debug - uniquement en développement.
     */
    fun debug(tag: String, message: String) {
        if (isEnabled) {
            println("🔍 [$tag] $message")
        }
    }
    
    /**
     * Log d'information.
     */
    fun info(tag: String, message: String) {
        if (isEnabled) {
            println("ℹ️ [$tag] $message")
        }
    }
    
    /**
     * Log de succès.
     */
    fun success(tag: String, message: String) {
        if (isEnabled) {
            println("✅ [$tag] $message")
        }
    }
    
    /**
     * Log d'avertissement - toujours affiché.
     */
    fun warning(tag: String, message: String) {
        println("⚠️ [$tag] $message")
    }
    
    /**
     * Log d'erreur - toujours affiché.
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        println("❌ [$tag] $message")
        throwable?.printStackTrace()
    }
    
    /**
     * Log réseau (tokens, requêtes) - JAMAIS en production.
     * Ces logs peuvent contenir des informations sensibles.
     */
    fun network(tag: String, message: String) {
        if (isEnabled) {
            println("🌐 [$tag] $message")
        }
    }
    
    /**
     * Log sécurité (tokens) - version masquée.
     */
    fun secureToken(tag: String, tokenName: String, token: String?) {
        if (isEnabled && token != null) {
            val masked = if (token.length > 10) "${token.take(6)}...${token.takeLast(4)}" else "***"
            println("🔒 [$tag] $tokenName: $masked")
        }
    }
}
