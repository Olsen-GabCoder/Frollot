package com.frollot.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Énumération des statuts de paiement Stripe.
 */
enum class StripePaymentStatus {
    pending,        // Paiement en attente
    processing,     // Paiement en cours de traitement
    succeeded,      // Paiement réussi
    failed,         // Paiement échoué
    canceled,       // Paiement annulé
    refunded,       // Paiement remboursé intégralement
    partially_refunded; // Paiement partiellement remboursé

    fun getDisplayName(): String {
        return when (this) {
            pending -> "En attente"
            processing -> "En cours"
            succeeded -> "Payé"
            failed -> "Échoué"
            canceled -> "Annulé"
            refunded -> "Remboursé"
            partially_refunded -> "Partiellement remboursé"
        }
    }
}

/**
 * Entité représentant un paiement Stripe.
 *
 * Stocke toutes les informations liées à une transaction de paiement :
 * - Référence Stripe (PaymentIntent, Charge)
 * - Montant et devise
 * - Statut du paiement
 * - Informations de remboursement
 */
@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payment_booking", columnList = "booking_id"),
        Index(name = "idx_payment_client", columnList = "client_id"),
        Index(name = "idx_payment_salon", columnList = "salon_id"),
        Index(name = "idx_payment_stripe_intent", columnList = "stripe_payment_intent_id"),
        Index(name = "idx_payment_status", columnList = "status"),
        Index(name = "idx_payment_created_at", columnList = "created_at")
    ]
)
data class Payment(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    var booking: Booking? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    var client: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    @JsonIgnore
    var salon: Salon? = null,

    @Column(name = "stripe_payment_intent_id", unique = true, length = 255)
    var stripePaymentIntentId: String? = null,

    @Column(name = "stripe_charge_id", length = 255)
    var stripeChargeId: String? = null,

    @Column(name = "stripe_session_id", length = 255)
    var stripeSessionId: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(length = 3, nullable = false)
    var currency: String = "EUR",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: StripePaymentStatus = StripePaymentStatus.pending,

    @Column(name = "payment_method", length = 50)
    var paymentMethod: String? = null,

    @Column(name = "payment_method_type", length = 50)
    var paymentMethodType: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    var metadata: Map<String, String>? = null,

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    var failureReason: String? = null,

    @Column(name = "refunded_amount", precision = 10, scale = 2, nullable = false)
    var refundedAmount: BigDecimal = BigDecimal.ZERO,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null,

    @Column(name = "refunded_at")
    var refundedAt: LocalDateTime? = null
) {
    /**
     * Vérifie si le paiement est réussi.
     */
    fun isSucceeded(): Boolean {
        return status == StripePaymentStatus.succeeded
    }

    /**
     * Vérifie si le paiement peut être remboursé.
     */
    fun canBeRefunded(): Boolean {
        return status == StripePaymentStatus.succeeded && refundedAmount < amount
    }

    /**
     * Calcule le montant restant à rembourser.
     */
    fun getRemainingRefundableAmount(): BigDecimal {
        return if (canBeRefunded()) {
            amount - refundedAmount
        } else {
            BigDecimal.ZERO
        }
    }

    /**
     * Vérifie si le paiement est en échec.
     */
    fun isFailed(): Boolean {
        return status == StripePaymentStatus.failed || status == StripePaymentStatus.canceled
    }
}

