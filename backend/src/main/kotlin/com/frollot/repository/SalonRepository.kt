
package com.frollot.repository

import com.frollot.model.Salon
import com.frollot.model.ServiceCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SalonRepository : JpaRepository<Salon, String> {
    // Méthode custom pour récupérer tous les salons d'un propriétaire
    fun findByOwnerId(ownerId: String): List<Salon>

    /**
     * Recherche avancée de salons avec filtres optionnels.
     *
     * Règles :
     * - query : recherche partielle (case-insensitive) sur name OU description
     * - city : recherche partielle (case-insensitive) sur city OU address
     * - category : filtre par catégorie de service via une jointure sur SalonService
     *
     * Tous les paramètres sont optionnels ; si tous sont nuls, la méthode est équivalente à findAll().
     */
    @Query(
        """
        SELECT DISTINCT s
        FROM Salon s
        LEFT JOIN SalonService ss ON ss.salon = s
        WHERE
            (:query IS NULL OR 
             LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR 
             LOWER(COALESCE(s.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))
        AND
            (:city IS NULL OR 
             LOWER(s.city) LIKE LOWER(CONCAT('%', :city, '%')) OR 
             LOWER(s.address) LIKE LOWER(CONCAT('%', :city, '%')))
        AND
            (:category IS NULL OR ss.category = :category)
        """
    )
    fun search(
        @Param("query") query: String?,
        @Param("city") city: String?,
        @Param("category") category: ServiceCategory?
    ): List<Salon>

    /**
     * Récupère les salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     * 
     * Utilise la formule de Haversine pour calculer la distance en kilomètres.
     * 
     * @param latitude Latitude du point central
     * @param longitude Longitude du point central
     * @param radiusKm Rayon de recherche en kilomètres
     * @return Liste des salons triés par distance croissante
     */
    @Query(
        value = """
        SELECT s.*, (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(s.latitude)) *
                cos(radians(s.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(s.latitude))
            )
        ) AS distance
        FROM salons s
        WHERE s.latitude IS NOT NULL 
          AND s.longitude IS NOT NULL
        HAVING distance <= :radius
        ORDER BY distance ASC
        """,
        nativeQuery = true
    )
    fun findSalonsNearby(
        @Param("lat") latitude: Double,
        @Param("lng") longitude: Double,
        @Param("radius") radiusKm: Double
    ): List<Salon>
}
