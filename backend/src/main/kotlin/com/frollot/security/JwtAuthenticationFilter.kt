package com.frollot.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.frollot.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtre interceptant chaque requête HTTP pour valider le token JWT.
 *
 * Workflow optimisé (Phase 2.1):
 * 1. Extrait le token du header "Authorization: Bearer <token>"
 * 2. Valide le token avec JwtTokenProvider
 * 3. Si valide, crée l'utilisateur depuis les claims du token (évite la requête BDD)
 * 4. Vérifie isActive depuis le token (si false, ne charge pas depuis BDD)
 * 5. Place l'utilisateur dans le SecurityContext de Spring Security
 * 6. Laisse passer la requête aux controllers
 *
 * OPTIMISATION : Utilise les claims du token au lieu de charger depuis la BDD.
 * Cela réduit drastiquement le nombre de requêtes à la base de données.
 * 
 * NOTE : Les informations du token peuvent être obsolètes (isActive/isVerified).
 * Pour des vérifications critiques, les controllers doivent vérifier en BDD si nécessaire.
 *
 * Ordre: 1 (s'exécute après RateLimitFilter mais avant UsernamePasswordAuthenticationFilter)
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter(), Ordered {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun getOrder(): Int = 1

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // 1. Extraire le token JWT du header Authorization
            val jwt = extractJwtFromRequest(request)

            // 2. Si token présent
            if (jwt != null) {
                // 2.1. Valider le token
                if (!jwtTokenProvider.validateToken(jwt)) {
                    // Token invalide (expiré, malformé, etc.)
                    logger.warn("Token JWT invalide pour la requête: {}", request.requestURI)
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT invalide ou expiré")
                    return
                }

                // 2.2. Vérifier isActive depuis le token (évite la requête BDD si inactif)
                val isActive = jwtTokenProvider.isUserActiveFromToken(jwt)
                
                if (!isActive) {
                    // Utilisateur inactif selon le token
                    logger.warn("Tentative d'authentification avec un compte inactif")
                    sendErrorResponse(response, HttpStatus.FORBIDDEN, "Compte désactivé")
                    return
                }

                // 2.3. Validation de cohérence (Phase 2.2)
                // Vérifier que les claims essentiels sont présents et cohérents
                val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                val email = jwtTokenProvider.getEmailFromToken(jwt)
                val userType = jwtTokenProvider.getUserTypeFromToken(jwt)
                
                if (userId.isNullOrBlank() || email.isNullOrBlank() || userType.isNullOrBlank()) {
                    logger.error("Claims essentiels manquants ou invalides dans le token JWT")
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT invalide: claims manquants")
                    return
                }

                // 2.4. Créer l'utilisateur depuis les claims du token (OPTIMISATION)
                // Cela évite une requête en base de données à chaque requête HTTP
                val user = jwtTokenProvider.getUserFromToken(jwt)

                // 2.5. Vérifier emailVerified depuis le token (vérification email obligatoire)
                val isEmailVerified = jwtTokenProvider.isUserEmailVerifiedFromToken(jwt)
                if (!isEmailVerified) {
                    logger.debug("Utilisateur avec email non vérifié: {}", user.email)
                }

                // 2.6. Vérifier isVerified depuis le token (vérification professionnelle optionnelle)
                // Note: isVerified n'empêche pas l'authentification, mais peut être
                // utilisé par les controllers pour restreindre certaines actions premium
                val isVerified = jwtTokenProvider.isUserVerifiedFromToken(jwt)
                if (!isVerified) {
                    logger.debug("Utilisateur non vérifié professionnellement: {}", user.email)
                }

                // 2.6. Créer l'objet Authentication avec les rôles
                val authorities = listOf(
                    SimpleGrantedAuthority("ROLE_${user.userType.name.uppercase()}")
                )

                val authentication = UsernamePasswordAuthenticationToken(
                    user,          // Principal (objet User créé depuis les claims)
                    null,          // Credentials (pas besoin, déjà authentifié)
                    authorities    // Authorities (rôles)
                )

                authentication.details = WebAuthenticationDetailsSource()
                    .buildDetails(request)

                // 2.7. Placer l'utilisateur dans le SecurityContext
                SecurityContextHolder.getContext().authentication = authentication

                logger.debug("Utilisateur authentifié: {} ({}, emailVerified: {}, isVerified: {})", user.email, user.userType, isEmailVerified, isVerified)
            }
            // Si pas de token, laisser passer (endpoints publics)
        } catch (e: Exception) {
            logger.error("Erreur lors de l'authentification JWT", e)
            // En cas d'erreur inattendue, ne pas bloquer la requête mais ne pas authentifier
            // Spring Security se chargera de rejeter les requêtes non authentifiées vers les endpoints protégés
        }

        // 3. Continuer la chaîne de filtres
        filterChain.doFilter(request, response)
    }

    /**
     * Envoie une réponse d'erreur JSON au client.
     * 
     * @param response La réponse HTTP
     * @param status Le code de statut HTTP
     * @param message Le message d'erreur
     */
    private fun sendErrorResponse(
        response: HttpServletResponse,
        status: HttpStatus,
        message: String
    ) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        val errorResponse = mapOf(
            "error" to status.reasonPhrase,
            "message" to message,
            "status" to status.value()
        )
        
        try {
            objectMapper.writeValue(response.writer, errorResponse)
            response.writer.flush()
        } catch (e: Exception) {
            logger.error("Erreur lors de l'écriture de la réponse d'erreur", e)
        }
    }

    /**
     * Extrait le token JWT du header "Authorization".
     *
     * Format attendu: "Authorization: Bearer <token>"
     *
     * @return Le token JWT sans le préfixe "Bearer ", ou null si absent/invalide
     */
    private fun extractJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7) // Enlever "Bearer "
        } else {
            null
        }
    }

    /**
     * Compare les claims du token avec les données en base de données.
     * 
     * Cette méthode est optionnelle et peut être utilisée pour une sécurité renforcée.
     * Elle vérifie que les informations critiques (isActive, isVerified) sont cohérentes
     * entre le token et la base de données.
     * 
     * NOTE : Cette méthode effectue une requête BDD, donc elle ne doit pas être appelée
     * à chaque requête. Elle peut être utilisée :
     * - Pour des endpoints critiques
     * - Périodiquement (par exemple, toutes les 100 requêtes)
     * - Lors d'actions sensibles
     * 
     * @param token Le token JWT valide
     * @return true si les claims sont cohérents avec la BDD, false sinon
     */
    fun validateTokenConsistency(token: String): Boolean {
        return try {
            val userId = jwtTokenProvider.getUserIdFromToken(token)
            val dbUser = userRepository.findById(userId).orElse(null)
            
            if (dbUser == null) {
                logger.warn("Utilisateur non trouvé en BDD lors de la validation de cohérence - userId: {}", userId)
                return false
            }

            // Vérifier la cohérence des champs critiques
            val tokenIsActive = jwtTokenProvider.isUserActiveFromToken(token)
            val tokenIsVerified = jwtTokenProvider.isUserVerifiedFromToken(token)
            val tokenEmailVerified = jwtTokenProvider.isUserEmailVerifiedFromToken(token)

            val isConsistent = dbUser.isActive == tokenIsActive &&
                              dbUser.isVerified == tokenIsVerified &&
                              dbUser.emailVerified == tokenEmailVerified

            if (!isConsistent) {
                logger.warn(
                    "Incohérence détectée entre token et BDD - userId: {}, BDD(isActive={}, isVerified={}, emailVerified={}) vs Token(isActive={}, isVerified={}, emailVerified={})",
                    userId, dbUser.isActive, dbUser.isVerified, dbUser.emailVerified, tokenIsActive, tokenIsVerified, tokenEmailVerified
                )
            }

            isConsistent
        } catch (e: Exception) {
            logger.error("Erreur lors de la validation de cohérence du token", e)
            false
        }
    }
}