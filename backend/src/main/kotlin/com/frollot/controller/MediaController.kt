package com.frollot.controller

import com.frollot.model.User
import com.frollot.service.MediaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Contrôleur REST pour la gestion des fichiers média.
 *
 * Expose une API pour :
 * - Upload d'images (authentification requise)
 * - Récupération d'images (via static resource handler - publique)
 */
@RestController
@RequestMapping("/api/media")
@Tag(
    name = "Gestion des Médias",
    description = "API pour l'upload et la gestion des fichiers média (images)"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:9090", "http://10.0.2.2:9090"],
    allowCredentials = "true"
)
class MediaController(
    private val mediaService: MediaService,
    @Value("\${server.port:9090}") private val serverPort: String
) {

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Récupère l'ID de l'utilisateur authentifié depuis le SecurityContext.
     */
    private fun getAuthenticatedUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifié")
        }
        return (authentication.principal as User).id!!
    }

    // ========== ENDPOINTS ==========

    /**
     * Upload un fichier image.
     *
     * @param file Le fichier à uploader
     * @return L'URL complète du fichier uploadé
     */
    @Operation(
        summary = "Uploader une image",
        description = "Upload un fichier image et retourne son URL publique. Authentification requise."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Fichier uploadé avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Fichier invalide"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            )
        ]
    )
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        try {
            // Récupérer l'utilisateur authentifié
            val authenticatedUserId = getAuthenticatedUserId()

            // Sauvegarder le fichier
            val fileUrl = mediaService.saveFile(file)

            // Construire l'URL complète
            val baseUrl = "http://10.0.2.2:$serverPort"
            val fullUrl = "$baseUrl$fileUrl"

            // Créer la réponse avec des valeurs non-null
            val response = mapOf(
                "url" to fullUrl,
                "path" to fileUrl,
                "uploadedBy" to authenticatedUserId,
                "filename" to (file.originalFilename ?: "file")
            )

            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf(
                    "error" to "Validation error",
                    "message" to (e.message ?: "Fichier invalide")
                ))
        } catch (e: IllegalStateException) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf(
                    "error" to "Unauthorized",
                    "message" to (e.message ?: "Authentication required")
                ))
        } catch (e: Exception) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf(
                    "error" to "Upload error",
                    "message" to (e.message ?: "Erreur lors de l'upload")
                ))
        }
    }

    // ========== GESTION DES ERREURS ==========

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentication required")
            ))
    }
}