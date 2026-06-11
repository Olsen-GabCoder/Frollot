package com.frollot.repository

import com.frollot.model.PostArchive
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des archives sur les posts.
 */
@Repository
interface PostArchiveRepository : JpaRepository<PostArchive, String> {

    /**
     * Vérifie si un utilisateur a déjà archivé un post.
     */
    fun existsByPostIdAndUserId(postId: String, userId: String): Boolean

    /**
     * Récupère l'archive d'un utilisateur sur un post spécifique.
     */
    fun findByPostIdAndUserId(postId: String, userId: String): PostArchive?

    /**
     * Récupère tous les posts archivés par un utilisateur avec pagination.
     */
    fun findByUserIdOrderByArchivedAtDesc(userId: String, pageable: Pageable): Page<PostArchive>

    /**
     * Récupère tous les posts archivés par un utilisateur.
     */
    @Query("SELECT pa.post FROM PostArchive pa WHERE pa.user.id = :userId ORDER BY pa.archivedAt DESC")
    fun findPostsArchivedByUser(@Param("userId") userId: String): List<com.frollot.model.Post>

    /**
     * Récupère tous les IDs de posts archivés par un utilisateur.
     * Utilisé pour filtrer le feed.
     */
    @Query("SELECT pa.post.id FROM PostArchive pa WHERE pa.user.id = :userId")
    fun findArchivedPostIdsByUserId(@Param("userId") userId: String): List<String>

    /**
     * Supprime tous les archives d'un post.
     */
    fun deleteByPostId(postId: String)
}

