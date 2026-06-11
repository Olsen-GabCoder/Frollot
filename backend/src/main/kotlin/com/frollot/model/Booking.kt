package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Entité représentant une réservation dans un salon de coiffure.
 *
 * Gère le cycle complet d'une réservation :
 * - Création (pending)
 * - Confirmation (confirmed)
 * - Exécution (in_progress)
 * - Finalisation (completed)
 * - Annulation possible (cancelled, no_show)
 *
 * @property id Identifiant unique (UUID)
 * @property salon Salon où se déroule la réservation
 * @property client Client ayant effectué la réservation
 * @property staff Coiffeur assigné (optionnel - peut être null si réservation générale)
 * @property service Service réservé (référence à salon_services)
 * @property bookingDatetime Date et heure de la réservation
 * @property durationMinutes Durée prévue en minutes
 * @property status Statut actuel de la réservation
 * @property priceFinal Prix final de la prestation
 * @property paymentStatus Statut du paiement
 * @property paymentMethod Méthode de paiement utilisée
 * @property notesClient Notes du client (demandes spéciales)
 * @property notesSalon Notes internes du salon
 * @property reminderSentAt Date d'envoi du rappel automatique
 * @property confirmedAt Date de confirmation de la réservation
 * @property completedAt Date de finalisation de la prestation
 * @property cancelledAt Date d'annulation (si applicable)
 * @property createdAt Date de création de la réservation
 * @property updatedAt Date de dernière modification
 */
@Entity
@Table(
    name = "bookings",
    indexes = [
        Index(name = "idx_booking_salon", columnList = "salon_id"),
        Index(name = "idx_booking_client", columnList = "client_id"),
        Index(name = "idx_booking_staff", columnList = "staff_id"),
        Index(name = "idx_booking_datetime", columnList = "booking_datetime"),
        Index(name = "idx_booking_status", columnList = "status")
    ]
)
data class Booking(
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
        foreignKey = ForeignKey(name = "fk_booking_salon")
    )
    @JsonIgnore
    var salon: Salon? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "client_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_booking_client")
    )
    @JsonIgnore
    var client: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "staff_id",
        nullable = true,
        foreignKey = ForeignKey(name = "fk_booking_staff")
    )
    @JsonIgnore
    var staff: SalonStaff? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "service_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_booking_service")
    )
    @JsonIgnore
    var service: SalonService? = null,

    @Column(
        name = "booking_datetime",
        nullable = false
    )
    var bookingDatetime: LocalDateTime = LocalDateTime.now(),

    @Column(
        name = "duration_minutes",
        nullable = false
    )
    var durationMinutes: Int = 30,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false
    )
    var status: BookingStatus = BookingStatus.pending,

    @Column(
        name = "price_final",
        precision = 10,
        scale = 2,
        nullable = true
    )
    var priceFinal: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "payment_status",
        nullable = false
    )
    var paymentStatus: PaymentStatus = PaymentStatus.unpaid,

    @Column(
        name = "payment_method",
        length = 50,
        nullable = true
    )
    var paymentMethod: String? = null,

    @Column(
        name = "notes_client",
        columnDefinition = "TEXT",
        nullable = true
    )
    var notesClient: String? = null,

    @Column(
        name = "notes_salon",
        columnDefinition = "TEXT",
        nullable = true
    )
    var notesSalon: String? = null,

    @Column(
        name = "reminder_sent_at",
        nullable = true
    )
    var reminderSentAt: LocalDateTime? = null,

    @Column(
        name = "confirmed_at",
        nullable = true
    )
    var confirmedAt: LocalDateTime? = null,

    @Column(
        name = "completed_at",
        nullable = true
    )
    var completedAt: LocalDateTime? = null,

    @Column(
        name = "cancelled_at",
        nullable = true
    )
    var cancelledAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(
        name = "created_at",
        updatable = false,
        nullable = false
    )
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(
        name = "updated_at",
        nullable = false
    )
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Calcule l'heure de fin de la réservation.
     */
    fun getEndDatetime(): LocalDateTime {
        return bookingDatetime.plusMinutes(durationMinutes.toLong())
    }

    /**
     * Vérifie si la réservation peut être annulée.
     */
    fun canBeCancelled(): Boolean {
        return status in listOf(BookingStatus.pending, BookingStatus.confirmed) &&
                bookingDatetime.isAfter(LocalDateTime.now())
    }

    /**
     * Vérifie si la réservation peut être confirmée.
     */
    fun canBeConfirmed(): Boolean {
        return status == BookingStatus.pending &&
                bookingDatetime.isAfter(LocalDateTime.now())
    }

    /**
     * Vérifie si deux réservations se chevauchent temporellement.
     */
    fun overlapsWith(other: Booking): Boolean {
        val thisStart = this.bookingDatetime
        val thisEnd = this.getEndDatetime()
        val otherStart = other.bookingDatetime
        val otherEnd = other.getEndDatetime()

        return !(thisEnd.isBefore(otherStart) || thisEnd.isEqual(otherStart) ||
                thisStart.isAfter(otherEnd) || thisStart.isEqual(otherEnd))
    }

    /**
     * Vérifie si la réservation est dans le passé.
     */
    fun isPast(): Boolean {
        return getEndDatetime().isBefore(LocalDateTime.now())
    }

    /**
     * Vérifie si la réservation est valide pour la création.
     */
    fun isValid(): Boolean {
        return salon != null &&
                client != null &&
                service != null &&
                bookingDatetime.isAfter(LocalDateTime.now()) &&
                durationMinutes > 0
    }

    override fun toString(): String {
        return "Booking(id=$id, salon=${salon?.id}, client=${client?.email}, " +
                "datetime=$bookingDatetime, status=$status, price=$priceFinal)"
    }
}

/**
 * Énumération des statuts possibles d'une réservation.
 *
 * IMPORTANT : Les noms correspondent EXACTEMENT aux valeurs MySQL (snake_case minuscules).
 */
enum class BookingStatus {
    /** Réservation créée, en attente de confirmation */
    @JsonProperty("pending")
    pending,

    /** Réservation confirmée par le salon */
    @JsonProperty("confirmed")
    confirmed,

    /** Prestation en cours d'exécution */
    @JsonProperty("in_progress")
    in_progress,

    /** Prestation terminée avec succès */
    @JsonProperty("completed")
    completed,

    /** Réservation annulée (par le client ou le salon) */
    @JsonProperty("cancelled")
    cancelled,

    /** Client ne s'est pas présenté */
    @JsonProperty("no_show")
    no_show;

    /**
     * Retourne le libellé français du statut.
     */
    fun getDisplayName(): String {
        return when (this) {
            pending -> "En attente"
            confirmed -> "Confirmée"
            in_progress -> "En cours"
            completed -> "Terminée"
            cancelled -> "Annulée"
            no_show -> "Absence"
        }
    }

    /**
     * Vérifie si le statut est final (ne peut plus être modifié).
     */
    fun isFinal(): Boolean {
        return this in listOf(completed, cancelled, no_show)
    }
}

/**
 * Énumération des statuts de paiement.
 */
enum class PaymentStatus {
    /** Paiement non effectué */
    @JsonProperty("unpaid")
    unpaid,

    /** Paiement effectué */
    @JsonProperty("paid")
    paid,

    /** Paiement remboursé */
    @JsonProperty("refunded")
    refunded;

    /**
     * Retourne le libellé français du statut.
     */
    fun getDisplayName(): String {
        return when (this) {
            unpaid -> "Non payé"
            paid -> "Payé"
            refunded -> "Remboursé"
        }
    }
}