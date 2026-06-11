package com.frollot.repository

import com.frollot.model.SalonHighlightedPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des posts mis en avant par les salons.
 * Phase E.2 - Profil Salon Social
 */
@Repository
interface SalonHighlightedPostRepository : JpaRepository<SalonHighlightedPost, String> {

    /**
     * Récupère tous les posts mis en avant d'un salon, triés par ordre.
     */
    fun findBySalonIdOrderByOrderIndexAsc(salonId: String): List<SalonHighlightedPost>

    /**
     * Vérifie si un post est déjà mis en avant par un salon.
     */
    fun existsBySalonIdAndPostId(salonId: String, postId: String): Boolean

    /**
     * Supprime tous les posts mis en avant d'un salon.
     */
    fun deleteBySalonId(salonId: String)

    /**
     * Supprime un post mis en avant spécifique.
     */
    fun deleteBySalonIdAndPostId(salonId: String, postId: String)
}

