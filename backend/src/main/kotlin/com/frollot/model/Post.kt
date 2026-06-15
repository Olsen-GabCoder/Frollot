package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un post dans le réseau social.
 */
@Entity
@Table(
    name = "posts",
    indexes = [
        Index(name = "idx_post_author", columnList = "author_id"),
        Index(name = "idx_post_created", columnList = "created_at")
    ]
)
data class Post(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    var author: User? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    var postType: PostType = PostType.GENERAL,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(name = "likes_count", nullable = false)
    var likesCount: Int = 0,

    @Column(name = "comments_count", nullable = false)
    var commentsCount: Int = 0,

    @Column(name = "shares_count", nullable = false)
    var sharesCount: Int = 0,

    @Column(name = "is_pinned", nullable = false)
    var isPinned: Boolean = false,

    @Convert(converter = PostVisibilityConverter::class)
    @Column(name = "visibility", nullable = false, length = 20)
    var visibility: PostVisibility = PostVisibility.PUBLIC, // Phase F.3 - Visibilité des Posts

    // Phase H.3 - Modération de Contenu Coiffure
    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    // V041 - Archivage global (façon Instagram) : masqué pour tous, réversible par l'auteur
    @Column(name = "is_archived", nullable = false)
    var isArchived: Boolean = false,

    @Column(name = "archived_at")
    var archivedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    var likes: MutableList<PostLike> = mutableListOf(),

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    var comments: MutableList<Comment> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le post est valide.
     */
    fun isValid(): Boolean {
        return content.isNotBlank() && author != null
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

