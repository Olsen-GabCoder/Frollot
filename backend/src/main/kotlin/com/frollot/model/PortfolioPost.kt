package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant l'association entre un portfolio et un post.
 * 
 * Permet d'organiser les posts dans un portfolio avec un ordre spécifique.
 * Contrainte d'unicité : Un même post ne peut être ajouté qu'une seule fois à un portfolio.
 * 
 * @property id Identifiant unique (UUID)
 * @property portfolio Portfolio auquel le post appartient
 * @property post Post associé au portfolio
 * @property orderIndex Ordre d'affichage dans le portfolio (0 = premier)
 * @property addedAt Date d'ajout du post au portfolio
 */
@Entity
@Table(
    name = "portfolio_posts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_portfolio_post",
            columnNames = ["portfolio_id", "post_id"]
        )
    ],
    indexes = [
        Index(name = "idx_portfolio_posts_portfolio", columnList = "portfolio_id"),
        Index(name = "idx_portfolio_posts_post", columnList = "post_id"),
        Index(name = "idx_portfolio_posts_order", columnList = "portfolio_id, order_index")
    ]
)
data class PortfolioPost(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @JsonIgnore
    var portfolio: Portfolio? = null,

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
        return portfolio != null && post != null && orderIndex >= 0
    }
}

