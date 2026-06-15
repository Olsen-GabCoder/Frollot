package com.frollot.controller

import com.frollot.dto.*
import com.frollot.model.PostType
import com.frollot.service.SearchService
import com.frollot.service.SearchType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Controller pour la recherche unifiée.
 * Phase C.1 - Recherche spécialisée coiffure
 */
@RestController
@RequestMapping("/api/social/search")
@Tag(
    name = "Recherche",
    description = "API de recherche unifiée pour posts, salons, utilisateurs et hashtags"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class SearchController(
    private val searchService: SearchService
) {

    /**
     * Récupère l'ID de l'utilisateur authentifié depuis le SecurityContext.
     */
    private fun getAuthenticatedUserIdOrNull(): String? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null || !authentication.isAuthenticated) {
                null
            } else {
                val principal = authentication.principal
                when (principal) {
                    is com.frollot.model.User -> principal.id
                    else -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recherche unifiée dans tous les types de contenu.
     * Phase C.1 - Recherche spécialisée coiffure
     */
    @Operation(
        summary = "Recherche unifiée",
        description = "Recherche dans tous les types de contenu (posts, salons, utilisateurs, hashtags) ou un type spécifique"
    )
    @GetMapping
    fun unifiedSearch(
        @Parameter(description = "Terme de recherche", required = false)
        @RequestParam(required = false) q: String?,
        @Parameter(description = "Type de recherche (posts, salons, users, hashtags, all)", example = "all")
        @RequestParam(defaultValue = "ALL") type: SearchType,
        @Parameter(description = "Type de post (pour filtrer les posts)")
        @RequestParam(required = false) postType: PostType?,
        @Parameter(description = "ID du service associé (pour filtrer les posts)")
        @RequestParam(required = false) serviceId: String?,
        @Parameter(description = "ID du salon tagué (pour filtrer les posts)")
        @RequestParam(required = false) salonId: String?,
        @Parameter(description = "Nom du hashtag associé (pour filtrer les posts)")
        @RequestParam(required = false) hashtagName: String?,
        @Parameter(description = "ID de l'auteur (pour filtrer les posts)")
        @RequestParam(required = false) authorId: String?,
        @Parameter(description = "Numéro de page (0-indexed, uniquement pour posts)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page (uniquement pour posts)", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable: Pageable = PageRequest.of(page, size)
        val filters = SearchFilters(
            postType = postType,
            serviceId = serviceId,
            salonId = salonId,
            hashtagName = hashtagName,
            authorId = authorId
        )
        val results = searchService.unifiedSearch(q, type, filters, pageable, currentUserId)
        return ResponseEntity.ok(results)
    }
}

