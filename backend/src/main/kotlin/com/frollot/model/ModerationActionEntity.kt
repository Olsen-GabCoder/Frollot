package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

/**
 * Entité représentant une action de modération effectuée sur un contenu.
 * Phase H.3 - Modération de Contenu Coiffure
 * 
 * Cette entité enregistre toutes les actions de modération (masquer, supprimer, avertir)
 * effectuées par les administrateurs sur les contenus (posts, commentaires, utilisateurs, salons).
 */
@Entity
@Table(
    name = "moderation_actions",
    indexes = [
        Index(name = "idx_content_entity", columnList = "content_entity_type, content_entity_id"),
        Index(name = "idx_moderator", columnList = "moderator_id"),
        Index(name = "idx_action", columnList = "action"),
        Index(name = "idx_created_at", columnList = "created_at"),
        Index(name = "idx_appeal_status", columnList = "appeal_status")
    ]
)
data class ModerationActionEntity(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_entity_type", nullable = false, length = 20)
    var contentEntityType: ReportedEntityType = ReportedEntityType.POST,

    @Column(name = "content_entity_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var contentEntityId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    var action: ModerationActionType = ModerationActionType.WARN,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false)
    @JsonIgnore
    var moderator: User? = null,

    @Column(name = "reason", columnDefinition = "TEXT")
    var reason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "appeal_status", nullable = false, length = 20)
    var appealStatus: AppealStatus = AppealStatus.NONE,

    @Column(name = "appeal_reason", columnDefinition = "TEXT")
    var appealReason: String? = null,

    @Column(name = "appeal_processed_at")
    var appealProcessedAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si l'action de modération est valide.
     */
    fun isValid(): Boolean {
        return contentEntityId.isNotBlank() && moderator != null && action != null
    }

    /**
     * Vérifie si l'action peut être annulée (appel approuvé).
     */
    fun canBeReversed(): Boolean {
        return appealStatus == AppealStatus.APPROVED
    }
}

