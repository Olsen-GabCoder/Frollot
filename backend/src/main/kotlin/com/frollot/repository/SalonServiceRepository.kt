package com.frollot.repository

import com.frollot.model.SalonService
import com.frollot.model.ServiceCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

/**
 * Repository Spring Data JPA pour l'entité SalonService.
 *
 * Fournit des méthodes d'accès aux données pour les prestations de services
 * avec des requêtes optimisées et paginées.
 *
 * IMPORTANT : Étend JpaRepository pour bénéficier des opérations CRUD standard
 * (save, findById, findAll, delete, etc.) sans implémentation manuelle.
 */
@Repository
interface SalonServiceRepository : JpaRepository<SalonService, String> {

    /**
     * Récupère toutes les prestations d'un salon spécifique.
     *
     * Cette méthode est essentielle pour afficher le catalogue complet
     * des services disponibles dans un salon donné.
     *
     * @param salonId Identifiant du salon
     * @return Liste des services du salon, triés par date de création (plus récent d'abord)
     */
    @Query("""
        SELECT s FROM SalonService s 
        WHERE s.salon.id = :salonId 
        ORDER BY s.createdAt DESC
    """)
    fun findBySalonId(@Param("salonId") salonId: String): List<SalonService>

    /**
     * Récupère les prestations d'un salon avec pagination.
     *
     * Optimisé pour les catalogues volumineux (> 20 services).
     *
     * @param salonId Identifiant du salon
     * @param pageable Configuration de pagination (page, taille, tri)
     * @return Page de services avec métadonnées de pagination
     */
    @Query("""
        SELECT s FROM SalonService s 
        WHERE s.salon.id = :salonId
    """)
    fun findBySalonId(
        @Param("salonId") salonId: String,
        pageable: Pageable
    ): Page<SalonService>

    /**
     * Récupère les prestations d'un salon par catégorie.
     *
     * Permet le filtrage côté backend pour une meilleure performance
     * dans les interfaces de filtrage par catégorie.
     *
     * @param salonId Identifiant du salon
     * @param category Catégorie de service
     * @return Liste des services du salon dans la catégorie spécifiée
     */
    fun findBySalonIdAndCategory(
        salonId: String,
        category: ServiceCategory
    ): List<SalonService>

    /**
     * Recherche des prestations par nom ou description.
     *
     * Recherche insensible à la casse avec LIKE pour les fonctionnalités
     * de recherche plein texte dans le catalogue.
     *
     * @param salonId Identifiant du salon
     * @param searchTerm Terme de recherche
     * @return Liste des services correspondant au terme de recherche
     */
    @Query("""
        SELECT s FROM SalonService s 
        WHERE s.salon.id = :salonId 
        AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
        OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY 
            CASE 
                WHEN LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) THEN 1
                ELSE 2
            END,
            s.name
    """)
    fun searchBySalonAndTerm(
        @Param("salonId") salonId: String,
        @Param("searchTerm") searchTerm: String
    ): List<SalonService>

    /**
     * Vérifie l'existence d'une prestation avec le même nom dans un salon.
     *
     * Utilisé pour éviter les doublons lors de la création/modification.
     *
     * @param salonId Identifiant du salon
     * @param name Nom du service
     * @return true si un service avec ce nom existe déjà dans le salon
     */
    fun existsBySalonIdAndNameIgnoreCase(
        salonId: String,
        name: String
    ): Boolean

    /**
     * Récupère les prestations par plage de prix.
     *
     * Supporte les fonctionnalités de filtrage par prix dans l'UI.
     *
     * @param salonId Identifiant du salon
     * @param minPrice Prix minimum (inclus)
     * @param maxPrice Prix maximum (inclus)
     * @return Liste des services dans la plage de prix
     */
    @Query("""
        SELECT s FROM SalonService s 
        WHERE s.salon.id = :salonId 
        AND s.price BETWEEN :minPrice AND :maxPrice
        ORDER BY s.price
    """)
    fun findBySalonIdAndPriceBetween(
        @Param("salonId") salonId: String,
        @Param("minPrice") minPrice: BigDecimal,
        @Param("maxPrice") maxPrice: BigDecimal
    ): List<SalonService>

    /**
     * Compte le nombre de prestations dans un salon.
     *
     * Utile pour les statistiques et l'affichage de métriques.
     *
     * @param salonId Identifiant du salon
     * @return Nombre total de services dans le salon
     */
    fun countBySalonId(salonId: String): Long

    /**
     * Récupère les prestations d'un salon triées par durée.
     *
     * @param salonId Identifiant du salon
     * @param ascending true pour croissant, false pour décroissant
     * @return Liste des services triés par durée
     */
    @Query("""
        SELECT s FROM SalonService s 
        WHERE s.salon.id = :salonId 
        ORDER BY 
            CASE WHEN :ascending = true THEN s.durationMinutes END ASC,
            CASE WHEN :ascending = false THEN s.durationMinutes END DESC
    """)
    fun findBySalonIdOrderByDuration(
        @Param("salonId") salonId: String,
        @Param("ascending") ascending: Boolean = true
    ): List<SalonService>

    /**
     * Supprime toutes les prestations d'un salon.
     *
     * IMPORTANT : À utiliser avec précaution (généralement lors de la suppression d'un salon).
     *
     * @param salonId Identifiant du salon
     * @return Nombre d'entités supprimées
     */
    fun deleteBySalonId(salonId: String): Long
}