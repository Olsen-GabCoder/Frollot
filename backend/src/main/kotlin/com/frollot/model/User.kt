package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Enumération des types d'utilisateurs dans le système Frollot
 */
enum class UserType {
    client,
    hairstylist,
    salon_owner,
    admin
}


/**
 * Entité représentant un utilisateur dans le système
 */
@Entity
@Table(name = "users")
data class User(
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

    // V045 — visibilité du numéro (déclaratif, sans vérification). FALSE = privé (défaut) :
    // visible du seul propriétaire + canal transactionnel (réservations). TRUE = vues publiques aussi.
    @Column(name = "phone_public", nullable = false)
    var phonePublic: Boolean = false,

    @Column(name = "is_verified")
    var isVerified: Boolean = false,

    // Phase H.2 - Vérification Salons/Coiffeurs
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", length = 20)
    var verificationType: VerificationType? = null,

    // Vérification d'email obligatoire
    @Column(name = "email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "email_verification_token", length = 100)
    var emailVerificationToken: String? = null,

    @Column(name = "email_verification_token_expires_at")
    var emailVerificationTokenExpiresAt: LocalDateTime? = null,

    @Column(name = "email_verification_sent_at")
    var emailVerificationSentAt: LocalDateTime? = null,

    // Changement d'email avec re-vérification (V040) :
    // nouvel email en attente de confirmation, l'email actif ne change pas avant validation
    @Column(name = "pending_email")
    var pendingEmail: String? = null,

    @Column(name = "password_reset_token", length = 36)
    var passwordResetToken: String? = null,

    @Column(name = "password_reset_token_expiry")
    var passwordResetTokenExpiry: LocalDateTime? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "cover_image_url", length = 500)
    var coverImageUrl: String? = null,

    // Phase E.1 - Profil Coiffeur Enrichi
    @Column(columnDefinition = "TEXT")
    var bio: String? = null,

    @Column(name = "years_experience")
    var yearsExperience: Int? = null,

    @Column(columnDefinition = "TEXT")
    var certifications: String? = null,

    @Column(name = "instagram_handle", length = 100)
    var instagramHandle: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_highlighted")
    @JsonIgnore
    var portfolioHighlighted: Portfolio? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_specialties",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "specialty", length = 100)
    var specialties: MutableList<String> = mutableListOf(),

    // Phase 3 - Fonctionnalité Langue : Langue préférée de l'utilisateur
    @Column(name = "preferred_language", length = 2)
    var preferredLanguage: String? = "fr", // Par défaut français

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)