package com.frollot.controller

import com.frollot.dto.*
import com.frollot.model.User
import com.frollot.service.VerificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Contrôleur REST pour la gestion de la vérification des utilisateurs et salons.
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
@RestController
@RequestMapping("/api/verification")
@Tag(
    name = "Vérification",
    description = "API de gestion de la vérification des utilisateurs et salons"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class VerificationController(
    private val verificationService: VerificationService
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

        val principal = authentication.principal
        return when (principal) {
            is User -> principal.id!!
            is String -> {
                throw IllegalStateException("Authentification invalide: le principal est une chaîne")
            }
            else -> throw IllegalStateException("Type de principal non supporté: ${principal?.javaClass?.name}")
        }
    }

    // ========== ENDPOINTS DE VÉRIFICATION (H.2) ==========

    /**
     * Demande une vérification pour un utilisateur ou un salon.
     */
    @Operation(
        summary = "Demander une vérification",
        description = "Permet à un utilisateur ou un salon de demander une vérification. L'utilisateur doit être le propriétaire du compte/salon."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Demande de vérification enregistrée avec succès",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé"),
            ApiResponse(responseCode = "409", description = "Entité déjà vérifiée")
        ]
    )
    @PostMapping("/request/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    fun requestVerification(
        @Parameter(description = "Type d'entité ('user' ou 'salon')", required = true)
        @PathVariable entityType: String,
        @Parameter(description = "ID de l'entité à vérifier", required = true)
        @PathVariable entityId: String,
        @Parameter(description = "Détails de la demande de vérification", required = true)
        @Valid @RequestBody request: RequestVerificationRequest
    ): ResponseEntity<Map<String, String>> {
        val currentUserId = getAuthenticatedUserId()
        val message = verificationService.requestVerification(entityType, entityId, request, currentUserId)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    /**
     * Vérifie un utilisateur (admin uniquement).
     */
    @Operation(
        summary = "Vérifier un utilisateur",
        description = "Vérifie un utilisateur avec un type de vérification spécifique. Réservé aux administrateurs."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Utilisateur vérifié avec succès",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé (non administrateur)"),
            ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        ]
    )
    @PutMapping("/users/{userId}/verify")
    @PreAuthorize("hasAuthority('admin')")
    fun verifyUser(
        @Parameter(description = "ID de l'utilisateur à vérifier", required = true)
        @PathVariable userId: String,
        @Parameter(description = "Détails de la vérification", required = true)
        @Valid @RequestBody request: VerifyUserRequest
    ): ResponseEntity<UserResponse> {
        val currentUserId = getAuthenticatedUserId()
        val verifiedUser = verificationService.verifyUser(userId, request, currentUserId)
        return ResponseEntity.ok(verifiedUser)
    }

    /**
     * Vérifie un salon (admin uniquement).
     */
    @Operation(
        summary = "Vérifier un salon",
        description = "Vérifie un salon avec un type de vérification spécifique. Réservé aux administrateurs."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Salon vérifié avec succès",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = SalonResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé (non administrateur)"),
            ApiResponse(responseCode = "404", description = "Salon non trouvé")
        ]
    )
    @PutMapping("/salons/{salonId}/verify")
    @PreAuthorize("hasAuthority('admin')")
    fun verifySalon(
        @Parameter(description = "ID du salon à vérifier", required = true)
        @PathVariable salonId: String,
        @Parameter(description = "Détails de la vérification", required = true)
        @Valid @RequestBody request: VerifySalonRequest
    ): ResponseEntity<SalonResponse> {
        val currentUserId = getAuthenticatedUserId()
        val verifiedSalon = verificationService.verifySalon(salonId, request, currentUserId)
        return ResponseEntity.ok(verifiedSalon)
    }

    /**
     * Révoque la vérification d'un utilisateur ou d'un salon (admin uniquement).
     */
    @Operation(
        summary = "Révoquer une vérification",
        description = "Révoque la vérification d'un utilisateur ou d'un salon. Réservé aux administrateurs."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Vérification révoquée avec succès",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé (non administrateur)"),
            ApiResponse(responseCode = "404", description = "Entité non trouvée")
        ]
    )
    @DeleteMapping("/{entityType}/{entityId}/revoke")
    @PreAuthorize("hasAuthority('admin')")
    fun revokeVerification(
        @Parameter(description = "Type d'entité ('user' ou 'salon')", required = true)
        @PathVariable entityType: String,
        @Parameter(description = "ID de l'entité", required = true)
        @PathVariable entityId: String
    ): ResponseEntity<Map<String, String>> {
        val currentUserId = getAuthenticatedUserId()
        val message = verificationService.revokeVerification(entityType, entityId, currentUserId)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    // ========== GESTION DES ERREURS ==========

    @ExceptionHandler(VerificationService.UserNotFoundException::class)
    fun handleUserNotFoundException(ex: VerificationService.UserNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to "Not Found", "message" to (ex.message ?: "Utilisateur non trouvé")))
    }

    @ExceptionHandler(VerificationService.SalonNotFoundException::class)
    fun handleSalonNotFoundException(ex: VerificationService.SalonNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to "Not Found", "message" to (ex.message ?: "Salon non trouvé")))
    }

    @ExceptionHandler(VerificationService.UnauthorizedVerificationException::class)
    fun handleUnauthorizedVerificationException(ex: VerificationService.UnauthorizedVerificationException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "Forbidden", "message" to (ex.message ?: "Accès refusé")))
    }

    @ExceptionHandler(VerificationService.AlreadyVerifiedException::class)
    fun handleAlreadyVerifiedException(ex: VerificationService.AlreadyVerifiedException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("error" to "Conflict", "message" to (ex.message ?: "Entité déjà vérifiée")))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to "Bad Request", "message" to (ex.message ?: "Requête invalide")))
    }
}

