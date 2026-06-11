package com.frollot.repository

import com.frollot.model.PostShare
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des partages de posts.
 * Phase D.3 - Partage de Posts (Reposts)
 */
@Repository
interface PostShareRepository : JpaRepository<PostShare, String> {

    /**
     * Vérifie si un utilisateur a déjà partagé un post.
     */
    fun existsByPostIdAndUserId(postId: String, userId: String): Boolean

    /**
     * Récupère le partage d'un utilisateur sur un post spécifique.
     */
    fun findByPostIdAndUserId(postId: String, userId: String): PostShare?

    /**
     * Compte le nombre de partages d'un post.
     */
    fun countByPostId(postId: String): Long

    /**
     * Récupère tous les partages d'un post avec pagination.
     */
    fun findByPostIdOrderByCreatedAtDesc(postId: String, pageable: Pageable): Page<PostShare>

    /**
     * Récupère tous les partages d'un post (sans pagination, pour compatibilité).
     */
    fun findByPostIdOrderByCreatedAtDesc(postId: String): List<PostShare>

    /**
     * Récupère tous les posts partagés par un utilisateur.
     */
    @Query("SELECT ps.post FROM PostShare ps WHERE ps.user.id = :userId ORDER BY ps.createdAt DESC")
    fun findPostsSharedByUser(@Param("userId") userId: String): List<com.frollot.model.Post>

    /**
     * Supprime tous les partages d'un post.
     */
    fun deleteByPostId(postId: String)
}

