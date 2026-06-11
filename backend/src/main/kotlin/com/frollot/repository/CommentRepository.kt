package com.frollot.repository

import com.frollot.model.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des commentaires.
 */
@Repository
interface CommentRepository : JpaRepository<Comment, String> {

    /**
     * Récupère tous les commentaires d'un post, triés par date croissante.
     * 
     * Utilise EntityGraph pour charger l'auteur en une seule requête.
     */
    @EntityGraph(attributePaths = ["author"])
    fun findByPostIdOrderByCreatedAtAsc(postId: String, pageable: Pageable): Page<Comment>

    /**
     * Récupère tous les commentaires d'un post (sans pagination).
     */
    @EntityGraph(attributePaths = ["author"])
    fun findByPostIdOrderByCreatedAtAsc(postId: String): List<Comment>

    /**
     * Compte le nombre de commentaires d'un post.
     */
    fun countByPostId(postId: String): Long

    /**
     * Récupère tous les commentaires d'un utilisateur.
     */
    @EntityGraph(attributePaths = ["author", "post"])
    fun findByAuthorIdOrderByCreatedAtDesc(authorId: String): List<Comment>

    /**
     * Supprime tous les commentaires d'un post.
     */
    fun deleteByPostId(postId: String)
}

