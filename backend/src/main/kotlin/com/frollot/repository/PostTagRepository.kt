package com.frollot.repository

import com.frollot.model.PostTag
import com.frollot.model.TaggedType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des tags/mentions sur les posts.
 */
@Repository
interface PostTagRepository : JpaRepository<PostTag, String> {

    /**
     * Récupère tous les tags d'un post.
     */
    fun findByPostIdOrderByCreatedAtAsc(postId: String): List<PostTag>

    /**
     * Vérifie si un tag existe déjà pour un post donné.
     */
    fun existsByPostIdAndTaggedTypeAndTaggedId(
        postId: String,
        taggedType: TaggedType,
        taggedId: String
    ): Boolean

    /**
     * Récupère un tag spécifique.
     */
    fun findByPostIdAndTaggedTypeAndTaggedId(
        postId: String,
        taggedType: TaggedType,
        taggedId: String
    ): PostTag?

    /**
     * Récupère tous les tags d'un type spécifique pour une entité taguée.
     */
    fun findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
        taggedType: TaggedType,
        taggedId: String
    ): List<PostTag>

    /**
     * Supprime tous les tags d'un post.
     */
    fun deleteByPostId(postId: String)
}

