package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un post mis en avant par un salon.
 * Phase E.2 - Profil Salon Social
 * 
 * Permet aux salons de mettre en avant certains posts dans leur profil social,
 * avec un ordre personnalisable.
 * 
 * @property id Identifiant unique (UUID)
 * @property salon Salon qui met en avant le post
 * @property post Post mis en avant
 * @property orderIndex Ordre d'affichage (0 = premier)
 * @property createdAt Date d'ajout en tant que post mis en avant
 */
@Entity
@Table(
    name = "salon_highlighted_posts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_salon_post",
            columnNames = ["salon_id", "post_id"]
        )
    ],
    indexes = [
        Index(name = "idx_salon", columnList = "salon_id"),
        Index(name = "idx_post", columnList = "post_id"),
        Index(name = "idx_order", columnList = "salon_id, order_index")
    ]
)
data class SalonHighlightedPost(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    @JsonIgnore
    var salon: Salon? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si l'association est valide.
     */
    fun isValid(): Boolean {
        return salon != null && post != null
    }
}

