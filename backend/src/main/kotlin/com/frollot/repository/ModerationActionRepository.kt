package com.frollot.repository

import com.frollot.model.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des actions de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Repository
interface ModerationActionRepository : JpaRepository<ModerationActionEntity, String> {

    /**
     * Trouve toutes les actions de modération pour une entité spécifique.
     */
    fun findByContentEntityTypeAndContentEntityIdOrderByCreatedAtDesc(
        contentEntityType: ReportedEntityType,
        contentEntityId: String,
        pageable: Pageable
    ): Page<ModerationActionEntity>

    /**
     * Trouve la dernière action de modération active pour une entité spécifique.
     * Une action est active si elle n'a pas été annulée par un appel approuvé.
     */
    fun findFirstByContentEntityTypeAndContentEntityIdAndAppealStatusNotOrderByCreatedAtDesc(
        contentEntityType: ReportedEntityType,
        contentEntityId: String,
        appealStatus: AppealStatus
    ): ModerationActionEntity?

    /**
     * Trouve toutes les actions de modération effectuées par un modérateur.
     */
    fun findByModeratorIdOrderByCreatedAtDesc(
        moderatorId: String,
        pageable: Pageable
    ): Page<ModerationActionEntity>

    /**
     * Trouve toutes les actions de modération avec un type d'action spécifique.
     */
    fun findByActionOrderByCreatedAtDesc(
        action: ModerationActionType,
        pageable: Pageable
    ): Page<ModerationActionEntity>

    /**
     * Trouve toutes les actions de modération avec un statut d'appel spécifique.
     */
    fun findByAppealStatusOrderByCreatedAtDesc(
        appealStatus: AppealStatus,
        pageable: Pageable
    ): Page<ModerationActionEntity>

    /**
     * Compte le nombre d'actions de modération pour une entité spécifique.
     */
    fun countByContentEntityTypeAndContentEntityId(
        contentEntityType: ReportedEntityType,
        contentEntityId: String
    ): Long

    /**
     * Vérifie si une entité a une action de modération active (non annulée).
     */
    fun existsByContentEntityTypeAndContentEntityIdAndActionAndAppealStatusNot(
        contentEntityType: ReportedEntityType,
        contentEntityId: String,
        action: ModerationActionType,
        appealStatus: AppealStatus
    ): Boolean
}

