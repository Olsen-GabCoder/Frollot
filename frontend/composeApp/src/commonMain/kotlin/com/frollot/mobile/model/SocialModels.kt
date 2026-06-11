@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.frollot.mobile.model

import kotlinx.datetime.*
import kotlinx.serialization.Serializable

// ============================================
// REQUEST DTOs
// ============================================

/**
 * Énumération des types de posts spécialisés pour l'univers de la coiffure.
 */
@Serializable
enum class PostType {
    GENERAL,
    AVANT_APRES,
    PORTFOLIO,
    TENDANCE,
    CONSEIL,
    REALISATION,
    INSPIRATION;

    /**
     * Retourne le libellé utilisateur du type de post.
     */
    fun getDisplayName(): String {
        return when (this) {
            GENERAL -> "Général"
            AVANT_APRES -> "Avant/Après"
            PORTFOLIO -> "Portfolio"
            TENDANCE -> "Tendance"
            CONSEIL -> "Conseil"
            REALISATION -> "Réalisation"
            INSPIRATION -> "Inspiration"
        }
    }

    /**
     * Retourne l'emoji associé au type de post.
     */
    fun getEmoji(): String {
        return when (this) {
            GENERAL -> "📝"
            AVANT_APRES -> "✨"
            PORTFOLIO -> "🎨"
            TENDANCE -> "🔥"
            CONSEIL -> "💡"
            REALISATION -> "✂️"
            INSPIRATION -> "💫"
        }
    }

    /**
     * Retourne la description du type de post.
     */
    fun getDescription(): String {
        return when (this) {
            GENERAL -> "Post général"
            AVANT_APRES -> "Montrer une transformation avant/après"
            PORTFOLIO -> "Ajouter à votre portfolio"
            TENDANCE -> "Partager une tendance coiffure"
            CONSEIL -> "Donner des conseils et astuces"
            REALISATION -> "Montrer une réalisation"
            INSPIRATION -> "Partager une inspiration"
        }
    }
}

/**
 * Enum pour le tri des posts.
 * Phase C.2 - Feed par Salon
 */
@Serializable
enum class SortBy {
    RECENT,    // Plus récents en premier
    POPULAR    // Plus populaires en premier (likes + commentaires + shares)
}

/**
 * Enum pour les périodes de trending.
 * Phase C.3 - Trending Coiffure
 */
@Serializable
enum class TrendPeriod {
    LAST_24H,    // Dernières 24 heures
    LAST_7D,     // 7 derniers jours
    LAST_30D     // 30 derniers jours
}

/**
 * Énumération des niveaux de visibilité des posts.
 * Phase F.3 - Visibilité des Posts (Public/Followers/Private)
 */
@Serializable
enum class PostVisibility {
    PUBLIC,      // Post public : visible par tous
    FOLLOWERS,   // Post visible uniquement par les followers
    PRIVATE;     // Post privé : visible uniquement par l'auteur

    /**
     * Retourne le libellé utilisateur du niveau de visibilité.
     */
    fun getDisplayName(): String {
        return when (this) {
            PUBLIC -> "Public"
            FOLLOWERS -> "Abonnés uniquement"
            PRIVATE -> "Privé"
        }
    }

    /**
     * Retourne l'emoji associé au niveau de visibilité.
     */
    fun getEmoji(): String {
        return when (this) {
            PUBLIC -> "🌐"
            FOLLOWERS -> "👥"
            PRIVATE -> "🔒"
        }
    }

    /**
     * Retourne la description du niveau de visibilité.
     */
    fun getDescription(): String {
        return when (this) {
            PUBLIC -> "Visible par tous"
            FOLLOWERS -> "Visible uniquement par vos abonnés"
            PRIVATE -> "Visible uniquement par vous"
        }
    }
}

/**
 * Enum pour les types d'entités pouvant être suivies.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 */
@Serializable
enum class FollowingType {
    USER,      // Suivre un utilisateur (client)
    SALON,     // Suivre un salon
    COIFFEUR   // Suivre un coiffeur (User avec userType = hairstylist)
}

/**
 * DTO pour représenter une relation de suivi.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 */
@Serializable
data class FollowResponse(
    val id: String,
    val followerId: String,
    val followingType: FollowingType,
    val followingId: String,
    val createdAt: String?
)

/**
 * DTO pour créer un nouveau post.
 */
/**
 * DTO pour créer un média associé à un post.
 */
@Serializable
data class CreatePostMediaRequest(
    val mediaUrl: String,
    val mediaType: PostMediaType,
    val orderIndex: Int = 0
)

@Serializable
data class CreatePostRequest(
    val authorId: String,
    val content: String,
    val imageUrl: String? = null, // Image principale (rétrocompatibilité)
    val postType: PostType = PostType.GENERAL,
    val visibility: PostVisibility = PostVisibility.PUBLIC, // Phase F.3 - Visibilité des Posts
    val tags: List<CreateTagRequest> = emptyList(),
    val serviceIds: List<String> = emptyList(),
    val media: List<CreatePostMediaRequest> = emptyList() // Médias multiples (pour AVANT_APRES)
)

/**
 * DTO pour créer un nouveau commentaire.
 */
@Serializable
data class CreateCommentRequest(
    val postId: String,
    val authorId: String,
    val content: String
)

/**
 * Énumération des types d'entités pouvant être taguées.
 */
@Serializable
enum class TaggedType {
    salon,
    user
}

/**
 * Énumération des types de médias pour les posts.
 */
@Serializable
enum class PostMediaType {
    before,
    after,
    process,
    detail;

    /**
     * Retourne le libellé utilisateur du type.
     */
    fun getDisplayName(): String {
        return when (this) {
            before -> "Avant"
            after -> "Après"
            process -> "Processus"
            detail -> "Détail"
        }
    }

    /**
     * Retourne l'emoji associé au type.
     */
    fun getEmoji(): String {
        return when (this) {
            before -> "📸"
            after -> "✨"
            process -> "⚙️"
            detail -> "🔍"
        }
    }
}

/**
 * Énumération des types de réactions spécialisées pour l'univers de la coiffure.
 * Phase D.4 - Réactions Spécialisées Coiffure
 */
@Serializable
enum class ReactionType {
    LIKE,
    LOVE,
    WOW,
    INSPIRANT,
    MAGNIFIQUE,
    BRAVO;

    /**
     * Retourne le libellé utilisateur du type de réaction.
     */
    fun getDisplayName(): String {
        return when (this) {
            LIKE -> "J'aime"
            LOVE -> "J'adore"
            WOW -> "Wow"
            INSPIRANT -> "Inspirant"
            MAGNIFIQUE -> "Magnifique"
            BRAVO -> "Bravo"
        }
    }

    /**
     * Retourne l'emoji associé au type de réaction.
     */
    fun getEmoji(): String {
        return when (this) {
            LIKE -> "👍"
            LOVE -> "❤️"
            WOW -> "😮"
            INSPIRANT -> "✨"
            MAGNIFIQUE -> "💎"
            BRAVO -> "👏"
        }
    }

    /**
     * Retourne la description du type de réaction.
     */
    fun getDescription(): String {
        return when (this) {
            LIKE -> "Like classique"
            LOVE -> "J'adore cette couleur !"
            WOW -> "Transformation incroyable !"
            INSPIRANT -> "Je veux la même chose !"
            MAGNIFIQUE -> "Travail de qualité !"
            BRAVO -> "Félicitations au coiffeur !"
        }
    }
}

/**
 * Énumération des catégories de hashtags coiffure.
 */
@Serializable
enum class HairHashtagCategory {
    TECHNIQUE,
    STYLE,
    COULEUR,
    LONGUEUR,
    TEXTURE;

    /**
     * Retourne le libellé utilisateur de la catégorie.
     */
    fun getDisplayName(): String {
        return when (this) {
            TECHNIQUE -> "Technique"
            STYLE -> "Style"
            COULEUR -> "Couleur"
            LONGUEUR -> "Longueur"
            TEXTURE -> "Texture"
        }
    }

    /**
     * Retourne l'emoji associé à la catégorie.
     */
    fun getEmoji(): String {
        return when (this) {
            TECHNIQUE -> "✂️"
            STYLE -> "💇"
            COULEUR -> "🎨"
            LONGUEUR -> "📏"
            TEXTURE -> "🌀"
        }
    }
}

/**
 * DTO de réponse pour un média associé à un post.
 */
@Serializable
data class PostMediaResponse(
    val id: String,
    val mediaUrl: String,
    val mediaType: PostMediaType,
    val mediaTypeLabel: String,
    val orderIndex: Int,
    val createdAt: String? = null // ISO-8601
)

/**
 * DTO de réponse pour un hashtag coiffure.
 */
@Serializable
data class HairHashtagResponse(
    val id: String,
    val name: String,
    val category: HairHashtagCategory,
    val categoryLabel: String,
    val categoryEmoji: String,
    val usageCount: Int,
    val createdAt: String? = null // ISO-8601
)

/**
 * Énumération des types de propriétaires de portfolios.
 */
@Serializable
enum class PortfolioOwnerType {
    coiffeur,
    salon;

    /**
     * Retourne le libellé utilisateur du type.
     */
    fun getDisplayName(): String {
        return when (this) {
            coiffeur -> "Coiffeur"
            salon -> "Salon"
        }
    }
}

/**
 * DTO pour créer un nouveau portfolio.
 */
@Serializable
data class CreatePortfolioRequest(
    val ownerType: PortfolioOwnerType,
    val ownerId: String, // Pour les salons, l'ID du salon. Pour les coiffeurs, ignoré
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true
)

/**
 * DTO pour mettre à jour un portfolio.
 */
@Serializable
data class UpdatePortfolioRequest(
    val name: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean? = null
)

/**
 * DTO de réponse pour un portfolio.
 */
@Serializable
data class PortfolioResponse(
    val id: String,
    val ownerId: String,
    val ownerType: PortfolioOwnerType,
    val ownerTypeLabel: String,
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean,
    val postsCount: Int,
    val createdAt: String? = null, // ISO-8601
    val updatedAt: String? = null // ISO-8601
)

/**
 * DTO de réponse pour une association portfolio-post.
 */
@Serializable
data class PortfolioPostResponse(
    val id: String,
    val portfolioId: String,
    val postId: String,
    val orderIndex: Int,
    val addedAt: String? = null // ISO-8601
)

/**
 * DTO pour créer un tag/mention dans un post.
 */
@Serializable
data class CreateTagRequest(
    val taggedType: TaggedType,
    val taggedId: String
)

// ============================================
// RESPONSE DTOs
// ============================================

/**
 * DTO de réponse pour un tag/mention.
 */
@Serializable
data class TagResponse(
    val id: String,
    val postId: String,
    val taggedType: TaggedType,
    val taggedId: String,
    val createdAt: String? = null // ISO-8601
)

/**
 * DTO de réponse pour un post complet.
 */
@Serializable
data class PostResponse(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorEmail: String,
    val authorUserType: UserType? = null, // Type d'utilisateur de l'auteur
    val content: String,
    val imageUrl: String? = null,
    val postType: PostType = PostType.GENERAL,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int = 0, // Phase D.3 - Partage de Posts
    val isLikedByCurrentUser: Boolean = false,
    val isFavoritedByCurrentUser: Boolean = false,
    val isSharedByCurrentUser: Boolean = false, // Phase D.3 - Partage de Posts
    val reactions: Map<String, Int> = emptyMap(), // Phase D.4 - Réactions Spécialisées (clé: nom de ReactionType en lowercase)
    val currentUserReaction: ReactionType? = null, // Phase D.4 - Réaction de l'utilisateur courant
    val tags: List<TagResponse> = emptyList(),
    val services: List<SalonService> = emptyList(),
    val hashtags: List<HairHashtagResponse> = emptyList(),
    val media: List<PostMediaResponse> = emptyList(), // Médias multiples (pour AVANT_APRES) - Phase B.5
    val isPinned: Boolean = false, // Phase F.2 - Posts Épinglés
    val visibility: PostVisibility = PostVisibility.PUBLIC, // Phase F.3 - Visibilité des Posts
    val createdAt: String? = null, // ISO-8601
    val updatedAt: String? = null // ISO-8601
) {
    /**
     * Formatte la date de création pour l'affichage (format relatif).
     * 
     * Phase 4 - Fonctionnalité Langue : Utilise le formatage localisé.
     * Note: Utilise le français par défaut car cette propriété est dans une data class.
     * Pour un formatage selon la langue courante, utilisez formatLocalizedRelativeTime() dans les composables.
     */
    val formattedDate: String
        get() = try {
            createdAt?.let {
                // Utiliser le formatage localisé avec français par défaut
                com.frollot.mobile.localization.formatRelativeTimeForLanguageStatic(it, "fr")
            } ?: "Date inconnue"
        } catch (e: Exception) {
            createdAt ?: "Date inconnue"
        }

    /**
     * Badge de likes avec emoji.
     */
    val likesBadge: String
        get() = when {
            likesCount == 0 -> "❤️"
            likesCount < 1000 -> "$likesCount ❤️"
            else -> "${likesCount / 1000}k ❤️"
        }
}

/**
 * DTO de réponse pour un commentaire.
 */
@Serializable
data class CommentResponse(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorEmail: String,
    val content: String,
    val createdAt: String? = null, // ISO-8601
    val updatedAt: String? = null // ISO-8601
) {
    /**
     * Formatte la date de création pour l'affichage (format relatif).
     * 
     * Phase 4 - Fonctionnalité Langue : Utilise le formatage localisé.
     * Note: Utilise le français par défaut car cette propriété est dans une data class.
     * Pour un formatage selon la langue courante, utilisez formatLocalizedRelativeTime() dans les composables.
     */
    val formattedDate: String
        get() = try {
            createdAt?.let {
                // Utiliser le formatage localisé avec français par défaut
                com.frollot.mobile.localization.formatRelativeTimeForLanguageStatic(it, "fr")
            } ?: "Date inconnue"
        } catch (e: Exception) {
            createdAt ?: "Date inconnue"
        }
}

/**
 * DTO pour une page de résultats paginés.
 */
@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int = 0,        // Backend uses 'page', kept 'number' as alias
    val size: Int = 20,
    val isFirst: Boolean = true,
    val isLast: Boolean = true,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    // Aliases for compatibility
    val number: Int = page,
    val first: Boolean = isFirst,
    val last: Boolean = isLast
)

// ============================================
// SEARCH MODELS (Phase C.1 - Recherche spécialisée coiffure)
// ============================================

/**
 * Type de recherche pour la recherche unifiée.
 */
@Serializable
enum class SearchType {
    POSTS,
    SALONS,
    USERS,
    HASHTAGS,
    ALL
}

/**
 * Filtres de recherche pour les posts.
 */
@Serializable
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
 */
@Serializable
data class SearchResponse(
    val posts: List<PostResponse>,
    val salons: List<Salon>, // Utilise le modèle Salon existant
    val users: List<User>, // Utilise le modèle User existant
    val hashtags: List<HairHashtagResponse>,
    val totalPosts: Int,
    val totalSalons: Int,
    val totalUsers: Int,
    val totalHashtags: Int
)

/**
 * DTO de réponse pour un partage de post.
 * Phase D.3 - Partage de Posts (Reposts)
 */
@Serializable
data class PostShareResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val sharedContent: String? = null,
    val createdAt: String? = null // ISO-8601
)

/**
 * DTO de réponse pour un utilisateur (profil simplifié).
 * Phase E.2 - Profil Salon Social
 */
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val userType: UserType,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val isVerified: Boolean,
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val isActive: Boolean,
    val avatarUrl: String?,
    val createdAt: String? = null, // ISO-8601
    val isFollowedByCurrentUser: Boolean? = null,
    val followersCount: Long? = null
)

// ============================================
// Phase F.1 - Collections Thématiques
// ============================================

/**
 * Énumération des catégories de collections.
 * Phase F.1 - Collections Thématiques
 */
@Serializable
enum class CollectionCategory {
    INSPIRATION,  // Collections d'inspiration (ex: "Mes coupes préférées")
    PORTFOLIO,    // Collections portfolio (ex: "Mes réalisations 2024")
    TENDANCE,     // Collections tendance (ex: "Tendances été 2024")
    PERSONNEL     // Collections personnelles (ex: "Mes transformations")
}

/**
 * DTO pour créer une nouvelle collection.
 * Phase F.1 - Collections Thématiques
 */
@Serializable
data class CreateCollectionRequest(
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true,
    val category: CollectionCategory
)

/**
 * DTO pour mettre à jour une collection.
 * Phase F.1 - Collections Thématiques
 */
@Serializable
data class UpdateCollectionRequest(
    val name: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean? = null,
    val category: CollectionCategory? = null
)

/**
 * DTO de réponse pour une collection.
 * Phase F.1 - Collections Thématiques
 */
@Serializable
data class CollectionResponse(
    val id: String,
    val userId: String,
    val userName: String,
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true,
    val category: CollectionCategory,
    val postsCount: Int = 0,
    val createdAt: String? = null, // ISO-8601
    val updatedAt: String? = null // ISO-8601
)

/**
 * DTO de réponse pour l'association collection-post.
 * Phase F.1 - Collections Thématiques
 */
@Serializable
data class CollectionPostResponse(
    val id: String,
    val collectionId: String,
    val post: PostResponse,
    val orderIndex: Int = 0,
    val addedAt: String? = null // ISO-8601
)

// ============================================
// Phase E.1 - Profil Coiffeur Enrichi
// ============================================

/**
 * DTO pour mettre à jour le profil coiffeur.
 * Phase E.1 - Profil Coiffeur Enrichi
 */
@Serializable
data class UpdateCoiffeurProfileRequest(
    val bio: String? = null,
    val specialties: List<String> = emptyList(),
    val yearsExperience: Int? = null,
    val certifications: String? = null,
    val instagramHandle: String? = null,
    val portfolioHighlightedId: String? = null
)

/**
 * DTO pour les statistiques du profil coiffeur.
 * Phase E.1 - Profil Coiffeur Enrichi
 */
@Serializable
data class CoiffeurProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0,
    val followingCount: Long = 0
)

/**
 * DTO de réponse pour le profil coiffeur enrichi.
 * Phase E.1 - Profil Coiffeur Enrichi
 */
@Serializable
data class CoiffeurProfileResponse(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null, // Photo de couverture du profil
    val isVerified: Boolean = false,
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val bio: String? = null,
    val specialties: List<String> = emptyList(),
    val yearsExperience: Int? = null,
    val certifications: String? = null,
    val instagramHandle: String? = null,
    val portfolioHighlighted: PortfolioResponse? = null,
    val statistics: CoiffeurProfileStatistics,
    val portfolios: List<PortfolioResponse> = emptyList(),
    val recentPosts: List<PostResponse> = emptyList(),
    val badges: List<UserBadgeResponse> = emptyList(), // Phase E.3 - Badges et Certifications
    val isFollowedByCurrentUser: Boolean? = null
)

// ============================================
// Phase E.4 - Profil Client
// ============================================

/**
 * DTO pour les statistiques du profil client.
 * Phase E.4 - Profil Client
 */
@Serializable
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
@Serializable
data class ClientProfileResponse(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null, // Photo de couverture du profil
    val isVerified: Boolean = false,
    val bio: String? = null,
    val statistics: ClientProfileStatistics,
    val recentPosts: List<PostResponse>,
    val collections: List<CollectionResponse>,
    val badges: List<UserBadgeResponse> = emptyList(),
    val isFollowedByCurrentUser: Boolean? = null
)

// ============================================
// Phase E.5 - Profil Propriétaire de Salon
// ============================================

/**
 * DTO pour les statistiques du profil propriétaire de salon.
 * Phase E.5 - Profil Propriétaire de Salon
 */
@Serializable
data class SalonOwnerProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val salonsCount: Long = 0,
    val collectionsCount: Long = 0
)

/**
 * DTO de résumé de salon pour le profil propriétaire.
 */
@Serializable
data class SalonSummaryResponse(
    val id: String,
    val name: String,
    val city: String,
    val coverPhotoUrl: String?,
    val isVerified: Boolean,
    val followersCount: Long = 0
)

/**
 * DTO de réponse pour le profil propriétaire de salon enrichi.
 * Phase E.5 - Profil Propriétaire de Salon
 */
@Serializable
data class SalonOwnerProfileResponse(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null, // Photo de couverture du profil
    val isVerified: Boolean = false,
    val bio: String? = null,
    val statistics: SalonOwnerProfileStatistics,
    val salons: List<SalonSummaryResponse>,
    val recentPosts: List<PostResponse>,
    val collections: List<CollectionResponse>,
    val badges: List<UserBadgeResponse> = emptyList(),
    val isFollowedByCurrentUser: Boolean? = null
)

// ============================================
// Phase E.2 - Profil Salon Social
// ============================================

/**
 * DTO pour mettre à jour le profil social salon.
 * Phase E.2 - Profil Salon Social
 */
@Serializable
data class UpdateSalonSocialProfileRequest(
    val socialDescription: String? = null,
    val socialCoverImage: String? = null,
    val highlightedPostIds: List<String> = emptyList()
)

/**
 * DTO pour les statistiques du profil social salon.
 * Phase E.2 - Profil Salon Social
 */
@Serializable
data class SalonSocialProfileStatistics(
    val postsCount: Long = 0,
    val totalLikes: Long = 0,
    val followersCount: Long = 0
)

/**
 * DTO de réponse pour le profil social salon enrichi.
 * Phase E.2 - Profil Salon Social
 */
@Serializable
data class SalonSocialProfileResponse(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val slug: String,
    val coverPhotoUrl: String? = null,
    val socialDescription: String? = null,
    val socialCoverImage: String? = null,
    val isVerified: Boolean = false, // Phase H.2 - Vérification Salons/Coiffeurs
    val verificationType: VerificationType? = null, // Phase H.2 - Vérification Salons/Coiffeurs
    val highlightedPosts: List<PostResponse> = emptyList(),
    val statistics: SalonSocialProfileStatistics,
    val portfolios: List<PortfolioResponse> = emptyList(),
    val recentPosts: List<PostResponse> = emptyList(),
    val team: List<UserResponse> = emptyList(),
    val services: List<SalonService> = emptyList(),
    val isFollowedByCurrentUser: Boolean? = null,
    val isOwner: Boolean = false // Indique si l'utilisateur courant est le propriétaire du salon
)

// ============================================
// Phase E.3 - Badges et Certifications
// ============================================

/**
 * Énumération des catégories de badges.
 * Phase E.3 - Badges et Certifications
 */
@Serializable
enum class BadgeCategory {
    CERTIFICATION,
    COMPETITION,
    FORMATION,
    PARTENARIAT;

    /**
     * Retourne le libellé utilisateur de la catégorie.
     */
    fun getDisplayName(): String {
        return when (this) {
            CERTIFICATION -> "Certification"
            COMPETITION -> "Compétition"
            FORMATION -> "Formation"
            PARTENARIAT -> "Partenariat"
        }
    }
}

/**
 * DTO pour attribuer un badge à un utilisateur.
 * Phase E.3 - Badges et Certifications
 */
@Serializable
data class AwardBadgeRequest(
    val badgeId: String
)

/**
 * DTO de réponse pour un badge.
 * Phase E.3 - Badges et Certifications
 */
@Serializable
data class BadgeResponse(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val category: BadgeCategory
)

/**
 * DTO de réponse pour l'association user-badge.
 * Phase E.3 - Badges et Certifications
 */
@Serializable
data class UserBadgeResponse(
    val id: String,
    val badge: BadgeResponse,
    val earnedAt: String? = null, // ISO-8601
    val isDisplayed: Boolean
)

// ============================================
// MODERATION MODELS
// Phase H.1 - Signalement de Contenu
// ============================================

/**
 * Énumération des raisons de signalement de contenu.
 * Phase H.1 - Signalement de Contenu
 */
@Serializable
enum class ReportReason {
    INAPPROPRIE,
    SPAM,
    FAUX,
    COPYRIGHT,
    AUTRE;

    /**
     * Retourne le libellé utilisateur de la raison.
     */
    fun getDisplayName(): String {
        return when (this) {
            INAPPROPRIE -> "Contenu inapproprié"
            SPAM -> "Spam publicitaire"
            FAUX -> "Faux avant/après"
            COPYRIGHT -> "Violation de droits d'auteur"
            AUTRE -> "Autre"
        }
    }

    /**
     * Retourne la description de la raison.
     */
    fun getDescription(): String {
        return when (this) {
            INAPPROPRIE -> "Contenu violent, harcelant ou offensant"
            SPAM -> "Publicité non sollicitée ou contenu répétitif"
            FAUX -> "Transformation ou résultat trompeur"
            COPYRIGHT -> "Utilisation non autorisée de contenu protégé"
            AUTRE -> "Autre raison à préciser"
        }
    }
}

/**
 * Énumération des statuts d'un signalement.
 * Phase H.1 - Signalement de Contenu
 */
@Serializable
enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED;

    /**
     * Retourne le libellé utilisateur du statut.
     */
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "En attente"
            REVIEWED -> "En cours d'examen"
            RESOLVED -> "Résolu"
            DISMISSED -> "Rejeté"
        }
    }
}

/**
 * Énumération des types d'entités pouvant être signalées.
 * Phase H.1 - Signalement de Contenu
 */
@Serializable
enum class ReportedEntityType {
    POST,
    COMMENT,
    USER,
    SALON;

    /**
     * Retourne le libellé utilisateur du type d'entité.
     */
    fun getDisplayName(): String {
        return when (this) {
            POST -> "Post"
            COMMENT -> "Commentaire"
            USER -> "Utilisateur"
            SALON -> "Salon"
        }
    }
}

/**
 * DTO pour créer un signalement de contenu.
 * Phase H.1 - Signalement de Contenu
 */
@Serializable
data class CreateReportRequest(
    val reportedEntityType: ReportedEntityType,
    val reportedEntityId: String,
    val reason: ReportReason,
    val additionalInfo: String? = null
)

/**
 * DTO de réponse pour un signalement.
 * Phase H.1 - Signalement de Contenu
 */
@Serializable
data class ReportResponse(
    val id: String,
    val reportedEntityType: ReportedEntityType,
    val reportedEntityId: String,
    val reporterId: String,
    val reporterName: String,
    val reason: ReportReason,
    val status: ReportStatus,
    val additionalInfo: String? = null,
    val createdAt: String? = null, // ISO-8601
    val updatedAt: String? = null // ISO-8601
)

/**
 * DTO pour traiter un signalement (admin uniquement).
 * Phase H.1 - Signalement de Contenu
 */
@Serializable
data class HandleReportRequest(
    val status: ReportStatus,
    val adminNote: String? = null
)

// ============================================
// Phase H.2 - Vérification Salons/Coiffeurs
// ============================================

/**
 * Énumération des types de vérification pour les utilisateurs et salons.
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
@Serializable
enum class VerificationType {
    EMAIL,          // Vérification basique : email vérifié
    PHONE,          // Vérification téléphone
    BUSINESS,       // Vérification entreprise : SIRET, documents d'entreprise, etc.
    PROFESSIONAL;   // Vérification professionnelle : diplômes, certifications professionnelles

    /**
     * Retourne le libellé utilisateur du type de vérification.
     */
    fun getDisplayName(): String {
        return when (this) {
            EMAIL -> "Email vérifié"
            PHONE -> "Téléphone vérifié"
            BUSINESS -> "Entreprise vérifiée"
            PROFESSIONAL -> "Professionnel vérifié"
        }
    }

    /**
     * Retourne la description du type de vérification.
     */
    fun getDescription(): String {
        return when (this) {
            EMAIL -> "Email vérifié par confirmation"
            PHONE -> "Numéro de téléphone vérifié"
            BUSINESS -> "Entreprise vérifiée (SIRET, documents)"
            PROFESSIONAL -> "Diplômes et certifications vérifiés"
        }
    }

    /**
     * Retourne l'emoji associé au type de vérification.
     */
    fun getEmoji(): String {
        return when (this) {
            EMAIL -> "📧"
            PHONE -> "📱"
            BUSINESS -> "🏢"
            PROFESSIONAL -> "🎓"
        }
    }
}

/**
 * DTO pour demander une vérification.
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
@Serializable
data class RequestVerificationRequest(
    val verificationType: VerificationType,
    val additionalInfo: String? = null // Informations supplémentaires (documents, SIRET, etc.)
)

/**
 * DTO pour vérifier un utilisateur (admin uniquement).
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
@Serializable
data class VerifyUserRequest(
    val verificationType: VerificationType,
    val adminNote: String? = null // Note interne pour l'admin
)

/**
 * DTO pour vérifier un salon (admin uniquement).
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
@Serializable
data class VerifySalonRequest(
    val verificationType: VerificationType,
    val adminNote: String? = null // Note interne pour l'admin
)

// ============================================
// Phase H.3 - Modération de Contenu Coiffure
// ============================================

/**
 * Énumération des types d'actions de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Serializable
enum class ModerationActionType {
    HIDE,    // Masquer le contenu
    DELETE,  // Supprimer définitivement le contenu
    WARN;    // Avertir l'auteur sans modifier le contenu

    /**
     * Retourne le libellé utilisateur de l'action de modération.
     */
    fun getDisplayName(): String {
        return when (this) {
            HIDE -> "Masquer"
            DELETE -> "Supprimer"
            WARN -> "Avertir"
        }
    }

    /**
     * Retourne la description de l'action de modération.
     */
    fun getDescription(): String {
        return when (this) {
            HIDE -> "Le contenu sera masqué pour tous les utilisateurs sauf l'auteur et les administrateurs."
            DELETE -> "Le contenu sera supprimé définitivement et ne pourra pas être restauré."
            WARN -> "Un avertissement sera envoyé à l'auteur sans modifier le contenu."
        }
    }
}

/**
 * Énumération des statuts d'appel d'une action de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Serializable
enum class AppealStatus {
    NONE,     // Aucun appel n'a été fait
    PENDING,  // Un appel est en attente de traitement
    APPROVED, // L'appel a été approuvé, l'action de modération est annulée
    REJECTED; // L'appel a été rejeté, l'action de modération est maintenue

    /**
     * Retourne le libellé utilisateur du statut d'appel.
     */
    fun getDisplayName(): String {
        return when (this) {
            NONE -> "Aucun appel"
            PENDING -> "En attente"
            APPROVED -> "Approuvé"
            REJECTED -> "Rejeté"
        }
    }
}

/**
 * DTO pour modérer un contenu (admin uniquement).
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Serializable
data class ModerateContentRequest(
    val contentEntityType: ReportedEntityType,
    val contentEntityId: String,
    val action: ModerationActionType,
    val reason: String? = null
)

/**
 * DTO pour faire appel d'une action de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Serializable
data class AppealModerationRequest(
    val moderationActionId: String,
    val appealReason: String
)

/**
 * DTO pour traiter un appel de modération (admin uniquement).
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Serializable
data class HandleAppealRequest(
    val appealStatus: AppealStatus,
    val adminNote: String? = null
)

/**
 * DTO de réponse pour une action de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 */
@Serializable
data class ModerationActionResponse(
    val id: String,
    val contentEntityType: ReportedEntityType,
    val contentEntityId: String,
    val action: ModerationActionType,
    val moderatorId: String,
    val moderatorName: String,
    val reason: String?,
    val appealStatus: AppealStatus,
    val appealReason: String?,
    val appealProcessedAt: String? = null, // ISO-8601
    val createdAt: String? = null // ISO-8601
)
