package com.frollot.mobile.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Réponse pour un PaymentIntent créé.
 */
@Serializable
data class PaymentIntentResponse(
    @SerialName("paymentIntentId")
    val paymentIntentId: String,

    @SerialName("clientSecret")
    val clientSecret: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("currency")
    val currency: String,

    @SerialName("status")
    val status: String
)

/**
 * Réponse pour un paiement.
 */
@Serializable
data class PaymentResponse(
    @SerialName("id")
    val id: String,

    @SerialName("bookingId")
    val bookingId: String,

    @SerialName("clientId")
    val clientId: String,

    @SerialName("salonId")
    val salonId: String,

    @SerialName("stripePaymentIntentId")
    val stripePaymentIntentId: String? = null,

    @SerialName("stripeChargeId")
    val stripeChargeId: String? = null,

    @SerialName("amount")
    val amount: Double,

    @SerialName("currency")
    val currency: String,

    @SerialName("status")
    val status: PaymentStatus,

    @SerialName("statusLabel")
    val statusLabel: String,

    @SerialName("paymentMethod")
    val paymentMethod: String? = null,

    @SerialName("paymentMethodType")
    val paymentMethodType: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("failureReason")
    val failureReason: String? = null,

    @SerialName("refundedAmount")
    val refundedAmount: Double,

    @SerialName("remainingRefundableAmount")
    val remainingRefundableAmount: Double
)

/**
 * Requête pour un remboursement.
 */
@Serializable
data class RefundRequest(
    @SerialName("amount")
    val amount: Double? = null // null = remboursement total
)

// ========== STRIPE CHECKOUT ==========

/**
 * Requête pour créer une Stripe Checkout Session.
 */
@Serializable
data class CheckoutSessionRequest(
    @SerialName("bookingId")
    val bookingId: String,

    @SerialName("successUrl")
    val successUrl: String,

    @SerialName("cancelUrl")
    val cancelUrl: String
)

/**
 * Réponse d'une Stripe Checkout Session créée.
 */
@Serializable
data class CheckoutSessionResponse(
    @SerialName("sessionId")
    val sessionId: String,

    @SerialName("checkoutUrl")
    val checkoutUrl: String,

    @SerialName("expiresAt")
    val expiresAt: Long
)

/**
 * Statut d'une session de paiement.
 */
@Serializable
data class PaymentSessionStatus(
    @SerialName("sessionId")
    val sessionId: String,

    @SerialName("paymentStatus")
    val paymentStatus: String,

    @SerialName("bookingId")
    val bookingId: String,

    @SerialName("amountTotal")
    val amountTotal: Long,

    @SerialName("currency")
    val currency: String,

    @SerialName("customerEmail")
    val customerEmail: String? = null
) {
    val isPaid: Boolean
        get() = paymentStatus == "paid"
    
    val formattedAmount: String
        get() = "${amountTotal / 100.0}€"
}
