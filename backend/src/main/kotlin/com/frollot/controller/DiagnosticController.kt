package com.frollot.controller

import com.frollot.service.EmailConfigurationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

/**
 * Contrôleur de diagnostic pour vérifier les configurations système.
 * À SUPPRIMER EN PRODUCTION !
 */
@RestController
@RequestMapping("/api/dev/diagnostic")
@CrossOrigin(origins = ["*"])
@Tag(name = "Diagnostic", description = "Endpoints de diagnostic système")
class DiagnosticController(
    @Autowired(required = false) private val multipartProperties: MultipartProperties?,
    private val emailConfigurationService: EmailConfigurationService
) {

    /**
     * Vérifie la configuration multipart.
     */
    @GetMapping("/multipart-config")
    fun getMultipartConfig(): ResponseEntity<Map<String, Any?>> {
        val config = mutableMapOf<String, Any?>()

        if (multipartProperties == null) {
            config["status"] = "ERROR"
            config["message"] = "MultipartProperties bean NOT FOUND - multipart NOT configured!"
            config["multipartEnabled"] = false
        } else {
            config["status"] = "OK"
            config["message"] = "Multipart configuration found"
            config["multipartEnabled"] = multipartProperties.enabled
            config["maxFileSize"] = multipartProperties.maxFileSize.toString()
            config["maxRequestSize"] = multipartProperties.maxRequestSize.toString()
            config["fileSizeThreshold"] = multipartProperties.fileSizeThreshold.toString()
            config["location"] = multipartProperties.location ?: "default (temp)"
        }

        return ResponseEntity.ok(config)
    }

    /**
     * Vérifie la configuration email actuelle.
     */
    @Operation(summary = "Configuration email actuelle")
    @GetMapping("/email-config")
    fun getEmailConfiguration(): ResponseEntity<Map<String, Any>> {
        val config = emailConfigurationService.getConfigurationSummary()
        val effectiveMode = config["effectiveMode"] as EmailConfigurationService.EmailMode

        return ResponseEntity.ok(mapOf(
            "emailConfiguration" to config,
            "effectiveMode" to effectiveMode.name,
            "isSmtpConfigured" to emailConfigurationService.isSmtpConfigured(),
            "devRedirectEmail" to emailConfigurationService.getDevRedirectEmail(),
            "description" to getModeDescription(effectiveMode)
        ))
    }

    private fun getModeDescription(mode: EmailConfigurationService.EmailMode): String {
        return when (mode) {
            EmailConfigurationService.EmailMode.PRODUCTION ->
                "Envoi réel d'emails en production"
            EmailConfigurationService.EmailMode.DEV_REDIRECT ->
                "Envoi d'emails sur adresse de test en développement"
            EmailConfigurationService.EmailMode.DEV_SEND ->
                "Envoi réel d'emails même en développement"
            EmailConfigurationService.EmailMode.DEV_LOG ->
                "Logging des tokens uniquement (pas d'envoi)"
            EmailConfigurationService.EmailMode.DISABLED ->
                "Service d'email désactivé"
        }
    }
}