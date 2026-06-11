package com.frollot.mobile.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val userType: UserType,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val preferredLanguage: String? = null, // Phase 3 - Fonctionnalité Langue
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val avatarUrl: String? = null,
    val verificationType: VerificationType? = null, // CORRECTION : Champ manquant ajouté (Phase H.2)
    val isFollowedByCurrentUser: Boolean? = null, // CORRECTION : Champ ajouté pour compatibilité UserResponse
    val followersCount: Long? = null // CORRECTION : Champ ajouté pour compatibilité UserResponse
)

@Serializable
enum class UserType {
    client,
    hairstylist,
    salon_owner,
    admin
}

@Serializable
data class LoginRequest(
    val email: String = "",
    val password: String = ""
)

@Serializable
data class RegisterRequest(
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val userType: UserType = UserType.client
)
