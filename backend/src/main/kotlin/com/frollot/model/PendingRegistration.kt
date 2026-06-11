package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant une pré-inscription en attente de vérification email
 */
@Entity
@Table(name = "pending_registrations")
data class PendingRegistration(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    var id: String? = null,

    @Column(unique = true, nullable = false)
    var email: String = "",

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var userType: UserType = UserType.client,

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Column(name = "verification_token", length = 100, unique = true)
    var verificationToken: String? = null,

    @Column(name = "token_expires_at")
    var tokenExpiresAt: LocalDateTime? = null,

    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: LocalDateTime? = null,

    @Column(name = "attempts")
    var attempts: Int = 0,

    @Column(name = "last_attempt_at")
    var lastAttemptAt: LocalDateTime? = null
)
