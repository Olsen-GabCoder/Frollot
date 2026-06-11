package com.frollot.repository

import com.frollot.model.CollectionPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des associations collection-post.
 * Phase F.1 - Collections Thématiques
 */
@Repository
interface CollectionPostRepository : JpaRepository<CollectionPost, String> {

    /**
     * Récupère tous les posts d'une collection, ordonnés par orderIndex puis par date d'ajout.
     */
    fun findByCollectionIdOrderByOrderIndexAscAddedAtDesc(collectionId: String): List<CollectionPost>

    /**
     * Récupère tous les posts d'une collection avec pagination.
     */
    fun findByCollectionIdOrderByOrderIndexAscAddedAtDesc(
        collectionId: String,
        pageable: Pageable
    ): Page<CollectionPost>

    /**
     * Vérifie si un post est déjà dans une collection.
     */
    fun existsByCollectionIdAndPostId(collectionId: String, postId: String): Boolean

    /**
     * Récupère l'association collection-post spécifique.
     */
    fun findByCollectionIdAndPostId(collectionId: String, postId: String): CollectionPost?

    /**
     * Compte le nombre de posts dans une collection.
     */
    fun countByCollectionId(collectionId: String): Long

    /**
     * Supprime un post d'une collection.
     */
    fun deleteByCollectionIdAndPostId(collectionId: String, postId: String)

    /**
     * Supprime tous les posts d'une collection.
     */
    fun deleteByCollectionId(collectionId: String)

    /**
     * Récupère toutes les collections contenant un post spécifique.
     */
    @Query("SELECT cp.collection FROM CollectionPost cp WHERE cp.post.id = :postId")
    fun findCollectionsByPostId(@Param("postId") postId: String): List<com.frollot.model.Collection>

    /**
     * Récupère le nombre de collections contenant un post spécifique.
     */
    fun countByPostId(postId: String): Long
}

