package com.frollot.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Validateur des variables d'environnement au démarrage.
 * 
 * Vérifie que toutes les configurations critiques sont correctement définies
 * et alerte en cas de configuration manquante ou incorrecte.
 * 
 * En production, bloque le démarrage si les configurations critiques sont manquantes.
 * En développement, affiche des avertissements mais laisse l'application démarrer.
 */
@Component
class EnvironmentValidator(
    @Value("\${spring.profiles.active:dev}")
    private val activeProfiles: String,
    
    @Value("\${app.security.jwt.secret}")
    private val jwtSecret: String,
    
    @Value("\${spring.datasource.url}")
    private val dbUrl: String,
    
    @Value("\${spring.datasource.username}")
    private val dbUsername: String,
    
    @Value("\${spring.datasource.password}")
    private val dbPassword: String,
    
    @Value("\${stripe.secret-key:}")
    private val stripeSecretKey: String,
    
    @Value("\${stripe.webhook-secret:}")
    private val stripeWebhookSecret: String,
    
    @Value("\${app.email.enabled:false}")
    private val emailEnabled: Boolean,
    
    @Value("\${spring.mail.username:}")
    private val smtpUsername: String,
    
    @Value("\${spring.mail.password:}")
    private val smtpPassword: String,
    
    @Value("\${firebase.enabled:false}")
    private val firebaseEnabled: Boolean,
    
    @Value("\${firebase.service-account-path:}")
    private val firebaseServiceAccountPath: String
) {
    
    private val logger = LoggerFactory.getLogger(EnvironmentValidator::class.java)
    
    private val isProduction: Boolean
        get() = activeProfiles.contains("prod", ignoreCase = true)
    
    @EventListener(ApplicationReadyEvent::class)
    fun validateEnvironmentOnStartup() {
        logger.info("╔════════════════════════════════════════════════════════════╗")
        logger.info("║              🔒 VALIDATION DE L'ENVIRONNEMENT               ║")
        logger.info("╚════════════════════════════════════════════════════════════╝")
        logger.info("Profil actif: $activeProfiles")
        
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // ========================================
        // 1. JWT Secret
        // ========================================
        when {
            jwtSecret.length < 32 -> {
                issues.add("JWT_SECRET trop court (${jwtSecret.length} caractères, minimum 32)")
            }
            jwtSecret == "your-jwt-secret-key-change-in-production" -> {
                if (isProduction) {
                    issues.add("JWT_SECRET utilise la valeur par défaut en PRODUCTION !")
                } else {
                    warnings.add("JWT_SECRET utilise la valeur par défaut (OK en développement)")
                }
            }
            else -> {
                logger.info("✅ JWT_SECRET: Configuré correctement (${jwtSecret.length} caractères)")
            }
        }
        
        // ========================================
        // 2. Base de données
        // ========================================
        if (dbUrl.isBlank()) {
            issues.add("DB_URL n'est pas configurée")
        } else if (dbPassword == "changeme" && isProduction) {
            issues.add("DB_PASSWORD utilise la valeur par défaut en PRODUCTION !")
        } else {
            logger.info("✅ Base de données: Configurée ($dbUsername@${dbUrl.substringBefore("?")})")
        }
        
        // ========================================
        // 3. Stripe (Paiements)
        // ========================================
        when {
            stripeSecretKey.isBlank() || stripeSecretKey.contains("your_stripe") -> {
                warnings.add("Stripe non configuré - les paiements ne fonctionneront pas")
            }
            stripeSecretKey.startsWith("sk_test_") -> {
                if (isProduction) {
                    issues.add("Stripe utilise des clés de TEST en PRODUCTION !")
                } else {
                    logger.info("✅ Stripe: Configuré (mode test)")
                }
            }
            stripeSecretKey.startsWith("sk_live_") -> {
                logger.info("✅ Stripe: Configuré (mode production)")
            }
        }
        
        if (stripeWebhookSecret.isBlank() || stripeWebhookSecret.contains("your_webhook")) {
            warnings.add("Stripe webhook secret non configuré - les webhooks seront rejetés")
        }
        
        // ========================================
        // 4. Email
        // ========================================
        if (emailEnabled) {
            if (smtpUsername.isBlank() || smtpPassword.isBlank()) {
                issues.add("Email activé mais SMTP_USERNAME/SMTP_PASSWORD non configurés")
            } else {
                logger.info("✅ Email: Configuré ($smtpUsername)")
            }
        } else {
            warnings.add("Email désactivé - les notifications par email ne fonctionneront pas")
        }
        
        // ========================================
        // 5. Firebase (Push notifications)
        // ========================================
        if (firebaseEnabled) {
            if (firebaseServiceAccountPath.isBlank()) {
                issues.add("Firebase activé mais FIREBASE_SERVICE_ACCOUNT_PATH non configuré")
            } else {
                val file = java.io.File(firebaseServiceAccountPath)
                if (!file.exists() && !javaClass.classLoader.getResource(firebaseServiceAccountPath)?.file.isNullOrBlank().not()) {
                    issues.add("Firebase service account file introuvable: $firebaseServiceAccountPath")
                } else {
                    logger.info("✅ Firebase: Configuré")
                }
            }
        } else {
            warnings.add("Firebase désactivé - les notifications push ne fonctionneront pas")
        }
        
        // ========================================
        // Afficher les résultats
        // ========================================
        if (warnings.isNotEmpty()) {
            logger.warn("⚠️ AVERTISSEMENTS (non bloquants):")
            warnings.forEach { logger.warn("   - $it") }
        }
        
        if (issues.isNotEmpty()) {
            logger.error("❌ PROBLÈMES DE CONFIGURATION:")
            issues.forEach { logger.error("   - $it") }
            
            if (isProduction) {
                logger.error("")
                logger.error("╔════════════════════════════════════════════════════════════╗")
                logger.error("║ 🛑 L'APPLICATION NE PEUT PAS DÉMARRER EN PRODUCTION         ║")
                logger.error("║    AVEC DES CONFIGURATIONS CRITIQUES MANQUANTES !          ║")
                logger.error("╚════════════════════════════════════════════════════════════╝")
                throw IllegalStateException(
                    "Configuration invalide pour la production. Problèmes: ${issues.joinToString(", ")}"
                )
            } else {
                logger.warn("")
                logger.warn("⚠️ Ces problèmes seraient bloquants en production !")
            }
        } else {
            logger.info("")
            logger.info("╔════════════════════════════════════════════════════════════╗")
            logger.info("║ ✅ TOUTES LES CONFIGURATIONS CRITIQUES SONT VALIDÉES        ║")
            logger.info("╚════════════════════════════════════════════════════════════╝")
        }
    }
}

