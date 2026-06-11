package com.frollot.controller

import com.frollot.dto.CreateStaffRequest
import com.frollot.dto.StaffResponse
import com.frollot.dto.StaffStatistics
import com.frollot.dto.UpdateStaffRequest
import com.frollot.model.ServiceCategory
import com.frollot.model.User
import com.frollot.service.SalonStaffService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
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
 * Contrôleur REST pour la gestion du staff (équipe) des salons.
 *
 * Expose une API complète CRUD pour gérer les coiffeurs/employés.
 *
 * Tous les endpoints sont préfixés par : /api/salons/{salonId}/staff
 */
@RestController
@RequestMapping("/api/salons/{salonId}/staff")
@Tag(
    name = "Gestion du Staff",
    description = "API de gestion des équipes des salons de coiffure"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:9090", "http://10.0.2.2:9090"],
    allowCredentials = "true"
)
class SalonStaffController(
    private val salonStaffService: SalonStaffService
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

    // ========== ENDPOINTS DE CRÉATION ==========

    /**
     * Ajoute un nouveau membre au staff d'un salon.
     */
    @Operation(
        summary = "Ajouter un membre au staff",
        description = "Ajoute un coiffeur (hairstylist) à l'équipe d'un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Membre ajouté avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon ou utilisateur non trouvé"
            ),
            ApiResponse(
                responseCode = "409",
                description = "L'utilisateur fait déjà partie du staff"
            )
        ]
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun addStaffMember(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String,

        @Parameter(description = "Données du membre à ajouter", required = true)
        @Valid @RequestBody request: CreateStaffRequest
    ): ResponseEntity<StaffResponse> {
        val authenticatedUserId = getAuthenticatedUserId()

        // S'assurer que le salonId dans le chemin correspond à celui de la requête
        val validatedRequest = request.copy(salonId = salonId)

        val staff = salonStaffService.addStaffMember(validatedRequest, authenticatedUserId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/salons/$salonId/staff/${staff.id}")
            .body(staff)
    }

    // ========== ENDPOINTS DE LECTURE ==========

    /**
     * Récupère tous les membres du staff d'un salon.
     */
    @Operation(
        summary = "Lister les membres du staff",
        description = "Retourne la liste complète de l'équipe d'un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste récupérée avec succès",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = StaffResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon non trouvé"
            )
        ]
    )
    @GetMapping
    fun getAllStaff(
        @PathVariable salonId: String
    ): ResponseEntity<List<StaffResponse>> {
        val staff = salonStaffService.getStaffBySalon(salonId)
        return ResponseEntity.ok(staff)
    }

    /**
     * Récupère les membres actifs du staff d'un salon.
     */
    @Operation(
        summary = "Lister les membres actifs",
        description = "Retourne uniquement les membres actifs de l'équipe"
    )
    @GetMapping("/active")
    fun getActiveStaff(
        @PathVariable salonId: String
    ): ResponseEntity<List<StaffResponse>> {
        val staff = salonStaffService.getActiveStaffBySalon(salonId)
        return ResponseEntity.ok(staff)
    }

    /**
     * Récupère un membre du staff par son ID.
     */
    @Operation(
        summary = "Récupérer un membre par ID",
        description = "Retourne les détails d'un membre spécifique du staff"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Membre trouvé",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Membre non trouvé"
            )
        ]
    )
    @GetMapping("/{staffId}")
    fun getStaffById(
        @PathVariable salonId: String,
        @PathVariable staffId: String
    ): ResponseEntity<StaffResponse> {
        val staff = salonStaffService.getStaffById(staffId)

        // Vérifier que le staff appartient bien au salon spécifié
        if (staff.salonId != salonId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return ResponseEntity.ok(staff)
    }

    /**
     * Filtre les membres du staff par spécialité.
     */
    @Operation(
        summary = "Filtrer par spécialité",
        description = "Retourne les membres ayant une spécialité spécifique"
    )
    @GetMapping("/specialties/{specialty}")
    fun getStaffBySpecialty(
        @PathVariable salonId: String,
        @PathVariable specialty: ServiceCategory
    ): ResponseEntity<List<StaffResponse>> {
        val staff = salonStaffService.getStaffBySpecialty(salonId, specialty)
        return ResponseEntity.ok(staff)
    }

    // ========== ENDPOINTS DE MISE À JOUR ==========

    /**
     * Met à jour un membre du staff.
     */
    @Operation(
        summary = "Mettre à jour un membre",
        description = "Met à jour les spécialités ou le statut d'un membre"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Membre mis à jour avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Membre non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Non autorisé"
            )
        ]
    )
    @PutMapping("/{staffId}")
    @PreAuthorize("isAuthenticated()")
    fun updateStaff(
        @PathVariable salonId: String,
        @PathVariable staffId: String,
        @Valid @RequestBody request: UpdateStaffRequest
    ): ResponseEntity<StaffResponse> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Vérifier que le staff appartient bien au salon
        val existingStaff = salonStaffService.getStaffById(staffId)
        if (existingStaff.salonId != salonId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val updatedStaff = salonStaffService.updateStaff(staffId, request, authenticatedUserId)
        return ResponseEntity.ok(updatedStaff)
    }

    // ========== ENDPOINTS DE SUPPRESSION ==========

    /**
     * Supprime un membre du staff.
     */
    @Operation(
        summary = "Supprimer un membre",
        description = "Retire définitivement un membre de l'équipe"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Membre supprimé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Membre non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Non autorisé"
            )
        ]
    )
    @DeleteMapping("/{staffId}")
    @PreAuthorize("isAuthenticated()")
    fun removeStaff(
        @PathVariable salonId: String,
        @PathVariable staffId: String
    ): ResponseEntity<Void> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Vérifier que le staff appartient bien au salon
        val existingStaff = salonStaffService.getStaffById(staffId)
        if (existingStaff.salonId != salonId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        salonStaffService.removeStaff(staffId, authenticatedUserId)
        return ResponseEntity.noContent().build()
    }

    // ========== ENDPOINTS DE STATISTIQUES ==========

    /**
     * Récupère les statistiques du staff d'un salon.
     */
    @Operation(
        summary = "Récupérer les statistiques",
        description = "Retourne des statistiques sur l'équipe du salon"
    )
    @GetMapping("/statistics")
    fun getStaffStatistics(
        @PathVariable salonId: String
    ): ResponseEntity<StaffStatistics> {
        val statistics = salonStaffService.getStaffStatistics(salonId)
        return ResponseEntity.ok(statistics)
    }

    // ========== GESTION DES ERREURS GLOBALES ==========

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Validation error",
                "message" to (ex.message ?: "Invalid request data")
            ))
    }

    @ExceptionHandler(
        SalonStaffService.StaffNotFoundException::class
    )
    fun handleNotFoundException(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Resource not found")
            ))
    }

    @ExceptionHandler(SalonStaffService.StaffAlreadyExistsException::class)
    fun handleDuplicateException(ex: SalonStaffService.StaffAlreadyExistsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf(
                "error" to "Conflict",
                "message" to (ex.message ?: "Resource already exists")
            ))
    }

    @ExceptionHandler(
        SalonStaffService.UnauthorizedAccessException::class,
        SalonStaffService.InvalidUserTypeException::class
    )
    fun handleUnauthorizedException(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Access denied")
            ))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentication required")
            ))
    }
}