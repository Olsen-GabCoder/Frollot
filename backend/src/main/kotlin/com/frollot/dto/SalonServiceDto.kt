package com.frollot.dto

import com.frollot.model.SalonService
import com.frollot.model.ServiceCategory
import jakarta.validation.constraints.*
import java.math.BigDecimal

// ============================================
// 1. ServiceResponse - DTO de réponse
// ============================================

/**
 * DTO de réponse pour une prestation de service.
 *
 * TOUS les champs sont obligatoires pour éviter les erreurs de sérialisation côté mobile.
 * Ce DTO correspond EXACTEMENT au modèle mobile SalonService.kt
 */
data class ServiceResponse(
    val id: String,
    val salonId: String,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val formattedDuration: String,
    val price: Double,
    val category: ServiceCategory,
    val categoryLabel: String,
    val categoryEmoji: String,
    /** URLs des images d'illustration du service */
    val imageUrls: List<String> = emptyList()
) {
    companion object {
        /**
         * Convertit une entité SalonService en ServiceResponse.
         * S'assure que tous les champs requis sont présents.
         *
         * @param service Entité SalonService à convertir
         * @return ServiceResponse avec tous les champs remplis
         * @throws IllegalArgumentException si l'entité est invalide
         */
        fun fromEntity(service: SalonService): ServiceResponse {
            // Validation stricte des champs obligatoires
            require(service.id.isNotBlank()) {
                "Service ID cannot be blank"
            }
            require(service.salon?.id != null) {
                "Salon ID cannot be null for service ${service.id}"
            }
            require(service.name.isNotBlank()) {
                "Service name cannot be blank"
            }

            return ServiceResponse(
                id = service.id,
                salonId = service.salon!!.id!!,
                name = service.name,
                description = service.description,
                durationMinutes = service.durationMinutes,
                formattedDuration = service.getFormattedDuration(),
                price = service.price.toDouble(),
                category = service.category,
                categoryLabel = service.category.getDisplayName(),
                categoryEmoji = service.category.getEmoji(),
                imageUrls = service.imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            )
        }

        /**
         * Convertit une liste d'entités en liste de DTOs.
         *
         * @param services Liste d'entités SalonService
         * @return Liste de ServiceResponse
         */
        fun fromEntities(services: List<SalonService>): List<ServiceResponse> {
            return services.map { fromEntity(it) }
        }
    }
}

// ============================================
// 2. CreateServiceRequest - DTO de création
// ============================================

/**
 * DTO de requête pour créer une nouvelle prestation de service.
 *
 * Utilisé par les endpoints POST /api/salons/{salonId}/services
 */
data class CreateServiceRequest(
    @field:NotBlank(message = "Le salonId est obligatoire")
    val salonId: String,

    @field:NotBlank(message = "Le nom est obligatoire")
    @field:Size(min = 3, max = 150, message = "Le nom doit contenir entre 3 et 150 caractères")
    val name: String,

    @field:Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    val description: String? = null,

    @field:Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    @field:Max(value = 480, message = "La durée ne peut pas dépasser 480 minutes (8h)")
    val durationMinutes: Int = 30,

    @field:DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    @field:DecimalMax(value = "10000.0", message = "Le prix ne peut pas dépasser 10 000€")
    @field:Digits(integer = 5, fraction = 2, message = "Le prix doit avoir maximum 5 chiffres entiers et 2 décimales")
    val price: BigDecimal = BigDecimal.ZERO,

    @field:NotNull(message = "La catégorie est obligatoire")
    val category: ServiceCategory = ServiceCategory.COUPE,

    /** URLs des images d'illustration du service (max 5 images) */
    @field:Size(max = 5, message = "Maximum 5 images autorisées")
    val imageUrls: List<String>? = null
) {
    /**
     * Valide les données de la requête.
     * Lancé avant la création du service.
     *
     * @throws IllegalArgumentException si les données sont invalides
     */
    fun validate() {
        require(salonId.isNotBlank()) { "Le salonId est obligatoire" }
        require(name.isNotBlank()) { "Le nom est obligatoire" }
        require(name.length in 3..150) { "Le nom doit contenir entre 3 et 150 caractères" }
        require(durationMinutes in 1..480) { "La durée doit être entre 1 et 480 minutes" }
        require(price >= BigDecimal.ZERO) { "Le prix ne peut pas être négatif" }
        require(price <= BigDecimal(10000)) { "Le prix ne peut pas dépasser 10 000€" }

        description?.let {
            require(it.length <= 1000) { "La description ne peut pas dépasser 1000 caractères" }
        }
    }
}

// ============================================
// 3. UpdateServiceRequest - DTO de mise à jour
// ============================================

/**
 * DTO de requête pour mettre à jour une prestation existante.
 * Tous les champs sont optionnels (null = ne pas modifier).
 *
 * Utilisé par les endpoints PUT /api/salons/{salonId}/services/{serviceId}
 */
data class UpdateServiceRequest(
    @field:Size(min = 3, max = 150, message = "Le nom doit contenir entre 3 et 150 caractères")
    val name: String? = null,

    @field:Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    val description: String? = null,

    @field:Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    @field:Max(value = 480, message = "La durée ne peut pas dépasser 480 minutes (8h)")
    val durationMinutes: Int? = null,

    @field:DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    @field:DecimalMax(value = "10000.0", message = "Le prix ne peut pas dépasser 10 000€")
    @field:Digits(integer = 5, fraction = 2, message = "Le prix doit avoir maximum 5 chiffres entiers et 2 décimales")
    val price: BigDecimal? = null,

    val category: ServiceCategory? = null,

    /** URLs des images d'illustration du service (max 5 images) */
    @field:Size(max = 5, message = "Maximum 5 images autorisées")
    val imageUrls: List<String>? = null
) {
    /**
     * Applique les modifications au service existant.
     * Ne modifie que les champs non-null de la requête.
     *
     * @param service Service à modifier
     * @return Service modifié (même instance)
     */
    fun applyTo(service: SalonService): SalonService {
        name?.let {
            service.name = it.trim()
        }

        description?.let {
            service.description = it.trim().takeIf { str -> str.isNotBlank() }
        }

        durationMinutes?.let {
            service.durationMinutes = it
        }

        price?.let {
            service.price = it
        }

        category?.let {
            service.category = it
        }

        imageUrls?.let {
            service.imageUrls = it.joinToString(",")
        }

        return service
    }

    /**
     * Vérifie si la requête contient au moins une modification.
     *
     * @return true si au moins un champ est non-null
     */
    fun hasChanges(): Boolean {
        return name != null ||
                description != null ||
                durationMinutes != null ||
                price != null ||
                category != null ||
                imageUrls != null
    }

    /**
     * Valide les modifications demandées.
     *
     * @throws IllegalArgumentException si les valeurs sont invalides
     */
    fun validate() {
        name?.let {
            require(it.length in 3..150) { "Le nom doit contenir entre 3 et 150 caractères" }
        }

        description?.let {
            require(it.length <= 1000) { "La description ne peut pas dépasser 1000 caractères" }
        }

        durationMinutes?.let {
            require(it in 1..480) { "La durée doit être entre 1 et 480 minutes" }
        }

        price?.let {
            require(it >= BigDecimal.ZERO) { "Le prix ne peut pas être négatif" }
            require(it <= BigDecimal(10000)) { "Le prix ne peut pas dépasser 10 000€" }
        }
    }
}