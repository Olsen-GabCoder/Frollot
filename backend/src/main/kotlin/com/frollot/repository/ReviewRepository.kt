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
     * Calcule la note moyenne des avis ciblant un ou plusieurs coiffeurs (par staffId).
     *
     * @param staffIds Liste des SalonStaff.id du coiffeur (multi-salon possible)
     * @return La note moyenne (0.00 si aucun avis)
     */
    @Query(
        """
        SELECT COALESCE(AVG(r.rating), 0.0)
        FROM Review r
        WHERE r.staff.id IN :staffIds
        AND r.isVisible = true
        """
    )
    fun findAverageRatingByStaffIds(@Param("staffIds") staffIds: List<String>): BigDecimal

    /**
     * Compte le nombre d'avis visibles ciblant un ou plusieurs coiffeurs (par staffId).
     *
     * @param staffIds Liste des SalonStaff.id du coiffeur (multi-salon possible)
     * @return Le nombre d'avis
     */
    @Query(
        """
        SELECT COUNT(r)
        FROM Review r
        WHERE r.staff.id IN :staffIds
        AND r.isVisible = true
        """
    )
    fun countByStaffIdsAndIsVisibleTrue(@Param("staffIds") staffIds: List<String>): Long

    /**
     * Verifie si un avis-salon (sans booking) existe deja pour un client sur un salon.
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.salon.id = :salonId AND r.client.id = :clientId AND r.booking IS NULL")
    fun existsBySalonIdAndClientIdAndBookingIsNull(@Param("salonId") salonId: String, @Param("clientId") clientId: String): Boolean

    /**
     * Moyenne des avis VERIFIES (booking IS NOT NULL) d'un salon.
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.salon.id = :salonId AND r.isVisible = true AND r.booking IS NOT NULL")
    fun findVerifiedAverageRatingBySalonId(@Param("salonId") salonId: String): BigDecimal

    /**
     * Nombre d'avis VERIFIES (booking IS NOT NULL) d'un salon.
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.salon.id = :salonId AND r.isVisible = true AND r.booking IS NOT NULL")
    fun countVerifiedBySalonId(@Param("salonId") salonId: String): Long

    /**
     * Moyenne des avis GENERAUX (booking IS NULL) d'un salon.
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.salon.id = :salonId AND r.isVisible = true AND r.booking IS NULL")
    fun findGeneralAverageRatingBySalonId(@Param("salonId") salonId: String): BigDecimal

    /**
     * Nombre d'avis GENERAUX (booking IS NULL) d'un salon.
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.salon.id = :salonId AND r.isVisible = true AND r.booking IS NULL")
    fun countGeneralBySalonId(@Param("salonId") salonId: String): Long

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

