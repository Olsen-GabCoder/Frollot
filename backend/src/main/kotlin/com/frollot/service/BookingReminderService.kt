package com.frollot.service

import com.frollot.model.BookingStatus
import com.frollot.repository.BookingRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service de rappel automatique pour les réservations.
 *
 * Envoie des emails de rappel 24h avant les réservations.
 */
@Service
@Transactional
class BookingReminderService(
    private val bookingRepository: BookingRepository,
    private val emailService: EmailService
) {

    /**
     * Envoie les rappels de réservation 24h avant.
     * Exécuté toutes les heures.
     */
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à :00
    fun sendReminders() {
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        
        // Trouver les réservations qui ont lieu demain (entre 24h et 25h)
        val bookings = bookingRepository.findAll().filter { booking ->
            booking.bookingDatetime.isAfter(now.plusHours(23)) &&
            booking.bookingDatetime.isBefore(now.plusHours(25)) &&
            booking.status in listOf(BookingStatus.pending, BookingStatus.confirmed) &&
            booking.reminderSentAt == null // Ne pas renvoyer si déjà envoyé
        }

        bookings.forEach { booking ->
            try {
                emailService.sendBookingReminder(booking)
                booking.reminderSentAt = now
                bookingRepository.save(booking)
                println("✅ Rappel envoyé pour réservation ${booking.id}")
            } catch (e: Exception) {
                println("❌ Erreur lors de l'envoi du rappel pour réservation ${booking.id}: ${e.message}")
            }
        }
    }
}

