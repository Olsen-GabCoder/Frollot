package com.frollot.dto

import com.frollot.model.InvitationStatus
import com.frollot.model.ServiceCategory
import com.frollot.model.StaffInvitation
import java.time.LocalDateTime

/**
 * DTO de requête pour créer une invitation d'équipe.
 */
/**
 * Statut d'invitabilité d'un coiffeur pour un salon donné.
 */
enum class Invitability {
    INVITABLE,
    ALREADY_MEMBER_ELSEWHERE,
    ALREADY_INVITED,
    ALREADY_IN_THIS_SALON
}

/**
 * DTO de réponse pour la recherche de coiffeurs invitables.
 */
data class InvitableStylistResponse(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val email: String?,
    val invitability: Invitability
)

data class CreateInvitationRequest(
    val invitedUserId: String,
    val specialties: List<ServiceCategory> = emptyList()
)

/**
 * DTO de réponse pour une invitation d'équipe.
 */
data class InvitationResponse(
    val id: String,
    val salonId: String,
    val salonName: String,
    val salonCoverUrl: String?,
    val invitedUserId: String?,
    val invitedUserName: String?,
    val invitedUserAvatar: String?,
    val invitedEmail: String?,
    val role: String,
    val specialties: List<ServiceCategory>,
    val status: InvitationStatus,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(entity: StaffInvitation): InvitationResponse {
            val user = entity.invitedUser
            return InvitationResponse(
                id = entity.id ?: "",
                salonId = entity.salon?.id ?: "",
                salonName = entity.salon?.name ?: "",
                salonCoverUrl = entity.salon?.coverPhotoUrl,
                invitedUserId = user?.id,
                invitedUserName = if (user != null) "${user.firstName ?: ""} ${user.lastName ?: ""}".trim() else null,
                invitedUserAvatar = user?.avatarUrl,
                invitedEmail = entity.invitedEmail ?: user?.email,
                role = entity.role,
                specialties = entity.specialties.toList(),
                status = if (entity.status == InvitationStatus.PENDING && entity.isExpired()) InvitationStatus.EXPIRED else entity.status,
                expiresAt = entity.expiresAt,
                createdAt = entity.createdAt
            )
        }
    }
}
