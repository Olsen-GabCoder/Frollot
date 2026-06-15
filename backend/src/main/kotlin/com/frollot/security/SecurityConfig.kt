package com.frollot.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler

/**
 * Configuration de la sécurité de l'application.
 *
 * Définit:
 * - Quels endpoints sont publics (login, register) et lesquels nécessitent authentification
 * - L'ordre des filtres de sécurité (JWT filter avant les autres)
 * - La politique de session (stateless, sans cookies)
 * - Les paramètres CORS
 * - L'encodeur de mots de passe (BCrypt)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter,
    private val environment: Environment
) {

    /**
     * Configuration de la chaîne de filtres de sécurité.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Désactiver CSRF (pas nécessaire pour API REST stateless)
            .csrf { it.disable() }

            // Configuration CORS
            .cors { it.configurationSource(corsConfigurationSource()) }

            // Headers de sécurité HTTP
            .headers { headers ->
                headers
                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:;")
                    }
                    .httpStrictTransportSecurity { hsts ->
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000) // 1 an
                    }
                    .frameOptions { it.deny() }
                    .xssProtection { }
                    .contentTypeOptions { }
                    .referrerPolicy { it.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN) }
            }

            // Politique de session STATELESS (pas de session HTTP, tout via JWT)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // Gestion des erreurs d'authentification/autorisation
            .exceptionHandling { exceptions ->
                // 401 Unauthorized pour les requêtes non authentifiées
                exceptions.authenticationEntryPoint(authenticationEntryPoint())
                // 403 Forbidden pour les requêtes authentifiées mais non autorisées
                exceptions.accessDeniedHandler(accessDeniedHandler())
            }

            // Configuration des autorisations
            .authorizeHttpRequests { auth ->
                auth
                    // ========== ENDPOINTS PUBLICS (sans authentification) ==========

                    // Authentification
                    .requestMatchers("/api/users/login", "/api/users/login/2fa", "/api/users/register", "/api/users/refresh", "/api/users/complete-registration", "/api/users/forgot-password", "/api/users/reset-password").permitAll()
                    
                    // Webhook Stripe (doit être public mais protégé par signature)
                    .requestMatchers("/api/payments/webhook").permitAll()

                    // 🔧 DEV ONLY - Utilitaires de hashage (BLOQUÉ EN PRODUCTION)
                    .requestMatchers("/api/dev/**").let { matcher ->
                        if (isDevProfile()) {
                            matcher.permitAll()
                        } else {
                            matcher.denyAll()
                        }
                    }

                    // Documentation API (Swagger)
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                    // Health checks
                    .requestMatchers("/manage/health/**").permitAll()

                    // Fichiers statiques (uploads)
                    .requestMatchers("/uploads/**").permitAll()

                    // Lecture publique des salons (marketplace)
                    .requestMatchers(HttpMethod.GET, "/api/salons", "/api/salons/**").permitAll()

                    // Feed social public
                    .requestMatchers(HttpMethod.GET, "/api/social/feed").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/social/posts/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/social/users/*/posts").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/social/posts/*/comments").permitAll()

                    // Lecture publique des services et staff
                    .requestMatchers(HttpMethod.GET, "/api/salons/*/services/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/salons/*/staff/**").permitAll()

                    // ========== ENDPOINTS PROTÉGÉS (authentification requise) ==========

                    // Gestion des utilisateurs
                    .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()

                    // Création/modification de salons
                    .requestMatchers(HttpMethod.POST, "/api/salons").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/salons/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/salons/**").authenticated()

                    // Gestion des services (propriétaires seulement - vérifié dans le service)
                    .requestMatchers(HttpMethod.POST, "/api/salons/*/services/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/salons/*/services/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/salons/*/services/**").authenticated()

                    // Gestion du staff (propriétaires seulement - vérifié dans le service)
                    .requestMatchers(HttpMethod.POST, "/api/salons/*/staff/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/salons/*/staff/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/salons/*/staff/**").authenticated()

                    // Réservations - créneaux disponibles PUBLIC, reste authentifié
                    .requestMatchers(HttpMethod.POST, "/api/salons/*/available-slots").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bookings/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/bookings/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").authenticated()
                    .requestMatchers("/api/clients/*/bookings/**").authenticated()
                    .requestMatchers("/api/staff/*/bookings/**").authenticated()

                    // 🔒 FILE D'ATTENTE
                    // Lecture du statut: PUBLIC
                    .requestMatchers(HttpMethod.GET, "/api/salons/*/queue").permitAll()

                    // Actions sur la file d'attente: AUTHENTIFIÉ
                    .requestMatchers(HttpMethod.POST, "/api/salons/*/queue/join").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/salons/*/queue/leave").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/salons/*/queue/call-next").authenticated()

                    // Bloquer tout autre pattern sous /queue par sécurité
                    .requestMatchers("/api/salons/*/queue/**").authenticated()

                    // Réseau social (création/modification)
                    .requestMatchers(HttpMethod.POST, "/api/social/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/social/**").authenticated()

                    // Upload de médias
                    .requestMatchers("/api/media/**").authenticated()

                    // Avis clients
                    .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/salons/*/reviews/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/bookings/*/review/exists").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/clients/*/reviews").authenticated()

                    // Par défaut, tous les autres endpoints nécessitent authentification
                    .anyRequest().authenticated()
            }

            // Ajouter le filtre JWT avant le filtre d'authentification standard
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            // Ajouter le filtre de rate limiting AVANT le filtre JWT (en utilisant l'instance)
            .addFilterBefore(
                rateLimitFilter,
                jwtAuthenticationFilter.javaClass
            )

        return http.build()
    }

    /**
     * Configuration CORS pour autoriser les requêtes depuis le frontend.
     * Whitelist stricte des origines autorisées.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Origines autorisées (whitelist stricte)
        val allowedOrigins = if (isDevProfile()) {
            // En développement : localhost autorisé
            listOf(
                "http://localhost:3000",  // React/Next.js local
                "http://localhost:8081",  // Expo dev server
                "http://localhost:8082",  // Expo dev server (alt)
                "http://localhost:9090",  // Frontend Compose local
                "http://127.0.0.1:8081", // Expo dev server
                "http://127.0.0.1:9090",
                "http://10.0.2.2:9090"   // Émulateur Android
            )
        } else {
            // En production : uniquement les origines de production
            listOf(
                "https://app.frollot.com",
                "https://staging.frollot.com"
            )
        }
        configuration.allowedOrigins = allowedOrigins

        // Méthodes HTTP autorisées
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

        // Headers autorisés (whitelist stricte au lieu de "*")
        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Refresh-Token" // S8 : identification de la session courante (GET/DELETE /me/sessions)
        )

        // Headers exposés au frontend
        configuration.exposedHeaders = listOf("Authorization", "Content-Type", "X-User-Id")

        // Autoriser les credentials (cookies, headers d'auth)
        configuration.allowCredentials = true

        // Durée de cache de la config CORS (1h)
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    /**
     * Vérifie si le profil "dev" est actif.
     */
    private fun isDevProfile(): Boolean {
        val activeProfiles = environment.activeProfiles
        return activeProfiles.isEmpty() || activeProfiles.contains("dev")
    }

    /**
     * Bean pour encoder/vérifier les mots de passe avec BCrypt.
     *
     * BCrypt utilise un salt aléatoire et un coût de 10 rounds par défaut.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Bean AuthenticationManager pour Spring Security.
     * Nécessaire pour certaines opérations d'authentification.
     */
    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    /**
     * Point d'entrée pour les erreurs d'authentification (401 Unauthorized).
     * Retourne une réponse JSON au lieu de rediriger vers une page de login.
     */
    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { _, response, authException ->
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            response.writer.write(
                """
                {
                    "error": "Unauthorized",
                    "message": "Authentication required",
                    "status": 401
                }
                """.trimIndent()
            )
        }
    }

    /**
     * Handler pour les erreurs d'accès refusé (403 Forbidden).
     * Pour les utilisateurs authentifiés mais sans les permissions nécessaires.
     */
    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler {
        return AccessDeniedHandler { _, response, _ ->
            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            response.writer.write(
                """
                {
                    "error": "Forbidden",
                    "message": "Access denied",
                    "status": 403
                }
                """.trimIndent()
            )
        }
    }
}