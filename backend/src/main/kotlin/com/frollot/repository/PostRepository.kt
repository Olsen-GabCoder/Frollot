package com.frollot.repository

import com.frollot.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository pour la gestion des posts.
 * 
 * Utilise @EntityGraph pour éviter le problème N+1 lors du chargement
 * des posts avec leurs auteurs et likes.
 */
@Repository
interface PostRepository : JpaRepository<Post, String> {

    /**
     * Récupère tous les posts avec leurs auteurs, triés par date décroissante.
     * 
     * Utilise EntityGraph pour charger l'auteur en une seule requête.
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(pageable: Pageable): Page<Post>

    /**
     * Récupère tous les posts avec leurs auteurs, triés par date décroissante.
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(): List<Post>

    /**
     * Récupère les posts d'un utilisateur spécifique.
     */
    @EntityGraph(attributePaths = ["author"])
    fun findByAuthorIdOrderByCreatedAtDesc(authorId: String, pageable: Pageable): Page<Post>

    /**
     * Récupère les posts d'un utilisateur spécifique (sans pagination).
     */
    @EntityGraph(attributePaths = ["author"])
    fun findByAuthorIdOrderByCreatedAtDesc(authorId: String): List<Post>

    /**
     * Compte le nombre de posts d'un utilisateur.
     */
    fun countByAuthorId(authorId: String): Long

    /**
     * Calcule la somme des likes de tous les posts d'un utilisateur.
     * Phase E.1 - Profil Coiffeur Enrichi
     */
    @Query("SELECT COALESCE(SUM(p.likesCount), 0) FROM Post p WHERE p.author.id = :authorId")
    fun sumLikesByAuthorId(@Param("authorId") authorId: String): Long?

    /**
     * Récupère les posts créés après une date donnée.
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("SELECT p FROM Post p WHERE p.createdAt > :since ORDER BY p.createdAt DESC")
    fun findPostsCreatedAfter(@Param("since") since: LocalDateTime): List<Post>

    /**
     * Récupère les posts filtrés par type avec pagination.
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("SELECT p FROM Post p WHERE p.postType = :postType ORDER BY p.createdAt DESC")
    fun findByPostTypeOrderByCreatedAtDesc(@Param("postType") postType: com.frollot.model.PostType, pageable: Pageable): Page<Post>

    /**
     * Recherche des posts par contenu (texte).
     * Recherche case-insensitive dans le champ content.
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    fun searchByContent(@Param("query") query: String, pageable: Pageable): Page<Post>

    /**
     * Recherche des posts avec filtres combinés.
     * Phase C.1 - Recherche spécialisée coiffure
     * 
     * Filtres supportés :
     * - postType : Type de post
     * - authorId : Auteur du post
     * 
     * Note : Les filtres par service, salon et hashtag nécessitent des jointures
     * et sont gérés dans SocialService pour éviter des requêtes trop complexes.
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("""
        SELECT DISTINCT p FROM Post p
        WHERE (:query IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:postType IS NULL OR p.postType = :postType)
        AND (:authorId IS NULL OR p.author.id = :authorId)
        ORDER BY p.createdAt DESC
    """)
    fun searchWithFilters(
        @Param("query") query: String?,
        @Param("postType") postType: com.frollot.model.PostType?,
        @Param("authorId") authorId: String?,
        pageable: Pageable
    ): Page<Post>

    /**
     * Récupère les posts trending (les plus populaires) créés après une date donnée.
     * Phase C.3 - Trending Coiffure
     * 
     * Trie par score de popularité : likesCount * 1 + commentsCount * 2 + sharesCount * 3
     * En cas d'égalité, trie par date de création décroissante.
     * 
     * @param sinceDate Date minimale de création (pour filtrer par période)
     * @param pageable Pagination
     * @return Page de posts triés par popularité
     */
    @EntityGraph(attributePaths = ["author"])
    @Query("""
        SELECT p FROM Post p 
        WHERE p.createdAt >= :sinceDate
        ORDER BY (p.likesCount * 1 + p.commentsCount * 2 + p.sharesCount * 3) DESC, p.createdAt DESC
    """)
    fun findTrendingPosts(
        @Param("sinceDate") sinceDate: LocalDateTime,
        pageable: Pageable
    ): Page<Post>

    /**
     * Compte le nombre de posts épinglés d'un auteur.
     * Phase F.2 - Posts Épinglés pour Salons
     */
    fun countByAuthorIdAndIsPinnedTrue(authorId: String): Long

    /**
     * Récupère les posts épinglés d'un auteur, triés par date de création décroissante.
     * Phase F.2 - Posts Épinglés pour Salons
     */
    @EntityGraph(attributePaths = ["author"])
    fun findByAuthorIdAndIsPinnedTrueOrderByCreatedAtDesc(authorId: String): List<Post>
}

