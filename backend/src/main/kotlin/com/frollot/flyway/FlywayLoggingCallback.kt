package com.frollot.flyway

import org.flywaydb.core.api.callback.BaseCallback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.slf4j.LoggerFactory

/**
 * Callback Flyway pour logging avancé et validation de sécurité.
 *
 * Phase 4.3 - Durcissement Flyway et règles d'évolution du schéma
 */
class FlywayLoggingCallback : BaseCallback() {

    private val logger = LoggerFactory.getLogger(FlywayLoggingCallback::class.java)

    override fun handle(event: Event, context: Context) {
        when (event) {
            Event.BEFORE_MIGRATE -> {
                logger.info("🔄 [Flyway] DÉBUT DE MIGRATION")
                validateMigrationSecurity(context)
            }

            Event.AFTER_MIGRATE -> {
                logger.info("✅ [Flyway] MIGRATION RÉUSSIE")
            }

            Event.AFTER_MIGRATE_ERROR -> {
                logger.error("❌ [Flyway] ERREUR DE MIGRATION - Intervention manuelle requise")
            }

            Event.AFTER_VALIDATE -> {
                logger.info("✅ [Flyway] VALIDATION TERMINÉE")
            }

            else -> {
                // Pas de log pour les autres événements
            }
        }
    }

    private fun validateMigrationSecurity(context: Context) {
        val configuration = context.configuration

        // Validation de sécurité : baseline-on-migrate ne devrait pas être activé en production
        val activeProfiles = System.getProperty("spring.profiles.active", "")
        val isProduction = activeProfiles.contains("prod", ignoreCase = true)

        if (isProduction && configuration.isBaselineOnMigrate) {
            logger.error("🚨 SÉCURITÉ VIOLÉE: baseline-on-migrate activé en PRODUCTION!")
            throw IllegalStateException("Baseline on migrate interdit en production")
        }

        logger.info("🔒 Validation de sécurité passée")
    }
}