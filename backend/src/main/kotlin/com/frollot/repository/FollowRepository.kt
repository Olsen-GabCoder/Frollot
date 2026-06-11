package com.frollot.repository

import com.frollot.model.Follow
import com.frollot.model.FollowingType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des relations de suivi.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 */
@Repository
interface FollowRepository : JpaRepository<Follow, String> {
    
    /**
     * Récupère toutes les entités suivies par un utilisateur.
     */
    fun findByFollowerId(followerId: String): List<Follow>
    
    /**
     * Récupère tous les followers d'une entité donnée.
     */
    fun findByFollowingTypeAndFollowingId(followingType: FollowingType, followingId: String): List<Follow>
    
    /**
     * Vérifie si un utilisateur suit déjà une entité donnée.
     */
    fun existsByFollowerIdAndFollowingTypeAndFollowingId(
        followerId: String,
        followingType: FollowingType,
        followingId: String
    ): Boolean
    
    /**
     * Récupère une relation de suivi spécifique.
     */
    fun findByFollowerIdAndFollowingTypeAndFollowingId(
        followerId: String,
        followingType: FollowingType,
        followingId: String
    ): Follow?
    
    /**
     * Supprime une relation de suivi.
     */
    fun deleteByFollowerIdAndFollowingTypeAndFollowingId(
        followerId: String,
        followingType: FollowingType,
        followingId: String
    )
    
    /**
     * Compte le nombre de followers d'une entité donnée.
     */
    fun countByFollowingTypeAndFollowingId(followingType: FollowingType, followingId: String): Long
    
    /**
     * Compte le nombre d'entités suivies par un utilisateur.
     */
    fun countByFollowerId(followerId: String): Long
}

