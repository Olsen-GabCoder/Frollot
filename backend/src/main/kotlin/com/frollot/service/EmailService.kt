package com.frollot.service

import com.frollot.model.Booking
import com.frollot.model.BookingStatus
import com.frollot.model.QueueEntry
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Service de gestion des emails.
 *
 * Gère l'envoi d'emails pour :
 * - Confirmations de réservation
 * - Rappels de réservation (24h avant)
 * - Changements de statut de réservation
 * - Notifications de file d'attente
 */
@Service
@Transactional
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    @Value("\${app.email.from}")
    private val fromEmail: String,
    @Value("\${app.email.enabled:false}")
    private val emailEnabled: Boolean
) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")

    // ========== MÉTHODES UTILITAIRES POUR LOCALISATION ==========
    
    /**
     * Détermine la langue à utiliser pour un email.
     * Phase 3 - Fonctionnalité Langue
     * 
     * Ordre de priorité :
     * 1. Langue préférée de l'utilisateur (si connecté)
     * 2. Français (langue par défaut)
     * 
     * @param user Utilisateur (peut être null pour emails système)
     * @return Code de la langue (fr, en, es, de, ar)
     */
    private fun determineEmailLanguage(user: com.frollot.model.User?): String {
        val userLanguage = user?.preferredLanguage
        val supportedLanguages = setOf("fr", "en", "es", "de", "ar")
        
        return if (userLanguage != null && supportedLanguages.contains(userLanguage)) {
            userLanguage
        } else {
            "fr" // Fallback vers français
        }
    }
    
    /**
     * Crée un Context Thymeleaf avec la locale appropriée.
     * Phase 3 - Fonctionnalité Langue
     * 
     * @param languageCode Code de la langue (fr, en, es, de, ar)
     * @return Context Thymeleaf configuré avec la locale
     */
    private fun createLocalizedContext(languageCode: String): Context {
        val locale = when (languageCode) {
            "en" -> Locale.ENGLISH
            "es" -> Locale("es")
            "de" -> Locale("de")
            "ar" -> Locale("ar")
            else -> Locale.FRENCH
        }
        return Context(locale)
    }
    
    /**
     * Sélectionne le template email selon la langue.
     * Phase 3 - Fonctionnalité Langue
     * 
     * @param templateName Nom de base du template (ex: "booking-confirmation")
     * @param languageCode Code de la langue (fr, en, es, de, ar)
     * @return Nom du template avec suffixe de langue (ex: "email/booking-confirmation_fr.html")
     */
    private fun getEmailTemplateName(templateName: String, languageCode: String): String {
        val supportedLanguages = setOf("fr", "en", "es", "de", "ar")
        val validLanguage = if (supportedLanguages.contains(languageCode)) languageCode else "fr"
        return "email/${templateName}_$validLanguage"
    }

    // ========== EMAILS DE RÉSERVATION ==========

    /**
     * Envoie un email de confirmation après création d'une réservation.
     * Phase 3 - Fonctionnalité Langue : Utilise la langue préférée de l'utilisateur.
     */
    fun sendBookingConfirmation(booking: Booking) {
        if (!emailEnabled) {
            println("📧 Email désactivé - Confirmation réservation ${booking.id} non envoyée")
            return
        }

        try {
            // Déterminer la langue de l'utilisateur
            val language = determineEmailLanguage(booking.client)
            val context = createLocalizedContext(language)
            
            context.setVariable("booking", booking)
            context.setVariable("salonName", booking.salon?.name ?: "Salon")
            context.setVariable("serviceName", booking.service?.name ?: "Service")
            context.setVariable("staffName", booking.staff?.user?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Non assigné" } ?: "Non assigné")
            context.setVariable("bookingDate", booking.bookingDatetime.format(dateTimeFormatter))
            context.setVariable("duration", "${booking.durationMinutes} minutes")
            context.setVariable("price", String.format("%.2f", booking.priceFinal ?: 0.0) + "€")
            context.setVariable("clientName", booking.client?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Client" } ?: "Client")

            // Sélectionner le template selon la langue
            val templateName = getEmailTemplateName("booking-confirmation", language)
            val htmlContent = templateEngine.process(templateName, context)

            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromEmail)
            helper.setTo(booking.client?.email ?: return)
            
            // Sujet localisé (sera géré dans les templates)
            val subject = when (language) {
                "en" -> "Booking Confirmation - ${booking.salon?.name}"
                "es" -> "Confirmación de reserva - ${booking.salon?.name}"
                "de" -> "Buchungsbestätigung - ${booking.salon?.name}"
                "ar" -> "تأكيد الحجز - ${booking.salon?.name}"
                else -> "Confirmation de votre réservation - ${booking.salon?.name}"
            }
            helper.setSubject(subject)
            helper.setText(htmlContent, true)

            mailSender.send(message)
            println("✅ Email de confirmation envoyé à ${booking.client?.email} (langue: $language)")
        } catch (e: Exception) {
            println("❌ Erreur lors de l'envoi de l'email de confirmation: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Envoie un email de rappel 24h avant la réservation.
     * Phase 3 - Fonctionnalité Langue : Utilise la langue préférée de l'utilisateur.
     */
    fun sendBookingReminder(booking: Booking) {
        if (!emailEnabled) {
            println("📧 Email désactivé - Rappel réservation ${booking.id} non envoyé")
            return
        }

        try {
            // Déterminer la langue de l'utilisateur
            val language = determineEmailLanguage(booking.client)
            val context = createLocalizedContext(language)
            
            context.setVariable("booking", booking)
            context.setVariable("salonName", booking.salon?.name ?: "Salon")
            context.setVariable("serviceName", booking.service?.name ?: "Service")
            context.setVariable("staffName", booking.staff?.user?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Non assigné" } ?: "Non assigné")
            context.setVariable("bookingDate", booking.bookingDatetime.format(dateTimeFormatter))
            context.setVariable("duration", "${booking.durationMinutes} minutes")
            context.setVariable("price", String.format("%.2f", booking.priceFinal ?: 0.0) + "€")
            context.setVariable("clientName", booking.client?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Client" } ?: "Client")
            context.setVariable("salonAddress", booking.salon?.address ?: "")

            // Sélectionner le template selon la langue
            val templateName = getEmailTemplateName("booking-reminder", language)
            val htmlContent = templateEngine.process(templateName, context)

            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromEmail)
            helper.setTo(booking.client?.email ?: return)
            
            // Sujet localisé
            val subject = when (language) {
                "en" -> "Reminder: Your booking tomorrow - ${booking.salon?.name}"
                "es" -> "Recordatorio: Tu reserva mañana - ${booking.salon?.name}"
                "de" -> "Erinnerung: Ihre Buchung morgen - ${booking.salon?.name}"
                "ar" -> "تذكير: حجزك غداً - ${booking.salon?.name}"
                else -> "Rappel : Votre réservation demain - ${booking.salon?.name}"
            }
            helper.setSubject(subject)
            helper.setText(htmlContent, true)

            mailSender.send(message)
            println("✅ Email de rappel envoyé à ${booking.client?.email} (langue: $language)")
        } catch (e: Exception) {
            println("❌ Erreur lors de l'envoi de l'email de rappel: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Envoie un email lors d'un changement de statut de réservation.
     * Phase 3 - Fonctionnalité Langue : Utilise la langue préférée de l'utilisateur.
     */
    fun sendBookingStatusChange(booking: Booking, oldStatus: BookingStatus) {
        if (!emailEnabled) {
            println("📧 Email désactivé - Changement statut réservation ${booking.id} non envoyé")
            return
        }

        // Ne pas envoyer d'email pour les changements internes
        if (oldStatus == booking.status) {
            return
        }

        try {
            // Déterminer la langue de l'utilisateur
            val language = determineEmailLanguage(booking.client)
            val context = createLocalizedContext(language)
            
            context.setVariable("booking", booking)
            context.setVariable("salonName", booking.salon?.name ?: "Salon")
            context.setVariable("serviceName", booking.service?.name ?: "Service")
            context.setVariable("bookingDate", booking.bookingDatetime.format(dateTimeFormatter))
            context.setVariable("oldStatus", oldStatus.getDisplayName())
            context.setVariable("newStatus", booking.status.getDisplayName())
            context.setVariable("clientName", booking.client?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Client" } ?: "Client")

            // Sélectionner le template selon la langue
            val templateName = getEmailTemplateName("booking-status-change", language)
            val htmlContent = templateEngine.process(templateName, context)

            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromEmail)
            helper.setTo(booking.client?.email ?: return)
            
            // Sujet localisé
            val subject = when (language) {
                "en" -> "Booking Update - ${booking.salon?.name}"
                "es" -> "Actualización de reserva - ${booking.salon?.name}"
                "de" -> "Buchungsaktualisierung - ${booking.salon?.name}"
                "ar" -> "تحديث الحجز - ${booking.salon?.name}"
                else -> "Mise à jour de votre réservation - ${booking.salon?.name}"
            }
            helper.setSubject(subject)
            helper.setText(htmlContent, true)

            mailSender.send(message)
            println("✅ Email de changement de statut envoyé à ${booking.client?.email} (langue: $language)")
        } catch (e: Exception) {
            println("❌ Erreur lors de l'envoi de l'email de changement de statut: ${e.message}")
            e.printStackTrace()
        }
    }

    // ========== EMAILS DE FILE D'ATTENTE ==========

    /**
     * Envoie un email de notification lorsque le tour du client approche dans la file d'attente.
     * Phase 3 - Fonctionnalité Langue : Utilise la langue préférée de l'utilisateur.
     */
    fun sendQueueNotification(queueEntry: QueueEntry, position: Int, estimatedWaitMinutes: Int) {
        if (!emailEnabled) {
            println("📧 Email désactivé - Notification file d'attente ${queueEntry.id} non envoyée")
            return
        }

        try {
            // Déterminer la langue de l'utilisateur
            val language = determineEmailLanguage(queueEntry.client)
            val context = createLocalizedContext(language)
            
            context.setVariable("queueEntry", queueEntry)
            context.setVariable("salonName", queueEntry.queue?.salon?.name ?: "Salon")
            context.setVariable("serviceName", queueEntry.requestedService?.name ?: "Service")
            context.setVariable("estimatedWaitTime", "$estimatedWaitMinutes minutes")
            context.setVariable("position", position)
            context.setVariable("clientName", queueEntry.client?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Client" } ?: "Client")
            context.setVariable("salonAddress", queueEntry.queue?.salon?.address ?: "")

            // Sélectionner le template selon la langue
            val templateName = getEmailTemplateName("queue-notification", language)
            val htmlContent = templateEngine.process(templateName, context)

            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromEmail)
            helper.setTo(queueEntry.client?.email ?: return)
            
            // Sujet localisé
            val subject = when (language) {
                "en" -> "Your turn is coming - ${queueEntry.queue?.salon?.name}"
                "es" -> "Tu turno se acerca - ${queueEntry.queue?.salon?.name}"
                "de" -> "Ihr Termin rückt näher - ${queueEntry.queue?.salon?.name}"
                "ar" -> "دورك يقترب - ${queueEntry.queue?.salon?.name}"
                else -> "Votre tour approche - ${queueEntry.queue?.salon?.name}"
            }
            helper.setSubject(subject)
            helper.setText(htmlContent, true)

            mailSender.send(message)
            println("✅ Email de notification file d'attente envoyé à ${queueEntry.client?.email} (langue: $language)")
        } catch (e: Exception) {
            println("❌ Erreur lors de l'envoi de l'email de file d'attente: ${e.message}")
            e.printStackTrace()
        }
    }

    // ========== MÉTHODE UTILITAIRE ==========

    /**
     * Envoie un email simple (sans template).
     */
    fun sendSimpleEmail(to: String, subject: String, text: String) {
        if (!emailEnabled) {
            println("📧 Email désactivé - Email simple non envoyé à $to")
            return
        }

        try {
            val message = SimpleMailMessage()
            message.setFrom(fromEmail)
            message.setTo(to)
            message.setSubject(subject)
            message.setText(text)
            mailSender.send(message)
            println("✅ Email simple envoyé à $to")
        } catch (e: Exception) {
            println("❌ Erreur lors de l'envoi de l'email simple: ${e.message}")
            e.printStackTrace()
        }
    }
}

