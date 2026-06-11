package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un partage de post.
 * Phase D.3 - Partage de Posts (Reposts)
 * 
 * Contrainte d'unicité : Un utilisateur ne peut partager un post qu'une seule fois.
 * Le commentaire de partage (sharedContent) est optionnel.
 */
@Entity
@Table(
    name = "post_shares",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_share_unique",
            columnNames = ["post_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_post", columnList = "post_id"),
        Index(name = "idx_user", columnList = "user_id"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
data class PostShare(
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

    @Column(name = "shared_content", columnDefinition = "TEXT")
    var sharedContent: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le partage est valide.
     */
    fun isValid(): Boolean {
        return post != null && user != null
    }

    /**
     * Retourne le nom complet de l'utilisateur qui a partagé.
     */
    fun getUserName(): String {
        val user = user ?: return "Utilisateur inconnu"
        return "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
            .ifBlank { user.email }
    }
}

