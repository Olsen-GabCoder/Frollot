package com.frollot.service

import com.frollot.dto.FollowResponse
import com.frollot.dto.UserResponse
import com.frollot.model.Follow
import com.frollot.model.FollowingType
import com.frollot.model.Salon
import com.frollot.model.User
import com.frollot.model.UserType
import com.frollot.repository.FollowRepository
import com.frollot.repository.SalonRepository
import com.frollot.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service pour la gestion des relations de suivi.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 */
@Service
@Transactional
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val salonRepository: SalonRepository
) {

    /**
     * Suit un salon.
     * 
     * @param followerId ID de l'utilisateur qui suit
     * @param salonId ID du salon à suivre
     * @return FollowResponse
     * @throws RuntimeException si le salon n'existe pas ou si déjà suivi
     */
    @Transactional
    fun followSalon(followerId: String, salonId: String): FollowResponse {
        // Vérifier que le salon existe
        if (!salonRepository.existsById(salonId)) {
            throw RuntimeException("Salon avec ID '$salonId' non trouvé")
        }

        // Vérifier que l'utilisateur existe
        val follower = userRepository.findById(followerId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$followerId' non trouvé") }

        // Vérifier si déjà suivi
        if (followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                followerId,
                FollowingType.SALON,
                salonId
            )
        ) {
            throw RuntimeException("Vous suivez déjà ce salon")
        }

        // Créer le follow
        val follow = Follow(
            id = UUID.randomUUID().toString(),
            follower = follower,
            followingType = FollowingType.SALON,
            followingId = salonId
        )

        if (!follow.isValid()) {
            throw IllegalStateException("Impossible de créer un follow invalide")
        }

        val savedFollow = followRepository.save(follow)
        println("✅ Follow salon créé: User $followerId suit Salon $salonId")

        return FollowResponse.fromEntity(savedFollow)
    }

    /**
     * Suit un coiffeur (User avec userType = hairstylist).
     * 
     * @param followerId ID de l'utilisateur qui suit
     * @param coiffeurId ID du coiffeur à suivre
     * @return FollowResponse
     * @throws RuntimeException si le coiffeur n'existe pas, n'est pas un coiffeur, ou si déjà suivi
     */
    @Transactional
    fun followCoiffeur(followerId: String, coiffeurId: String): FollowResponse {
        // Vérifier que le coiffeur existe et est bien un coiffeur
        val coiffeur = userRepository.findById(coiffeurId)
            .orElseThrow { RuntimeException("Coiffeur avec ID '$coiffeurId' non trouvé") }

        if (coiffeur.userType != UserType.hairstylist) {
            throw RuntimeException("L'utilisateur avec ID '$coiffeurId' n'est pas un coiffeur")
        }

        // Vérifier que l'utilisateur qui suit existe
        val follower = userRepository.findById(followerId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$followerId' non trouvé") }

        // Vérifier si déjà suivi
        if (followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                followerId,
                FollowingType.COIFFEUR,
                coiffeurId
            )
        ) {
            throw RuntimeException("Vous suivez déjà ce coiffeur")
        }

        // Créer le follow
        val follow = Follow(
            id = UUID.randomUUID().toString(),
            follower = follower,
            followingType = FollowingType.COIFFEUR,
            followingId = coiffeurId
        )

        if (!follow.isValid()) {
            throw IllegalStateException("Impossible de créer un follow invalide")
        }

        val savedFollow = followRepository.save(follow)
        println("✅ Follow coiffeur créé: User $followerId suit Coiffeur $coiffeurId")

        return FollowResponse.fromEntity(savedFollow)
    }

    /**
     * Suit un utilisateur (client, owner ou tout type).
     */
    @Transactional
    fun followUser(followerId: String, targetUserId: String): FollowResponse {
        if (followerId == targetUserId) {
            throw RuntimeException("Vous ne pouvez pas vous suivre vous-même")
        }

        val target = userRepository.findById(targetUserId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$targetUserId' non trouvé") }

        val follower = userRepository.findById(followerId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$followerId' non trouvé") }

        if (followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                followerId, FollowingType.USER, targetUserId
            )
        ) {
            throw RuntimeException("Vous suivez déjà cet utilisateur")
        }

        val follow = Follow(
            id = UUID.randomUUID().toString(),
            follower = follower,
            followingType = FollowingType.USER,
            followingId = targetUserId
        )

        val savedFollow = followRepository.save(follow)
        return FollowResponse.fromEntity(savedFollow)
    }

    /**
     * Ne plus suivre une entité.
     *
     * @param followerId ID de l'utilisateur qui ne suit plus
     * @param followingType Type de l'entité (SALON, COIFFEUR, USER)
     * @param followingId ID de l'entité à ne plus suivre
     * @throws RuntimeException si le follow n'existe pas
     */
    @Transactional
    fun unfollow(followerId: String, followingType: FollowingType, followingId: String) {
        val follow = followRepository.findByFollowerIdAndFollowingTypeAndFollowingId(
            followerId,
            followingType,
            followingId
        )

        if (follow == null) {
            throw RuntimeException("Vous ne suivez pas cette entité")
        }

        followRepository.delete(follow)
        println("❌ Follow supprimé: User $followerId ne suit plus ${followingType.name} $followingId")
    }

    /**
     * Vérifie si un utilisateur suit une entité donnée.
     * 
     * @param followerId ID de l'utilisateur
     * @param followingType Type de l'entité
     * @param followingId ID de l'entité
     * @return true si l'utilisateur suit l'entité, false sinon
     */
    @Transactional(readOnly = true)
    fun isFollowing(followerId: String, followingType: FollowingType, followingId: String): Boolean {
        return followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
            followerId,
            followingType,
            followingId
        )
    }

    /**
     * Récupère toutes les entités suivies par un utilisateur.
     * 
     * @param followerId ID de l'utilisateur
     * @return Liste des FollowResponse
     */
    @Transactional(readOnly = true)
    fun getFollowing(followerId: String): List<FollowResponse> {
        val follows = followRepository.findByFollowerId(followerId)
        return follows.map { FollowResponse.fromEntity(it) }
    }

    /**
     * Récupère tous les followers d'une entité donnée.
     * 
     * @param followingType Type de l'entité
     * @param followingId ID de l'entité
     * @return Liste des UserResponse (utilisateurs qui suivent)
     */
    @Transactional(readOnly = true)
    fun getFollowers(followingType: FollowingType, followingId: String): List<UserResponse> {
        val follows = followRepository.findByFollowingTypeAndFollowingId(followingType, followingId)
        return follows.mapNotNull { follow ->
            follow.follower?.let { user ->
                UserResponse.fromEntity(user)
            }
        }
    }

    /**
     * Compte le nombre d'entités suivies par un utilisateur.
     * 
     * @param followerId ID de l'utilisateur
     * @return Nombre d'entités suivies
     */
    @Transactional(readOnly = true)
    fun getFollowingCount(followerId: String): Long {
        return followRepository.countByFollowerId(followerId)
    }

    /**
     * Compte le nombre de followers d'une entité donnée.
     * 
     * @param followingType Type de l'entité
     * @param followingId ID de l'entité
     * @return Nombre de followers
     */
    @Transactional(readOnly = true)
    fun getFollowersCount(followingType: FollowingType, followingId: String): Long {
        return followRepository.countByFollowingTypeAndFollowingId(followingType, followingId)
    }
}

