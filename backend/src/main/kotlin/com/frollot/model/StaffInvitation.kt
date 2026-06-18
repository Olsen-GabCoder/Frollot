package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Statuts possibles d'une invitation d'équipe.
 */
enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED,
    CANCELLED
}

/**
 * Entité représentant une invitation à rejoindre l'équipe d'un salon.
 *
 * Flux : owner crée l'invitation (PENDING) → le coiffeur l'accepte (ACCEPTED, SalonStaff créé)
 * ou la refuse (DECLINED). L'owner peut l'annuler (CANCELLED). Expire après 7 jours.
 */
@Entity
@Table(
    name = "staff_invitations",
    indexes = [
        Index(name = "idx_staff_inv_salon", columnList = "salon_id"),
        Index(name = "idx_staff_inv_user", columnList = "invited_user_id"),
        Index(name = "idx_staff_inv_status", columnList = "status"),
        Index(name = "idx_staff_inv_token", columnList = "token")
    ]
)
data class StaffInvitation(
    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false, foreignKey = ForeignKey(name = "fk_staff_inv_salon"))
    var salon: Salon? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = true, foreignKey = ForeignKey(name = "fk_staff_inv_user"))
    var invitedUser: User? = null,

    @Column(name = "invited_email", length = 255, nullable = true)
    var invitedEmail: String? = null,

    @Column(name = "role", length = 30, nullable = false)
    var role: String = "hairstylist",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "staff_invitation_specialties",
        joinColumns = [JoinColumn(name = "invitation_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty")
    var specialties: MutableList<ServiceCategory> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: InvitationStatus = InvitationStatus.PENDING,

    @Column(name = "token", length = 36, nullable = false)
    var token: String = "",

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
    fun isPending(): Boolean = status == InvitationStatus.PENDING && !isExpired()
}
