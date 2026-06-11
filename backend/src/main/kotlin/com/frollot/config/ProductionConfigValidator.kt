package com.frollot.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * Validateur de configuration de production.
 * Vérifie que toutes les variables sensibles sont correctement configurées.
 *
 * Phase 5.1 - Préparation à la production
 */
@Component
@Profile("prod")
class ProductionConfigValidator(
    @Value("\${app.security.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${spring.datasource.url}")
    private val dbUrl: String,

    @Value("\${spring.datasource.username}")
    private val dbUsername: String,

    @Value("\${spring.datasource.password}")
    private val dbPassword: String,

    @Value("\${stripe.secret-key}")
    private val stripeSecretKey: String,

    @Value("\${spring.profiles.active:}")
    private val activeProfiles: String
) {

    private val logger = LoggerFactory.getLogger(ProductionConfigValidator::class.java)

    @PostConstruct
    fun validateProductionConfig() {
        logger.info("🔒 [PRODUCTION VALIDATOR] Validation de la configuration de production")

        val issues = mutableListOf<String>()

        // Validation JWT Secret
        if (isDefaultJwtSecret(jwtSecret)) {
            issues.add("🚨 JWT_SECRET utilise la valeur par défaut dangereuse")
        }
        if (jwtSecret.length < 32) {
            issues.add("🚨 JWT_SECRET trop court (< 32 caractères, sécurité insuffisante)")
        }

        // Validation base de données
        if (dbUrl.contains("localhost") || dbUrl.contains("127.0.0.1")) {
            issues.add("🚨 Base de données pointe vers localhost en production")
        }
        if (dbUsername == "root" || dbUsername == "admin") {
            issues.add("🚨 Utilisateur base de données privilégié détecté")
        }
        if (dbPassword.length < 12) {
            issues.add("🚨 Mot de passe base de données trop faible (< 12 caractères)")
        }

        // Validation Stripe
        if (stripeSecretKey.contains("sk_test_")) {
            issues.add("🚨 Clé Stripe de TEST détectée en production")
        }

        // Validation profil
        if (!activeProfiles.contains("prod", ignoreCase = true)) {
            issues.add("⚠️ Profil 'prod' non détecté - certaines validations peuvent être ignorées")
        }

        // Rapport final
        if (issues.isEmpty()) {
            logger.info("✅ [PRODUCTION VALIDATOR] Configuration de production validée avec succès")
            logger.info("🔒 Sécurité: Toutes les vérifications passées")
        } else {
            logger.error("🚨 [PRODUCTION VALIDATOR] PROBLÈMES DE CONFIGURATION DÉTECTÉS:")
            issues.forEach { issue ->
                logger.error("   $issue")
            }

            if (issues.any { it.contains("🚨") }) {
                logger.error("💥 ARRÊT CRITIQUE: Problèmes de sécurité détectés - Application va s'arrêter")
                throw IllegalStateException("Configuration de production invalide: ${issues.size} problème(s) détecté(s)")
            } else {
                logger.warn("⚠️ Problèmes mineurs détectés - Application continue mais vérifiez la configuration")
            }
        }
    }

    private fun isDefaultJwtSecret(secret: String): Boolean {
        return secret == "your-jwt-secret-key-change-in-production" ||
               secret == "CHANGEME_LONG_RANDOM_SECRET_256_BITS_MINIMUM" ||
               secret.length < 32
    }
}