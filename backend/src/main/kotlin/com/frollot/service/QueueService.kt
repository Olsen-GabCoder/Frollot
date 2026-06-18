package com.frollot.service

import com.frollot.dto.JoinQueueRequest
import com.frollot.dto.LeaveQueueRequest
import com.frollot.dto.QueueEntryResponse
import com.frollot.dto.QueueStatusResponse
import com.frollot.model.QueueEntry
import com.frollot.model.QueueEntryStatus
import com.frollot.model.UserType
import com.frollot.model.WaitingQueue
import com.frollot.repository.QueueEntryRepository
import com.frollot.repository.SalonRepository
import com.frollot.repository.SalonServiceRepository
import com.frollot.repository.UserRepository
import com.frollot.repository.WaitingQueueRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class QueueService(
    private val waitingQueueRepository: WaitingQueueRepository,
    private val queueEntryRepository: QueueEntryRepository,
    private val salonRepository: SalonRepository,
    private val userRepository: UserRepository,
    private val salonServiceRepository: SalonServiceRepository,
    private val emailService: EmailService? = null, // Optionnel pour éviter les erreurs si non configuré
    private val notificationService: NotificationService? = null, // Optionnel pour éviter les erreurs si non configuré
    private val salonAuthorizationService: SalonAuthorizationService
) {

    class QueueNotFoundException(salonId: String) :
        RuntimeException("File d'attente non trouvée pour le salon '$salonId'")

    class QueueEntryNotFoundException(entryId: String) :
        RuntimeException("Entrée de file non trouvée avec l'ID '$entryId'")

    class AlreadyInQueueException : RuntimeException("Vous êtes déjà dans la file d'attente")

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé pour cette action")

    class InvalidQueueOperationException(message: String) : RuntimeException(message)

    @Transactional
    fun joinQueue(request: JoinQueueRequest): QueueEntryResponse {
        val salon = salonRepository.findById(request.salonId)
            .orElseThrow { SalonServiceService.SalonNotFoundException(request.salonId) }

        val client = userRepository.findById(request.userId)
            .orElseThrow { RuntimeException("Utilisateur non trouvé") }

        if (client.userType != UserType.client) {
            throw InvalidQueueOperationException("Seuls les clients peuvent rejoindre la file")
        }

        val queue = waitingQueueRepository.findBySalonId(request.salonId)
            ?: waitingQueueRepository.save(
                WaitingQueue(
                    id = UUID.randomUUID().toString(),
                    salon = salon
                )
            )

        // Vérifier si le client est déjà dans la file (waiting ou called)
        if (queueEntryRepository.existsByQueueIdAndClientIdAndStatusIn(
                queue.id!!,
                client.id!!,
                listOf(QueueEntryStatus.waiting, QueueEntryStatus.called)
            )
        ) {
            throw AlreadyInQueueException()
        }

        val service = request.serviceId?.let { serviceId ->
            val found = salonServiceRepository.findById(serviceId)
                .orElseThrow { SalonServiceService.ServiceNotFoundException(serviceId) }
            if (found.salon?.id != request.salonId) {
                throw InvalidQueueOperationException("Le service ne correspond pas au salon")
            }
            found
        }

        val duration = request.requestedDurationMinutes
            ?: service?.durationMinutes
            ?: 30

        if (duration <= 0) {
            throw InvalidQueueOperationException("La durée demandée doit être positive")
        }

        val entry = QueueEntry(
            id = UUID.randomUUID().toString(),
            queue = queue,
            client = client,
            requestedService = service,
            requestedDurationMinutes = duration,
            status = QueueEntryStatus.waiting,
            notes = request.notes
        )

        val savedEntry = queueEntryRepository.save(entry)

        return toEntryResponse(queue, savedEntry)
    }

    @Transactional
    fun leaveQueue(salonId: String, request: LeaveQueueRequest): QueueEntryResponse {
        val entry = queueEntryRepository.findById(request.entryId)
            .orElseThrow { QueueEntryNotFoundException(request.entryId) }

        val queue = entry.queue ?: throw QueueNotFoundException(salonId)

        if (queue.salon?.id != salonId) {
            throw QueueNotFoundException(salonId)
        }

        request.userId?.let { requesterId ->
            val isOwner = queue.salon?.owner?.id == requesterId
            val isClient = entry.client?.id == requesterId
            if (!isOwner && !isClient) {
                throw UnauthorizedAccessException(requesterId)
            }
        }

        if (entry.status == QueueEntryStatus.completed || entry.status == QueueEntryStatus.cancelled) {
            return toEntryResponse(queue, entry)
        }

        entry.status = QueueEntryStatus.cancelled
        entry.leftAt = LocalDateTime.now()

        val saved = queueEntryRepository.save(entry)
        return toEntryResponse(queue, saved)
    }

    @Transactional(readOnly = true)
    fun getQueueStatus(salonId: String): QueueStatusResponse {
        val queue = waitingQueueRepository.findBySalonId(salonId)
            ?: return QueueStatusResponse(
                salonId = salonId,
                entries = emptyList(),
                estimatedWaitForNew = 0
            )

        val entries = queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(
            queue.id!!,
            listOf(QueueEntryStatus.waiting, QueueEntryStatus.called)
        )

        val mappedEntries = entries.mapIndexed { index, entry ->
            val waitMinutes = calculateWaitForEntry(entries, entry.id!!)
            QueueEntryResponse.fromEntity(
                entry = entry,
                position = index + 1,
                estimatedWaitMinutes = waitMinutes
            )
        }

        val estimatedForNew = entries.sumOf { it.requestedDurationMinutes }

        return QueueStatusResponse(
            salonId = salonId,
            entries = mappedEntries,
            estimatedWaitForNew = estimatedForNew
        )
    }

    @Transactional
    fun callNextClient(salonId: String, userId: String? = null): QueueEntryResponse {
        val queue = waitingQueueRepository.findBySalonId(salonId)
            ?: throw QueueNotFoundException(salonId)

        userId?.let {
            val salonId = queue.salon?.id ?: throw UnauthorizedAccessException(it)
            salonAuthorizationService.requirePermission(it, salonId, "queue.call_next")
        }

        val nextEntry = queueEntryRepository.findFirstByQueueIdAndStatusOrderByJoinedAtAsc(
            queue.id!!,
            QueueEntryStatus.waiting
        ) ?: throw InvalidQueueOperationException("Aucun client en attente")

        nextEntry.status = QueueEntryStatus.called
        nextEntry.calledAt = LocalDateTime.now()

        val saved = queueEntryRepository.save(nextEntry)
        
        // Envoyer notification email si le client approche (position <= 2)
        val activeEntries = queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(
            queue.id!!,
            listOf(QueueEntryStatus.waiting, QueueEntryStatus.called)
        )
        val position = activeEntries.indexOfFirst { it.id == saved.id } + 1
        val estimatedWait = calculateWaitForEntry(activeEntries, saved.id!!)
        if (position <= 2) {
            emailService?.sendQueueNotification(saved, position, estimatedWait)
            notificationService?.sendQueueNotification(saved, position, estimatedWait)
        }
        
        return toEntryResponse(queue, saved)
    }

    private fun toEntryResponse(queue: WaitingQueue, entry: QueueEntry): QueueEntryResponse {
        val activeEntries = queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(
            queue.id!!,
            listOf(QueueEntryStatus.waiting, QueueEntryStatus.called)
        )

        val position = activeEntries.indexOfFirst { it.id == entry.id } + 1
        val waitMinutes = calculateWaitForEntry(activeEntries, entry.id!!)

        return QueueEntryResponse.fromEntity(
            entry = entry,
            position = position.takeIf { it > 0 } ?: activeEntries.size + 1,
            estimatedWaitMinutes = waitMinutes
        )
    }

    private fun calculateWaitForEntry(entries: List<QueueEntry>, targetEntryId: String): Int {
        var total = 0
        for (entry in entries) {
            if (entry.id == targetEntryId) break
            if (entry.status == QueueEntryStatus.waiting) {
                total += entry.requestedDurationMinutes
            }
        }
        return total
    }
}

