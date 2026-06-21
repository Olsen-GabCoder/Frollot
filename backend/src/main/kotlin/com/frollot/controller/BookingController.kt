package com.frollot.controller

import com.frollot.dto.*
import com.frollot.model.User
import com.frollot.service.BookingService
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
 * Contrôleur REST pour la gestion des réservations (Bookings).
 *
 * Expose une API complète pour :
 * - Créer et gérer des réservations
 * - Calculer les créneaux disponibles
 * - Gérer le cycle de vie des réservations
 * - Obtenir des statistiques
 *
 * Endpoints préfixés par : /api/bookings et /api/salons/{salonId}/bookings
 */
@RestController
@Tag(
    name = "Gestion des Réservations",
    description = "API de gestion des réservations dans les salons de coiffure"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class BookingController(
    private val bookingService: BookingService
) {

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Récupère l'ID de l'utilisateur authentifié depuis le SecurityContext.
     *
     * @return L'ID de l'utilisateur authentifié
     * @throws IllegalStateException si aucun utilisateur n'est authentifié
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
     * Crée une nouvelle réservation.
     *
     * Seuls les clients peuvent créer des réservations.
     * Le clientId dans la requête doit correspondre à l'utilisateur authentifié.
     */
    @Operation(
        summary = "Créer une réservation",
        description = "Crée une nouvelle réservation pour un client dans un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Réservation créée avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BookingResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon, client, service ou coiffeur non trouvé"
            ),
            ApiResponse(
                responseCode = "409",
                description = "Créneau non disponible"
            )
        ]
    )
    @PostMapping("/api/bookings")
    @PreAuthorize("isAuthenticated()")
    fun createBooking(
        @Parameter(description = "Données de la réservation", required = true)
        @Valid @RequestBody request: CreateBookingRequest
    ): ResponseEntity<BookingResponse> {
        // Récupérer l'ID du client authentifié
        val authenticatedClientId = getAuthenticatedUserId()

        // Vérifier que le clientId dans la requête correspond à l'utilisateur authentifié
        if (request.clientId != authenticatedClientId) {
            throw IllegalArgumentException("Vous ne pouvez créer une réservation que pour vous-même")
        }

        val booking = bookingService.createBooking(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/bookings/${booking.id}")
            .body(booking)
    }

    // ========== ENDPOINTS DE LECTURE ==========

    /**
     * Récupère une réservation par son ID.
     *
     * Seuls le client concerné, le propriétaire du salon ou le coiffeur assigné peuvent voir une réservation.
     */
    @Operation(
        summary = "Récupérer une réservation par ID",
        description = "Retourne les détails complets d'une réservation"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Réservation trouvée",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BookingResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Réservation non trouvée"
            )
        ]
    )
    @GetMapping("/api/bookings/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    fun getBookingById(
        @PathVariable bookingId: String
    ): ResponseEntity<BookingResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val booking = bookingService.getBookingById(bookingId, authenticatedUserId)
        return ResponseEntity.ok(booking)
    }

    /**
     * Récupère toutes les réservations d'un salon.
     *
     * Seuls les propriétaires du salon peuvent voir toutes leurs réservations.
     */
    @Operation(
        summary = "Lister les réservations d'un salon",
        description = "Retourne toutes les réservations d'un salon, triées par date décroissante"
    )
    @GetMapping("/api/salons/{salonId}/bookings")
    @PreAuthorize("isAuthenticated()")
    fun getBookingsBySalon(
        @PathVariable salonId: String
    ): ResponseEntity<List<BookingResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()
        val bookings = bookingService.getBookingsBySalon(salonId, authenticatedUserId)
        return ResponseEntity.ok(bookings)
    }

    /**
     * Récupère les réservations à venir d'un salon.
     */
    @Operation(
        summary = "Lister les réservations à venir d'un salon",
        description = "Retourne uniquement les réservations futures et actives"
    )
    @GetMapping("/api/salons/{salonId}/bookings/upcoming")
    @PreAuthorize("isAuthenticated()")
    fun getUpcomingBookingsBySalon(
        @PathVariable salonId: String
    ): ResponseEntity<List<BookingResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()
        val bookings = bookingService.getUpcomingBookingsBySalon(salonId, authenticatedUserId)
        return ResponseEntity.ok(bookings)
    }

    /**
     * Récupère toutes les réservations d'un client.
     *
     * Un client ne peut voir que ses propres réservations.
     */
    @Operation(
        summary = "Lister les réservations d'un client",
        description = "Retourne toutes les réservations d'un client, triées par date décroissante"
    )
    @GetMapping("/api/clients/{clientId}/bookings")
    @PreAuthorize("isAuthenticated()")
    fun getBookingsByClient(
        @PathVariable clientId: String
    ): ResponseEntity<List<BookingResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Un client ne peut voir que ses propres réservations
        if (clientId != authenticatedUserId) {
            throw SecurityException("Vous ne pouvez voir que vos propres réservations")
        }

        val bookings = bookingService.getBookingsByClient(clientId)
        return ResponseEntity.ok(bookings)
    }

    /**
     * Récupère les réservations à venir d'un client.
     *
     * Un client ne peut voir que ses propres réservations.
     */
    @Operation(
        summary = "Lister les réservations à venir d'un client",
        description = "Retourne uniquement les réservations futures du client"
    )
    @GetMapping("/api/clients/{clientId}/bookings/upcoming")
    @PreAuthorize("isAuthenticated()")
    fun getUpcomingBookingsByClient(
        @PathVariable clientId: String
    ): ResponseEntity<List<BookingResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Un client ne peut voir que ses propres réservations
        if (clientId != authenticatedUserId) {
            throw SecurityException("Vous ne pouvez voir que vos propres réservations")
        }

        val bookings = bookingService.getUpcomingBookingsByClient(clientId)
        return ResponseEntity.ok(bookings)
    }

    /**
     * Récupère les réservations d'un coiffeur.
     *
     * Un coiffeur ne peut voir que ses propres réservations.
     */
    @Operation(
        summary = "Lister les réservations d'un coiffeur",
        description = "Retourne toutes les réservations assignées à un coiffeur"
    )
    @GetMapping("/api/staff/{staffId}/bookings")
    @PreAuthorize("isAuthenticated()")
    fun getBookingsByStaff(
        @PathVariable staffId: String
    ): ResponseEntity<List<BookingResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()
        val bookings = bookingService.getBookingsByStaff(staffId, authenticatedUserId)
        return ResponseEntity.ok(bookings)
    }

    // ========== ENDPOINTS DE MISE À JOUR ==========

    /**
     * Met à jour le statut d'une réservation.
     *
     * Seuls le propriétaire du salon ou le client concerné peuvent mettre à jour le statut.
     */
    @Operation(
        summary = "Mettre à jour le statut",
        description = "Change le statut d'une réservation (confirmation, annulation, etc.)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Statut mis à jour avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Réservation non trouvée"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Non autorisé"
            )
        ]
    )
    @PatchMapping("/api/bookings/{bookingId}/status")
    @PreAuthorize("isAuthenticated()")
    fun updateBookingStatus(
        @PathVariable bookingId: String,
        @Valid @RequestBody request: UpdateBookingStatusRequest
    ): ResponseEntity<BookingResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val booking = bookingService.updateBookingStatus(bookingId, request, authenticatedUserId)
        return ResponseEntity.ok(booking)
    }

    /**
     * Met à jour le paiement d'une réservation.
     *
     * Seuls le propriétaire du salon peut mettre à jour le paiement.
     */
    @Operation(
        summary = "Mettre à jour le paiement",
        description = "Change le statut de paiement et enregistre la méthode utilisée"
    )
    @PatchMapping("/api/bookings/{bookingId}/payment")
    @PreAuthorize("isAuthenticated()")
    fun updateBookingPayment(
        @PathVariable bookingId: String,
        @Valid @RequestBody request: UpdateBookingPaymentRequest
    ): ResponseEntity<BookingResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val booking = bookingService.updateBookingPayment(bookingId, request, authenticatedUserId)
        return ResponseEntity.ok(booking)
    }

    /**
     * Annule une réservation.
     *
     * Seuls le propriétaire du salon ou le client concerné peuvent annuler une réservation.
     */
    @Operation(
        summary = "Annuler une réservation",
        description = "Annule une réservation (si elle peut encore être annulée)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Réservation annulée avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "La réservation ne peut plus être annulée"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Réservation non trouvée"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Non autorisé"
            )
        ]
    )
    @DeleteMapping("/api/bookings/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    fun cancelBooking(
        @PathVariable bookingId: String
    ): ResponseEntity<BookingResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val booking = bookingService.cancelBooking(bookingId, authenticatedUserId)
        return ResponseEntity.ok(booking)
    }

    // ========== ENDPOINTS DE DISPONIBILITÉ ==========

    /**
     * Calcule les créneaux disponibles pour un service.
     */
    @Operation(
        summary = "Calculer les créneaux disponibles",
        description = "Retourne tous les créneaux disponibles pour un service donné dans un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Créneaux calculés avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AvailableSlotsResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon ou service non trouvé"
            )
        ]
    )
    @PostMapping("/api/salons/{salonId}/available-slots")
    fun getAvailableSlots(
        @PathVariable salonId: String,
        @Valid @RequestBody request: AvailableSlotsRequest
    ): ResponseEntity<AvailableSlotsResponse> {
        val validatedRequest = request.copy(salonId = salonId)
        val slots = bookingService.getAvailableSlots(validatedRequest)
        return ResponseEntity.ok(slots)
    }

    // ========== ENDPOINTS DE STATISTIQUES ==========

    /**
     * Récupère les statistiques des réservations d'un salon.
     *
     * Seuls le propriétaire du salon peut voir les statistiques.
     */
    @Operation(
        summary = "Récupérer les statistiques",
        description = "Retourne des statistiques sur les réservations d'un salon"
    )
    @GetMapping("/api/salons/{salonId}/bookings/statistics")
    @PreAuthorize("isAuthenticated()")
    fun getBookingStatistics(
        @PathVariable salonId: String
    ): ResponseEntity<BookingStatistics> {
        val authenticatedUserId = getAuthenticatedUserId()
        val statistics = bookingService.getBookingStatistics(salonId, authenticatedUserId)
        return ResponseEntity.ok(statistics)
    }

    /**
     * Série temporelle journalière des réservations d'un salon.
     *
     * Renvoie un point par jour sur [from, to] (jours vides inclus avec count=0, revenue=0).
     */
    @Operation(
        summary = "Série temporelle journalière",
        description = "Retourne count + revenue par jour sur une plage, y compris les jours sans réservation"
    )
    @GetMapping("/api/salons/{salonId}/bookings/daily")
    @PreAuthorize("isAuthenticated()")
    fun getDailyBookings(
        @PathVariable salonId: String,
        @RequestParam from: java.time.LocalDate,
        @RequestParam to: java.time.LocalDate
    ): ResponseEntity<List<DailyBookingPoint>> {
        val authenticatedUserId = getAuthenticatedUserId()
        val daily = bookingService.getDailyBookings(salonId, from, to, authenticatedUserId)
        return ResponseEntity.ok(daily)
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

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<Map<String, String>> {
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

    @ExceptionHandler(BookingService.BookingNotFoundException::class)
    fun handleNotFoundException(ex: BookingService.BookingNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Booking not found")
            ))
    }

    @ExceptionHandler(BookingService.SlotUnavailableException::class)
    fun handleSlotUnavailableException(ex: BookingService.SlotUnavailableException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf(
                "error" to "Slot Unavailable",
                "message" to (ex.message ?: "The requested slot is not available")
            ))
    }

    @ExceptionHandler(BookingService.InvalidBookingException::class)
    fun handleInvalidBookingException(ex: BookingService.InvalidBookingException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Invalid Booking",
                "message" to (ex.message ?: "Invalid booking data")
            ))
    }

    @ExceptionHandler(BookingService.UnauthorizedAccessException::class)
    fun handleUnauthorizedException(ex: BookingService.UnauthorizedAccessException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Access denied")
            ))
    }
}