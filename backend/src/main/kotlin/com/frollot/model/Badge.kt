package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un badge disponible dans le système.
 * Phase E.3 - Badges et Certifications
 * 
 * Un badge est une reconnaissance visuelle qui peut être attribuée aux utilisateurs
 * pour mettre en valeur leur expertise, leurs certifications, leurs compétitions, etc.
 * 
 * @property id Identifiant unique (UUID)
 * @property name Nom du badge (ex: "Formation L'Oréal", "Champion Régional 2023")
 * @property description Description détaillée du badge
 * @property iconUrl URL de l'icône du badge
 * @property category Catégorie du badge (CERTIFICATION, COMPETITION, FORMATION, PARTENARIAT)
 * @property createdAt Date de création du badge
 */
@Entity
@Table(
    name = "badges",
    indexes = [
        Index(name = "idx_category", columnList = "category"),
        Index(name = "idx_name", columnList = "name")
    ]
)
data class Badge(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @Column(nullable = false, length = 200)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "icon_url", length = 500)
    var iconUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var category: BadgeCategory = BadgeCategory.CERTIFICATION,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le badge est valide.
     */
    fun isValid(): Boolean {
        return name.isNotBlank()
    }
}

