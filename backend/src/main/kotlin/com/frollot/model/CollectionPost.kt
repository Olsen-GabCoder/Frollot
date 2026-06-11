package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant l'association entre une collection et un post.
 * Phase F.1 - Collections Thématiques
 * 
 * Permet d'organiser les posts dans une collection avec un ordre spécifique.
 */
@Entity
@Table(
    name = "collection_posts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_collection_post",
            columnNames = ["collection_id", "post_id"]
        )
    ],
    indexes = [
        Index(name = "idx_collection_post_collection", columnList = "collection_id"),
        Index(name = "idx_collection_post_post", columnList = "post_id"),
        Index(name = "idx_collection_post_order", columnList = "collection_id, order_index"),
        Index(name = "idx_collection_post_added_at", columnList = "added_at")
    ]
)
data class CollectionPost(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonIgnore
    var collection: Collection? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @CreationTimestamp
    @Column(name = "added_at", updatable = false, nullable = false)
    var addedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si l'association est valide.
     */
    fun isValid(): Boolean {
        return collection != null && post != null
    }
}

