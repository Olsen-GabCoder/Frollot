package com.frollot.repository

import com.frollot.model.PostService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des associations entre posts et services.
 */
@Repository
interface PostServiceRepository : JpaRepository<PostService, String> {

    /**
     * Vérifie si un service est déjà associé à un post.
     */
    fun existsByPostIdAndServiceId(postId: String, serviceId: String): Boolean

    /**
     * Récupère l'association entre un post et un service.
     */
    fun findByPostIdAndServiceId(postId: String, serviceId: String): PostService?

    /**
     * Récupère tous les services associés à un post.
     */
    @Query("SELECT ps.service FROM PostService ps WHERE ps.post.id = :postId")
    fun findServicesByPostId(@Param("postId") postId: String): List<com.frollot.model.SalonService>

    /**
     * Récupère tous les posts associés à un service.
     */
    @Query("SELECT ps.post FROM PostService ps WHERE ps.service.id = :serviceId ORDER BY ps.createdAt DESC")
    fun findPostsByServiceId(@Param("serviceId") serviceId: String): List<com.frollot.model.Post>

    /**
     * Supprime toutes les associations d'un post.
     */
    fun deleteByPostId(postId: String)

    /**
     * Supprime toutes les associations d'un service.
     */
    fun deleteByServiceId(serviceId: String)
}

