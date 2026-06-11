package com.frollot.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Entité représentant un refresh token.
 *
 * Les refresh tokens sont stockés en base de données pour permettre :
 * - La révocation (logout, changement de mot de passe)
 * - La rotation (nouveau token à chaque refresh)
 * - Le nettoyage automatique des tokens expirés
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_token_token", columnList = "token"),
        Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_token_expires_at", columnList = "expires_at")
    ]
)
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 500)
    val token: String,

    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    val userId: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val revoked: Boolean = false,
    
    // Nouvelles colonnes pour les informations de session
    @Column(name = "device_name", length = 100)
    val deviceName: String? = null,
    
    @Column(name = "device_type", length = 50)
    val deviceType: String? = null, // "mobile", "desktop", "tablet"
    
    @Column(name = "ip_address", length = 45)
    val ipAddress: String? = null,
    
    @Column(name = "user_agent", length = 500)
    val userAgent: String? = null,
    
    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,
    
    @Column(name = "location", length = 100)
    val location: String? = null
) {
    /**
     * Vérifie si le token est expiré.
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    /**
     * Vérifie si le token est valide (non expiré et non révoqué).
     */
    fun isValid(): Boolean {
        return !revoked && !isExpired()
    }
}

