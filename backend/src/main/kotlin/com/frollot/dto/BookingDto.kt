package com.frollot.dto

import com.frollot.model.Booking
import com.frollot.model.BookingStatus
import com.frollot.model.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// ============================================
// REQUEST DTOs (Client → Serveur)
// ============================================

/**
 * DTO pour créer une nouvelle réservation.
 *
 * @property salonId ID du salon
 * @property clientId ID du client effectuant la réservation
 * @property staffId ID du coiffeur souhaité (optionnel - null = premier disponible)
 * @property serviceId ID du service à réserver
 * @property bookingDatetime Date et heure souhaitées
 * @property notesClient Notes ou demandes spéciales du client
 */
data class CreateBookingRequest(
    val salonId: String,
    val clientId: String,
    val staffId: String? = null,
    val serviceId: String,
    val bookingDatetime: LocalDateTime,
    val notesClient: String? = null
)

/**
 * DTO pour mettre à jour le statut d'une réservation.
 *
 * @property status Nouveau statut
 * @property notesSalon Notes internes du salon (optionnel)
 */
data class UpdateBookingStatusRequest(
    val status: BookingStatus,
    val notesSalon: String? = null
)

/**
 * DTO pour mettre à jour le paiement d'une réservation.
 *
 * @property paymentStatus Statut du paiement
 * @property paymentMethod Méthode de paiement utilisée
 * @property priceFinal Prix final facturé (optionnel - par défaut = prix du service)
 */
data class UpdateBookingPaymentRequest(
    val paymentStatus: PaymentStatus,
    val paymentMethod: String? = null,
    val priceFinal: BigDecimal? = null
)

/**
 * DTO pour rechercher les créneaux disponibles.
 *
 * @property salonId ID du salon
 * @property serviceId ID du service
 * @property staffId ID du coiffeur (optionnel - si null, cherche parmi tous les coiffeurs)
 * @property date Date pour laquelle rechercher les créneaux
 */
data class AvailableSlotsRequest(
    val salonId: String,
    val serviceId: String,
    val staffId: String? = null,
    val date: LocalDate
)

// ============================================
// RESPONSE DTOs (Serveur → Client)
// ============================================

/**
 * DTO de réponse pour une réservation complète.
 *
 * Contient toutes les informations nécessaires à l'affichage côté client/salon.
 */
data class BookingResponse(
    val id: String,
    val salonId: String,
    val salonName: String,
    val clientId: String,
    val clientName: String,
    val clientEmail: String,
    val clientPhone: String?,
    val staffId: String?,
    val staffName: String?,
    val staffAvatarUrl: String?,
    val clientAvatarUrl: String?,
    val serviceId: String,
    val serviceName: String,
    val serviceCategory: String,
    val bookingDatetime: LocalDateTime,
    val endDatetime: LocalDateTime,
    val durationMinutes: Int,
    val formattedDuration: String,
    val status: BookingStatus,
    val statusLabel: String,
    val priceFinal: BigDecimal?,
    val formattedPrice: String?,
    val paymentStatus: PaymentStatus,
    val paymentStatusLabel: String,
    val paymentMethod: String?,
    val notesClient: String?,
    val notesSalon: String?,
    val reminderSentAt: LocalDateTime?,
    val confirmedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val canBeCancelled: Boolean,
    val canBeConfirmed: Boolean,
    val isPast: Boolean
) {
    companion object {
        /**
         * Convertit une entité Booking en BookingResponse.
         */
        fun fromEntity(booking: Booking): BookingResponse {
            val service = booking.service!!
            val client = booking.client!!
            val salon = booking.salon!!

            return BookingResponse(
                id = booking.id!!,
                salonId = salon.id!!,
                salonName = salon.name,
                clientId = client.id!!,
                clientName = "${client.firstName ?: ""} ${client.lastName ?: ""}".trim()
                    .ifBlank { client.email },
                clientEmail = client.email,
                clientPhone = client.phoneNumber,
                staffId = booking.staff?.id,
                staffName = booking.staff?.user?.let { user ->
                    "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                        .ifBlank { user.email }
                },
                staffAvatarUrl = booking.staff?.user?.avatarUrl,
                clientAvatarUrl = client.avatarUrl,
                serviceId = service.id!!,
                serviceName = service.name,
                serviceCategory = service.category.getDisplayName(),
                bookingDatetime = booking.bookingDatetime,
                endDatetime = booking.getEndDatetime(),
                durationMinutes = booking.durationMinutes,
                formattedDuration = formatDuration(booking.durationMinutes),
                status = booking.status,
                statusLabel = booking.status.getDisplayName(),
                priceFinal = booking.priceFinal ?: service.price,
                formattedPrice = (booking.priceFinal ?: service.price).let {
                    String.format("%.2f€", it)
                },
                paymentStatus = booking.paymentStatus,
                paymentStatusLabel = booking.paymentStatus.getDisplayName(),
                paymentMethod = booking.paymentMethod,
                notesClient = booking.notesClient,
                notesSalon = booking.notesSalon,
                reminderSentAt = booking.reminderSentAt,
                confirmedAt = booking.confirmedAt,
                completedAt = booking.completedAt,
                cancelledAt = booking.cancelledAt,
                createdAt = booking.createdAt,
                canBeCancelled = booking.canBeCancelled(),
                canBeConfirmed = booking.canBeConfirmed(),
                isPast = booking.isPast()
            )
        }

        /**
         * Formate une durée en minutes en format lisible (ex: "1h30", "45min").
         */
        private fun formatDuration(minutes: Int): String {
            val hours = minutes / 60
            val mins = minutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}h${mins.toString().padStart(2, '0')}"
                hours > 0 -> "${hours}h"
                else -> "${mins}min"
            }
        }
    }
}

/**
 * DTO simplifié pour les listes de réservations.
 *
 * Contient les informations essentielles pour l'affichage en liste.
 */
data class BookingSummary(
    val id: String,
    val salonName: String,
    val clientName: String,
    val staffName: String?,
    val serviceName: String,
    val bookingDatetime: LocalDateTime,
    val durationMinutes: Int,
    val status: BookingStatus,
    val statusLabel: String,
    val price: String
) {
    companion object {
        fun fromEntity(booking: Booking): BookingSummary {
            val service = booking.service!!
            val client = booking.client!!
            val salon = booking.salon!!

            return BookingSummary(
                id = booking.id!!,
                salonName = salon.name,
                clientName = "${client.firstName ?: ""} ${client.lastName ?: ""}".trim()
                    .ifBlank { client.email },
                staffName = booking.staff?.user?.let { user ->
                    "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                },
                serviceName = service.name,
                bookingDatetime = booking.bookingDatetime,
                durationMinutes = booking.durationMinutes,
                status = booking.status,
                statusLabel = booking.status.getDisplayName(),
                price = String.format("%.2f€", booking.priceFinal ?: service.price)
            )
        }
    }
}

/**
 * DTO représentant un créneau horaire disponible.
 *
 * @property datetime Date et heure du créneau
 * @property staffId ID du coiffeur disponible (si recherche avec staff spécifique)
 * @property staffName Nom du coiffeur disponible
 * @property available Indique si le créneau est disponible
 */
data class TimeSlot(
    val datetime: LocalDateTime,
    val staffId: String?,
    val staffName: String?,
    val available: Boolean
)

/**
 * DTO de réponse pour la recherche de créneaux disponibles.
 *
 * @property date Date recherchée
 * @property salonId ID du salon
 * @property serviceId ID du service
 * @property slots Liste des créneaux disponibles
 */
data class AvailableSlotsResponse(
    val date: LocalDate,
    val salonId: String,
    val serviceId: String,
    val slots: List<TimeSlot>
)

/**
 * DTO pour les statistiques d'un salon.
 */
data class BookingStatistics(
    val salonId: String,
    val totalBookings: Int,
    val pendingBookings: Int,
    val confirmedBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,
    val totalRevenue: BigDecimal,
    val averagePrice: BigDecimal
)

/**
 * Un point de la série temporelle journalière (bookings/daily).
 */
data class DailyBookingPoint(
    val date: java.time.LocalDate,
    val count: Int,
    val revenue: BigDecimal
)