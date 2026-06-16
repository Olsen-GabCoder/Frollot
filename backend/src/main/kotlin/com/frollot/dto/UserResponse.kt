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
    val phonePublic: Boolean = false, // V045 — visibilité choisie par l'utilisateur
    val isVerified: Boolean,
    val emailVerified: Boolean = false,
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val isActive: Boolean,
    val avatarUrl: String?,
    val preferredLanguage: String? = null, // Phase 3 - Fonctionnalité Langue
    val createdAt: LocalDateTime?,
    val isFollowedByCurrentUser: Boolean? = null,
    val followersCount: Long? = null
) {
    companion object {
        /**
         * V045 — règle de visibilité du numéro APPLIQUÉE ICI, point unique.
         *
         * PAR DÉFAUT (includePrivatePhone=false) : vue PUBLIQUE — phoneNumber n'est
         * présent que si l'utilisateur a choisi phone_public=true. Tout futur appelant
         * est donc privé-par-défaut. Les vues propriétaire (GET/PUT /me, avatar) et
         * admin (liste, vérification) passent explicitement includePrivatePhone=true.
         *
         * Le canal transactionnel (BookingDto.clientPhone) ne passe PAS par ce DTO :
         * il reste toujours visible du salon.
         */
        fun fromEntity(user: User, includePrivatePhone: Boolean = false): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                userType = user.userType,
                firstName = user.firstName,
                lastName = user.lastName,
                phoneNumber = if (includePrivatePhone || user.phonePublic) user.phoneNumber else null,
                phonePublic = user.phonePublic,
                isVerified = user.isVerified,
                emailVerified = user.emailVerified,
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
