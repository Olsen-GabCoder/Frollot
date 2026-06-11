package com.frollot.repository

import com.frollot.model.PostHashtag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des associations entre posts et hashtags.
 */
@Repository
interface PostHashtagRepository : JpaRepository<PostHashtag, String> {

    /**
     * Vérifie si un hashtag est déjà associé à un post.
     */
    fun existsByPostIdAndHashtagId(postId: String, hashtagId: String): Boolean

    /**
     * Récupère tous les hashtags associés à un post.
     */
    @Query("SELECT ph.hashtag FROM PostHashtag ph WHERE ph.post.id = :postId ORDER BY ph.createdAt ASC")
    fun findHashtagsByPostId(@Param("postId") postId: String): List<com.frollot.model.HairHashtag>

    /**
     * Récupère tous les posts associés à un hashtag.
     */
    @Query("SELECT ph.post FROM PostHashtag ph WHERE ph.hashtag.id = :hashtagId ORDER BY ph.createdAt DESC")
    fun findPostsByHashtagId(@Param("hashtagId") hashtagId: String): List<com.frollot.model.Post>

    /**
     * Récupère tous les posts associés à un hashtag par nom.
     */
    @Query("SELECT ph.post FROM PostHashtag ph WHERE LOWER(ph.hashtag.name) = LOWER(:hashtagName) ORDER BY ph.createdAt DESC")
    fun findPostsByHashtagName(@Param("hashtagName") hashtagName: String): List<com.frollot.model.Post>

    /**
     * Supprime toutes les associations d'un post.
     */
    fun deleteByPostId(postId: String)

    /**
     * Supprime toutes les associations d'un hashtag.
     */
    fun deleteByHashtagId(hashtagId: String)
}

