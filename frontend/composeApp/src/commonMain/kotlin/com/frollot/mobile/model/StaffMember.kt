package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * DTO représentant un membre du staff (coiffeur/employé) d'un salon.
 *
 * IMPORTANT : Correspond au StaffResponse du backend.
 *
 * @property id Identifiant unique du staff
 * @property salonId Identifiant du salon
 * @property salonName Nom du salon
 * @property userId Identifiant de l'utilisateur (coiffeur)
 * @property userFirstName Prénom du coiffeur
 * @property userLastName Nom du coiffeur
 * @property userEmail Email du coiffeur
 * @property specialties Liste des catégories de services maîtrisées
 * @property specialtyLabels Libellés français des spécialités
 * @property isActive Statut actif/inactif
 * @property createdAt Date d'ajout dans l'équipe
 */
@Serializable
data class StaffMember(
    val id: String = "",
    val salonId: String = "",
    val salonName: String = "",
    val userId: String = "",
    val userFirstName: String = "",
    val userLastName: String = "",
    val userEmail: String = "",
    /** URL de la photo de profil du coiffeur */
    val userAvatarUrl: String? = null,
    val specialties: List<ServiceCategory> = emptyList(),
    val specialtyLabels: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String? = null
) {
    /**
     * Nom complet du coiffeur.
     */
    val fullName: String
        get() = "$userFirstName $userLastName".trim().ifBlank { userEmail }

    /**
     * Badge de statut pour l'UI.
     */
    val statusLabel: String
        get() = if (isActive) "Actif" else "Inactif"

    /**
     * Badge de statut en emoji.
     */
    val statusEmoji: String
        get() = if (isActive) "✅" else "❌"

    /**
     * Nombre de spécialités.
     */
    val specialtyCount: Int
        get() = specialties.size

    /**
     * Spécialités en format texte (ex: "Coupe, Coloration").
     */
    val specialtiesText: String
        get() = if (specialtyLabels.isEmpty()) {
            "Toutes prestations"
        } else {
            specialtyLabels.joinToString(", ")
        }

    /**
     * Vérifie si le staff peut effectuer un service donné.
     */
    fun canPerformService(serviceCategory: ServiceCategory): Boolean {
        return specialties.isEmpty() || specialties.contains(serviceCategory)
    }

    /**
     * Retourne les emojis des spécialités.
     */
    val specialtyEmojis: String
        get() = specialties.joinToString(" ") { it.getEmoji() }

    /**
     * Vérifie si l'objet est valide (tous les champs requis sont remplis).
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
                salonId.isNotBlank() &&
                userId.isNotBlank() &&
                userEmail.isNotBlank()
    }

    /**
     * Vérifie si le coiffeur a une photo de profil.
     */
    val hasAvatar: Boolean
        get() = !userAvatarUrl.isNullOrBlank()
}

/**
 * DTO pour créer un nouveau membre du staff.
 *
 * IMPORTANT : Correspond au CreateStaffRequest du backend.
 *
 * @property salonId Identifiant du salon
 * @property userId Identifiant de l'utilisateur (doit être hairstylist)
 * @property specialties Liste des catégories de services maîtrisées
 * @property isActive Statut actif/inactif
 */
@Serializable
data class CreateStaffRequest(
    val salonId: String,
    val userId: String,
    val specialties: List<ServiceCategory> = emptyList(),
    val isActive: Boolean = true
) {
    /**
     * Valide les données avant envoi.
     */
    fun validate(): Boolean {
        require(salonId.isNotBlank()) { "L'identifiant du salon est obligatoire" }
        require(userId.isNotBlank()) { "L'identifiant de l'utilisateur est obligatoire" }
        require(specialties.size <= 10) { "Maximum 10 spécialités autorisées" }
        return true
    }
}

/**
 * DTO pour mettre à jour un membre du staff.
 *
 * IMPORTANT : Correspond au UpdateStaffRequest du backend.
 */
@Serializable
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
}

/**
 * DTO pour les statistiques du staff d'un salon.
 *
 * IMPORTANT : Correspond au StaffStatistics du backend.
 */
@Serializable
data class StaffStatistics(
    val totalStaff: Long,
    val activeStaff: Long,
    val inactiveStaff: Long,
    val specialtyDistribution: Map<String, Int>
) {
    /**
     * Pourcentage de staff actif.
     */
    val activePercentage: Int
        get() = if (totalStaff > 0) {
            ((activeStaff.toDouble() / totalStaff) * 100).toInt()
        } else {
            0
        }

    /**
     * Texte résumé des statistiques.
     */
    val summary: String
        get() = "$activeStaff actif${if (activeStaff > 1) "s" else ""} sur $totalStaff"
}
