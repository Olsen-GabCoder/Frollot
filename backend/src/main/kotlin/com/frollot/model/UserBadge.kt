package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant l'association entre un utilisateur et un badge.
 * Phase E.3 - Badges et Certifications
 * 
 * Un utilisateur peut avoir plusieurs badges, et un badge peut être attribué à plusieurs utilisateurs.
 * Contrainte d'unicité : Un utilisateur ne peut avoir qu'une seule fois un badge donné.
 * 
 * @property id Identifiant unique (UUID)
 * @property user Utilisateur qui a obtenu le badge
 * @property badge Badge obtenu
 * @property earnedAt Date d'obtention du badge
 * @property isDisplayed Indique si le badge doit être affiché sur le profil (par défaut true)
 */
@Entity
@Table(
    name = "user_badges",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_badge",
            columnNames = ["user_id", "badge_id"]
        )
    ],
    indexes = [
        Index(name = "idx_user", columnList = "user_id"),
        Index(name = "idx_badge", columnList = "badge_id"),
        Index(name = "idx_displayed", columnList = "user_id, is_displayed"),
        Index(name = "idx_earned_at", columnList = "earned_at")
    ]
)
data class UserBadge(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    var user: User? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "badge_id", nullable = false)
    var badge: Badge? = null,

    @CreationTimestamp
    @Column(name = "earned_at", updatable = false, nullable = false)
    var earnedAt: LocalDateTime? = null,

    @Column(name = "is_displayed", nullable = false)
    var isDisplayed: Boolean = true
) {
    /**
     * Vérifie si l'association est valide.
     */
    fun isValid(): Boolean {
        return user != null && badge != null
    }
}

