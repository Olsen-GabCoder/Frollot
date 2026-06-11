package com.frollot.flyway

import org.flywaydb.core.api.callback.BaseCallback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.slf4j.LoggerFactory

/**
 * Callback Flyway spécifique au développement.
 * Plus permissif que la version production.
 *
 * Phase 4.3 - Durcissement Flyway et règles d'évolution du schéma
 */
class FlywayDevCallback : BaseCallback() {

    private val logger = LoggerFactory.getLogger(FlywayDevCallback::class.java)

    override fun handle(event: Event, context: Context) {
        when (event) {
            Event.BEFORE_MIGRATE -> {
                logger.info("🔄 [Flyway DEV] Migration en cours...")
            }

            Event.AFTER_MIGRATE -> {
                logger.info("✅ [Flyway DEV] Migration réussie")
            }

            Event.AFTER_MIGRATE_ERROR -> {
                logger.error("❌ [Flyway DEV] Migration échouée - vérifiez les logs détaillés")
                logger.error("💡 [Flyway DEV] En développement, vous pouvez :")
                logger.error("   ├── Corriger la migration et relancer")
                logger.error("   ├── Utiliser flyway repair si checksum modifié")
                logger.error("   ├── Supprimer manuellement la migration en base pour test")
            }

            else -> {
                // Pas de log pour les autres événements en dev
            }
        }
    }
}