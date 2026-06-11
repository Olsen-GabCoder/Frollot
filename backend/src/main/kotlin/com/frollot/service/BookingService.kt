package com.frollot.service

import com.frollot.dto.*
import com.frollot.model.*
import com.frollot.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Service de gestion des réservations (Bookings).
 *
 * Gère :
 * - Création et validation des réservations
 * - Détection de conflits de créneaux
 * - Calcul des créneaux disponibles
 * - Gestion du cycle de vie (confirmation, annulation, finalisation)
 */
@Service
@Transactional
class BookingService(
    private val bookingRepository: BookingRepository,
    private val salonRepository: SalonRepository,
    private val userRepository: UserRepository,
    private val salonServiceRepository: SalonServiceRepository,
    private val salonStaffRepository: SalonStaffRepository,
    private val emailService: EmailService? = null, // Optionnel pour éviter les erreurs si non configuré
    private val notificationService: NotificationService? = null // Optionnel pour éviter les erreurs si non configuré
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class BookingNotFoundException(bookingId: String) :
        RuntimeException("Réservation avec ID '$bookingId' non trouvée")

    class SlotUnavailableException(message: String) :
        RuntimeException(message)

    class InvalidBookingException(message: String) :
        RuntimeException(message)

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé")

    // ========== CRÉATION DE RÉSERVATION ==========

    /**
     * Crée une nouvelle réservation.
     *
     * Effectue toutes les validations nécessaires :
     * - Existence des entités (salon, client, service, staff)
     * - Disponibilité du créneau
     * - Cohérence des données
     */
    @Transactional
    fun createBooking(request: CreateBookingRequest): BookingResponse {
        println("🔍 Création réservation - Salon: ${request.salonId}, Client: ${request.clientId}")

        // 1. Vérification de l'existence du salon
        val salon = salonRepository.findById(request.salonId)
            .orElseThrow { SalonServiceService.SalonNotFoundException(request.salonId) }

        // 2. Vérification de l'existence du client
        val client = userRepository.findById(request.clientId)
            .orElseThrow { RuntimeException("Client avec ID '${request.clientId}' non trouvé") }

        if (client.userType != UserType.client) {
            throw InvalidBookingException("L'utilisateur doit être de type 'client'")
        }

        // 3. Vérification de l'existence du service
        val service = salonServiceRepository.findById(request.serviceId)
            .orElseThrow { SalonServiceService.ServiceNotFoundException(request.serviceId) }

        // Vérifier que le service appartient bien au salon
        if (service.salon?.id != request.salonId) {
            throw InvalidBookingException("Le service n'appartient pas à ce salon")
        }

        // 4. Gestion du staff (optionnel)
        var staff: SalonStaff? = null
        if (request.staffId != null) {
            staff = salonStaffRepository.findById(request.staffId)
                .orElseThrow { SalonStaffService.StaffNotFoundException(request.staffId) }

            // Vérifier que le staff appartient bien au salon
            if (staff.salon?.id != request.salonId) {
                throw InvalidBookingException("Le coiffeur n'appartient pas à ce salon")
            }

            // Vérifier que le staff est actif
            if (!staff.isActive) {
                throw InvalidBookingException("Le coiffeur n'est pas disponible")
            }

            // Vérifier que le staff peut effectuer ce service
            if (!staff.canPerformService(service.category)) {
                throw InvalidBookingException("Le coiffeur ne propose pas ce type de service")
            }
        }

        // 5. Vérification de la date (ne peut pas réserver dans le passé)
        if (request.bookingDatetime.isBefore(LocalDateTime.now())) {
            throw InvalidBookingException("Impossible de réserver dans le passé")
        }

        // 6. Calcul de la durée (depuis le service)
        val durationMinutes = service.durationMinutes
        val endDatetime = request.bookingDatetime.plusMinutes(durationMinutes.toLong())

        // 7. Vérification de la disponibilité du créneau

        // 7a. Vérifier que le client n'a pas déjà une réservation au même moment
        if (bookingRepository.hasClientConflict(
                request.clientId,
                request.bookingDatetime,
                endDatetime
            ) > 0
        ) {
            throw SlotUnavailableException("Vous avez déjà une réservation à ce moment-là")
        }

        // 7b. Si un coiffeur est spécifié, vérifier sa disponibilité
        if (staff != null) {
            if (bookingRepository.hasStaffConflict(
                    staff.id!!,
                    request.bookingDatetime,
                    endDatetime
                ) > 0
            ) {
                throw SlotUnavailableException("Le coiffeur n'est pas disponible à ce créneau")
            }
        }

        // 8. Création de l'entité Booking
        val booking = Booking(
            id = UUID.randomUUID().toString(),
            salon = salon,
            client = client,
            staff = staff,
            service = service,
            bookingDatetime = request.bookingDatetime,
            durationMinutes = durationMinutes,
            status = BookingStatus.pending,
            priceFinal = service.price,
            paymentStatus = PaymentStatus.unpaid, // Par défaut non payé, paiement via Stripe
            notesClient = request.notesClient
        )

        // 9. Validation de l'entité
        if (!booking.isValid()) {
            throw InvalidBookingException("Les données de la réservation sont invalides")
        }

        // 10. Sauvegarde
        val savedBooking = bookingRepository.save(booking)

        println("✅ Réservation créée: ${savedBooking.id} - ${client.email} - ${service.name}")

        // 11. Envoyer email de confirmation
        emailService?.sendBookingConfirmation(savedBooking)

        return BookingResponse.fromEntity(savedBooking)
    }

    // ========== LECTURE DE RÉSERVATIONS ==========

    /**
     * Récupère une réservation par son ID.
     */
    @Transactional(readOnly = true)
    fun getBookingById(bookingId: String): BookingResponse {
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { BookingNotFoundException(bookingId) }

        return BookingResponse.fromEntity(booking)
    }

    /**
     * Récupère toutes les réservations d'un salon.
     */
    @Transactional(readOnly = true)
    fun getBookingsBySalon(salonId: String): List<BookingResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonServiceService.SalonNotFoundException(salonId)
        }

        val bookings = bookingRepository.findBySalonIdOrderByBookingDatetimeDesc(salonId)
        return bookings.map { BookingResponse.fromEntity(it) }
    }

    /**
     * Récupère les réservations à venir d'un salon.
     */
    @Transactional(readOnly = true)
    fun getUpcomingBookingsBySalon(salonId: String): List<BookingResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonServiceService.SalonNotFoundException(salonId)
        }

        val bookings = bookingRepository.findUpcomingBookingsBySalon(salonId)
        return bookings.map { BookingResponse.fromEntity(it) }
    }

    /**
     * Récupère toutes les réservations d'un client.
     */
    @Transactional(readOnly = true)
    fun getBookingsByClient(clientId: String): List<BookingResponse> {
        if (!userRepository.existsById(clientId)) {
            throw RuntimeException("Client non trouvé")
        }

        val bookings = bookingRepository.findByClientIdOrderByBookingDatetimeDesc(clientId)
        return bookings.map { BookingResponse.fromEntity(it) }
    }

    /**
     * Récupère les réservations à venir d'un client.
     */
    @Transactional(readOnly = true)
    fun getUpcomingBookingsByClient(clientId: String): List<BookingResponse> {
        if (!userRepository.existsById(clientId)) {
            throw RuntimeException("Client non trouvé")
        }

        val bookings = bookingRepository.findUpcomingBookingsByClient(clientId)
        return bookings.map { BookingResponse.fromEntity(it) }
    }

    /**
     * Récupère les réservations d'un coiffeur.
     */
    @Transactional(readOnly = true)
    fun getBookingsByStaff(staffId: String): List<BookingResponse> {
        if (!salonStaffRepository.existsById(staffId)) {
            throw SalonStaffService.StaffNotFoundException(staffId)
        }

        val bookings = bookingRepository.findByStaffIdOrderByBookingDatetimeAsc(staffId)
        return bookings.map { BookingResponse.fromEntity(it) }
    }

    // ========== MISE À JOUR DE RÉSERVATION ==========

    /**
     * Met à jour le statut d'une réservation.
     */
    @Transactional
    fun updateBookingStatus(
        bookingId: String,
        request: UpdateBookingStatusRequest,
        userId: String? = null
    ): BookingResponse {
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { BookingNotFoundException(bookingId) }

        // Vérification des autorisations (propriétaire du salon ou client)
        userId?.let {
            val isOwner = booking.salon?.owner?.id == userId
            val isClient = booking.client?.id == userId
            if (!isOwner && !isClient) {
                throw UnauthorizedAccessException(userId)
            }
        }

        // Mise à jour du statut
        val oldStatus = booking.status
        booking.status = request.status
        request.notesSalon?.let { booking.notesSalon = it }

        // Mise à jour des timestamps selon le statut
        when (request.status) {
            BookingStatus.confirmed -> {
                if (booking.confirmedAt == null) {
                    booking.confirmedAt = LocalDateTime.now()
                }
            }
            BookingStatus.completed -> {
                if (booking.completedAt == null) {
                    booking.completedAt = LocalDateTime.now()
                }
            }
            BookingStatus.cancelled, BookingStatus.no_show -> {
                if (booking.cancelledAt == null) {
                    booking.cancelledAt = LocalDateTime.now()
                }
            }
            else -> {}
        }

        val savedBooking = bookingRepository.save(booking)

        println("✏️ Statut réservation mis à jour: ${savedBooking.id} → ${request.status}")

        // Envoyer email et notification push de changement de statut
        if (oldStatus != savedBooking.status) {
            emailService?.sendBookingStatusChange(savedBooking, oldStatus)
            notificationService?.sendBookingStatusNotification(savedBooking, savedBooking.status.getDisplayName())
        }

        return BookingResponse.fromEntity(savedBooking)
    }

    /**
     * Met à jour le paiement d'une réservation.
     */
    @Transactional
    fun updateBookingPayment(
        bookingId: String,
        request: UpdateBookingPaymentRequest,
        userId: String? = null
    ): BookingResponse {
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { BookingNotFoundException(bookingId) }

        // Vérification des autorisations (propriétaire du salon uniquement)
        userId?.let {
            if (booking.salon?.owner?.id != userId) {
                throw UnauthorizedAccessException(userId)
            }
        }

        booking.paymentStatus = request.paymentStatus
        request.paymentMethod?.let { booking.paymentMethod = it }
        request.priceFinal?.let { booking.priceFinal = it }

        val savedBooking = bookingRepository.save(booking)

        println("💰 Paiement mis à jour: ${savedBooking.id} → ${request.paymentStatus}")

        return BookingResponse.fromEntity(savedBooking)
    }

    /**
     * Annule une réservation.
     */
    @Transactional
    fun cancelBooking(bookingId: String, userId: String? = null): BookingResponse {
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { BookingNotFoundException(bookingId) }

        // Vérifier que la réservation peut être annulée
        if (!booking.canBeCancelled()) {
            throw InvalidBookingException("Cette réservation ne peut plus être annulée")
        }

        // Vérification des autorisations
        userId?.let {
            val isOwner = booking.salon?.owner?.id == userId
            val isClient = booking.client?.id == userId
            if (!isOwner && !isClient) {
                throw UnauthorizedAccessException(userId)
            }
        }

        booking.status = BookingStatus.cancelled
        booking.cancelledAt = LocalDateTime.now()

        val savedBooking = bookingRepository.save(booking)

        println("🗑️ Réservation annulée: ${savedBooking.id}")

        return BookingResponse.fromEntity(savedBooking)
    }

    // ========== CALCUL DES CRÉNEAUX DISPONIBLES ==========

    /**
     * Calcule les créneaux disponibles pour un service dans un salon.
     *
     * Algorithme :
     * - Génère tous les créneaux possibles de la journée (9h-19h par défaut)
     * - Exclut les créneaux déjà réservés
     * - Retourne uniquement les créneaux disponibles
     */
    @Transactional(readOnly = true)
    fun getAvailableSlots(request: AvailableSlotsRequest): AvailableSlotsResponse {
        // 1. Vérification du salon
        if (!salonRepository.existsById(request.salonId)) {
            throw SalonServiceService.SalonNotFoundException(request.salonId)
        }

        // 2. Vérification du service
        val service = salonServiceRepository.findById(request.serviceId)
            .orElseThrow { SalonServiceService.ServiceNotFoundException(request.serviceId) }

        // 3. Déterminer le staff concerné
        val staffList = if (request.staffId != null) {
            val staff = salonStaffRepository.findById(request.staffId)
                .orElseThrow { SalonStaffService.StaffNotFoundException(request.staffId) }
            listOf(staff)
        } else {
            // Tous les coiffeurs actifs du salon pouvant faire ce service
            salonStaffRepository.findBySalonIdAndSpecialty(request.salonId, service.category)
        }

        if (staffList.isEmpty()) {
            return AvailableSlotsResponse(
                date = request.date,
                salonId = request.salonId,
                serviceId = request.serviceId,
                slots = emptyList()
            )
        }

        // 4. Paramètres de génération de créneaux
        val targetDate = request.date.toLocalDate()
        val openingTime = LocalTime.of(9, 0) // 9h
        val closingTime = LocalTime.of(19, 0) // 19h
        val slotInterval = 30 // minutes entre chaque créneau

        val slots = mutableListOf<TimeSlot>()

        // 5. Génération des créneaux pour chaque coiffeur
        for (staff in staffList) {
            var currentTime = LocalDateTime.of(targetDate, openingTime)
            val endOfDay = LocalDateTime.of(targetDate, closingTime)

            while (currentTime.plusMinutes(service.durationMinutes.toLong()).isBefore(endOfDay) ||
                currentTime.plusMinutes(service.durationMinutes.toLong()).isEqual(endOfDay)
            ) {
                val slotEnd = currentTime.plusMinutes(service.durationMinutes.toLong())

                // Vérifier la disponibilité
                val isAvailable = bookingRepository.hasStaffConflict(
                    staff.id!!,
                    currentTime,
                    slotEnd
                ) == 0L

                slots.add(
                    TimeSlot(
                        datetime = currentTime,
                        staffId = staff.id,
                        staffName = "${staff.user?.firstName} ${staff.user?.lastName}",
                        available = isAvailable
                    )
                )

                currentTime = currentTime.plusMinutes(slotInterval.toLong())
            }
        }

        // 6. Filtrer uniquement les créneaux disponibles et futurs
        val now = LocalDateTime.now()
        val availableSlots = slots.filter { it.available && it.datetime.isAfter(now) }

        return AvailableSlotsResponse(
            date = request.date,
            salonId = request.salonId,
            serviceId = request.serviceId,
            slots = availableSlots
        )
    }

    // ========== STATISTIQUES ==========

    /**
     * Récupère les statistiques d'un salon.
     */
    @Transactional(readOnly = true)
    fun getBookingStatistics(salonId: String): BookingStatistics {
        if (!salonRepository.existsById(salonId)) {
            throw SalonServiceService.SalonNotFoundException(salonId)
        }

        val total = bookingRepository.countBySalonId(salonId).toInt()
        val pending = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.pending).toInt()
        val confirmed = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.confirmed).toInt()
        val completed = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.completed).toInt()
        val cancelled = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.cancelled).toInt()
        val totalRevenue = bookingRepository.calculateTotalRevenue(salonId)
        val averagePrice = bookingRepository.calculateAveragePrice(salonId)

        return BookingStatistics(
            salonId = salonId,
            totalBookings = total,
            pendingBookings = pending,
            confirmedBookings = confirmed,
            completedBookings = completed,
            cancelledBookings = cancelled,
            totalRevenue = totalRevenue,
            averagePrice = averagePrice
        )
    }
}