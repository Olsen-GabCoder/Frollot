package com.frollot.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.FileInputStream
import java.io.InputStream

/**
 * Configuration Firebase pour les notifications push FCM.
 *
 * Cette configuration est conditionnelle : elle ne s'active que si :
 * - La propriété 'firebase.enabled' est à 'true' (par défaut: false)
 * - ET un fichier de configuration Firebase est disponible
 *
 * Pour activer Firebase :
 * 1. Télécharger le fichier service-account-key.json depuis Firebase Console
 * 2. Le placer dans src/main/resources/firebase/service-account-key.json
 * 3. OU définir firebase.service-account-path dans application.yml
 * 4. Définir firebase.enabled=true dans application.yml
 */
@Configuration
class FirebaseConfig {

    @Bean
    @ConditionalOnProperty(
        name = ["firebase.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun firebaseMessaging(): FirebaseMessaging {
        val serviceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
            ?: "firebase/service-account-key.json"

        val inputStream: InputStream = try {
            // Essayer d'abord depuis le classpath
            val resource: Resource = ClassPathResource(serviceAccountPath)
            if (resource.exists()) {
                resource.inputStream
            } else {
                // Essayer depuis le système de fichiers
                val file = java.io.File(serviceAccountPath)
                if (file.exists()) {
                    FileInputStream(file)
                } else {
                    throw IllegalStateException(
                        "Fichier de configuration Firebase introuvable: $serviceAccountPath\n" +
                                "Placez le fichier service-account-key.json dans src/main/resources/firebase/ " +
                                "ou définissez FIREBASE_SERVICE_ACCOUNT_PATH"
                    )
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Erreur lors du chargement de la configuration Firebase: ${e.message}",
                e
            )
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(inputStream))
            .build()

        // Initialiser FirebaseApp seulement s'il n'existe pas déjà
        val app = try {
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(options)
        }

        println("✅ Firebase initialisé avec succès")
        return FirebaseMessaging.getInstance(app)
    }
}

