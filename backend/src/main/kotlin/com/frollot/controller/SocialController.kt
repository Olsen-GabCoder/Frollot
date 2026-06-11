package com.frollot.controller

import com.frollot.dto.*
import com.frollot.model.HairHashtagCategory
import com.frollot.model.User
import com.frollot.service.SocialService
import com.frollot.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Contrôleur REST pour la gestion du réseau social.
 *
 * Expose une API complète pour :
 * - Créer et récupérer des posts
 * - Gérer les likes (toggle)
 * - Gérer les commentaires
 * - Obtenir le feed avec pagination
 */
@RestController
@RequestMapping("/api/social")
@Tag(
    name = "Réseau Social",
    description = "API de gestion du réseau social (posts, likes, commentaires)"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:9090", "http://10.0.2.2:9090"],
    allowCredentials = "true"
)
class SocialController(
    private val socialService: SocialService,
    private val userService: com.frollot.service.UserService
) {

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Récupère l'ID de l'utilisateur authentifié depuis le SecurityContext.
     */
    private fun getAuthenticatedUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifié")
        }

        val principal = authentication.principal
        return when (principal) {
            is User -> principal.id!!
            is String -> {
                // Si le principal est une String (par exemple "anonymousUser" ou un username)
                throw IllegalStateException("Authentification invalide: le principal est une chaîne")
            }
            else -> throw IllegalStateException("Type de principal non supporté: ${principal?.javaClass?.name}")
        }
    }

    /**
     * Récupère l'ID de l'utilisateur authentifié, ou null si non authentifié.
     */
    private fun getAuthenticatedUserIdOrNull(): String? {
        return try {
            getAuthenticatedUserId()
        } catch (e: Exception) {
            // Attraper toute exception (ClassCastException, IllegalStateException, etc.)
            null
        }
    }

    // ========== ENDPOINTS DE POSTS ==========

    /**
     * Crée un nouveau post.
     */
    @Operation(
        summary = "Créer un post",
        description = "Crée un nouveau post dans le réseau social"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Post créé avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PostResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            )
        ]
    )
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    fun createPost(
        @Parameter(description = "Données du post", required = true)
        @Valid @RequestBody request: CreatePostRequest
    ): ResponseEntity<PostResponse> {
        val authenticatedUserId = getAuthenticatedUserId()

        // Créer une nouvelle requête avec l'ID de l'utilisateur authentifié
        val authenticatedRequest = request.copy(authorId = authenticatedUserId)

        val post = socialService.createPost(authenticatedRequest)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/social/posts/${post.id}")
            .body(post)
    }

    /**
     * Récupère un post par son ID.
     */
    @Operation(
        summary = "Récupérer un post par ID",
        description = "Retourne les détails complets d'un post"
    )
    @GetMapping("/posts/{postId}")
    fun getPostById(
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val post = socialService.getPostById(postId, currentUserId)
        return ResponseEntity.ok(post)
    }

    /**
     * Récupère le feed (tous les posts) avec pagination.
     */
    @Operation(
        summary = "Récupérer le feed",
        description = "Retourne tous les posts triés par date décroissante avec pagination"
    )
    @GetMapping("/feed")
    fun getFeed(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable: Pageable = PageRequest.of(page, size)
        val feed = socialService.getFeed(pageable, currentUserId)
        return ResponseEntity.ok(feed)
    }

    /**
     * Recherche des posts par contenu (texte).
     * Phase C.1 - Recherche spécialisée coiffure
     */
    @Operation(
        summary = "Rechercher des posts par contenu",
        description = "Recherche des posts contenant le terme spécifié dans leur contenu (case-insensitive)"
    )
    @GetMapping("/posts/search")
    fun searchPostsByContent(
        @Parameter(description = "Terme de recherche", required = true)
        @RequestParam q: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.searchPostsByContent(q, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Recherche des posts avec filtres avancés.
     * Phase C.1 - Recherche spécialisée coiffure
     */
    @Operation(
        summary = "Rechercher des posts avec filtres",
        description = "Recherche des posts avec filtres avancés (type, service, salon, hashtag, auteur)"
    )
    @GetMapping("/posts/search/advanced")
    fun searchPostsWithFilters(
        @Parameter(description = "Terme de recherche dans le contenu (optionnel)")
        @RequestParam(required = false) q: String?,
        @Parameter(description = "Type de post")
        @RequestParam(required = false) postType: com.frollot.model.PostType?,
        @Parameter(description = "ID du service associé")
        @RequestParam(required = false) serviceId: String?,
        @Parameter(description = "ID du salon tagué")
        @RequestParam(required = false) salonId: String?,
        @Parameter(description = "Nom du hashtag associé")
        @RequestParam(required = false) hashtagName: String?,
        @Parameter(description = "ID de l'auteur")
        @RequestParam(required = false) authorId: String?,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val filters = com.frollot.dto.SearchFilters(
            postType = postType,
            serviceId = serviceId,
            salonId = salonId,
            hashtagName = hashtagName,
            authorId = authorId
        )
        val posts = socialService.searchPosts(q, filters, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Récupère les posts d'un utilisateur.
     */
    @Operation(
        summary = "Récupérer les posts d'un utilisateur",
        description = "Retourne tous les posts d'un utilisateur spécifique"
    )
    @GetMapping("/users/{userId}/posts")
    fun getPostsByUser(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable: Pageable = PageRequest.of(page, size)
        val posts = socialService.getPostsByUser(userId, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Récupère les posts d'un salon avec filtres et tri.
     * Phase C.2 - Feed par Salon
     */
    @Operation(
        summary = "Récupérer les posts d'un salon",
        description = "Retourne tous les posts tagués avec un salon, avec filtres par type de post et service, et tri par récence ou popularité"
    )
    @GetMapping("/salons/{salonId}/posts")
    fun getPostsBySalon(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String,
        @Parameter(description = "Filtre par type de post")
        @RequestParam(required = false) postType: com.frollot.model.PostType?,
        @Parameter(description = "Filtre par service associé")
        @RequestParam(required = false) serviceId: String?,
        @Parameter(description = "Tri par récence (RECENT) ou popularité (POPULAR)", example = "RECENT")
        @RequestParam(defaultValue = "RECENT") sortBy: com.frollot.model.SortBy,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.getPostsBySalon(salonId, postType, serviceId, sortBy, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Récupère les posts trending (les plus populaires) pour une période donnée.
     * Phase C.3 - Trending Coiffure
     */
    @Operation(
        summary = "Récupérer les posts trending",
        description = "Retourne les posts les plus populaires (triés par engagement) pour une période donnée (24h, 7j, 30j)"
    )
    @GetMapping("/posts/trending")
    fun getTrendingPosts(
        @Parameter(description = "Période de trending (LAST_24H, LAST_7D, LAST_30D)", example = "LAST_7D")
        @RequestParam(defaultValue = "LAST_7D") period: com.frollot.model.TrendPeriod,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.getTrendingPosts(period, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Récupère les posts des salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     */
    @Operation(
        summary = "Récupérer les posts à proximité",
        description = "Retourne les posts tagués avec des salons situés dans un rayon donné autour d'une position géographique"
    )
    @GetMapping("/posts/nearby")
    fun getPostsNearby(
        @Parameter(description = "Latitude du point central", required = true, example = "48.8566")
        @RequestParam lat: Double,
        @Parameter(description = "Longitude du point central", required = true, example = "2.3522")
        @RequestParam lng: Double,
        @Parameter(description = "Rayon de recherche en kilomètres", example = "10")
        @RequestParam(defaultValue = "10.0") radius: Double,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.getPostsNearby(lat, lng, radius, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Récupère le feed des entités suivies par un utilisateur.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    @Operation(
        summary = "Récupérer le feed Following",
        description = "Retourne les posts des salons et coiffeurs suivis par l'utilisateur authentifié"
    )
    @GetMapping("/feed/following")
    fun getFollowingFeed(
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserId()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.getFollowingFeed(currentUserId, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Supprime un post.
     */
    @Operation(
        summary = "Supprimer un post",
        description = "Supprime un post (uniquement par son auteur)"
    )
    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    fun deletePost(
        @PathVariable postId: String
    ): ResponseEntity<Map<String, String>> {
        val authenticatedUserId = getAuthenticatedUserId()
        socialService.deletePost(postId, authenticatedUserId)
        return ResponseEntity.ok(mapOf("message" to "Post supprimé avec succès"))
    }

    // ========== ENDPOINTS DE LIKES ==========

    /**
     * Toggle le like d'un utilisateur sur un post.
     */
    @Operation(
        summary = "Toggle like",
        description = "Ajoute ou retire un like sur un post"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Like togglé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Post non trouvé"
            )
        ]
    )
    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    fun toggleLike(
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val post = socialService.toggleLike(postId, authenticatedUserId)
        return ResponseEntity.ok(post)
    }

    /**
     * Vérifie si un utilisateur a liké un post.
     */
    @Operation(
        summary = "Vérifier si un post est liké",
        description = "Retourne true si l'utilisateur a liké le post"
    )
    @GetMapping("/posts/{postId}/liked")
    @PreAuthorize("isAuthenticated()")
    fun isPostLiked(
        @PathVariable postId: String
    ): ResponseEntity<Map<String, Boolean>> {
        val authenticatedUserId = getAuthenticatedUserId()
        val isLiked = socialService.isPostLikedByUser(postId, authenticatedUserId)
        return ResponseEntity.ok(mapOf("isLiked" to isLiked))
    }

    // ========== ENDPOINTS DE FAVORIS ==========

    /**
     * Toggle favorite.
     */
    @Operation(
        summary = "Toggle favorite",
        description = "Ajoute ou retire un favori sur un post"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Favori togglé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Post non trouvé"
            )
        ]
    )
    @PostMapping("/posts/{postId}/favorite")
    @PreAuthorize("isAuthenticated()")
    fun toggleFavorite(
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val post = socialService.toggleFavorite(postId, authenticatedUserId)
        return ResponseEntity.ok(post)
    }

    /**
     * Récupère les favoris d'un utilisateur.
     */
    @Operation(
        summary = "Récupérer les favoris d'un utilisateur",
        description = "Retourne la liste paginée des posts favoris d'un utilisateur"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des favoris récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur non trouvé"
            )
        ]
    )
    @GetMapping("/users/{userId}/favorites")
    @PreAuthorize("isAuthenticated()")
    fun getFavoritesByUser(
        @PathVariable userId: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()
        // Vérifier que l'utilisateur demande ses propres favoris ou est admin
        if (userId != authenticatedUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val pageable = PageRequest.of(page, size)
        val favorites = socialService.getFavoritesByUser(userId, pageable, authenticatedUserId)
        return ResponseEntity.ok(favorites)
    }

    // ========== ENDPOINTS DE ARCHIVES ==========

    /**
     * Archive un post.
     */
    @Operation(
        summary = "Archiver un post",
        description = "Archive un post (le masque du feed principal)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Post archivé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Post non trouvé"
            )
        ]
    )
    @PostMapping("/posts/{postId}/archive")
    @PreAuthorize("isAuthenticated()")
    fun archivePost(
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val post = socialService.archivePost(postId, authenticatedUserId)
        return ResponseEntity.ok(post)
    }

    /**
     * Désarchive un post.
     */
    @Operation(
        summary = "Désarchiver un post",
        description = "Désarchive un post (le réaffiche dans le feed principal)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Post désarchivé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Post non trouvé"
            )
        ]
    )
    @DeleteMapping("/posts/{postId}/archive")
    @PreAuthorize("isAuthenticated()")
    fun unarchivePost(
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val post = socialService.unarchivePost(postId, authenticatedUserId)
        return ResponseEntity.ok(post)
    }

    /**
     * Récupère les archives d'un utilisateur.
     */
    @Operation(
        summary = "Récupérer les archives d'un utilisateur",
        description = "Retourne la liste paginée des posts archivés d'un utilisateur"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des archives récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur non trouvé"
            )
        ]
    )
    @GetMapping("/users/{userId}/archives")
    @PreAuthorize("isAuthenticated()")
    fun getArchivedPosts(
        @PathVariable userId: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val authenticatedUserId = getAuthenticatedUserId()
        // Vérifier que l'utilisateur demande ses propres archives ou est admin
        if (userId != authenticatedUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val pageable = PageRequest.of(page, size)
        val archives = socialService.getArchivedPosts(userId, pageable, authenticatedUserId)
        return ResponseEntity.ok(archives)
    }

    // ========== GESTION DES PARTAGES ==========
    // Phase D.3 - Partage de Posts (Reposts)

    @Operation(
        summary = "Partager un post",
        description = "Permet à l'utilisateur authentifié de partager un post avec un commentaire optionnel"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Post partagé avec succès"),
            ApiResponse(responseCode = "400", description = "Requête invalide (ex: déjà partagé, commentaire trop long)"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "404", description = "Post non trouvé")
        ]
    )
    @PostMapping("/posts/{postId}/share")
    @PreAuthorize("isAuthenticated()")
    fun sharePost(
        @Parameter(description = "ID du post à partager", required = true)
        @PathVariable postId: String,
        @RequestBody request: SharePostRequest
    ): ResponseEntity<PostResponse> {
        val currentUserId = getAuthenticatedUserId()
        request.validate()
        val post = socialService.sharePost(postId, currentUserId, request.sharedContent)
        return ResponseEntity.ok(post)
    }

    @Operation(
        summary = "Annuler le partage d'un post",
        description = "Permet à l'utilisateur authentifié d'annuler le partage d'un post qu'il a précédemment partagé"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Partage annulé avec succès"),
            ApiResponse(responseCode = "400", description = "Requête invalide (ex: post non partagé)"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "404", description = "Post non trouvé")
        ]
    )
    @DeleteMapping("/posts/{postId}/share")
    @PreAuthorize("isAuthenticated()")
    fun unsharePost(
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val currentUserId = getAuthenticatedUserId()
        val post = socialService.unsharePost(postId, currentUserId)
        return ResponseEntity.ok(post)
    }

    @Operation(
        summary = "Vérifier si un post est partagé",
        description = "Retourne true si l'utilisateur authentifié a partagé le post, false sinon"
    )
    @GetMapping("/posts/{postId}/shared")
    @PreAuthorize("isAuthenticated()")
    fun isPostShared(
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String
    ): ResponseEntity<Map<String, Boolean>> {
        val currentUserId = getAuthenticatedUserId()
        val isShared = socialService.isPostSharedByUser(postId, currentUserId)
        return ResponseEntity.ok(mapOf("isShared" to isShared))
    }

    @Operation(
        summary = "Récupérer les partages d'un post",
        description = "Retourne la liste paginée des utilisateurs qui ont partagé le post"
    )
    @GetMapping("/posts/{postId}/shares")
    fun getSharesByPost(
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostShareResponse>> {
        val pageable = PageRequest.of(page, size)
        val shares = socialService.getSharesByPost(postId, pageable)
        return ResponseEntity.ok(shares)
    }

    // ========== ENDPOINTS DE RÉACTIONS SPÉCIALISÉES ==========
    // Phase D.4 - Réactions Spécialisées Coiffure

    @Operation(
        summary = "Ajouter ou modifier une réaction sur un post",
        description = "Permet à l'utilisateur authentifié d'ajouter une réaction spécialisée (like, love, wow, inspirant, magnifique, bravo) sur un post. Si l'utilisateur a déjà une réaction, elle est remplacée."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Réaction ajoutée/modifiée avec succès"),
            ApiResponse(responseCode = "400", description = "Requête invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "404", description = "Post non trouvé")
        ]
    )
    @PostMapping("/posts/{postId}/reactions")
    @PreAuthorize("isAuthenticated()")
    fun addReaction(
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String,
        @Parameter(description = "Type de réaction", required = true)
        @Valid @RequestBody request: AddReactionRequest
    ): ResponseEntity<PostResponse> {
        val currentUserId = getAuthenticatedUserId()
        request.validate()
        val post = socialService.addReaction(postId, currentUserId, request.reactionType)
        return ResponseEntity.ok(post)
    }

    @Operation(
        summary = "Supprimer une réaction sur un post",
        description = "Permet à l'utilisateur authentifié de supprimer sa réaction sur un post"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Réaction supprimée avec succès"),
            ApiResponse(responseCode = "400", description = "Requête invalide (ex: aucune réaction existante)"),
            ApiResponse(responseCode = "401", description = "Non authentifié"),
            ApiResponse(responseCode = "404", description = "Post non trouvé")
        ]
    )
    @DeleteMapping("/posts/{postId}/reactions")
    @PreAuthorize("isAuthenticated()")
    fun removeReaction(
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val currentUserId = getAuthenticatedUserId()
        val post = socialService.removeReaction(postId, currentUserId)
        return ResponseEntity.ok(post)
    }

    @Operation(
        summary = "Récupérer les réactions d'un post",
        description = "Retourne les compteurs de réactions par type pour un post"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Compteurs de réactions"),
            ApiResponse(responseCode = "404", description = "Post non trouvé")
        ]
    )
    @GetMapping("/posts/{postId}/reactions")
    fun getReactionsByPost(
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String
    ): ResponseEntity<Map<String, Int>> {
        val reactions = socialService.getReactionsByPost(postId)
        // Convertir l'enum en String pour la sérialisation JSON
        val reactionsMap = reactions.mapKeys { it.key.name.lowercase() }
        return ResponseEntity.ok(reactionsMap)
    }

    // ========== ENDPOINTS DE COMMENTAIRES ==========

    /**
     * Crée un nouveau commentaire.
     */
    @Operation(
        summary = "Créer un commentaire",
        description = "Ajoute un commentaire à un post"
    )
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    fun createComment(
        @PathVariable postId: String,
        @Valid @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CommentResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val validatedRequest = request.copy(postId = postId, authorId = authenticatedUserId)
        val comment = socialService.createComment(validatedRequest)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/social/comments/${comment.id}")
            .body(comment)
    }

    /**
     * Récupère les commentaires d'un post.
     */
    @Operation(
        summary = "Récupérer les commentaires d'un post",
        description = "Retourne tous les commentaires d'un post avec pagination"
    )
    @GetMapping("/posts/{postId}/comments")
    fun getCommentsByPost(
        @PathVariable postId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<Page<CommentResponse>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val comments = socialService.getCommentsByPost(postId, pageable)
        return ResponseEntity.ok(comments)
    }

    /**
     * Supprime un commentaire.
     */
    @Operation(
        summary = "Supprimer un commentaire",
        description = "Supprime un commentaire (uniquement par son auteur)"
    )
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteComment(
        @PathVariable commentId: String
    ): ResponseEntity<Map<String, String>> {
        val authenticatedUserId = getAuthenticatedUserId()
        socialService.deleteComment(commentId, authenticatedUserId)
        return ResponseEntity.ok(mapOf("message" to "Commentaire supprimé avec succès"))
    }

    // ========== ENDPOINTS DE TAGS/MENTIONS ==========

    /**
     * Ajoute un tag (salon ou utilisateur) à un post.
     */
    @Operation(
        summary = "Ajouter un tag à un post",
        description = "Tague un salon ou un utilisateur dans un post (uniquement par l'auteur du post)"
    )
    @PostMapping("/posts/{postId}/tags")
    @PreAuthorize("isAuthenticated()")
    fun addTag(
        @PathVariable postId: String,
        @Valid @RequestBody request: CreateTagRequest
    ): ResponseEntity<TagResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val tag = socialService.addTag(postId, request.taggedType, request.taggedId, authenticatedUserId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/social/posts/$postId/tags/${tag.id}")
            .body(tag)
    }

    /**
     * Supprime un tag d'un post.
     */
    @Operation(
        summary = "Supprimer un tag",
        description = "Supprime un tag d'un post (uniquement par l'auteur du post)"
    )
    @DeleteMapping("/posts/{postId}/tags/{tagId}")
    @PreAuthorize("isAuthenticated()")
    fun removeTag(
        @PathVariable postId: String,
        @PathVariable tagId: String
    ): ResponseEntity<Map<String, String>> {
        val authenticatedUserId = getAuthenticatedUserId()
        socialService.removeTag(tagId, authenticatedUserId)
        return ResponseEntity.ok(mapOf("message" to "Tag supprimé avec succès"))
    }

    /**
     * Récupère tous les tags d'un post.
     */
    @Operation(
        summary = "Récupérer les tags d'un post",
        description = "Retourne tous les tags/mentions d'un post"
    )
    @GetMapping("/posts/{postId}/tags")
    fun getTagsByPost(
        @PathVariable postId: String
    ): ResponseEntity<List<TagResponse>> {
        val tags = socialService.getTagsByPost(postId)
        return ResponseEntity.ok(tags)
    }

    // ========== ENDPOINTS DE HASHTAGS ==========

    /**
     * Récupère les hashtags les plus utilisés (trending).
     */
    @Operation(
        summary = "Récupérer les hashtags trending",
        description = "Retourne les hashtags les plus utilisés"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des hashtags trending récupérée avec succès"
            )
        ]
    )
    @GetMapping("/hashtags/trending")
    fun getTrendingHashtags(
        @Parameter(description = "Nombre de hashtags à retourner", example = "20")
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<HairHashtagResponse>> {
        val hashtags = socialService.getTrendingHashtags(limit)
        return ResponseEntity.ok(hashtags)
    }

    /**
     * Recherche des hashtags par nom.
     */
    @Operation(
        summary = "Rechercher des hashtags",
        description = "Recherche des hashtags par nom (insensible à la casse)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des hashtags correspondants"
            )
        ]
    )
    @GetMapping("/hashtags/search")
    fun searchHashtags(
        @Parameter(description = "Terme de recherche", required = true)
        @RequestParam q: String
    ): ResponseEntity<List<HairHashtagResponse>> {
        val hashtags = socialService.searchHashtags(q)
        return ResponseEntity.ok(hashtags)
    }

    /**
     * Suggère des hashtags basés sur un préfixe.
     */
    @Operation(
        summary = "Suggérer des hashtags",
        description = "Retourne des suggestions de hashtags basées sur un préfixe (pour autocomplétion)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des suggestions de hashtags"
            )
        ]
    )
    @GetMapping("/hashtags/suggest")
    fun suggestHashtags(
        @Parameter(description = "Préfixe de recherche", required = true)
        @RequestParam prefix: String,
        @Parameter(description = "Nombre de suggestions", example = "10")
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<HairHashtagResponse>> {
        val hashtags = socialService.suggestHashtags(prefix, limit)
        return ResponseEntity.ok(hashtags)
    }

    /**
     * Récupère les posts associés à un hashtag.
     */
    @Operation(
        summary = "Récupérer les posts d'un hashtag",
        description = "Retourne tous les posts associés à un hashtag spécifique"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste paginée des posts du hashtag"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Hashtag non trouvé"
            )
        ]
    )
    @GetMapping("/hashtags/{hashtagName}/posts")
    fun getPostsByHashtag(
        @PathVariable hashtagName: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.getPostsByHashtag(hashtagName, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Récupère les hashtags par catégorie.
     */
    @Operation(
        summary = "Récupérer les hashtags par catégorie",
        description = "Retourne tous les hashtags d'une catégorie spécifique"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des hashtags de la catégorie"
            )
        ]
    )
    @GetMapping("/hashtags/categories/{category}")
    fun getHashtagsByCategory(
        @PathVariable category: HairHashtagCategory
    ): ResponseEntity<List<HairHashtagResponse>> {
        val hashtags = socialService.getHashtagsByCategory(category)
        return ResponseEntity.ok(hashtags)
    }

    // ========== ENDPOINTS DE PROFIL COIFFEUR ==========
    // Phase E.1 - Profil Coiffeur Enrichi

    @Operation(
        summary = "Récupérer le profil enrichi d'un coiffeur",
        description = "Retourne le profil social complet d'un coiffeur avec bio, spécialités, portfolios, statistiques et posts récents"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profil coiffeur récupéré avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CoiffeurProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Coiffeur non trouvé"
            ),
            ApiResponse(
                responseCode = "400",
                description = "L'utilisateur n'est pas un coiffeur"
            )
        ]
    )
    @GetMapping("/coiffeurs/{coiffeurId}/profile")
    fun getCoiffeurProfile(
        @Parameter(description = "ID du coiffeur", required = true)
        @PathVariable coiffeurId: String
    ): ResponseEntity<CoiffeurProfileResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val profile = socialService.getCoiffeurProfile(coiffeurId, currentUserId)
        return ResponseEntity.ok(profile)
    }

    @Operation(
        summary = "Mettre à jour le profil enrichi d'un coiffeur",
        description = "Permet à un coiffeur de mettre à jour son profil social (bio, spécialités, années d'expérience, certifications, Instagram, portfolio mis en avant). Seul le propriétaire du profil peut le modifier."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profil coiffeur mis à jour avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CoiffeurProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide (validation échouée)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire du profil)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Coiffeur ou portfolio non trouvé"
            )
        ]
    )
    @PutMapping("/coiffeurs/{coiffeurId}/profile")
    @PreAuthorize("isAuthenticated()")
    fun updateCoiffeurProfile(
        @Parameter(description = "ID du coiffeur", required = true)
        @PathVariable coiffeurId: String,
        @Parameter(description = "Données de mise à jour du profil", required = true)
        @Valid @RequestBody request: UpdateCoiffeurProfileRequest
    ): ResponseEntity<CoiffeurProfileResponse> {
        val currentUserId = getAuthenticatedUserId()
        val profile = socialService.updateCoiffeurProfile(coiffeurId, request, currentUserId)
        return ResponseEntity.ok(profile)
    }

    // ========== ENDPOINTS DE PROFIL SALON SOCIAL ==========
    // Phase E.2 - Profil Salon Social

    @Operation(
        summary = "Récupérer le profil social enrichi d'un salon",
        description = "Retourne le profil social complet d'un salon avec cover image, description sociale, posts mis en avant, équipe, services, statistiques et posts récents"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profil salon social récupéré avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SalonSocialProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon non trouvé"
            )
        ]
    )
    @GetMapping("/salons/{salonId}/profile")
    fun getSalonSocialProfile(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String
    ): ResponseEntity<SalonSocialProfileResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val profile = socialService.getSalonSocialProfile(salonId, currentUserId)
        return ResponseEntity.ok(profile)
    }

    @Operation(
        summary = "Mettre à jour le profil social d'un salon",
        description = "Permet au propriétaire d'un salon de mettre à jour son profil social (description sociale, cover image, posts mis en avant). Seul le propriétaire du salon peut le modifier."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profil salon social mis à jour avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SalonSocialProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide (validation échouée)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire du salon)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon ou post non trouvé"
            )
        ]
    )
    @PutMapping("/salons/{salonId}/profile")
    @PreAuthorize("isAuthenticated()")
    fun updateSalonSocialProfile(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String,
        @Parameter(description = "Données de mise à jour du profil social", required = true)
        @Valid @RequestBody request: UpdateSalonSocialProfileRequest
    ): ResponseEntity<SalonSocialProfileResponse> {
        val currentUserId = getAuthenticatedUserId()
        val profile = socialService.updateSalonSocialProfile(salonId, request, currentUserId)
        return ResponseEntity.ok(profile)
    }

    // ========== ENDPOINTS DE BADGES ==========
    // Phase E.3 - Badges et Certifications

    @Operation(
        summary = "Récupérer tous les badges disponibles",
        description = "Retourne la liste de tous les badges disponibles dans le système, optionnellement filtrés par catégorie"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des badges récupérée avec succès"
            )
        ]
    )
    @GetMapping("/badges")
    fun getAvailableBadges(
        @Parameter(description = "Catégorie de badge (optionnel)", required = false)
        @RequestParam(required = false) category: com.frollot.model.BadgeCategory?
    ): ResponseEntity<List<BadgeResponse>> {
        val badges = socialService.getAvailableBadges(category)
        return ResponseEntity.ok(badges)
    }

    @Operation(
        summary = "Récupérer les badges d'un utilisateur",
        description = "Retourne la liste des badges d'un utilisateur, avec option pour inclure les badges masqués"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des badges de l'utilisateur"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur non trouvé"
            )
        ]
    )
    @GetMapping("/users/{userId}/badges")
    fun getUserBadges(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @Parameter(description = "Inclure les badges masqués (uniquement si l'utilisateur est le propriétaire)")
        @RequestParam(defaultValue = "false") includeHidden: Boolean
    ): ResponseEntity<List<UserBadgeResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        // Seul le propriétaire peut voir les badges masqués
        val finalIncludeHidden = includeHidden && (currentUserId == userId)
        val badges = socialService.getUserBadges(userId, finalIncludeHidden)
        return ResponseEntity.ok(badges)
    }

    // ========== ENDPOINTS DE PROFIL CLIENT ==========
    // Phase E.4 - Profil Client

    @Operation(
        summary = "Récupérer le profil enrichi d'un client",
        description = "Retourne le profil social complet d'un client avec statistiques, posts récents, collections et badges"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profil client récupéré avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ClientProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Client non trouvé"
            ),
            ApiResponse(
                responseCode = "400",
                description = "L'utilisateur n'est pas un client"
            )
        ]
    )
    @GetMapping("/clients/{clientId}/profile")
    fun getClientProfile(
        @Parameter(description = "ID du client", required = true)
        @PathVariable clientId: String
    ): ResponseEntity<ClientProfileResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val profile = socialService.getClientProfile(clientId, currentUserId)
        return ResponseEntity.ok(profile)
    }

    // ========== ENDPOINTS DE PROFIL PROPRIÉTAIRE DE SALON ==========
    // Phase E.5 - Profil Propriétaire de Salon

    @Operation(
        summary = "Récupérer le profil enrichi d'un propriétaire de salon",
        description = "Retourne le profil social complet d'un propriétaire de salon avec statistiques, salons possédés, posts récents, collections et badges"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profil propriétaire récupéré avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SalonOwnerProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Propriétaire non trouvé"
            ),
            ApiResponse(
                responseCode = "400",
                description = "L'utilisateur n'est pas un propriétaire de salon"
            )
        ]
    )
    @GetMapping("/owners/{ownerId}/profile")
    fun getSalonOwnerProfile(
        @Parameter(description = "ID du propriétaire", required = true)
        @PathVariable ownerId: String
    ): ResponseEntity<SalonOwnerProfileResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val profile = socialService.getSalonOwnerProfile(ownerId, currentUserId)
        return ResponseEntity.ok(profile)
    }

    @Operation(
        summary = "Attribuer un badge à un utilisateur",
        description = "Permet à un administrateur d'attribuer un badge à un utilisateur"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Badge attribué avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide (ex: utilisateur a déjà ce badge)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas administrateur)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur ou badge non trouvé"
            )
        ]
    )
    @PostMapping("/users/{userId}/badges")
    @PreAuthorize("isAuthenticated()")
    fun awardBadge(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @Parameter(description = "Données d'attribution du badge", required = true)
        @Valid @RequestBody request: AwardBadgeRequest
    ): ResponseEntity<UserBadgeResponse> {
        val currentUserId = getAuthenticatedUserId()
        request.validate()
        val userBadge = socialService.awardBadge(userId, request.badgeId, currentUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(userBadge)
    }

    @Operation(
        summary = "Afficher ou masquer un badge",
        description = "Permet à un utilisateur d'afficher ou masquer un badge sur son profil"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Affichage du badge modifié avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire du profil)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur ou badge non trouvé"
            )
        ]
    )
    @PutMapping("/users/{userId}/badges/{badgeId}")
    @PreAuthorize("isAuthenticated()")
    fun toggleBadgeDisplay(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID du badge", required = true)
        @PathVariable badgeId: String
    ): ResponseEntity<UserBadgeResponse> {
        val currentUserId = getAuthenticatedUserId()
        val userBadge = socialService.toggleBadgeDisplay(userId, badgeId, currentUserId)
        return ResponseEntity.ok(userBadge)
    }

    @Operation(
        summary = "Retirer un badge d'un utilisateur",
        description = "Permet à un administrateur de retirer un badge d'un utilisateur"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Badge retiré avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas administrateur)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur ou badge non trouvé"
            )
        ]
    )
    @DeleteMapping("/users/{userId}/badges/{badgeId}")
    @PreAuthorize("isAuthenticated()")
    fun removeBadge(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID du badge", required = true)
        @PathVariable badgeId: String
    ): ResponseEntity<Map<String, String>> {
        val currentUserId = getAuthenticatedUserId()
        socialService.removeBadge(userId, badgeId, currentUserId)
        return ResponseEntity.ok(mapOf("message" to "Badge retiré avec succès"))
    }

    // ========== ENDPOINTS DE COLLECTIONS ==========
    // Phase F.1 - Collections Thématiques

    @Operation(
        summary = "Créer une nouvelle collection",
        description = "Permet à un utilisateur de créer une collection thématique pour organiser ses posts"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Collection créée avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide (validation échouée)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            )
        ]
    )
    @PostMapping("/collections")
    @PreAuthorize("isAuthenticated()")
    fun createCollection(
        @Parameter(description = "Données de création de la collection", required = true)
        @Valid @RequestBody request: CreateCollectionRequest
    ): ResponseEntity<CollectionResponse> {
        val currentUserId = getAuthenticatedUserId()
        val collection = socialService.createCollection(request, currentUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(collection)
    }

    @Operation(
        summary = "Mettre à jour une collection",
        description = "Permet à un utilisateur de modifier sa collection (nom, description, visibilité, catégorie). Seul le propriétaire peut modifier."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Collection mise à jour avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Requête invalide"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Collection non trouvée"
            )
        ]
    )
    @PutMapping("/collections/{collectionId}")
    @PreAuthorize("isAuthenticated()")
    fun updateCollection(
        @Parameter(description = "ID de la collection", required = true)
        @PathVariable collectionId: String,
        @Parameter(description = "Données de mise à jour", required = true)
        @Valid @RequestBody request: UpdateCollectionRequest
    ): ResponseEntity<CollectionResponse> {
        val currentUserId = getAuthenticatedUserId()
        val collection = socialService.updateCollection(collectionId, request, currentUserId)
        return ResponseEntity.ok(collection)
    }

    @Operation(
        summary = "Supprimer une collection",
        description = "Permet à un utilisateur de supprimer sa collection. Seul le propriétaire peut supprimer."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Collection supprimée avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Collection non trouvée"
            )
        ]
    )
    @DeleteMapping("/collections/{collectionId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteCollection(
        @Parameter(description = "ID de la collection", required = true)
        @PathVariable collectionId: String
    ): ResponseEntity<Map<String, String>> {
        val currentUserId = getAuthenticatedUserId()
        socialService.deleteCollection(collectionId, currentUserId)
        return ResponseEntity.ok(mapOf("message" to "Collection supprimée avec succès"))
    }

    @Operation(
        summary = "Récupérer les collections d'un utilisateur",
        description = "Retourne toutes les collections d'un utilisateur (publiques uniquement si ce n'est pas le propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des collections récupérée avec succès"
            )
        ]
    )
    @GetMapping("/users/{userId}/collections")
    fun getCollectionsByUser(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @Parameter(description = "Inclure les collections privées (uniquement si l'utilisateur est le propriétaire)")
        @RequestParam(defaultValue = "false") includePrivate: Boolean
    ): ResponseEntity<List<CollectionResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val collections = socialService.getCollectionsByUser(userId, includePrivate, currentUserId)
        return ResponseEntity.ok(collections)
    }

    @Operation(
        summary = "Récupérer une collection par son ID",
        description = "Retourne les détails d'une collection (publique uniquement si ce n'est pas le propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Collection récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (collection privée)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Collection non trouvée"
            )
        ]
    )
    @GetMapping("/collections/{collectionId}")
    fun getCollectionById(
        @Parameter(description = "ID de la collection", required = true)
        @PathVariable collectionId: String
    ): ResponseEntity<CollectionResponse> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val collection = socialService.getCollectionById(collectionId, currentUserId)
        return ResponseEntity.ok(collection)
    }

    @Operation(
        summary = "Ajouter un post à une collection",
        description = "Permet d'ajouter un post à une collection. Seul le propriétaire de la collection peut ajouter des posts."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Post ajouté à la collection avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Le post est déjà dans cette collection"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire de la collection)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Collection ou post non trouvé"
            )
        ]
    )
    @PostMapping("/collections/{collectionId}/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    fun addPostToCollection(
        @Parameter(description = "ID de la collection", required = true)
        @PathVariable collectionId: String,
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String
    ): ResponseEntity<CollectionPostResponse> {
        val currentUserId = getAuthenticatedUserId()
        val collectionPost = socialService.addPostToCollection(collectionId, postId, currentUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionPost)
    }

    @Operation(
        summary = "Retirer un post d'une collection",
        description = "Permet de retirer un post d'une collection. Seul le propriétaire de la collection peut retirer des posts."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Post retiré de la collection avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (pas le propriétaire de la collection)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Collection, post ou association non trouvée"
            )
        ]
    )
    @DeleteMapping("/collections/{collectionId}/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    fun removePostFromCollection(
        @Parameter(description = "ID de la collection", required = true)
        @PathVariable collectionId: String,
        @Parameter(description = "ID du post", required = true)
        @PathVariable postId: String
    ): ResponseEntity<Map<String, String>> {
        val currentUserId = getAuthenticatedUserId()
        socialService.removePostFromCollection(collectionId, postId, currentUserId)
        return ResponseEntity.ok(mapOf("message" to "Post retiré de la collection avec succès"))
    }

    @Operation(
        summary = "Récupérer les posts d'une collection",
        description = "Retourne tous les posts d'une collection avec pagination (publique uniquement si ce n'est pas le propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste paginée des posts de la collection"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (collection privée)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Collection non trouvée"
            )
        ]
    )
    @GetMapping("/collections/{collectionId}/posts")
    fun getCollectionPosts(
        @Parameter(description = "ID de la collection", required = true)
        @PathVariable collectionId: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<CollectionPostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val pageable = PageRequest.of(page, size)
        val posts = socialService.getCollectionPosts(collectionId, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    @Operation(
        summary = "Récupérer les collections publiques",
        description = "Retourne les collections publiques pour la découverte, optionnellement filtrées par catégorie"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste paginée des collections publiques"
            )
        ]
    )
    @GetMapping("/collections/public")
    fun getPublicCollections(
        @Parameter(description = "Catégorie de collection (optionnel)", required = false)
        @RequestParam(required = false) category: com.frollot.model.CollectionCategory?,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<CollectionResponse>> {
        val pageable = PageRequest.of(page, size)
        val collections = socialService.getPublicCollections(category, pageable)
        return ResponseEntity.ok(collections)
    }

    // ========== PHASE F.2 : POSTS ÉPINGLÉS POUR SALONS ==========

    @Operation(
        summary = "Épingler un post",
        description = "Épingle un post pour son auteur. Maximum 3 posts épinglés par auteur. Seul l'auteur du post peut l'épingler."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Post épinglé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Post non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Seul l'auteur du post peut l'épingler"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Limite de 3 posts épinglés atteinte"
            )
        ]
    )
    @PostMapping("/posts/{postId}/pin")
    fun pinPost(
        @Parameter(description = "ID du post à épingler", required = true)
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val userId = getAuthenticatedUserId()
        val post = socialService.pinPost(postId, userId)
        return ResponseEntity.ok(post)
    }

    @Operation(
        summary = "Désépingler un post",
        description = "Désépingle un post. Seul l'auteur du post peut le désépingler."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Post désépinglé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Post non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Seul l'auteur du post peut le désépingler"
            )
        ]
    )
    @DeleteMapping("/posts/{postId}/pin")
    fun unpinPost(
        @Parameter(description = "ID du post à désépingler", required = true)
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val userId = getAuthenticatedUserId()
        val post = socialService.unpinPost(postId, userId)
        return ResponseEntity.ok(post)
    }

    @Operation(
        summary = "Récupérer les posts épinglés d'un utilisateur",
        description = "Retourne la liste des posts épinglés d'un utilisateur, triés par date de création décroissante"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des posts épinglés"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur non trouvé"
            )
        ]
    )
    @GetMapping("/users/{authorId}/pinned-posts")
    fun getPinnedPosts(
        @Parameter(description = "ID de l'auteur des posts", required = true)
        @PathVariable authorId: String
    ): ResponseEntity<List<PostResponse>> {
        val currentUserId = getAuthenticatedUserIdOrNull()
        val posts = socialService.getPinnedPosts(authorId, currentUserId)
        return ResponseEntity.ok(posts)
    }

    // ========== PHOTO DE COUVERTURE UTILISATEUR ==========

    @Operation(
        summary = "Mettre à jour la photo de couverture d'un utilisateur",
        description = "Met à jour la photo de couverture du profil utilisateur. Seul le propriétaire du compte peut modifier sa photo de couverture."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Photo de couverture mise à jour avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Seul le propriétaire du compte peut modifier la photo de couverture"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            )
        ]
    )
    @PutMapping("/users/{userId}/cover-image")
    @PreAuthorize("isAuthenticated()")
    fun updateUserCoverImage(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, String>> {
        return try {
            val authenticatedUserId = getAuthenticatedUserId()
            val coverImageUrl = request["coverImageUrl"]
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "coverImageUrl est requis"))

            val updatedUser = userService.updateUserCoverImage(userId, coverImageUrl, authenticatedUserId)
            ResponseEntity.ok(mapOf(
                "message" to "Photo de couverture mise à jour avec succès",
                "coverImageUrl" to (updatedUser.coverImageUrl ?: "")
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "Utilisateur non trouvé")))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to (e.message ?: "Accès refusé")))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erreur serveur: ${e.message}"))
        }
    }

    // ========== GESTION DES ERREURS ==========

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentication required")
            ))
    }

    @ExceptionHandler(SocialService.PostNotFoundException::class)
    fun handlePostNotFoundException(ex: SocialService.PostNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Post non trouvé")
            ))
    }

    @ExceptionHandler(SocialService.UnauthorizedAccessException::class)
    fun handleUnauthorizedException(ex: SocialService.UnauthorizedAccessException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Accès refusé")
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Validation error",
                "message" to (ex.message ?: "Données invalides")
            ))
    }

    @ExceptionHandler(ClassCastException::class)
    fun handleClassCastException(ex: ClassCastException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to "Problème d'authentification: token JWT invalide ou expiré"
            ))
    }
}