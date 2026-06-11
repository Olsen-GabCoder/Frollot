package com.frollot.repository

import com.frollot.model.Collection
import com.frollot.model.CollectionCategory
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des collections thématiques.
 * Phase F.1 - Collections Thématiques
 */
@Repository
interface CollectionRepository : JpaRepository<Collection, String> {

    /**
     * Récupère toutes les collections d'un utilisateur.
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Collection>

    /**
     * Récupère toutes les collections publiques d'un utilisateur.
     */
    fun findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId: String): List<Collection>

    /**
     * Récupère toutes les collections d'un utilisateur avec pagination.
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<Collection>

    /**
     * Récupère toutes les collections publiques d'un utilisateur avec pagination.
     */
    fun findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<Collection>

    /**
     * Récupère toutes les collections publiques d'un utilisateur par catégorie.
     */
    fun findByUserIdAndIsPublicTrueAndCategoryOrderByCreatedAtDesc(
        userId: String,
        category: CollectionCategory
    ): List<Collection>

    /**
     * Récupère toutes les collections publiques (pour découverte).
     */
    fun findByIsPublicTrueOrderByCreatedAtDesc(pageable: Pageable): Page<Collection>

    /**
     * Récupère toutes les collections publiques par catégorie.
     */
    fun findByIsPublicTrueAndCategoryOrderByCreatedAtDesc(
        category: CollectionCategory,
        pageable: Pageable
    ): Page<Collection>

    /**
     * Vérifie si une collection appartient à un utilisateur.
     */
    fun existsByIdAndUserId(collectionId: String, userId: String): Boolean

    /**
     * Compte le nombre de collections d'un utilisateur.
     */
    fun countByUserId(userId: String): Long

    /**
     * Compte le nombre de collections publiques d'un utilisateur.
     */
    fun countByUserIdAndIsPublicTrue(userId: String): Long
}

