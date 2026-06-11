package com.frollot.service

import com.frollot.dto.*
import com.frollot.model.*
import com.frollot.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service de gestion de la vérification des utilisateurs et salons.
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
@Service
@Transactional
class VerificationService(
    private val userRepository: UserRepository,
    private val salonRepository: SalonRepository
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class UserNotFoundException(userId: String) :
        RuntimeException("Utilisateur avec ID '$userId' non trouvé")

    class SalonNotFoundException(salonId: String) :
        RuntimeException("Salon avec ID '$salonId' non trouvé")

    class UnauthorizedVerificationException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé à effectuer cette opération de vérification")

    class AlreadyVerifiedException(entityId: String, entityType: String) :
        RuntimeException("L'entité '$entityType' avec ID '$entityId' est déjà vérifiée")

    class VerificationRequestNotFoundException :
        RuntimeException("Aucune demande de vérification trouvée")

    // ========== GESTION DES DEMANDES DE VÉRIFICATION (H.2) ==========

    /**
     * Permet à un utilisateur ou un salon de demander une vérification.
     * 
     * @param requestType Le type d'entité demandant la vérification ("user" ou "salon")
     * @param entityId L'ID de l'entité (utilisateur ou salon)
     * @param request Les détails de la demande de vérification
     * @param currentUserId L'ID de l'utilisateur authentifié
     * @return Message de confirmation
     */
    fun requestVerification(
        requestType: String,
        entityId: String,
        request: RequestVerificationRequest,
        currentUserId: String
    ): String {
        request.validate()

        when (requestType.lowercase()) {
            "user" -> {
                val user = userRepository.findById(entityId)
                    .orElseThrow { UserNotFoundException(entityId) }

                // Vérifier que l'utilisateur authentifié est bien le propriétaire du compte
                if (user.id != currentUserId) {
                    throw UnauthorizedVerificationException(currentUserId)
                }

                // Vérifier que l'utilisateur n'est pas déjà vérifié
                if (user.isVerified && user.verificationType != null) {
                    throw AlreadyVerifiedException(entityId, "utilisateur")
                }

                // Pour l'instant, on enregistre juste la demande (dans un futur système, on pourrait créer une table de demandes)
                // Ici, on simule l'enregistrement d'une demande
                println("📋 Demande de vérification reçue pour utilisateur ${user.email} (${user.id}) - Type: ${request.verificationType}")
                return "Votre demande de vérification a été enregistrée. Elle sera traitée par notre équipe sous peu."
            }

            "salon" -> {
                val salon = salonRepository.findById(entityId)
                    .orElseThrow { SalonNotFoundException(entityId) }

                // Vérifier que l'utilisateur authentifié est bien le propriétaire du salon
                if (salon.owner?.id != currentUserId) {
                    throw UnauthorizedVerificationException(currentUserId)
                }

                // Vérifier que le salon n'est pas déjà vérifié
                if (salon.isVerified && salon.verificationType != null) {
                    throw AlreadyVerifiedException(entityId, "salon")
                }

                // Pour l'instant, on enregistre juste la demande
                println("📋 Demande de vérification reçue pour salon ${salon.name} (${salon.id}) - Type: ${request.verificationType}")
                return "Votre demande de vérification a été enregistrée. Elle sera traitée par notre équipe sous peu."
            }

            else -> throw IllegalArgumentException("Type d'entité invalide: $requestType. Utilisez 'user' ou 'salon'.")
        }
    }

    /**
     * Vérifie un utilisateur (admin uniquement).
     * 
     * @param userId L'ID de l'utilisateur à vérifier
     * @param request Les détails de la vérification
     * @param currentUserId L'ID de l'admin effectuant la vérification
     * @return L'utilisateur vérifié
     */
    fun verifyUser(userId: String, request: VerifyUserRequest, currentUserId: String): UserResponse {
        request.validate()

        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        val currentUser = userRepository.findById(currentUserId)
            .orElseThrow { UserNotFoundException(currentUserId) }

        // Vérifier que l'utilisateur courant est admin
        if (currentUser.userType != UserType.admin) {
            throw UnauthorizedVerificationException(currentUserId)
        }

        // Mettre à jour l'utilisateur
        user.isVerified = true
        user.verificationType = request.verificationType

        val savedUser = userRepository.save(user)
        println("✅ Utilisateur ${savedUser.email} (${savedUser.id}) vérifié avec le type ${request.verificationType} par admin ${currentUser.email}")
        return UserResponse.fromEntity(savedUser)
    }

    /**
     * Vérifie un salon (admin uniquement).
     * 
     * @param salonId L'ID du salon à vérifier
     * @param request Les détails de la vérification
     * @param currentUserId L'ID de l'admin effectuant la vérification
     * @return Le salon vérifié
     */
    fun verifySalon(salonId: String, request: VerifySalonRequest, currentUserId: String): SalonResponse {
        request.validate()

        val salon = salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }

        val currentUser = userRepository.findById(currentUserId)
            .orElseThrow { UserNotFoundException(currentUserId) }

        // Vérifier que l'utilisateur courant est admin
        if (currentUser.userType != UserType.admin) {
            throw UnauthorizedVerificationException(currentUserId)
        }

        // Mettre à jour le salon
        salon.isVerified = true
        salon.verificationType = request.verificationType

        val savedSalon = salonRepository.save(salon)
        println("✅ Salon ${savedSalon.name} (${savedSalon.id}) vérifié avec le type ${request.verificationType} par admin ${currentUser.email}")
        return toSalonResponse(savedSalon)
    }

    /**
     * Révoque la vérification d'un utilisateur ou d'un salon (admin uniquement).
     * 
     * @param entityType Le type d'entité ("user" ou "salon")
     * @param entityId L'ID de l'entité
     * @param currentUserId L'ID de l'admin effectuant la révocation
     * @return Message de confirmation
     */
    fun revokeVerification(entityType: String, entityId: String, currentUserId: String): String {
        val currentUser = userRepository.findById(currentUserId)
            .orElseThrow { UserNotFoundException(currentUserId) }

        // Vérifier que l'utilisateur courant est admin
        if (currentUser.userType != UserType.admin) {
            throw UnauthorizedVerificationException(currentUserId)
        }

        when (entityType.lowercase()) {
            "user" -> {
                val user = userRepository.findById(entityId)
                    .orElseThrow { UserNotFoundException(entityId) }

                user.isVerified = false
                user.verificationType = null
                userRepository.save(user)
                println("❌ Vérification révoquée pour utilisateur ${user.email} (${user.id}) par admin ${currentUser.email}")
                return "La vérification de l'utilisateur a été révoquée."
            }

            "salon" -> {
                val salon = salonRepository.findById(entityId)
                    .orElseThrow { SalonNotFoundException(entityId) }

                salon.isVerified = false
                salon.verificationType = null
                salonRepository.save(salon)
                println("❌ Vérification révoquée pour salon ${salon.name} (${salon.id}) par admin ${currentUser.email}")
                return "La vérification du salon a été révoquée."
            }

            else -> throw IllegalArgumentException("Type d'entité invalide: $entityType. Utilisez 'user' ou 'salon'.")
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Convertit une entité Salon en SalonResponse.
     * Phase H.2 - Ajout des champs isVerified et verificationType
     */
    private fun toSalonResponse(salon: Salon): SalonResponse {
        return SalonResponse(
            id = salon.id!!,
            name = salon.name,
            address = salon.address,
            city = salon.city,
            postalCode = salon.postalCode,
            description = salon.description,
            slug = salon.slug,
            ownerId = salon.owner?.id ?: "",
            coverPhotoUrl = salon.coverPhotoUrl,
            latitude = salon.latitude,
            longitude = salon.longitude,
            isVerified = salon.isVerified, // Phase H.2
            verificationType = salon.verificationType, // Phase H.2
            isFollowedByCurrentUser = null,
            followersCount = null,
            createdAt = salon.createdAt
        )
    }
}

