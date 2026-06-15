package com.frollot.service

import com.frollot.model.RefreshToken
import com.frollot.model.User
import com.frollot.repository.RefreshTokenRepository
import com.frollot.repository.UserRepository
import com.frollot.security.JwtTokenProvider
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Service de gestion des refresh tokens.
 *
 * Gère :
 * - Création de refresh tokens
 * - Validation et rotation des tokens
 * - Révocation (logout, logout all devices)
 * - Nettoyage automatique des tokens expirés
 */
@Service
@Transactional
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    companion object {
        private const val REFRESH_TOKEN_VALIDITY_DAYS = 7L

        /**
         * Plafond de sessions actives simultanées par utilisateur (S8b).
         * Au-delà, les sessions les moins récemment utilisées sont automatiquement
         * révoquées lors d'une nouvelle connexion (éviction LRU — on ne refuse jamais
         * le login, sinon un utilisateur ayant perdu ses anciens appareils serait bloqué).
         */
        const val MAX_ACTIVE_SESSIONS = 5
    }

    /**
     * Crée un nouveau refresh token pour un utilisateur (simple).
     *
     * @param userId ID de l'utilisateur
     * @return Le token généré (UUID)
     */
    @Transactional
    fun createRefreshToken(userId: String): String {
        return createRefreshTokenWithDeviceInfo(userId, null, null)
    }
    
    /**
     * Crée un nouveau refresh token avec les informations de device.
     *
     * @param userId ID de l'utilisateur
     * @param userAgent User-Agent de la requête
     * @param ipAddress Adresse IP du client
     * @param deviceName Nom de l'appareil (optionnel)
     * @return Le token généré (UUID)
     */
    @Transactional
    fun createRefreshTokenWithDeviceInfo(
        userId: String, 
        userAgent: String? = null, 
        ipAddress: String? = null,
        deviceName: String? = null
    ): String {
        // Plafond de sessions (S8b) : si la limite est atteinte, révoquer les sessions
        // les moins récemment utilisées pour que le total après création reste <= MAX.
        // Sûr vis-à-vis de la rotation : rotateRefreshToken révoque l'ancien token AVANT
        // d'appeler cette méthode (le flush automatique pré-requête l'exclut du décompte).
        enforceSessionLimit(userId)

        // Générer un token UUID sécurisé
        val token = UUID.randomUUID().toString()

        // Calculer la date d'expiration (7 jours)
        val expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        
        // Détecter le type de device
        val deviceType = detectDeviceType(userAgent)

        // Créer et sauvegarder le refresh token
        val refreshToken = RefreshToken(
            token = token,
            userId = userId,
            expiresAt = expiresAt,
            userAgent = userAgent,
            ipAddress = ipAddress,
            deviceName = deviceName,
            deviceType = deviceType,
            lastUsedAt = LocalDateTime.now()
        )

        refreshTokenRepository.save(refreshToken)

        return token
    }
    
    /**
     * Révoque les sessions excédentaires (les moins récemment utilisées d'abord)
     * pour qu'après la création d'une nouvelle session, l'utilisateur ait au plus
     * [MAX_ACTIVE_SESSIONS] sessions actives.
     */
    private fun enforceSessionLimit(userId: String) {
        val activeTokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now())
        if (activeTokens.size < MAX_ACTIVE_SESSIONS) return

        activeTokens
            .sortedBy { it.lastUsedAt ?: it.createdAt }
            .take(activeTokens.size - MAX_ACTIVE_SESSIONS + 1)
            .forEach { refreshTokenRepository.save(it.copy(revoked = true)) }
    }

    /**
     * Détecte le type de device à partir du User-Agent.
     */
    private fun detectDeviceType(userAgent: String?): String {
        if (userAgent.isNullOrBlank()) return "unknown"
        
        return when {
            userAgent.contains("Mobile", ignoreCase = true) || 
                userAgent.contains("Android", ignoreCase = true) && !userAgent.contains("Tablet") ||
                userAgent.contains("iPhone", ignoreCase = true) -> "mobile"
            userAgent.contains("iPad", ignoreCase = true) || 
                userAgent.contains("Tablet", ignoreCase = true) -> "tablet"
            else -> "desktop"
        }
    }

    /**
     * Valide un refresh token.
     *
     * @param token Le token à valider
     * @return Le RefreshToken si valide, null sinon
     */
    @Transactional(readOnly = true)
    fun validateRefreshToken(token: String): RefreshToken? {
        val refreshToken = refreshTokenRepository.findByToken(token) ?: return null

        return if (refreshToken.isValid()) {
            refreshToken
        } else {
            null
        }
    }

    /**
     * Effectue la rotation d'un refresh token.
     *
     * 1. Valide l'ancien token
     * 2. Révoque l'ancien token
     * 3. Crée un nouveau refresh token
     * 4. Génère un nouveau access token
     *
     * @param oldToken L'ancien refresh token
     * @return Pair<accessToken, refreshToken>
     */
    @Transactional
    fun rotateRefreshToken(oldToken: String): Pair<String, String> {
        // 1. Valider l'ancien token
        val refreshToken = validateRefreshToken(oldToken)
            ?: throw IllegalArgumentException("Refresh token invalide ou expiré")

        // 2. Révoquer l'ancien token
        val revokedToken = refreshToken.copy(revoked = true)
        refreshTokenRepository.save(revokedToken)

        // 3. Créer un nouveau refresh token
        val newRefreshToken = createRefreshToken(refreshToken.userId)

        // 4. Générer un nouveau access token
        // Charger l'utilisateur complet depuis le repository
        val user = userRepository.findById(refreshToken.userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }
        val newAccessToken = jwtTokenProvider.generateToken(user)

        return Pair(newAccessToken, newRefreshToken)
    }

    /**
     * Révoque un refresh token (logout).
     *
     * @param token Le token à révoquer
     */
    @Transactional
    fun revokeRefreshToken(token: String) {
        val refreshToken = refreshTokenRepository.findByToken(token)
        refreshToken?.let {
            val revoked = it.copy(revoked = true)
            refreshTokenRepository.save(revoked)
        }
    }

    /**
     * Révoque tous les tokens d'un utilisateur (logout all devices).
     *
     * @param userId ID de l'utilisateur
     */
    @Transactional
    fun revokeAllUserTokens(userId: String) {
        val tokens = refreshTokenRepository.findAllByUserId(userId)
        tokens.forEach { token ->
            if (!token.revoked) {
                val revoked = token.copy(revoked = true)
                refreshTokenRepository.save(revoked)
            }
        }
    }

    /**
     * Nettoie automatiquement les tokens expirés depuis plus de 24h.
     *
     * Exécuté tous les jours à 2h du matin.
     */
    @Scheduled(cron = "0 0 2 * * *") // 2h du matin tous les jours
    @Transactional
    fun cleanupExpiredTokens() {
        val cutoffDate = LocalDateTime.now().minusDays(1)
        refreshTokenRepository.deleteByExpiresAtBefore(cutoffDate)
    }

    /**
     * Récupère toutes les sessions actives (tokens valides) d'un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @return Liste des tokens valides
     */
    @Transactional(readOnly = true)
    fun getActiveSessions(userId: String): List<RefreshToken> {
        return refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now())
    }

    /**
     * Révoque un token spécifique par son ID.
     *
     * @param tokenId ID du token à révoquer
     * @param userId ID de l'utilisateur (pour vérification)
     * @return true si révoqué, false si non trouvé ou pas autorisé
     */
    @Transactional
    fun revokeTokenById(tokenId: Long, userId: String): Boolean {
        val token = refreshTokenRepository.findById(tokenId).orElse(null)
            ?: return false
        
        // Vérifier que le token appartient bien à l'utilisateur
        if (token.userId != userId) {
            return false
        }
        
        if (!token.revoked) {
            val revoked = token.copy(revoked = true)
            refreshTokenRepository.save(revoked)
        }
        
        return true
    }

    /**
     * Compte le nombre de sessions actives d'un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de sessions actives
     */
    @Transactional(readOnly = true)
    fun countActiveSessions(userId: String): Int {
        return refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now()).size
    }
    
    /**
     * Met à jour la date de dernière utilisation d'un token.
     *
     * @param token Le token utilisé
     */
    @Transactional
    fun updateLastUsed(token: String) {
        val refreshToken = refreshTokenRepository.findByToken(token)
        refreshToken?.let {
            it.lastUsedAt = LocalDateTime.now()
            refreshTokenRepository.save(it)
        }
    }
    
    /**
     * Récupère l'ID du token actuel à partir du token string.
     *
     * @param token Le token string
     * @return L'ID du token ou null
     */
    @Transactional(readOnly = true)
    fun getTokenId(token: String): Long? {
        return refreshTokenRepository.findByToken(token)?.id
    }
    
    /**
     * Révoque toutes les sessions sauf la session courante.
     *
     * @param userId ID de l'utilisateur
     * @param currentTokenId ID du token à conserver
     * @return Nombre de sessions révoquées
     */
    @Transactional
    fun revokeAllOtherSessions(userId: String, currentTokenId: Long): Int {
        val tokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now())
        var count = 0
        tokens.forEach { token ->
            if (token.id != currentTokenId && !token.revoked) {
                val revoked = token.copy(revoked = true)
                refreshTokenRepository.save(revoked)
                count++
            }
        }
        return count
    }
}

