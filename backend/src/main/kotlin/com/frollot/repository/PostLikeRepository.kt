package com.frollot.repository

import com.frollot.model.PostLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des likes sur les posts.
 */
@Repository
interface PostLikeRepository : JpaRepository<PostLike, String> {

    /**
     * Vérifie si un utilisateur a déjà liké un post.
     */
    fun existsByPostIdAndUserId(postId: String, userId: String): Boolean

    /**
     * Récupère le like d'un utilisateur sur un post spécifique.
     */
    fun findByPostIdAndUserId(postId: String, userId: String): PostLike?

    /**
     * Compte le nombre de likes d'un post.
     */
    fun countByPostId(postId: String): Long

    /**
     * Récupère tous les likes d'un post.
     */
    fun findByPostIdOrderByCreatedAtDesc(postId: String): List<PostLike>

    /**
     * Récupère tous les posts likés par un utilisateur.
     */
    @Query("SELECT pl.post FROM PostLike pl WHERE pl.user.id = :userId ORDER BY pl.createdAt DESC")
    fun findPostsLikedByUser(@Param("userId") userId: String): List<com.frollot.model.Post>

    /**
     * Supprime tous les likes d'un post.
     */
    fun deleteByPostId(postId: String)
}

