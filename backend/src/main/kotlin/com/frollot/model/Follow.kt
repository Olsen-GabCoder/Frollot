package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant une relation de suivi entre un utilisateur et une entité (salon, coiffeur, user).
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 * 
 * Contrainte d'unicité : Un utilisateur ne peut suivre qu'une seule fois une entité donnée.
 */
@Entity
@Table(
    name = "follows",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_follow_unique",
            columnNames = ["follower_id", "following_type", "following_id"]
        )
    ],
    indexes = [
        Index(name = "idx_follower", columnList = "follower_id"),
        Index(name = "idx_following", columnList = "following_type, following_id")
    ]
)
data class Follow(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    @JsonIgnore
    var follower: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "following_type", nullable = false, length = 20)
    var followingType: FollowingType = FollowingType.SALON,

    @Column(name = "following_id", nullable = false, length = 36, columnDefinition = "VARCHAR(36)")
    var followingId: String = "",

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le follow est valide.
     */
    fun isValid(): Boolean {
        return follower != null && followingId.isNotBlank()
    }
}

