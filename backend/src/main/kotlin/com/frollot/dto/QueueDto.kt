package com.frollot.dto

import com.frollot.model.QueueEntry
import com.frollot.model.QueueEntryStatus
import java.time.LocalDateTime

/**
 * Requête pour rejoindre la file d'attente d'un salon.
 */
data class JoinQueueRequest(
    val salonId: String,
    val userId: String,
    val serviceId: String? = null,
    val requestedDurationMinutes: Int? = null,
    val notes: String? = null
)

/**
 * Requête pour quitter la file d'attente.
 */
data class LeaveQueueRequest(
    val entryId: String,
    val userId: String? = null
)

/**
 * Représentation d'une entrée de file côté API.
 */
data class QueueEntryResponse(
    val entryId: String,
    val salonId: String,
    val clientId: String,
    val clientName: String,
    val status: QueueEntryStatus,
    val position: Int,
    val estimatedWaitMinutes: Int,
    val requestedServiceId: String?,
    val requestedServiceName: String?,
    val requestedDurationMinutes: Int,
    val joinedAt: LocalDateTime?,
    val calledAt: LocalDateTime?,
    val notes: String?
) {
    companion object {
        fun fromEntity(
            entry: QueueEntry,
            position: Int,
            estimatedWaitMinutes: Int
        ): QueueEntryResponse {
            val client = entry.client!!
            val service = entry.requestedService

            val clientName = listOfNotNull(client.firstName, client.lastName)
                .joinToString(" ")
                .ifBlank { client.email }

            return QueueEntryResponse(
                entryId = entry.id!!,
                salonId = entry.queue?.salon?.id!!,
                clientId = client.id!!,
                clientName = clientName,
                status = entry.status,
                position = position,
                estimatedWaitMinutes = estimatedWaitMinutes,
                requestedServiceId = service?.id,
                requestedServiceName = service?.name,
                requestedDurationMinutes = entry.requestedDurationMinutes,
                joinedAt = entry.joinedAt,
                calledAt = entry.calledAt,
                notes = entry.notes
            )
        }
    }
}

/**
 * Statut global de la file pour un salon.
 */
data class QueueStatusResponse(
    val salonId: String,
    val entries: List<QueueEntryResponse>,
    val estimatedWaitForNew: Int,
    val lastUpdatedAt: LocalDateTime = LocalDateTime.now()
)

