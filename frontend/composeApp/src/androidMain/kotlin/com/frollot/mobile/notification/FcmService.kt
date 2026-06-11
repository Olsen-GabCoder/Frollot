package com.frollot.mobile.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service FCM pour recevoir les notifications push Android.
 *
 * NOTE: Pour une intégration complète :
 * 1. Ajouter le plugin Google Services dans build.gradle.kts
 * 2. Télécharger google-services.json depuis Firebase Console
 * 3. Placer google-services.json dans frontend/composeApp/src/androidMain/
 * 4. Déclarer ce service dans AndroidManifest.xml
 */
class FcmService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FcmService"
    }

    /**
     * Appelé lorsqu'un nouveau token FCM est généré.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nouveau token FCM: $token")
        
        // TODO: Envoyer le token au backend via API
        // api.registerDeviceToken(token)
    }

    /**
     * Appelé lorsqu'une notification push est reçue.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Notification reçue de: ${remoteMessage.from}")

        // Vérifier si la notification contient des données
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Données de notification: ${remoteMessage.data}")
            handleNotificationData(remoteMessage.data)
        }

        // Vérifier si la notification contient un payload de notification
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Titre: ${notification.title}")
            Log.d(TAG, "Corps: ${notification.body}")
            
            // TODO: Afficher la notification dans l'UI
            // showNotification(notification.title, notification.body, remoteMessage.data)
        }
    }

    /**
     * Traite les données de la notification.
     */
    private fun handleNotificationData(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "booking_status" -> {
                val bookingId = data["booking_id"]
                val status = data["status"]
                Log.d(TAG, "Changement de statut de réservation: $bookingId -> $status")
                // TODO: Naviguer vers l'écran de détails de réservation
            }
            "queue_notification" -> {
                val queueEntryId = data["queue_entry_id"]
                val position = data["position"]
                val estimatedWait = data["estimated_wait_minutes"]
                Log.d(TAG, "Notification file d'attente: $queueEntryId, position $position, attente $estimatedWait min")
                // TODO: Afficher une notification ou naviguer vers l'écran de file d'attente
            }
            "like" -> {
                val postId = data["post_id"]
                Log.d(TAG, "Nouveau like sur post: $postId")
                // TODO: Rafraîchir le feed social
            }
            "comment" -> {
                val postId = data["post_id"]
                Log.d(TAG, "Nouveau commentaire sur post: $postId")
                // TODO: Rafraîchir le feed social
            }
        }
    }
}

