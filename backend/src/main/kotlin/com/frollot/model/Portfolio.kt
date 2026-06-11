package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un portfolio de créations coiffure.
 * 
 * Un portfolio permet à un coiffeur ou un salon d'organiser ses posts
 * en collections thématiques (ex: "Mes Colorations 2024", "Nos Coupes Tendance").
 * 
 * @property id Identifiant unique (UUID)
 * @property ownerId ID du propriétaire (coiffeur ou salon)
 * @property ownerType Type de propriétaire (coiffeur ou salon)
 * @property name Nom du portfolio
 * @property description Description du portfolio
 * @property coverImageUrl URL de l'image de couverture
 * @property isPublic Indique si le portfolio est public ou privé
 * @property createdAt Date de création
 * @property updatedAt Date de dernière modification
 */
@Entity
@Table(
    name = "portfolios",
    indexes = [
        Index(name = "idx_portfolio_owner", columnList = "owner_id, owner_type"),
        Index(name = "idx_portfolio_public", columnList = "is_public")
    ]
)
data class Portfolio(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @Column(name = "owner_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var ownerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    var ownerType: PortfolioOwnerType = PortfolioOwnerType.coiffeur,

    @Column(nullable = false, length = 200)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "cover_image_url", length = 500)
    var coverImageUrl: String? = null,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le portfolio est valide.
     */
    fun isValid(): Boolean {
        return ownerId.isNotBlank() && name.isNotBlank() && name.length <= 200
    }
}

