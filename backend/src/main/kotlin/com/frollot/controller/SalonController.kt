// ============================================
// FICHIER: SalonController.kt - VERSION COMPLÈTE CORRIGÉE
// ============================================
package com.frollot.controller

import com.frollot.dto.CreateSalonRequest
import com.frollot.dto.PageResponse
import com.frollot.dto.SalonResponse
import com.frollot.model.FollowingType
import com.frollot.model.ServiceCategory
import com.frollot.model.User
import com.frollot.service.FollowService
import com.frollot.service.SalonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/salons")
@CrossOrigin(origins = ["http://localhost:3000", "http://10.0.2.2:8090"])
@Tag(
    name = "Gestion des Salons",
    description = "API de gestion des salons de coiffure"
)
class SalonController(
    private val salonService: SalonService,
    private val followService: FollowService, // Phase D.2 - Pour les informations de follow
    private val salonAuthorizationService: com.frollot.service.SalonAuthorizationService
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
        return (authentication.principal as User).id!!
    }

    // ========== ENDPOINTS ==========

    /**
     * Crée un nouveau salon.
     */
    @Operation(
        summary = "Créer un salon",
        description = "Crée un nouveau salon de coiffure. L'utilisateur authentifié devient le propriétaire."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Salon créé avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Utilisateur n'est pas salon_owner"
            )
        ]
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createSalon(
        @Parameter(description = "Données du salon", required = true)
        @RequestBody request: CreateSalonRequest
    ): ResponseEntity<Any> {
        return try {
            // Récupérer l'utilisateur authentifié
            val authenticatedUserId = getAuthenticatedUserId()

            // Créer une requête sécurisée avec l'ID de l'utilisateur authentifié
            val securedRequest = request.copy(
                ownerId = authenticatedUserId,
                id = "", // S'assurer que l'ID est généré côté serveur
                createdAt = "" // S'assurer que la date est générée côté serveur
            )

            val salon = salonService.createSalon(securedRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(salon)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to (e.message ?: "Authentication required")))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        }
    }

    /**
     * Récupère tous les salons (Marketplace) avec pagination optionnelle.
     * 
     * Si les paramètres de pagination ne sont pas fournis, retourne tous les résultats
     * (rétro-compatibilité avec les clients existants).
     */
    @Operation(
        summary = "Lister tous les salons",
        description = "Retourne la liste de tous les salons disponibles (marketplace) avec pagination optionnelle"
    )
    @GetMapping
    fun getSalons(
        @Parameter(description = "Terme de recherche") 
        @RequestParam(required = false, name = "q") q: String?,
        @Parameter(description = "Filtrer par ville") 
        @RequestParam(required = false) city: String?,
        @Parameter(description = "Filtrer par catégorie de service") 
        @RequestParam(required = false) category: String?,
        @Parameter(description = "Numéro de page (0-indexed)") 
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "Taille de la page") 
        @RequestParam(required = false, defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<SalonResponse>> {
        val normalizedQuery = q?.takeIf { it.isNotBlank() }
        val normalizedCity = city?.takeIf { it.isNotBlank() }

        val categoryEnum = category
            ?.takeIf { it.isNotBlank() }
            ?.let {
                try {
                    ServiceCategory.valueOf(it)
                } catch (ex: IllegalArgumentException) {
                    null
                }
            }

        val salons = salonService.searchSalons(
            query = normalizedQuery,
            city = normalizedCity,
            category = categoryEnum
        )
        
        // Appliquer la pagination manuellement pour rétro-compatibilité
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, salons.size)
        val paginatedSalons = if (startIndex < salons.size) {
            salons.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        val response = PageResponse(
            content = paginatedSalons,
            page = page,
            size = size,
            totalElements = salons.size.toLong(),
            totalPages = if (size > 0) ((salons.size + size - 1) / size) else 1,
            isFirst = page == 0,
            isLast = endIndex >= salons.size,
            hasNext = endIndex < salons.size,
            hasPrevious = page > 0
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * Récupère un salon spécifique par son ID.
     */
    @Operation(
        summary = "Récupérer un salon par ID",
        description = "Retourne les détails d'un salon spécifique"
    )
    @GetMapping("/{id}")
    fun getSalonById(@PathVariable id: String): ResponseEntity<Any> {
        println("🔍 Controller: Requête GET /api/salons/$id")

        return try {
            val currentUserId = getAuthenticatedUserIdOrNull()
            
            // Récupérer le salon de base
            val salonEntity = salonService.getSalonById(id)
            
            // Récupérer les informations de follow si un utilisateur est authentifié
            val isFollowed = currentUserId?.let {
                try {
                    followService.isFollowing(it, FollowingType.SALON, id)
                } catch (e: Exception) {
                    false
                }
            } ?: false
            
            val followersCount = try {
                followService.getFollowersCount(FollowingType.SALON, id)
            } catch (e: Exception) {
                0L
            }
            
            // Créer la réponse avec les informations de follow
            val salonResponse = SalonResponse(
                id = salonEntity.id,
                name = salonEntity.name,
                address = salonEntity.address,
                city = salonEntity.city,
                postalCode = salonEntity.postalCode,
                description = salonEntity.description,
                slug = salonEntity.slug,
                ownerId = salonEntity.ownerId,
                coverPhotoUrl = salonEntity.coverPhotoUrl,
                latitude = salonEntity.latitude,
                longitude = salonEntity.longitude,
                isVerified = salonEntity.isVerified, // Phase H.2 - Vérification Salons/Coiffeurs
                verificationType = salonEntity.verificationType, // Phase H.2 - Vérification Salons/Coiffeurs
                isFollowedByCurrentUser = if (currentUserId != null) isFollowed else null,
                followersCount = followersCount,
                createdAt = salonEntity.createdAt
            )
            
            println("✅ Controller: Salon trouvé - ${salonResponse.name}")
            ResponseEntity.ok(salonResponse)
        } catch (e: NoSuchElementException) {
            println("❌ Controller: Salon non trouvé - ${e.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "Salon non trouvé")))
        } catch (e: Exception) {
            println("❌ Controller: Erreur inattendue - ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erreur serveur"))
        }
    }
    
    /**
     * Récupère l'ID de l'utilisateur authentifié ou null si non authentifié.
     */
    private fun getAuthenticatedUserIdOrNull(): String? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null || !authentication.isAuthenticated) {
                null
            } else {
                (authentication.principal as? User)?.id
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère tous les salons d'un propriétaire.
     */
    @Operation(
        summary = "Récupérer les salons d'un propriétaire",
        description = "Retourne la liste de tous les salons appartenant à un propriétaire spécifique"
    )
    @GetMapping("/owner/{ownerId}")
    fun getSalonsByOwner(@PathVariable ownerId: String): ResponseEntity<List<SalonResponse>> {
        val salons = salonService.getSalonsByOwner(ownerId)
        return ResponseEntity.ok(salons)
    }

    /**
     * Récupère les salons les plus engagés (trending).
     * Phase C.3 - Trending Coiffure
     */
    @Operation(
        summary = "Récupérer les salons trending",
        description = "Retourne les salons les plus engagés basés sur l'engagement des posts tagués avec ces salons"
    )
    @GetMapping("/trending")
    fun getTrendingSalons(
        @Parameter(description = "Nombre de salons à retourner", example = "10")
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<SalonResponse>> {
        val salons = salonService.getTrendingSalons(limit)
        return ResponseEntity.ok(salons)
    }

    /**
     * Récupère les salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     */
    @Operation(
        summary = "Récupérer les salons à proximité",
        description = "Retourne les salons situés dans un rayon donné autour d'une position géographique, triés par distance croissante"
    )
    @GetMapping("/nearby")
    fun getSalonsNearby(
        @Parameter(description = "Latitude du point central", required = true, example = "48.8566")
        @RequestParam lat: Double,
        @Parameter(description = "Longitude du point central", required = true, example = "2.3522")
        @RequestParam lng: Double,
        @Parameter(description = "Rayon de recherche en kilomètres", example = "10")
        @RequestParam(defaultValue = "10.0") radius: Double
    ): ResponseEntity<List<SalonResponse>> {
        val salons = salonService.getSalonsNearby(lat, lng, radius)
        return ResponseEntity.ok(salons)
    }

    /**
     * Met à jour la photo de couverture d'un salon.
     */
    @Operation(
        summary = "Mettre à jour la photo de couverture",
        description = "Met à jour la photo de couverture d'un salon. Seul le propriétaire peut effectuer cette action."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Photo de couverture mise à jour avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Utilisateur non autorisé (pas le propriétaire)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Salon non trouvé"
            )
        ]
    )
    @PutMapping("/{salonId}/cover-photo")
    @PreAuthorize("isAuthenticated()")
    fun updateSalonCoverPhoto(
        @PathVariable salonId: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Any> {
        return try {
            val authenticatedUserId = getAuthenticatedUserId()
            val coverPhotoUrl = request["coverPhotoUrl"]
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "coverPhotoUrl est requis"))

            val updatedSalon = salonService.updateSalonCoverPhoto(salonId, coverPhotoUrl, authenticatedUserId)
            ResponseEntity.ok(updatedSalon)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "Salon non trouvé")))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to (e.message ?: "Accès refusé")))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erreur serveur"))
        }
    }

    // ========== PERMISSIONS ==========

    @Operation(
        summary = "Mes permissions sur ce salon",
        description = "Retourne le rôle et la liste des permissions de l'appelant pour ce salon"
    )
    @GetMapping("/{salonId}/my-permissions")
    @PreAuthorize("isAuthenticated()")
    fun getMyPermissions(
        @PathVariable salonId: String
    ): ResponseEntity<Map<String, Any>> {
        val userId = getAuthenticatedUserId()
        val role = salonAuthorizationService.getUserRole(userId, salonId)
            ?: return ResponseEntity.ok(mapOf(
                "role" to "none",
                "permissions" to emptyList<String>()
            ))
        val permissions = salonAuthorizationService.getUserPermissions(userId, salonId)
        return ResponseEntity.ok(mapOf(
            "role" to role,
            "permissions" to permissions.sorted()
        ))
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
}