package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un favori sur un post.
 * 
 * Contrainte d'unicité : Un utilisateur ne peut mettre un post en favori qu'une seule fois.
 */
@Entity
@Table(
    name = "post_favorites",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_favorite_user",
            columnNames = ["post_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_favorite_post", columnList = "post_id"),
        Index(name = "idx_favorite_user", columnList = "user_id")
    ]
)
data class PostFavorite(
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le favori est valide.
     */
    fun isValid(): Boolean {
        return post != null && user != null
    }
}

