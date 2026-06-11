package com.frollot.mobile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class QueueEntryStatus {
    @SerialName("waiting")
    WAITING,

    @SerialName("called")
    CALLED,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("completed")
    COMPLETED;

    val emoji: String
        get() = when (this) {
            WAITING -> "⏳"
            CALLED -> "📣"
            CANCELLED -> "❌"
            COMPLETED -> "✅"
        }

    val label: String
        get() = when (this) {
            WAITING -> "En attente"
            CALLED -> "Appelé"
            CANCELLED -> "Annulé"
            COMPLETED -> "Terminé"
        }
}

@Serializable
data class JoinQueueRequest(
    val salonId: String,
    val userId: String,
    val serviceId: String? = null,
    val requestedDurationMinutes: Int? = null,
    val notes: String? = null
)

@Serializable
data class LeaveQueueRequest(
    val entryId: String,
    val userId: String? = null
)

@Serializable
data class QueueEntryResponse(
    @SerialName("entryId")
    val entryId: String,

    @SerialName("salonId")
    val salonId: String,

    @SerialName("clientId")
    val clientId: String,

    @SerialName("clientName")
    val clientName: String,

    @SerialName("status")
    val status: QueueEntryStatus,

    @SerialName("position")
    val position: Int,

    @SerialName("estimatedWaitMinutes")
    val estimatedWaitMinutes: Int,

    @SerialName("requestedServiceId")
    val requestedServiceId: String? = null,

    @SerialName("requestedServiceName")
    val requestedServiceName: String? = null,

    @SerialName("requestedDurationMinutes")
    val requestedDurationMinutes: Int,

    @SerialName("joinedAt")
    val joinedAt: String? = null,

    @SerialName("calledAt")
    val calledAt: String? = null,

    @SerialName("notes")
    val notes: String? = null
) {
    val badge: String
        get() = "${status.emoji} ${status.label}"
}

@Serializable
data class QueueStatusResponse(
    @SerialName("salonId")
    val salonId: String,

    @SerialName("entries")
    val entries: List<QueueEntryResponse> = emptyList(),

    @SerialName("estimatedWaitForNew")
    val estimatedWaitForNew: Int = 0,

    @SerialName("lastUpdatedAt")
    val lastUpdatedAt: String? = null
) {
    val isEmpty: Boolean get() = entries.isEmpty()
    val activeSize: Int get() = entries.count {
        it.status == QueueEntryStatus.WAITING || it.status == QueueEntryStatus.CALLED
    }
}
