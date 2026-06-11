package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un membre du staff (coiffeur/employé) d'un salon.
 *
 * Cette table fait le lien entre :
 * - Un Salon (l'employeur)
 * - Un User (l'employé de type hairstylist)
 *
 * Permet de gérer :
 * - L'équipe d'un salon
 * - Les spécialités de chaque coiffeur
 * - Les assignations de réservations
 *
 * @property id Identifiant unique (UUID)
 * @property salon Salon employeur (relation ManyToOne)
 * @property user Utilisateur employé - doit être de type hairstylist (relation ManyToOne)
 * @property specialties Catégories de services dans lesquelles le staff est spécialisé
 * @property isActive Indique si le staff est actuellement actif (peut recevoir des réservations)
 * @property createdAt Date d'ajout dans l'équipe
 * @property updatedAt Date de dernière modification
 */
@Entity
@Table(
    name = "salon_staff",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_salon_staff_user",
            columnNames = ["salon_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_salon_staff_salon_id", columnList = "salon_id"),
        Index(name = "idx_salon_staff_user_id", columnList = "user_id"),
        Index(name = "idx_salon_staff_active", columnList = "is_active")
    ]
)
data class SalonStaff(
    @Id
    @Column(
        name = "id",
        length = 36,
        columnDefinition = "CHAR(36)",
        nullable = false,
        updatable = false
    )
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "salon_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_salon_staff_salon")
    )
    @JsonIgnore
    var salon: Salon? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_salon_staff_user")
    )
    @JsonIgnore
    var user: User? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "salon_staff_specialties",
        joinColumns = [JoinColumn(name = "staff_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty")
    var specialties: MutableList<ServiceCategory> = mutableListOf(),

    @Column(
        name = "is_active",
        nullable = false
    )
    var isActive: Boolean = true,

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
     * Vérifie si le staff peut effectuer un service donné.
     */
    fun canPerformService(serviceCategory: ServiceCategory): Boolean {
        return specialties.isEmpty() || specialties.contains(serviceCategory)
    }

    /**
     * Ajoute une spécialité au staff.
     */
    fun addSpecialty(category: ServiceCategory) {
        if (!specialties.contains(category)) {
            specialties.add(category)
        }
    }

    /**
     * Retire une spécialité du staff.
     */
    fun removeSpecialty(category: ServiceCategory) {
        specialties.remove(category)
    }

    /**
     * Vérifie si le staff est valide pour la création.
     */
    fun isValid(): Boolean {
        return salon != null &&
                user != null &&
                user?.userType == UserType.hairstylist
    }

    override fun toString(): String {
        return "SalonStaff(id=$id, salonId=${salon?.id}, userId=${user?.id}, specialties=$specialties, isActive=$isActive)"
    }
}