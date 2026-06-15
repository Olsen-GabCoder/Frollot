package com.frollot.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Configuration 2FA TOTP d'un utilisateur (S9a, table V044).
 *
 * Activation en deux temps :
 * - setup   : la ligne existe avec enabled=false (secret généré, pas encore prouvé).
 *             Un nouveau setup écrase la ligne non confirmée (pattern S4 pending_email).
 * - confirm : premier code TOTP valide -> enabled=true + confirmedAt.
 *
 * Le secret est chiffré au repos (AES-256-GCM, TotpEncryptionService) :
 * il n'est JAMAIS stocké ni exposé en clair après l'activation.
 */
@Entity
@Table(name = "user_two_factor")
data class UserTwoFactor(
    @Id
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    val userId: String,

    @Column(name = "secret_encrypted", nullable = false, length = 512)
    var secretEncrypted: String,

    @Column(nullable = false)
    var enabled: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "confirmed_at")
    var confirmedAt: LocalDateTime? = null
)
