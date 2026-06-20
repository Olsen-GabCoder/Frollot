package com.frollot.service

import com.frollot.dto.CreateReviewRequest
import com.frollot.dto.CreateSalonReviewRequest
import com.frollot.dto.ReplyToReviewRequest
import com.frollot.dto.ReviewResponse
import com.frollot.dto.SalonReviewStats
import com.frollot.model.BookingStatus
import com.frollot.model.Review
import com.frollot.repository.BookingRepository
import com.frollot.repository.ReviewRepository
import com.frollot.repository.SalonRepository
import com.frollot.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.frollot.model.Review as ReviewEntity
import com.frollot.service.SalonAuthorizationService
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

/**
 * Service de gestion des avis (Reviews).
 *
 * Gère :
 * - Création d'avis (uniquement pour les réservations terminées)
 * - Validation qu'un client ne peut pas laisser plusieurs avis pour la même réservation
 * - Mise à jour automatique de la note moyenne et du nombre d'avis du salon (dénormalisation)
 * - Récupération des avis d'un salon avec pagination
 */
@Service
@Transactional
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val bookingRepository: BookingRepository,
    private val salonRepository: SalonRepository,
    private val userRepository: UserRepository,
    private val salonAuthorizationService: SalonAuthorizationService
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class ReviewNotFoundException(reviewId: String) :
        RuntimeException("Avis avec ID '$reviewId' non trouvé")

    class BookingNotFoundException(bookingId: String) :
        RuntimeException("Réservation avec ID '$bookingId' non trouvée")

    class SalonNotFoundException(salonId: String) :
        RuntimeException("Salon avec ID '$salonId' non trouvé")

    class UserNotFoundException(userId: String) :
        RuntimeException("Utilisateur avec ID '$userId' non trouvé")

    class InvalidReviewException(message: String) :
        RuntimeException(message)

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé")

    // ========== CRÉATION D'AVIS ==========

    /**
     * Crée un nouvel avis pour une réservation terminée.
     *
     * Contraintes :
     * - La réservation doit avoir le statut "completed"
     * - Le client doit être le propriétaire de la réservation
     * - Un seul avis par réservation est autorisé
     * - La note doit être entre 1 et 5
     *
     * Après création, met à jour automatiquement la note moyenne et le nombre d'avis du salon.
     */
    @Transactional
    fun createReview(request: CreateReviewRequest, clientId: String): ReviewResponse {
        // 1. Validation de la requête
        request.validate()

        // 2. Vérifier que la réservation existe
        val booking = bookingRepository.findById(request.bookingId)
            .orElseThrow { BookingNotFoundException(request.bookingId) }

        // 3. Vérifier que la réservation est terminée
        if (booking.status != BookingStatus.completed) {
            throw InvalidReviewException(
                "Impossible de laisser un avis : la réservation doit être terminée (statut actuel: ${booking.status})"
            )
        }

        // 4. Vérifier que le client est bien le propriétaire de la réservation
        if (booking.client?.id != clientId) {
            throw UnauthorizedAccessException(
                "Vous ne pouvez pas laisser un avis pour une réservation qui ne vous appartient pas"
            )
        }

        // 5. Vérifier qu'un avis n'existe pas déjà pour cette réservation
        if (reviewRepository.existsByBookingId(request.bookingId)) {
            throw InvalidReviewException(
                "Un avis existe déjà pour cette réservation"
            )
        }

        // 6. Vérifier que le salon existe
        val salon = salonRepository.findById(request.salonId)
            .orElseThrow { SalonNotFoundException(request.salonId) }

        // 7. Vérifier que la réservation appartient bien au salon
        if (booking.salon?.id != request.salonId) {
            throw InvalidReviewException(
                "La réservation n'appartient pas au salon spécifié"
            )
        }

        // 8. Vérifier que le client existe
        val client = userRepository.findById(clientId)
            .orElseThrow { UserNotFoundException(clientId) }

        // 9. Créer l'avis
        val review = Review(
            id = UUID.randomUUID().toString(),
            salon = salon,
            staff = booking.staff,
            client = client,
            booking = booking,
            rating = request.rating,
            title = request.title,
            content = request.content,
            isVerified = true, // Vérifié car basé sur une réservation réelle
            isVisible = true
        )

        // 10. Sauvegarder l'avis
        val savedReview = reviewRepository.save(review)

        // 11. Mettre à jour les statistiques du salon (dénormalisation)
        updateSalonReviewStats(salon.id!!)

        // 12. Retourner la réponse
        return toResponseWithByName(savedReview)
    }

    // ========== CREATION D'AVIS-SALON (sans reservation) ==========

    /**
     * Cree un avis-salon libre (sans reservation).
     * Anti-abus : 1 seul avis-salon par user par salon.
     */
    @Transactional
    fun createSalonReview(request: CreateSalonReviewRequest, clientId: String): ReviewResponse {
        request.validate()

        val salon = salonRepository.findById(request.salonId)
            .orElseThrow { SalonNotFoundException(request.salonId) }

        // Anti-abus : un seul avis-salon par user par salon
        if (reviewRepository.existsBySalonIdAndClientIdAndBookingIsNull(request.salonId, clientId)) {
            throw InvalidReviewException("Vous avez deja laisse un avis sur ce salon")
        }

        val client = userRepository.findById(clientId)
            .orElseThrow { UserNotFoundException(clientId) }

        // Interdire a l'owner de noter son propre salon
        if (salon.owner?.id == clientId) {
            throw InvalidReviewException("Vous ne pouvez pas noter votre propre salon")
        }

        val review = Review(
            id = UUID.randomUUID().toString(),
            salon = salon,
            staff = null,
            client = client,
            booking = null,
            rating = request.rating,
            title = request.title?.trim(),
            content = request.content?.trim(),
            isVerified = false,
            isVisible = true
        )

        val savedReview = reviewRepository.save(review)
        updateSalonReviewStats(salon.id!!)
        return toResponseWithByName(savedReview)
    }

    // ========== LECTURE D'AVIS ==========

    /**
     * Récupère tous les avis d'un salon avec pagination.
     */
    @Transactional(readOnly = true)
    fun getSalonReviews(salonId: String, pageable: Pageable): Page<ReviewResponse> {
        // Vérifier que le salon existe
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        return reviewRepository
            .findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc(salonId, pageable)
            .map { toResponseWithByName(it) }
    }

    /**
     * Récupère tous les avis d'un salon (sans pagination).
     */
    @Transactional(readOnly = true)
    fun getSalonReviews(salonId: String): List<ReviewResponse> {
        // Vérifier que le salon existe
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        return reviewRepository
            .findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc(salonId)
            .map { toResponseWithByName(it) }
    }

    /**
     * Récupère un avis par son ID.
     */
    @Transactional(readOnly = true)
    fun getReviewById(reviewId: String): ReviewResponse {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ReviewNotFoundException(reviewId) }

        return toResponseWithByName(review)
    }

    /**
     * Récupère tous les avis d'un client.
     */
    @Transactional(readOnly = true)
    fun getClientReviews(clientId: String): List<ReviewResponse> {
        return reviewRepository
            .findByClientIdOrderByCreatedAtDesc(clientId)
            .map { toResponseWithByName(it) }
    }

    /**
     * Vérifie si un avis existe pour une réservation donnée.
     */
    @Transactional(readOnly = true)
    fun hasReviewForBooking(bookingId: String): Boolean {
        return reviewRepository.existsByBookingId(bookingId)
    }

    // ========== RÉPONSE SALON ==========

    /**
     * Répondre à un avis (owner/manager du salon).
     */
    fun replyToReview(reviewId: String, salonId: String, userId: String, request: ReplyToReviewRequest): ReviewResponse {
        request.validate()

        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ReviewNotFoundException(reviewId) }

        // Vérifier que l'avis appartient au salon
        if (review.salon?.id != salonId) {
            throw InvalidReviewException("Cet avis n'appartient pas au salon '$salonId'")
        }

        // Vérifier la permission review.reply
        salonAuthorizationService.requirePermission(userId, salonId, "review.reply")

        val responder = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        review.responseSalon = request.reply.trim()
        review.responseAt = LocalDateTime.now()
        review.responseBy = responder

        val saved = reviewRepository.save(review)
        return toResponseWithByName(saved)
    }

    /** Résout le nom de l'auteur de la réponse pour l'affichage « par X ». */
    private fun toResponseWithByName(review: ReviewEntity): ReviewResponse {
        val byName = review.responseBy?.let { user ->
            "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifBlank { user.email }
        }
        return ReviewResponse.fromEntity(review, responseByName = byName)
    }

    // ========== STATISTIQUES ==========

    /**
     * Récupère les statistiques d'avis d'un salon.
     */
    @Transactional(readOnly = true)
    fun getSalonReviewStats(salonId: String): SalonReviewStats {
        val salon = salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }

        val averageRating = reviewRepository.findAverageRatingBySalonId(salonId)
        val totalReviews = reviewRepository.countBySalonIdAndIsVisibleTrue(salonId).toInt()

        // Calculer la distribution des notes
        val reviews = reviewRepository.findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc(salonId)
        val distribution = reviews
            .groupingBy { it.rating }
            .eachCount()
            .mapKeys { it.key }
            .mapValues { it.value.toLong() }

        // Compléter avec les notes manquantes (0)
        val fullDistribution = (1..5).associateWith { rating ->
            distribution[rating] ?: 0L
        }

        val verifiedAvg = reviewRepository.findVerifiedAverageRatingBySalonId(salonId)
        val verifiedCnt = reviewRepository.countVerifiedBySalonId(salonId).toInt()
        val generalAvg = reviewRepository.findGeneralAverageRatingBySalonId(salonId)
        val generalCnt = reviewRepository.countGeneralBySalonId(salonId).toInt()

        return SalonReviewStats.fromSalonAndDistribution(
            salonId = salonId,
            averageRating = averageRating,
            totalReviews = totalReviews,
            distribution = fullDistribution,
            verifiedAverage = verifiedAvg,
            verifiedCount = verifiedCnt,
            generalAverage = generalAvg,
            generalCount = generalCnt
        )
    }

    // ========== MISE À JOUR DES STATISTIQUES ==========

    /**
     * Met à jour la note moyenne et le nombre d'avis d'un salon.
     *
     * Cette méthode est appelée automatiquement après chaque création/suppression d'avis
     * pour maintenir la cohérence des données dénormalisées.
     */
    @Transactional
    fun updateSalonReviewStats(salonId: String) {
        val salon = salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }

        val averageRating = reviewRepository.findAverageRatingBySalonId(salonId)
        val totalReviews = reviewRepository.countBySalonIdAndIsVisibleTrue(salonId).toInt()

        salon.ratingAverage = averageRating.setScale(2, RoundingMode.HALF_UP)
        salon.totalReviews = totalReviews

        salonRepository.save(salon)

        println("✅ Statistiques d'avis mises à jour pour le salon $salonId : moyenne=${salon.ratingAverage}, total=$totalReviews")
    }
}

