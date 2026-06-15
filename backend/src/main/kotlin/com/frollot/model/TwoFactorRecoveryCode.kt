package com.frollot.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Code de récupération 2FA à usage unique (S9a, table V044).
 *
 * Stocké HACHÉ (BCrypt, 60 caractères) — jamais en clair.
 * Les 10 codes en clair ne sont montrés qu'une seule fois,
 * dans la réponse du POST /me/2fa/confirm.
 */
@Entity
@Table(
    name = "two_factor_recovery_codes",
    indexes = [
        Index(name = "idx_2fa_recovery_user", columnList = "user_id")
    ]
)
data class TwoFactorRecoveryCode(
    @Id
    @Column(nullable = false, length = 36, columnDefinition = "CHAR(36)")
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    val userId: String,

    @Column(name = "code_hash", nullable = false, length = 60)
    val codeHash: String,

    @Column(name = "used_at")
    var usedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
