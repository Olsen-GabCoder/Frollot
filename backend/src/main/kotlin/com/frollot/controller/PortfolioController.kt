package com.frollot.controller

import com.frollot.dto.*
import com.frollot.model.PortfolioOwnerType
import com.frollot.model.User
import com.frollot.service.PortfolioService
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
 * Contrôleur REST pour la gestion des portfolios coiffeur/salon.
 *
 * Expose une API complète pour :
 * - Créer et gérer des portfolios
 * - Ajouter/retirer des posts dans les portfolios
 * - Réorganiser les posts
 * - Consulter les portfolios publics
 */
@RestController
@RequestMapping("/api/portfolios")
@Tag(
    name = "Portfolios",
    description = "API de gestion des portfolios coiffeur/salon"
)
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:9090", "http://10.0.2.2:9090"],
    allowCredentials = "true"
)
class PortfolioController(
    private val portfolioService: PortfolioService
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
                throw IllegalStateException("Authentification invalide: le principal est une chaîne")
            }
            else -> throw IllegalStateException("Type de principal non supporté: ${principal?.javaClass?.name}")
        }
    }

    // ========== ENDPOINTS DE PORTFOLIOS ==========

    /**
     * Crée un nouveau portfolio.
     */
    @Operation(
        summary = "Créer un portfolio",
        description = "Crée un nouveau portfolio pour un coiffeur ou un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Portfolio créé avec succès",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PortfolioResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            ),
            ApiResponse(
                responseCode = "403",
                description = "L'utilisateur n'a pas les droits pour créer ce type de portfolio"
            )
        ]
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createPortfolio(
        @Parameter(description = "Données du portfolio", required = true)
        @Valid @RequestBody request: CreatePortfolioRequest
    ): ResponseEntity<PortfolioResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val portfolio = portfolioService.createPortfolio(request, authenticatedUserId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/portfolios/${portfolio.id}")
            .body(portfolio)
    }

    /**
     * Met à jour un portfolio.
     */
    @Operation(
        summary = "Mettre à jour un portfolio",
        description = "Met à jour les informations d'un portfolio (uniquement par son propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio mis à jour avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé"
            )
        ]
    )
    @PutMapping("/{portfolioId}")
    @PreAuthorize("isAuthenticated()")
    fun updatePortfolio(
        @PathVariable portfolioId: String,
        @Valid @RequestBody request: UpdatePortfolioRequest
    ): ResponseEntity<PortfolioResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val portfolio = portfolioService.updatePortfolio(portfolioId, request, authenticatedUserId)
        return ResponseEntity.ok(portfolio)
    }

    /**
     * Supprime un portfolio.
     */
    @Operation(
        summary = "Supprimer un portfolio",
        description = "Supprime un portfolio (uniquement par son propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio supprimé avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé"
            )
        ]
    )
    @DeleteMapping("/{portfolioId}")
    @PreAuthorize("isAuthenticated()")
    fun deletePortfolio(
        @PathVariable portfolioId: String
    ): ResponseEntity<Map<String, String>> {
        val authenticatedUserId = getAuthenticatedUserId()
        portfolioService.deletePortfolio(portfolioId, authenticatedUserId)
        return ResponseEntity.ok(mapOf("message" to "Portfolio supprimé avec succès"))
    }

    /**
     * Récupère un portfolio par son ID.
     */
    @Operation(
        summary = "Récupérer un portfolio par ID",
        description = "Retourne les détails d'un portfolio (public ou si l'utilisateur est le propriétaire)"
    )
    @GetMapping("/{portfolioId}")
    fun getPortfolioById(
        @PathVariable portfolioId: String
    ): ResponseEntity<PortfolioResponse> {
        val currentUserId = try {
            getAuthenticatedUserId()
        } catch (e: Exception) {
            null
        }
        val portfolio = portfolioService.getPortfolioById(portfolioId, currentUserId)
        return ResponseEntity.ok(portfolio)
    }

    /**
     * Récupère tous les portfolios d'un propriétaire.
     */
    @Operation(
        summary = "Récupérer les portfolios d'un propriétaire",
        description = "Retourne tous les portfolios (publics ou tous si l'utilisateur est le propriétaire) d'un coiffeur ou d'un salon"
    )
    @GetMapping("/owner/{ownerId}")
    fun getPortfoliosByOwner(
        @PathVariable ownerId: String,
        @Parameter(description = "Type de propriétaire", required = true)
        @RequestParam ownerType: PortfolioOwnerType,
        @Parameter(description = "Inclure les portfolios privés (uniquement si l'utilisateur est le propriétaire)")
        @RequestParam(defaultValue = "false") includePrivate: Boolean
    ): ResponseEntity<List<PortfolioResponse>> {
        val currentUserId = try {
            getAuthenticatedUserId()
        } catch (e: Exception) {
            null
        }
        val portfolios = portfolioService.getPortfoliosByOwner(ownerId, ownerType, currentUserId, includePrivate)
        return ResponseEntity.ok(portfolios)
    }

    // ========== ENDPOINTS DE POSTS DANS LES PORTFOLIOS ==========

    /**
     * Ajoute un post à un portfolio.
     */
    @Operation(
        summary = "Ajouter un post à un portfolio",
        description = "Ajoute un post à un portfolio (uniquement par le propriétaire du portfolio)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Post ajouté au portfolio avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio ou post non trouvé"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Le post est déjà dans le portfolio"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé"
            )
        ]
    )
    @PostMapping("/{portfolioId}/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    fun addPostToPortfolio(
        @PathVariable portfolioId: String,
        @PathVariable postId: String
    ): ResponseEntity<PortfolioPostResponse> {
        val authenticatedUserId = getAuthenticatedUserId()
        val portfolioPost = portfolioService.addPostToPortfolio(portfolioId, postId, authenticatedUserId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/portfolios/$portfolioId/posts/$postId")
            .body(portfolioPost)
    }

    /**
     * Retire un post d'un portfolio.
     */
    @Operation(
        summary = "Retirer un post d'un portfolio",
        description = "Retire un post d'un portfolio (uniquement par le propriétaire du portfolio)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Post retiré du portfolio avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio ou post non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé"
            )
        ]
    )
    @DeleteMapping("/{portfolioId}/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    fun removePostFromPortfolio(
        @PathVariable portfolioId: String,
        @PathVariable postId: String
    ): ResponseEntity<Map<String, String>> {
        val authenticatedUserId = getAuthenticatedUserId()
        portfolioService.removePostFromPortfolio(portfolioId, postId, authenticatedUserId)
        return ResponseEntity.ok(mapOf("message" to "Post retiré du portfolio avec succès"))
    }

    /**
     * Récupère tous les posts d'un portfolio.
     */
    @Operation(
        summary = "Récupérer les posts d'un portfolio",
        description = "Retourne tous les posts d'un portfolio avec pagination (public ou si l'utilisateur est le propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Liste des posts récupérée avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé (portfolio privé)"
            )
        ]
    )
    @GetMapping("/{portfolioId}/posts")
    fun getPortfolioPosts(
        @PathVariable portfolioId: String,
        @Parameter(description = "Numéro de page (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val currentUserId = try {
            getAuthenticatedUserId()
        } catch (e: Exception) {
            null
        }
        val pageable: Pageable = PageRequest.of(page, size)
        val posts = portfolioService.getPortfolioPosts(portfolioId, pageable, currentUserId)
        return ResponseEntity.ok(posts)
    }

    /**
     * Réorganise les posts d'un portfolio.
     */
    @Operation(
        summary = "Réorganiser les posts d'un portfolio",
        description = "Met à jour l'ordre des posts dans un portfolio (uniquement par le propriétaire)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ordre des posts mis à jour avec succès"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio non trouvé"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé"
            )
        ]
    )
    @PutMapping("/{portfolioId}/posts/reorder")
    @PreAuthorize("isAuthenticated()")
    fun reorderPortfolioPosts(
        @PathVariable portfolioId: String,
        @Parameter(description = "Liste des IDs de posts dans le nouvel ordre", required = true)
        @RequestBody postIds: List<String>
    ): ResponseEntity<Map<String, String>> {
        val authenticatedUserId = getAuthenticatedUserId()
        portfolioService.reorderPortfolioPosts(portfolioId, postIds, authenticatedUserId)
        return ResponseEntity.ok(mapOf("message" to "Ordre des posts mis à jour avec succès"))
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

    @ExceptionHandler(PortfolioService.PortfolioNotFoundException::class)
    fun handlePortfolioNotFoundException(ex: PortfolioService.PortfolioNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (ex.message ?: "Portfolio non trouvé")
            ))
    }

    @ExceptionHandler(PortfolioService.UnauthorizedAccessException::class)
    fun handleUnauthorizedAccessException(ex: PortfolioService.UnauthorizedAccessException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Accès refusé")
            ))
    }

    @ExceptionHandler(PortfolioService.PostAlreadyInPortfolioException::class)
    fun handlePostAlreadyInPortfolioException(ex: PortfolioService.PostAlreadyInPortfolioException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "Bad Request",
                "message" to (ex.message ?: "Le post est déjà dans le portfolio")
            ))
    }

    @ExceptionHandler(PortfolioService.InvalidOwnerException::class)
    fun handleInvalidOwnerException(ex: PortfolioService.InvalidOwnerException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf(
                "error" to "Forbidden",
                "message" to (ex.message ?: "Type de propriétaire invalide")
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
}

