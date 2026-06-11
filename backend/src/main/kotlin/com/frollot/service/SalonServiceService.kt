package com.frollot.service

import com.frollot.dto.CreateServiceRequest
import com.frollot.dto.ServiceResponse
import com.frollot.dto.UpdateServiceRequest
import com.frollot.model.SalonService
import com.frollot.model.ServiceCategory
import com.frollot.model.UserType
import com.frollot.repository.SalonRepository
import com.frollot.repository.SalonServiceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * Service de gestion des prestations de salon - VERSION CORRIGÉE
 */
@Service
@Transactional
class SalonServiceService(
    private val salonServiceRepository: SalonServiceRepository,
    private val salonRepository: SalonRepository
) {

    // ========== EXCEPTIONS MÉTIER ==========

    class SalonNotFoundException(salonId: String) :
        RuntimeException("Salon avec ID '$salonId' non trouvé")

    class ServiceNotFoundException(serviceId: String) :
        RuntimeException("Service avec ID '$serviceId' non trouvé")

    class DuplicateServiceException(salonId: String, serviceName: String) :
        RuntimeException("Le service '$serviceName' existe déjà dans le salon '$salonId'")

    class UnauthorizedAccessException(userId: String) :
        RuntimeException("L'utilisateur '$userId' n'est pas autorisé à gérer les services")

    // ========== OPÉRATIONS CRUD ==========

    /**
     * Crée une nouvelle prestation de service pour un salon.
     * VERSION CORRIGÉE - Sans rechargement inutile causant ObjectOptimisticLockingFailureException
     */
    @Transactional
    fun createService(request: CreateServiceRequest, userId: String? = null): ServiceResponse {
        println("🔵 Début création service: ${request.name}")

        // 1. Validation de la requête
        request.validate()

        // 2. Vérification de l'existence du salon
        val salon = salonRepository.findById(request.salonId)
            .orElseThrow { SalonNotFoundException(request.salonId) }

        println("✅ Salon trouvé: ${salon.name}")

        // 3. Vérification des autorisations (si userId est fourni)
        userId?.let {
            if (salon.owner?.id != userId) {
                throw UnauthorizedAccessException(userId)
            }
        }

        // 4. Vérification de l'unicité du nom dans le salon
        if (salonServiceRepository.existsBySalonIdAndNameIgnoreCase(request.salonId, request.name)) {
            println("❌ Service existe déjà: ${request.name}")
            throw DuplicateServiceException(request.salonId, request.name)
        }

        // 5. Génération d'un ID unique
        val serviceId = UUID.randomUUID().toString()
        println("🆔 ID généré: $serviceId")

        // 6. Création de l'entité - CORRECTION: ID avant la création
        val service = SalonService(
            id = serviceId,
            name = request.name.trim(),
            description = request.description?.trim(),
            durationMinutes = request.durationMinutes,
            price = request.price,
            category = request.category,
            salon = salon,
            imageUrls = request.imageUrls?.joinToString(",")
        )

        // 7. Validation de l'entité
        if (!service.isValid()) {
            println("❌ Service invalide")
            throw IllegalArgumentException("Les données du service sont invalides")
        }

        // 8. Persistance - CORRECTION: Save une seule fois
        println("💾 Sauvegarde du service...")
        val savedService = salonServiceRepository.save(service)
        println("✅ Service sauvegardé: ${savedService.id}")

        // 9. Log d'audit
        println("✅ Service créé: ${savedService.name} (${savedService.id}) pour le salon ${salon.name}")

        // 10. Conversion en DTO de réponse
        return ServiceResponse.fromEntity(savedService)
    }

    /**
     * Récupère toutes les prestations d'un salon.
     */
    @Transactional(readOnly = true)
    fun getServicesBySalon(salonId: String): List<ServiceResponse> {
        // Vérification de l'existence du salon
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        val services = salonServiceRepository.findBySalonId(salonId)

        return ServiceResponse.fromEntities(services)
    }

    /**
     * Récupère une prestation par son ID.
     */
    @Transactional(readOnly = true)
    fun getServiceById(serviceId: String): ServiceResponse {
        val service = salonServiceRepository.findById(serviceId)
            .orElseThrow { ServiceNotFoundException(serviceId) }

        return ServiceResponse.fromEntity(service)
    }

    /**
     * Met à jour une prestation existante.
     */
    @Transactional
    fun updateService(
        serviceId: String,
        request: UpdateServiceRequest,
        userId: String? = null
    ): ServiceResponse {
        // 1. Récupération du service existant
        val service = salonServiceRepository.findById(serviceId)
            .orElseThrow { ServiceNotFoundException(serviceId) }

        // 2. Vérification des autorisations (si userId est fourni)
        userId?.let {
            if (service.salon?.owner?.id != userId) {
                throw UnauthorizedAccessException(userId)
            }
        }

        // 3. Vérification de l'unicité si le nom change
        request.name?.let { newName ->
            if (newName != service.name &&
                salonServiceRepository.existsBySalonIdAndNameIgnoreCase(
                    service.salon?.id ?: "",
                    newName
                )
            ) {
                throw DuplicateServiceException(
                    service.salon?.id ?: "",
                    newName
                )
            }
        }

        // 4. Application des modifications
        val updatedService = request.applyTo(service)

        // 5. Persistance
        val savedService = salonServiceRepository.save(updatedService)

        // 6. Log d'audit
        println("✏️ Service mis à jour: ${savedService.name} (${savedService.id})")

        return ServiceResponse.fromEntity(savedService)
    }

    /**
     * Supprime une prestation.
     */
    @Transactional
    fun deleteService(serviceId: String, userId: String? = null) {
        val service = salonServiceRepository.findById(serviceId)
            .orElseThrow { ServiceNotFoundException(serviceId) }

        // Vérification des autorisations (si userId est fourni)
        userId?.let {
            if (service.salon?.owner?.id != userId) {
                throw UnauthorizedAccessException(userId)
            }
        }

        salonServiceRepository.delete(service)

        println("🗑️ Service supprimé: ${service.name} (${service.id})")
    }

    // ========== OPÉRATIONS DE RECHERCHE ET FILTRAGE ==========

    @Transactional(readOnly = true)
    fun searchServices(salonId: String, searchTerm: String): List<ServiceResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        val services = salonServiceRepository.searchBySalonAndTerm(salonId, searchTerm)

        return ServiceResponse.fromEntities(services)
    }

    @Transactional(readOnly = true)
    fun getServicesByCategory(salonId: String, category: ServiceCategory): List<ServiceResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        val services = salonServiceRepository.findBySalonIdAndCategory(salonId, category)

        return ServiceResponse.fromEntities(services)
    }

    @Transactional(readOnly = true)
    fun getServicesByPriceRange(
        salonId: String,
        minPrice: BigDecimal,
        maxPrice: BigDecimal
    ): List<ServiceResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        if (minPrice > maxPrice) {
            throw IllegalArgumentException("Le prix minimum doit être inférieur au prix maximum")
        }

        val services = salonServiceRepository.findBySalonIdAndPriceBetween(
            salonId, minPrice, maxPrice
        )

        return ServiceResponse.fromEntities(services)
    }

    @Transactional(readOnly = true)
    fun getServicesBySalonPaginated(
        salonId: String,
        pageable: Pageable
    ): Page<ServiceResponse> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        val page = salonServiceRepository.findBySalonId(salonId, pageable)

        return page.map { ServiceResponse.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getServiceStatistics(salonId: String): Map<String, Any> {
        if (!salonRepository.existsById(salonId)) {
            throw SalonNotFoundException(salonId)
        }

        val totalServices = salonServiceRepository.countBySalonId(salonId)
        val services = salonServiceRepository.findBySalonId(salonId)

        val categoriesCount: Map<ServiceCategory, Int> = ServiceCategory.entries.associateWith { category ->
            services.count { it.category == category }
        }

        val averagePrice = if (services.isNotEmpty()) {
            services.map { it.price }
                .fold(BigDecimal.ZERO) { acc, price -> acc.add(price) }
                .divide(BigDecimal(services.size), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val averageDuration = if (services.isNotEmpty()) {
            services.map { it.durationMinutes }.average()
        } else {
            0.0
        }

        return mapOf(
            "totalServices" to totalServices,
            "categories" to categoriesCount,
            "averagePrice" to averagePrice,
            "averageDuration" to averageDuration,
            "priceRange" to mapOf(
                "min" to (services.minOfOrNull { it.price } ?: BigDecimal.ZERO),
                "max" to (services.maxOfOrNull { it.price } ?: BigDecimal.ZERO)
            ),
            "durationRange" to mapOf(
                "min" to (services.minOfOrNull { it.durationMinutes } ?: 0),
                "max" to (services.maxOfOrNull { it.durationMinutes } ?: 0)
            )
        )
    }

    // ========== MÉTHODES UTILITAIRES ==========

    fun canUserManageServices(salonId: String, userId: String): Boolean {
        return try {
            val salon = salonRepository.findById(salonId).orElse(null)
            salon?.owner?.id == userId || salon?.owner?.userType == UserType.admin
        } catch (e: Exception) {
            false
        }
    }

    @Transactional
    fun importServices(salonId: String, services: List<CreateServiceRequest>): List<ServiceResponse> {
        val salon = salonRepository.findById(salonId)
            .orElseThrow { SalonNotFoundException(salonId) }

        val createdServices = mutableListOf<SalonService>()

        services.forEach { request ->
            if (!salonServiceRepository.existsBySalonIdAndNameIgnoreCase(salonId, request.name)) {
                val service = SalonService(
                    id = UUID.randomUUID().toString(),
                    name = request.name.trim(),
                    description = request.description?.trim(),
                    durationMinutes = request.durationMinutes,
                    price = request.price,
                    category = request.category,
                    salon = salon
                )

                if (service.isValid()) {
                    createdServices.add(service)
                }
            }
        }

        val savedServices = salonServiceRepository.saveAll(createdServices)

        println("📦 Import de ${savedServices.size} services pour le salon ${salon.name}")

        return ServiceResponse.fromEntities(savedServices)
    }
}