package com.frollot.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Filtre de rate limiting pour protéger contre les attaques brute force.
 *
 * Limites configurées :
 * - /api/users/login : 5 tentatives / minute, blocage progressif après échecs
 * - /api/users/register : 3 tentatives / minute
 * - /api/auth/refresh : 10 tentatives / minute
 * - /api/payments/webhook : 50 / minute (Stripe peut envoyer des rafales)
 * - Autres endpoints : 100 requêtes / minute
 *
 * Utilise l'algorithme Token Bucket (Bucket4j).
 * 
 * Fonctionnalités de sécurité :
 * - Blocage progressif après échecs de login
 * - Logging des tentatives suspectes
 * - Protection contre le password spraying
 * 
 * Configuration :
 * - Peut être désactivé via app.security.rate-limit.enabled=false (utile pour les tests)
 */
@Component
class RateLimitFilter(
    @org.springframework.beans.factory.annotation.Value("\${app.security.rate-limit.enabled:true}")
    private val rateLimitEnabled: Boolean = true
) : OncePerRequestFilter(), Ordered {

    private val logger = LoggerFactory.getLogger(RateLimitFilter::class.java)

    override fun getOrder(): Int = 0 // Exécuter en premier, avant tous les autres filtres

    // Cache des buckets par IP
    private val buckets = ConcurrentHashMap<String, Bucket>()
    
    // Tracking des échecs de login pour blocage progressif
    private val loginFailures = ConcurrentHashMap<String, LoginFailureRecord>()

    // Configuration des limites par endpoint
    private val endpointLimits = mapOf(
        "/api/users/login" to Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))),
        "/api/users/register" to Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))),
        "/api/auth/refresh" to Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))),
        "/api/payments/webhook" to Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1)))
    )

    // Limite par défaut pour les autres endpoints
    private val defaultLimit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))
    
    /**
     * Record pour suivre les échecs de login par IP
     */
    data class LoginFailureRecord(
        var failureCount: Int = 0,
        var lastFailureTime: Instant = Instant.now(),
        var blockedUntil: Instant? = null
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Si le rate limiting est désactivé (tests), laisser passer toutes les requêtes
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response)
            return
        }
        
        val requestPath = request.requestURI
        val clientIp = getClientIpAddress(request)

        // Ignorer les endpoints de santé et Swagger
        if (shouldSkipRateLimit(requestPath)) {
            filterChain.doFilter(request, response)
            return
        }
        
        // Vérifier le blocage progressif pour login
        if (requestPath == "/api/users/login" && request.method == "POST") {
            val record = loginFailures[clientIp]
            if (record != null && record.blockedUntil != null && Instant.now().isBefore(record.blockedUntil)) {
                val remainingSeconds = Duration.between(Instant.now(), record.blockedUntil).seconds
                logger.warn("🛑 IP bloquée pour brute force: $clientIp (${record.failureCount} échecs)")
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json"
                response.writer.write(
                    """
                    {
                        "error": "Too Many Failed Attempts",
                        "message": "Account temporarily locked due to too many failed login attempts. Please try again later.",
                        "retryAfter": $remainingSeconds
                    }
                    """.trimIndent()
                )
                return
            }
        }

        // Déterminer la limite pour cet endpoint
        val limit = endpointLimits[requestPath] ?: defaultLimit

        // Récupérer ou créer le bucket pour cette IP + endpoint
        val bucketKey = "$clientIp:$requestPath"
        val bucket = buckets.computeIfAbsent(bucketKey) {
            Bucket.builder()
                .addLimit(limit)
                .build()
        }

        // Vérifier si la requête peut être traitée
        if (bucket.tryConsume(1)) {
            // Token disponible, laisser passer
            filterChain.doFilter(request, response)
        } else {
            // Limite dépassée, retourner 429 Too Many Requests
            logger.warn("⚠️ Rate limit atteint pour IP: $clientIp sur $requestPath")
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write(
                """
                {
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded. Please try again later.",
                    "retryAfter": 60
                }
                """.trimIndent()
            )
        }
    }
    
    /**
     * Enregistre un échec de login pour le blocage progressif.
     * À appeler depuis le service d'authentification après un échec.
     */
    fun recordLoginFailure(clientIp: String) {
        val record = loginFailures.computeIfAbsent(clientIp) { LoginFailureRecord() }
        record.failureCount++
        record.lastFailureTime = Instant.now()
        
        // Blocage progressif basé sur le nombre d'échecs
        record.blockedUntil = when {
            record.failureCount >= 10 -> Instant.now().plus(Duration.ofHours(1))    // 1 heure après 10 échecs
            record.failureCount >= 7 -> Instant.now().plus(Duration.ofMinutes(15))   // 15 min après 7 échecs
            record.failureCount >= 5 -> Instant.now().plus(Duration.ofMinutes(5))    // 5 min après 5 échecs
            record.failureCount >= 3 -> Instant.now().plus(Duration.ofMinutes(1))    // 1 min après 3 échecs
            else -> null
        }
        
        if (record.blockedUntil != null) {
            logger.warn("🚨 IP bloquée temporairement: $clientIp après ${record.failureCount} échecs")
        }
    }
    
    /**
     * Réinitialise le compteur d'échecs après un login réussi.
     */
    fun clearLoginFailures(clientIp: String) {
        loginFailures.remove(clientIp)
    }

    /**
     * Extrait l'adresse IP réelle du client.
     * Prend en compte les proxies et load balancers.
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        // Vérifier les headers de proxy
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            // Prendre la première IP (client original)
            return xForwardedFor.split(",").first().trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }

        // Fallback sur l'IP directe
        return request.remoteAddr ?: "unknown"
    }

    /**
     * Détermine si le rate limiting doit être ignoré pour cet endpoint.
     */
    private fun shouldSkipRateLimit(path: String): Boolean {
        return path.startsWith("/manage/health") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/actuator")
    }
}

