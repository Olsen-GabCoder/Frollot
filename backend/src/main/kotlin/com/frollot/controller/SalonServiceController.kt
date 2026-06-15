package com.frollot.controller

import com.frollot.dto.CreateServiceRequest
import com.frollot.dto.ServiceResponse
import com.frollot.dto.UpdateServiceRequest
import com.frollot.model.ServiceCategory
import com.frollot.model.User
import com.frollot.service.SalonServiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Contrôleur REST pour la gestion des prestations de services des salons.
 *
 * Expose une API complète CRUD avec recherche, filtrage et pagination.
 *
 * Tous les endpoints sont préfixés par : /api/salons/{salonId}/services
 *
 * @property salonServiceService Service de logique métier pour les prestations
 */
@RestController
@RequestMapping("/api/salons/{salonId}/services")
@Tag(
    name = "Catalogue des Services",
    description = "API de gestion des prestations de services des salons de coiffure"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090"],
    allowCredentials = "true"
)
class SalonServiceController(
    private val salonServiceService: SalonServiceService
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
     * Crée une nouvelle prestation de service pour un salon.
     */
    @Operation(
        summary = "Créer une nouvelle prestation",
        description = "Crée une prestation de service dans le catalogue d'un salon spécifique"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Prestation créée avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ServiceResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données de la requête invalides"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon non trouvé"
            ),
            ApiResponse(
                responseCode = "409",
                description = "Une prestation avec ce nom existe déjà dans le salon"
            )
        ]
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createService(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String,

        @Parameter(description = "Données de la prestation à créer", required = true)
        @Valid @RequestBody request: CreateServiceRequest
    ): ResponseEntity<ServiceResponse> {
        val authenticatedUserId = getAuthenticatedUserId()

        // S'assurer que le salonId dans le chemin correspond à celui de la requête
        val validatedRequest = request.copy(salonId = salonId)

        val service = salonServiceService.createService(validatedRequest, authenticatedUserId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/salons/$salonId/services/${service.id}")
            .body(service)
    }

    /**
     * Importe plusieurs prestations en une seule opération.
     */
    @Operation(
        summary = "Importer plusieurs prestations",
        description = "Crée plusieurs prestations de service en batch pour un salon"
    )
    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    fun importServices(
        @PathVariable salonId: String,
        @Valid @RequestBody requests: List<CreateServiceRequest>
    ): ResponseEntity<List<ServiceResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()
        val services = salonServiceService.importServices(salonId, requests)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(services)
    }

    // ========== ENDPOINTS DE LECTURE ==========

    /**
     * Récupère toutes les prestations d'un salon.
     */
    @Operation(
        summary = "Lister toutes les prestations d'un salon",
        description = "Retourne la liste complète des prestations disponibles dans un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des prestations récupérée avec succès",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = ServiceResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon non trouvé"
            )
        ]
    )
    @GetMapping
    fun getAllServices(
        @PathVariable salonId: String
    ): ResponseEntity<List<ServiceResponse>> {
        val services = salonServiceService.getServicesBySalon(salonId)

        return ResponseEntity.ok(services)
    }

    /**
     * Récupère les prestations d'un salon avec pagination.
     */
    @Operation(
        summary = "Lister les prestations avec pagination",
        description = "Retourne les prestations d'un salon avec pagination, tri et filtrage"
    )
    @GetMapping("/paginated")
    fun getServicesPaginated(
        @PathVariable salonId: String,
        @Parameter(description = "Paramètres de pagination (page, size, sort)")
        @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable
    ): ResponseEntity<Page<ServiceResponse>> {
        val page = salonServiceService.getServicesBySalonPaginated(salonId, pageable)

        return ResponseEntity.ok(page)
    }

    /**
     * Récupère une prestation spécifique par son ID.
     */
    @Operation(
        summary = "Récupérer une prestation par ID",
        description = "Retourne les détails d'une prestation spécifique"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Prestation trouvée",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ServiceResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Prestation non trouvée"
            )
        ]
    )
    @GetMapping("/{serviceId}")
    fun getServiceById(
        @PathVariable salonId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<ServiceResponse> {
        val service = salonServiceService.getServiceById(serviceId)

        // Vérifier que le service appartient bien au salon spécifié
        if (service.salonId != salonId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return ResponseEntity.ok(service)
    }

    // ========== ENDPOINTS DE RECHERCHE ET FILTRAGE ==========

    /**
     * Recherche des prestations par terme.
     */
    @Operation(
        summary = "Rechercher des prestations",
        description = "Recherche des prestations par nom ou description (insensible à la casse)"
    )
    @GetMapping("/search")
    fun searchServices(
        @PathVariable salonId: String,
        @Parameter(description = "Terme de recherche", required = true)
        @RequestParam("q") searchTerm: String
    ): ResponseEntity<List<ServiceResponse>> {
        val services = salonServiceService.searchServices(salonId, searchTerm)

        return ResponseEntity.ok(services)
    }

    /**
     * Filtre les prestations par catégorie.
     */
    @Operation(
        summary = "Filtrer par catégorie",
        description = "Retourne les prestations d'une catégorie spécifique"
    )
    @GetMapping("/categories/{category}")
    fun getServicesByCategory(
        @PathVariable salonId: String,
        @PathVariable category: ServiceCategory
    ): ResponseEntity<List<ServiceResponse>> {
        val services = salonServiceService.getServicesByCategory(salonId, category)

        return ResponseEntity.ok(services)
    }

    /**
     * Filtre les prestations par plage de prix.
     */
    @Operation(
        summary = "Filtrer par plage de prix",
        description = "Retourne les prestations dont le prix est dans une plage spécifique"
    )
    @GetMapping("/filter/price")
    fun getServicesByPriceRange(
        @PathVariable salonId: String,
        @Parameter(description = "Prix minimum", required = true)
        @RequestParam("min") minPrice: BigDecimal,
        @Parameter(description = "Prix maximum", required = true)
        @RequestParam("max") maxPrice: BigDecimal
    ): ResponseEntity<List<ServiceResponse>> {
        val services = salonServiceService.getServicesByPriceRange(
            salonId, minPrice, maxPrice
        )

        return ResponseEntity.ok(services)
    }

    // ========== ENDPOINTS DE MISE À JOUR ==========

    /**
     * Met à jour une prestation existante.
     */
    @Operation(
        summary = "Mettre à jour une prestation",
        description = "Met à jour les informations d'une prestation existante"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Prestation mise à jour avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ServiceResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données de la requête invalides"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Prestation non trouvée"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Utilisateur non autorisé"
            )
        ]
    )
    @PutMapping("/{serviceId}")
    @PreAuthorize("isAuthenticated()")
    fun updateService(
        @PathVariable salonId: String,
        @PathVariable serviceId: String,
        @Valid @RequestBody request: UpdateServiceRequest
    ): ResponseEntity<ServiceResponse> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Vérifier que le service appartient bien au salon
        val existingService = salonServiceService.getServiceById(serviceId)
        if (existingService.salonId != salonId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val updatedService = salonServiceService.updateService(serviceId, request, authenticatedUserId)

        return ResponseEntity.ok(updatedService)
    }

    // ========== ENDPOINTS DE SUPPRESSION ==========

    /**
     * Supprime une prestation.
     */
    @Operation(
        summary = "Supprimer une prestation",
        description = "Supprime définitivement une prestation du catalogue"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Prestation supprimée avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Prestation non trouvée"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Utilisateur non autorisé"
            )
        ]
    )
    @DeleteMapping("/{serviceId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteService(
        @PathVariable salonId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Void> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Vérifier que le service appartient bien au salon
        val existingService = salonServiceService.getServiceById(serviceId)
        if (existingService.salonId != salonId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        salonServiceService.deleteService(serviceId, authenticatedUserId)

        return ResponseEntity.noContent().build()
    }

    // ========== ENDPOINTS DE STATISTIQUES ==========

    /**
     * Récupère les statistiques des prestations d'un salon.
     */
    @Operation(
        summary = "Récupérer les statistiques",
        description = "Retourne des statistiques sur les prestations d'un salon"
    )
    @GetMapping("/statistics")
    fun getServiceStatistics(
        @PathVariable salonId: String
    ): ResponseEntity<Map<String, Any>> {
        val statistics = salonServiceService.getServiceStatistics(salonId)

        return ResponseEntity.ok(statistics)
    }

    /**
     * Récupère les catégories disponibles avec le nombre de prestations.
     */
    @Operation(
        summary = "Récupérer les catégories",
        description = "Retourne la liste des catégories disponibles avec le compte de prestations"
    )
    @GetMapping("/categories")
    fun getCategoriesWithCount(
        @PathVariable salonId: String
    ): ResponseEntity<Map<ServiceCategory, Int>> {
        val statistics = salonServiceService.getServiceStatistics(salonId)

        @Suppress("UNCHECKED_CAST")
        val categories = statistics["categories"] as? Map<ServiceCategory, Int>
            ?: emptyMap()

        return ResponseEntity.ok(categories)
    }

    // ========== GESTION DES ERREURS GLOBALES ==========

    /**
     * Gestionnaire d'exceptions pour les erreurs de validation.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Validation error",
                "message" to (ex.message ?: "Invalid request data")
            ))
    }

    /**
     * Gestionnaire d'exceptions pour les ressources non trouvées.
     */
    @ExceptionHandler(
        SalonServiceService.SalonNotFoundException::class,
        SalonServiceService.ServiceNotFoundException::class
    )
    fun handleNotFoundException(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Resource not found")
            ))
    }

    /**
     * Gestionnaire d'exceptions pour les conflits (doublons).
     */
    @ExceptionHandler(SalonServiceService.DuplicateServiceException::class)
    fun handleDuplicateException(ex: SalonServiceService.DuplicateServiceException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf(
                "error" to "Conflict",
                "message" to (ex.message ?: "Duplicate resource")
            ))
    }

    /**
     * Gestionnaire d'exceptions pour les accès non autorisés.
     */
    @ExceptionHandler(SalonServiceService.UnauthorizedAccessException::class)
    fun handleUnauthorizedException(ex: SalonServiceService.UnauthorizedAccessException): ResponseEntity<Map<String, String>> {
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