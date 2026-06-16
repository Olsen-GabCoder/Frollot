package com.frollot.dto

import java.math.BigDecimal
import com.frollot.model.Comment
import com.frollot.model.HairHashtag
import com.frollot.model.HairHashtagCategory
import com.frollot.model.Portfolio
import com.frollot.model.PortfolioOwnerType
import com.frollot.model.PortfolioPost
import com.frollot.model.Post
import com.frollot.model.PostLike
import com.frollot.model.PostMedia
import com.frollot.model.PostShare
import com.frollot.model.PostMediaType
import com.frollot.model.PostTag
import com.frollot.model.PostType
import com.frollot.model.ReactionType
import com.frollot.model.TaggedType
import java.time.LocalDateTime
import kotlin.collections.map

// ============================================
// REQUEST DTOs (Client → Serveur)
// ============================================

/**
 * DTO pour créer un média associé à un post.
 */
data class CreatePostMediaRequest(
    val mediaUrl: String,
    val mediaType: PostMediaType,
    val orderIndex: Int = 0
)

/**
 * DTO pour créer un nouveau post.
 */
data class CreatePostRequest(
    val authorId: String,
    val content: String,
    val imageUrl: String? = null, // Image principale (rétrocompatibilité)
    val postType: PostType = PostType.GENERAL,
    val visibility: com.frollot.model.PostVisibility = com.frollot.model.PostVisibility.PUBLIC, // Phase F.3 - Visibilité des Posts
    val tags: List<CreateTagRequest> = emptyList(),
    val serviceIds: List<String> = emptyList(),
    val media: List<CreatePostMediaRequest> = emptyList() // Médias multiples (pour AVANT_APRES)
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (content.isBlank()) {
            throw IllegalArgumentException("Le contenu du post ne peut pas être vide")
        }
        if (content.length > 5000) {
            throw IllegalArgumentException("Le contenu du post ne peut pas dépasser 5000 caractères")
        }
        if (imageUrl != null && imageUrl.length > 500) {
            throw IllegalArgumentException("L'URL de l'image ne peut pas dépasser 500 caractères")
        }
    }
}

/**
 * DTO pour créer un nouveau commentaire.
 */
data class CreateCommentRequest(
    val postId: String,
    val authorId: String,
    val content: String
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (content.isBlank()) {
            throw IllegalArgumentException("Le contenu du commentaire ne peut pas être vide")
        }
        if (content.length > 1000) {
            throw IllegalArgumentException("Le contenu du commentaire ne peut pas dépasser 1000 caractères")
        }
    }
}

/**
 * DTO pour partager un post avec un commentaire optionnel.
 * Phase D.3 - Partage de Posts (Reposts)
 */
data class SharePostRequest(
    val sharedContent: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (sharedContent != null && sharedContent.length > 500) {
            throw IllegalArgumentException("Le commentaire de partage ne peut pas dépasser 500 caractères")
        }
    }
}

/**
 * DTO pour ajouter une réaction à un post.
 * Phase D.4 - Réactions Spécialisées Coiffure
 */
data class AddReactionRequest(
    val reactionType: com.frollot.model.ReactionType
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        // La validation est gérée par l'enum ReactionType
    }
}

/**
 * DTO pour créer un tag/mention dans un post.
 */
data class CreateTagRequest(
    val taggedType: TaggedType,
    val taggedId: String
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (taggedId.isBlank()) {
            throw IllegalArgumentException("L'ID de l'entité taguée ne peut pas être vide")
        }
    }
}

// ============================================
// SEARCH DTOs (Phase C.1 - Recherche spécialisée coiffure)
// ============================================

/**
 * Filtres de recherche pour les posts.
 * Phase C.1 - Recherche spécialisée coiffure
 */
data class SearchFilters(
    val postType: PostType? = null,
    val serviceId: String? = null,
    val salonId: String? = null,
    val hashtagName: String? = null,
    val authorId: String? = null
) {
    /**
     * Vérifie si au moins un filtre est défini.
     */
    fun hasAnyFilter(): Boolean {
        return postType != null || serviceId != null || salonId != null || hashtagName != null || authorId != null
    }
}

/**
 * Réponse de recherche unifiée.
 * Phase C.1 - Recherche spécialisée coiffure
 */
data class SearchResponse(
    val posts: List<PostResponse>,
    val salons: List<SalonResponse>,
    val users: List<UserResponse>,
    val hashtags: List<HairHashtagResponse>,
    val totalPosts: Int,
    val totalSalons: Int,
    val totalUsers: Int,
    val totalHashtags: Int
)

// ============================================
// RESPONSE DTOs (Serveur → Client)
// ============================================

/**
 * DTO de réponse pour un média associé à un post.
 */
data class PostMediaResponse(
    val id: String,
    val mediaUrl: String,
    val mediaType: PostMediaType,
    val mediaTypeLabel: String,
    val orderIndex: Int,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité PostMedia en PostMediaResponse.
         */
        fun fromEntity(media: PostMedia): PostMediaResponse {
            return PostMediaResponse(
                id = media.id!!,
                mediaUrl = media.mediaUrl,
                mediaType = media.mediaType,
                mediaTypeLabel = media.mediaType.getDisplayName(),
                orderIndex = media.orderIndex,
                createdAt = media.createdAt
            )
        }

        /**
         * Convertit une liste d'entités en liste de DTOs.
         */
        fun fromEntities(mediaList: List<PostMedia>): List<PostMediaResponse> {
            return mediaList.map { fromEntity(it) }
        }
    }
}

/**
 * DTO de réponse pour un post complet.
 */
data class PostResponse(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorEmail: String,
    val authorUserType: com.frollot.model.UserType? = null, // Type d'utilisateur de l'auteur
    val authorAvatarUrl: String? = null, // Avatar de l'auteur
    val content: String,
    val imageUrl: String?, // Image principale (rétrocompatibilité)
    val postType: PostType,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int = 0, // Phase D.3 - Partage de Posts
    val isLikedByCurrentUser: Boolean = false,
    val isFavoritedByCurrentUser: Boolean = false,
    val isSharedByCurrentUser: Boolean = false, // Phase D.3 - Partage de Posts
    val reactions: Map<String, Int> = emptyMap(), // Phase D.4 - Réactions Spécialisées (clé: nom de ReactionType en lowercase)
    val currentUserReaction: ReactionType? = null, // Phase D.4 - Réaction de l'utilisateur courant
    val tags: List<TagResponse> = emptyList(),
    val services: List<ServiceResponse> = emptyList(),
    val hashtags: List<HairHashtagResponse> = emptyList(),
    val media: List<PostMediaResponse> = emptyList(), // Médias multiples (pour AVANT_APRES)
    val isPinned: Boolean = false, // Phase F.2 - Posts Épinglés
    val visibility: com.frollot.model.PostVisibility = com.frollot.model.PostVisibility.PUBLIC, // Phase F.3 - Visibilité des Posts
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité Post en PostResponse.
         */
        fun fromEntity(
            post: Post,
            isLikedByCurrentUser: Boolean = false,
            isFavoritedByCurrentUser: Boolean = false,
            isSharedByCurrentUser: Boolean = false, // Phase D.3
            commentsCount: Int = 0,
            sharesCount: Int = 0, // Phase D.3
            reactions: Map<ReactionType, Int> = emptyMap(), // Phase D.4 - Entrée: Map<ReactionType, Int>
            currentUserReaction: ReactionType? = null, // Phase D.4
            tags: List<TagResponse> = emptyList(),
            services: List<ServiceResponse> = emptyList(),
            hashtags: List<HairHashtagResponse> = emptyList(),
            media: List<PostMediaResponse> = emptyList()
        ): PostResponse {
            val author = post.author!!
            // Convertir Map<ReactionType, Int> en Map<String, Int> avec clés en lowercase
            // pour correspondre au format attendu par le frontend
            val reactionsMap = reactions.mapKeys { it.key.name.lowercase() }
            
            return PostResponse(
                id = post.id!!,
                authorId = author.id!!,
                authorName = post.getAuthorName(),
                authorEmail = author.email,
                authorUserType = author.userType, // Type d'utilisateur de l'auteur
                authorAvatarUrl = author.avatarUrl,
                content = post.content,
                imageUrl = post.imageUrl,
                postType = post.postType,
                likesCount = post.likesCount,
                commentsCount = commentsCount.takeIf { it > 0 } ?: post.commentsCount,
                sharesCount = sharesCount.takeIf { it > 0 } ?: post.sharesCount, // Phase D.3
                isLikedByCurrentUser = isLikedByCurrentUser,
                isFavoritedByCurrentUser = isFavoritedByCurrentUser,
                isSharedByCurrentUser = isSharedByCurrentUser, // Phase D.3
                reactions = reactionsMap, // Phase D.4 - Converti en Map<String, Int> avec clés lowercase
                currentUserReaction = currentUserReaction, // Phase D.4
                tags = tags,
                services = services,
                hashtags = hashtags,
                media = media,
                isPinned = post.isPinned, // Phase F.2 - Posts Épinglés
                visibility = post.visibility, // Phase F.3 - Visibilité des Posts
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
    }
}

/**
 * DTO de réponse pour un commentaire.
 */
data class CommentResponse(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorEmail: String,
    val content: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité Comment en CommentResponse.
         */
        fun fromEntity(comment: Comment): CommentResponse {
            val author = comment.author!!
            return CommentResponse(
                id = comment.id!!,
                postId = comment.post!!.id!!,
                authorId = author.id!!,
                authorName = comment.getAuthorName(),
                authorEmail = author.email,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt
            )
        }
    }
}

/**
 * DTO de réponse pour un like.
 */
data class LikeResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité PostLike en LikeResponse.
         */
        fun fromEntity(like: PostLike): LikeResponse {
            return LikeResponse(
                id = like.id!!,
                postId = like.post!!.id!!,
                userId = like.user!!.id!!,
                createdAt = like.createdAt
            )
        }
    }
}

/**
 * DTO de réponse pour un partage de post.
 * Phase D.3 - Partage de Posts (Reposts)
 */
data class PostShareResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val sharedContent: String?,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité PostShare en PostShareResponse.
         */
        fun fromEntity(share: PostShare): PostShareResponse {
            return PostShareResponse(
                id = share.id!!,
                postId = share.post!!.id!!,
                userId = share.user!!.id!!,
                userName = share.getUserName(),
                sharedContent = share.sharedContent,
                createdAt = share.createdAt
            )
        }
    }
}

/**
 * DTO de réponse pour une réaction spécialisée.
 * Phase D.4 - Réactions Spécialisées Coiffure
 */
data class ReactionResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val reactionType: ReactionType,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité PostReaction en ReactionResponse.
         */
        fun fromEntity(reaction: com.frollot.model.PostReaction): ReactionResponse {
            return ReactionResponse(
                id = reaction.id!!,
                postId = reaction.post!!.id!!,
                userId = reaction.user!!.id!!,
                userName = reaction.getUserName(),
                reactionType = reaction.reactionType,
                createdAt = reaction.createdAt
            )
        }
    }
}

/**
 * DTO de réponse pour un tag/mention.
 */
data class TagResponse(
    val id: String,
    val postId: String,
    val taggedType: TaggedType,
    val taggedId: String,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité PostTag en TagResponse.
         */
        fun fromEntity(tag: PostTag): TagResponse {
            return TagResponse(
                id = tag.id!!,
                postId = tag.post!!.id!!,
                taggedType = tag.taggedType,
                taggedId = tag.taggedId,
                createdAt = tag.createdAt
            )
        }
    }
}

/**
 * DTO de réponse pour un hashtag coiffure.
 */
data class HairHashtagResponse(
    val id: String,
    val name: String,
    val category: HairHashtagCategory,
    val categoryLabel: String,
    val categoryEmoji: String,
    val usageCount: Int,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité HairHashtag en HairHashtagResponse.
         */
        fun fromEntity(hashtag: HairHashtag): HairHashtagResponse {
            return HairHashtagResponse(
                id = hashtag.id!!,
                name = hashtag.name,
                category = hashtag.category,
                categoryLabel = hashtag.category.getDisplayName(),
                categoryEmoji = hashtag.category.getEmoji(),
                usageCount = hashtag.usageCount,
                createdAt = hashtag.createdAt
            )
        }

        /**
         * Convertit une liste d'entités en liste de DTOs.
         */
        fun fromEntities(hashtags: List<HairHashtag>): List<HairHashtagResponse> {
            return hashtags.map { fromEntity(it) }
        }
    }
}

// ============================================
// PORTFOLIO DTOs
// ============================================

/**
 * DTO pour créer un nouveau portfolio.
 */
data class CreatePortfolioRequest(
    val ownerType: PortfolioOwnerType,
    val ownerId: String, // Pour les salons, l'ID du salon. Pour les coiffeurs, ignoré (utilise userId de la session)
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true
)

/**
 * DTO pour mettre à jour un portfolio.
 */
data class UpdatePortfolioRequest(
    val name: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean? = null
)

/**
 * DTO de réponse pour un portfolio.
 */
data class PortfolioResponse(
    val id: String,
    val ownerId: String,
    val ownerType: PortfolioOwnerType,
    val ownerTypeLabel: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val isPublic: Boolean,
    val postsCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité Portfolio en PortfolioResponse.
         */
        fun fromEntity(portfolio: Portfolio, postsCount: Int = 0): PortfolioResponse {
            return PortfolioResponse(
                id = portfolio.id!!,
                ownerId = portfolio.ownerId,
                ownerType = portfolio.ownerType,
                ownerTypeLabel = portfolio.ownerType.getDisplayName(),
                name = portfolio.name,
                description = portfolio.description,
                coverImageUrl = portfolio.coverImageUrl,
                isPublic = portfolio.isPublic,
                postsCount = postsCount,
                createdAt = portfolio.createdAt,
                updatedAt = portfolio.updatedAt
            )
        }
    }
}

/**
 * DTO de réponse pour une association portfolio-post.
 */
data class PortfolioPostResponse(
    val id: String,
    val portfolioId: String,
    val postId: String,
    val orderIndex: Int,
    val addedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité PortfolioPost en PortfolioPostResponse.
         */
        fun fromEntity(portfolioPost: PortfolioPost): PortfolioPostResponse {
            return PortfolioPostResponse(
                id = portfolioPost.id!!,
                portfolioId = portfolioPost.portfolio!!.id!!,
                postId = portfolioPost.post!!.id!!,
                orderIndex = portfolioPost.orderIndex,
                addedAt = portfolioPost.addedAt
            )
        }
    }
}

// ============================================
// Phase E.1 - Profil Coiffeur Enrichi
// ============================================

/**
 * DTO pour mettre à jour le profil coiffeur.
 * Phase E.1 - Profil Coiffeur Enrichi
 */
data class UpdateCoiffeurProfileRequest(
    val bio: String? = null,
    val specialties: List<String> = emptyList(),
    val yearsExperience: Int? = null,
    val certifications: String? = null,
    val instagramHandle: String? = null,
    val portfolioHighlightedId: String? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (bio != null && bio.length > 1000) {
            throw IllegalArgumentException("La biographie ne peut pas dépasser 1000 caractères")
        }
        if (specialties.size > 5) {
            throw IllegalArgumentException("Le nombre de spécialités ne peut pas dépasser 5")
        }
        specialties.forEach { specialty ->
            if (specialty.isBlank()) {
                throw IllegalArgumentException("Une spécialité ne peut pas être vide")
            }
            if (specialty.length > 100) {
                throw IllegalArgumentException("Une spécialité ne peut pas dépasser 100 caractères")
            }
        }
        if (yearsExperience != null && (yearsExperience < 0 || yearsExperience > 100)) {
            throw IllegalArgumentException("Les années d'expérience doivent être entre 0 et 100")
        }
        if (certifications != null && certifications.length > 2000) {
            throw IllegalArgumentException("Les certifications ne peuvent pas dépasser 2000 caractères")
        }
        if (instagramHandle != null) {
            val handle = instagramHandle.trim().removePrefix("@")
            if (handle.length > 30) {
                throw IllegalArgumentException("Le handle Instagram ne peut pas dépasser 30 caractères")
            }
            if (!handle.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                throw IllegalArgumentException("Le handle Instagram contient des caractères invalides")
            }
        }
    }
}

/**
 * DTO pour les statistiques du profil coiffeur.
 * Phase E.1 - Profil Coiffeur Enrichi
 */
data class CoiffeurProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val averageRating: BigDecimal = BigDecimal.ZERO,
    val totalReviews: Int = 0
)

/**
 * DTO de réponse pour le profil coiffeur enrichi.
 * Phase E.1 - Profil Coiffeur Enrichi
 */
data class CoiffeurProfileResponse(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val coverImageUrl: String? = null, // Photo de couverture du profil
    val city: String? = null,
    val isVerified: Boolean,
    val bio: String?,
    val specialties: List<String>,
    val yearsExperience: Int?,
    val certifications: String?,
    val instagramHandle: String?,
    val portfolioHighlighted: PortfolioResponse?,
    val statistics: CoiffeurProfileStatistics,
    val portfolios: List<PortfolioResponse>,
    val recentPosts: List<PostResponse>,
    val badges: List<UserBadgeResponse> = emptyList(), // Phase E.3 - Badges et Certifications
    val isFollowedByCurrentUser: Boolean? = null
) {
    companion object {
        /**
         * Convertit un User en CoiffeurProfileResponse.
         * Note: Cette méthode nécessite des données supplémentaires (statistiques, portfolios, posts, badges)
         * qui doivent être fournies séparément.
         */
        fun fromUser(
            user: com.frollot.model.User,
            portfolioHighlighted: PortfolioResponse? = null,
            statistics: CoiffeurProfileStatistics = CoiffeurProfileStatistics(),
            portfolios: List<PortfolioResponse> = emptyList(),
            recentPosts: List<PostResponse> = emptyList(),
            badges: List<UserBadgeResponse> = emptyList(), // Phase E.3 - Badges et Certifications
            isFollowedByCurrentUser: Boolean? = null
        ): CoiffeurProfileResponse {
            return CoiffeurProfileResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                coverImageUrl = user.coverImageUrl,
                city = user.city,
                isVerified = user.isVerified,
                bio = user.bio,
                specialties = user.specialties.toList(),
                yearsExperience = user.yearsExperience,
                certifications = user.certifications,
                instagramHandle = user.instagramHandle,
                portfolioHighlighted = portfolioHighlighted,
                statistics = statistics,
                portfolios = portfolios,
                recentPosts = recentPosts,
                badges = badges,
                isFollowedByCurrentUser = isFollowedByCurrentUser
            )
        }
    }
}

// ============================================
// Phase E.2 - Profil Salon Social
// ============================================

// ============================================
// Phase E.4 - Profil Client
// ============================================

/**
 * DTO pour les statistiques du profil client.
 * Phase E.4 - Profil Client
 */
data class ClientProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val collectionsCount: Long = 0,
    val bookingsCount: Long = 0
)

/**
 * DTO de réponse pour le profil client enrichi.
 * Phase E.4 - Profil Client
 */
data class ClientProfileResponse(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val coverImageUrl: String? = null, // Photo de couverture du profil
    val city: String? = null,
    val isVerified: Boolean,
    val bio: String?,
    val statistics: ClientProfileStatistics,
    val recentPosts: List<PostResponse>,
    val collections: List<CollectionResponse>,
    val badges: List<UserBadgeResponse> = emptyList(),
    val isFollowedByCurrentUser: Boolean? = null
) {
    companion object {
        /**
         * Convertit un User en ClientProfileResponse.
         */
        fun fromUser(
            user: com.frollot.model.User,
            statistics: ClientProfileStatistics = ClientProfileStatistics(),
            recentPosts: List<PostResponse> = emptyList(),
            collections: List<CollectionResponse> = emptyList(),
            badges: List<UserBadgeResponse> = emptyList(),
            isFollowedByCurrentUser: Boolean? = null
        ): ClientProfileResponse {
            return ClientProfileResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                coverImageUrl = user.coverImageUrl,
                city = user.city,
                isVerified = user.isVerified,
                bio = user.bio,
                statistics = statistics,
                recentPosts = recentPosts,
                collections = collections,
                badges = badges,
                isFollowedByCurrentUser = isFollowedByCurrentUser
            )
        }
    }
}

// ============================================
// Phase E.5 - Profil Propriétaire de Salon
// ============================================

/**
 * DTO pour les statistiques du profil propriétaire de salon.
 * Phase E.5 - Profil Propriétaire de Salon
 */
data class SalonOwnerProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val salonsCount: Long = 0,
    val collectionsCount: Long = 0
)

/**
 * DTO de réponse pour le profil propriétaire de salon enrichi.
 * Phase E.5 - Profil Propriétaire de Salon
 */
data class SalonOwnerProfileResponse(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val coverImageUrl: String? = null, // Photo de couverture du profil
    val city: String? = null,
    val isVerified: Boolean,
    val bio: String?,
    val statistics: SalonOwnerProfileStatistics,
    val salons: List<SalonSummaryResponse>, // Liste des salons possédés
    val recentPosts: List<PostResponse>,
    val collections: List<CollectionResponse>,
    val badges: List<UserBadgeResponse> = emptyList(),
    val isFollowedByCurrentUser: Boolean? = null
) {
    companion object {
        /**
         * Convertit un User en SalonOwnerProfileResponse.
         */
        fun fromUser(
            user: com.frollot.model.User,
            statistics: SalonOwnerProfileStatistics = SalonOwnerProfileStatistics(),
            salons: List<SalonSummaryResponse> = emptyList(),
            recentPosts: List<PostResponse> = emptyList(),
            collections: List<CollectionResponse> = emptyList(),
            badges: List<UserBadgeResponse> = emptyList(),
            isFollowedByCurrentUser: Boolean? = null
        ): SalonOwnerProfileResponse {
            return SalonOwnerProfileResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                coverImageUrl = user.coverImageUrl,
                city = user.city,
                isVerified = user.isVerified,
                bio = user.bio,
                statistics = statistics,
                salons = salons,
                recentPosts = recentPosts,
                collections = collections,
                badges = badges,
                isFollowedByCurrentUser = isFollowedByCurrentUser
            )
        }
    }
}

/**
 * DTO de résumé de salon pour le profil propriétaire.
 */
data class SalonSummaryResponse(
    val id: String,
    val name: String,
    val city: String,
    val coverPhotoUrl: String?,
    val isVerified: Boolean,
    val followersCount: Long = 0
) {
    companion object {
        fun fromSalon(salon: com.frollot.model.Salon, followersCount: Long = 0): SalonSummaryResponse {
            return SalonSummaryResponse(
                id = salon.id!!,
                name = salon.name,
                city = salon.city,
                coverPhotoUrl = salon.coverPhotoUrl,
                isVerified = salon.isVerified,
                followersCount = followersCount
            )
        }
    }
}

// ============================================
// Phase E.2 - Profil Salon Social
// ============================================

/**
 * DTO pour mettre à jour le profil social salon.
 * Phase E.2 - Profil Salon Social
 */
data class UpdateSalonSocialProfileRequest(
    val socialDescription: String? = null,
    val socialCoverImage: String? = null,
    val highlightedPostIds: List<String> = emptyList()
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (socialDescription != null && socialDescription.length > 2000) {
            throw IllegalArgumentException("La description sociale ne peut pas dépasser 2000 caractères")
        }
        if (socialCoverImage != null && socialCoverImage.length > 500) {
            throw IllegalArgumentException("L'URL de l'image de couverture sociale ne peut pas dépasser 500 caractères")
        }
        if (highlightedPostIds.size > 10) {
            throw IllegalArgumentException("Le nombre de posts mis en avant ne peut pas dépasser 10")
        }
    }
}

/**
 * DTO pour les statistiques du profil social salon.
 * Phase E.2 - Profil Salon Social
 */
data class SalonSocialProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0,
    val averageRating: BigDecimal = BigDecimal.ZERO,
    val totalReviews: Int = 0
)

/**
 * DTO de réponse pour le profil social salon enrichi.
 * Phase E.2 - Profil Salon Social
 */
data class SalonSocialProfileResponse(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val slug: String,
    val coverPhotoUrl: String?,
    val socialDescription: String?,
    val socialCoverImage: String?,
    val highlightedPosts: List<PostResponse>,
    val statistics: SalonSocialProfileStatistics,
    val portfolios: List<PortfolioResponse>,
    val recentPosts: List<PostResponse>,
    val team: List<UserResponse>, // Profils simplifiés des coiffeurs
    val services: List<com.frollot.dto.ServiceResponse>,
    val isFollowedByCurrentUser: Boolean? = null,
    val isOwner: Boolean = false // Indique si l'utilisateur courant est le propriétaire du salon
) {
    companion object {
        /**
         * Convertit un Salon en SalonSocialProfileResponse.
         * Note: Cette méthode nécessite des données supplémentaires (statistiques, portfolios, posts, équipe, services)
         * qui doivent être fournies séparément.
         */
        fun fromSalon(
            salon: com.frollot.model.Salon,
            highlightedPosts: List<PostResponse> = emptyList(),
            statistics: SalonSocialProfileStatistics = SalonSocialProfileStatistics(),
            portfolios: List<PortfolioResponse> = emptyList(),
            recentPosts: List<PostResponse> = emptyList(),
            team: List<UserResponse> = emptyList(),
            services: List<com.frollot.dto.ServiceResponse> = emptyList(),
            isFollowedByCurrentUser: Boolean? = null,
            isOwner: Boolean = false
        ): SalonSocialProfileResponse {
            return SalonSocialProfileResponse(
                id = salon.id!!,
                name = salon.name,
                address = salon.address,
                city = salon.city,
                postalCode = salon.postalCode,
                slug = salon.slug,
                coverPhotoUrl = salon.coverPhotoUrl,
                socialDescription = salon.socialDescription,
                socialCoverImage = salon.socialCoverImage,
                highlightedPosts = highlightedPosts,
                statistics = statistics,
                portfolios = portfolios,
                recentPosts = recentPosts,
                team = team,
            services = services,
            isFollowedByCurrentUser = isFollowedByCurrentUser,
            isOwner = isOwner
            )
        }
    }
}

// ============================================
// Phase E.3 - Badges et Certifications
// ============================================

/**
 * DTO pour attribuer un badge à un utilisateur.
 * Phase E.3 - Badges et Certifications
 */
data class AwardBadgeRequest(
    val badgeId: String
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (badgeId.isBlank()) {
            throw IllegalArgumentException("L'ID du badge ne peut pas être vide")
        }
    }
}

/**
 * DTO de réponse pour un badge.
 * Phase E.3 - Badges et Certifications
 */
data class BadgeResponse(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val category: com.frollot.model.BadgeCategory
) {
    companion object {
        /**
         * Convertit une entité Badge en BadgeResponse.
         */
        fun fromEntity(badge: com.frollot.model.Badge): BadgeResponse {
            return BadgeResponse(
                id = badge.id!!,
                name = badge.name,
                description = badge.description,
                iconUrl = badge.iconUrl,
                category = badge.category
            )
        }
    }
}

/**
 * DTO de réponse pour l'association user-badge.
 * Phase E.3 - Badges et Certifications
 */
data class UserBadgeResponse(
    val id: String,
    val badge: BadgeResponse,
    val earnedAt: LocalDateTime?,
    val isDisplayed: Boolean
) {
    companion object {
        /**
         * Convertit une entité UserBadge en UserBadgeResponse.
         */
        fun fromEntity(userBadge: com.frollot.model.UserBadge): UserBadgeResponse {
            return UserBadgeResponse(
                id = userBadge.id!!,
                badge = BadgeResponse.fromEntity(userBadge.badge!!),
                earnedAt = userBadge.earnedAt,
                isDisplayed = userBadge.isDisplayed
            )
        }
    }
}

// ============================================
// Phase F.1 - Collections Thématiques
// ============================================

/**
 * DTO pour créer une nouvelle collection.
 * Phase F.1 - Collections Thématiques
 */
data class CreateCollectionRequest(
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true,
    val category: com.frollot.model.CollectionCategory
) {
    /**
     * Valide les données de la requête.
     */
    fun validate(): String? {
        if (name.isBlank()) {
            return "Le nom de la collection est requis"
        }
        if (name.length > 200) {
            return "Le nom de la collection ne peut pas dépasser 200 caractères"
        }
        if (description != null && description.length > 2000) {
            return "La description ne peut pas dépasser 2000 caractères"
        }
        return null
    }
}

/**
 * DTO pour mettre à jour une collection.
 * Phase F.1 - Collections Thématiques
 */
data class UpdateCollectionRequest(
    val name: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean? = null,
    val category: com.frollot.model.CollectionCategory? = null
) {
    /**
     * Valide les données de la requête.
     */
    fun validate(): String? {
        name?.let {
            if (it.isBlank()) {
                return "Le nom de la collection ne peut pas être vide"
            }
            if (it.length > 200) {
                return "Le nom de la collection ne peut pas dépasser 200 caractères"
            }
        }
        description?.let {
            if (it.length > 2000) {
                return "La description ne peut pas dépasser 2000 caractères"
            }
        }
        return null
    }
}

/**
 * DTO de réponse pour une collection.
 * Phase F.1 - Collections Thématiques
 */
data class CollectionResponse(
    val id: String,
    val userId: String,
    val userName: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val isPublic: Boolean,
    val category: com.frollot.model.CollectionCategory,
    val postsCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité Collection en CollectionResponse.
         */
        fun fromEntity(
            collection: com.frollot.model.Collection,
            postsCount: Int = 0
        ): CollectionResponse {
            val user = collection.user!!
            val userName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                .ifBlank { user.email }
            return CollectionResponse(
                id = collection.id!!,
                userId = user.id!!,
                userName = userName,
                name = collection.name,
                description = collection.description,
                coverImageUrl = collection.coverImageUrl,
                isPublic = collection.isPublic,
                category = collection.category,
                postsCount = postsCount,
                createdAt = collection.createdAt,
                updatedAt = collection.updatedAt
            )
        }
    }
}

/**
 * DTO de réponse pour l'association collection-post.
 * Phase F.1 - Collections Thématiques
 */
data class CollectionPostResponse(
    val id: String,
    val collectionId: String,
    val post: PostResponse,
    val orderIndex: Int,
    val addedAt: LocalDateTime?
) {
    companion object {
        /**
         * Convertit une entité CollectionPost en CollectionPostResponse.
         */
        fun fromEntity(
            collectionPost: com.frollot.model.CollectionPost,
            postResponse: PostResponse
        ): CollectionPostResponse {
            return CollectionPostResponse(
                id = collectionPost.id!!,
                collectionId = collectionPost.collection!!.id!!,
                post = postResponse,
                orderIndex = collectionPost.orderIndex,
                addedAt = collectionPost.addedAt
            )
        }
    }
}

