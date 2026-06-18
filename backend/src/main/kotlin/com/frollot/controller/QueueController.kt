package com.frollot.controller

import com.frollot.dto.JoinQueueRequest
import com.frollot.dto.LeaveQueueRequest
import com.frollot.dto.QueueEntryResponse
import com.frollot.dto.QueueStatusResponse
import com.frollot.model.User
import com.frollot.service.QueueService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/salons/{salonId}/queue")
@Tag(name = "File d'attente", description = "Gestion des clients sans rendez-vous")
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class QueueController(
    private val queueService: QueueService
) {

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Récupère l'ID de l'utilisateur authentifié depuis le SecurityContext.
     */
    private fun getAuthenticatedUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifié")
        }
        return (authentication.principal as User).id!!
    }

    // ========== ENDPOINTS ==========

    @Operation(summary = "Rejoindre la file d'attente")
    @PostMapping("/join")
    @PreAuthorize("hasRole('CLIENT')")
    fun joinQueue(
        @PathVariable salonId: String,
        @Valid @RequestBody request: JoinQueueRequest
    ): ResponseEntity<QueueEntryResponse> {
        // Récupérer l'ID de l'utilisateur authentifié
        val authenticatedUserId = getAuthenticatedUserId()

        // Mettre à jour la requête avec l'ID authentifié
        val updatedRequest = request.copy(userId = authenticatedUserId, salonId = salonId)

        val entry = queueService.joinQueue(updatedRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(entry)
    }

    @Operation(summary = "Quitter la file d'attente")
    @PostMapping("/leave")
    @PreAuthorize("isAuthenticated()")
    fun leaveQueue(
        @PathVariable salonId: String,
        @Valid @RequestBody request: LeaveQueueRequest
    ): ResponseEntity<QueueEntryResponse> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Mettre à jour la requête avec l'ID authentifié
        val updatedRequest = request.copy(userId = authenticatedUserId)

        val entry = queueService.leaveQueue(salonId, updatedRequest)
        return ResponseEntity.ok(entry)
    }

    @Operation(summary = "Statut complet de la file")
    @GetMapping
    fun getStatus(
        @PathVariable salonId: String
    ): ResponseEntity<QueueStatusResponse> {
        val status = queueService.getQueueStatus(salonId)
        return ResponseEntity.ok(status)
    }

    @Operation(summary = "Appeler le prochain client (owner/manager/coiffeur)")
    @PostMapping("/call-next")
    @PreAuthorize("isAuthenticated()")
    fun callNext(
        @PathVariable salonId: String
    ): ResponseEntity<QueueEntryResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val entry = queueService.callNextClient(salonId, authenticatedUserId)
        return ResponseEntity.ok(entry)
    }

    // ========== GESTION DES ERREURS GLOBALES ==========

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "Bad Request",
                "message" to (ex.message ?: "Requête invalide")
            )
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentification requise")
            )
        )
    }

    @ExceptionHandler(
        QueueService.QueueNotFoundException::class,
        QueueService.QueueEntryNotFoundException::class
    )
    fun handleNotFound(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Ressource introuvable")
            )
        )
    }

    @ExceptionHandler(
        QueueService.AlreadyInQueueException::class,
        QueueService.InvalidQueueOperationException::class
    )
    fun handleConflict(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            mapOf(
                "error" to "Conflict",
                "message" to (ex.message ?: "Opération impossible")
            )
        )
    }

    @ExceptionHandler(QueueService.UnauthorizedAccessException::class)
    fun handleForbidden(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Action non autorisée")
            )
        )
    }
}