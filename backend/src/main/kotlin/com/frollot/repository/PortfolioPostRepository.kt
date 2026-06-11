package com.frollot.repository

import com.frollot.model.PortfolioPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Repository pour la gestion des associations entre portfolios et posts.
 */
@Repository
interface PortfolioPostRepository : JpaRepository<PortfolioPost, String> {

    /**
     * Vérifie si un post est déjà dans un portfolio.
     */
    fun existsByPortfolioIdAndPostId(portfolioId: String, postId: String): Boolean

    /**
     * Récupère tous les posts d'un portfolio, triés par ordre.
     */
    @Query("SELECT pp.post FROM PortfolioPost pp WHERE pp.portfolio.id = :portfolioId ORDER BY pp.orderIndex ASC, pp.addedAt ASC")
    fun findPostsByPortfolioId(@Param("portfolioId") portfolioId: String): List<com.frollot.model.Post>

    /**
     * Récupère toutes les associations d'un portfolio, triées par ordre.
     */
    fun findByPortfolioIdOrderByOrderIndexAscAddedAtAsc(portfolioId: String): List<PortfolioPost>

    /**
     * Récupère toutes les associations d'un post.
     */
    fun findByPostId(postId: String): List<PortfolioPost>

    /**
     * Supprime toutes les associations d'un portfolio.
     */
    fun deleteByPortfolioId(portfolioId: String)

    /**
     * Supprime une association spécifique.
     */
    fun deleteByPortfolioIdAndPostId(portfolioId: String, postId: String)

    /**
     * Récupère le nombre de posts dans un portfolio.
     */
    fun countByPortfolioId(portfolioId: String): Long

    /**
     * Met à jour l'ordre d'un post dans un portfolio.
     */
    @Modifying
    @Transactional
    @Query("UPDATE PortfolioPost pp SET pp.orderIndex = :orderIndex WHERE pp.id = :portfolioPostId")
    fun updateOrderIndex(@Param("portfolioPostId") portfolioPostId: String, @Param("orderIndex") orderIndex: Int)
}

