package com.frollot.dto

import com.frollot.model.User
import com.frollot.model.UserType
import com.frollot.model.VerificationType
import java.time.LocalDateTime

/**
 * DTO pour représenter un utilisateur dans les réponses API.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 * Phase H.2 - Vérification Salons/Coiffeurs (ajout verificationType)
 */
data class UserResponse(
    val id: String,
    val email: String,
    val userType: UserType,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val isVerified: Boolean,
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val isActive: Boolean,
    val avatarUrl: String?,
    val preferredLanguage: String? = null, // Phase 3 - Fonctionnalité Langue
    val createdAt: LocalDateTime?,
    val isFollowedByCurrentUser: Boolean? = null,
    val followersCount: Long? = null
) {
    companion object {
        fun fromEntity(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                userType = user.userType,
                firstName = user.firstName,
                lastName = user.lastName,
                phoneNumber = user.phoneNumber,
                isVerified = user.isVerified,
                verificationType = user.verificationType, // Phase H.2
                isActive = user.isActive,
                avatarUrl = user.avatarUrl,
                preferredLanguage = user.preferredLanguage, // Phase 3
                createdAt = user.createdAt,
                isFollowedByCurrentUser = null,
                followersCount = null
            )
        }
    }
}
