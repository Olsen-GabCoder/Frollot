package com.frollot.service

import com.frollot.dto.CreateStaffRequest
import com.frollot.dto.StaffResponse
import com.frollot.dto.StaffStatistics
import com.frollot.dto.UpdateStaffRequest
import com.frollot.model.SalonStaff
import com.frollot.model.ServiceCategory
import com.frollot.model.UserType
import com.frollot.repository.SalonRepository
import com.frollot.repository.SalonStaffRepository
import com.frollot.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service de gestion du staff (équipe) des salons.
 *
 * Orchestre la logique métier pour :
 * - Ajouter/retirer des coiffeurs d'un salon
 * - Gérer les spécialités
 * - Activer/désactiver des membres
 */
@Service
@Transactional
class SalonStaffService(
    private val salonStaffRepository: SalonStaffRepository,
    private val salonRepository: SalonRepository,
    private val userRepository: UserRepository
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class StaffNotFoundException(staffId: String) :
        RuntimeException("Staff avec ID '$staffId' non trouvé")

    class StaffAlreadyExistsException(salonId: String, userId: String) :
        RuntimeException("L'utilisateur '$userId' fait déjà partie du staff du salon '$salonId'")

    class InvalidUserTypeException(userId: String) :
        RuntimeException("L'utilisateur '$userId' doit être de type 'hairstylist' pour rejoindre un staff")

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé à gérer le staff")

    // ========== OPÉRATIONS CRUD ==========

    /**
     * Ajoute un nouveau membre au staff d'un salon.
     */
    @Transactional
    fun addStaffMember(request: CreateStaffRequest, ownerId: String? = null): StaffResponse {
        // 1. Validation de la requête
        request.validate()

        // 2. Vérification de l'existence du salon
        val salon = salonRepository.findById(request.salonId)
            .orElseThrow { SalonServiceService.SalonNotFoundException(request.salonId) }

        // 3. Vérification des autorisations (si ownerId est fourni)
        ownerId?.let {
            if (salon.owner?.id != ownerId) {
                throw UnauthorizedAccessException(ownerId)
            }
        }

        // 4. Vérification de l'existence de l'utilisateur
        val user = userRepository.findById(request.userId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '${request.userId}' non trouvé") }

        // 5. Vérification du type d'utilisateur
        if (user.userType != UserType.hairstylist) {
            throw InvalidUserTypeException(request.userId)
        }

        // 6. Vérification si l'utilisateur ne fait pas déjà partie du staff
        if (salonStaffRepository.existsBySalonIdAndUserId(request.salonId, request.userId)) {
            throw StaffAlreadyExistsException(request.salonId, request.userId)
        }

        // 7. Création de l'entité
        val staff = SalonStaff(
            salon = salon,
            user = user,
            specialties = request.specialties.toMutableList(),
            isActive = request.isActive
        )
        staff.id = UUID.randomUUID().toString()

        // 8. Validation de l'entité
        if (!staff.isValid()) {
            throw IllegalArgumentException("Les données du staff sont invalides")
        }

        // 9. Persistance
        val savedStaff = salonStaffRepository.save(staff)

        // 10. Log d'audit
        println("✅ Staff ajouté: ${user.firstName} ${user.lastName} dans le salon ${salon.name}")

        return StaffResponse.fromEntity(savedStaff)
    }

    /**
     * Récupère tous les membres du staff d'un salon.
     */
    @Transactional(readOnly = true)
    fun getStaffBySalon(salonId: String): List<StaffResponse> {
        val salon = salonRepository.findById(salonId)
            .orElseThrow { SalonServiceService.SalonNotFoundException(salonId) }

        val staffList = salonStaffRepository.findBySalonId(salonId)
        val responses = StaffResponse.fromEntities(staffList).toMutableList()

        // Include the salon owner at the top if not already in the staff table
        val ownerId = salon.owner?.id
        if (ownerId != null && responses.none { it.userId == ownerId }) {
            val owner = salon.owner!!
            responses.add(0, StaffResponse(
                id = "owner-$salonId",
                salonId = salonId,
                salonName = salon.name ?: "",
                userId = ownerId,
                userFirstName = owner.firstName ?: "",
                userLastName = owner.lastName ?: "",
                userEmail = owner.email ?: "",
                userAvatarUrl = owner.avatarUrl,
                role = "owner",
                specialties = emptyList(),
                specialtyLabels = emptyList(),
                isActive = true,
                createdAt = salon.createdAt
            ))
        }

        return responses
    }

    /**
     * Récupère les membres actifs du staff d'un salon.
     */
    @Transactional(readOnly = true)
    fun getActiveStaffBySalon(salonId: String): List<StaffResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonServiceService.SalonNotFoundException(salonId)
        }

        val staffList = salonStaffRepository.findActiveBySalonId(salonId)
        return StaffResponse.fromEntities(staffList)
    }

    /**
     * Récupère un membre du staff par son ID.
     */
    @Transactional(readOnly = true)
    fun getStaffById(staffId: String): StaffResponse {
        val staff = salonStaffRepository.findById(staffId)
            .orElseThrow { StaffNotFoundException(staffId) }

        return StaffResponse.fromEntity(staff)
    }

    /**
     * Récupère les membres du staff ayant une spécialité donnée.
     */
    @Transactional(readOnly = true)
    fun getStaffBySpecialty(salonId: String, specialty: ServiceCategory): List<StaffResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonServiceService.SalonNotFoundException(salonId)
        }

        val staffList = salonStaffRepository.findBySalonIdAndSpecialty(salonId, specialty)
        return StaffResponse.fromEntities(staffList)
    }

    /**
     * Met à jour un membre du staff.
     */
    @Transactional
    fun updateStaff(
        staffId: String,
        request: UpdateStaffRequest,
        ownerId: String? = null
    ): StaffResponse {
        // 1. Récupération du staff existant
        val staff = salonStaffRepository.findById(staffId)
            .orElseThrow { StaffNotFoundException(staffId) }

        // 2. Vérification des autorisations
        ownerId?.let {
            if (staff.salon?.owner?.id != ownerId) {
                throw UnauthorizedAccessException(ownerId)
            }
        }

        // 3. Application des modifications
        val updatedStaff = request.applyTo(staff)

        // 4. Persistance
        val savedStaff = salonStaffRepository.save(updatedStaff)

        // 5. Log d'audit
        println("✏️ Staff mis à jour: ${savedStaff.user?.firstName} ${savedStaff.user?.lastName}")

        return StaffResponse.fromEntity(savedStaff)
    }

    /**
     * Supprime un membre du staff.
     */
    @Transactional
    fun removeStaff(staffId: String, ownerId: String? = null) {
        val staff = salonStaffRepository.findById(staffId)
            .orElseThrow { StaffNotFoundException(staffId) }

        // Vérification des autorisations
        ownerId?.let {
            if (staff.salon?.owner?.id != ownerId) {
                throw UnauthorizedAccessException(ownerId)
            }
        }

        salonStaffRepository.delete(staff)

        println("🗑️ Staff supprimé: ${staff.user?.firstName} ${staff.user?.lastName}")
    }

    /**
     * Récupère les statistiques du staff d'un salon.
     */
    @Transactional(readOnly = true)
    fun getStaffStatistics(salonId: String): StaffStatistics {
        if (!salonRepository.existsById(salonId)) {
            throw SalonServiceService.SalonNotFoundException(salonId)
        }

        val totalStaff = salonStaffRepository.countBySalonId(salonId)
        val activeStaff = salonStaffRepository.countBySalonIdAndIsActive(salonId, true)
        val inactiveStaff = totalStaff - activeStaff

        val allStaff = salonStaffRepository.findBySalonId(salonId)
        val specialtyDistribution = ServiceCategory.entries.associateWith { category ->
            allStaff.count { staff -> staff.specialties.contains(category) }
        }

        return StaffStatistics(
            totalStaff = totalStaff,
            activeStaff = activeStaff,
            inactiveStaff = inactiveStaff,
            specialtyDistribution = specialtyDistribution
        )
    }

    /**
     * Vérifie si un utilisateur peut gérer le staff d'un salon.
     */
    fun canUserManageStaff(salonId: String, userId: String): Boolean {
        return try {
            val salon = salonRepository.findById(salonId).orElse(null)
            salon?.owner?.id == userId || salon?.owner?.userType == UserType.admin
        } catch (e: Exception) {
            false
        }
    }
}