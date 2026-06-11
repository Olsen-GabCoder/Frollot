package com.frollot.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Logger pour afficher la configuration au démarrage.
 * À SUPPRIMER EN PRODUCTION.
 */
@Component
class StartupConfigLogger(
    @Autowired(required = false) private val multipartProperties: MultipartProperties?
) {

    @EventListener(ApplicationReadyEvent::class)
    fun logConfigurationOnStartup() {
        val separator = "=".repeat(80)

        println(separator)
        println("🔧 CONFIGURATION MULTIPART AU DÉMARRAGE")
        println(separator)

        if (multipartProperties == null) {
            println("❌ ERREUR CRITIQUE: MultipartProperties bean NOT FOUND!")
            println("❌ La configuration spring.servlet.multipart n'est PAS chargée!")
            println("❌ Vérifiez votre application.yml")
        } else {
            println("✅ MultipartProperties bean trouvé")
            println("   → Enabled: ${multipartProperties.enabled}")
            println("   → Max File Size: ${multipartProperties.maxFileSize}")
            println("   → Max Request Size: ${multipartProperties.maxRequestSize}")
            println("   → File Size Threshold: ${multipartProperties.fileSizeThreshold}")
            println("   → Location: ${multipartProperties.location ?: "default (temp)"}")
        }

        println(separator)
    }
}