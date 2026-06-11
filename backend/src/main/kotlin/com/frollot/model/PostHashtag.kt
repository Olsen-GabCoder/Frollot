package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant l'association entre un post et un hashtag coiffure.
 * 
 * Permet de lier un post à un ou plusieurs hashtags.
 * Contrainte d'unicité : Un même hashtag ne peut être associé qu'une seule fois à un post.
 */
@Entity
@Table(
    name = "post_hashtags",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_hashtag",
            columnNames = ["post_id", "hashtag_id"]
        )
    ],
    indexes = [
        Index(name = "idx_post_hashtag_post", columnList = "post_id"),
        Index(name = "idx_post_hashtag_hashtag", columnList = "hashtag_id")
    ]
)
data class PostHashtag(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    @JsonIgnore
    var hashtag: HairHashtag? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si l'association est valide.
     */
    fun isValid(): Boolean {
        return post != null && hashtag != null
    }
}

