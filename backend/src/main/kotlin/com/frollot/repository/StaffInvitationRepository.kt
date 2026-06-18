package com.frollot.repository

import com.frollot.model.InvitationStatus
import com.frollot.model.StaffInvitation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffInvitationRepository : JpaRepository<StaffInvitation, String> {

    fun findBySalonIdOrderByCreatedAtDesc(salonId: String): List<StaffInvitation>

    fun findByInvitedUserIdAndStatusOrderByCreatedAtDesc(
        invitedUserId: String,
        status: InvitationStatus
    ): List<StaffInvitation>

    fun existsBySalonIdAndInvitedUserIdAndStatus(
        salonId: String,
        invitedUserId: String,
        status: InvitationStatus
    ): Boolean
}
