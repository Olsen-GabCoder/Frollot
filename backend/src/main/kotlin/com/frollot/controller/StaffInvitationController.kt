package com.frollot.controller

import com.frollot.dto.CreateInvitationRequest
import com.frollot.dto.InvitableStylistResponse
import com.frollot.dto.InvitationResponse
import com.frollot.model.User
import com.frollot.service.InvitationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@Tag(
    name = "Invitations d'équipe",
    description = "API de gestion des invitations à rejoindre l'équipe d'un salon"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class StaffInvitationController(
    private val invitationService: InvitationService
) {

    private fun getAuthenticatedUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifié")
        }
        return (authentication.principal as User).id!!
    }

    // ========== SEARCH ==========

    @Operation(summary = "Recherche de coiffeurs invitables", description = "Cherche des coiffeurs par nom/email avec statut d'invitabilité pour un salon")
    @GetMapping("/api/salons/{salonId}/staff/search")
    @PreAuthorize("isAuthenticated()")
    fun searchInvitableStylists(
        @PathVariable salonId: String,
        @RequestParam query: String
    ): ResponseEntity<List<InvitableStylistResponse>> {
        val ownerId = getAuthenticatedUserId()
        val results = invitationService.searchInvitableStylists(salonId, query, ownerId)
        return ResponseEntity.ok(results)
    }

    // ========== OWNER ENDPOINTS ==========

    @Operation(summary = "Créer une invitation", description = "Invite un coiffeur à rejoindre l'équipe du salon")
    @PostMapping("/api/salons/{salonId}/invitations")
    @PreAuthorize("isAuthenticated()")
    fun createInvitation(
        @PathVariable salonId: String,
        @Valid @RequestBody request: CreateInvitationRequest
    ): ResponseEntity<InvitationResponse> {
        val ownerId = getAuthenticatedUserId()
        val invitation = invitationService.createInvitation(salonId, request, ownerId)
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation)
    }

    @Operation(summary = "Lister les invitations du salon", description = "Retourne toutes les invitations d'un salon (owner)")
    @GetMapping("/api/salons/{salonId}/invitations")
    @PreAuthorize("isAuthenticated()")
    fun getSalonInvitations(
        @PathVariable salonId: String
    ): ResponseEntity<List<InvitationResponse>> {
        val ownerId = getAuthenticatedUserId()
        val invitations = invitationService.getSalonInvitations(salonId, ownerId)
        return ResponseEntity.ok(invitations)
    }

    @Operation(summary = "Annuler une invitation", description = "Annule une invitation en attente (owner)")
    @DeleteMapping("/api/salons/{salonId}/invitations/{invitationId}")
    @PreAuthorize("isAuthenticated()")
    fun cancelInvitation(
        @PathVariable salonId: String,
        @PathVariable invitationId: String
    ): ResponseEntity<InvitationResponse> {
        val ownerId = getAuthenticatedUserId()
        val invitation = invitationService.cancelInvitation(salonId, invitationId, ownerId)
        return ResponseEntity.ok(invitation)
    }

    // ========== INVITED USER ENDPOINTS ==========

    @Operation(summary = "Mes invitations", description = "Retourne les invitations en attente du coiffeur connecté")
    @GetMapping("/api/users/me/invitations")
    @PreAuthorize("isAuthenticated()")
    fun getMyInvitations(): ResponseEntity<List<InvitationResponse>> {
        val userId = getAuthenticatedUserId()
        val invitations = invitationService.getMyInvitations(userId)
        return ResponseEntity.ok(invitations)
    }

    @Operation(summary = "Accepter une invitation", description = "Accepte une invitation et rejoint l'équipe du salon")
    @PostMapping("/api/invitations/{invitationId}/accept")
    @PreAuthorize("isAuthenticated()")
    fun acceptInvitation(
        @PathVariable invitationId: String
    ): ResponseEntity<InvitationResponse> {
        val userId = getAuthenticatedUserId()
        val invitation = invitationService.acceptInvitation(invitationId, userId)
        return ResponseEntity.ok(invitation)
    }

    @Operation(summary = "Refuser une invitation", description = "Refuse une invitation d'équipe")
    @PostMapping("/api/invitations/{invitationId}/decline")
    @PreAuthorize("isAuthenticated()")
    fun declineInvitation(
        @PathVariable invitationId: String
    ): ResponseEntity<InvitationResponse> {
        val userId = getAuthenticatedUserId()
        val invitation = invitationService.declineInvitation(invitationId, userId)
        return ResponseEntity.ok(invitation)
    }

    // ========== EXCEPTION HANDLERS ==========

    @ExceptionHandler(InvitationService.SalonNotFoundException::class, InvitationService.UserNotFoundException::class, InvitationService.InvitationNotFoundException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to "Not Found", "message" to (ex.message ?: "Resource not found")))
    }

    @ExceptionHandler(InvitationService.UnauthorizedException::class)
    fun handleForbidden(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "Forbidden", "message" to (ex.message ?: "Access denied")))
    }

    @ExceptionHandler(InvitationService.DuplicateInvitationException::class, InvitationService.AlreadyMemberException::class)
    fun handleConflict(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(mapOf("error" to "Conflict", "message" to (ex.message ?: "Conflict")))
    }

    @ExceptionHandler(InvitationService.InvalidUserTypeException::class, InvitationService.InvitationNotPendingException::class, InvitationService.InvitationExpiredException::class)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to "Bad Request", "message" to (ex.message ?: "Invalid request")))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleUnauthorized(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to "Unauthorized", "message" to (ex.message ?: "Authentication required")))
    }
}
