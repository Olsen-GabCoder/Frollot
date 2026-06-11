package com.frollot.dto

import com.frollot.model.Payment
import com.frollot.model.StripePaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO de réponse pour un PaymentIntent créé.
 */
data class PaymentIntentResponse(
    val paymentIntentId: String,
    val clientSecret: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String
)

/**
 * DTO de réponse pour un paiement.
 */
data class PaymentResponse(
    val id: String,
    val bookingId: String,
    val clientId: String,
    val salonId: String,
    val stripePaymentIntentId: String?,
    val stripeChargeId: String?,
    val amount: BigDecimal,
    val currency: String,
    val status: StripePaymentStatus,
    val statusLabel: String,
    val paymentMethod: String?,
    val paymentMethodType: String?,
    val description: String?,
    val failureReason: String?,
    val refundedAmount: BigDecimal,
    val remainingRefundableAmount: BigDecimal,
    val createdAt: LocalDateTime?,
    val paidAt: LocalDateTime?,
    val refundedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(payment: Payment): PaymentResponse {
            return PaymentResponse(
                id = payment.id!!,
                bookingId = payment.booking?.id!!,
                clientId = payment.client?.id!!,
                salonId = payment.salon?.id!!,
                stripePaymentIntentId = payment.stripePaymentIntentId,
                stripeChargeId = payment.stripeChargeId,
                amount = payment.amount,
                currency = payment.currency,
                status = payment.status,
                statusLabel = payment.status.getDisplayName(),
                paymentMethod = payment.paymentMethod,
                paymentMethodType = payment.paymentMethodType,
                description = payment.description,
                failureReason = payment.failureReason,
                refundedAmount = payment.refundedAmount,
                remainingRefundableAmount = payment.getRemainingRefundableAmount(),
                createdAt = payment.createdAt,
                paidAt = payment.paidAt,
                refundedAt = payment.refundedAt
            )
        }
    }
}

/**
 * DTO pour créer un remboursement.
 */
data class RefundRequest(
    val amount: BigDecimal? = null // null = remboursement total
)

// ========== STRIPE CHECKOUT ==========

/**
 * DTO de requête pour créer une Stripe Checkout Session.
 */
data class CheckoutSessionRequest(
    val bookingId: String,
    val successUrl: String,
    val cancelUrl: String
)

/**
 * DTO de réponse pour une Stripe Checkout Session.
 */
data class CheckoutSessionResponse(
    val sessionId: String,
    val checkoutUrl: String,
    val expiresAt: Long
)

/**
 * DTO pour le statut d'une session de paiement.
 */
data class PaymentSessionStatus(
    val sessionId: String,
    val paymentStatus: String,
    val bookingId: String,
    val amountTotal: Long,
    val currency: String,
    val customerEmail: String?
)

