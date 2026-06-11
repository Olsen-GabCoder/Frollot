package com.frollot.config

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

/**
 * Health checks personnalisés pour la production.
 * Vérifications avancées de l'état des services critiques.
 *
 * Phase 5.3 - Préparation à la production
 */

// Health check base de données avancé
@Component
class DatabaseHealthIndicator(
    private val jdbcTemplate: JdbcTemplate
) : HealthIndicator {

    override fun health(): Health {
        return try {
            // Vérification connexion
            jdbcTemplate.execute("SELECT 1")

            // Vérification tables critiques
            val criticalTables = listOf("users", "salons", "bookings", "flyway_schema_history")
            val existingTables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String::class.java
            )

            val missingTables = criticalTables.filter { !existingTables.contains(it) }

            if (missingTables.isEmpty()) {
                Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("schema", "complet")
                    .withDetail("tables_critical", criticalTables.size)
                    .build()
            } else {
                Health.down()
                    .withDetail("error", "Tables manquantes: ${missingTables.joinToString()}")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message ?: "Connexion échouée")
                .withDetail("type", "MySQL")
                .build()
        }
    }
}

// Health check email avancé
@Component
class EmailHealthIndicator(
    private val mailSender: JavaMailSender,
    private val environment: org.springframework.core.env.Environment
) : HealthIndicator {

    override fun health(): Health {
        val emailEnabled = environment.getProperty("app.email.enabled", "false").toBoolean()

        return if (!emailEnabled) {
            Health.unknown()
                .withDetail("status", "désactivé")
                .withDetail("reason", "Configuration email désactivée")
                .build()
        } else {
            try {
                // Test basique de connexion SMTP (sans envoyer d'email)
                val session = (mailSender as org.springframework.mail.javamail.JavaMailSenderImpl).session
                val transport = session.transport

                // Tentative de connexion (timeout court)
                transport.connect()
                transport.close()

                Health.up()
                    .withDetail("smtp_host", environment.getProperty("spring.mail.host"))
                    .withDetail("smtp_port", environment.getProperty("spring.mail.port"))
                    .withDetail("connection", "OK")
                    .build()
            } catch (e: Exception) {
                Health.down()
                    .withDetail("error", "Connexion SMTP échouée")
                    .withDetail("details", e.message ?: "Unknown error")
                    .build()
            }
        }
    }
}

// Health check sécurité
@Component
class SecurityHealthIndicator(
    private val environment: org.springframework.core.env.Environment
) : HealthIndicator {

    override fun health(): Health {
        val issues = mutableListOf<String>()

        // Vérification JWT
        val jwtSecret = environment.getProperty("app.security.jwt.secret", "")
        if (jwtSecret.length < 32) {
            issues.add("JWT secret trop court")
        }

        // Vérification CORS
        val corsOrigins = environment.getProperty("cors.allowed-origins", "")
        if (corsOrigins.contains("*")) {
            issues.add("CORS trop permissif")
        }

        // Vérification profil
        val profiles = environment.activeProfiles
        val isProduction = profiles.contains("prod")
        val isDev = profiles.isEmpty() || profiles.contains("dev")

        return if (issues.isEmpty()) {
            Health.up()
                .withDetail("jwt_config", "OK")
                .withDetail("cors_config", "OK")
                .withDetail("profile", if (isProduction) "production" else "development")
                .build()
        } else {
            val status = if (isProduction && issues.any { it.contains("secret") }) Health.down() else Health.up()
            status
                .withDetail("issues", issues)
                .withDetail("severity", if (isProduction) "high" else "medium")
                .build()
        }
    }
}

// Health check Flyway
@Component
class FlywayHealthIndicator(
    private val dataSource: javax.sql.DataSource
) : HealthIndicator {

    override fun health(): Health {
        return try {
            val connection = dataSource.connection
            val resultSet = connection.createStatement().executeQuery(
                "SELECT COUNT(*) as migration_count, " +
                "SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as successful_migrations, " +
                "SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) as failed_migrations " +
                "FROM flyway_schema_history"
            )

            if (resultSet.next()) {
                val total = resultSet.getInt("migration_count")
                val successful = resultSet.getInt("successful_migrations")
                val failed = resultSet.getInt("failed_migrations")

                connection.close()

                if (failed == 0) {
                    Health.up()
                        .withDetail("migrations_total", total)
                        .withDetail("migrations_successful", successful)
                        .withDetail("schema_status", "OK")
                        .build()
                } else {
                    Health.down()
                        .withDetail("error", "$failed migration(s) échouée(s)")
                        .withDetail("migrations_total", total)
                        .withDetail("migrations_successful", successful)
                        .build()
                }
            } else {
                connection.close()
                Health.unknown()
                    .withDetail("status", "Table flyway_schema_history introuvable")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", "Vérification Flyway échouée: ${e.message}")
                .build()
        }
    }
}