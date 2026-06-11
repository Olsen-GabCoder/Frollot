package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un token de device pour les notifications push FCM.
 */
@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = [
        UniqueConstraint(name = "unique_user_token", columnNames = ["user_id", "token"])
    ],
    indexes = [
        Index(name = "idx_device_token_user", columnList = "user_id"),
        Index(name = "idx_device_token_token", columnList = "token"),
        Index(name = "idx_device_token_active", columnList = "is_active")
    ]
)
data class DeviceToken(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(nullable = false, length = 500)
    var token: String = "",

    @Column(length = 20, nullable = false)
    var platform: String = "android",

    @Column(name = "device_info", length = 255)
    var deviceInfo: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
)

