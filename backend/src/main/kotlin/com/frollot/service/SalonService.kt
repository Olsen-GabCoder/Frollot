// ============================================
// FICHIER: SalonService.kt - VERSION COMPLÈTE CORRIGÉE
// ============================================
package com.frollot.service

import com.frollot.dto.CreateSalonRequest
import com.frollot.dto.SalonResponse
import com.frollot.model.Salon
import com.frollot.model.ServiceCategory
import com.frollot.model.UserType
import com.frollot.model.TaggedType
import com.frollot.repository.PostTagRepository
import com.frollot.repository.SalonRepository
import com.frollot.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SalonService(
    private val salonRepository: SalonRepository,
    private val userRepository: UserRepository,
    private val postTagRepository: PostTagRepository, // Phase C.3 - Pour calculer l'engagement
    private val salonAuthorizationService: SalonAuthorizationService
) {

    fun createSalon(request: CreateSalonRequest): SalonResponse {
        // 1. Vérifier que le propriétaire existe
        val owner = userRepository.findById(request.ownerId)
            .orElseThrow { RuntimeException("Propriétaire non trouvé") }

        // 2. Vérifier que c'est bien un salon_owner
        if (owner.userType != UserType.salon_owner) {
            throw RuntimeException("Seul un propriétaire peut créer un salon")
        }

        // 3. Générer un slug à partir du nom
        val slug = generateSlug(request.name)

        // 4. Créer le salon
        val salon = Salon(
            id = UUID.randomUUID().toString(),
            name = request.name,
            address = request.address,
            city = request.city,
            postalCode = request.postalCode,
            description = request.description,
            slug = slug,
            owner = owner,
            coverPhotoUrl = request.coverPhotoUrl,
            latitude = request.latitude,
            longitude = request.longitude
        )

        // 5. Sauvegarder
        val savedSalon = salonRepository.save(salon)

        // 6. Retourner le DTO de réponse
        return toSalonResponse(savedSalon)
    }

    fun getAllSalons(): List<SalonResponse> {
        return salonRepository.findAll().map { toSalonResponse(it) }
    }

    /**
     * Recherche avancée de salons avec filtres optionnels.
     *
     * @param query     Texte libre (nom ou description du salon)
     * @param city      Ville ou partie d'adresse
     * @param category  Catégorie de service proposée par le salon
     */
    fun searchSalons(
        query: String?,
        city: String?,
        category: ServiceCategory?
    ): List<SalonResponse> {
        val normalizedQuery = query?.takeIf { it.isNotBlank() }
        val normalizedCity = city?.takeIf { it.isNotBlank() }

        // Si aucun filtre n'est renseigné, on renvoie simplement tous les salons
        if (normalizedQuery == null && normalizedCity == null && category == null) {
            return getAllSalons()
        }

        return salonRepository.search(
            query = normalizedQuery,
            city = normalizedCity,
            category = category
        ).map { toSalonResponse(it) }
    }

    /**
     * ⭐ NOUVELLE MÉTHODE - Récupère un salon par son ID
     */
    fun getSalonById(id: String): SalonResponse {
        println("🔍 Service: Recherche du salon avec ID: $id")

        val salon = salonRepository.findById(id)
            .orElseThrow {
                println("❌ Service: Salon avec l'ID $id introuvable")
                NoSuchElementException("Salon avec l'ID $id introuvable")
            }

        println("✅ Service: Salon trouvé - ${salon.name}")
        return toSalonResponse(salon)
    }

    fun getSalonsByOwner(ownerId: String): List<SalonResponse> {
        return salonRepository.findByOwnerId(ownerId).map { toSalonResponse(it) }
    }

    /**
     * Met à jour la photo de couverture d'un salon.
     * 
     * @param salonId L'ID du salon
     * @param coverPhotoUrl L'URL de la nouvelle photo de couverture
     * @param ownerId L'ID du propriétaire (pour vérifier les permissions)
     * @return Le salon mis à jour
     * @throws NoSuchElementException si le salon n'existe pas
     * @throws RuntimeException si l'utilisateur n'est pas le propriétaire
     */
    fun updateSalonCoverPhoto(salonId: String, coverPhotoUrl: String, ownerId: String): SalonResponse {
        val salon = salonRepository.findById(salonId)
            .orElseThrow { NoSuchElementException("Salon avec l'ID $salonId introuvable") }

        // Vérification des autorisations
        salonAuthorizationService.requirePermission(ownerId, salonId, "salon.update_cover")

        salon.coverPhotoUrl = coverPhotoUrl
        val updatedSalon = salonRepository.save(salon)
        return toSalonResponse(updatedSalon)
    }

    /**
     * Méthode utilitaire pour convertir une entité Salon en SalonResponse.
     * Évite la duplication de code.
     */
    private fun toSalonResponse(
        salon: Salon,
        isFollowedByCurrentUser: Boolean? = null,
        followersCount: Long? = null
    ): SalonResponse {
        return SalonResponse(
            id = salon.id!!,
            name = salon.name,
            address = salon.address,
            city = salon.city,
            postalCode = salon.postalCode,
            description = salon.description,
            slug = salon.slug,
            ownerId = salon.owner!!.id!!,
            coverPhotoUrl = salon.coverPhotoUrl,
            latitude = salon.latitude,
            longitude = salon.longitude,
            isVerified = salon.isVerified, // Phase H.2 - Vérification Salons/Coiffeurs
            verificationType = salon.verificationType, // Phase H.2 - Vérification Salons/Coiffeurs
            isFollowedByCurrentUser = isFollowedByCurrentUser,
            followersCount = followersCount,
            createdAt = salon.createdAt
        )
    }

    /**
     * Récupère les salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     * 
     * @param latitude Latitude du point central
     * @param longitude Longitude du point central
     * @param radiusKm Rayon de recherche en kilomètres (défaut: 10 km)
     * @return Liste des salons triés par distance croissante
     */
    @Transactional(readOnly = true)
    fun getSalonsNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): List<SalonResponse> {
        val salons = salonRepository.findSalonsNearby(latitude, longitude, radiusKm)
        return salons.map { toSalonResponse(it) }
    }

    /**
     * Récupère les salons les plus engagés (trending).
     * Phase C.3 - Trending Coiffure
     * 
     * Calcule le score d'engagement pour chaque salon basé sur les posts tagués :
     * - Score d'un post = likesCount * 1 + commentsCount * 2 + sharesCount * 3
     * - Score d'un salon = somme des scores de tous les posts tagués avec ce salon
     * 
     * @param limit Nombre de salons à retourner
     * @return Liste des salons triés par engagement décroissant
     */
    @Transactional(readOnly = true)
    fun getTrendingSalons(limit: Int = 10): List<SalonResponse> {
        // Récupérer tous les salons
        val allSalons = salonRepository.findAll()
        
        // Calculer le score d'engagement pour chaque salon
        val salonsWithScores = allSalons.map { salon ->
            // Récupérer tous les posts tagués avec ce salon
            val postTags = postTagRepository.findByTaggedTypeAndTaggedIdOrderByCreatedAtDesc(
                TaggedType.salon,
                salon.id!!
            )
            
            // Calculer le score total d'engagement
            val engagementScore = postTags
                .mapNotNull { it.post }
                .sumOf { post ->
                    val likes = post.likesCount
                    val comments = post.commentsCount
                    val shares = post.sharesCount
                    likes * 1 + comments * 2 + shares * 3
                }
            
            salon to engagementScore
        }
        
        // Trier par score décroissant et prendre les limit premiers
        val trendingSalons = salonsWithScores
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
        
        return trendingSalons.map { toSalonResponse(it) }
    }

    /**
     * Génère un slug à partir d'un nom de salon.
     * Exemple : "Salon Élégance Paris" -> "salon-elegance-paris"
     */
    private fun generateSlug(name: String): String {
        return name
            .lowercase()
            .replace(" ", "-")
            .replace("[^a-z0-9-]".toRegex(), "")
            .take(255)
    }
}