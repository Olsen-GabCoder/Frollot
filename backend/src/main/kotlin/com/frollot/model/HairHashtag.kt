package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un hashtag spécialisé pour l'univers de la coiffure.
 * 
 * Les hashtags sont normalisés en lowercase et uniques.
 * Ils sont catégorisés pour faciliter la découverte et la recherche.
 */
@Entity
@Table(
    name = "hair_hashtags",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_hashtag_name",
            columnNames = ["name"]
        )
    ],
    indexes = [
        Index(name = "idx_hashtag_name", columnList = "name"),
        Index(name = "idx_hashtag_category", columnList = "category"),
        Index(name = "idx_hashtag_usage", columnList = "usage_count")
    ]
)
data class HairHashtag(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @Column(nullable = false, length = 100, unique = true)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var category: HairHashtagCategory = HairHashtagCategory.STYLE,

    @Column(name = "usage_count", nullable = false)
    var usageCount: Int = 0,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le hashtag est valide.
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && name.length <= 100
    }

    /**
     * Normalise le nom du hashtag (lowercase, sans #).
     */
    fun normalizeName(): String {
        return name.lowercase().trim().removePrefix("#")
    }
}

