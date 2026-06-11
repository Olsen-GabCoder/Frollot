package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant une collection thématique de posts.
 * Phase F.1 - Collections Thématiques
 * 
 * Permet aux utilisateurs d'organiser leurs posts favoris en collections thématiques.
 */
@Entity
@Table(
    name = "collections",
    indexes = [
        Index(name = "idx_collection_user", columnList = "user_id"),
        Index(name = "idx_collection_category", columnList = "category"),
        Index(name = "idx_collection_public", columnList = "is_public"),
        Index(name = "idx_collection_created_at", columnList = "created_at")
    ]
)
data class Collection(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    var user: User? = null,

    @Column(nullable = false, length = 200)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "cover_image_url", length = 500)
    var coverImageUrl: String? = null,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = true,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: CollectionCategory = CollectionCategory.INSPIRATION,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si la collection est valide.
     */
    fun isValid(): Boolean {
        return user != null && name.isNotBlank()
    }
}

