package com.frollot.controller

import com.frollot.dto.CreateReviewRequest
import com.frollot.dto.ReviewResponse
import com.frollot.dto.SalonReviewStats
import com.frollot.model.User
import com.frollot.service.ReviewService
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

@RestController
@RequestMapping("/api")
@Tag(
    name = "Gestion des Avis",
    description = "API de gestion des avis et notes des salons"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class ReviewController(
    private val reviewService: ReviewService
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

    @Operation(
        summary = "Créer un avis",
        description = "Crée un nouvel avis pour une réservation terminée. Un seul avis par réservation est autorisé."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Avis créé avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReviewResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides ou réservation non terminée"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Réservation, salon ou client non trouvé"
            ),
            ApiResponse(
                responseCode = "409",
                description = "Un avis existe déjà pour cette réservation"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Non autorisé (le client n'est pas propriétaire de la réservation)"
            )
        ]
    )
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CLIENT')")
    fun createReview(
        @Parameter(description = "Données de l'avis", required = true)
        @Valid @RequestBody request: CreateReviewRequest
    ): ResponseEntity<ReviewResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val review = reviewService.createReview(request, authenticatedUserId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/reviews/${review.id}")
            .body(review)
    }

    // ========== ENDPOINTS DE LECTURE ==========

    @Operation(
        summary = "Récupérer les avis d'un salon",
        description = "Retourne tous les avis visibles d'un salon, triés par date décroissante"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des avis récupérée avec succès",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = ReviewResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon non trouvé"
            )
        ]
    )
    @GetMapping("/salons/{salonId}/reviews")
    fun getSalonReviews(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String,
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<ReviewResponse>> {
        val reviews = reviewService.getSalonReviews(salonId, pageable)
        return ResponseEntity.ok(reviews)
    }

    @Operation(
        summary = "Récupérer tous les avis d'un salon",
        description = "Retourne tous les avis visibles d'un salon sans pagination"
    )
    @GetMapping("/salons/{salonId}/reviews/all")
    fun getAllSalonReviews(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String
    ): ResponseEntity<List<ReviewResponse>> {
        val reviews = reviewService.getSalonReviews(salonId)
        return ResponseEntity.ok(reviews)
    }

    @Operation(
        summary = "Récupérer un avis par ID",
        description = "Retourne les détails complets d'un avis"
    )
    @GetMapping("/reviews/{reviewId}")
    fun getReviewById(
        @Parameter(description = "ID de l'avis", required = true)
        @PathVariable reviewId: String
    ): ResponseEntity<ReviewResponse> {
        val review = reviewService.getReviewById(reviewId)
        return ResponseEntity.ok(review)
    }

    @Operation(
        summary = "Récupérer les avis d'un client",
        description = "Retourne tous les avis laissés par un client"
    )
    @GetMapping("/clients/{clientId}/reviews")
    @PreAuthorize("isAuthenticated()")
    fun getClientReviews(
        @Parameter(description = "ID du client", required = true)
        @PathVariable clientId: String
    ): ResponseEntity<List<ReviewResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Un client ne peut voir que ses propres avis
        if (clientId != authenticatedUserId) {
            throw SecurityException("Vous ne pouvez voir que vos propres avis")
        }

        val reviews = reviewService.getClientReviews(clientId)
        return ResponseEntity.ok(reviews)
    }

    @Operation(
        summary = "Vérifier si un avis existe pour une réservation",
        description = "Retourne true si un avis existe déjà pour la réservation spécifiée"
    )
    @GetMapping("/bookings/{bookingId}/review/exists")
    @PreAuthorize("isAuthenticated()")
    fun hasReviewForBooking(
        @Parameter(description = "ID de la réservation", required = true)
        @PathVariable bookingId: String
    ): ResponseEntity<Map<String, Boolean>> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Vérifier que l'utilisateur est autorisé à voir cette information
        // (client propriétaire de la réservation ou propriétaire du salon)
        // Cette vérification pourrait être ajoutée dans le service si nécessaire
        val exists = reviewService.hasReviewForBooking(bookingId)
        return ResponseEntity.ok(mapOf("exists" to exists))
    }

    // ========== ENDPOINTS DE STATISTIQUES ==========

    @Operation(
        summary = "Récupérer les statistiques d'avis d'un salon",
        description = "Retourne la note moyenne, le nombre total d'avis et la distribution des notes"
    )
    @GetMapping("/salons/{salonId}/reviews/stats")
    fun getSalonReviewStats(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String
    ): ResponseEntity<SalonReviewStats> {
        val stats = reviewService.getSalonReviewStats(salonId)
        return ResponseEntity.ok(stats)
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

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentication required")
            ))
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Access denied")
            ))
    }

    @ExceptionHandler(ReviewService.ReviewNotFoundException::class)
    fun handleReviewNotFoundException(ex: ReviewService.ReviewNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Review not found")
            ))
    }

    @ExceptionHandler(ReviewService.BookingNotFoundException::class)
    fun handleBookingNotFoundException(ex: ReviewService.BookingNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Booking not found")
            ))
    }

    @ExceptionHandler(ReviewService.SalonNotFoundException::class)
    fun handleSalonNotFoundException(ex: ReviewService.SalonNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Salon not found")
            ))
    }

    @ExceptionHandler(ReviewService.InvalidReviewException::class)
    fun handleInvalidReviewException(ex: ReviewService.InvalidReviewException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Invalid Review",
                "message" to (ex.message ?: "Invalid review data")
            ))
    }

    @ExceptionHandler(ReviewService.UnauthorizedAccessException::class)
    fun handleUnauthorizedAccessException(ex: ReviewService.UnauthorizedAccessException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Access denied")
            ))
    }
}