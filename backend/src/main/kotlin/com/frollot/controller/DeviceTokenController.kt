package com.frollot.controller

import com.frollot.dto.RegisterDeviceTokenRequest
import com.frollot.model.DeviceToken
import com.frollot.model.User
import com.frollot.repository.DeviceTokenRepository
import com.frollot.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/device-tokens")
@Tag(
    name = "Device Tokens",
    description = "API de gestion des tokens FCM pour notifications push"
)
class DeviceTokenController(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val userRepository: UserRepository
) {

    /**
     * Enregistre ou met à jour un token de device.
     */
    @Operation(
        summary = "Enregistrer un token de device",
        description = "Enregistre un token FCM pour recevoir des notifications push"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Token enregistré"),
            ApiResponse(responseCode = "400", description = "Données invalides"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun registerToken(
        @RequestBody request: RegisterDeviceTokenRequest,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        val userId = authentication.name
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur non trouvé") }

        // Vérifier si le token existe déjà pour cet utilisateur
        val existingToken = deviceTokenRepository.findByToken(request.token)
        
        if (existingToken != null && existingToken.user?.id == userId) {
            // Mettre à jour le token existant
            existingToken.isActive = true
            existingToken.deviceInfo = request.deviceInfo
            existingToken.platform = request.platform ?: "android"
            deviceTokenRepository.save(existingToken)
            return ResponseEntity.ok(mapOf("message" to "Token mis à jour"))
        } else if (existingToken != null) {
            // Token utilisé par un autre utilisateur, le désactiver
            existingToken.isActive = false
            deviceTokenRepository.save(existingToken)
        }

        // Créer un nouveau token
        val deviceToken = DeviceToken(
            id = java.util.UUID.randomUUID().toString(),
            user = user,
            token = request.token,
            platform = request.platform ?: "android",
            deviceInfo = request.deviceInfo,
            isActive = true
        )

        deviceTokenRepository.save(deviceToken)

        return ResponseEntity.ok(mapOf("message" to "Token enregistré avec succès"))
    }

    /**
     * Supprime un token de device.
     */
    @Operation(summary = "Supprimer un token", description = "Désactive un token FCM")
    @DeleteMapping("/{token}")
    @PreAuthorize("isAuthenticated()")
    fun deleteToken(
        @PathVariable token: String,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        val userId = authentication.name
        val deviceToken = deviceTokenRepository.findByToken(token)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Token non trouvé"))

        if (deviceToken.user?.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Non autorisé"))
        }

        deviceToken.isActive = false
        deviceTokenRepository.save(deviceToken)

        return ResponseEntity.ok(mapOf("message" to "Token désactivé"))
    }

    /**
     * Désactive tous les tokens d'un utilisateur (logout all devices).
     */
    @Operation(summary = "Désactiver tous les tokens", description = "Désactive tous les tokens FCM d'un utilisateur")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    fun deleteAllTokens(
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        val userId = authentication.name
        deviceTokenRepository.deactivateAllByUserId(userId)
        return ResponseEntity.ok(mapOf("message" to "Tous les tokens ont été désactivés"))
    }
}

