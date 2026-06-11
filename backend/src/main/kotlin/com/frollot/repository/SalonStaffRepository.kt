package com.frollot.repository

import com.frollot.model.SalonStaff
import com.frollot.model.ServiceCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository Spring Data JPA pour l'entité SalonStaff.
 *
 * Fournit des méthodes d'accès aux données pour la gestion
 * des équipes des salons de coiffure.
 */
@Repository
interface SalonStaffRepository : JpaRepository<SalonStaff, String> {

    /**
     * Récupère tous les membres du staff d'un salon.
     *
     * @param salonId Identifiant du salon
     * @return Liste des membres du staff, triés par date de création
     */
    @Query("""
        SELECT s FROM SalonStaff s 
        WHERE s.salon.id = :salonId 
        ORDER BY s.createdAt DESC
    """)
    fun findBySalonId(@Param("salonId") salonId: String): List<SalonStaff>

    /**
     * Récupère les membres du staff actifs d'un salon.
     *
     * @param salonId Identifiant du salon
     * @return Liste des membres actifs
     */
    @Query("""
        SELECT s FROM SalonStaff s 
        WHERE s.salon.id = :salonId 
        AND s.isActive = true
        ORDER BY s.createdAt DESC
    """)
    fun findActiveBySalonId(@Param("salonId") salonId: String): List<SalonStaff>

    /**
     * Récupère les membres du staff d'un salon ayant une spécialité donnée.
     *
     * @param salonId Identifiant du salon
     * @param specialty Catégorie de service recherchée
     * @return Liste des membres ayant cette spécialité
     */
    @Query("""
        SELECT s FROM SalonStaff s 
        JOIN s.specialties sp
        WHERE s.salon.id = :salonId 
        AND sp = :specialty
        AND s.isActive = true
    """)
    fun findBySalonIdAndSpecialty(
        @Param("salonId") salonId: String,
        @Param("specialty") specialty: ServiceCategory
    ): List<SalonStaff>

    /**
     * Vérifie si un utilisateur fait déjà partie du staff d'un salon.
     *
     * @param salonId Identifiant du salon
     * @param userId Identifiant de l'utilisateur
     * @return true si l'utilisateur est déjà dans le staff
     */
    fun existsBySalonIdAndUserId(
        salonId: String,
        userId: String
    ): Boolean

    /**
     * Récupère tous les salons dans lesquels un utilisateur travaille.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Liste des staff où cet utilisateur est employé
     */
    @Query("""
        SELECT s FROM SalonStaff s 
        WHERE s.user.id = :userId
        ORDER BY s.createdAt DESC
    """)
    fun findByUserId(@Param("userId") userId: String): List<SalonStaff>

    /**
     * Compte le nombre de membres dans l'équipe d'un salon.
     *
     * @param salonId Identifiant du salon
     * @return Nombre total de membres
     */
    fun countBySalonId(salonId: String): Long

    /**
     * Compte le nombre de membres actifs dans l'équipe d'un salon.
     *
     * @param salonId Identifiant du salon
     * @return Nombre de membres actifs
     */
    fun countBySalonIdAndIsActive(salonId: String, isActive: Boolean): Long

    /**
     * Supprime tous les membres du staff d'un salon.
     * IMPORTANT : À utiliser avec précaution (généralement lors de la suppression d'un salon).
     *
     * @param salonId Identifiant du salon
     * @return Nombre d'entités supprimées
     */
    fun deleteBySalonId(salonId: String): Long

    /**
     * Récupère un membre du staff par salon et utilisateur.
     *
     * @param salonId Identifiant du salon
     * @param userId Identifiant de l'utilisateur
     * @return Le staff correspondant ou null
     */
    @Query("""
        SELECT s FROM SalonStaff s 
        WHERE s.salon.id = :salonId 
        AND s.user.id = :userId
    """)
    fun findBySalonIdAndUserId(
        @Param("salonId") salonId: String,
        @Param("userId") userId: String
    ): SalonStaff?
}