package com.frollot.repository

import com.frollot.model.Payment
import com.frollot.model.StripePaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface PaymentRepository : JpaRepository<Payment, String> {

    /**
     * Trouve un paiement par son PaymentIntent Stripe.
     */
    fun findByStripePaymentIntentId(paymentIntentId: String): Payment?

    /**
     * Trouve un paiement par son Checkout Session ID Stripe.
     */
    fun findByStripeSessionId(sessionId: String): Payment?

    /**
     * Trouve tous les paiements d'une réservation.
     */
    fun findByBookingIdOrderByCreatedAtDesc(bookingId: String): List<Payment>

    /**
     * Trouve tous les paiements d'un client.
     */
    fun findByClientIdOrderByCreatedAtDesc(clientId: String): List<Payment>

    /**
     * Trouve tous les paiements d'un salon.
     */
    fun findBySalonIdOrderByCreatedAtDesc(salonId: String): List<Payment>

    /**
     * Trouve les paiements réussis d'un salon.
     */
    fun findBySalonIdAndStatus(salonId: String, status: StripePaymentStatus): List<Payment>

    /**
     * Calcule le revenu total d'un salon (paiements réussis).
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM Payment p 
        WHERE p.salon.id = :salonId 
        AND p.status = :status
    """)
    fun calculateTotalRevenue(
        @Param("salonId") salonId: String,
        @Param("status") status: StripePaymentStatus
    ): BigDecimal

    /**
     * Compte les paiements par statut pour un salon.
     */
    fun countBySalonIdAndStatus(salonId: String, status: StripePaymentStatus): Long
}

