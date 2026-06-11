package com.frollot.mobile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ========== CHANGEMENT DE MOT DE PASSE ==========

/**
 * Requête pour changer le mot de passe.
 */
@Serializable
data class ChangePasswordRequest(
    @SerialName("currentPassword")
    val currentPassword: String,
    
    @SerialName("newPassword")
    val newPassword: String,
    
    @SerialName("confirmPassword")
    val confirmPassword: String
)

/**
 * Réponse du changement de mot de passe.
 */
@Serializable
data class ChangePasswordResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String
)

// ========== SESSIONS ACTIVES ==========

/**
 * Représente une session active (refresh token) avec informations détaillées.
 */
@Serializable
data class SessionInfo(
    @SerialName("id")
    val id: Long,
    
    @SerialName("createdAt")
    val createdAt: String,
    
    @SerialName("expiresAt")
    val expiresAt: String,
    
    @SerialName("isCurrent")
    val isCurrent: Boolean = false,
    
    @SerialName("deviceName")
    val deviceName: String? = null,
    
    @SerialName("deviceType")
    val deviceType: String? = null,  // "mobile", "desktop", "tablet", "unknown"
    
    @SerialName("ipAddress")
    val ipAddress: String? = null,
    
    @SerialName("location")
    val location: String? = null,
    
    @SerialName("lastUsedAt")
    val lastUsedAt: String? = null,
    
    @SerialName("isActive")
    val isActive: Boolean = true,
    
    @SerialName("browser")
    val browser: String? = null,
    
    @SerialName("operatingSystem")
    val operatingSystem: String? = null
) {
    /**
     * Retourne le nom d'affichage du device.
     */
    val displayName: String
        get() = when {
            !deviceName.isNullOrBlank() -> deviceName
            !browser.isNullOrBlank() && !operatingSystem.isNullOrBlank() -> "$browser sur $operatingSystem"
            !operatingSystem.isNullOrBlank() -> operatingSystem
            !browser.isNullOrBlank() -> browser
            else -> when (deviceType) {
                "mobile" -> "Appareil mobile"
                "tablet" -> "Tablette"
                "desktop" -> "Ordinateur"
                else -> "Appareil inconnu"
            }
        }
    
    /**
     * Retourne une description courte de l'appareil.
     */
    val shortDescription: String
        get() = buildString {
            operatingSystem?.let { append(it) }
            browser?.let { 
                if (isNotEmpty()) append(" • ")
                append(it) 
            }
            if (isEmpty()) {
                append(when (deviceType) {
                    "mobile" -> "Mobile"
                    "tablet" -> "Tablette"
                    "desktop" -> "Desktop"
                    else -> "Inconnu"
                })
            }
        }
    
    /**
     * Indique si la session a été utilisée récemment (dans les 5 dernières minutes).
     */
    val isRecentlyActive: Boolean
        get() = isCurrent // Pour l'instant, seule la session courante est "active"
}

/**
 * Réponse de la liste des sessions.
 */
@Serializable
data class SessionsListResponse(
    @SerialName("sessions")
    val sessions: List<SessionInfo>,
    
    @SerialName("totalCount")
    val totalCount: Int,
    
    @SerialName("currentSessionId")
    val currentSessionId: Long? = null
)

/**
 * Réponse de révocation de session.
 */
@Serializable
data class RevokeSessionResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("revokedCount")
    val revokedCount: Int = 1
)

// ========== CHANGEMENT D'EMAIL ==========

/**
 * Requête pour changer l'email.
 */
@Serializable
data class ChangeEmailRequest(
    @SerialName("newEmail")
    val newEmail: String,
    
    @SerialName("password")
    val password: String
)

/**
 * Réponse du changement d'email.
 */
@Serializable
data class ChangeEmailResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("newEmail")
    val newEmail: String? = null
)

// ========== CHANGEMENT DE TÉLÉPHONE ==========

/**
 * Requête pour changer le téléphone.
 */
@Serializable
data class ChangePhoneRequest(
    @SerialName("newPhone")
    val newPhone: String?,
    
    @SerialName("password")
    val password: String
)

/**
 * Réponse du changement de téléphone.
 */
@Serializable
data class ChangePhoneResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("newPhone")
    val newPhone: String? = null
)

// ========== SUPPRESSION DE COMPTE ==========

/**
 * Requête pour supprimer le compte.
 */
@Serializable
data class DeleteAccountRequest(
    @SerialName("password")
    val password: String,
    
    @SerialName("confirmDeletion")
    val confirmDeletion: Boolean = false
)

/**
 * Réponse de la suppression de compte.
 */
@Serializable
data class DeleteAccountResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String
)

