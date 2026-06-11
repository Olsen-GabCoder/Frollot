package com.frollot.repository

import com.frollot.model.UserBadge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des badges attribués aux utilisateurs.
 * Phase E.3 - Badges et Certifications
 */
@Repository
interface UserBadgeRepository : JpaRepository<UserBadge, String> {

    /**
     * Récupère tous les badges d'un utilisateur, triés par date d'obtention décroissante.
     */
    fun findByUserIdOrderByEarnedAtDesc(userId: String): List<UserBadge>

    /**
     * Récupère les badges affichés d'un utilisateur, triés par date d'obtention décroissante.
     */
    fun findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(userId: String): List<UserBadge>

    /**
     * Vérifie si un utilisateur a déjà un badge donné.
     */
    fun existsByUserIdAndBadgeId(userId: String, badgeId: String): Boolean

    /**
     * Récupère tous les utilisateurs ayant un badge donné.
     */
    fun findByBadgeId(badgeId: String): List<UserBadge>

    /**
     * Récupère l'association user-badge spécifique.
     */
    fun findByUserIdAndBadgeId(userId: String, badgeId: String): UserBadge?

    /**
     * Compte le nombre de badges d'un utilisateur.
     */
    fun countByUserId(userId: String): Long

    /**
     * Compte le nombre de badges affichés d'un utilisateur.
     */
    fun countByUserIdAndIsDisplayedTrue(userId: String): Long
}

