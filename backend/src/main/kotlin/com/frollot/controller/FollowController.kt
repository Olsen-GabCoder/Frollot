package com.frollot.controller

import com.frollot.dto.FollowResponse
import com.frollot.dto.UserResponse
import com.frollot.model.FollowingType
import com.frollot.model.User
import com.frollot.service.FollowService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Controller pour la gestion des relations de suivi.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 */
@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = ["http://localhost:3000", "http://10.0.2.2:8090"])
@Tag(
    name = "Follow",
    description = "API de gestion des relations de suivi (salons, coiffeurs)"
)
class FollowController(
    private val followService: FollowService
) {

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

    /**
     * Récupère l'ID de l'utilisateur authentifié ou null si non authentifié.
     */
    private fun getAuthenticatedUserIdOrNull(): String? {
        return try {
            getAuthenticatedUserId()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Suit un salon.
     */
    @Operation(
        summary = "Suivre un salon",
        description = "Permet à un utilisateur authentifié de suivre un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Salon suivi avec succès"),
            ApiResponse(responseCode = "400", description = "Salon déjà suivi ou salon introuvable"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PostMapping("/salons/{salonId}/follow")
    fun followSalon(
        @Parameter(description = "ID du salon à suivre", required = true)
        @PathVariable salonId: String
    ): ResponseEntity<FollowResponse> {
        val followerId = getAuthenticatedUserId()
        val follow = followService.followSalon(followerId, salonId)
        return ResponseEntity.ok(follow)
    }

    /**
     * Ne plus suivre un salon.
     */
    @Operation(
        summary = "Ne plus suivre un salon",
        description = "Permet à un utilisateur authentifié de ne plus suivre un salon"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Salon non suivi avec succès"),
            ApiResponse(responseCode = "400", description = "Salon non suivi"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @DeleteMapping("/salons/{salonId}/follow")
    fun unfollowSalon(
        @Parameter(description = "ID du salon à ne plus suivre", required = true)
        @PathVariable salonId: String
    ): ResponseEntity<Map<String, String>> {
        val followerId = getAuthenticatedUserId()
        followService.unfollow(followerId, FollowingType.SALON, salonId)
        return ResponseEntity.ok(mapOf("message" to "Salon non suivi avec succès"))
    }

    /**
     * Suit un coiffeur.
     */
    @Operation(
        summary = "Suivre un coiffeur",
        description = "Permet à un utilisateur authentifié de suivre un coiffeur (User avec userType = hairstylist)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Coiffeur suivi avec succès"),
            ApiResponse(responseCode = "400", description = "Coiffeur déjà suivi, n'est pas un coiffeur, ou introuvable"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PostMapping("/coiffeurs/{coiffeurId}/follow")
    fun followCoiffeur(
        @Parameter(description = "ID du coiffeur à suivre", required = true)
        @PathVariable coiffeurId: String
    ): ResponseEntity<FollowResponse> {
        val followerId = getAuthenticatedUserId()
        val follow = followService.followCoiffeur(followerId, coiffeurId)
        return ResponseEntity.ok(follow)
    }

    /**
     * Ne plus suivre un coiffeur.
     */
    @Operation(
        summary = "Ne plus suivre un coiffeur",
        description = "Permet à un utilisateur authentifié de ne plus suivre un coiffeur"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Coiffeur non suivi avec succès"),
            ApiResponse(responseCode = "400", description = "Coiffeur non suivi"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @DeleteMapping("/coiffeurs/{coiffeurId}/follow")
    fun unfollowCoiffeur(
        @Parameter(description = "ID du coiffeur à ne plus suivre", required = true)
        @PathVariable coiffeurId: String
    ): ResponseEntity<Map<String, String>> {
        val followerId = getAuthenticatedUserId()
        followService.unfollow(followerId, FollowingType.COIFFEUR, coiffeurId)
        return ResponseEntity.ok(mapOf("message" to "Coiffeur non suivi avec succès"))
    }

    /**
     * Récupère la liste des entités suivies par un utilisateur.
     */
    @Operation(
        summary = "Récupérer les entités suivies",
        description = "Retourne la liste de toutes les entités (salons, coiffeurs) suivies par un utilisateur"
    )
    @GetMapping("/users/{userId}/following")
    fun getFollowing(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String
    ): ResponseEntity<List<FollowResponse>> {
        val following = followService.getFollowing(userId)
        return ResponseEntity.ok(following)
    }

    /**
     * Récupère la liste des followers d'un salon.
     */
    @Operation(
        summary = "Récupérer les followers d'un salon",
        description = "Retourne la liste des utilisateurs qui suivent un salon"
    )
    @GetMapping("/salons/{salonId}/followers")
    fun getSalonFollowers(
        @Parameter(description = "ID du salon", required = true)
        @PathVariable salonId: String
    ): ResponseEntity<List<UserResponse>> {
        val followers = followService.getFollowers(FollowingType.SALON, salonId)
        return ResponseEntity.ok(followers)
    }

    /**
     * Récupère la liste des followers d'un coiffeur.
     */
    @Operation(
        summary = "Récupérer les followers d'un coiffeur",
        description = "Retourne la liste des utilisateurs qui suivent un coiffeur"
    )
    @GetMapping("/coiffeurs/{coiffeurId}/followers")
    fun getCoiffeurFollowers(
        @Parameter(description = "ID du coiffeur", required = true)
        @PathVariable coiffeurId: String
    ): ResponseEntity<List<UserResponse>> {
        val followers = followService.getFollowers(FollowingType.COIFFEUR, coiffeurId)
        return ResponseEntity.ok(followers)
    }
}

