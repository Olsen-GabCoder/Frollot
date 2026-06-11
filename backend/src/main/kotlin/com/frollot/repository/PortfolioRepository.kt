package com.frollot.repository

import com.frollot.model.Portfolio
import com.frollot.model.PortfolioOwnerType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des portfolios.
 */
@Repository
interface PortfolioRepository : JpaRepository<Portfolio, String> {

    /**
     * Récupère tous les portfolios d'un propriétaire.
     */
    fun findByOwnerIdAndOwnerTypeOrderByCreatedAtDesc(
        ownerId: String,
        ownerType: PortfolioOwnerType
    ): List<Portfolio>

    /**
     * Récupère tous les portfolios publics d'un propriétaire.
     */
    fun findByOwnerIdAndOwnerTypeAndIsPublicTrueOrderByCreatedAtDesc(
        ownerId: String,
        ownerType: PortfolioOwnerType
    ): List<Portfolio>

    /**
     * Vérifie si un portfolio existe pour un propriétaire.
     */
    fun existsByIdAndOwnerIdAndOwnerType(
        portfolioId: String,
        ownerId: String,
        ownerType: PortfolioOwnerType
    ): Boolean

    /**
     * Récupère un portfolio par son ID et son propriétaire.
     */
    fun findByIdAndOwnerIdAndOwnerType(
        portfolioId: String,
        ownerId: String,
        ownerType: PortfolioOwnerType
    ): Portfolio?

    /**
     * Récupère tous les portfolios publics.
     */
    @Query("SELECT p FROM Portfolio p WHERE p.isPublic = true ORDER BY p.createdAt DESC")
    fun findPublicPortfolios(): List<Portfolio>
}

