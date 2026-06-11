package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un like sur un post.
 * 
 * Contrainte d'unicité : Un utilisateur ne peut liker un post qu'une seule fois.
 */
@Entity
@Table(
    name = "post_likes",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_like_user",
            columnNames = ["post_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_like_post", columnList = "post_id"),
        Index(name = "idx_like_user", columnList = "user_id")
    ]
)
data class PostLike(
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
     * Vérifie si le like est valide.
     */
    fun isValid(): Boolean {
        return post != null && user != null
    }
}

