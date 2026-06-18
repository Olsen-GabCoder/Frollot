package com.frollot.service

import com.frollot.dto.CreateInvitationRequest
import com.frollot.dto.Invitability
import com.frollot.dto.InvitableStylistResponse
import com.frollot.dto.InvitationResponse
import com.frollot.model.*
import com.frollot.repository.SalonRepository
import com.frollot.repository.SalonStaffRepository
import com.frollot.repository.StaffInvitationRepository
import com.frollot.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class InvitationService(
    private val invitationRepository: StaffInvitationRepository,
    private val salonRepository: SalonRepository,
    private val salonStaffRepository: SalonStaffRepository,
    private val userRepository: UserRepository,
    private val salonAuthorizationService: SalonAuthorizationService
) {

    /**
     * Recherche de coiffeurs invitables pour un salon, avec statut d'invitabilité.
     */
    @Transactional(readOnly = true)
    fun searchInvitableStylists(salonId: String, query: String, ownerId: String): List<InvitableStylistResponse> {
        salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }
        salonAuthorizationService.requirePermission(ownerId, salonId, "invitation.search")
        if (query.length < 2) return emptyList()

        val users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            firstName = query, lastName = query, email = query
        ).filter { it.userType == UserType.hairstylist }.take(10)

        return users.map { user ->
            val userId = user.id!!
            val invitability = when {
                salonStaffRepository.existsBySalonIdAndUserId(salonId, userId) -> Invitability.ALREADY_IN_THIS_SALON
                salonStaffRepository.findByUserId(userId).isNotEmpty() -> Invitability.ALREADY_MEMBER_ELSEWHERE
                invitationRepository.existsBySalonIdAndInvitedUserIdAndStatus(salonId, userId, InvitationStatus.PENDING) -> Invitability.ALREADY_INVITED
                else -> Invitability.INVITABLE
            }
            InvitableStylistResponse(
                id = userId,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                email = user.email,
                invitability = invitability
            )
        }
    }

    /**
     * Crée une invitation pour rejoindre l'équipe d'un salon.
     */
    fun createInvitation(salonId: String, request: CreateInvitationRequest, ownerId: String): InvitationResponse {
        // 1. Vérifier le salon
        val salon = salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }

        // 2. Vérifier la permission (owner seul via matrice)
        salonAuthorizationService.requirePermission(ownerId, salonId, "invitation.create")

        // 3. Vérifier le user cible
        val invitedUser = userRepository.findById(request.invitedUserId)
            .orElseThrow { UserNotFoundException(request.invitedUserId) }

        // 4. Vérifier userType == hairstylist
        if (invitedUser.userType != UserType.hairstylist) {
            throw InvalidUserTypeException(request.invitedUserId)
        }

        // 5. EXCLUSIVITÉ : refuse si déjà membre d'un salon (n'importe lequel)
        val existingMemberships = salonStaffRepository.findByUserId(request.invitedUserId)
        if (existingMemberships.isNotEmpty()) {
            throw AlreadyMemberException(request.invitedUserId, existingMemberships.first().salon?.name ?: "")
        }

        // 6. DOUBLE INVITATION : refuse si une PENDING existe déjà pour (salon + user)
        if (invitationRepository.existsBySalonIdAndInvitedUserIdAndStatus(
                salonId, request.invitedUserId, InvitationStatus.PENDING
            )
        ) {
            throw DuplicateInvitationException(salonId, request.invitedUserId)
        }

        // 7. Créer l'invitation
        val invitation = StaffInvitation(
            id = UUID.randomUUID().toString(),
            salon = salon,
            invitedUser = invitedUser,
            invitedEmail = invitedUser.email,
            role = "hairstylist",
            specialties = request.specialties.toMutableList(),
            status = InvitationStatus.PENDING,
            token = UUID.randomUUID().toString(),
            expiresAt = LocalDateTime.now().plusDays(7)
        )

        val saved = invitationRepository.save(invitation)
        println("📨 Invitation créée: ${invitedUser.email} → salon ${salon.name}")

        return InvitationResponse.fromEntity(saved)
    }

    /**
     * Liste les invitations d'un salon (owner).
     * Les PENDING expirées sont marquées EXPIRED à la lecture.
     */
    @Transactional
    fun getSalonInvitations(salonId: String, userId: String): List<InvitationResponse> {
        salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }
        salonAuthorizationService.requirePermission(userId, salonId, "invitation.list")

        val invitations = invitationRepository.findBySalonIdOrderByCreatedAtDesc(salonId)

        // Marquer les expirées
        invitations.filter { it.status == InvitationStatus.PENDING && it.isExpired() }.forEach {
            it.status = InvitationStatus.EXPIRED
            invitationRepository.save(it)
        }

        return invitations.map { InvitationResponse.fromEntity(it) }
    }

    /**
     * Liste les invitations PENDING (non expirées) du user connecté.
     */
    @Transactional(readOnly = true)
    fun getMyInvitations(userId: String): List<InvitationResponse> {
        return invitationRepository
            .findByInvitedUserIdAndStatusOrderByCreatedAtDesc(userId, InvitationStatus.PENDING)
            .filter { !it.isExpired() }
            .map { InvitationResponse.fromEntity(it) }
    }

    /**
     * Accepte une invitation : crée le SalonStaff et passe le statut à ACCEPTED.
     */
    fun acceptInvitation(invitationId: String, userId: String): InvitationResponse {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { InvitationNotFoundException(invitationId) }

        // Vérifier que c'est bien l'invité
        if (invitation.invitedUser?.id != userId) {
            throw UnauthorizedException(userId)
        }

        // Vérifier PENDING
        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationNotPendingException(invitationId, invitation.status)
        }

        // Vérifier expiration
        if (invitation.isExpired()) {
            invitation.status = InvitationStatus.EXPIRED
            invitationRepository.save(invitation)
            throw InvitationExpiredException(invitationId)
        }

        // EXCLUSIVITÉ re-vérifiée : refuser si entre-temps le user est devenu membre d'un salon
        val existingMemberships = salonStaffRepository.findByUserId(userId)
        if (existingMemberships.isNotEmpty()) {
            throw AlreadyMemberException(userId, existingMemberships.first().salon?.name ?: "")
        }

        // Créer le SalonStaff (rattachement réel)
        val staff = SalonStaff(
            salon = invitation.salon,
            user = invitation.invitedUser,
            role = invitation.role,
            specialties = invitation.specialties.toMutableList(),
            isActive = true
        )
        staff.id = UUID.randomUUID().toString()
        salonStaffRepository.save(staff)

        // Passer l'invitation à ACCEPTED
        invitation.status = InvitationStatus.ACCEPTED
        val saved = invitationRepository.save(invitation)

        println("✅ Invitation acceptée: ${invitation.invitedUser?.email} rejoint ${invitation.salon?.name}")

        return InvitationResponse.fromEntity(saved)
    }

    /**
     * Refuse une invitation.
     */
    fun declineInvitation(invitationId: String, userId: String): InvitationResponse {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { InvitationNotFoundException(invitationId) }

        if (invitation.invitedUser?.id != userId) {
            throw UnauthorizedException(userId)
        }
        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationNotPendingException(invitationId, invitation.status)
        }

        invitation.status = InvitationStatus.DECLINED
        val saved = invitationRepository.save(invitation)

        return InvitationResponse.fromEntity(saved)
    }

    /**
     * Annule une invitation (owner).
     */
    fun cancelInvitation(salonId: String, invitationId: String, ownerId: String): InvitationResponse {
        salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }
        salonAuthorizationService.requirePermission(ownerId, salonId, "invitation.cancel")

        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { InvitationNotFoundException(invitationId) }
        if (invitation.salon?.id != salonId) {
            throw InvitationNotFoundException(invitationId)
        }
        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationNotPendingException(invitationId, invitation.status)
        }

        invitation.status = InvitationStatus.CANCELLED
        val saved = invitationRepository.save(invitation)

        return InvitationResponse.fromEntity(saved)
    }

    // ========== EXCEPTIONS ==========

    class SalonNotFoundException(id: String) :
        RuntimeException("Salon '$id' non trouvé")

    class UserNotFoundException(id: String) :
        RuntimeException("Utilisateur '$id' non trouvé")

    class UnauthorizedException(userId: String) :
        RuntimeException("Utilisateur '$userId' non autorisé pour cette opération")

    class InvalidUserTypeException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas un coiffeur (hairstylist)")

    class AlreadyMemberException(userId: String, salonName: String) :
        RuntimeException("Ce coiffeur est déjà membre du salon '$salonName'. Un coiffeur ne peut appartenir qu'à un seul salon.")

    class DuplicateInvitationException(salonId: String, userId: String) :
        RuntimeException("Une invitation en attente existe déjà pour cet utilisateur dans ce salon")

    class InvitationNotFoundException(id: String) :
        RuntimeException("Invitation '$id' non trouvée")

    class InvitationNotPendingException(id: String, status: InvitationStatus) :
        RuntimeException("L'invitation '$id' n'est pas en attente (statut actuel: $status)")

    class InvitationExpiredException(id: String) :
        RuntimeException("L'invitation '$id' a expiré")
}
