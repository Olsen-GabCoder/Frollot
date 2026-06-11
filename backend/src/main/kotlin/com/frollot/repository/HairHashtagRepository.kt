package com.frollot.repository

import com.frollot.model.HairHashtag
import com.frollot.model.HairHashtagCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository pour la gestion des hashtags coiffure.
 */
@Repository
interface HairHashtagRepository : JpaRepository<HairHashtag, String> {

    /**
     * Trouve un hashtag par son nom (insensible à la casse).
     */
    fun findByNameIgnoreCase(name: String): HairHashtag?

    /**
     * Vérifie si un hashtag existe par son nom.
     */
    fun existsByNameIgnoreCase(name: String): Boolean

    /**
     * Récupère les hashtags par catégorie.
     */
    fun findByCategory(category: HairHashtagCategory): List<HairHashtag>

    /**
     * Récupère les hashtags les plus utilisés (trending).
     */
    @Query("SELECT h FROM HairHashtag h ORDER BY h.usageCount DESC")
    fun findTrendingHashtags(): List<HairHashtag>

    /**
     * Recherche des hashtags par nom (insensible à la casse).
     */
    @Query("SELECT h FROM HairHashtag h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY h.usageCount DESC")
    fun searchByName(@Param("query") query: String): List<HairHashtag>

    /**
     * Suggère des hashtags basés sur un préfixe.
     */
    @Query("SELECT h FROM HairHashtag h WHERE LOWER(h.name) LIKE LOWER(CONCAT(:prefix, '%')) ORDER BY h.usageCount DESC")
    fun suggestByPrefix(@Param("prefix") prefix: String): List<HairHashtag>
}

