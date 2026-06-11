package com.frollot.repository

import com.frollot.model.Booking
import com.frollot.model.BookingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository pour la gestion des réservations (Booking).
 *
 * Fournit des méthodes de recherche avancées pour :
 * - Filtrer par salon, client, coiffeur, service
 * - Rechercher par plage horaire
 * - Détecter les conflits de réservation
 * - Calculer des statistiques
 */
@Repository
interface BookingRepository : JpaRepository<Booking, String> {

    // ============================================
    // RECHERCHE PAR ENTITÉ
    // ============================================

    /**
     * Trouve toutes les réservations d'un salon.
     * Triées par date décroissante (les plus récentes en premier).
     */
    fun findBySalonIdOrderByBookingDatetimeDesc(salonId: String): List<Booking>

    /**
     * Trouve toutes les réservations d'un salon avec pagination.
     */
    fun findBySalonIdOrderByBookingDatetimeDesc(
        salonId: String,
        pageable: Pageable
    ): Page<Booking>

    /**
     * Trouve toutes les réservations d'un client.
     * Triées par date décroissante.
     */
    fun findByClientIdOrderByBookingDatetimeDesc(clientId: String): List<Booking>

    /**
     * Trouve toutes les réservations d'un coiffeur.
     * Triées par date ascendante (prochaines en premier).
     */
    fun findByStaffIdOrderByBookingDatetimeAsc(staffId: String): List<Booking>

    /**
     * Trouve toutes les réservations d'un service.
     */
    fun findByServiceIdOrderByBookingDatetimeDesc(serviceId: String): List<Booking>

    // ============================================
    // RECHERCHE PAR STATUT
    // ============================================

    /**
     * Trouve les réservations d'un salon par statut.
     */
    fun findBySalonIdAndStatus(
        salonId: String,
        status: BookingStatus
    ): List<Booking>

    /**
     * Trouve les réservations d'un client par statut.
     */
    fun findByClientIdAndStatus(
        clientId: String,
        status: BookingStatus
    ): List<Booking>

    /**
     * Trouve les réservations d'un coiffeur par statut.
     */
    fun findByStaffIdAndStatus(
        staffId: String,
        status: BookingStatus
    ): List<Booking>

    // ============================================
    // RECHERCHE PAR PLAGE HORAIRE
    // ============================================

    /**
     * Trouve les réservations d'un salon dans une plage horaire.
     *
     * @param salonId ID du salon
     * @param start Début de la plage
     * @param end Fin de la plage
     * @return Liste des réservations dans cette plage
     */
    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.salon.id = :salonId
        AND b.bookingDatetime >= :start
        AND b.bookingDatetime < :end
        AND b.status NOT IN (com.frollot.model.BookingStatus.cancelled, com.frollot.model.BookingStatus.no_show)
        ORDER BY b.bookingDatetime ASC
        """
    )
    fun findBySalonAndDatetimeRange(
        @Param("salonId") salonId: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Booking>

    /**
     * Trouve les réservations d'un coiffeur dans une plage horaire.
     *
     * @param staffId ID du coiffeur
     * @param start Début de la plage
     * @param end Fin de la plage
     * @return Liste des réservations dans cette plage
     */
    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.staff.id = :staffId
        AND b.bookingDatetime >= :start
        AND b.bookingDatetime < :end
        AND b.status NOT IN (com.frollot.model.BookingStatus.cancelled, com.frollot.model.BookingStatus.no_show)
        ORDER BY b.bookingDatetime ASC
        """
    )
    fun findByStaffAndDatetimeRange(
        @Param("staffId") staffId: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Booking>

    /**
     * Trouve les réservations d'un client dans une plage horaire.
     */
    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.client.id = :clientId
        AND b.bookingDatetime >= :start
        AND b.bookingDatetime < :end
        ORDER BY b.bookingDatetime ASC
        """
    )
    fun findByClientAndDatetimeRange(
        @Param("clientId") clientId: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Booking>

    // ============================================
    // DÉTECTION DE CONFLITS
    // ============================================

    /**
     * Vérifie si un créneau est disponible pour un coiffeur.
     *
     * Retourne true s'il existe déjà une réservation qui chevauche le créneau demandé.
     *
     * @param staffId ID du coiffeur
     * @param start Début du créneau souhaité
     * @param end Fin du créneau souhaité
     * @param excludeBookingId ID de la réservation à exclure (pour les modifications)
     * @return true si conflit détecté, false sinon
     */
    @Query(
        value = """
        SELECT COUNT(*) > 0
        FROM bookings b
        WHERE b.staff_id = :staffId
        AND b.id != :excludeBookingId
        AND b.status NOT IN ('cancelled', 'no_show')
        AND (
            b.booking_datetime < :end AND
            DATE_ADD(b.booking_datetime, INTERVAL b.duration_minutes MINUTE) > :start
        )
        """,
        nativeQuery = true
    )
    fun hasStaffConflict(
        @Param("staffId") staffId: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("excludeBookingId") excludeBookingId: String = ""
    ): Long

    /**
     * Vérifie si un client a déjà une réservation dans un créneau donné.
     */
    @Query(
        value = """
        SELECT COUNT(*) > 0
        FROM bookings b
        WHERE b.client_id = :clientId
        AND b.id != :excludeBookingId
        AND b.status NOT IN ('cancelled', 'no_show')
        AND (
            b.booking_datetime < :end AND
            DATE_ADD(b.booking_datetime, INTERVAL b.duration_minutes MINUTE) > :start
        )
        """,
        nativeQuery = true
    )
    fun hasClientConflict(
        @Param("clientId") clientId: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("excludeBookingId") excludeBookingId: String = ""
    ): Long

    // ============================================
    // STATISTIQUES
    // ============================================

    /**
     * Compte le nombre de réservations d'un salon par statut.
     */
    fun countBySalonIdAndStatus(salonId: String, status: BookingStatus): Long

    /**
     * Compte le nombre total de réservations d'un salon.
     */
    fun countBySalonId(salonId: String): Long

    /**
     * Calcule le revenu total d'un salon.
     */
    @Query(
        """
        SELECT COALESCE(SUM(b.priceFinal), 0)
        FROM Booking b
        WHERE b.salon.id = :salonId
        AND b.status = com.frollot.model.BookingStatus.completed
        AND b.paymentStatus = com.frollot.model.PaymentStatus.paid
        """
    )
    fun calculateTotalRevenue(@Param("salonId") salonId: String): java.math.BigDecimal

    /**
     * Calcule le prix moyen des réservations d'un salon.
     */
    @Query(
        """
        SELECT COALESCE(AVG(b.priceFinal), 0)
        FROM Booking b
        WHERE b.salon.id = :salonId
        AND b.status = com.frollot.model.BookingStatus.completed
        """
    )
    fun calculateAveragePrice(@Param("salonId") salonId: String): java.math.BigDecimal

    // ============================================
    // REQUÊTES UTILITAIRES
    // ============================================

    /**
     * Trouve les réservations à venir d'un client (non annulées).
     */
    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.client.id = :clientId
        AND b.bookingDatetime > :now
        AND b.status NOT IN (com.frollot.model.BookingStatus.cancelled, com.frollot.model.BookingStatus.no_show, com.frollot.model.BookingStatus.completed)
        ORDER BY b.bookingDatetime ASC
        """
    )
    fun findUpcomingBookingsByClient(
        @Param("clientId") clientId: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<Booking>

    /**
     * Trouve les réservations à venir d'un salon.
     */
    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.salon.id = :salonId
        AND b.bookingDatetime > :now
        AND b.status NOT IN (com.frollot.model.BookingStatus.cancelled, com.frollot.model.BookingStatus.no_show, com.frollot.model.BookingStatus.completed)
        ORDER BY b.bookingDatetime ASC
        """
    )
    fun findUpcomingBookingsBySalon(
        @Param("salonId") salonId: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<Booking>

    /**
     * Trouve les réservations nécessitant un rappel.
     * (Confirmées, à venir dans les prochaines 24h, rappel non encore envoyé)
     */
    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.status = com.frollot.model.BookingStatus.confirmed
        AND b.reminderSentAt IS NULL
        AND b.bookingDatetime BETWEEN :now AND :tomorrow
        ORDER BY b.bookingDatetime ASC
        """
    )
    fun findBookingsNeedingReminder(
        @Param("now") now: LocalDateTime = LocalDateTime.now(),
        @Param("tomorrow") tomorrow: LocalDateTime = LocalDateTime.now().plusDays(1)
    ): List<Booking>
}