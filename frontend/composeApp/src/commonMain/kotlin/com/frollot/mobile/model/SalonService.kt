package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * DTO représentant une prestation de service d'un salon.
 *
 * IMPORTANT : Ce DTO DOIT correspondre EXACTEMENT au ServiceResponse du backend.
 * Structure basée sur com.frollot.dto.ServiceResponse (backend).
 *
 * Utilisé pour :
 * - Affichage dans l'écran de détail du salon
 * - Sélection d'un service pour réservation
 * - Filtrage et recherche de services
 *
 * @property id Identifiant unique du service (UUID)
 * @property salonId Identifiant du salon propriétaire
 * @property name Nom de la prestation (ex: "Coupe Homme", "Coloration complète")
 * @property description Description détaillée du service (optionnelle)
 * @property durationMinutes Durée en minutes (format numérique brut)
 * @property formattedDuration Durée formatée pour l'UI (ex: "1h30", "45min")
 * @property price Prix en euros (Double car JSON convertit BigDecimal en Double)
 * @property category Catégorie technique (enum)
 * @property categoryLabel Libellé utilisateur de la catégorie (ex: "Coupe & Taille")
 * @property categoryEmoji Emoji associé à la catégorie pour l'UI (ex: "✂️")
 */
@Serializable
data class SalonService(
    val id: String,
    val salonId: String,
    val name: String,
    val description: String? = null,
    val durationMinutes: Int,
    val formattedDuration: String,
    val price: Double,
    val category: ServiceCategory,
    val categoryLabel: String,
    val categoryEmoji: String,
    /** URLs des images d'illustration du service */
    val imageUrls: List<String> = emptyList()
) {
    /**
     * Prix formaté avec le symbole euro et deux décimales.
     *
     * Exemple: 25.50 → "25.50€"
     */
    val formattedPrice: String
        get() = formatPrice(price)

    /**
     * Durée et prix combinés pour un affichage compact.
     *
     * Utilisé dans les cartes de services pour un aperçu rapide.
     * Exemple: "1h30 • 25.50€"
     */
    val durationAndPrice: String
        get() = "$formattedDuration • $formattedPrice"

    /**
     * Vérifie si le service a une description.
     */
    val hasDescription: Boolean
        get() = !description.isNullOrBlank()

    /**
     * Vérifie si le service a des images d'illustration.
     */
    val hasImages: Boolean
        get() = imageUrls.isNotEmpty()

    /**
     * Retourne la première image d'illustration (pour affichage en miniature).
     */
    val thumbnailUrl: String?
        get() = imageUrls.firstOrNull()

    /**
     * Retourne une estimation du temps d'attente en format lisible.
     * Utilisé pour les features de file d'attente.
     */
    fun getEstimatedWaitTime(position: Int): String {
        val totalMinutes = durationMinutes * position
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "~${hours}h${minutes}min"
            hours > 0 -> "~${hours}h"
            else -> "~${minutes}min"
        }
    }

    /**
     * Formate un prix en euros avec 2 décimales.
     * 
     * Phase 4 - Fonctionnalité Langue : Utilise le formatage localisé.
     * Note: Utilise le français par défaut car cette méthode est dans une data class.
     * Pour un formatage selon la langue courante, utilisez formatLocalizedCurrency() dans les composables.
     */
    private fun formatPrice(price: Double): String {
        return com.frollot.mobile.localization.formatCurrencyForLanguageStatic(price, "fr")
    }

    /**
     * Retourne une représentation textuelle pour les logs.
     */
    override fun toString(): String {
        return "SalonService(id=$id, name='$name', duration=$durationMinutes min, price=$price€, category=$category)"
    }
}

/**
 * DTO pour créer un nouveau service depuis l'application mobile.
 *
 * IMPORTANT : Correspond au CreateServiceRequest du backend.
 * Le backend attend un BigDecimal, on envoie donc un String qui sera converti.
 *
 * @property salonId Identifiant du salon
 * @property name Nom de la prestation
 * @property description Description détaillée (optionnelle)
 * @property durationMinutes Durée estimée en minutes (défaut: 30 min)
 * @property price Prix en euros au format String (sera converti en BigDecimal côté backend)
 * @property category Catégorie du service (défaut: COUPE)
 */
@Serializable
data class CreateServiceRequest(
    val salonId: String,
    val name: String,
    val description: String? = null,
    val durationMinutes: Int = 30,
    val price: String = "0.0",
    val category: ServiceCategory = ServiceCategory.COUPE,
    /** URLs des images d'illustration (max 5) */
    val imageUrls: List<String>? = null
)

/**
 * DTO pour mettre à jour un service existant depuis l'application mobile.
 *
 * IMPORTANT : Correspond au UpdateServiceRequest du backend.
 * Tous les champs sont optionnels (null = ne pas modifier).
 */
@Serializable
data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val durationMinutes: Int? = null,
    val price: String? = null,
    val category: ServiceCategory? = null,
    /** URLs des images d'illustration (max 5) */
    val imageUrls: List<String>? = null
) {
    /**
     * Vérifie si la requête contient au moins une modification.
     */
    fun hasChanges(): Boolean {
        return name != null ||
                description != null ||
                durationMinutes != null ||
                price != null ||
                category != null ||
                imageUrls != null
    }
}
