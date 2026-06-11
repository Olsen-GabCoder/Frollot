package com.frollot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

/**
 * Service de configuration email robuste.
 *
 * Gère les différents modes d'envoi d'email selon l'environnement et la configuration :
 * - PRODUCTION : Envoi réel d'emails
 * - DEV_REDIRECT : Envoi sur adresse de test en développement
 * - DEV_LOG : Logging seulement (ancien comportement)
 * - DISABLED : Pas d'email du tout
 *
 * Assure la résilience en cas de configuration SMTP manquante.
 */
@Service
class EmailConfigurationService(
    private val environment: Environment,

    @Value("\${app.email.enabled:true}")
    private val emailEnabled: Boolean,

    @Value("\${app.email.dev.mode:send}")
    private val devMode: String = "send", // Forcer le mode send par défaut

    @Value("\${app.email.dev.redirect-email:test@frollot.com}")
    private val devRedirectEmail: String,

    @Value("\${app.email.smtp.fallback:true}")
    private val smtpFallback: Boolean,

    @Value("\${spring.mail.username:}")
    private val smtpUsername: String,

    @Value("\${spring.mail.password:}")
    private val smtpPassword: String
) {

    /**
     * Modes d'envoi d'email disponibles.
     */
    enum class EmailMode {
        PRODUCTION,    // Envoi réel d'emails en production
        DEV_REDIRECT,  // Envoi sur adresse de test en développement
        DEV_SEND,      // Envoi réel d'emails même en développement
        DEV_LOG,       // Logging seulement (mode debug)
        DISABLED       // Pas d'email du tout
    }

    /**
     * Détermine le mode d'envoi d'email effectif selon la configuration.
     *
     * Logique de décision :
     * 1. Si email désactivé → DISABLED
     * 2. Si en production et SMTP configuré → PRODUCTION
     * 3. Si en développement et mode redirect → DEV_REDIRECT
     * 4. Si en développement et mode send → DEV_SEND (envoi réel)
     * 5. Si en développement et mode log ou fallback SMTP → DEV_LOG
     * 6. Sinon → DISABLED (sécurisé)
     */
    fun getEffectiveEmailMode(): EmailMode {
        return when {
            // 1. Email explicitement désactivé
            !emailEnabled -> {
                println("📧 [EmailConfig] Email désactivé par configuration")
                EmailMode.DISABLED
            }

            // 2. Production avec SMTP configuré
            isProductionProfile() && isSmtpConfigured() -> {
                println("📧 [EmailConfig] Mode PRODUCTION - SMTP configuré")
                EmailMode.PRODUCTION
            }

            // 3. Développement avec mode redirect
            isDevelopmentProfile() && devMode == "redirect" -> {
                println("📧 [EmailConfig] Mode DEV_REDIRECT - Envoi sur ${devRedirectEmail}")
                EmailMode.DEV_REDIRECT
            }

            // 4. Développement avec mode send (envoi réel même en développement)
            isDevelopmentProfile() && devMode == "send" -> {
                if (isSmtpConfigured()) {
                    println("📧 [EmailConfig] Mode DEV_SEND - Envoi réel en développement")
                    EmailMode.DEV_SEND
                } else {
                    println("⚠️ [EmailConfig] Mode DEV_SEND demandé mais SMTP non configuré - Fallback DEV_LOG")
                    EmailMode.DEV_LOG
                }
            }

            // 5. Développement avec mode log ou fallback SMTP
            isDevelopmentProfile() && (devMode == "log" || (smtpFallback && !isSmtpConfigured())) -> {
                println("📧 [EmailConfig] Mode DEV_LOG - Logging seulement")
                EmailMode.DEV_LOG
            }

            // 5. Cas par défaut sécurisé
            else -> {
                println("⚠️ [EmailConfig] Configuration ambiguë - Mode DISABLED (sécurisé)")
                EmailMode.DISABLED
            }
        }
    }

    /**
     * Vérifie si la configuration SMTP est complète.
     */
    fun isSmtpConfigured(): Boolean {
        val configured = !smtpUsername.isNullOrBlank() && !smtpPassword.isNullOrBlank()
        println("📧 [EmailConfig] SMTP configuré: $configured")
        return configured
    }

    /**
     * Retourne l'adresse email de redirection pour le mode développement.
     */
    fun getDevRedirectEmail(): String {
        return devRedirectEmail
    }

    /**
     * Vérifie si on est en profil production.
     */
    private fun isProductionProfile(): Boolean {
        return environment.activeProfiles.contains("prod")
    }

    /**
     * Vérifie si on est en profil développement.
     */
    internal fun isDevelopmentProfile(): Boolean {
        val profiles = environment.activeProfiles
        return profiles.isEmpty() || profiles.contains("default") || profiles.contains("dev")
    }

    /**
     * Retourne un résumé de la configuration actuelle.
     */
    fun getConfigurationSummary(): Map<String, Any> {
        return mapOf(
            "emailEnabled" to emailEnabled,
            "devMode" to devMode,
            "devRedirectEmail" to devRedirectEmail,
            "smtpFallback" to smtpFallback,
            "smtpConfigured" to isSmtpConfigured(),
            "productionProfile" to isProductionProfile(),
            "developmentProfile" to isDevelopmentProfile(),
            "effectiveMode" to getEffectiveEmailMode()
        )
    }
}
