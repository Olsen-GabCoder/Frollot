package com.frollot.dto

import com.frollot.model.*
import java.time.LocalDateTime

// ============================================
// REQUEST DTOs (Client → Serveur)
// Phase H.1 - Signalement de Contenu
// ============================================

/**
 * DTO pour créer un signalement de contenu.
 * Phase H.1 - Signalement de Contenu
 */
data class CreateReportRequest(
    val reportedEntityType: ReportedEntityType,
    val reportedEntityId: String,
    val reason: ReportReason,
    val additionalInfo: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (reportedEntityId.isBlank()) {
            throw IllegalArgumentException("L'ID de l'entité signalée ne peut pas être vide")
        }
        if (additionalInfo != null && additionalInfo.length > 1000) {
            throw IllegalArgumentException("Les informations supplémentaires ne peuvent pas dépasser 1000 caractères")
        }
    }
}

/**
 * DTO pour traiter un signalement (admin uniquement).
 * Phase H.1 - Signalement de Contenu
 */
data class HandleReportRequest(
    val status: ReportStatus,
    val adminNote: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (status == ReportStatus.PENDING) {
            throw IllegalArgumentException("Le statut ne peut pas être PENDING lors du traitement")
        }
        if (adminNote != null && adminNote.length > 500) {
            throw IllegalArgumentException("La note d'administration ne peut pas dépasser 500 caractères")
        }
    }
}

// ============================================
// RESPONSE DTOs (Serveur → Client)
// Phase H.1 - Signalement de Contenu
// ============================================

/**
 * DTO de réponse pour un signalement.
 * Phase H.1 - Signalement de Contenu
 */
data class ReportResponse(
    val id: String,
    val reportedEntityType: ReportedEntityType,
    val reportedEntityId: String,
    val reporterId: String,
    val reporterName: String,
    val reason: ReportReason,
    val status: ReportStatus,
    val additionalInfo: String? = null,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité Report en ReportResponse.
         */
        fun fromEntity(report: Report): ReportResponse {
            return ReportResponse(
                id = report.id!!,
                reportedEntityType = report.reportedEntityType,
                reportedEntityId = report.reportedEntityId,
                reporterId = report.reporter?.id ?: "",
                reporterName = report.reporter?.let { 
                    "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().ifBlank { it.email }
                } ?: "Utilisateur inconnu",
                reason = report.reason,
                status = report.status,
                additionalInfo = report.additionalInfo,
                createdAt = report.createdAt,
                updatedAt = report.updatedAt
            )
        }
    }
}

// ============================================
// REQUEST DTOs (Client → Serveur)
// Phase H.3 - Modération de Contenu Coiffure
// ============================================

/**
 * DTO pour modérer un contenu (admin uniquement).
 * Phase H.3 - Modération de Contenu Coiffure
 */
data class ModerateContentRequest(
    val contentEntityType: ReportedEntityType,
    val contentEntityId: String,
    val action: ModerationActionType,
    val reason: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (contentEntityId.isBlank()) {
            throw IllegalArgumentException("L'ID du contenu ne peut pas être vide")
        }
        if (reason != null && reason.length > 2000) {
            throw IllegalArgumentException("La raison de modération ne peut pas dépasser 2000 caractères")
        }
    }
}

/**
 * DTO pour faire appel d'une action de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
data class AppealModerationRequest(
    val moderationActionId: String,
    val appealReason: String
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (moderationActionId.isBlank()) {
            throw IllegalArgumentException("L'ID de l'action de modération ne peut pas être vide")
        }
        if (appealReason.isBlank()) {
            throw IllegalArgumentException("La raison de l'appel ne peut pas être vide")
        }
        if (appealReason.length > 2000) {
            throw IllegalArgumentException("La raison de l'appel ne peut pas dépasser 2000 caractères")
        }
    }
}

/**
 * DTO pour traiter un appel de modération (admin uniquement).
 * Phase H.3 - Modération de Contenu Coiffure
 */
data class HandleAppealRequest(
    val appealStatus: AppealStatus,
    val adminNote: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (appealStatus == AppealStatus.NONE || appealStatus == AppealStatus.PENDING) {
            throw IllegalArgumentException("Le statut d'appel doit être APPROVED ou REJECTED")
        }
        if (adminNote != null && adminNote.length > 500) {
            throw IllegalArgumentException("La note d'administration ne peut pas dépasser 500 caractères")
        }
    }
}

// ============================================
// RESPONSE DTOs (Serveur → Client)
// Phase H.3 - Modération de Contenu Coiffure
// ============================================

/**
 * DTO de réponse pour une action de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
data class ModerationActionResponse(
    val id: String,
    val contentEntityType: ReportedEntityType,
    val contentEntityId: String,
    val action: ModerationActionType,
    val moderatorId: String,
    val moderatorName: String,
    val reason: String?,
    val appealStatus: AppealStatus,
    val appealReason: String?,
    val appealProcessedAt: LocalDateTime?,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité ModerationActionEntity en ModerationActionResponse.
         */
        fun fromEntity(action: ModerationActionEntity): ModerationActionResponse {
            return ModerationActionResponse(
                id = action.id!!,
                contentEntityType = action.contentEntityType,
                contentEntityId = action.contentEntityId,
                action = action.action,
                moderatorId = action.moderator?.id ?: "",
                moderatorName = action.moderator?.let {
                    "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().ifBlank { it.email }
                } ?: "Modérateur inconnu",
                reason = action.reason,
                appealStatus = action.appealStatus,
                appealReason = action.appealReason,
                appealProcessedAt = action.appealProcessedAt,
                createdAt = action.createdAt
            )
        }
    }
}

