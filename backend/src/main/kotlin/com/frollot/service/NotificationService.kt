package com.frollot.service

import com.frollot.model.Booking
import com.frollot.model.DeviceToken
import com.frollot.model.QueueEntry
import com.frollot.repository.DeviceTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service de gestion des notifications push FCM.
 *
 * Gère l'envoi de notifications push pour :
 * - Changements de statut de réservation
 * - Notifications de file d'attente
 * - Nouveaux likes/commentaires (optionnel)
 *
 * NOTE: Si Firebase n'est pas configuré (firebase.enabled=false),
 * FirebaseMessaging sera null et les notifications seront simplement ignorées.
 */
@Service
@Transactional
class NotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    @Autowired(required = false) private val firebaseMessaging: FirebaseMessaging?
) {

    // ========== NOTIFICATIONS DE RÉSERVATION ==========

    /**
     * Envoie une notification push pour un changement de statut de réservation.
     */
    fun sendBookingStatusNotification(booking: Booking, statusLabel: String) {
        val userId = booking.client?.id ?: return
        val tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)

        if (tokens.isEmpty()) {
            println("📱 Aucun token actif pour l'utilisateur $userId")
            return
        }

        val title = "Mise à jour de réservation"
        val body = "Votre réservation chez ${booking.salon?.name} est maintenant : $statusLabel"

        tokens.forEach { token ->
            try {
                sendNotification(
                    token = token.token,
                    title = title,
                    body = body,
                    data = mapOf(
                        "type" to "booking_status",
                        "booking_id" to (booking.id ?: ""),
                        "status" to statusLabel
                    )
                )
                updateLastUsedAt(token)
            } catch (e: Exception) {
                println("❌ Erreur lors de l'envoi de notification à ${token.token}: ${e.message}")
                // Désactiver le token en cas d'erreur (token invalide)
                if (e.message?.contains("invalid") == true || e.message?.contains("not found") == true) {
                    token.isActive = false
                    deviceTokenRepository.save(token)
                }
            }
        }
    }

    // ========== NOTIFICATIONS DE FILE D'ATTENTE ==========

    /**
     * Envoie une notification push lorsque le tour du client approche.
     */
    fun sendQueueNotification(queueEntry: QueueEntry, position: Int, estimatedWaitMinutes: Int) {
        val userId = queueEntry.client?.id ?: return
        val tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)

        if (tokens.isEmpty()) {
            println("📱 Aucun token actif pour l'utilisateur $userId")
            return
        }

        val title = "Votre tour approche"
        val body = when {
            position == 1 -> "C'est votre tour ! Rendez-vous au salon ${queueEntry.queue?.salon?.name}"
            position == 2 -> "Vous êtes le prochain ! Attente estimée : $estimatedWaitMinutes minutes"
            else -> "Position dans la file : $position. Attente estimée : $estimatedWaitMinutes minutes"
        }

        tokens.forEach { token ->
            try {
                sendNotification(
                    token = token.token,
                    title = title,
                    body = body,
                    data = mapOf(
                        "type" to "queue_notification",
                        "queue_entry_id" to (queueEntry.id ?: ""),
                        "position" to position.toString(),
                        "estimated_wait_minutes" to estimatedWaitMinutes.toString()
                    )
                )
                updateLastUsedAt(token)
            } catch (e: Exception) {
                println("❌ Erreur lors de l'envoi de notification à ${token.token}: ${e.message}")
                if (e.message?.contains("invalid") == true || e.message?.contains("not found") == true) {
                    token.isActive = false
                    deviceTokenRepository.save(token)
                }
            }
        }
    }

    // ========== NOTIFICATIONS SOCIALES (OPTIONNEL) ==========

    /**
     * Envoie une notification push pour un nouveau like.
     */
    fun sendLikeNotification(userId: String, postId: String, likerName: String) {
        val tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)

        tokens.forEach { token ->
            try {
                sendNotification(
                    token = token.token,
                    title = "Nouveau like",
                    body = "$likerName a aimé votre publication",
                    data = mapOf(
                        "type" to "like",
                        "post_id" to postId
                    )
                )
                updateLastUsedAt(token)
            } catch (e: Exception) {
                println("❌ Erreur lors de l'envoi de notification: ${e.message}")
            }
        }
    }

    /**
     * Envoie une notification push pour un nouveau commentaire.
     */
    fun sendCommentNotification(userId: String, postId: String, commenterName: String) {
        val tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)

        tokens.forEach { token ->
            try {
                sendNotification(
                    token = token.token,
                    title = "Nouveau commentaire",
                    body = "$commenterName a commenté votre publication",
                    data = mapOf(
                        "type" to "comment",
                        "post_id" to postId
                    )
                )
                updateLastUsedAt(token)
            } catch (e: Exception) {
                println("❌ Erreur lors de l'envoi de notification: ${e.message}")
            }
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Envoie une notification FCM.
     */
    private fun sendNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        if (firebaseMessaging == null) {
            println("⚠️ Firebase non configuré - notification ignorée: $title - $body")
            return
        }

        try {
            val notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build()

            val message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .build()

            val response = firebaseMessaging.send(message)
            println("✅ Notification envoyée: $response")
        } catch (e: Exception) {
            println("❌ Erreur lors de l'envoi de notification: ${e.message}")
            throw e
        }
    }

    /**
     * Met à jour last_used_at pour un token.
     */
    private fun updateLastUsedAt(token: DeviceToken) {
        token.lastUsedAt = LocalDateTime.now()
        deviceTokenRepository.save(token)
    }

    /**
     * Nettoie les tokens inactifs depuis plus de 30 jours.
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 2 * * *") // 2h du matin tous les jours
    fun cleanupInactiveTokens() {
        val cutoffDate = LocalDateTime.now().minusDays(30)
        deviceTokenRepository.deleteInactiveTokensBefore(cutoffDate)
        println("🧹 Nettoyage des tokens inactifs terminé")
    }
}

