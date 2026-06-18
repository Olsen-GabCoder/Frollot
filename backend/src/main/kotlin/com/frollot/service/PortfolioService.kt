package com.frollot.service

import com.frollot.dto.*
import com.frollot.model.*
import com.frollot.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service de gestion des portfolios coiffeur/salon.
 * 
 * Gère :
 * - Création et modification de portfolios
 * - Ajout/retrait de posts dans les portfolios
 * - Réorganisation des posts
 * - Récupération des portfolios publics et privés
 */
@Service
@Transactional
class PortfolioService(
    private val portfolioRepository: PortfolioRepository,
    private val portfolioPostRepository: PortfolioPostRepository,
    private val userRepository: UserRepository,
    private val salonRepository: SalonRepository,
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postFavoriteRepository: PostFavoriteRepository,
    private val commentRepository: CommentRepository,
    private val postTagRepository: PostTagRepository,
    private val postServiceRepository: PostServiceRepository,
    private val postHashtagRepository: PostHashtagRepository,
    private val salonAuthorizationService: SalonAuthorizationService
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class PortfolioNotFoundException(portfolioId: String) :
        RuntimeException("Portfolio avec ID '$portfolioId' non trouvé")

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé à accéder à ce portfolio")

    class PostNotFoundException(postId: String) :
        RuntimeException("Post avec ID '$postId' non trouvé")

    class PostAlreadyInPortfolioException(postId: String, portfolioId: String) :
        RuntimeException("Le post '$postId' est déjà dans le portfolio '$portfolioId'")

    class InvalidOwnerException(message: String) : RuntimeException(message)

    // ========== GESTION DES PORTFOLIOS ==========

    /**
     * Crée un nouveau portfolio.
     */
    @Transactional
    fun createPortfolio(request: CreatePortfolioRequest, userId: String): PortfolioResponse {
        // Validation
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Le nom du portfolio ne peut pas être vide")
        }

        // Vérifier que l'utilisateur existe
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }

        // Déterminer le type de propriétaire et valider
        val ownerType = when (request.ownerType) {
            PortfolioOwnerType.coiffeur -> {
                if (user.userType != UserType.hairstylist) {
                    throw InvalidOwnerException("L'utilisateur doit être de type 'hairstylist' pour créer un portfolio coiffeur")
                }
                PortfolioOwnerType.coiffeur
            }
            PortfolioOwnerType.salon -> {
                // Vérifier que le salon existe
                if (!salonRepository.existsById(request.ownerId)) {
                    throw InvalidOwnerException("Salon avec ID '${request.ownerId}' non trouvé")
                }
                // Vérification des autorisations via le système de permissions
                salonAuthorizationService.requirePermission(userId, request.ownerId, "portfolio.create")
                PortfolioOwnerType.salon
            }
        }

        // Vérifier que l'ownerId correspond
        val finalOwnerId = when (request.ownerType) {
            PortfolioOwnerType.coiffeur -> userId
            PortfolioOwnerType.salon -> request.ownerId
        }

        // Création du portfolio
        val portfolio = Portfolio(
            id = UUID.randomUUID().toString(),
            ownerId = finalOwnerId,
            ownerType = ownerType,
            name = request.name.trim(),
            description = request.description?.trim()?.takeIf { it.isNotBlank() },
            coverImageUrl = request.coverImageUrl?.trim()?.takeIf { it.isNotBlank() },
            isPublic = request.isPublic
        )

        // Validation
        if (!portfolio.isValid()) {
            throw IllegalArgumentException("Les données du portfolio sont invalides")
        }

        // Sauvegarde
        val savedPortfolio = portfolioRepository.save(portfolio)

        println("✅ Portfolio créé: ${savedPortfolio.id} par $userId (${savedPortfolio.ownerType})")

        return PortfolioResponse.fromEntity(savedPortfolio, 0)
    }

    /**
     * Met à jour un portfolio.
     */
    @Transactional
    fun updatePortfolio(portfolioId: String, request: UpdatePortfolioRequest, userId: String): PortfolioResponse {
        // Récupérer le portfolio
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier les autorisations
        requirePortfolioMutationAccess(portfolio, userId, "portfolio.update")

        // Mise à jour
        request.name?.let { portfolio.name = it.trim() }
        request.description?.let { portfolio.description = it.trim().takeIf { desc -> desc.isNotBlank() } }
        request.coverImageUrl?.let { portfolio.coverImageUrl = it.trim().takeIf { url -> url.isNotBlank() } }
        request.isPublic?.let { portfolio.isPublic = it }

        // Validation
        if (!portfolio.isValid()) {
            throw IllegalArgumentException("Les données du portfolio sont invalides")
        }

        // Sauvegarde
        val savedPortfolio = portfolioRepository.save(portfolio)
        val postsCount = portfolioPostRepository.countByPortfolioId(portfolioId).toInt()

        println("✅ Portfolio mis à jour: $portfolioId")

        return PortfolioResponse.fromEntity(savedPortfolio, postsCount)
    }

    /**
     * Supprime un portfolio.
     */
    @Transactional
    fun deletePortfolio(portfolioId: String, userId: String) {
        // Récupérer le portfolio
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier les autorisations (owner SEUL pour delete)
        requirePortfolioMutationAccess(portfolio, userId, "portfolio.delete")

        // Suppression (les portfolio_posts seront supprimés en cascade)
        portfolioRepository.delete(portfolio)

        println("🗑️ Portfolio supprimé: $portfolioId")
    }

    /**
     * Récupère un portfolio par son ID.
     */
    @Transactional(readOnly = true)
    fun getPortfolioById(portfolioId: String, userId: String? = null): PortfolioResponse {
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier l'accès (public ou propriétaire)
        if (!portfolio.isPublic && (userId == null || !hasAccessToPortfolio(portfolio, userId))) {
            throw UnauthorizedAccessException(userId ?: "anonymous")
        }

        val postsCount = portfolioPostRepository.countByPortfolioId(portfolioId).toInt()

        return PortfolioResponse.fromEntity(portfolio, postsCount)
    }

    /**
     * Récupère tous les portfolios d'un propriétaire.
     */
    @Transactional(readOnly = true)
    fun getPortfoliosByOwner(
        ownerId: String,
        ownerType: PortfolioOwnerType,
        userId: String? = null,
        includePrivate: Boolean = false
    ): List<PortfolioResponse> {
        // Si l'utilisateur demande ses propres portfolios, inclure les privés
        val isOwner = when (ownerType) {
            PortfolioOwnerType.coiffeur -> userId == ownerId
            PortfolioOwnerType.salon -> {
                val salon = salonRepository.findById(ownerId).orElse(null)
                salon?.owner?.id == userId
            }
        }

        val portfolios = if (includePrivate && isOwner) {
            portfolioRepository.findByOwnerIdAndOwnerTypeOrderByCreatedAtDesc(ownerId, ownerType)
        } else {
            portfolioRepository.findByOwnerIdAndOwnerTypeAndIsPublicTrueOrderByCreatedAtDesc(ownerId, ownerType)
        }

        return portfolios.map { portfolio ->
            val postsCount = portfolioPostRepository.countByPortfolioId(portfolio.id!!).toInt()
            PortfolioResponse.fromEntity(portfolio, postsCount)
        }
    }

    // ========== GESTION DES POSTS DANS LES PORTFOLIOS ==========

    /**
     * Ajoute un post à un portfolio.
     */
    @Transactional
    fun addPostToPortfolio(portfolioId: String, postId: String, userId: String): PortfolioPostResponse {
        // Récupérer le portfolio
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier les autorisations
        requirePortfolioMutationAccess(portfolio, userId, "portfolio.manage_posts")

        // Vérifier que le post existe
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérifier que le post n'est pas déjà dans le portfolio
        if (portfolioPostRepository.existsByPortfolioIdAndPostId(portfolioId, postId)) {
            throw PostAlreadyInPortfolioException(postId, portfolioId)
        }

        // Déterminer le prochain index d'ordre
        val maxOrderIndex = portfolioPostRepository.findByPortfolioIdOrderByOrderIndexAscAddedAtAsc(portfolioId)
            .maxOfOrNull { it.orderIndex } ?: -1

        // Création de l'association
        val portfolioPost = PortfolioPost(
            id = UUID.randomUUID().toString(),
            portfolio = portfolio,
            post = post,
            orderIndex = maxOrderIndex + 1
        )

        // Validation
        if (!portfolioPost.isValid()) {
            throw IllegalArgumentException("Les données de l'association sont invalides")
        }

        // Sauvegarde
        val savedPortfolioPost = portfolioPostRepository.save(portfolioPost)

        println("✅ Post ajouté au portfolio: $postId -> $portfolioId (ordre: ${savedPortfolioPost.orderIndex})")

        return PortfolioPostResponse.fromEntity(savedPortfolioPost)
    }

    /**
     * Retire un post d'un portfolio.
     */
    @Transactional
    fun removePostFromPortfolio(portfolioId: String, postId: String, userId: String) {
        // Récupérer le portfolio
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier les autorisations
        requirePortfolioMutationAccess(portfolio, userId, "portfolio.manage_posts")

        // Suppression
        portfolioPostRepository.deleteByPortfolioIdAndPostId(portfolioId, postId)

        println("🗑️ Post retiré du portfolio: $postId <- $portfolioId")
    }

    /**
     * Récupère tous les posts d'un portfolio.
     */
    @Transactional(readOnly = true)
    fun getPortfolioPosts(portfolioId: String, pageable: Pageable, userId: String? = null): Page<PostResponse> {
        // Récupérer le portfolio
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier l'accès
        if (!portfolio.isPublic && (userId == null || !hasAccessToPortfolio(portfolio, userId))) {
            throw UnauthorizedAccessException(userId ?: "anonymous")
        }

        // Récupérer les posts
        val allPosts = portfolioPostRepository.findPostsByPortfolioId(portfolioId)
            .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)

        // Pagination manuelle
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, allPosts.size)
        val paginatedPosts = if (start < allPosts.size) {
            allPosts.subList(start, end)
        } else {
            emptyList()
        }

        val page = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            allPosts.size.toLong()
        )

        // Convertir en PostResponse avec toutes les données associées
        return page.map { post ->
            val postId = post.id!!
            val isLiked = userId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = userId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { TagResponse.fromEntity(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            PostResponse.fromEntity(
                post,
                isLikedByCurrentUser = isLiked,
                isFavoritedByCurrentUser = isFavorited,
                isSharedByCurrentUser = false,
                commentsCount = commentsCount,
                sharesCount = 0,
                tags = tags,
                services = services,
                hashtags = hashtags,
                media = emptyList()
            )
        }
    }

    /**
     * Réorganise les posts d'un portfolio.
     */
    @Transactional
    fun reorderPortfolioPosts(portfolioId: String, postIds: List<String>, userId: String) {
        // Récupérer le portfolio
        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow { PortfolioNotFoundException(portfolioId) }

        // Vérifier les autorisations
        requirePortfolioMutationAccess(portfolio, userId, "portfolio.manage_posts")

        // Mettre à jour l'ordre de chaque post
        postIds.forEachIndexed { index, postId ->
            val portfolioPost = portfolioPostRepository.findByPortfolioIdOrderByOrderIndexAscAddedAtAsc(portfolioId)
                .find { it.post?.id == postId }
            
            if (portfolioPost != null) {
                portfolioPost.orderIndex = index
                portfolioPostRepository.save(portfolioPost)
            }
        }

        println("✅ Ordre des posts mis à jour pour le portfolio: $portfolioId")
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Vérifie si un utilisateur a accès à un portfolio (lecture privée / ownership simple).
     */
    private fun hasAccessToPortfolio(portfolio: Portfolio, userId: String): Boolean {
        return when (portfolio.ownerType) {
            PortfolioOwnerType.coiffeur -> portfolio.ownerId == userId
            PortfolioOwnerType.salon -> salonAuthorizationService.hasPermission(userId, portfolio.ownerId, "portfolio.update")
        }
    }

    /**
     * Vérifie l'accès mutation sur un portfolio avec la permission exacte.
     * Coiffeur perso = propriétaire direct. Salon = requirePermission.
     */
    private fun requirePortfolioMutationAccess(portfolio: Portfolio, userId: String, permission: String) {
        when (portfolio.ownerType) {
            PortfolioOwnerType.coiffeur -> {
                if (portfolio.ownerId != userId) {
                    throw UnauthorizedAccessException(userId)
                }
            }
            PortfolioOwnerType.salon -> {
                salonAuthorizationService.requirePermission(userId, portfolio.ownerId, permission)
            }
        }
    }
}

