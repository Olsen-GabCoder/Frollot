package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant l'association entre un post et un service de coiffure.
 * 
 * Permet de lier un post à un ou plusieurs services proposés par les salons.
 * Un post peut être associé à plusieurs services, et un service peut être associé à plusieurs posts.
 * 
 * Contrainte d'unicité : Un même service ne peut être associé qu'une seule fois à un post.
 */
@Entity
@Table(
    name = "post_services",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_service",
            columnNames = ["post_id", "service_id"]
        )
    ],
    indexes = [
        Index(name = "idx_post_service_post", columnList = "post_id"),
        Index(name = "idx_post_service_service", columnList = "service_id")
    ]
)
data class PostService(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnore
    var service: SalonService? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si l'association est valide.
     */
    fun isValid(): Boolean {
        return post != null && service != null
    }
}

