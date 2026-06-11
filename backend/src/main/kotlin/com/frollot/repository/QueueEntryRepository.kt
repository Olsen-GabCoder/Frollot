package com.frollot.repository

import com.frollot.model.QueueEntry
import com.frollot.model.QueueEntryStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface QueueEntryRepository : JpaRepository<QueueEntry, String> {

    fun findByQueueIdAndStatusInOrderByJoinedAtAsc(
        queueId: String,
        statuses: List<QueueEntryStatus>
    ): List<QueueEntry>

    fun existsByQueueIdAndClientIdAndStatusIn(
        queueId: String,
        clientId: String,
        statuses: List<QueueEntryStatus>
    ): Boolean

    fun findFirstByQueueIdAndStatusOrderByJoinedAtAsc(
        queueId: String,
        status: QueueEntryStatus
    ): QueueEntry?

    @Query(
        """
        SELECT COALESCE(SUM(q.requestedDurationMinutes), 0)
        FROM QueueEntry q
        WHERE q.queue.id = :queueId
        AND q.status = com.frollot.model.QueueEntryStatus.waiting
        """
    )
    fun sumWaitingDurations(@Param("queueId") queueId: String): Int
}

