package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Entité représentant une prestation de service dans un salon de coiffure.
 *
 * IMPORTANT : Nommé "SalonService" pour éviter les conflits avec les annotations @Service de Spring.
 *
 * @property id Identifiant unique (UUID)
 * @property name Nom de la prestation (ex: "Coupe Homme", "Coloration complète")
 * @property description Description détaillée du service
 * @property durationMinutes Durée estimée en minutes
 * @property price Prix de la prestation (utilise BigDecimal pour la précision monétaire)
 * @property category Catégorie du service (coupe, coloration, soin, etc.)
 * @property salon Salon propriétaire de cette prestation (relation ManyToOne)
 * @property createdAt Date de création (auto-générée)
 * @property updatedAt Date de dernière modification (auto-générée)
 */
@Entity
@Table(
    name = "salon_services",
    indexes = [
        Index(name = "idx_salon_service_salon_id", columnList = "salon_id"),
        Index(name = "idx_salon_service_category", columnList = "category"),
        Index(name = "idx_salon_service_price", columnList = "price")
    ]
)
data class SalonService(
    @Id
    @Column(
        name = "id",
        length = 36,
        columnDefinition = "CHAR(36)",
        nullable = false,
        updatable = false
    )
    var id: String = "",

    @Column(
        name = "name",
        nullable = false,
        length = 150
    )
    var name: String = "",

    @Column(
        name = "description",
        columnDefinition = "TEXT",
        nullable = true
    )
    var description: String? = null,

    @Column(
        name = "duration_minutes",
        nullable = false
    )
    var durationMinutes: Int = 30,

    @Column(
        name = "price",
        nullable = false,
        precision = 10,
        scale = 2
    )
    var price: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "category",
        nullable = false,
        length = 50
    )
    var category: ServiceCategory = ServiceCategory.COUPE,

    /**
     * URLs des images d'illustration du service.
     * Stockées en JSON dans la base de données.
     * Permet aux clients de visualiser à quoi correspond le service.
     */
    @Column(
        name = "image_urls",
        columnDefinition = "TEXT",
        nullable = true
    )
    var imageUrls: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "salon_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_salon_service_salon")
    )
    @JsonIgnore // Empêche la sérialisation récursive lors des réponses JSON
    var salon: Salon? = null,

    @CreationTimestamp
    @Column(
        name = "created_at",
        updatable = false,
        nullable = false
    )
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(
        name = "updated_at",
        nullable = false
    )
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Méthode utilitaire pour formater la durée en heures/minutes.
     * Exemple : 90 minutes → "1h30"
     */
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h${minutes.toString().padStart(2, '0')}"
            hours > 0 -> "${hours}h"
            else -> "${minutes}min"
        }
    }

    /**
     * Vérifie si le service est valide pour la création.
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                durationMinutes > 0 &&
                price >= BigDecimal.ZERO &&
                salon != null
    }

    override fun toString(): String {
        return "SalonService(id=$id, name='$name', duration=$durationMinutes min, price=$price €, category=$category)"
    }

    // IMPORTANT: equals() et hashCode() pour éviter les problèmes avec @Version
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SalonService) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

/**
 * Enumération des catégories de services disponibles.
 *
 * Cette classification permet de filtrer et organiser les prestations
 * dans l'interface utilisateur.
 */
enum class ServiceCategory {
    /** Coupes de cheveux pour hommes, femmes, enfants */
    COUPE,

    /** Coloration, mèches, balayage, ombré hair */
    COLORATION,

    /** Soins capillaires, masques, traitements */
    SOIN,

    /** Coiffage pour événements, brushing, lissage */
    COIFFAGE,

    /** Soins de barbe, taille, rasage traditionnel */
    BARBE,

    /** Techniques spéciales (dreadlocks, tresses africaines, perruques) */
    TECHNIQUE,

    /** Autres prestations non catégorisées */
    AUTRE;

    /**
     * Retourne le libellé utilisateur de la catégorie.
     */
    fun getDisplayName(): String {
        return when (this) {
            COUPE -> "Coupe & Taille"
            COLORATION -> "Coloration"
            SOIN -> "Soins"
            COIFFAGE -> "Coiffage"
            BARBE -> "Barbier"
            TECHNIQUE -> "Techniques Spéciales"
            AUTRE -> "Autres Prestations"
        }
    }

    /**
     * Retourne l'emoji associé à la catégorie pour l'UI.
     */
    fun getEmoji(): String {
        return when (this) {
            COUPE -> "✂️"
            COLORATION -> "🎨"
            SOIN -> "💆"
            COIFFAGE -> "💇"
            BARBE -> "🧔"
            TECHNIQUE -> "🌟"
            AUTRE -> "📋"
        }
    }
}