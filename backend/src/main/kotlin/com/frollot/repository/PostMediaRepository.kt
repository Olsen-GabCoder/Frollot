package com.frollot.repository

import com.frollot.model.PostMedia
import com.frollot.model.PostMediaType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des médias associés aux posts.
 */
@Repository
interface PostMediaRepository : JpaRepository<PostMedia, String> {

    /**
     * Récupère tous les médias d'un post, triés par type puis par ordre.
     */
    fun findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId: String): List<PostMedia>

    /**
     * Récupère tous les médias d'un post d'un type spécifique, triés par ordre.
     */
    fun findByPostIdAndMediaTypeOrderByOrderIndexAsc(postId: String, mediaType: PostMediaType): List<PostMedia>

    /**
     * Supprime tous les médias d'un post.
     */
    fun deleteByPostId(postId: String)

    /**
     * Compte le nombre de médias d'un post.
     */
    fun countByPostId(postId: String): Long
}

