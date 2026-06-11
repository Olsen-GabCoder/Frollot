package com.frollot.repository

import com.frollot.model.PostFavorite
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des favoris sur les posts.
 */
@Repository
interface PostFavoriteRepository : JpaRepository<PostFavorite, String> {

    /**
     * Vérifie si un utilisateur a déjà mis un post en favori.
     */
    fun existsByPostIdAndUserId(postId: String, userId: String): Boolean

    /**
     * Récupère le favori d'un utilisateur sur un post spécifique.
     */
    fun findByPostIdAndUserId(postId: String, userId: String): PostFavorite?

    /**
     * Compte le nombre de favoris d'un post.
     */
    fun countByPostId(postId: String): Long

    /**
     * Récupère tous les favoris d'un post.
     */
    fun findByPostIdOrderByCreatedAtDesc(postId: String): List<PostFavorite>

    /**
     * Récupère tous les favoris d'un utilisateur avec pagination.
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<PostFavorite>

    /**
     * Récupère tous les posts favoris par un utilisateur.
     */
    @Query("SELECT pf.post FROM PostFavorite pf WHERE pf.user.id = :userId ORDER BY pf.createdAt DESC")
    fun findPostsFavoritedByUser(@Param("userId") userId: String): List<com.frollot.model.Post>

    /**
     * Supprime tous les favoris d'un post.
     */
    fun deleteByPostId(postId: String)
}

