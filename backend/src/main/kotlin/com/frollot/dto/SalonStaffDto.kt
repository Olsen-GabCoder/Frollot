package com.frollot.dto

import com.frollot.model.ServiceCategory
import java.time.LocalDateTime

/**
 * DTO pour créer un nouveau membre du staff.
 *
 * @property salonId Identifiant du salon
 * @property userId Identifiant de l'utilisateur (doit être hairstylist)
 * @property specialties Liste des catégories de services maîtrisées
 * @property isActive Statut actif/inactif (défaut: true)
 */
data class CreateStaffRequest(
    val salonId: String = "",
    val userId: String = "",
    val specialties: List<ServiceCategory> = emptyList(),
    val isActive: Boolean = true
) {
    /**
     * Valide les données de la requête.
     */
    fun validate(): Boolean {
        require(salonId.isNotBlank()) { "L'identifiant du salon est obligatoire" }
        require(userId.isNotBlank()) { "L'identifiant de l'utilisateur est obligatoire" }
        require(specialties.size <= 10) { "Maximum 10 spécialités autorisées" }
        return true
    }

    fun toLogString(): String {
        return "CreateStaffRequest(salonId='***', userId='***', specialties=$specialties, isActive=$isActive)"
    }
}

/**
 * DTO pour la réponse d'un membre du staff.
 *
 * Contient toutes les informations nécessaires pour l'affichage,
 * y compris les données du User associé.
 */
data class StaffResponse(
    val id: String,
    val salonId: String,
    val salonName: String,
    val userId: String,
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    /** URL de la photo de profil du coiffeur */
    val userAvatarUrl: String?,
    val role: String,
    val specialties: List<ServiceCategory>,
    val specialtyLabels: List<String>,
    val isActive: Boolean,
    val createdAt: LocalDateTime?
) {
    /**
     * Nom complet du staff.
     */
    val fullName: String
        get() = "$userFirstName $userLastName"

    /**
     * Badge de statut (Actif/Inactif).
     */
    val statusLabel: String
        get() = if (isActive) "Actif" else "Inactif"

    /**
     * Nombre de spécialités.
     */
    val specialtyCount: Int
        get() = specialties.size

    companion object {
        /**
         * Factory method pour créer une réponse à partir d'une entité SalonStaff.
         */
        fun fromEntity(staff: com.frollot.model.SalonStaff): StaffResponse {
            return StaffResponse(
                id = staff.id ?: "",
                salonId = staff.salon?.id ?: "",
                salonName = staff.salon?.name ?: "",
                userId = staff.user?.id ?: "",
                userFirstName = staff.user?.firstName ?: "",
                userLastName = staff.user?.lastName ?: "",
                userEmail = staff.user?.email ?: "",
                userAvatarUrl = staff.user?.avatarUrl,
                role = staff.role,
                specialties = staff.specialties,
                specialtyLabels = staff.specialties.map { it.getDisplayName() },
                isActive = staff.isActive,
                createdAt = staff.createdAt
            )
        }

        /**
         * Factory method pour créer une liste de réponses.
         */
        fun fromEntities(staffList: List<com.frollot.model.SalonStaff>): List<StaffResponse> {
            return staffList.map { fromEntity(it) }
        }
    }
}

/**
 * DTO pour mettre à jour un membre du staff.
 * Tous les champs sont optionnels (null = ne pas modifier).
 */
data class UpdateStaffRequest(
    val specialties: List<ServiceCategory>? = null,
    val isActive: Boolean? = null
) {
    /**
     * Vérifie si la requête contient des modifications.
     */
    fun hasChanges(): Boolean {
        return specialties != null || isActive != null
    }

    /**
     * Applique les modifications à une entité existante.
     */
    fun applyTo(entity: com.frollot.model.SalonStaff): com.frollot.model.SalonStaff {
        specialties?.let { entity.specialties = it.toMutableList() }
        isActive?.let { entity.isActive = it }
        return entity
    }
}

/**
 * DTO pour les statistiques du staff d'un salon.
 */
data class StaffStatistics(
    val totalStaff: Long,
    val activeStaff: Long,
    val inactiveStaff: Long,
    val specialtyDistribution: Map<ServiceCategory, Int>
)