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
 * Service de gestion du réseau social.
 * 
 * Gère :
 * - Création et récupération des posts
 * - Gestion des likes (toggle)
 * - Gestion des commentaires
 * - Feed avec pagination
 */
@Service
@Transactional
class SocialService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postFavoriteRepository: PostFavoriteRepository,
    // V041 : PostArchiveRepository retiré (archivage global via posts.is_archived ; table post_archives dormante)
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val postTagRepository: PostTagRepository,
    private val salonRepository: SalonRepository,
    private val postServiceRepository: PostServiceRepository,
    private val salonServiceRepository: com.frollot.repository.SalonServiceRepository,
    private val hairHashtagRepository: HairHashtagRepository,
    private val postHashtagRepository: PostHashtagRepository,
    private val postMediaRepository: PostMediaRepository,
    private val followRepository: com.frollot.repository.FollowRepository, // Phase D.2 - Pour le feed Following
    private val postShareRepository: PostShareRepository, // Phase D.3 - Partage de Posts
    private val postReactionRepository: com.frollot.repository.PostReactionRepository, // Phase D.4 - Réactions Spécialisées
    private val portfolioRepository: com.frollot.repository.PortfolioRepository, // Phase E.1 - Profil Coiffeur Enrichi
    private val portfolioPostRepository: com.frollot.repository.PortfolioPostRepository, // Phase E.1 - Profil Coiffeur Enrichi
    private val salonHighlightedPostRepository: com.frollot.repository.SalonHighlightedPostRepository, // Phase E.2 - Profil Salon Social
    private val salonStaffRepository: com.frollot.repository.SalonStaffRepository, // Phase E.2 - Profil Salon Social
    private val badgeRepository: com.frollot.repository.BadgeRepository, // Phase E.3 - Badges et Certifications
    private val userBadgeRepository: com.frollot.repository.UserBadgeRepository, // Phase E.3 - Badges et Certifications
    private val collectionRepository: com.frollot.repository.CollectionRepository, // Phase F.1 - Collections Thématiques
    private val collectionPostRepository: com.frollot.repository.CollectionPostRepository, // Phase F.1 - Collections Thématiques
    private val reviewRepository: ReviewRepository, // Lot 2 - Note coiffeur
    private val salonAuthorizationService: SalonAuthorizationService
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class PostNotFoundException(postId: String) :
        RuntimeException("Post avec ID '$postId' non trouvé")

    class CommentNotFoundException(commentId: String) :
        RuntimeException("Commentaire avec ID '$commentId' non trouvé")

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé")

    class InvalidPostException(message: String) : RuntimeException(message)

    class TagNotFoundException(tagId: String) :
        RuntimeException("Tag avec ID '$tagId' non trouvé")

    class TagAlreadyExistsException(message: String) : RuntimeException(message)

    class TaggedEntityNotFoundException(message: String) : RuntimeException(message)

    // ========== PHASE F.3 : VISIBILITÉ DES POSTS ==========

    /**
     * Vérifie si un post est visible par un utilisateur donné.
     * Phase F.3 - Visibilité des Posts
     * 
     * @param post Le post à vérifier
     * @param currentUserId L'ID de l'utilisateur courant (null si non connecté)
     * @return true si le post est visible par l'utilisateur, false sinon
     */
    private fun isPostVisible(post: Post, currentUserId: String?): Boolean {
        return when (post.visibility) {
            com.frollot.model.PostVisibility.PUBLIC -> {
                // Public : visible par tous
                true
            }
            com.frollot.model.PostVisibility.FOLLOWERS -> {
                // Followers : visible uniquement si l'utilisateur suit l'auteur ou est l'auteur
                if (currentUserId == null) {
                    false
                } else {
                    val authorId = post.author?.id
                    if (authorId == null) {
                        false
                    } else if (authorId == currentUserId) {
                        // L'auteur peut toujours voir ses propres posts
                        true
                    } else {
                        // Vérifier si l'utilisateur suit l'auteur (FollowingType.USER car l'auteur est un User)
                        followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                            currentUserId,
                            com.frollot.model.FollowingType.USER,
                            authorId
                        )
                    }
                }
            }
            com.frollot.model.PostVisibility.PRIVATE -> {
                // Privé : visible uniquement par l'auteur
                if (currentUserId == null) {
                    false
                } else {
                    post.author?.id == currentUserId
                }
            }
        }
    }

    // ========== GESTION DES POSTS ==========

    /**
     * Crée un nouveau post.
     */
    @Transactional
    fun createPost(request: CreatePostRequest): PostResponse {
        // Validation
        request.validate()

        // Vérification de l'existence de l'utilisateur
        val author = userRepository.findById(request.authorId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '${request.authorId}' non trouvé") }

        // Lot C : déterminer le contexte (post perso ou au nom du salon)
        val authorType: com.frollot.model.AuthorType
        val salonId: String?
        if (request.postAsSalonId != null) {
            if (!salonRepository.existsById(request.postAsSalonId)) {
                throw RuntimeException("Salon avec ID '${request.postAsSalonId}' non trouvé")
            }
            salonAuthorizationService.requirePermission(
                request.authorId, request.postAsSalonId, "social.post_as_salon"
            )
            authorType = com.frollot.model.AuthorType.salon
            salonId = request.postAsSalonId
        } else {
            authorType = com.frollot.model.AuthorType.user
            salonId = null
        }

        // Création du post
        val post = Post(
            id = UUID.randomUUID().toString(),
            authorType = authorType,
            author = author,
            salonId = salonId,
            content = request.content.trim(),
            postType = request.postType,
            imageUrl = request.imageUrl?.trim()?.takeIf { it.isNotBlank() },
            visibility = request.visibility, // Phase F.3 - Visibilité des Posts
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0
        )

        // Validation de l'entité
        if (!post.isValid()) {
            throw InvalidPostException("Les données du post sont invalides")
        }

        // Sauvegarde
        val savedPost = postRepository.save(post)

        // Création des tags si présents
        val tags = mutableListOf<TagResponse>()
        request.tags.forEach { tagRequest ->
            try {
                // Vérification que l'entité taguée existe
                when (tagRequest.taggedType) {
                    TaggedType.salon -> {
                        if (!salonRepository.existsById(tagRequest.taggedId)) {
                            println("⚠️ Salon avec ID '${tagRequest.taggedId}' non trouvé, tag ignoré")
                            return@forEach
                        }
                    }
                    TaggedType.user -> {
                        if (!userRepository.existsById(tagRequest.taggedId)) {
                            println("⚠️ Utilisateur avec ID '${tagRequest.taggedId}' non trouvé, tag ignoré")
                            return@forEach
                        }
                    }
                }

                // Vérification que le tag n'existe pas déjà
                if (!postTagRepository.existsByPostIdAndTaggedTypeAndTaggedId(
                        savedPost.id!!,
                        tagRequest.taggedType,
                        tagRequest.taggedId
                    )
                ) {
                    val tag = PostTag(
                        id = UUID.randomUUID().toString(),
                        post = savedPost,
                        taggedType = tagRequest.taggedType,
                        taggedId = tagRequest.taggedId
                    )

                    if (tag.isValid()) {
                        val savedTag = postTagRepository.save(tag)
                        tags.add(buildTagResponse(savedTag))
                        println("🏷️ Tag ajouté: ${tagRequest.taggedType} - ${tagRequest.taggedId}")
                    }
                }
            } catch (e: Exception) {
                println("⚠️ Erreur lors de la création du tag: ${e.message}")
                // On continue avec les autres tags même si un échoue
            }
        }

        // Extraction et association des hashtags depuis le contenu
        val hashtagNames = extractHashtagsFromContent(request.content)
        val associatedHashtags = mutableListOf<HairHashtag>()
        
        hashtagNames.forEach { hashtagName ->
            try {
                // Normaliser le nom (lowercase, sans #)
                val normalizedName = hashtagName.lowercase().trim().removePrefix("#")
                
                if (normalizedName.isBlank()) {
                    return@forEach
                }

                // Chercher ou créer le hashtag
                var hashtag = hairHashtagRepository.findByNameIgnoreCase(normalizedName)
                
                if (hashtag == null) {
                    // Créer un nouveau hashtag avec catégorie par défaut (STYLE)
                    // La catégorie pourrait être déterminée intelligemment plus tard
                    hashtag = HairHashtag(
                        id = UUID.randomUUID().toString(),
                        name = normalizedName,
                        category = HairHashtagCategory.STYLE,
                        usageCount = 0
                    )
                    
                    if (hashtag.isValid()) {
                        hashtag = hairHashtagRepository.save(hashtag)
                        println("📌 Nouveau hashtag créé: #$normalizedName")
                    } else {
                        return@forEach
                    }
                }

                // Vérifier que l'association n'existe pas déjà
                if (!postHashtagRepository.existsByPostIdAndHashtagId(savedPost.id!!, hashtag.id!!)) {
                    val postHashtag = PostHashtag(
                        id = UUID.randomUUID().toString(),
                        post = savedPost,
                        hashtag = hashtag
                    )

                    if (postHashtag.isValid()) {
                        postHashtagRepository.save(postHashtag)
                        // Incrémenter le compteur d'utilisation
                        hashtag.usageCount++
                        hairHashtagRepository.save(hashtag)
                        associatedHashtags.add(hashtag)
                        println("🏷️ Hashtag associé: #${hashtag.name}")
                    }
                }
            } catch (e: Exception) {
                println("⚠️ Erreur lors de l'association du hashtag: ${e.message}")
                // On continue avec les autres hashtags même si un échoue
            }
        }

        // Création des médias si présents (Phase B.5)
        val mediaList = mutableListOf<PostMediaResponse>()
        request.media.forEach { mediaRequest ->
            try {
                val postMedia = PostMedia(
                    id = UUID.randomUUID().toString(),
                    post = savedPost,
                    mediaUrl = mediaRequest.mediaUrl.trim(),
                    mediaType = mediaRequest.mediaType,
                    orderIndex = mediaRequest.orderIndex
                )

                if (postMedia.isValid()) {
                    val savedMedia = postMediaRepository.save(postMedia)
                    mediaList.add(PostMediaResponse.fromEntity(savedMedia))
                    println("📸 Média ajouté: ${mediaRequest.mediaType} (ordre: ${mediaRequest.orderIndex})")
                }
            } catch (e: Exception) {
                println("⚠️ Erreur lors de la création du média: ${e.message}")
            }
        }

        println("✅ Post créé: ${savedPost.id} par ${author.email} avec ${tags.size} tag(s), ${associatedHashtags.size} hashtag(s) et ${mediaList.size} média(x)")

        val commentsCount = commentRepository.countByPostId(savedPost.id!!).toInt()
        val hashtags = HairHashtagResponse.fromEntities(associatedHashtags)
        val (sName, sAvatar) = resolveSalonInfo(savedPost)
        return PostResponse.fromEntity(
            savedPost,
            isLikedByCurrentUser = false,
            isFavoritedByCurrentUser = false,
            isSharedByCurrentUser = false,
            commentsCount = commentsCount,
            sharesCount = 0,
            tags = tags,
            services = emptyList(),
            hashtags = hashtags,
            media = mediaList,
            salonName = sName,
            salonAvatarUrl = sAvatar
        )
    }

    /**
     * Extrait les hashtags (#mot) du contenu d'un post.
     */
    private fun extractHashtagsFromContent(content: String): List<String> {
        val hashtagPattern = Regex("#(\\w+)")
        return hashtagPattern.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    // ========== GESTION DES HASHTAGS ==========

    /**
     * Récupère les hashtags les plus utilisés (trending).
     */
    @Transactional(readOnly = true)
    fun getTrendingHashtags(limit: Int = 20): List<HairHashtagResponse> {
        val hashtags = hairHashtagRepository.findTrendingHashtags().take(limit)
        return HairHashtagResponse.fromEntities(hashtags)
    }

    /**
     * Recherche des hashtags par nom.
     */
    @Transactional(readOnly = true)
    fun searchHashtags(query: String): List<HairHashtagResponse> {
        if (query.isBlank()) {
            return emptyList()
        }
        val hashtags = hairHashtagRepository.searchByName(query)
        return HairHashtagResponse.fromEntities(hashtags)
    }

    /**
     * Suggère des hashtags basés sur un préfixe.
     */
    @Transactional(readOnly = true)
    fun suggestHashtags(prefix: String, limit: Int = 10): List<HairHashtagResponse> {
        if (prefix.isBlank()) {
            return emptyList()
        }
        val normalizedPrefix = prefix.lowercase().trim().removePrefix("#")
        val hashtags = hairHashtagRepository.suggestByPrefix(normalizedPrefix).take(limit)
        return HairHashtagResponse.fromEntities(hashtags)
    }

    /**
     * Récupère les posts associés à un hashtag.
     */
    @Transactional(readOnly = true)
    fun getPostsByHashtag(hashtagName: String, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        val normalizedName = hashtagName.lowercase().trim().removePrefix("#")
        val posts = postHashtagRepository.findPostsByHashtagName(normalizedName)
            .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)

        // Pagination manuelle
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, posts.size)
        val paginatedPosts = if (start < posts.size) {
            posts.subList(start, end)
        } else {
            emptyList()
        }
        
        val page = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            posts.size.toLong()
        )

        return page.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Récupère les hashtags par catégorie.
     */
    @Transactional(readOnly = true)
    fun getHashtagsByCategory(category: HairHashtagCategory): List<HairHashtagResponse> {
        val hashtags = hairHashtagRepository.findByCategory(category)
        return HairHashtagResponse.fromEntities(hashtags)
    }

    /**
     * Récupère un post par son ID.
     */
    @Transactional(readOnly = true)
    fun getPostById(postId: String, currentUserId: String? = null): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // V041 - Archivage global : un post archivé est introuvable (404) sauf pour son auteur
        if (post.isArchived && post.author?.id != currentUserId) {
            throw PostNotFoundException(postId)
        }

        val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
        val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
        val isShared = currentUserId?.let { postShareRepository.existsByPostIdAndUserId(postId, it) } ?: false
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val sharesCount = post.sharesCount
        val reactions = getReactionsByPost(postId) // Phase D.4
        val currentUserReaction = currentUserId?.let { getUserReaction(postId, it) } // Phase D.4
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        val services = postServiceRepository.findServicesByPostId(postId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
        val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
            .map { HairHashtagResponse.fromEntity(it) }
        val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
            .map { PostMediaResponse.fromEntity(it) }

        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = isShared,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            reactions = reactions, // Phase D.4
            currentUserReaction = currentUserReaction, // Phase D.4
            tags = tags,
            services = services,
            hashtags = hashtags,
            media = media
        )
    }

    /**
     * Récupère le feed des entités suivies par un utilisateur.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     * 
     * Récupère les posts des salons et coiffeurs suivis par l'utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param pageable Pagination
     * @param currentUserId ID de l'utilisateur courant (pour isLiked, isFavorited)
     * @return Page de PostResponse triés par date décroissante
     */
    @Transactional(readOnly = true)
    fun getFollowingFeed(userId: String, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        // 1. Récupérer tous les follows de l'utilisateur
        val follows = followRepository.findByFollowerId(userId)
        
        if (follows.isEmpty()) {
            // Aucun follow, retourner une page vide
            return org.springframework.data.domain.PageImpl(emptyList(), pageable, 0)
        }
        
        // 2. Séparer les salons et coiffeurs suivis
        val followedSalonIds = follows
            .filter { it.followingType == com.frollot.model.FollowingType.SALON }
            .map { it.followingId }
            .toSet()
        
        val followedCoiffeurIds = follows
            .filter { it.followingType == com.frollot.model.FollowingType.COIFFEUR }
            .map { it.followingId }
            .toSet()

        val followedUserIds = follows
            .filter { it.followingType == com.frollot.model.FollowingType.USER }
            .map { it.followingId }
            .toSet()

        // 3. Récupérer les posts des salons suivis (via PostTag)
        val salonPosts = mutableSetOf<com.frollot.model.Post>()
        followedSalonIds.forEach { salonId ->
            val postTags = postTagRepository.findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
                com.frollot.model.TaggedType.salon,
                salonId
            )
            postTags.mapNotNull { it.post }.forEach { post ->
                salonPosts.add(post)
            }
        }
        
        // 4. Récupérer les posts des coiffeurs suivis (via authorId)
        val coiffeurPosts = if (followedCoiffeurIds.isNotEmpty()) {
            followedCoiffeurIds.flatMap { coiffeurId ->
                postRepository.findByAuthorIdOrderByCreatedAtDesc(coiffeurId)
            }
        } else {
            emptyList()
        }

        // 4b. Récupérer les posts des utilisateurs suivis (via authorId)
        val userPosts = if (followedUserIds.isNotEmpty()) {
            followedUserIds.flatMap { uid ->
                postRepository.findByAuthorIdOrderByCreatedAtDesc(uid)
            }
        } else {
            emptyList()
        }

        // 5. Fusionner et trier par date décroissante (distinctBy élimine les doublons coiffeur+user)
        val allPosts = (salonPosts + coiffeurPosts + userPosts)
            .distinctBy { it.id }
            .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)
            .sortedByDescending { it.createdAt }
        
        // 6. Appliquer la pagination manuellement
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, allPosts.size)
        val paginatedPosts = allPosts.subList(start, end)
        
        // 7. Convertir en PostResponse
        val postsPage = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            allPosts.size.toLong()
        )
        
        return postsPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Récupère le feed (tous les posts) avec pagination.
     * Exclut les posts archivés par l'utilisateur courant.
     */
    @Transactional(readOnly = true)
    fun getFeed(pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        val postsPage = if (currentUserId != null) {
            val allPosts = postRepository.findAllOrderByCreatedAtDesc(Pageable.unpaged())
            val filteredPosts = allPosts.content
                .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)
                .filter { isPostVisible(it, currentUserId) } // Phase F.3 - Filtrer par visibilité
                .filter { !it.isHidden && !it.isDeleted } // Phase H.3 - Filtrer les contenus modérés
            
            // Créer une page paginée manuellement
            val start = pageable.pageNumber * pageable.pageSize
            val end = minOf(start + pageable.pageSize, filteredPosts.size)
            val paginatedPosts = filteredPosts.subList(start, end)
            
            org.springframework.data.domain.PageImpl(
                paginatedPosts,
                pageable,
                filteredPosts.size.toLong()
            )
        } else {
            // Utilisateur non connecté : uniquement les posts publics
            val allPosts = postRepository.findAllOrderByCreatedAtDesc(Pageable.unpaged())
            val publicPosts = allPosts.content
                .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)
                .filter { it.visibility == com.frollot.model.PostVisibility.PUBLIC } // Phase F.3
                .filter { !it.isHidden && !it.isDeleted } // Phase H.3 - Filtrer les contenus modérés
            
            val start = pageable.pageNumber * pageable.pageSize
            val end = minOf(start + pageable.pageSize, publicPosts.size)
            val paginatedPosts = publicPosts.subList(start, end)
            
            org.springframework.data.domain.PageImpl(
                paginatedPosts,
                pageable,
                publicPosts.size.toLong()
            )
        }

        return postsPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isShared = currentUserId?.let { postShareRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val sharesCount = post.sharesCount
            val reactions = getReactionsByPost(postId) // Phase D.4
            val currentUserReaction = currentUserId?.let { getUserReaction(postId, it) } // Phase D.4
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
            val (sN, sA) = resolveSalonInfo(post)
            PostResponse.fromEntity(
                post,
                isLikedByCurrentUser = isLiked,
                isFavoritedByCurrentUser = isFavorited,
                isSharedByCurrentUser = isShared,
                commentsCount = commentsCount,
                sharesCount = sharesCount,
                reactions = reactions, // Phase D.4
                currentUserReaction = currentUserReaction, // Phase D.4
                tags = tags,
                services = services,
                hashtags = hashtags,
                media = media,
                salonName = sN,
                salonAvatarUrl = sA
            )
        }
    }

    /**
     * Recherche des posts par contenu (texte).
     * Phase C.1 - Recherche spécialisée coiffure
     */
    @Transactional(readOnly = true)
    fun searchPostsByContent(query: String, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        if (query.isBlank()) {
            // Si la requête est vide, retourner une page vide
            return org.springframework.data.domain.PageImpl(emptyList(), pageable, 0)
        }

        val allPosts = postRepository.searchByContent(query.trim(), Pageable.unpaged())

        // Phase F.3 - Filtrer par visibilité
        val visiblePosts = allPosts.content
            .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)
            .filter { isPostVisible(it, currentUserId) }
        
        // Pagination manuelle
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, visiblePosts.size)
        val paginatedPosts = if (start < visiblePosts.size) {
            visiblePosts.subList(start, end)
        } else {
            emptyList()
        }
        
        val postsPage = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            visiblePosts.size.toLong()
        )

        return postsPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Recherche des posts avec filtres avancés.
     * Phase C.1 - Recherche spécialisée coiffure
     * 
     * Filtres supportés :
     * - query : Recherche dans le contenu (optionnel)
     * - postType : Type de post
     * - serviceId : Service associé
     * - salonId : Salon tagué
     * - hashtagName : Hashtag associé
     * - authorId : Auteur du post
     */
    @Transactional(readOnly = true)
    fun searchPosts(query: String?, filters: com.frollot.dto.SearchFilters, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        // Étape 1 : Recherche de base avec filtres simples (postType, authorId)
        val normalizedQuery = query?.takeIf { it.isNotBlank() }?.trim()
        val basePostsPage = postRepository.searchWithFilters(
            query = normalizedQuery,
            postType = filters.postType,
            authorId = filters.authorId,
            pageable = Pageable.unpaged() // On récupère tout pour filtrer ensuite
        )

        var filteredPosts = basePostsPage.content.toMutableList()

        // Étape 2 : Filtrer par serviceId si spécifié
        if (filters.serviceId != null) {
            val postsByService = postServiceRepository.findPostsByServiceId(filters.serviceId)
            filteredPosts = filteredPosts.filter { it.id in postsByService.map { p -> p.id } }.toMutableList()
        }

        // Étape 3 : Filtrer par salonId si spécifié (via PostTag)
        if (filters.salonId != null) {
            val postsBySalon = postTagRepository.findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
                com.frollot.model.TaggedType.salon,
                filters.salonId
            ).map { it.post }.filterNotNull()
            filteredPosts = filteredPosts.filter { it.id in postsBySalon.map { p -> p.id } }.toMutableList()
        }

        // Étape 4 : Filtrer par hashtagName si spécifié
        if (filters.hashtagName != null) {
            val normalizedHashtagName = filters.hashtagName.lowercase().trim().removePrefix("#")
            val postsByHashtag = postHashtagRepository.findPostsByHashtagName(normalizedHashtagName)
            filteredPosts = filteredPosts.filter { it.id in postsByHashtag.map { p -> p.id } }.toMutableList()
        }

        // Étape 5 : Phase F.3 - Filtrer par visibilité
        filteredPosts = filteredPosts.filter { isPostVisible(it, currentUserId) }.toMutableList()

        // Étape 5.5 : Phase H.3 - Filtrer les contenus modérés
        filteredPosts = filteredPosts.filter { !it.isHidden && !it.isDeleted }.toMutableList()

        // Étape 5.6 : V041 - Archivage global (masqué pour tous)
        filteredPosts = filteredPosts.filter { !it.isArchived }.toMutableList()

        // Étape 6 : Trier par date décroissante
        filteredPosts.sortByDescending { it.createdAt }

        // Étape 7 : Pagination manuelle
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, filteredPosts.size)
        val paginatedPosts = if (start < filteredPosts.size) {
            filteredPosts.subList(start, end)
        } else {
            emptyList()
        }

        val page = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            filteredPosts.size.toLong()
        )

        // Étape 8 : Convertir en PostResponse
        return page.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Récupère les posts d'un utilisateur.
     * Phase F.3 - Filtre par visibilité : l'utilisateur voit tous ses posts, les autres voient selon la visibilité.
     */
    @Transactional(readOnly = true)
    fun getPostsByUser(userId: String, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        val allPosts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
        
        // Phase F.3 - Filtrer par visibilité (sauf si l'utilisateur consulte son propre profil)
        // Phase H.3 - Filtrer les contenus modérés (sauf pour l'auteur et les admins)
        val filteredPosts = if (currentUserId == userId) {
            // L'utilisateur voit tous ses propres posts (même modérés),
            // sauf les archivés qui vivent dans l'écran Archives (V041)
            allPosts.content
                .filter { !it.isArchived }
        } else {
            // Filtrer selon la visibilité et la modération
            allPosts.content
                .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)
                .filter { isPostVisible(it, currentUserId) }
                .filter { !it.isHidden && !it.isDeleted }
        }
        
        // Pagination manuelle
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, filteredPosts.size)
        val paginatedPosts = if (start < filteredPosts.size) {
            filteredPosts.subList(start, end)
        } else {
            emptyList()
        }
        
        val postsPage = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            filteredPosts.size.toLong()
        )

        return postsPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Récupère les posts des salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     * 
     * Récupère les posts tagués avec des salons situés dans le rayon spécifié.
     * 
     * @param latitude Latitude du point central
     * @param longitude Longitude du point central
     * @param radiusKm Rayon de recherche en kilomètres (défaut: 10 km)
     * @param pageable Pagination
     * @param currentUserId ID de l'utilisateur courant (pour isLiked, isFavorited)
     * @return Page de PostResponse triés par date décroissante
     */
    @Transactional(readOnly = true)
    fun getPostsNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0,
        pageable: Pageable,
        currentUserId: String? = null
    ): Page<PostResponse> {
        // 1. Récupérer les salons dans le rayon
        val nearbySalons = salonRepository.findSalonsNearby(latitude, longitude, radiusKm)
        val nearbySalonIds = nearbySalons.map { it.id!! }.toSet()

        if (nearbySalonIds.isEmpty()) {
            // Aucun salon dans le rayon, retourner une page vide
            return org.springframework.data.domain.PageImpl(emptyList(), pageable, 0)
        }

        // 2. Récupérer les posts tagués avec ces salons
        val allTaggedPosts = mutableSetOf<com.frollot.model.Post>()
        nearbySalonIds.forEach { salonId ->
            val postTags = postTagRepository.findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
                com.frollot.model.TaggedType.salon,
                salonId
            )
            postTags.mapNotNull { it.post }.forEach { post ->
                allTaggedPosts.add(post)
            }
        }

        // 3. Phase F.3 - Filtrer par visibilité
        val visiblePosts = allTaggedPosts
            .filter { !it.isArchived } // V041 - Archivage global (masqué pour tous)
            .filter { isPostVisible(it, currentUserId) }

        // 4. Trier par date décroissante
        val sortedPosts = visiblePosts.sortedByDescending { it.createdAt }

        // 5. Appliquer la pagination manuellement
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, sortedPosts.size)
        val paginatedPosts = sortedPosts.subList(start, end)

        // 6. Convertir en PostResponse
        val postsPage = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            sortedPosts.size.toLong()
        )

        return postsPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Récupère les posts trending (les plus populaires) pour une période donnée.
     * Phase C.3 - Trending Coiffure
     * 
     * Trie les posts par score de popularité (likes * 1 + comments * 2 + shares * 3)
     * pour les posts créés dans la période spécifiée.
     * 
     * @param period Période de trending (24h, 7j, 30j)
     * @param pageable Pagination
     * @param currentUserId ID de l'utilisateur courant (pour isLiked, isFavorited)
     * @return Page de PostResponse triés par popularité
     */
    @Transactional(readOnly = true)
    fun getTrendingPosts(
        period: com.frollot.model.TrendPeriod,
        pageable: Pageable,
        currentUserId: String? = null
    ): Page<PostResponse> {
        // Calculer la date de début selon la période
        val sinceDate = when (period) {
            com.frollot.model.TrendPeriod.LAST_24H -> java.time.LocalDateTime.now().minusHours(24)
            com.frollot.model.TrendPeriod.LAST_7D -> java.time.LocalDateTime.now().minusDays(7)
            com.frollot.model.TrendPeriod.LAST_30D -> java.time.LocalDateTime.now().minusDays(30)
        }

        // Récupérer les posts trending depuis le repository
        val postsPage = postRepository.findTrendingPosts(sinceDate, pageable)

        // Convertir en PostResponse
        return postsPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Récupère les posts d'un salon avec filtres et tri.
     * Phase C.2 - Feed par Salon
     * 
     * Récupère uniquement les posts tagués avec le salon (via PostTag).
     * 
     * @param salonId ID du salon
     * @param postType Filtre par type de post (optionnel)
     * @param serviceId Filtre par service associé (optionnel)
     * @param sortBy Tri par récence ou popularité
     * @param pageable Pagination
     * @param currentUserId ID de l'utilisateur courant (pour isLiked, isFavorited)
     * @return Page de PostResponse
     */
    @Transactional(readOnly = true)
    fun getPostsBySalon(
        salonId: String,
        postType: PostType? = null,
        serviceId: String? = null,
        sortBy: SortBy = SortBy.RECENT,
        pageable: Pageable,
        currentUserId: String? = null
    ): Page<PostResponse> {
        // Vérifier que le salon existe
        if (!salonRepository.existsById(salonId)) {
            throw RuntimeException("Salon avec ID '$salonId' non trouvé")
        }

        // Étape 1 : Récupérer tous les posts tagués avec le salon
        val postTags = postTagRepository.findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
            TaggedType.salon,
            salonId
        )
        var posts = postTags.mapNotNull { it.post }.distinctBy { it.id }

        // Étape 2 : Filtrer par type de post si spécifié
        if (postType != null) {
            posts = posts.filter { it.postType == postType }
        }

        // Étape 3 : Filtrer par service si spécifié
        if (serviceId != null) {
            val postsByService = postServiceRepository.findPostsByServiceId(serviceId)
            val servicePostIds = postsByService.map { it.id }.toSet()
            posts = posts.filter { it.id in servicePostIds }
        }

        // Étape 4 : Phase F.3 - Filtrer par visibilité
        posts = posts.filter { isPostVisible(it, currentUserId) }

        // Étape 4.5 : Phase H.3 - Filtrer les contenus modérés
        posts = posts.filter { !it.isHidden && !it.isDeleted }

        // Étape 4.6 : V041 - Archivage global (masqué pour tous)
        posts = posts.filter { !it.isArchived }

        // Étape 5 : Trier selon sortBy
        posts = when (sortBy) {
            SortBy.RECENT -> {
                // Déjà triés par date décroissante par le repository
                posts
            }
            SortBy.POPULAR -> {
                // Trier par score de popularité : likes * 1 + comments * 2 + shares * 3
                posts.sortedByDescending { post ->
                    val likes = post.likesCount
                    val comments = post.commentsCount
                    val shares = post.sharesCount
                    likes * 1 + comments * 2 + shares * 3
                }
            }
        }

        // Étape 6 : Pagination manuelle
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, posts.size)
        val paginatedPosts = if (start < posts.size) {
            posts.subList(start, end)
        } else {
            emptyList()
        }

        val page = org.springframework.data.domain.PageImpl(
            paginatedPosts,
            pageable,
            posts.size.toLong()
        )

        // Étape 7 : Convertir en PostResponse
        return page.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    /**
     * Supprime un post.
     */
    @Transactional
    fun deletePost(postId: String, userId: String) {
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification des autorisations
        if (post.author?.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        // Suppression des likes et commentaires associés
        postLikeRepository.deleteByPostId(postId)
        commentRepository.deleteByPostId(postId)

        // Suppression du post
        postRepository.delete(post)

        println("🗑️ Post supprimé: $postId")
    }

    // ========== GESTION DES LIKES ==========

    /**
     * Toggle le like d'un utilisateur sur un post.
     * 
     * Si l'utilisateur a déjà liké, supprime le like.
     * Sinon, ajoute un like.
     * 
     * Met à jour atomiquement le compteur de likes.
     * 
     * @return PostResponse avec isLikedByCurrentUser correctement mis à jour
     */
    @Transactional
    fun toggleLike(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'existence de l'utilisateur
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        // Vérifier si l'utilisateur a déjà liké
        val existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
        
        // Déterminer l'état final après le toggle
        val willBeLiked: Boolean

        if (existingLike != null) {
            // Supprimer le like
            postLikeRepository.delete(existingLike)
            post.likesCount = (post.likesCount - 1).coerceAtLeast(0)
            postRepository.save(post)
            willBeLiked = false
            println("👎 Like retiré: Post $postId par User $userId")
        } else {
            // Ajouter le like
            val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }
            
            val newLike = PostLike(
                id = UUID.randomUUID().toString(),
                post = post,
                user = user
            )

            if (!newLike.isValid()) {
                throw IllegalStateException("Impossible de créer un like invalide")
            }

            postLikeRepository.save(newLike)
            post.likesCount += 1
            postRepository.save(post)
            willBeLiked = true
            println("👍 Like ajouté: Post $postId par User $userId")
        }

        // Vérification de cohérence : le compteur doit correspondre au nombre réel de likes
        val actualLikesCount = postLikeRepository.countByPostId(postId).toInt()
        if (post.likesCount != actualLikesCount) {
            println("⚠️ Incohérence détectée: likesCount=${post.likesCount} mais count réel=$actualLikesCount. Correction automatique.")
            post.likesCount = actualLikesCount
            postRepository.save(post)
        }

        // Récupérer le statut final
        val isFavorited = postFavoriteRepository.existsByPostIdAndUserId(postId, userId)
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = willBeLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = false,
            commentsCount = commentsCount,
            sharesCount = 0,
            tags = tags
        )
    }

    /**
     * Vérifie si un utilisateur a liké un post.
     */
    @Transactional(readOnly = true)
    fun isPostLikedByUser(postId: String, userId: String): Boolean {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId)
    }

    // ========== GESTION DES FAVORIS ==========

    /**
     * Ajoute ou retire un favori sur un post (toggle).
     * 
     * @return PostResponse avec isFavoritedByCurrentUser correctement mis à jour
     */
    @Transactional
    fun toggleFavorite(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'existence de l'utilisateur
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        // Vérifier si l'utilisateur a déjà mis en favori
        val existingFavorite = postFavoriteRepository.findByPostIdAndUserId(postId, userId)
        
        // Déterminer l'état final après le toggle
        val willBeFavorited: Boolean

        if (existingFavorite != null) {
            // Supprimer le favori
            postFavoriteRepository.delete(existingFavorite)
            willBeFavorited = false
            println("📌 Favori retiré: Post $postId par User $userId")
        } else {
            // Ajouter le favori
            val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }
            
            val newFavorite = PostFavorite(
                id = UUID.randomUUID().toString(),
                post = post,
                user = user
            )

            if (!newFavorite.isValid()) {
                throw IllegalStateException("Impossible de créer un favori invalide")
            }

            postFavoriteRepository.save(newFavorite)
            willBeFavorited = true
            println("⭐ Favori ajouté: Post $postId par User $userId")
        }

        // Récupérer le statut final (likes et favoris)
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = willBeFavorited,
            isSharedByCurrentUser = false,
            commentsCount = commentsCount,
            sharesCount = 0,
            tags = tags
        )
    }

    /**
     * Récupère les favoris d'un utilisateur avec pagination.
     */
    @Transactional(readOnly = true)
    fun getFavoritesByUser(userId: String, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        // V041 - Archivage global : un post archivé n'apparaît dans les favoris de personne
        // (exclusion simple, y compris pour l'auteur — cohérent avec "masqué pour tous").
        // Filtrage en mémoire + pagination manuelle (même approche que getFeed).
        val allFavorites = postFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
            .content
            .filter { it.post?.isArchived == false }
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, allFavorites.size)
        val favoritesPage = org.springframework.data.domain.PageImpl(
            if (start < allFavorites.size) allFavorites.subList(start, end) else emptyList(),
            pageable,
            allFavorites.size.toLong()
        )

        return favoritesPage.map { favorite ->
            val post = favorite.post!!
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    // ========== GESTION DES ARCHIVES ==========

    /**
     * Archive un post (archivage global, façon Instagram).
     * V041 : un post archivé est masqué pour TOUS les utilisateurs.
     * Seul l'auteur du post peut l'archiver. Idempotent.
     *
     * @return PostResponse mis à jour
     */
    @Transactional
    fun archivePost(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'ownership : seul l'auteur peut archiver
        if (post.author?.id != userId) {
            throw UnauthorizedAccessException("Seul l'auteur du post peut l'archiver")
        }

        if (!post.isArchived) {
            post.isArchived = true
            post.archivedAt = java.time.LocalDateTime.now()
            postRepository.save(post)
            println("📦 Post archivé (global): Post $postId par User $userId")
        }

        return buildPostResponse(post, userId)
    }

    /**
     * Désarchive un post (archivage global, façon Instagram).
     * V041 : le post redevient visible pour tous.
     * Seul l'auteur du post peut le désarchiver. Idempotent.
     *
     * @return PostResponse mis à jour
     */
    @Transactional
    fun unarchivePost(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'ownership : seul l'auteur peut désarchiver
        if (post.author?.id != userId) {
            throw UnauthorizedAccessException("Seul l'auteur du post peut le désarchiver")
        }

        if (post.isArchived) {
            post.isArchived = false
            post.archivedAt = null
            postRepository.save(post)
            println("📤 Post désarchivé (global): Post $postId par User $userId")
        }

        return buildPostResponse(post, userId)
    }

    /**
     * Récupère les posts archivés d'un utilisateur avec pagination.
     * V041 : lit le flag isArchived sur posts (post_archives dormante).
     * URL et forme de réponse inchangées.
     */
    @Transactional(readOnly = true)
    fun getArchivedPosts(userId: String, pageable: Pageable, currentUserId: String? = null): Page<PostResponse> {
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        val archivedPage = postRepository.findByAuthorIdAndIsArchivedTrueOrderByArchivedAtDesc(userId, pageable)

        return archivedPage.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
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
                media = media
            )
        }
    }

    // ========== GESTION DES PARTAGES ==========
    // Phase D.3 - Partage de Posts (Reposts)

    /**
     * Partage un post avec un commentaire optionnel.
     * 
     * Si l'utilisateur a déjà partagé, lance une exception.
     * Sinon, crée un nouveau partage.
     * 
     * Met à jour atomiquement le compteur de partages.
     * 
     * @param postId ID du post à partager
     * @param userId ID de l'utilisateur qui partage
     * @param sharedContent Commentaire optionnel (max 500 caractères)
     * @return PostResponse avec isSharedByCurrentUser = true et sharesCount mis à jour
     */
    @Transactional
    fun sharePost(postId: String, userId: String, sharedContent: String? = null): PostResponse {
        // Validation du commentaire
        if (sharedContent != null && sharedContent.length > 500) {
            throw IllegalArgumentException("Le commentaire de partage ne peut pas dépasser 500 caractères")
        }

        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'existence de l'utilisateur
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }

        // Vérifier si l'utilisateur a déjà partagé
        val existingShare = postShareRepository.findByPostIdAndUserId(postId, userId)
        if (existingShare != null) {
            throw IllegalStateException("L'utilisateur a déjà partagé ce post")
        }

        // Créer le partage
        val newShare = PostShare(
            id = UUID.randomUUID().toString(),
            post = post,
            user = user,
            sharedContent = sharedContent?.takeIf { it.isNotBlank() }
        )

        if (!newShare.isValid()) {
            throw IllegalStateException("Impossible de créer un partage invalide")
        }

        postShareRepository.save(newShare)
        post.sharesCount += 1
        postRepository.save(post)
        println("📤 Post partagé: Post $postId par User $userId")

        // Vérification de cohérence : le compteur doit correspondre au nombre réel de partages
        val actualSharesCount = postShareRepository.countByPostId(postId).toInt()
        if (post.sharesCount != actualSharesCount) {
            println("⚠️ Incohérence détectée: sharesCount=${post.sharesCount} mais count réel=$actualSharesCount. Correction automatique.")
            post.sharesCount = actualSharesCount
            postRepository.save(post)
        }

        // Récupérer le statut final
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val isFavorited = postFavoriteRepository.existsByPostIdAndUserId(postId, userId)
        val isShared = true
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val sharesCount = post.sharesCount
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        val services = postServiceRepository.findServicesByPostId(postId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
        val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
            .map { HairHashtagResponse.fromEntity(it) }
        val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
            .map { PostMediaResponse.fromEntity(it) }
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = isShared,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            tags = tags,
            services = services,
            hashtags = hashtags,
            media = media
        )
    }

    /**
     * Annule le partage d'un post par un utilisateur.
     * 
     * @param postId ID du post
     * @param userId ID de l'utilisateur
     * @return PostResponse avec isSharedByCurrentUser = false et sharesCount mis à jour
     */
    @Transactional
    fun unsharePost(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'existence de l'utilisateur
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        // Vérifier si l'utilisateur a partagé
        val existingShare = postShareRepository.findByPostIdAndUserId(postId, userId)
        if (existingShare == null) {
            throw IllegalStateException("L'utilisateur n'a pas partagé ce post")
        }

        // Supprimer le partage
        postShareRepository.delete(existingShare)
        post.sharesCount = (post.sharesCount - 1).coerceAtLeast(0)
        postRepository.save(post)
        println("📥 Partage annulé: Post $postId par User $userId")

        // Vérification de cohérence : le compteur doit correspondre au nombre réel de partages
        val actualSharesCount = postShareRepository.countByPostId(postId).toInt()
        if (post.sharesCount != actualSharesCount) {
            println("⚠️ Incohérence détectée: sharesCount=${post.sharesCount} mais count réel=$actualSharesCount. Correction automatique.")
            post.sharesCount = actualSharesCount
            postRepository.save(post)
        }

        // Récupérer le statut final
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val isFavorited = postFavoriteRepository.existsByPostIdAndUserId(postId, userId)
        val isShared = false
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val sharesCount = post.sharesCount
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        val services = postServiceRepository.findServicesByPostId(postId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
        val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
            .map { HairHashtagResponse.fromEntity(it) }
        val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
            .map { PostMediaResponse.fromEntity(it) }
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = isShared,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            tags = tags,
            services = services,
            hashtags = hashtags,
            media = media
        )
    }

    /**
     * Vérifie si un utilisateur a partagé un post.
     */
    @Transactional(readOnly = true)
    fun isPostSharedByUser(postId: String, userId: String): Boolean {
        return postShareRepository.existsByPostIdAndUserId(postId, userId)
    }

    /**
     * Récupère les partages d'un post avec pagination.
     */
    @Transactional(readOnly = true)
    fun getSharesByPost(postId: String, pageable: Pageable): Page<PostShareResponse> {
        // Vérification de l'existence du post
        if (!postRepository.existsById(postId)) {
            throw PostNotFoundException(postId)
        }

        val sharesPage = postShareRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
        return sharesPage.map { PostShareResponse.fromEntity(it) }
    }

    // ========== GESTION DES RÉACTIONS SPÉCIALISÉES ==========
    // Phase D.4 - Réactions Spécialisées Coiffure

    /**
     * Ajoute ou modifie une réaction sur un post.
     * Si l'utilisateur a déjà une réaction, elle est remplacée par la nouvelle.
     * 
     * @param postId ID du post
     * @param userId ID de l'utilisateur
     * @param reactionType Type de réaction
     * @return PostResponse avec reactions et currentUserReaction mis à jour
     */
    @Transactional
    fun addReaction(postId: String, userId: String, reactionType: com.frollot.model.ReactionType): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'existence de l'utilisateur
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }

        // Vérifier si l'utilisateur a déjà une réaction
        val existingReaction = postReactionRepository.findByPostIdAndUserId(postId, userId)
        
        if (existingReaction != null) {
            // Remplacer la réaction existante
            existingReaction.reactionType = reactionType
            postReactionRepository.save(existingReaction)
            println("🔄 Réaction modifiée: Post $postId par User $userId → $reactionType")
        } else {
            // Créer une nouvelle réaction
            val newReaction = com.frollot.model.PostReaction(
                id = UUID.randomUUID().toString(),
                post = post,
                user = user,
                reactionType = reactionType
            )

            if (!newReaction.isValid()) {
                throw IllegalStateException("Impossible de créer une réaction invalide")
            }

            postReactionRepository.save(newReaction)
            println("👍 Réaction ajoutée: Post $postId par User $userId → $reactionType")
        }

        // Récupérer le statut final
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val isFavorited = postFavoriteRepository.existsByPostIdAndUserId(postId, userId)
        val isShared = postShareRepository.existsByPostIdAndUserId(postId, userId)
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val sharesCount = post.sharesCount
        val reactions = getReactionsByPost(postId)
        val currentUserReaction = reactionType
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        val services = postServiceRepository.findServicesByPostId(postId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
        val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
            .map { HairHashtagResponse.fromEntity(it) }
        val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
            .map { PostMediaResponse.fromEntity(it) }
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = isShared,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            reactions = reactions,
            currentUserReaction = currentUserReaction,
            tags = tags,
            services = services,
            hashtags = hashtags,
            media = media
        )
    }

    /**
     * Supprime la réaction d'un utilisateur sur un post.
     * 
     * @param postId ID du post
     * @param userId ID de l'utilisateur
     * @return PostResponse avec reactions et currentUserReaction mis à jour
     */
    @Transactional
    fun removeReaction(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification de l'existence de l'utilisateur
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        // Vérifier si l'utilisateur a une réaction
        val existingReaction = postReactionRepository.findByPostIdAndUserId(postId, userId)
        if (existingReaction == null) {
            throw IllegalStateException("L'utilisateur n'a pas réagi à ce post")
        }

        // Supprimer la réaction
        postReactionRepository.delete(existingReaction)
        println("👎 Réaction supprimée: Post $postId par User $userId")

        // Récupérer le statut final
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val isFavorited = postFavoriteRepository.existsByPostIdAndUserId(postId, userId)
        val isShared = postShareRepository.existsByPostIdAndUserId(postId, userId)
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val sharesCount = post.sharesCount
        val reactions = getReactionsByPost(postId)
        val currentUserReaction: com.frollot.model.ReactionType? = null
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        val services = postServiceRepository.findServicesByPostId(postId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
        val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
            .map { HairHashtagResponse.fromEntity(it) }
        val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
            .map { PostMediaResponse.fromEntity(it) }
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = isShared,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            reactions = reactions,
            currentUserReaction = currentUserReaction,
            tags = tags,
            services = services,
            hashtags = hashtags,
            media = media
        )
    }

    /**
     * Récupère les compteurs de réactions par type pour un post.
     * 
     * @param postId ID du post
     * @return Map<ReactionType, Int> avec le nombre de réactions par type
     */
    @Transactional(readOnly = true)
    fun getReactionsByPost(postId: String): Map<com.frollot.model.ReactionType, Int> {
        val reactions = postReactionRepository.findByPostId(postId)
        return reactions.groupingBy { it.reactionType }
            .eachCount()
            .mapValues { it.value }
    }

    /**
     * Récupère la réaction d'un utilisateur sur un post.
     * 
     * @param postId ID du post
     * @param userId ID de l'utilisateur
     * @return ReactionType ou null si l'utilisateur n'a pas réagi
     */
    @Transactional(readOnly = true)
    fun getUserReaction(postId: String, userId: String): com.frollot.model.ReactionType? {
        val reaction = postReactionRepository.findByPostIdAndUserId(postId, userId)
        return reaction?.reactionType
    }

    // ========== GESTION DES COMMENTAIRES ==========

    /**
     * Crée un nouveau commentaire.
     */
    @Transactional
    fun createComment(request: CreateCommentRequest): CommentResponse {
        // Validation
        request.validate()

        // Vérification de l'existence du post
        val post = postRepository.findById(request.postId)
            .orElseThrow { PostNotFoundException(request.postId) }

        // Vérification de l'existence de l'auteur
        val author = userRepository.findById(request.authorId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '${request.authorId}' non trouvé") }

        // Création du commentaire
        val comment = Comment(
            id = UUID.randomUUID().toString(),
            post = post,
            author = author,
            content = request.content.trim()
        )

        // Validation de l'entité
        if (!comment.isValid()) {
            throw IllegalArgumentException("Les données du commentaire sont invalides")
        }

        // Sauvegarde
        val savedComment = commentRepository.save(comment)

        println("💬 Commentaire créé: ${savedComment.id} sur Post ${request.postId}")

        return CommentResponse.fromEntity(savedComment)
    }

    /**
     * Récupère les commentaires d'un post.
     */
    @Transactional(readOnly = true)
    fun getCommentsByPost(postId: String, pageable: Pageable): Page<CommentResponse> {
        // Vérification de l'existence du post
        if (!postRepository.existsById(postId)) {
            throw PostNotFoundException(postId)
        }

        val commentsPage = commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)

        return commentsPage.map { CommentResponse.fromEntity(it) }
    }

    /**
     * Supprime un commentaire.
     */
    @Transactional
    fun deleteComment(commentId: String, userId: String) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { CommentNotFoundException(commentId) }

        // Vérification des autorisations
        if (comment.author?.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        val postId = comment.post!!.id!!
        commentRepository.delete(comment)

        // Mettre à jour le compteur de commentaires du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }
        post.commentsCount = commentRepository.countByPostId(postId).toInt()
        postRepository.save(post)

        println("🗑️ Commentaire supprimé: $commentId")
    }

    // ========== GESTION DES TAGS/MENTIONS ==========

    /**
     * Ajoute un tag (salon ou utilisateur) à un post.
     * 
     * Vérifie que :
     * - L'utilisateur est l'auteur du post
     * - L'entité taguée existe (salon ou utilisateur)
     * - Le tag n'existe pas déjà
     */
    @Transactional
    fun addTag(postId: String, taggedType: TaggedType, taggedId: String, userId: String): TagResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification que l'utilisateur est l'auteur du post
        if (post.author?.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        // Vérification que l'entité taguée existe
        when (taggedType) {
            TaggedType.salon -> {
                if (!salonRepository.existsById(taggedId)) {
                    throw TaggedEntityNotFoundException("Salon avec ID '$taggedId' non trouvé")
                }
            }
            TaggedType.user -> {
                if (!userRepository.existsById(taggedId)) {
                    throw TaggedEntityNotFoundException("Utilisateur avec ID '$taggedId' non trouvé")
                }
            }
        }

        // Vérification que le tag n'existe pas déjà
        if (postTagRepository.existsByPostIdAndTaggedTypeAndTaggedId(postId, taggedType, taggedId)) {
            throw TagAlreadyExistsException("Ce tag existe déjà pour ce post")
        }

        // Création du tag
        val tag = PostTag(
            id = UUID.randomUUID().toString(),
            post = post,
            taggedType = taggedType,
            taggedId = taggedId
        )

        // Validation
        if (!tag.isValid()) {
            throw IllegalArgumentException("Les données du tag sont invalides")
        }

        // Sauvegarde
        val savedTag = postTagRepository.save(tag)

        println("🏷️ Tag ajouté: Post $postId, Type $taggedType, ID $taggedId")

        return buildTagResponse(savedTag)
    }

    /**
     * Supprime un tag d'un post.
     * 
     * Vérifie que l'utilisateur est l'auteur du post.
     */
    @Transactional
    fun removeTag(tagId: String, userId: String) {
        val tag = postTagRepository.findById(tagId)
            .orElseThrow { TagNotFoundException(tagId) }

        // Vérification que l'utilisateur est l'auteur du post
        if (tag.post?.author?.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        postTagRepository.delete(tag)

        println("🗑️ Tag supprimé: $tagId")
    }

    /**
     * Récupère tous les tags d'un post.
     */
    @Transactional(readOnly = true)
    fun getTagsByPost(postId: String): List<TagResponse> {
        // Vérification de l'existence du post
        if (!postRepository.existsById(postId)) {
            throw PostNotFoundException(postId)
        }

        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)

        return tags.map { buildTagResponse(it) }
    }

    // ========== Phase E.1 - Profil Coiffeur Enrichi ==========

    /**
     * Récupère le profil enrichi d'un coiffeur.
     * Phase E.1 - Profil Coiffeur Enrichi
     */
    @Transactional(readOnly = true)
    fun getCoiffeurProfile(coiffeurId: String, currentUserId: String? = null): CoiffeurProfileResponse {
        // Vérifier que l'utilisateur existe et est un coiffeur
        val coiffeur = userRepository.findById(coiffeurId)
            .orElseThrow { RuntimeException("Coiffeur avec ID '$coiffeurId' non trouvé") }

        if (coiffeur.userType != UserType.hairstylist) {
            throw IllegalArgumentException("L'utilisateur avec ID '$coiffeurId' n'est pas un coiffeur")
        }

        // Récupérer le portfolio mis en avant
        val portfolioHighlighted = coiffeur.portfolioHighlighted?.let { portfolio ->
            val postsCount = portfolioPostRepository.countByPortfolioId(portfolio.id!!).toInt()
            PortfolioResponse.fromEntity(portfolio, postsCount)
        }

        // Récupérer tous les portfolios du coiffeur (publics uniquement si pas le propriétaire)
        val isOwner = currentUserId == coiffeurId
        val portfolios = if (isOwner) {
            portfolioRepository.findByOwnerIdAndOwnerTypeOrderByCreatedAtDesc(
                coiffeurId,
                PortfolioOwnerType.coiffeur
            )
        } else {
            portfolioRepository.findByOwnerIdAndOwnerTypeAndIsPublicTrueOrderByCreatedAtDesc(
                coiffeurId,
                PortfolioOwnerType.coiffeur
            )
        }.map { portfolio ->
            val postsCount = portfolioPostRepository.countByPortfolioId(portfolio.id!!).toInt()
            PortfolioResponse.fromEntity(portfolio, postsCount)
        }

        // Récupérer les posts récents (5 derniers)
        val recentPosts = getPostsByUser(coiffeurId, org.springframework.data.domain.PageRequest.of(0, 5), currentUserId)
            .content

        // Calculer les statistiques
        val postsCount = postRepository.countByAuthorId(coiffeurId)
        val totalLikes = postRepository.sumLikesByAuthorId(coiffeurId) ?: 0L
        val followersCount = followRepository.countByFollowingTypeAndFollowingId(
            FollowingType.COIFFEUR,
            coiffeurId
        )
        val followingCount = followRepository.countByFollowerId(coiffeurId)

        // Note coiffeur : agrégation sur tous les staffIds (multi-salon)
        val staffIds = salonStaffRepository.findByUserId(coiffeurId).mapNotNull { it.id }
        val averageRating = if (staffIds.isNotEmpty()) reviewRepository.findAverageRatingByStaffIds(staffIds) else java.math.BigDecimal.ZERO
        val totalReviews = if (staffIds.isNotEmpty()) reviewRepository.countByStaffIdsAndIsVisibleTrue(staffIds).toInt() else 0

        val statistics = CoiffeurProfileStatistics(
            postsCount = postsCount,
            totalLikes = totalLikes,
            followersCount = followersCount,
            followingCount = followingCount,
            averageRating = averageRating,
            totalReviews = totalReviews
        )

        // Vérifier si l'utilisateur courant suit ce coiffeur
        val isFollowedByCurrentUser = if (currentUserId != null) {
            followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                currentUserId,
                FollowingType.COIFFEUR,
                coiffeurId
            )
        } else {
            null
        }

        // Phase E.3 - Récupérer les badges affichés du coiffeur
        val badges = userBadgeRepository.findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(coiffeurId)
            .map { UserBadgeResponse.fromEntity(it) }

        return CoiffeurProfileResponse.fromUser(
            user = coiffeur,
            portfolioHighlighted = portfolioHighlighted,
            statistics = statistics,
            portfolios = portfolios,
            recentPosts = recentPosts,
            badges = badges,
            isFollowedByCurrentUser = isFollowedByCurrentUser
        )
    }

    /**
     * Met à jour le profil enrichi d'un coiffeur.
     * Phase E.1 - Profil Coiffeur Enrichi
     */
    @Transactional
    fun updateCoiffeurProfile(
        coiffeurId: String,
        request: UpdateCoiffeurProfileRequest,
        currentUserId: String
    ): CoiffeurProfileResponse {
        // Validation
        request.validate()

        // Vérifier que l'utilisateur existe et est un coiffeur
        val coiffeur = userRepository.findById(coiffeurId)
            .orElseThrow { RuntimeException("Coiffeur avec ID '$coiffeurId' non trouvé") }

        if (coiffeur.userType != UserType.hairstylist) {
            throw IllegalArgumentException("L'utilisateur avec ID '$coiffeurId' n'est pas un coiffeur")
        }

        // Vérifier l'ownership
        if (coiffeur.id != currentUserId) {
            throw UnauthorizedAccessException(currentUserId)
        }

        // Mettre à jour les champs
        request.bio?.let { coiffeur.bio = it.trim().takeIf { it.isNotBlank() } }
        request.yearsExperience?.let { coiffeur.yearsExperience = it }
        request.certifications?.let { coiffeur.certifications = it.trim().takeIf { it.isNotBlank() } }
        request.instagramHandle?.let { handle ->
            coiffeur.instagramHandle = handle.trim().removePrefix("@").takeIf { it.isNotBlank() }
        }

        // Mettre à jour les spécialités
        if (request.specialties.isNotEmpty()) {
            coiffeur.specialties.clear()
            coiffeur.specialties.addAll(request.specialties.map { it.trim() }.filter { it.isNotBlank() })
        }

        // Mettre à jour le portfolio mis en avant
        request.portfolioHighlightedId?.let { portfolioId ->
            // Vérifier que le portfolio existe et appartient au coiffeur
            val portfolio = portfolioRepository.findByIdAndOwnerIdAndOwnerType(
                portfolioId,
                coiffeurId,
                PortfolioOwnerType.coiffeur
            ) ?: throw RuntimeException("Portfolio avec ID '$portfolioId' non trouvé ou n'appartient pas au coiffeur")
            coiffeur.portfolioHighlighted = portfolio
        } ?: run {
            // Si null, retirer le portfolio mis en avant
            coiffeur.portfolioHighlighted = null
        }

        // Sauvegarder
        val savedCoiffeur = userRepository.save(coiffeur)

        // Retourner le profil mis à jour
        return getCoiffeurProfile(coiffeurId, currentUserId)
    }

    // ========== Phase E.2 - Profil Salon Social ==========

    /**
     * Récupère le profil social enrichi d'un salon.
     * Phase E.2 - Profil Salon Social
     */
    @Transactional(readOnly = true)
    fun getSalonSocialProfile(salonId: String, currentUserId: String? = null): SalonSocialProfileResponse {
        // Vérifier que le salon existe
        val salon = salonRepository.findById(salonId)
            .orElseThrow { RuntimeException("Salon avec ID '$salonId' non trouvé") }

        // Récupérer les posts mis en avant
        val highlightedPostEntities = salonHighlightedPostRepository.findBySalonIdOrderByOrderIndexAsc(salonId)
        val highlightedPosts = highlightedPostEntities.mapNotNull { highlightedPost ->
            val post = highlightedPost.post ?: return@mapNotNull null
            if (post.isArchived) return@mapNotNull null // V041 - Archivage global (masqué pour tous)
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val sharesCount = postShareRepository.countByPostId(postId).toInt()
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
            val reactions = getReactionsByPost(postId)
            val currentUserReaction = currentUserId?.let { getUserReaction(postId, it) }
            PostResponse.fromEntity(
                post,
                isLikedByCurrentUser = isLiked,
                isFavoritedByCurrentUser = isFavorited,
                isSharedByCurrentUser = false,
                commentsCount = commentsCount,
                sharesCount = sharesCount,
                tags = tags,
                services = services,
                hashtags = hashtags,
                media = media,
                reactions = reactions,
                currentUserReaction = currentUserReaction
            )
        }

        // Récupérer tous les portfolios du salon (publics uniquement si pas le propriétaire)
        val isOwner = currentUserId != null && salon.owner?.id == currentUserId
        val portfolios = if (isOwner) {
            portfolioRepository.findByOwnerIdAndOwnerTypeOrderByCreatedAtDesc(
                salonId,
                PortfolioOwnerType.salon
            )
        } else {
            portfolioRepository.findByOwnerIdAndOwnerTypeAndIsPublicTrueOrderByCreatedAtDesc(
                salonId,
                PortfolioOwnerType.salon
            )
        }.map { portfolio ->
            val postsCount = portfolioPostRepository.countByPortfolioId(portfolio.id!!).toInt()
            PortfolioResponse.fromEntity(portfolio, postsCount)
        }

        // Récupérer les posts récents (5 derniers)
        val recentPosts = getPostsBySalon(
            salonId,
            pageable = org.springframework.data.domain.PageRequest.of(0, 5),
            currentUserId = currentUserId
        ).content

        // Calculer les statistiques
        val postTags = postTagRepository.findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
            TaggedType.salon,
            salonId
        )
        val postsCount = postTags.mapNotNull { it.post?.id }.distinct().size.toLong()
        
        // Calculer le total des likes sur tous les posts du salon
        val allPostIds = postTags.mapNotNull { it.post?.id }.distinct()
        val totalLikes = allPostIds.sumOf { postId ->
            postRepository.findById(postId).orElse(null)?.likesCount?.toLong() ?: 0L
        }
        
        val followersCount = followRepository.countByFollowingTypeAndFollowingId(
            FollowingType.SALON,
            salonId
        )

        val statistics = SalonSocialProfileStatistics(
            postsCount = postsCount,
            totalLikes = totalLikes,
            followersCount = followersCount,
            averageRating = salon.ratingAverage,
            totalReviews = salon.totalReviews
        )

        // Récupérer l'équipe (coiffeurs actifs)
        val staffList = salonStaffRepository.findActiveBySalonId(salonId)
        val team = staffList.mapNotNull { staff ->
            val user = staff.user ?: return@mapNotNull null
            UserResponse.fromEntity(user)
        }

        // Récupérer les services du salon
        val services = salonServiceRepository.findBySalonId(salonId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }

        // Vérifier si l'utilisateur courant suit ce salon
        val isFollowedByCurrentUser = if (currentUserId != null) {
            followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                currentUserId,
                FollowingType.SALON,
                salonId
            )
        } else {
            null
        }

        return SalonSocialProfileResponse.fromSalon(
            salon = salon,
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

    /**
     * Récupère le profil enrichi d'un client.
     * Phase E.4 - Profil Client
     */
    @Transactional(readOnly = true)
    fun getClientProfile(clientId: String, currentUserId: String? = null): ClientProfileResponse {
        // Vérifier que l'utilisateur existe et est un client
        val client = userRepository.findById(clientId)
            .orElseThrow { RuntimeException("Client avec ID '$clientId' non trouvé") }

        if (client.userType != UserType.client) {
            throw IllegalArgumentException("L'utilisateur avec ID '$clientId' n'est pas un client")
        }

        // Récupérer les posts récents (5 derniers)
        val recentPosts = getPostsByUser(clientId, org.springframework.data.domain.PageRequest.of(0, 5), currentUserId)
            .content

        // Calculer les statistiques
        val postsCount = postRepository.countByAuthorId(clientId)
        val totalLikes = postRepository.sumLikesByAuthorId(clientId) ?: 0L
        val followersCount = followRepository.countByFollowingTypeAndFollowingId(
            FollowingType.USER,
            clientId
        )
        val followingCount = followRepository.countByFollowerId(clientId)
        
        // Compter les collections (publics uniquement si pas le propriétaire)
        val isOwner = currentUserId == clientId
        val collectionsCount = if (isOwner) {
            collectionRepository.countByUserId(clientId)
        } else {
            collectionRepository.countByUserIdAndIsPublicTrue(clientId)
        }
        
        // Compter les réservations (seulement pour le propriétaire)
        val bookingsCount = if (isOwner) {
            // Note: On pourrait ajouter un BookingRepository si nécessaire
            // Pour l'instant, on retourne 0
            0L
        } else {
            0L
        }

        val statistics = ClientProfileStatistics(
            postsCount = postsCount,
            totalLikes = totalLikes,
            followersCount = followersCount,
            followingCount = followingCount,
            collectionsCount = collectionsCount,
            bookingsCount = bookingsCount
        )

        // Vérifier si l'utilisateur courant suit ce client
        val isFollowedByCurrentUser = if (currentUserId != null) {
            followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                currentUserId,
                FollowingType.USER,
                clientId
            )
        } else {
            null
        }

        // Récupérer les collections (publics uniquement si pas le propriétaire)
        val collections = if (isOwner) {
            collectionRepository.findByUserIdOrderByCreatedAtDesc(clientId)
        } else {
            collectionRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(clientId)
        }.map { CollectionResponse.fromEntity(it) }

        // Récupérer les badges affichés du client
        val badges = userBadgeRepository.findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(clientId)
            .map { UserBadgeResponse.fromEntity(it) }

        return ClientProfileResponse.fromUser(
            user = client,
            statistics = statistics,
            recentPosts = recentPosts,
            collections = collections,
            badges = badges,
            isFollowedByCurrentUser = isFollowedByCurrentUser
        )
    }

    /**
     * Récupère le profil enrichi d'un propriétaire de salon.
     * Phase E.5 - Profil Propriétaire de Salon
     */
    @Transactional(readOnly = true)
    fun getSalonOwnerProfile(ownerId: String, currentUserId: String? = null): SalonOwnerProfileResponse {
        // Vérifier que l'utilisateur existe et est un propriétaire de salon
        val owner = userRepository.findById(ownerId)
            .orElseThrow { RuntimeException("Propriétaire avec ID '$ownerId' non trouvé") }

        if (owner.userType != UserType.salon_owner) {
            throw IllegalArgumentException("L'utilisateur avec ID '$ownerId' n'est pas un propriétaire de salon")
        }

        // Récupérer les salons possédés (triés par date de création décroissante)
        val ownedSalons = salonRepository.findByOwnerId(ownerId).sortedByDescending { it.createdAt }
        val salons = ownedSalons.map { salon ->
            val followersCount = followRepository.countByFollowingTypeAndFollowingId(
                FollowingType.SALON,
                salon.id!!
            )
            SalonSummaryResponse.fromSalon(salon, followersCount)
        }

        // Récupérer les posts récents (5 derniers)
        val recentPosts = getPostsByUser(ownerId, org.springframework.data.domain.PageRequest.of(0, 5), currentUserId)
            .content

        // Calculer les statistiques
        val postsCount = postRepository.countByAuthorId(ownerId)
        val totalLikes = postRepository.sumLikesByAuthorId(ownerId) ?: 0L
        val followersCount = followRepository.countByFollowingTypeAndFollowingId(
            FollowingType.USER,
            ownerId
        )
        val followingCount = followRepository.countByFollowerId(ownerId)
        val salonsCount = ownedSalons.size.toLong()
        
        // Compter les collections (publics uniquement si pas le propriétaire)
        val isOwner = currentUserId == ownerId
        val collectionsCount = if (isOwner) {
            collectionRepository.countByUserId(ownerId)
        } else {
            collectionRepository.countByUserIdAndIsPublicTrue(ownerId)
        }

        val statistics = SalonOwnerProfileStatistics(
            postsCount = postsCount,
            totalLikes = totalLikes,
            followersCount = followersCount,
            followingCount = followingCount,
            salonsCount = salonsCount,
            collectionsCount = collectionsCount
        )

        // Vérifier si l'utilisateur courant suit ce propriétaire
        val isFollowedByCurrentUser = if (currentUserId != null) {
            followRepository.existsByFollowerIdAndFollowingTypeAndFollowingId(
                currentUserId,
                FollowingType.USER,
                ownerId
            )
        } else {
            null
        }

        // Récupérer les collections (publics uniquement si pas le propriétaire)
        val collections = if (isOwner) {
            collectionRepository.findByUserIdOrderByCreatedAtDesc(ownerId)
        } else {
            collectionRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(ownerId)
        }.map { CollectionResponse.fromEntity(it) }

        // Récupérer les badges affichés du propriétaire
        val badges = userBadgeRepository.findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(ownerId)
            .map { UserBadgeResponse.fromEntity(it) }

        return SalonOwnerProfileResponse.fromUser(
            user = owner,
            statistics = statistics,
            salons = salons,
            recentPosts = recentPosts,
            collections = collections,
            badges = badges,
            isFollowedByCurrentUser = isFollowedByCurrentUser
        )
    }

    /**
     * Met à jour le profil social d'un salon.
     * Phase E.2 - Profil Salon Social
     */
    @Transactional
    fun updateSalonSocialProfile(
        salonId: String,
        request: UpdateSalonSocialProfileRequest,
        currentUserId: String
    ): SalonSocialProfileResponse {
        // Validation
        request.validate()

        // Vérifier que le salon existe
        val salon = salonRepository.findById(salonId)
            .orElseThrow { RuntimeException("Salon avec ID '$salonId' non trouvé") }

        // Vérification des autorisations
        salonAuthorizationService.requirePermission(currentUserId, salonId, "social.update_profile")

        // Mettre à jour les champs
        request.socialDescription?.let { salon.socialDescription = it.trim().takeIf { it.isNotBlank() } }
        request.socialCoverImage?.let { salon.socialCoverImage = it.trim().takeIf { it.isNotBlank() } }

        // Mettre à jour les posts mis en avant
        if (request.highlightedPostIds.isNotEmpty()) {
            // Supprimer les anciens posts mis en avant
            salonHighlightedPostRepository.deleteBySalonId(salonId)

            // Vérifier que tous les posts existent et sont tagués avec ce salon
            request.highlightedPostIds.forEachIndexed { index, postId ->
                val post = postRepository.findById(postId)
                    .orElseThrow { PostNotFoundException(postId) }

                // Vérifier que le post est tagué avec ce salon
                val isTagged = postTagRepository.existsByPostIdAndTaggedTypeAndTaggedId(
                    postId,
                    TaggedType.salon,
                    salonId
                )
                if (!isTagged) {
                    throw IllegalArgumentException("Le post '$postId' n'est pas tagué avec le salon '$salonId'")
                }

                // Créer l'association
                val highlightedPost = SalonHighlightedPost(
                    id = java.util.UUID.randomUUID().toString(),
                    salon = salon,
                    post = post,
                    orderIndex = index
                )
                salonHighlightedPostRepository.save(highlightedPost)
            }
        } else {
            // Si la liste est vide, supprimer tous les posts mis en avant
            salonHighlightedPostRepository.deleteBySalonId(salonId)
        }

        // Sauvegarder
        salonRepository.save(salon)

        // Retourner le profil mis à jour
        return getSalonSocialProfile(salonId, currentUserId)
    }

    // ========== Phase E.3 - Badges et Certifications ==========

    /**
     * Attribue un badge à un utilisateur.
     * Phase E.3 - Badges et Certifications
     * 
     * Seuls les administrateurs peuvent attribuer des badges.
     */
    @Transactional
    fun awardBadge(userId: String, badgeId: String, awardedByUserId: String): UserBadgeResponse {
        // Vérifier que l'utilisateur qui attribue est admin
        val awardedBy = userRepository.findById(awardedByUserId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$awardedByUserId' non trouvé") }
        
        if (awardedBy.userType != UserType.admin) {
            throw UnauthorizedAccessException(awardedByUserId)
        }

        // Vérifier que l'utilisateur existe
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }

        // Vérifier que le badge existe
        val badge = badgeRepository.findById(badgeId)
            .orElseThrow { RuntimeException("Badge avec ID '$badgeId' non trouvé") }

        // Vérifier que l'utilisateur n'a pas déjà ce badge
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            throw IllegalArgumentException("L'utilisateur a déjà ce badge")
        }

        // Créer l'association
        val userBadge = UserBadge(
            id = java.util.UUID.randomUUID().toString(),
            user = user,
            badge = badge,
            isDisplayed = true
        )

        val savedUserBadge = userBadgeRepository.save(userBadge)
        return UserBadgeResponse.fromEntity(savedUserBadge)
    }

    /**
     * Récupère les badges d'un utilisateur.
     * Phase E.3 - Badges et Certifications
     */
    @Transactional(readOnly = true)
    fun getUserBadges(userId: String, includeHidden: Boolean = false): List<UserBadgeResponse> {
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw RuntimeException("Utilisateur avec ID '$userId' non trouvé")
        }

        val userBadges = if (includeHidden) {
            userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId)
        } else {
            userBadgeRepository.findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(userId)
        }

        return userBadges.map { UserBadgeResponse.fromEntity(it) }
    }

    /**
     * Récupère tous les badges disponibles.
     * Phase E.3 - Badges et Certifications
     */
    @Transactional(readOnly = true)
    fun getAvailableBadges(category: BadgeCategory? = null): List<BadgeResponse> {
        val badges = if (category != null) {
            badgeRepository.findByCategoryOrderByNameAsc(category)
        } else {
            badgeRepository.findAllByOrderByNameAsc()
        }

        return badges.map { BadgeResponse.fromEntity(it) }
    }

    /**
     * Affiche ou masque un badge sur le profil d'un utilisateur.
     * Phase E.3 - Badges et Certifications
     * 
     * Seul le propriétaire du profil peut modifier l'affichage de ses badges.
     */
    @Transactional
    fun toggleBadgeDisplay(userId: String, badgeId: String, currentUserId: String): UserBadgeResponse {
        // Vérifier l'ownership
        if (userId != currentUserId) {
            throw UnauthorizedAccessException(currentUserId)
        }

        // Récupérer l'association user-badge
        val userBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
            ?: throw RuntimeException("L'utilisateur n'a pas ce badge")

        // Toggle l'affichage
        userBadge.isDisplayed = !userBadge.isDisplayed

        val savedUserBadge = userBadgeRepository.save(userBadge)
        return UserBadgeResponse.fromEntity(savedUserBadge)
    }

    /**
     * Retire un badge d'un utilisateur.
     * Phase E.3 - Badges et Certifications
     * 
     * Seuls les administrateurs peuvent retirer des badges.
     */
    @Transactional
    fun removeBadge(userId: String, badgeId: String, currentUserId: String) {
        // Vérifier que l'utilisateur qui retire est admin
        val admin = userRepository.findById(currentUserId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$currentUserId' non trouvé") }
        
        if (admin.userType != UserType.admin) {
            throw UnauthorizedAccessException(currentUserId)
        }

        // Récupérer et supprimer l'association
        val userBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
            ?: throw RuntimeException("L'utilisateur n'a pas ce badge")

        userBadgeRepository.delete(userBadge)
    }

    // ========== PHASE F.1 - COLLECTIONS THÉMATIQUES ==========

    /**
     * Crée une nouvelle collection.
     * Phase F.1 - Collections Thématiques
     */
    @Transactional
    fun createCollection(request: CreateCollectionRequest, userId: String): CollectionResponse {
        // Validation
        request.validate()?.let { throw InvalidPostException(it) }

        // Vérification de l'existence de l'utilisateur
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur avec ID '$userId' non trouvé") }

        // Création de la collection
        val collection = com.frollot.model.Collection(
            id = UUID.randomUUID().toString(),
            user = user,
            name = request.name.trim(),
            description = request.description?.trim()?.takeIf { desc -> desc.isNotBlank() },
            coverImageUrl = request.coverImageUrl?.trim()?.takeIf { url -> url.isNotBlank() },
            isPublic = request.isPublic,
            category = request.category
        )

        // Validation
        if (collection.name.isBlank() || collection.user == null) {
            throw InvalidPostException("Les données de la collection sont invalides")
        }

        // Sauvegarde
        val savedCollection = collectionRepository.save(collection)

        // Retourner la réponse avec le nombre de posts (0 pour une nouvelle collection)
        return CollectionResponse.fromEntity(savedCollection, postsCount = 0)
    }

    /**
     * Met à jour une collection.
     * Phase F.1 - Collections Thématiques
     */
    @Transactional
    fun updateCollection(
        collectionId: String,
        request: UpdateCollectionRequest,
        userId: String
    ): CollectionResponse {
        // Validation
        request.validate()?.let { throw InvalidPostException(it) }

        // Récupérer la collection
        val collection = collectionRepository.findById(collectionId)
            .orElseThrow { RuntimeException("Collection avec ID '$collectionId' non trouvée") }

        // Vérifier l'ownership
        if (collection.user!!.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        // Mise à jour des champs
        request.name?.let { collection.name = it.trim() }
        request.description?.let { collection.description = it.trim().takeIf { d -> d.isNotBlank() } }
        request.coverImageUrl?.let { collection.coverImageUrl = it.trim().takeIf { u -> u.isNotBlank() } }
        request.isPublic?.let { collection.isPublic = it }
        request.category?.let { collection.category = it }

        // Sauvegarde
        val savedCollection = collectionRepository.save(collection)

        // Compter les posts
        val postsCount = collectionPostRepository.countByCollectionId(collectionId).toInt()

        return CollectionResponse.fromEntity(savedCollection, postsCount = postsCount)
    }

    /**
     * Supprime une collection.
     * Phase F.1 - Collections Thématiques
     */
    @Transactional
    fun deleteCollection(collectionId: String, userId: String) {
        // Récupérer la collection
        val collection = collectionRepository.findById(collectionId)
            .orElseThrow { RuntimeException("Collection avec ID '$collectionId' non trouvée") }

        // Vérifier l'ownership
        if (collection.user!!.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        // Supprimer (les collection_posts seront supprimés en cascade)
        collectionRepository.delete(collection)
    }

    /**
     * Récupère toutes les collections d'un utilisateur.
     * Phase F.1 - Collections Thématiques
     */
    fun getCollectionsByUser(
        userId: String,
        includePrivate: Boolean = false,
        currentUserId: String? = null
    ): List<CollectionResponse> {
        // Vérifier si l'utilisateur demande ses propres collections
        val isOwner = currentUserId != null && userId == currentUserId

        val collections = if (includePrivate && isOwner) {
            // Récupérer toutes les collections (publiques et privées) si c'est le propriétaire
            collectionRepository.findByUserIdOrderByCreatedAtDesc(userId)
        } else {
            // Récupérer uniquement les collections publiques
            collectionRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId)
        }

        // Compter les posts pour chaque collection
        return collections.map { collection ->
            val postsCount = collectionPostRepository.countByCollectionId(collection.id!!).toInt()
            CollectionResponse.fromEntity(collection, postsCount = postsCount)
        }
    }

    /**
     * Récupère une collection par son ID.
     * Phase F.1 - Collections Thématiques
     */
    fun getCollectionById(collectionId: String, currentUserId: String?): CollectionResponse {
        val collection = collectionRepository.findById(collectionId)
            .orElseThrow { RuntimeException("Collection avec ID '$collectionId' non trouvée") }

        // Vérifier la visibilité
        val isOwner = currentUserId != null && collection.user!!.id == currentUserId
        if (!collection.isPublic && !isOwner) {
            throw UnauthorizedAccessException(currentUserId ?: "anonymous")
        }

        // Compter les posts
        val postsCount = collectionPostRepository.countByCollectionId(collectionId).toInt()

        return CollectionResponse.fromEntity(collection, postsCount = postsCount)
    }

    /**
     * Ajoute un post à une collection.
     * Phase F.1 - Collections Thématiques
     */
    @Transactional
    fun addPostToCollection(collectionId: String, postId: String, userId: String): CollectionPostResponse {
        // Vérifier que la collection existe et appartient à l'utilisateur
        val collection = collectionRepository.findById(collectionId)
            .orElseThrow { RuntimeException("Collection avec ID '$collectionId' non trouvée") }

        if (collection.user!!.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        // Vérifier que le post existe
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérifier si le post n'est pas déjà dans la collection (400, pas 500)
        if (collectionPostRepository.existsByCollectionIdAndPostId(collectionId, postId)) {
            throw IllegalArgumentException("Le post est déjà dans cette collection")
        }

        // Récupérer le dernier orderIndex pour cette collection
        val existingPosts = collectionPostRepository.findByCollectionIdOrderByOrderIndexAscAddedAtDesc(collectionId)
        val nextOrderIndex = existingPosts.maxOfOrNull { it.orderIndex }?.plus(1) ?: 0

        // Créer l'association
        val collectionPost = com.frollot.model.CollectionPost(
            id = UUID.randomUUID().toString(),
            collection = collection,
            post = post,
            orderIndex = nextOrderIndex
        )

        // Sauvegarde
        val savedCollectionPost = collectionPostRepository.save(collectionPost)

        // Cover automatique : si la collection n'a pas de cover et que le post a une image,
        // l'image du premier post ajouté devient la cover de la collection.
        if (collection.coverImageUrl.isNullOrBlank() && !post.imageUrl.isNullOrBlank()) {
            collection.coverImageUrl = post.imageUrl
            collectionRepository.save(collection)
        }

        // Convertir le post en PostResponse
        val postResponse = getPostById(postId, userId)

        return CollectionPostResponse.fromEntity(savedCollectionPost, postResponse)
    }

    /**
     * Retire un post d'une collection.
     * Phase F.1 - Collections Thématiques
     */
    @Transactional
    fun removePostFromCollection(collectionId: String, postId: String, userId: String) {
        // Vérifier que la collection existe et appartient à l'utilisateur
        val collection = collectionRepository.findById(collectionId)
            .orElseThrow { RuntimeException("Collection avec ID '$collectionId' non trouvée") }

        if (collection.user!!.id != userId) {
            throw UnauthorizedAccessException(userId)
        }

        // Récupérer et supprimer l'association
        val collectionPost = collectionPostRepository.findByCollectionIdAndPostId(collectionId, postId)
            ?: throw RuntimeException("Le post n'est pas dans cette collection")

        collectionPostRepository.delete(collectionPost)
    }

    /**
     * Récupère tous les posts d'une collection.
     * Phase F.1 - Collections Thématiques
     */
    fun getCollectionPosts(
        collectionId: String,
        pageable: Pageable,
        currentUserId: String?
    ): Page<CollectionPostResponse> {
        // Vérifier que la collection existe et est accessible
        val collection = collectionRepository.findById(collectionId)
            .orElseThrow { RuntimeException("Collection avec ID '$collectionId' non trouvée") }

        // Vérifier la visibilité
        val isOwner = currentUserId != null && collection.user!!.id == currentUserId
        if (!collection.isPublic && !isOwner) {
            throw UnauthorizedAccessException(currentUserId ?: "anonymous")
        }

        // Récupérer les posts avec pagination
        val collectionPostsPage = collectionPostRepository.findByCollectionIdOrderByOrderIndexAscAddedAtDesc(
            collectionId,
            pageable
        )

        // Convertir en CollectionPostResponse
        // V041 - Archivage global : on saute les posts archivés au lieu de laisser
        // getPostById jeter un 404 qui casserait toute la liste.
        val collectionPostResponses = collectionPostsPage.content.mapNotNull { collectionPost ->
            if (collectionPost.post?.isArchived == true) return@mapNotNull null
            val postResponse = getPostById(collectionPost.post!!.id!!, currentUserId)
            CollectionPostResponse.fromEntity(collectionPost, postResponse)
        }

        // Retourner une Page personnalisée
        return org.springframework.data.domain.PageImpl(
            collectionPostResponses,
            pageable,
            collectionPostsPage.totalElements
        )
    }

    /**
     * Récupère toutes les collections publiques (pour découverte).
     * Phase F.1 - Collections Thématiques
     */
    fun getPublicCollections(
        category: CollectionCategory? = null,
        pageable: Pageable
    ): Page<CollectionResponse> {
        val collectionsPage = if (category != null) {
            collectionRepository.findByIsPublicTrueAndCategoryOrderByCreatedAtDesc(category, pageable)
        } else {
            collectionRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable)
        }

        // Compter les posts pour chaque collection
        val collectionsWithCounts = collectionsPage.content.map { collection ->
            val postsCount = collectionPostRepository.countByCollectionId(collection.id!!).toInt()
            CollectionResponse.fromEntity(collection, postsCount = postsCount)
        }

        return org.springframework.data.domain.PageImpl(
            collectionsWithCounts,
            pageable,
            collectionsPage.totalElements
        )
    }

    // ========== PHASE F.2 : POSTS ÉPINGLÉS POUR SALONS ==========

    /**
     * Épingle un post pour son auteur.
     * Phase F.2 - Posts Épinglés pour Salons
     * 
     * Contraintes :
     * - Seul l'auteur du post peut l'épingler
     * - Maximum 3 posts épinglés par auteur
     * 
     * @param postId ID du post à épingler
     * @param userId ID de l'utilisateur qui demande l'épinglage (doit être l'auteur)
     * @return PostResponse mis à jour
     * @throws PostNotFoundException si le post n'existe pas
     * @throws UnauthorizedAccessException si l'utilisateur n'est pas l'auteur
     * @throws IllegalStateException si l'auteur a déjà 3 posts épinglés
     */
    @Transactional
    fun pinPost(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification que l'utilisateur est l'auteur du post
        if (post.author?.id != userId) {
            throw UnauthorizedAccessException("Seul l'auteur du post peut l'épingler")
        }

        // Vérifier si le post est déjà épinglé
        if (post.isPinned) {
            // Retourner le post tel quel
            return buildPostResponse(post, userId)
        }

        // Vérifier la limite de 3 posts épinglés
        val pinnedCount = postRepository.countByAuthorIdAndIsPinnedTrue(userId)
        if (pinnedCount >= 3) {
            throw IllegalStateException("Vous avez déjà épinglé 3 posts. Désépinglez-en un pour en épingler un autre.")
        }

        // Épingler le post
        post.isPinned = true
        postRepository.save(post)

        println("📌 Post épinglé: Post $postId par User $userId")

        return buildPostResponse(post, userId)
    }

    /**
     * Désépingle un post.
     * Phase F.2 - Posts Épinglés pour Salons
     * 
     * @param postId ID du post à désépingler
     * @param userId ID de l'utilisateur qui demande le désépinglage (doit être l'auteur)
     * @return PostResponse mis à jour
     * @throws PostNotFoundException si le post n'existe pas
     * @throws UnauthorizedAccessException si l'utilisateur n'est pas l'auteur
     */
    @Transactional
    fun unpinPost(postId: String, userId: String): PostResponse {
        // Vérification de l'existence du post
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException(postId) }

        // Vérification que l'utilisateur est l'auteur du post
        if (post.author?.id != userId) {
            throw UnauthorizedAccessException("Seul l'auteur du post peut le désépingler")
        }

        // Vérifier si le post est déjà désépinglé
        if (!post.isPinned) {
            // Retourner le post tel quel
            return buildPostResponse(post, userId)
        }

        // Désépingler le post
        post.isPinned = false
        postRepository.save(post)

        println("📌 Post désépinglé: Post $postId par User $userId")

        return buildPostResponse(post, userId)
    }

    /**
     * Récupère les posts épinglés d'un utilisateur.
     * Phase F.2 - Posts Épinglés pour Salons
     * 
     * @param authorId ID de l'auteur des posts
     * @param currentUserId ID de l'utilisateur courant (pour les statuts isLiked, etc.)
     * @return Liste des posts épinglés, triés par date de création décroissante
     */
    @Transactional(readOnly = true)
    fun getPinnedPosts(authorId: String, currentUserId: String? = null): List<PostResponse> {
        // Vérification de l'existence de l'auteur
        if (!userRepository.existsById(authorId)) {
            throw RuntimeException("Auteur avec ID '$authorId' non trouvé")
        }

        // Récupérer les posts épinglés (hors archivés - V041)
        val pinnedPosts = postRepository.findByAuthorIdAndIsPinnedTrueOrderByCreatedAtDesc(authorId)
            .filter { !it.isArchived }

        // Convertir en PostResponse
        return pinnedPosts.map { post ->
            val postId = post.id!!
            val isLiked = currentUserId?.let { postLikeRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isFavorited = currentUserId?.let { postFavoriteRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val isShared = currentUserId?.let { postShareRepository.existsByPostIdAndUserId(postId, it) } ?: false
            val commentsCount = commentRepository.countByPostId(postId).toInt()
            val sharesCount = post.sharesCount
            val reactions = getReactionsByPost(postId)
            val currentUserReaction = currentUserId?.let { getUserReaction(postId, it) }
            val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .map { buildTagResponse(it) }
            val services = postServiceRepository.findServicesByPostId(postId)
                .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
            val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
                .map { HairHashtagResponse.fromEntity(it) }
            val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
                .map { PostMediaResponse.fromEntity(it) }
            PostResponse.fromEntity(
                post,
                isLikedByCurrentUser = isLiked,
                isFavoritedByCurrentUser = isFavorited,
                isSharedByCurrentUser = isShared,
                commentsCount = commentsCount,
                sharesCount = sharesCount,
                reactions = reactions,
                currentUserReaction = currentUserReaction,
                tags = tags,
                services = services,
                hashtags = hashtags,
                media = media
            )
        }
    }

    /**
     * Helper pour construire un PostResponse avec tous les détails.
     * Phase F.2 - Posts Épinglés pour Salons
     */
    /** Lot B : enrichit un PostTag avec le nom/slug/avatar de l'entité taguée. */
    private fun buildTagResponse(tag: PostTag): TagResponse {
        val base = TagResponse.fromEntity(tag)
        return when (tag.taggedType) {
            TaggedType.salon -> {
                val salon = salonRepository.findById(tag.taggedId).orElse(null)
                if (salon != null) base.copy(
                    taggedName = salon.name,
                    taggedSlug = salon.slug,
                    taggedAvatarUrl = salon.coverPhotoUrl
                ) else base
            }
            TaggedType.user -> {
                val user = userRepository.findById(tag.taggedId).orElse(null)
                if (user != null) base.copy(
                    taggedName = listOfNotNull(user.firstName, user.lastName).joinToString(" "),
                    taggedAvatarUrl = user.avatarUrl
                ) else base
            }
        }
    }

    /** Lot C : résout salonName/salonAvatarUrl pour un post publié au nom d'un salon. */
    private fun resolveSalonInfo(post: Post): Pair<String?, String?> {
        val sid = post.salonId ?: return Pair(null, null)
        if (post.authorType != com.frollot.model.AuthorType.salon) return Pair(null, null)
        val salon = salonRepository.findById(sid).orElse(null) ?: return Pair(null, null)
        return Pair(salon.name, salon.coverPhotoUrl)
    }

    private fun buildPostResponse(post: Post, userId: String): PostResponse {
        val postId = post.id!!
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val isFavorited = postFavoriteRepository.existsByPostIdAndUserId(postId, userId)
        val isShared = postShareRepository.existsByPostIdAndUserId(postId, userId)
        val commentsCount = commentRepository.countByPostId(postId).toInt()
        val sharesCount = post.sharesCount
        val reactions = getReactionsByPost(postId)
        val currentUserReaction = getUserReaction(postId, userId)
        val tags = postTagRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .map { buildTagResponse(it) }
        val services = postServiceRepository.findServicesByPostId(postId)
            .map { com.frollot.dto.ServiceResponse.fromEntity(it) }
        val hashtags = postHashtagRepository.findHashtagsByPostId(postId)
            .map { HairHashtagResponse.fromEntity(it) }
        val media = postMediaRepository.findByPostIdOrderByMediaTypeAscOrderIndexAsc(postId)
            .map { PostMediaResponse.fromEntity(it) }
        val (sName, sAvatar) = resolveSalonInfo(post)
        return PostResponse.fromEntity(
            post,
            isLikedByCurrentUser = isLiked,
            isFavoritedByCurrentUser = isFavorited,
            isSharedByCurrentUser = isShared,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            reactions = reactions,
            currentUserReaction = currentUserReaction,
            tags = tags,
            services = services,
            hashtags = hashtags,
            media = media,
            salonName = sName,
            salonAvatarUrl = sAvatar
        )
    }
}

