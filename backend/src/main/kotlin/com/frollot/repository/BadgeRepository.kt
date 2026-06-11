package com.frollot.repository

import com.frollot.model.Badge
import com.frollot.model.BadgeCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des badges.
 * Phase E.3 - Badges et Certifications
 */
@Repository
interface BadgeRepository : JpaRepository<Badge, String> {

    /**
     * Récupère tous les badges d'une catégorie donnée.
     */
    fun findByCategoryOrderByNameAsc(category: BadgeCategory): List<Badge>

    /**
     * Récupère tous les badges, triés par nom.
     */
    fun findAllByOrderByNameAsc(): List<Badge>
}

