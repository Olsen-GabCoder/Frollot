package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un signalement de contenu.
 * Phase H.1 - Signalement de Contenu
 * 
 * Permet aux utilisateurs de signaler du contenu inapproprié (posts, commentaires, utilisateurs, salons).
 */
@Entity
@Table(
    name = "reports",
    indexes = [
        Index(name = "idx_reported_entity", columnList = "reported_entity_type,reported_entity_id"),
        Index(name = "idx_reporter", columnList = "reporter_id"),
        Index(name = "idx_status", columnList = "status"),
        Index(name = "idx_reason", columnList = "reason"),
        Index(name = "idx_created_at", columnList = "created_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_reporter_entity",
            columnNames = ["reporter_id", "reported_entity_type", "reported_entity_id"]
        )
    ]
)
data class Report(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "reported_entity_type", nullable = false, length = 20)
    var reportedEntityType: ReportedEntityType = ReportedEntityType.POST,

    @Column(name = "reported_entity_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var reportedEntityId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnore
    var reporter: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20)
    var reason: ReportReason = ReportReason.AUTRE,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ReportStatus = ReportStatus.PENDING,

    @Column(name = "additional_info", columnDefinition = "TEXT")
    var additionalInfo: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le signalement est valide.
     */
    fun isValid(): Boolean {
        return reportedEntityId.isNotBlank() && reporter != null
    }

    /**
     * Retourne l'ID du rapporteur.
     */
    fun getReporterId(): String? = reporter?.id
}

