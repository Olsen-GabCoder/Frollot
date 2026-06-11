package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant une réaction spécialisée sur un post.
 * Phase D.4 - Réactions Spécialisées Coiffure
 * 
 * Contrainte d'unicité : Un utilisateur ne peut avoir qu'une seule réaction par post.
 * Si l'utilisateur change de réaction, la précédente est remplacée.
 */
@Entity
@Table(
    name = "post_reactions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_reaction_unique",
            columnNames = ["post_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_post", columnList = "post_id"),
        Index(name = "idx_user", columnList = "user_id"),
        Index(name = "idx_reaction_type", columnList = "reaction_type"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
data class PostReaction(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    var user: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 20)
    var reactionType: ReactionType = ReactionType.LIKE,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si la réaction est valide.
     */
    fun isValid(): Boolean {
        return post != null && user != null && reactionType != null
    }

    /**
     * Retourne le nom complet de l'utilisateur qui a réagi.
     */
    fun getUserName(): String {
        val user = user ?: return "Utilisateur inconnu"
        return "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
            .ifBlank { user.email }
    }
}

