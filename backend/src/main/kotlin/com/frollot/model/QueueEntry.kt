package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entrée dans la file d'attente d'un salon.
 */
@Entity
@Table(
    name = "queue_entries",
    indexes = [
        Index(name = "idx_queue_entry_queue", columnList = "queue_id"),
        Index(name = "idx_queue_entry_client", columnList = "client_id"),
        Index(name = "idx_queue_entry_status", columnList = "status")
    ]
)
data class QueueEntry(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id", nullable = false)
    @JsonIgnore
    var queue: WaitingQueue? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    var client: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @JsonIgnore
    var requestedService: SalonService? = null,

    @Column(name = "requested_duration", nullable = false)
    var requestedDurationMinutes: Int = 30,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: QueueEntryStatus = QueueEntryStatus.waiting,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    var joinedAt: LocalDateTime? = null,

    @Column(name = "called_at")
    var calledAt: LocalDateTime? = null,

    @Column(name = "left_at")
    var leftAt: LocalDateTime? = null
)

enum class QueueEntryStatus {
    waiting,
    called,
    cancelled,
    completed
}

