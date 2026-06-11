package com.frollot.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * Configuration du monitoring et métriques personnalisées.
 * Métriques essentielles pour la production.
 *
 * Phase 5.4 - Préparation à la production
 */
@Configuration
class MonitoringConfig {

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config()
                .commonTags(
                    "application", "frollot-backend",
                    "version", "1.0.0",
                    "environment", System.getProperty("spring.profiles.active", "dev")
                )
        }
    }

    /**
     * Métriques métier personnalisées
     */
    @Bean
    fun businessMetricsRegistry(registry: MeterRegistry): BusinessMetrics {
        return BusinessMetrics(registry)
    }
}

class BusinessMetrics(private val registry: MeterRegistry) {

    // Compteurs d'authentification
    private val loginAttempts = registry.counter("frollot.auth.login.attempts")
    private val loginSuccess = registry.counter("frollot.auth.login.success")
    private val loginFailures = registry.counter("frollot.auth.login.failures")

    // Compteurs de mots de passe oubliés
    private val passwordResetRequests = registry.counter("frollot.auth.password.reset.requests")
    private val passwordResetSuccess = registry.counter("frollot.auth.password.reset.success")
    private val passwordResetFailures = registry.counter("frollot.auth.password.reset.failures")

    // Métriques de réservations
    private val bookingsCreated = registry.counter("frollot.bookings.created")
    private val bookingsCancelled = registry.counter("frollot.bookings.cancelled")
    private val bookingsCompleted = registry.counter("frollot.bookings.completed")

    // Métriques de performance DB
    private val dbConnectionsActive = AtomicLong(0)
    private val dbQueryDuration = registry.timer("frollot.db.query.duration")

    // Métriques utilisateur
    private val activeUsers = registry.gauge("frollot.users.active", AtomicLong(0))
    private val totalUsers = registry.gauge("frollot.users.total", AtomicLong(0))

    // Métriques de sécurité
    private val rateLimitExceeded = registry.counter("frollot.security.rate_limit.exceeded")
    private val suspiciousActivities = registry.counter("frollot.security.suspicious_activities")

    init {
        // Enregistrer les jauges
        registry.gauge("frollot.db.connections.active", dbConnectionsActive)
        registry.gauge("frollot.startup.time", LocalDateTime.now()) {
            System.currentTimeMillis().toDouble()
        }
    }

    // Méthodes pour mettre à jour les métriques
    fun recordLoginAttempt() = loginAttempts.increment()
    fun recordLoginSuccess() = loginSuccess.increment()
    fun recordLoginFailure() = loginFailures.increment()

    fun recordPasswordResetRequest() = passwordResetRequests.increment()
    fun recordPasswordResetSuccess() = passwordResetSuccess.increment()
    fun recordPasswordResetFailure() = passwordResetFailures.increment()

    fun recordBookingCreated() = bookingsCreated.increment()
    fun recordBookingCancelled() = bookingsCancelled.increment()
    fun recordBookingCompleted() = bookingsCompleted.increment()

    fun updateActiveDbConnections(count: Long) = dbConnectionsActive.set(count)
    fun recordDbQueryDuration(durationMs: Long) = dbQueryDuration.record(java.time.Duration.ofMillis(durationMs))

    fun updateActiveUsers(count: Long) = activeUsers?.set(count)
    fun updateTotalUsers(count: Long) = totalUsers?.set(count)

    fun recordRateLimitExceeded() = rateLimitExceeded.increment()
    fun recordSuspiciousActivity() = suspiciousActivities.increment()
}