package com.frollot.repository

import com.frollot.model.Report
import com.frollot.model.ReportStatus
import com.frollot.model.ReportedEntityType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des signalements de contenu.
 * Phase H.1 - Signalement de Contenu
 */
@Repository
interface ReportRepository : JpaRepository<Report, String> {

    /**
     * Vérifie si un utilisateur a déjà signalé une entité spécifique.
     */
    fun existsByReporterIdAndReportedEntityTypeAndReportedEntityId(
        reporterId: String,
        reportedEntityType: ReportedEntityType,
        reportedEntityId: String
    ): Boolean

    /**
     * Récupère tous les signalements d'un utilisateur.
     */
    fun findByReporterIdOrderByCreatedAtDesc(
        reporterId: String,
        pageable: Pageable
    ): Page<Report>

    /**
     * Récupère tous les signalements pour une entité spécifique.
     */
    fun findByReportedEntityTypeAndReportedEntityIdOrderByCreatedAtDesc(
        reportedEntityType: ReportedEntityType,
        reportedEntityId: String,
        pageable: Pageable
    ): Page<Report>

    /**
     * Récupère tous les signalements avec un statut spécifique.
     */
    fun findByStatusOrderByCreatedAtDesc(
        status: ReportStatus,
        pageable: Pageable
    ): Page<Report>

    /**
     * Récupère tous les signalements en attente (PENDING).
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.createdAt DESC")
    fun findPendingReports(pageable: Pageable): Page<Report>

    /**
     * Compte le nombre de signalements pour une entité spécifique.
     */
    fun countByReportedEntityTypeAndReportedEntityId(
        reportedEntityType: ReportedEntityType,
        reportedEntityId: String
    ): Long

    /**
     * Compte le nombre de signalements avec un statut spécifique pour une entité.
     */
    fun countByReportedEntityTypeAndReportedEntityIdAndStatus(
        reportedEntityType: ReportedEntityType,
        reportedEntityId: String,
        status: ReportStatus
    ): Long
}

