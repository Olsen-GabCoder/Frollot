package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un post archivé par un utilisateur.
 * 
 * Contrainte d'unicité : Un utilisateur ne peut archiver un post qu'une seule fois.
 * Un post archivé est masqué du feed principal de l'utilisateur mais reste accessible dans les archives.
 */
@Entity
@Table(
    name = "post_archives",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_archive_user",
            columnNames = ["post_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_archive_post", columnList = "post_id"),
        Index(name = "idx_archive_user", columnList = "user_id")
    ]
)
data class PostArchive(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var post: Post? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    var user: User? = null,

    @CreationTimestamp
    @Column(name = "archived_at", updatable = false, nullable = false)
    var archivedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si l'archive est valide.
     */
    fun isValid(): Boolean {
        return post != null && user != null
    }
}

