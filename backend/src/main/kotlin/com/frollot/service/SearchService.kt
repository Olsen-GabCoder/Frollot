package com.frollot.service

import com.frollot.dto.*
import com.frollot.model.ServiceCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service de recherche unifié pour la Phase C.1 - Recherche spécialisée coiffure.
 * 
 * Centralise toutes les recherches (posts, salons, utilisateurs, hashtags)
 * et délègue aux services existants pour éviter la duplication de code.
 */
@Service
@Transactional(readOnly = true)
class SearchService(
    private val socialService: SocialService,
    private val salonService: SalonService,
    private val userRepository: com.frollot.repository.UserRepository
) {

    /**
     * Recherche des posts avec filtres avancés.
     * Délègue à SocialService.searchPosts.
     */
    fun searchPosts(query: String?, filters: SearchFilters, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        return socialService.searchPosts(query, filters, pageable, currentUserId)
    }

    /**
     * Recherche des salons.
     * Délègue à SalonService.searchSalons.
     */
    fun searchSalons(query: String?, city: String?, category: ServiceCategory?): List<SalonResponse> {
        return salonService.searchSalons(query, city, category)
    }

    /**
     * Recherche des utilisateurs.
     * Délègue directement au repository pour éviter une couche supplémentaire.
     */
    fun searchUsers(query: String): List<UserResponse> {
        if (query.length < 2) {
            return emptyList()
        }
        val users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            firstName = query,
            lastName = query,
            email = query
        ).take(10) // Limiter à 10 résultats
        return users.map { UserResponse.fromEntity(it) }
    }

    /**
     * Recherche des hashtags.
     * Délègue à SocialService.searchHashtags.
     */
    fun searchHashtags(query: String): List<HairHashtagResponse> {
        return socialService.searchHashtags(query)
    }

    /**
     * Recherche unifiée - recherche dans tous les types.
     * Phase C.1 - Recherche spécialisée coiffure
     * 
     * @param query Terme de recherche
     * @param type Type de recherche (posts, salons, users, hashtags, all)
     * @param filters Filtres pour les posts (ignorés pour les autres types)
     * @param pageable Pagination (uniquement pour les posts)
     * @param currentUserId ID de l'utilisateur courant (pour les posts)
     * @return Résultats de recherche selon le type
     */
    fun unifiedSearch(
        query: String?,
        type: SearchType,
        filters: SearchFilters = SearchFilters(),
        pageable: Pageable,
        currentUserId: String? = null
    ): SearchResponse {
        val normalizedQuery = query?.takeIf { it.isNotBlank() }?.trim()

        return when (type) {
            SearchType.POSTS -> {
                val posts = searchPosts(normalizedQuery, filters, pageable, currentUserId)
                SearchResponse(
                    posts = posts.content,
                    salons = emptyList(),
                    users = emptyList(),
                    hashtags = emptyList(),
                    totalPosts = posts.totalElements.toInt(),
                    totalSalons = 0,
                    totalUsers = 0,
                    totalHashtags = 0
                )
            }
            SearchType.SALONS -> {
                val category = filters.serviceId?.let { 
                    // Si un serviceId est fourni, on pourrait récupérer sa catégorie
                    // Pour l'instant, on ignore ce filtre pour les salons
                    null
                }
                val salons = searchSalons(normalizedQuery, null, category)
                SearchResponse(
                    posts = emptyList(),
                    salons = salons,
                    users = emptyList(),
                    hashtags = emptyList(),
                    totalPosts = 0,
                    totalSalons = salons.size,
                    totalUsers = 0,
                    totalHashtags = 0
                )
            }
            SearchType.USERS -> {
                val users = if (normalizedQuery != null) {
                    searchUsers(normalizedQuery)
                } else {
                    emptyList()
                }
                SearchResponse(
                    posts = emptyList(),
                    salons = emptyList(),
                    users = users,
                    hashtags = emptyList(),
                    totalPosts = 0,
                    totalSalons = 0,
                    totalUsers = users.size,
                    totalHashtags = 0
                )
            }
            SearchType.HASHTAGS -> {
                val hashtags = if (normalizedQuery != null) {
                    searchHashtags(normalizedQuery)
                } else {
                    emptyList()
                }
                SearchResponse(
                    posts = emptyList(),
                    salons = emptyList(),
                    users = emptyList(),
                    hashtags = hashtags,
                    totalPosts = 0,
                    totalSalons = 0,
                    totalUsers = 0,
                    totalHashtags = hashtags.size
                )
            }
            SearchType.ALL -> {
                // Recherche dans tous les types
                val posts = if (normalizedQuery != null || filters.hasAnyFilter()) {
                    searchPosts(normalizedQuery, filters, Pageable.unpaged(), currentUserId).content.take(5)
                } else {
                    emptyList()
                }
                val salons = if (normalizedQuery != null) {
                    searchSalons(normalizedQuery, null, null).take(5)
                } else {
                    emptyList()
                }
                val users = if (normalizedQuery != null) {
                    searchUsers(normalizedQuery).take(5)
                } else {
                    emptyList()
                }
                val hashtags = if (normalizedQuery != null) {
                    searchHashtags(normalizedQuery).take(5)
                } else {
                    emptyList()
                }
                SearchResponse(
                    posts = posts,
                    salons = salons,
                    users = users,
                    hashtags = hashtags,
                    totalPosts = posts.size,
                    totalSalons = salons.size,
                    totalUsers = users.size,
                    totalHashtags = hashtags.size
                )
            }
        }
    }
}

/**
 * Type de recherche pour la recherche unifiée.
 */
enum class SearchType {
    POSTS,
    SALONS,
    USERS,
    HASHTAGS,
    ALL
}

