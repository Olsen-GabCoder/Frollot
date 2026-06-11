package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entité représentant un avis client sur un salon.
 *
 * Un client peut laisser un avis après une réservation terminée (status = completed).
 * Un seul avis par réservation est autorisé.
 *
 * @property id Identifiant unique (UUID)
 * @property salon Salon évalué (relation ManyToOne)
 * @property staff Coiffeur évalué (optionnel - relation ManyToOne)
 * @property client Client ayant laissé l'avis (relation ManyToOne)
 * @property booking Réservation associée (relation OneToOne, unique)
 * @property rating Note de 1 à 5 étoiles
 * @property title Titre de l'avis (optionnel)
 * @property content Commentaire de l'avis
 * @property responseSalon Réponse du salon (optionnel)
 * @property responseAt Date de réponse du salon
 * @property isVerified Indique si l'avis est vérifié (basé sur la réservation)
 * @property isVisible Indique si l'avis est visible publiquement
 * @property createdAt Date de création de l'avis
 */
@Entity
@Table(
    name = "reviews",
    indexes = [
        Index(name = "idx_review_salon", columnList = "salon_id"),
        Index(name = "idx_review_staff", columnList = "staff_id"),
        Index(name = "idx_review_rating", columnList = "rating"),
        Index(name = "idx_review_created_at", columnList = "created_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_review_booking",
            columnNames = ["booking_id"]
        )
    ]
)
data class Review(
    @Id
    @Column(
        name = "id",
        length = 36,
        columnDefinition = "CHAR(36)",
        nullable = false,
        updatable = false
    )
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "salon_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_review_salon")
    )
    @JsonIgnore
    var salon: Salon? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "staff_id",
        nullable = true,
        foreignKey = ForeignKey(name = "fk_review_staff")
    )
    @JsonIgnore
    var staff: SalonStaff? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "client_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_review_client")
    )
    @JsonIgnore
    var client: User? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "booking_id",
        nullable = true,
        unique = true,
        foreignKey = ForeignKey(name = "fk_review_booking")
    )
    @JsonIgnore
    var booking: Booking? = null,

    @Column(
        name = "rating",
        nullable = false
    )
    var rating: Int = 5,

    @Column(
        name = "title",
        length = 255,
        nullable = true
    )
    var title: String? = null,

    @Column(
        name = "content",
        columnDefinition = "TEXT",
        nullable = true
    )
    var content: String? = null,

    @Column(
        name = "response_salon",
        columnDefinition = "TEXT",
        nullable = true
    )
    var responseSalon: String? = null,

    @Column(
        name = "response_at",
        nullable = true
    )
    var responseAt: LocalDateTime? = null,

    @Column(
        name = "is_verified",
        nullable = false
    )
    var isVerified: Boolean = false,

    @Column(
        name = "is_visible",
        nullable = false
    )
    var isVisible: Boolean = true,

    @CreationTimestamp
    @Column(
        name = "created_at",
        updatable = false,
        nullable = false
    )
    var createdAt: LocalDateTime? = null
) {
    /**
     * Vérifie si la note est valide (entre 1 et 5).
     */
    fun isValidRating(): Boolean {
        return rating in 1..5
    }

    /**
     * Vérifie si l'avis peut être créé pour une réservation donnée.
     */
    fun canBeCreatedForBooking(booking: Booking): Boolean {
        return booking.status == BookingStatus.completed &&
                booking.client?.id == this.client?.id &&
                booking.salon?.id == this.salon?.id
    }
}

