package com.frollot.dto

import com.frollot.model.RefreshToken
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// ========== CHANGEMENT DE MOT DE PASSE ==========

/**
 * DTO pour la requête de changement de mot de passe.
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "Le mot de passe actuel est obligatoire")
    val currentPassword: String,
    
    @field:NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @field:Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
    val newPassword: String,
    
    @field:NotBlank(message = "La confirmation du mot de passe est obligatoire")
    val confirmPassword: String
)

/**
 * DTO de réponse pour le changement de mot de passe.
 */
data class ChangePasswordResponse(
    val success: Boolean,
    val message: String
)

// ========== SESSIONS ACTIVES ==========

/**
 * DTO représentant une session active (refresh token) avec informations complètes.
 */
data class SessionResponse(
    val id: Long,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val isCurrent: Boolean,
    val deviceName: String? = null,
    val deviceType: String? = null,  // "mobile", "desktop", "tablet", "unknown"
    val ipAddress: String? = null,
    val location: String? = null,
    val lastUsedAt: LocalDateTime? = null,
    val isActive: Boolean = true,
    val browser: String? = null,
    val operatingSystem: String? = null
) {
    companion object {
        fun fromEntity(token: RefreshToken, currentTokenId: Long? = null): SessionResponse {
            val userAgentInfo = parseUserAgent(token.userAgent)
            return SessionResponse(
                id = token.id!!,
                createdAt = token.createdAt,
                expiresAt = token.expiresAt,
                isCurrent = token.id == currentTokenId,
                deviceName = token.deviceName ?: userAgentInfo.deviceName,
                deviceType = token.deviceType ?: userAgentInfo.deviceType,
                ipAddress = token.ipAddress,
                location = token.location,
                lastUsedAt = token.lastUsedAt ?: token.createdAt,
                isActive = !token.revoked && !token.isExpired(),
                browser = userAgentInfo.browser,
                operatingSystem = userAgentInfo.operatingSystem
            )
        }
        
        private fun parseUserAgent(userAgent: String?): UserAgentInfo {
            if (userAgent.isNullOrBlank()) {
                return UserAgentInfo()
            }
            
            // Parse browser
            val browser = when {
                userAgent.contains("Chrome", ignoreCase = true) && 
                    !userAgent.contains("Edg", ignoreCase = true) -> "Chrome"
                userAgent.contains("Firefox", ignoreCase = true) -> "Firefox"
                userAgent.contains("Safari", ignoreCase = true) && 
                    !userAgent.contains("Chrome", ignoreCase = true) -> "Safari"
                userAgent.contains("Edg", ignoreCase = true) -> "Edge"
                userAgent.contains("Opera", ignoreCase = true) || 
                    userAgent.contains("OPR", ignoreCase = true) -> "Opera"
                userAgent.contains("Ktor", ignoreCase = true) -> "Frollot App"
                else -> null
            }
            
            // Parse OS
            val os = when {
                userAgent.contains("Windows", ignoreCase = true) -> "Windows"
                userAgent.contains("Mac OS", ignoreCase = true) -> "macOS"
                userAgent.contains("Android", ignoreCase = true) -> "Android"
                userAgent.contains("iPhone", ignoreCase = true) || 
                    userAgent.contains("iPad", ignoreCase = true) -> "iOS"
                userAgent.contains("Linux", ignoreCase = true) -> "Linux"
                else -> null
            }
            
            // Parse device type
            val deviceType = when {
                userAgent.contains("Mobile", ignoreCase = true) || 
                    userAgent.contains("Android", ignoreCase = true) ||
                    userAgent.contains("iPhone", ignoreCase = true) -> "mobile"
                userAgent.contains("iPad", ignoreCase = true) || 
                    userAgent.contains("Tablet", ignoreCase = true) -> "tablet"
                else -> "desktop"
            }
            
            // Generate device name
            val deviceName = when {
                os == "Android" -> "Android Device"
                os == "iOS" && userAgent.contains("iPhone") -> "iPhone"
                os == "iOS" && userAgent.contains("iPad") -> "iPad"
                os == "Windows" -> "PC Windows"
                os == "macOS" -> "Mac"
                os == "Linux" -> "Linux PC"
                else -> null
            }
            
            return UserAgentInfo(
                browser = browser,
                operatingSystem = os,
                deviceType = deviceType,
                deviceName = deviceName
            )
        }
    }
}

/**
 * Info extraite du User-Agent.
 */
private data class UserAgentInfo(
    val browser: String? = null,
    val operatingSystem: String? = null,
    val deviceType: String? = null,
    val deviceName: String? = null
)

/**
 * DTO de réponse pour la liste des sessions.
 */
data class SessionsListResponse(
    val sessions: List<SessionResponse>,
    val totalCount: Int,
    val currentSessionId: Long? = null
)

/**
 * DTO de réponse pour la révocation de sessions.
 */
data class RevokeSessionResponse(
    val success: Boolean,
    val message: String,
    val revokedCount: Int = 1
)

// ========== CHANGEMENT D'EMAIL ==========

/**
 * DTO pour la requête de changement d'email.
 */
data class ChangeEmailRequest(
    @field:NotBlank(message = "Le nouvel email est obligatoire")
    @field:jakarta.validation.constraints.Email(message = "Format d'email invalide")
    val newEmail: String,
    
    @field:NotBlank(message = "Le mot de passe est obligatoire")
    val password: String
)

/**
 * DTO de réponse pour le changement d'email.
 * Note V040 : à la demande (PUT /me/email), newEmail = email en attente (pending).
 * À la confirmation (POST /me/email/confirm), newEmail = email actif final.
 */
data class ChangeEmailResponse(
    val success: Boolean,
    val message: String,
    val newEmail: String? = null
)

/**
 * DTO pour la confirmation d'un changement d'email (V040).
 */
data class ConfirmEmailChangeRequest(
    @field:NotBlank(message = "Le code de vérification est obligatoire")
    val token: String
)

// ========== CHANGEMENT DE TÉLÉPHONE ==========

/**
 * DTO pour la requête de changement de téléphone (durci, incrément 1).
 *
 * Numéro DÉCLARATIF (aucune vérification OTP/SMS — couche future).
 * - newPhone : E.164 attendu (+XXXXXXXX). Null ou blanc = SUPPRESSION du numéro
 *   (normalisé NULL en base, jamais '' — l'UNIQUE sur phone_number l'exige).
 * - phonePublic : visibilité choisie (V045). false (défaut) = privé.
 */
data class ChangePhoneRequest(
    val newPhone: String? = null,

    val phonePublic: Boolean = false,

    @field:NotBlank(message = "Le mot de passe est obligatoire")
    val password: String
)

/**
 * DTO de réponse pour le changement de téléphone.
 */
data class ChangePhoneResponse(
    val success: Boolean,
    val message: String,
    val newPhone: String? = null,
    val phonePublic: Boolean = false
)

// ========== SUPPRESSION DE COMPTE ==========

/**
 * DTO pour la requête de suppression de compte.
 */
data class DeleteAccountRequest(
    @field:NotBlank(message = "Le mot de passe est obligatoire")
    val password: String,
    
    val confirmDeletion: Boolean = false
)

/**
 * DTO de réponse pour la suppression de compte.
 */
data class DeleteAccountResponse(
    val success: Boolean,
    val message: String
)

