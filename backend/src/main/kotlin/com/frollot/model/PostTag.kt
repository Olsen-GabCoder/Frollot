package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Énumération des types d'entités pouvant être taguées dans un post.
 */
enum class TaggedType {
    salon,
    user
}

/**
 * Entité représentant un tag/mention dans un post.
 * 
 * Permet de tagger un salon ou un utilisateur dans un post.
 * Contrainte d'unicité : Un même salon/utilisateur ne peut être tagué qu'une seule fois dans un post.
 */
@Entity
@Table(
    name = "post_tags",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_tagged",
            columnNames = ["post_id", "tagged_type", "tagged_id"]
        )
    ],
    indexes = [
        Index(name = "idx_tag_post", columnList = "post_id"),
        Index(name = "idx_tag_tagged_type", columnList = "tagged_type"),
        Index(name = "idx_tag_tagged_id", columnList = "tagged_id")
    ]
)
data class PostTag(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "tagged_type", nullable = false, length = 10)
    var taggedType: TaggedType = TaggedType.user,

    @Column(name = "tagged_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var taggedId: String = "",

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le tag est valide.
     */
    fun isValid(): Boolean {
        return post != null && taggedId.isNotBlank()
    }
}

