package com.frollot.repository

import com.frollot.model.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

/**
 * Repository pour la gestion des avis (Reviews).
 *
 * Fournit des méthodes de recherche et de calcul de statistiques.
 */
@Repository
interface ReviewRepository : JpaRepository<Review, String> {

    /**
     * Trouve tous les avis d'un salon, triés par date décroissante (plus récents en premier).
     */
    fun findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc(
        salonId: String,
        pageable: Pageable
    ): Page<Review>

    /**
     * Trouve tous les avis d'un salon (sans pagination).
     */
    fun findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc(salonId: String): List<Review>

    /**
     * Trouve tous les avis d'un client.
     */
    fun findByClientIdOrderByCreatedAtDesc(clientId: String): List<Review>

    /**
     * Trouve tous les avis d'un coiffeur.
     */
    fun findByStaffIdOrderByCreatedAtDesc(staffId: String): List<Review>

    /**
     * Vérifie si un avis existe déjà pour une réservation donnée.
     */
    fun existsByBookingId(bookingId: String): Boolean

    /**
     * Trouve l'avis associé à une réservation.
     */
    fun findByBookingId(bookingId: String): Review?

    /**
     * Calcule la note moyenne d'un salon.
     *
     * @param salonId ID du salon
     * @return La note moyenne (0.00 si aucun avis)
     */
    @Query(
        """
        SELECT COALESCE(AVG(r.rating), 0.0)
        FROM Review r
        WHERE r.salon.id = :salonId
        AND r.isVisible = true
        """
    )
    fun findAverageRatingBySalonId(@Param("salonId") salonId: String): BigDecimal

    /**
     * Compte le nombre total d'avis visibles d'un salon.
     *
     * @param salonId ID du salon
     * @return Le nombre d'avis
     */
    fun countBySalonIdAndIsVisibleTrue(salonId: String): Long

    /**
     * Trouve tous les avis d'un salon avec leurs relations chargées.
     */
    @Query(
        """
        SELECT r FROM Review r
        LEFT JOIN FETCH r.client
        LEFT JOIN FETCH r.staff
        WHERE r.salon.id = :salonId
        AND r.isVisible = true
        ORDER BY r.createdAt DESC
        """
    )
    fun findBySalonIdWithRelations(@Param("salonId") salonId: String): List<Review>
}

