package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un média (image) associé à un post.
 * 
 * Permet de gérer plusieurs images par post, notamment pour les posts de type AVANT_APRES.
 * Chaque média a un type (before, after, process, detail) et un ordre d'affichage.
 * 
 * @property id Identifiant unique (UUID)
 * @property post Post auquel le média appartient
 * @property mediaUrl URL du média (image)
 * @property mediaType Type du média (before, after, process, detail)
 * @property orderIndex Ordre d'affichage (0 = premier)
 * @property createdAt Date de création
 */
@Entity
@Table(
    name = "post_media",
    indexes = [
        Index(name = "idx_post_media_post", columnList = "post_id"),
        Index(name = "idx_post_media_type", columnList = "post_id, media_type"),
        Index(name = "idx_post_media_order", columnList = "post_id, order_index")
    ]
)
data class PostMedia(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @Column(name = "media_url", length = 500, nullable = false)
    var mediaUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    var mediaType: PostMediaType = PostMediaType.before,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le média est valide.
     */
    fun isValid(): Boolean {
        return post != null && mediaUrl.isNotBlank() && orderIndex >= 0
    }
}

