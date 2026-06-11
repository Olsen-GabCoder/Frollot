package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un commentaire sur un post.
 */
@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comment_post", columnList = "post_id"),
        Index(name = "idx_comment_author", columnList = "author_id"),
        Index(name = "idx_comment_created", columnList = "created_at")
    ]
)
data class Comment(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    var author: User? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = "",

    // Phase H.3 - Modération de Contenu Coiffure
    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le commentaire est valide.
     */
    fun isValid(): Boolean {
        return content.isNotBlank() && post != null && author != null
    }

    /**
     * Retourne le nom complet de l'auteur.
     */
    fun getAuthorName(): String {
        val user = author ?: return "Utilisateur inconnu"
        return "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
            .ifBlank { user.email }
    }
}

