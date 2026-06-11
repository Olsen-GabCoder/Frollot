package com.frollot.security

import com.frollot.model.User
import com.frollot.model.UserType
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * Service de génération et validation des tokens JWT.
 * 
 * ## Configuration du JWT_SECRET
 * 
 * ### Exigences de sécurité (Phase 2.4)
 * - **Longueur minimale** : 32 caractères (256 bits pour HMAC-SHA256)
 * - **Complexité** : Recommandé de mélanger majuscules, minuscules, chiffres et caractères spéciaux
 * - **Production** : Le secret DOIT être configuré via variable d'environnement, jamais la valeur par défaut
 * 
 * ### Génération d'un secret sécurisé
 * ```bash
 * # Méthode recommandée (génère 32 caractères aléatoires en base64)
 * openssl rand -base64 32
 * 
 * # Alternative (génère 32 caractères hexadécimaux)
 * openssl rand -hex 32
 * ```
 * 
 * ### Configuration
 * ```bash
 * # Développement (dans .env ou application.yml)
 * JWT_SECRET=votre-secret-securise-minimum-32-caracteres
 * 
 * # Production (variable d'environnement)
 * export JWT_SECRET=$(openssl rand -base64 32)
 * ```
 * 
 * ### Validation automatique
 * - Au démarrage, le secret est validé automatiquement
 * - En production, l'application refuse de démarrer si le secret est invalide
 * - En développement, des avertissements sont affichés mais l'application démarre
 * 
 * @see SECURITY_CONFIG.md pour plus de détails
 */
@Component
class JwtTokenProvider(
    @Value("\${app.security.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${app.security.jwt.expiration-hours:24}")
    private val jwtExpirationHours: Long,

    @Value("\${app.security.jwt.expiration-minutes:120}")
    private val jwtExpirationMinutes: Long,

    @Value("\${spring.profiles.active:}")
    private val activeProfiles: String
) {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    init {
        // Valider le secret au démarrage (Phase 2.4)
        validateJwtSecret()
    }

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Valide le secret JWT au démarrage de l'application.
     * 
     * Règles de validation :
     * - Minimum 32 caractères (256 bits pour HMAC-SHA256)
     * - Ne doit pas être la valeur par défaut en production
     * - Doit être aléatoire et complexe
     * 
     * @throws IllegalStateException si le secret est invalide
     */
    private fun validateJwtSecret() {
        val isProduction = activeProfiles.contains("prod", ignoreCase = true)
        val isDefaultSecret = jwtSecret == "your-jwt-secret-key-change-in-production" ||
                             jwtSecret == "CHANGEME_LONG_RANDOM_SECRET_256_BITS_MINIMUM" ||
                             jwtSecret.length < 32

        if (isDefaultSecret) {
            if (isProduction) {
                val error = """
                    ⚠️ ERREUR CRITIQUE DE SÉCURITÉ ⚠️
                    
                    Le JWT_SECRET n'est pas configuré correctement en production !
                    
                    Le secret JWT doit :
                    - Faire au minimum 32 caractères (256 bits)
                    - Être aléatoire et complexe
                    - Ne jamais être la valeur par défaut
                    - Être stocké dans une variable d'environnement sécurisée
                    
                    Configuration actuelle : ${if (jwtSecret.length < 32) "Trop court (${jwtSecret.length} caractères)" else "Valeur par défaut détectée"}
                    
                    Pour générer un secret sécurisé :
                    openssl rand -base64 32
                    
                    L'application ne peut pas démarrer avec un secret invalide en production.
                """.trimIndent()
                
                logger.error(error)
                throw IllegalStateException("JWT_SECRET invalide en production. Veuillez configurer un secret sécurisé.")
            } else {
                logger.warn(
                    """
                    ⚠️ ATTENTION : JWT_SECRET utilise une valeur par défaut ou est trop court !
                    
                    En développement, c'est acceptable, mais en production, vous DEVEZ :
                    1. Générer un secret sécurisé : openssl rand -base64 32
                    2. Le configurer via la variable d'environnement JWT_SECRET
                    3. S'assurer qu'il fait au minimum 32 caractères
                    
                    Secret actuel : ${if (jwtSecret.length < 32) "Trop court (${jwtSecret.length} caractères)" else "Valeur par défaut"}
                    """.trimIndent()
                )
            }
        }

        // Validation de la longueur minimale (même en développement)
        if (jwtSecret.length < 32) {
            val error = "JWT_SECRET doit faire au minimum 32 caractères (256 bits). Longueur actuelle : ${jwtSecret.length}"
            if (isProduction) {
                logger.error(error)
                throw IllegalStateException(error)
            } else {
                logger.warn("$error (acceptable en développement, mais pas en production)")
            }
        }

        // Validation de la complexité (optionnel, mais recommandé)
        val hasUpperCase = jwtSecret.any { it.isUpperCase() }
        val hasLowerCase = jwtSecret.any { it.isLowerCase() }
        val hasDigit = jwtSecret.any { it.isDigit() }
        val hasSpecial = jwtSecret.any { !it.isLetterOrDigit() }

        if (isProduction && (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecial)) {
            logger.warn(
                """
                Le JWT_SECRET pourrait être plus complexe. Recommandation :
                - Mélanger majuscules, minuscules, chiffres et caractères spéciaux
                - Utiliser un générateur aléatoire : openssl rand -base64 32
                """.trimIndent()
            )
        }

        logger.info("JWT_SECRET validé avec succès (longueur: ${jwtSecret.length} caractères)")
    }

    /**
     * Génère un access token JWT pour un utilisateur (courte durée).
     */
    fun generateToken(user: User): String {
        val now = Date()
        // Utiliser les minutes pour les access tokens (120 minutes = 2 heures par défaut)
        val expiryDate = Date(now.time + jwtExpirationMinutes * 60 * 1000)

        return Jwts.builder()
            .subject(user.id)
            .claim("userId", user.id)  // Ajout explicite du userId
            .claim("email", user.email)
            .claim("userType", user.userType.name)
            .claim("firstName", user.firstName)
            .claim("lastName", user.lastName)
            .claim("isActive", user.isActive)  // Ajout important !
            .claim("isVerified", user.isVerified)  // Vérification professionnelle/business
            .claim("emailVerified", user.emailVerified)  // Vérification email OTP
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Extrait l'ID utilisateur d'un token JWT.
     */
    fun getUserIdFromToken(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.subject ?: claims["userId"] as String
    }

    /**
     * Extrait l'email d'un token JWT.
     */
    fun getEmailFromToken(token: String): String? {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims["email"] as? String
    }

    /**
     * Extrait le userType d'un token JWT.
     */
    fun getUserTypeFromToken(token: String): String? {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims["userType"] as? String
    }

    /**
     * Vérifie si l'utilisateur est actif directement depuis le token.
     * Évite une requête en base si possible.
     */
    fun isUserActiveFromToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            (claims["isActive"] as? Boolean) ?: true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si l'utilisateur est vérifié professionnellement (business verification) directement depuis le token.
     * Évite une requête en base si possible.
     */
    fun isUserVerifiedFromToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            (claims["isVerified"] as? Boolean) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si l'email de l'utilisateur est vérifié (OTP verification) directement depuis le token.
     * Évite une requête en base si possible.
     */
    fun isUserEmailVerifiedFromToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            (claims["emailVerified"] as? Boolean) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Crée un objet User depuis les claims du token JWT.
     * 
     * Cette méthode permet d'éviter une requête en base de données
     * en utilisant les informations déjà présentes dans le token.
     * 
     * IMPORTANT : Les informations du token peuvent être obsolètes
     * (par exemple, isActive peut avoir changé). Cette méthode doit
     * être utilisée uniquement pour l'authentification de base.
     * Pour des vérifications critiques (isActive, isVerified), il
     * faut toujours vérifier en base de données.
     * 
     * @param token Le token JWT valide
     * @return Un objet User créé depuis les claims du token
     */
    fun getUserFromToken(token: String): User {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return User(
            id = claims.subject ?: (claims["userId"] as? String),
            email = (claims["email"] as? String) ?: "",
            userType = UserType.valueOf((claims["userType"] as? String) ?: "client"),
            firstName = claims["firstName"] as? String,
            lastName = claims["lastName"] as? String,
            isActive = (claims["isActive"] as? Boolean) ?: true,
            isVerified = (claims["isVerified"] as? Boolean) ?: false,
            emailVerified = (claims["emailVerified"] as? Boolean) ?: false,
            passwordHash = "" // Non inclus dans le token pour des raisons de sécurité
        )
    }

    /**
     * Extrait tous les claims d'un token JWT.
     * 
     * Utile pour la validation de cohérence ou pour comparer
     * les claims avec les données en base de données.
     * 
     * @param token Le token JWT valide
     * @return Une Map contenant tous les claims du token
     */
    fun getAllClaimsFromToken(token: String): Map<String, Any> {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return mapOf(
            "userId" to (claims.subject ?: (claims["userId"] as? String) ?: ""),
            "email" to ((claims["email"] as? String) ?: ""),
            "userType" to ((claims["userType"] as? String) ?: "client"),
            "firstName" to (claims["firstName"] as? String ?: ""),
            "lastName" to (claims["lastName"] as? String ?: ""),
            "isActive" to ((claims["isActive"] as? Boolean) ?: true),
            "isVerified" to ((claims["isVerified"] as? Boolean) ?: false),
            "emailVerified" to ((claims["emailVerified"] as? Boolean) ?: false)
        )
    }

    /**
     * Valide un token JWT.
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: SecurityException) {
            logger.debug("Signature JWT invalide: {}", e.message)
            false
        } catch (e: MalformedJwtException) {
            logger.debug("Token JWT malformé: {}", e.message)
            false
        } catch (e: ExpiredJwtException) {
            logger.debug("Token JWT expiré: {}", e.message)
            false
        } catch (e: UnsupportedJwtException) {
            logger.debug("Token JWT non supporté: {}", e.message)
            false
        } catch (e: IllegalArgumentException) {
            logger.debug("Claims JWT vides: {}", e.message)
            false
        } catch (e: Exception) {
            logger.error("Erreur inconnue lors de la validation du token JWT", e)
            false
        }
    }

    /**
     * Génère un refresh token (durée de vie plus longue).
     */
    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + 30L * 24 * 3600 * 1000) // 30 jours

        return Jwts.builder()
            .subject(user.id)
            .claim("type", "refresh")
            .claim("userId", user.id)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }
}