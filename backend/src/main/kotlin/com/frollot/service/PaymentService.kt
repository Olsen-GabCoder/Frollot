package com.frollot.service

import com.frollot.dto.CheckoutSessionRequest
import com.frollot.dto.CheckoutSessionResponse
import com.frollot.dto.PaymentIntentResponse
import com.frollot.dto.PaymentResponse
import com.frollot.dto.PaymentSessionStatus
import com.frollot.model.*
import com.frollot.model.StripePaymentStatus
import com.frollot.repository.BookingRepository
import com.frollot.repository.PaymentRepository
import com.frollot.repository.SalonRepository
import com.frollot.repository.UserRepository
import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.model.checkout.Session
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import com.stripe.param.checkout.SessionCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Service de gestion des paiements Stripe.
 *
 * Gère :
 * - Création de PaymentIntent
 * - Confirmation de paiement
 * - Gestion des webhooks Stripe
 * - Remboursements (total et partiel)
 */
@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository,
    private val salonRepository: SalonRepository,
    private val userRepository: UserRepository,
    @Value("\${stripe.secret-key}")
    private val stripeSecretKey: String
) {

    private val isStripeConfigured: Boolean

    init {
        // Vérifier si la clé Stripe est configurée (pas un placeholder)
        isStripeConfigured = stripeSecretKey.isNotBlank() && 
                !stripeSecretKey.contains("your_stripe_secret_key_here", ignoreCase = true) &&
                !stripeSecretKey.contains("changeme", ignoreCase = true)
        
        if (isStripeConfigured) {
            Stripe.apiKey = stripeSecretKey
            println("✅ Stripe configuré avec succès")
        } else {
            println("⚠️ ATTENTION: Clé Stripe non configurée ou placeholder détecté")
            println("   Pour activer Stripe, définissez STRIPE_SECRET_KEY avec une vraie clé API de test")
            println("   Exemple: STRIPE_SECRET_KEY=sk_test_51...")
        }
    }

    // ========== EXCEPTIONS MÉTIER ==========

    class PaymentNotFoundException(paymentId: String) :
        RuntimeException("Paiement avec ID '$paymentId' non trouvé")

    class BookingNotFoundException(bookingId: String) :
        RuntimeException("Réservation avec ID '$bookingId' non trouvée")

    class InvalidPaymentException(message: String) :
        RuntimeException(message)

    class StripePaymentException(message: String, cause: Throwable? = null) :
        RuntimeException(message, cause)

    // ========== CRÉATION DE PAYMENT INTENT ==========

    /**
     * Crée un PaymentIntent Stripe pour une réservation.
     *
     * @param bookingId ID de la réservation
     * @return PaymentIntent créé avec clientSecret pour le frontend
     */
    @Transactional
    fun createPaymentIntent(bookingId: String): PaymentIntentResponse {
        // 1. Vérifier que la réservation existe
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { BookingNotFoundException(bookingId) }

        // 2. Vérifier que la réservation n'a pas déjà un paiement réussi
        val existingPayment = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
            .firstOrNull { it.isSucceeded() }

        if (existingPayment != null) {
            throw InvalidPaymentException("Cette réservation a déjà été payée")
        }

        // 3. Calculer le montant (en centimes pour Stripe)
        val amount = booking.priceFinal ?: booking.service?.price ?: BigDecimal.ZERO
        val amountInCents = (amount * BigDecimal(100)).toLong()

        if (amountInCents <= 0) {
            throw InvalidPaymentException("Le montant doit être supérieur à 0")
        }

        // 4. Vérifier que Stripe est configuré
        if (!isStripeConfigured) {
            throw StripePaymentException(
                "Stripe n'est pas configuré. " +
                "Veuillez définir STRIPE_SECRET_KEY avec une clé API Stripe valide. " +
                "Pour les tests, utilisez une clé de test (sk_test_...). " +
                "Consultez https://stripe.com/docs/keys pour obtenir vos clés."
            )
        }

        // 5. Créer le PaymentIntent Stripe
        val params = PaymentIntentCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency("eur")
            .setDescription("Paiement réservation ${booking.id} - ${booking.service?.name}")
            .putMetadata("booking_id", bookingId)
            .putMetadata("salon_id", booking.salon?.id ?: "")
            .putMetadata("client_id", booking.client?.id ?: "")
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build()

        val paymentIntent: PaymentIntent = try {
            PaymentIntent.create(params)
        } catch (e: StripeException) {
            throw StripePaymentException("Erreur lors de la création du PaymentIntent: ${e.message}", e)
        }

        // 6. Créer l'entité Payment en base
        val payment = Payment(
            id = UUID.randomUUID().toString(),
            booking = booking,
            client = booking.client,
            salon = booking.salon,
            stripePaymentIntentId = paymentIntent.id,
            amount = amount,
            currency = "EUR",
            status = StripePaymentStatus.pending,
            description = "Paiement réservation ${booking.id}"
        )

        paymentRepository.save(payment)

        // 7. Retourner la réponse avec clientSecret
        return PaymentIntentResponse(
            paymentIntentId = paymentIntent.id,
            clientSecret = paymentIntent.clientSecret,
            amount = amount,
            currency = "EUR",
            status = paymentIntent.status
        )
    }

    // ========== CONFIRMATION DE PAIEMENT ==========

    /**
     * Confirme un paiement après que le client a complété le paiement côté frontend.
     *
     * @param paymentIntentId ID du PaymentIntent Stripe
     * @return Payment confirmé
     */
    @Transactional
    fun confirmPayment(paymentIntentId: String): PaymentResponse {
        // 1. Récupérer le PaymentIntent depuis Stripe
        val paymentIntent: PaymentIntent = try {
            PaymentIntent.retrieve(paymentIntentId)
        } catch (e: StripeException) {
            throw StripePaymentException("Erreur lors de la récupération du PaymentIntent: ${e.message}", e)
        }

        // 2. Récupérer le paiement en base
        val payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            ?: throw PaymentNotFoundException("Paiement non trouvé pour PaymentIntent: $paymentIntentId")

        // 3. Mettre à jour le statut selon le statut Stripe
        val newStatus = when (paymentIntent.status) {
            "succeeded" -> {
                payment.status = StripePaymentStatus.succeeded
                payment.paidAt = LocalDateTime.now()
                payment.stripeChargeId = paymentIntent.latestCharge
                
                // Mettre à jour le statut de paiement de la réservation
                payment.booking?.let { booking ->
                    booking.paymentStatus = com.frollot.model.PaymentStatus.paid
                    booking.paymentMethod = paymentIntent.paymentMethodTypes.firstOrNull()
                    bookingRepository.save(booking)
                }
                StripePaymentStatus.succeeded
            }
            "processing" -> {
                payment.status = StripePaymentStatus.processing
                StripePaymentStatus.processing
            }
            "requires_payment_method",
            "requires_confirmation",
            "requires_action",
            "requires_capture" -> {
                payment.status = StripePaymentStatus.pending
                StripePaymentStatus.pending
            }
            "canceled" -> {
                payment.status = StripePaymentStatus.canceled
                StripePaymentStatus.canceled
            }
            else -> {
                payment.status = StripePaymentStatus.failed
                payment.failureReason = paymentIntent.lastPaymentError?.message
                StripePaymentStatus.failed
            }
        }

        payment.paymentMethodType = paymentIntent.paymentMethodTypes.firstOrNull()
        paymentRepository.save(payment)

        return PaymentResponse.fromEntity(payment)
    }

    // ========== GESTION DES WEBHOOKS ==========

    /**
     * Traite un événement webhook Stripe.
     *
     * @param eventType Type d'événement Stripe
     * @param paymentIntentId ID du PaymentIntent
     * @param data Données de l'événement
     */
    @Transactional
    fun handleWebhookEvent(eventType: String, paymentIntentId: String, data: Map<String, Any>) {
        val payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            ?: throw PaymentNotFoundException("Paiement non trouvé pour PaymentIntent: $paymentIntentId")

        when (eventType) {
            "payment_intent.succeeded" -> {
                payment.status = StripePaymentStatus.succeeded
                payment.paidAt = LocalDateTime.now()
                payment.stripeChargeId = data["charge_id"] as? String
                
                payment.booking?.let { booking ->
                    booking.paymentStatus = com.frollot.model.PaymentStatus.paid
                    bookingRepository.save(booking)
                }
            }
            "payment_intent.payment_failed" -> {
                payment.status = StripePaymentStatus.failed
                payment.failureReason = (data["last_payment_error"] as? Map<*, *>)?.get("message") as? String
            }
            "payment_intent.canceled" -> {
                payment.status = StripePaymentStatus.canceled
            }
            "charge.refunded" -> {
                val refundAmount = (data["amount_refunded"] as? Number)?.toLong() ?: 0L
                val refundAmountDecimal = BigDecimal(refundAmount).divide(BigDecimal(100))
                
                if (refundAmountDecimal >= payment.amount) {
                    payment.status = StripePaymentStatus.refunded
                    payment.refundedAmount = payment.amount
                } else {
                    payment.status = StripePaymentStatus.partially_refunded
                    payment.refundedAmount = refundAmountDecimal
                }
                payment.refundedAt = LocalDateTime.now()
                
                payment.booking?.let { booking ->
                    booking.paymentStatus = com.frollot.model.PaymentStatus.refunded
                    bookingRepository.save(booking)
                }
            }
        }

        paymentRepository.save(payment)
    }

    // ========== REMBOURSEMENTS ==========

    /**
     * Effectue un remboursement total ou partiel.
     *
     * @param paymentId ID du paiement
     * @param amount Montant à rembourser (null = remboursement total)
     * @return Payment remboursé
     */
    @Transactional
    fun refundPayment(paymentId: String, amount: BigDecimal? = null): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException(paymentId) }

        if (!payment.canBeRefunded()) {
            throw InvalidPaymentException("Ce paiement ne peut pas être remboursé")
        }

        val refundAmount = amount ?: payment.getRemainingRefundableAmount()
        val refundAmountInCents = (refundAmount * BigDecimal(100)).toLong()

        if (refundAmountInCents <= 0) {
            throw InvalidPaymentException("Le montant de remboursement doit être supérieur à 0")
        }

        // Récupérer le charge ID
        val chargeId = payment.stripeChargeId
            ?: throw InvalidPaymentException("Charge ID manquant pour le remboursement")

        // Créer le remboursement Stripe
        val refundParams = RefundCreateParams.builder()
            .setCharge(chargeId)
            .setAmount(refundAmountInCents)
            .build()

        val refund: Refund = try {
            Refund.create(refundParams)
        } catch (e: StripeException) {
            throw StripePaymentException("Erreur lors du remboursement: ${e.message}", e)
        }

        // Mettre à jour le paiement
        val newRefundedAmount = payment.refundedAmount + refundAmount
        
        if (newRefundedAmount >= payment.amount) {
            payment.status = StripePaymentStatus.refunded
            payment.refundedAmount = payment.amount
        } else {
            payment.status = StripePaymentStatus.partially_refunded
            payment.refundedAmount = newRefundedAmount
        }
        payment.refundedAt = LocalDateTime.now()

        // Mettre à jour la réservation
        payment.booking?.let { booking ->
            if (payment.status == StripePaymentStatus.refunded) {
                booking.paymentStatus = com.frollot.model.PaymentStatus.refunded
            }
            bookingRepository.save(booking)
        }

        paymentRepository.save(payment)

        return PaymentResponse.fromEntity(payment)
    }

    // ========== STRIPE CHECKOUT SESSION ==========

    /**
     * Crée une Stripe Checkout Session pour une réservation.
     * 
     * C'est la méthode RECOMMANDÉE pour les paiements :
     * - 100% sécurisée (conforme PCI-DSS)
     * - Interface hébergée par Stripe
     * - Apple Pay, Google Pay, cartes automatiquement
     * - Multi-devises et international
     *
     * @param request Requête contenant l'ID de réservation et les URLs de retour
     * @return CheckoutSession créée avec l'URL de redirection
     */
    @Transactional
    fun createCheckoutSession(request: CheckoutSessionRequest): CheckoutSessionResponse {
        // 1. Vérifier que la réservation existe
        val booking = bookingRepository.findById(request.bookingId)
            .orElseThrow { BookingNotFoundException(request.bookingId) }

        // 2. Vérifier que la réservation n'a pas déjà un paiement réussi
        val existingPayment = paymentRepository.findByBookingIdOrderByCreatedAtDesc(request.bookingId)
            .firstOrNull { it.isSucceeded() }

        if (existingPayment != null) {
            throw InvalidPaymentException("Cette réservation a déjà été payée")
        }

        // 3. Calculer le montant (en centimes pour Stripe)
        val amount = booking.priceFinal ?: booking.service?.price ?: BigDecimal.ZERO
        val amountInCents = (amount * BigDecimal(100)).toLong()

        if (amountInCents <= 0) {
            throw InvalidPaymentException("Le montant doit être supérieur à 0")
        }

        // 4. Vérifier que Stripe est configuré
        if (!isStripeConfigured) {
            throw StripePaymentException(
                "Stripe n'est pas configuré. " +
                "Veuillez définir STRIPE_SECRET_KEY avec une clé API Stripe valide."
            )
        }

        // 5. Construire la description du produit
        val serviceName = booking.service?.name ?: "Service de coiffure"
        val salonName = booking.salon?.name ?: "Salon"

        // 6. Créer la Checkout Session Stripe
        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(request.successUrl + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(request.cancelUrl)
            .setCustomerEmail(booking.client?.email)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(amountInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(serviceName)
                                    .setDescription("Réservation chez $salonName")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putMetadata("booking_id", request.bookingId)
            .putMetadata("salon_id", booking.salon?.id ?: "")
            .putMetadata("client_id", booking.client?.id ?: "")
            .setPaymentIntentData(
                SessionCreateParams.PaymentIntentData.builder()
                    .putMetadata("booking_id", request.bookingId)
                    .putMetadata("salon_id", booking.salon?.id ?: "")
                    .putMetadata("client_id", booking.client?.id ?: "")
                    .build()
            )
            .build()

        val session: Session = try {
            Session.create(params)
        } catch (e: StripeException) {
            throw StripePaymentException("Erreur lors de la création de la session: ${e.message}", e)
        }

        // 7. Créer l'entité Payment en base (en attente)
        val payment = Payment(
            id = UUID.randomUUID().toString(),
            booking = booking,
            client = booking.client,
            salon = booking.salon,
            stripePaymentIntentId = session.paymentIntent, // Peut être null jusqu'au paiement
            amount = amount,
            currency = "EUR",
            status = StripePaymentStatus.pending,
            description = "Paiement réservation ${booking.id}",
            paymentMethod = "checkout_session",
            stripeSessionId = session.id
        )

        paymentRepository.save(payment)

        // 8. Retourner l'URL de redirection
        return CheckoutSessionResponse(
            sessionId = session.id,
            checkoutUrl = session.url,
            expiresAt = session.expiresAt
        )
    }

    /**
     * Récupère le statut d'une Checkout Session.
     *
     * @param sessionId ID de la session Stripe
     * @return Statut de la session
     */
    @Transactional(readOnly = true)
    fun getCheckoutSessionStatus(sessionId: String): PaymentSessionStatus {
        val session: Session = try {
            Session.retrieve(sessionId)
        } catch (e: StripeException) {
            throw StripePaymentException("Session non trouvée: ${e.message}", e)
        }

        return PaymentSessionStatus(
            sessionId = session.id,
            paymentStatus = session.paymentStatus ?: "unknown",
            bookingId = session.metadata?.get("booking_id") ?: "",
            amountTotal = session.amountTotal ?: 0L,
            currency = session.currency ?: "eur",
            customerEmail = session.customerEmail
        )
    }

    /**
     * Gère le webhook checkout.session.completed.
     * Appelé quand le client a terminé le paiement sur Stripe Checkout.
     *
     * @param sessionId ID de la session complétée
     */
    @Transactional
    fun handleCheckoutSessionCompleted(sessionId: String) {
        // 1. Récupérer la session depuis Stripe
        val session: Session = try {
            Session.retrieve(sessionId)
        } catch (e: StripeException) {
            throw StripePaymentException("Session non trouvée: ${e.message}", e)
        }

        // 2. Trouver le paiement associé
        val payment = paymentRepository.findByStripeSessionId(sessionId)
            ?: throw PaymentNotFoundException("Paiement non trouvé pour session: $sessionId")

        // 3. Mettre à jour le paiement
        if (session.paymentStatus == "paid") {
            payment.status = StripePaymentStatus.succeeded
            payment.paidAt = LocalDateTime.now()
            payment.stripePaymentIntentId = session.paymentIntent

            // 4. Mettre à jour la réservation
            payment.booking?.let { booking ->
                booking.paymentStatus = com.frollot.model.PaymentStatus.paid
                bookingRepository.save(booking)
            }

            paymentRepository.save(payment)
        }
    }

    // ========== LECTURE ==========

    /**
     * Récupère un paiement par son ID.
     */
    @Transactional(readOnly = true)
    fun getPaymentById(paymentId: String): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException(paymentId) }
        return PaymentResponse.fromEntity(payment)
    }

    /**
     * Récupère tous les paiements d'une réservation.
     */
    @Transactional(readOnly = true)
    fun getPaymentsByBooking(bookingId: String): List<PaymentResponse> {
        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
            .map { PaymentResponse.fromEntity(it) }
    }

    /**
     * Récupère tous les paiements d'un client.
     */
    @Transactional(readOnly = true)
    fun getPaymentsByClient(clientId: String): List<PaymentResponse> {
        return paymentRepository.findByClientIdOrderByCreatedAtDesc(clientId)
            .map { PaymentResponse.fromEntity(it) }
    }

    /**
     * Récupère tous les paiements d'un salon.
     */
    @Transactional(readOnly = true)
    fun getPaymentsBySalon(salonId: String): List<PaymentResponse> {
        return paymentRepository.findBySalonIdOrderByCreatedAtDesc(salonId)
            .map { PaymentResponse.fromEntity(it) }
    }
}

