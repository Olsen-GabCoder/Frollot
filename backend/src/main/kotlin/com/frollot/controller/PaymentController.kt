package com.frollot.controller

import com.frollot.dto.*
import com.frollot.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Event
import com.stripe.net.Webhook

@RestController
@RequestMapping("/api/payments")
@Tag(
    name = "Paiements",
    description = "API de gestion des paiements Stripe"
)
class PaymentController(
    private val paymentService: PaymentService,
    @Value("\${stripe.webhook-secret:}")
    private val stripeWebhookSecret: String
) {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    /**
     * Crée un PaymentIntent Stripe pour une réservation.
     * 
     * @deprecated Utilisez /create-checkout-session pour une intégration sécurisée.
     */
    @Operation(
        summary = "Créer un PaymentIntent (legacy)",
        description = "Crée un PaymentIntent Stripe pour une réservation. Retourne le clientSecret pour le frontend. DÉPRÉCIÉ: Utilisez /create-checkout-session pour une intégration sécurisée."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "PaymentIntent créé"),
            ApiResponse(responseCode = "400", description = "Données invalides"),
            ApiResponse(responseCode = "404", description = "Réservation non trouvée")
        ]
    )
    @PostMapping("/create-intent")
    @PreAuthorize("isAuthenticated()")
    fun createPaymentIntent(
        @Parameter(description = "ID de la réservation", required = true)
        @RequestParam bookingId: String
    ): ResponseEntity<PaymentIntentResponse> {
        val response = paymentService.createPaymentIntent(bookingId)
        return ResponseEntity.ok(response)
    }

    // ========== STRIPE CHECKOUT (RECOMMANDÉ) ==========

    /**
     * Crée une Stripe Checkout Session pour un paiement sécurisé.
     * 
     * C'est la méthode RECOMMANDÉE pour les paiements :
     * - Interface hébergée par Stripe (100% sécurisée)
     * - Apple Pay, Google Pay automatiquement
     * - Multi-devises et international
     */
    @Operation(
        summary = "Créer une Checkout Session",
        description = "Crée une Stripe Checkout Session. Retourne l'URL de redirection vers la page de paiement Stripe."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Checkout Session créée"),
            ApiResponse(responseCode = "400", description = "Données invalides"),
            ApiResponse(responseCode = "404", description = "Réservation non trouvée")
        ]
    )
    @PostMapping("/create-checkout-session")
    @PreAuthorize("isAuthenticated()")
    fun createCheckoutSession(
        @RequestBody request: CheckoutSessionRequest
    ): ResponseEntity<CheckoutSessionResponse> {
        val response = paymentService.createCheckoutSession(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Récupère le statut d'une Checkout Session.
     */
    @Operation(
        summary = "Statut d'une Checkout Session",
        description = "Récupère le statut d'une session de paiement après retour du client."
    )
    @GetMapping("/checkout-session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    fun getCheckoutSessionStatus(
        @PathVariable sessionId: String
    ): ResponseEntity<PaymentSessionStatus> {
        val status = paymentService.getCheckoutSessionStatus(sessionId)
        return ResponseEntity.ok(status)
    }

    /**
     * Confirme un paiement après complétion côté frontend.
     */
    @Operation(
        summary = "Confirmer un paiement",
        description = "Confirme un paiement après que le client a complété le paiement côté frontend."
    )
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    fun confirmPayment(
        @Parameter(description = "ID du PaymentIntent Stripe", required = true)
        @RequestParam paymentIntentId: String
    ): ResponseEntity<PaymentResponse> {
        val response = paymentService.confirmPayment(paymentIntentId)
        return ResponseEntity.ok(response)
    }

    /**
     * Webhook Stripe pour recevoir les événements de paiement.
     *
     * SÉCURITÉ : Ce endpoint est accessible publiquement mais protégé par :
     * - Vérification de la signature Stripe (Stripe-Signature header)
     * - Validation de l'événement avec le webhook secret
     * 
     * Documentation : https://stripe.com/docs/webhooks/signatures
     */
    @Operation(
        summary = "Webhook Stripe",
        description = "Endpoint webhook pour recevoir les événements Stripe (payment_intent.succeeded, etc.)"
    )
    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") sigHeader: String?
    ): ResponseEntity<Map<String, String>> {
        // Vérifier que la signature est présente
        if (sigHeader.isNullOrBlank()) {
            logger.warn("🚨 Webhook Stripe reçu sans signature")
            return ResponseEntity.badRequest().body(mapOf("error" to "Signature manquante"))
        }
        
        // Vérifier que le webhook secret est configuré
        if (stripeWebhookSecret.isBlank()) {
            logger.warn("⚠️ Webhook secret non configuré - traitement sans vérification de signature")
            // En mode développement, on peut accepter sans signature
            // En production, cela devrait être une erreur
        }
        
        // Vérifier la signature et parser l'événement
        val event: Event = try {
            if (stripeWebhookSecret.isNotBlank()) {
                Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret)
            } else {
                // Mode développement : parser sans vérification
                Event.GSON.fromJson(payload, Event::class.java)
            }
        } catch (e: SignatureVerificationException) {
            logger.error("🚨 Signature webhook Stripe invalide: ${e.message}")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Signature invalide"))
        } catch (e: Exception) {
            logger.error("❌ Erreur parsing webhook: ${e.message}")
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "Payload invalide"))
        }
        
        logger.info("📥 Webhook Stripe reçu: ${event.type}")
        
        // Extraire les données de l'événement
        val dataObject = event.dataObjectDeserializer.`object`
        if (dataObject.isEmpty) {
            logger.warn("⚠️ Objet de données vide dans l'événement webhook")
            return ResponseEntity.ok(mapOf("status" to "ignored", "reason" to "no data object"))
        }
        
        val stripeObject = dataObject.get()
        
        // Traiter selon le type d'événement
        try {
            when {
                // ===== CHECKOUT SESSION EVENTS (RECOMMANDÉ) =====
                stripeObject is com.stripe.model.checkout.Session -> {
                    val session = stripeObject
                    when (event.type) {
                        "checkout.session.completed" -> {
                            paymentService.handleCheckoutSessionCompleted(session.id)
                            logger.info("✅ Checkout Session ${session.id} complétée")
                        }
                        "checkout.session.expired" -> {
                            logger.info("⚠️ Checkout Session ${session.id} expirée")
                        }
                        else -> {
                            logger.info("ℹ️ Checkout Session event: ${event.type}")
                        }
                    }
                }
                
                // ===== PAYMENT INTENT EVENTS =====
                stripeObject is com.stripe.model.PaymentIntent -> {
                    val paymentIntent = stripeObject
                    paymentService.handleWebhookEvent(
                        eventType = event.type,
                        paymentIntentId = paymentIntent.id,
                        data = mapOf(
                            "status" to (paymentIntent.status ?: ""),
                            "amount" to (paymentIntent.amount ?: 0L),
                            "charge_id" to (paymentIntent.latestCharge ?: ""),
                            "last_payment_error" to (paymentIntent.lastPaymentError?.let { 
                                mapOf("message" to (it.message ?: "")) 
                            } ?: emptyMap<String, String>())
                        )
                    )
                    logger.info("✅ PaymentIntent ${paymentIntent.id} traité: ${event.type}")
                }
                
                // ===== CHARGE EVENTS =====
                stripeObject is com.stripe.model.Charge -> {
                    val charge = stripeObject
                    val paymentIntentId = charge.paymentIntent
                    if (paymentIntentId != null) {
                        paymentService.handleWebhookEvent(
                            eventType = event.type,
                            paymentIntentId = paymentIntentId,
                            data = mapOf(
                                "charge_id" to charge.id,
                                "amount_refunded" to (charge.amountRefunded ?: 0L),
                                "refunded" to charge.refunded
                            )
                        )
                        logger.info("✅ Charge ${charge.id} traité: ${event.type}")
                    }
                }
                else -> {
                    logger.info("ℹ️ Type d'événement non géré: ${event.type}")
                }
            }
            
            return ResponseEntity.ok(mapOf("status" to "success"))
            
        } catch (e: Exception) {
            logger.error("❌ Erreur traitement webhook: ${e.message}", e)
            // On retourne 200 même en cas d'erreur pour éviter les retries Stripe
            // Les erreurs doivent être gérées en interne (alertes, logs, etc.)
            return ResponseEntity.ok(mapOf(
                "status" to "error_logged",
                "message" to "Erreur traitée en interne"
            ))
        }
    }

    /**
     * Effectue un remboursement (total ou partiel).
     */
    @Operation(
        summary = "Rembourser un paiement",
        description = "Effectue un remboursement total ou partiel d'un paiement."
    )
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('SALON_OWNER') or hasRole('ADMIN')")
    fun refundPayment(
        @Parameter(description = "ID du paiement", required = true)
        @PathVariable paymentId: String,
        @RequestBody request: RefundRequest
    ): ResponseEntity<PaymentResponse> {
        val response = paymentService.refundPayment(paymentId, request.amount)
        return ResponseEntity.ok(response)
    }

    /**
     * Récupère un paiement par son ID.
     */
    @Operation(summary = "Récupérer un paiement", description = "Récupère les détails d'un paiement")
    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    fun getPayment(
        @PathVariable paymentId: String
    ): ResponseEntity<PaymentResponse> {
        val payment = paymentService.getPaymentById(paymentId)
        return ResponseEntity.ok(payment)
    }

    /**
     * Récupère tous les paiements d'une réservation.
     */
    @Operation(summary = "Paiements d'une réservation", description = "Récupère tous les paiements d'une réservation")
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    fun getPaymentsByBooking(
        @PathVariable bookingId: String
    ): ResponseEntity<List<PaymentResponse>> {
        val payments = paymentService.getPaymentsByBooking(bookingId)
        return ResponseEntity.ok(payments)
    }

    /**
     * Récupère tous les paiements d'un client.
     */
    @Operation(summary = "Paiements d'un client", description = "Récupère tous les paiements d'un client")
    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    fun getPaymentsByClient(
        @PathVariable clientId: String
    ): ResponseEntity<List<PaymentResponse>> {
        val payments = paymentService.getPaymentsByClient(clientId)
        return ResponseEntity.ok(payments)
    }

    /**
     * Récupère tous les paiements d'un salon.
     */
    @Operation(summary = "Paiements d'un salon", description = "Récupère tous les paiements d'un salon")
    @GetMapping("/salon/{salonId}")
    @PreAuthorize("isAuthenticated()")
    fun getPaymentsBySalon(
        @PathVariable salonId: String
    ): ResponseEntity<List<PaymentResponse>> {
        val payments = paymentService.getPaymentsBySalon(salonId)
        return ResponseEntity.ok(payments)
    }
}