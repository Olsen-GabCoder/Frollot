package com.frollot.controller

import com.frollot.dto.*
import com.frollot.model.*
import com.frollot.service.ModerationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Contrôleur REST pour la gestion de la modération et des signalements.
 * Phase H.1 - Signalement de Contenu
 *
 * Expose une API pour :
 * - Signaler du contenu inapproprié (utilisateurs)
 * - Consulter les signalements (admin)
 * - Traiter les signalements (admin)
 */
@RestController
@RequestMapping("/api/social/reports")
@Tag(
    name = "Modération",
    description = "API de gestion des signalements et de la modération"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class ModerationController(
    private val moderationService: ModerationService
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

    // ========== ENDPOINTS DE SIGNALEMENT ==========

    /**
     * Crée un nouveau signalement de contenu.
     * Phase H.1 - Signalement de Contenu
     */
    @Operation(
        summary = "Signaler du contenu",
        description = "Permet à un utilisateur authentifié de signaler du contenu inapproprié (post, commentaire, utilisateur, salon)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Signalement créé avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReportResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide (ex: déjà signalé, entité inexistante)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            )
        ]
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun reportContent(
        @Parameter(description = "Données du signalement", required = true)
        @Valid @RequestBody request: CreateReportRequest
    ): ResponseEntity<ReportResponse> {
        val reporterId = getAuthenticatedUserId()
        val report = moderationService.reportContent(request, reporterId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/social/reports/${report.id}")
            .body(report)
    }

    /**
     * Récupère les signalements d'un utilisateur.
     * Phase H.1 - Signalement de Contenu
     */
    @Operation(
        summary = "Récupérer mes signalements",
        description = "Retourne tous les signalements créés par l'utilisateur authentifié"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des signalements récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            )
        ]
    )
    @GetMapping("/my-reports")
    @PreAuthorize("isAuthenticated()")
    fun getMyReports(
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ReportResponse>> {
        val userId = getAuthenticatedUserId()
        val pageable: Pageable = PageRequest.of(page, size)
        val reports = moderationService.getReportsByUser(userId, pageable)
        return ResponseEntity.ok(reports)
    }

    // ========== ENDPOINTS ADMIN ==========

    /**
     * Récupère tous les signalements (admin uniquement).
     * Phase H.1 - Signalement de Contenu
     */
    @Operation(
        summary = "Récupérer tous les signalements",
        description = "Retourne tous les signalements avec pagination. Admin uniquement."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des signalements récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas administrateur)"
            )
        ]
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getReports(
        @Parameter(description = "Filtre par statut (optionnel)")
        @RequestParam(required = false) status: ReportStatus?,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ReportResponse>> {
        val adminId = getAuthenticatedUserId()
        val pageable: Pageable = PageRequest.of(page, size)
        val reports = moderationService.getReports(status, pageable, adminId)
        return ResponseEntity.ok(reports)
    }

    /**
     * Récupère les signalements en attente (admin uniquement).
     * Phase H.1 - Signalement de Contenu
     */
    @Operation(
        summary = "Récupérer les signalements en attente",
        description = "Retourne tous les signalements avec statut PENDING. Admin uniquement."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des signalements en attente récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas administrateur)"
            )
        ]
    )
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    fun getPendingReports(
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ReportResponse>> {
        val adminId = getAuthenticatedUserId()
        val pageable: Pageable = PageRequest.of(page, size)
        val reports = moderationService.getPendingReports(pageable, adminId)
        return ResponseEntity.ok(reports)
    }

    /**
     * Traite un signalement (admin uniquement).
     * Phase H.1 - Signalement de Contenu
     */
    @Operation(
        summary = "Traiter un signalement",
        description = "Permet à un administrateur de traiter un signalement (changer son statut). Admin uniquement."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Signalement traité avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReportResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas administrateur)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Signalement non trouvé"
            )
        ]
    )
    @PutMapping("/{reportId}/handle")
    @PreAuthorize("isAuthenticated()")
    fun handleReport(
        @Parameter(description = "ID du signalement", required = true)
        @PathVariable reportId: String,
        @Parameter(description = "Données de traitement", required = true)
        @Valid @RequestBody request: HandleReportRequest
    ): ResponseEntity<ReportResponse> {
        val adminId = getAuthenticatedUserId()
        val report = moderationService.handleReport(reportId, request, adminId)
        return ResponseEntity.ok(report)
    }

    // ========== ENDPOINTS DE MODÉRATION (H.3) ==========

    /**
     * Modère un contenu (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Operation(
        summary = "Modérer un contenu",
        description = "Permet à un administrateur de modérer un contenu (masquer, supprimer, avertir). Admin uniquement."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Contenu modéré avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ModerationActionResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé (pas administrateur)"),
            ApiResponse(responseCode = "404", description = "Contenu non trouvé")
        ]
    )
    @PostMapping("/moderate")
    @PreAuthorize("hasAuthority('admin')")
    fun moderateContent(
        @Parameter(description = "Données de modération", required = true)
        @Valid @RequestBody request: ModerateContentRequest
    ): ResponseEntity<ModerationActionResponse> {
        val adminId = getAuthenticatedUserId()
        val action = moderationService.moderateContent(request, adminId)
        return ResponseEntity.ok(action)
    }

    /**
     * Fait appel d'une action de modération.
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Operation(
        summary = "Faire appel d'une modération",
        description = "Permet à l'auteur d'un contenu modéré de faire appel de la décision de modération."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Appel créé avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ModerationActionResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé"),
            ApiResponse(responseCode = "404", description = "Action de modération non trouvée")
        ]
    )
    @PostMapping("/appeal")
    @PreAuthorize("isAuthenticated()")
    fun appealModeration(
        @Parameter(description = "Données de l'appel", required = true)
        @Valid @RequestBody request: AppealModerationRequest
    ): ResponseEntity<ModerationActionResponse> {
        val userId = getAuthenticatedUserId()
        val action = moderationService.appealModeration(request, userId)
        return ResponseEntity.ok(action)
    }

    /**
     * Traite un appel de modération (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Operation(
        summary = "Traiter un appel de modération",
        description = "Permet à un administrateur de traiter un appel de modération (approuver ou rejeter). Admin uniquement."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Appel traité avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ModerationActionResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "403", description = "Accès refusé (pas administrateur)"),
            ApiResponse(responseCode = "404", description = "Action de modération non trouvée")
        ]
    )
    @PutMapping("/appeals/{moderationActionId}/handle")
    @PreAuthorize("hasAuthority('admin')")
    fun handleAppeal(
        @Parameter(description = "ID de l'action de modération", required = true)
        @PathVariable moderationActionId: String,
        @Parameter(description = "Données de traitement de l'appel", required = true)
        @Valid @RequestBody request: HandleAppealRequest
    ): ResponseEntity<ModerationActionResponse> {
        val adminId = getAuthenticatedUserId()
        val action = moderationService.handleAppeal(moderationActionId, request, adminId)
        return ResponseEntity.ok(action)
    }

    /**
     * Récupère les actions de modération pour une entité spécifique (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Operation(
        summary = "Récupérer les actions de modération d'une entité",
        description = "Récupère toutes les actions de modération effectuées sur une entité spécifique. Admin uniquement."
    )
    @GetMapping("/actions/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('admin')")
    fun getModerationActionsByEntity(
        @Parameter(description = "Type d'entité", required = true)
        @PathVariable entityType: ReportedEntityType,
        @Parameter(description = "ID de l'entité", required = true)
        @PathVariable entityId: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ModerationActionResponse>> {
        val pageable = PageRequest.of(page, size)
        val actions = moderationService.getModerationActionsByEntity(entityType, entityId, pageable)
        return ResponseEntity.ok(actions)
    }

    /**
     * Récupère tous les appels en attente (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Operation(
        summary = "Récupérer les appels en attente",
        description = "Récupère tous les appels de modération en attente de traitement. Admin uniquement."
    )
    @GetMapping("/appeals/pending")
    @PreAuthorize("hasAuthority('admin')")
    fun getPendingAppeals(
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ModerationActionResponse>> {
        val pageable = PageRequest.of(page, size)
        val appeals = moderationService.getPendingAppeals(pageable)
        return ResponseEntity.ok(appeals)
    }

    // ========== GESTION DES ERREURS ==========

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentication required")
            ))
    }

    @ExceptionHandler(ModerationService.ReportNotFoundException::class)
    fun handleReportNotFoundException(ex: ModerationService.ReportNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Signalement non trouvé")
            ))
    }

    @ExceptionHandler(ModerationService.ReportAlreadyExistsException::class)
    fun handleReportAlreadyExistsException(ex: ModerationService.ReportAlreadyExistsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Bad Request",
                "message" to (ex.message ?: "Signalement déjà existant")
            ))
    }

    @ExceptionHandler(ModerationService.UnauthorizedAccessException::class)
    fun handleUnauthorizedException(ex: ModerationService.UnauthorizedAccessException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Accès refusé")
            ))
    }

    @ExceptionHandler(ModerationService.EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: ModerationService.EntityNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Entité non trouvée")
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Validation error",
                "message" to (ex.message ?: "Données invalides")
            ))
    }
}

