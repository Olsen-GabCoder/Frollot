package com.frollot.service

import com.frollot.dto.*
import com.frollot.model.*
import com.frollot.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service de gestion de la modération et des signalements.
 * Phase H.1 - Signalement de Contenu
 * 
 * Gère :
 * - Création de signalements par les utilisateurs
 * - Consultation des signalements (admin)
 * - Traitement des signalements (admin)
 */
@Service
@Transactional
class ModerationService(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val salonRepository: SalonRepository,
    private val moderationActionRepository: ModerationActionRepository // Phase H.3
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class ReportNotFoundException(reportId: String) :
        RuntimeException("Signalement avec ID '$reportId' non trouvé")

    class ReportAlreadyExistsException(message: String) : RuntimeException(message)

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé")

    class EntityNotFoundException(entityType: ReportedEntityType, entityId: String) :
        RuntimeException("Entité ${entityType.name} avec ID '$entityId' non trouvée")

    // ========== GESTION DES SIGNALEMENTS ==========

    /**
     * Crée un nouveau signalement de contenu.
     * Phase H.1 - Signalement de Contenu
     * 
     * @param request Données du signalement
     * @param reporterId ID de l'utilisateur qui signale
     * @return ReportResponse avec les détails du signalement créé
     */
    @Transactional
    fun reportContent(request: CreateReportRequest, reporterId: String): ReportResponse {
        // Validation
        request.validate()

        // Vérification de l'existence du rapporteur
        val reporter = userRepository.findById(reporterId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$reporterId' non trouvé") }

        // Vérification que l'entité signalée existe
        when (request.reportedEntityType) {
            ReportedEntityType.POST -> {
                if (!postRepository.existsById(request.reportedEntityId)) {
                    throw EntityNotFoundException(ReportedEntityType.POST, request.reportedEntityId)
                }
            }
            ReportedEntityType.COMMENT -> {
                if (!commentRepository.existsById(request.reportedEntityId)) {
                    throw EntityNotFoundException(ReportedEntityType.COMMENT, request.reportedEntityId)
                }
            }
            ReportedEntityType.USER -> {
                if (!userRepository.existsById(request.reportedEntityId)) {
                    throw EntityNotFoundException(ReportedEntityType.USER, request.reportedEntityId)
                }
            }
            ReportedEntityType.SALON -> {
                if (!salonRepository.existsById(request.reportedEntityId)) {
                    throw EntityNotFoundException(ReportedEntityType.SALON, request.reportedEntityId)
                }
            }
        }

        // Vérification que l'utilisateur n'a pas déjà signalé cette entité
        if (reportRepository.existsByReporterIdAndReportedEntityTypeAndReportedEntityId(
                reporterId,
                request.reportedEntityType,
                request.reportedEntityId
            )
        ) {
            throw ReportAlreadyExistsException(
                "Vous avez déjà signalé cette ${request.reportedEntityType.getDisplayName().lowercase()}"
            )
        }

        // Création du signalement
        val report = Report(
            id = UUID.randomUUID().toString(),
            reportedEntityType = request.reportedEntityType,
            reportedEntityId = request.reportedEntityId,
            reporter = reporter,
            reason = request.reason,
            status = ReportStatus.PENDING,
            additionalInfo = request.additionalInfo?.takeIf { it.isNotBlank() }
        )

        // Validation de l'entité
        if (!report.isValid()) {
            throw IllegalArgumentException("Les données du signalement sont invalides")
        }

        // Sauvegarde
        val savedReport = reportRepository.save(report)
        println("🚨 Signalement créé: ${request.reportedEntityType.name} $request.reportedEntityId par User $reporterId")

        return ReportResponse.fromEntity(savedReport)
    }

    /**
     * Récupère tous les signalements avec pagination (admin uniquement).
     * Phase H.1 - Signalement de Contenu
     * 
     * @param status Filtre par statut (optionnel)
     * @param pageable Paramètres de pagination
     * @param adminId ID de l'administrateur (pour vérification)
     * @return Page de ReportResponse
     */
    @Transactional(readOnly = true)
    fun getReports(
        status: ReportStatus? = null,
        pageable: Pageable,
        adminId: String
    ): Page<ReportResponse> {
        // Vérification que l'utilisateur est admin
        val admin = userRepository.findById(adminId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$adminId' non trouvé") }

        if (admin.userType != UserType.admin) {
            throw UnauthorizedAccessException(adminId)
        }

        // Récupération des signalements
        val reportsPage = if (status != null) {
            reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
        } else {
            reportRepository.findAll(pageable)
        }

        return reportsPage.map { ReportResponse.fromEntity(it) }
    }

    /**
     * Récupère les signalements en attente (admin uniquement).
     * Phase H.1 - Signalement de Contenu
     * 
     * @param pageable Paramètres de pagination
     * @param adminId ID de l'administrateur (pour vérification)
     * @return Page de ReportResponse
     */
    @Transactional(readOnly = true)
    fun getPendingReports(pageable: Pageable, adminId: String): Page<ReportResponse> {
        // Vérification que l'utilisateur est admin
        val admin = userRepository.findById(adminId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$adminId' non trouvé") }

        if (admin.userType != UserType.admin) {
            throw UnauthorizedAccessException(adminId)
        }

        val reportsPage = reportRepository.findPendingReports(pageable)
        return reportsPage.map { ReportResponse.fromEntity(it) }
    }

    /**
     * Traite un signalement (admin uniquement).
     * Phase H.1 - Signalement de Contenu
     * 
     * @param reportId ID du signalement à traiter
     * @param request Données de traitement (nouveau statut)
     * @param adminId ID de l'administrateur (pour vérification)
     * @return ReportResponse avec le statut mis à jour
     */
    @Transactional
    fun handleReport(
        reportId: String,
        request: HandleReportRequest,
        adminId: String
    ): ReportResponse {
        // Validation
        request.validate()

        // Vérification que l'utilisateur est admin
        val admin = userRepository.findById(adminId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$adminId' non trouvé") }

        if (admin.userType != UserType.admin) {
            throw UnauthorizedAccessException(adminId)
        }

        // Récupération du signalement
        val report = reportRepository.findById(reportId)
            .orElseThrow { ReportNotFoundException(reportId) }

        // Mise à jour du statut
        report.status = request.status

        // Sauvegarde
        val savedReport = reportRepository.save(report)
        println("✅ Signalement traité: $reportId -> ${request.status.name} par Admin $adminId")

        return ReportResponse.fromEntity(savedReport)
    }

    /**
     * Récupère les signalements d'un utilisateur.
     * Phase H.1 - Signalement de Contenu
     * 
     * @param userId ID de l'utilisateur
     * @param pageable Paramètres de pagination
     * @return Page de ReportResponse
     */
    @Transactional(readOnly = true)
    fun getReportsByUser(userId: String, pageable: Pageable): Page<ReportResponse> {
        val reportsPage = reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable)
        return reportsPage.map { ReportResponse.fromEntity(it) }
    }

    /**
     * Récupère les signalements pour une entité spécifique.
     * Phase H.1 - Signalement de Contenu
     * 
     * @param entityType Type de l'entité
     * @param entityId ID de l'entité
     * @param pageable Paramètres de pagination
     * @return Page de ReportResponse
     */
    @Transactional(readOnly = true)
    fun getReportsByEntity(
        entityType: ReportedEntityType,
        entityId: String,
        pageable: Pageable
    ): Page<ReportResponse> {
        val reportsPage = reportRepository.findByReportedEntityTypeAndReportedEntityIdOrderByCreatedAtDesc(
            entityType,
            entityId,
            pageable
        )
        return reportsPage.map { ReportResponse.fromEntity(it) }
    }

    /**
     * Compte le nombre de signalements pour une entité.
     * Phase H.1 - Signalement de Contenu
     * 
     * @param entityType Type de l'entité
     * @param entityId ID de l'entité
     * @return Nombre de signalements
     */
    @Transactional(readOnly = true)
    fun countReportsByEntity(entityType: ReportedEntityType, entityId: String): Long {
        return reportRepository.countByReportedEntityTypeAndReportedEntityId(entityType, entityId)
    }

    // ========== GESTION DE LA MODÉRATION (H.3) ==========

    /**
     * Modère un contenu (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param request Données de modération
     * @param moderatorId ID du modérateur (doit être admin)
     * @return ModerationActionResponse
     */
    fun moderateContent(request: ModerateContentRequest, moderatorId: String): ModerationActionResponse {
        request.validate()

        val moderator = userRepository.findById(moderatorId)
            .orElseThrow { EntityNotFoundException(ReportedEntityType.USER, moderatorId) }

        if (moderator.userType != UserType.admin) {
            throw UnauthorizedAccessException(moderatorId)
        }

        // Vérifier que l'entité existe
        when (request.contentEntityType) {
            ReportedEntityType.POST -> {
                val post = postRepository.findById(request.contentEntityId)
                    .orElseThrow { EntityNotFoundException(request.contentEntityType, request.contentEntityId) }
                
                // Appliquer l'action de modération
                when (request.action) {
                    ModerationActionType.HIDE -> {
                        post.isHidden = true
                        postRepository.save(post)
                    }
                    ModerationActionType.DELETE -> {
                        post.isDeleted = true
                        post.isHidden = true
                        postRepository.save(post)
                    }
                    ModerationActionType.WARN -> {
                        // Avertissement : pas de modification du contenu, juste enregistrement
                    }
                }
            }
            ReportedEntityType.COMMENT -> {
                val comment = commentRepository.findById(request.contentEntityId)
                    .orElseThrow { EntityNotFoundException(request.contentEntityType, request.contentEntityId) }
                
                when (request.action) {
                    ModerationActionType.HIDE -> {
                        comment.isHidden = true
                        commentRepository.save(comment)
                    }
                    ModerationActionType.DELETE -> {
                        comment.isDeleted = true
                        comment.isHidden = true
                        commentRepository.save(comment)
                    }
                    ModerationActionType.WARN -> {
                        // Avertissement : pas de modification du contenu
                    }
                }
            }
            ReportedEntityType.USER, ReportedEntityType.SALON -> {
                // Pour les utilisateurs et salons, on enregistre juste l'action
                // (pas de modification directe de l'entité pour l'instant)
            }
        }

        // Créer l'action de modération
        val moderationAction = ModerationActionEntity(
            id = UUID.randomUUID().toString(),
            contentEntityType = request.contentEntityType,
            contentEntityId = request.contentEntityId,
            action = request.action,
            moderator = moderator,
            reason = request.reason?.takeIf { it.isNotBlank() },
            appealStatus = AppealStatus.NONE
        )

        val savedAction = moderationActionRepository.save(moderationAction)
        println("🔨 Action de modération créée: ${savedAction.id} - ${request.action} sur ${request.contentEntityType} ${request.contentEntityId} par ${moderator.email}")
        
        return ModerationActionResponse.fromEntity(savedAction)
    }

    /**
     * Fait appel d'une action de modération.
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param request Données de l'appel
     * @param userId ID de l'utilisateur faisant l'appel (doit être l'auteur du contenu)
     * @return ModerationActionResponse avec le statut d'appel mis à jour
     */
    fun appealModeration(request: AppealModerationRequest, userId: String): ModerationActionResponse {
        request.validate()

        val moderationAction = moderationActionRepository.findById(request.moderationActionId)
            .orElseThrow { RuntimeException("Action de modération avec ID '${request.moderationActionId}' non trouvée") }

        // Vérifier que l'utilisateur est l'auteur du contenu modéré
        when (moderationAction.contentEntityType) {
            ReportedEntityType.POST -> {
                val post = postRepository.findById(moderationAction.contentEntityId)
                    .orElseThrow { EntityNotFoundException(ReportedEntityType.POST, moderationAction.contentEntityId) }
                if (post.author?.id != userId) {
                    throw UnauthorizedAccessException(userId)
                }
            }
            ReportedEntityType.COMMENT -> {
                val comment = commentRepository.findById(moderationAction.contentEntityId)
                    .orElseThrow { EntityNotFoundException(ReportedEntityType.COMMENT, moderationAction.contentEntityId) }
                if (comment.author?.id != userId) {
                    throw UnauthorizedAccessException(userId)
                }
            }
            ReportedEntityType.USER -> {
                if (moderationAction.contentEntityId != userId) {
                    throw UnauthorizedAccessException(userId)
                }
            }
            ReportedEntityType.SALON -> {
                // Vérifier que l'utilisateur est propriétaire du salon
                val salon = salonRepository.findById(moderationAction.contentEntityId)
                    .orElseThrow { EntityNotFoundException(ReportedEntityType.SALON, moderationAction.contentEntityId) }
                if (salon.owner?.id != userId) {
                    throw UnauthorizedAccessException(userId)
                }
            }
        }

        // Vérifier qu'un appel n'a pas déjà été fait
        if (moderationAction.appealStatus != AppealStatus.NONE) {
            throw RuntimeException("Un appel a déjà été fait pour cette action de modération")
        }

        // Mettre à jour l'action avec l'appel
        moderationAction.appealStatus = AppealStatus.PENDING
        moderationAction.appealReason = request.appealReason

        val savedAction = moderationActionRepository.save(moderationAction)
        println("📝 Appel de modération créé: ${savedAction.id} par utilisateur $userId")
        
        return ModerationActionResponse.fromEntity(savedAction)
    }

    /**
     * Traite un appel de modération (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param moderationActionId ID de l'action de modération
     * @param request Données de traitement de l'appel
     * @param adminId ID de l'admin traitant l'appel
     * @return ModerationActionResponse avec le statut d'appel mis à jour
     */
    fun handleAppeal(moderationActionId: String, request: HandleAppealRequest, adminId: String): ModerationActionResponse {
        request.validate()

        val admin = userRepository.findById(adminId)
            .orElseThrow { EntityNotFoundException(ReportedEntityType.USER, adminId) }

        if (admin.userType != UserType.admin) {
            throw UnauthorizedAccessException(adminId)
        }

        val moderationAction = moderationActionRepository.findById(moderationActionId)
            .orElseThrow { RuntimeException("Action de modération avec ID '$moderationActionId' non trouvée") }

        if (moderationAction.appealStatus != AppealStatus.PENDING) {
            throw RuntimeException("L'appel doit être en statut PENDING pour être traité")
        }

        // Mettre à jour le statut de l'appel
        moderationAction.appealStatus = request.appealStatus
        moderationAction.appealProcessedAt = java.time.LocalDateTime.now()

        // Si l'appel est approuvé, annuler l'action de modération
        if (request.appealStatus == AppealStatus.APPROVED) {
            when (moderationAction.contentEntityType) {
                ReportedEntityType.POST -> {
                    val post = postRepository.findById(moderationAction.contentEntityId)
                        .orElseThrow { EntityNotFoundException(ReportedEntityType.POST, moderationAction.contentEntityId) }
                    if (moderationAction.action == ModerationActionType.HIDE) {
                        post.isHidden = false
                    } else if (moderationAction.action == ModerationActionType.DELETE) {
                        post.isDeleted = false
                        post.isHidden = false
                    }
                    postRepository.save(post)
                }
                ReportedEntityType.COMMENT -> {
                    val comment = commentRepository.findById(moderationAction.contentEntityId)
                        .orElseThrow { EntityNotFoundException(ReportedEntityType.COMMENT, moderationAction.contentEntityId) }
                    if (moderationAction.action == ModerationActionType.HIDE) {
                        comment.isHidden = false
                    } else if (moderationAction.action == ModerationActionType.DELETE) {
                        comment.isDeleted = false
                        comment.isHidden = false
                    }
                    commentRepository.save(comment)
                }
                else -> {
                    // Pour USER et SALON, pas de modification directe
                }
            }
        }

        val savedAction = moderationActionRepository.save(moderationAction)
        println("✅ Appel de modération traité: ${savedAction.id} -> ${request.appealStatus.name} par Admin $adminId")
        
        return ModerationActionResponse.fromEntity(savedAction)
    }

    /**
     * Récupère les actions de modération pour une entité spécifique.
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Transactional(readOnly = true)
    fun getModerationActionsByEntity(
        entityType: ReportedEntityType,
        entityId: String,
        pageable: Pageable
    ): Page<ModerationActionResponse> {
        return moderationActionRepository.findByContentEntityTypeAndContentEntityIdOrderByCreatedAtDesc(
            entityType,
            entityId,
            pageable
        ).map { ModerationActionResponse.fromEntity(it) }
    }

    /**
     * Récupère toutes les actions de modération (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Transactional(readOnly = true)
    fun getAllModerationActions(pageable: Pageable): Page<ModerationActionResponse> {
        return moderationActionRepository.findAll(pageable).map { ModerationActionResponse.fromEntity(it) }
    }

    /**
     * Récupère les appels en attente (admin uniquement).
     * Phase H.3 - Modération de Contenu Coiffure
     */
    @Transactional(readOnly = true)
    fun getPendingAppeals(pageable: Pageable): Page<ModerationActionResponse> {
        return moderationActionRepository.findByAppealStatusOrderByCreatedAtDesc(
            AppealStatus.PENDING,
            pageable
        ).map { ModerationActionResponse.fromEntity(it) }
    }
}

