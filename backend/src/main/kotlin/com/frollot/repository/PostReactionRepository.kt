package com.frollot.repository

import com.frollot.model.PostReaction
import com.frollot.model.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des réactions spécialisées sur les posts.
 * Phase D.4 - Réactions Spécialisées Coiffure
 */
@Repository
interface PostReactionRepository : JpaRepository<PostReaction, String> {

    /**
     * Vérifie si un utilisateur a déjà réagi à un post.
     */
    fun existsByPostIdAndUserId(postId: String, userId: String): Boolean

    /**
     * Récupère la réaction d'un utilisateur sur un post spécifique.
     */
    fun findByPostIdAndUserId(postId: String, userId: String): PostReaction?

    /**
     * Compte le nombre de réactions d'un type spécifique sur un post.
     */
    fun countByPostIdAndReactionType(postId: String, reactionType: ReactionType): Long

    /**
     * Récupère toutes les réactions d'un post.
     */
    fun findByPostIdOrderByCreatedAtDesc(postId: String): List<PostReaction>

    /**
     * Récupère toutes les réactions d'un post (sans tri).
     */
    fun findByPostId(postId: String): List<PostReaction>

    /**
     * Récupère la réaction d'un utilisateur sur un post (pour vérification).
     */
    @Query("SELECT pr FROM PostReaction pr WHERE pr.post.id = :postId AND pr.user.id = :userId")
    fun findReactionByPostAndUser(
        @Param("postId") postId: String,
        @Param("userId") userId: String
    ): PostReaction?

    /**
     * Supprime toutes les réactions d'un post.
     */
    fun deleteByPostId(postId: String)
}

