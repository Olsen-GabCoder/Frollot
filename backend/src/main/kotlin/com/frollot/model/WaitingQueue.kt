package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * File d'attente associée à un salon pour les clients sans rendez-vous.
 */
@Entity
@Table(
    name = "waiting_queues",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_queue_salon", columnNames = ["salon_id"])
    ]
)
data class WaitingQueue(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false, unique = true)
    var salon: Salon? = null,

    @OneToMany(mappedBy = "queue", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entries: MutableList<QueueEntry> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

