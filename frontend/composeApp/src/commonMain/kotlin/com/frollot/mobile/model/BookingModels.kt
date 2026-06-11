package com.frollot.mobile.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ============================================
// ENUMS AVEC SÉRIALISATION COMPATIBLE BACKEND
// ============================================

/**
 * Énumération des statuts possibles d'une réservation.
 *
 * CORRECTION : @SerialName garantit la compatibilité avec le backend
 * - Code Kotlin : BookingStatus.PENDING
 * - JSON backend : "pending"
 */
@Serializable
enum class BookingStatus {
    /** Réservation créée, en attente de confirmation */
    @SerialName("pending")
    PENDING,

    /** Réservation confirmée par le salon */
    @SerialName("confirmed")
    CONFIRMED,

    /** Prestation en cours d'exécution */
    @SerialName("in_progress")
    IN_PROGRESS,

    /** Prestation terminée avec succès */
    @SerialName("completed")
    COMPLETED,

    /** Réservation annulée */
    @SerialName("cancelled")
    CANCELLED,

    /** Client ne s'est pas présenté */
    @SerialName("no_show")
    NO_SHOW;

    /**
     * Retourne le libellé français du statut.
     */
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "En attente"
            CONFIRMED -> "Confirmée"
            IN_PROGRESS -> "En cours"
            COMPLETED -> "Terminée"
            CANCELLED -> "Annulée"
            NO_SHOW -> "Absence"
        }
    }

    /**
     * Retourne l'emoji associé au statut.
     */
    fun getEmoji(): String {
        return when (this) {
            PENDING -> "⏳"
            CONFIRMED -> "✅"
            IN_PROGRESS -> "⚡"
            COMPLETED -> "🎉"
            CANCELLED -> "❌"
            NO_SHOW -> "👻"
        }
    }

    /**
     * Retourne la couleur associée au statut (format hex).
     */
    fun getColor(): Long {
        return when (this) {
            PENDING -> 0xFFFFA726      // Orange
            CONFIRMED -> 0xFF66BB6A    // Vert
            IN_PROGRESS -> 0xFF42A5F5  // Bleu
            COMPLETED -> 0xFF9CCC65    // Vert clair
            CANCELLED -> 0xFFEF5350    // Rouge
            NO_SHOW -> 0xFF78909C      // Gris
        }
    }

    /**
     * Vérifie si le statut est final (ne peut plus être modifié).
     */
    fun isFinal(): Boolean {
        return this in listOf(COMPLETED, CANCELLED, NO_SHOW)
    }
}

/**
 * Énumération des statuts de paiement.
 *
 * CORRECTION : @SerialName garantit la compatibilité avec le backend
 * Ajout des statuts Stripe pour l'intégration complète
 */
@Serializable
enum class PaymentStatus {
    /** Statuts Stripe **/
    @SerialName("pending")
    PENDING,

    @SerialName("processing")
    PROCESSING,

    @SerialName("succeeded")
    SUCCEEDED,

    @SerialName("failed")
    FAILED,

    @SerialName("canceled")
    CANCELED,

    @SerialName("partially_refunded")
    PARTIALLY_REFUNDED,

    /** Statuts existants **/
    @SerialName("unpaid")
    UNPAID,

    @SerialName("paid")
    PAID,

    @SerialName("refunded")
    REFUNDED;

    /**
     * Retourne le libellé français du statut.
     */
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "En attente"
            PROCESSING -> "En cours de traitement"
            SUCCEEDED -> "Réussi"
            FAILED -> "Échoué"
            CANCELED -> "Annulé"
            PARTIALLY_REFUNDED -> "Partiellement remboursé"
            UNPAID -> "Non payé"
            PAID -> "Payé"
            REFUNDED -> "Remboursé"
        }
    }

    /**
     * Retourne l'emoji associé au statut.
     */
    fun getEmoji(): String {
        return when (this) {
            PENDING -> "⏳"
            PROCESSING -> "🔄"
            SUCCEEDED, PAID -> "✅"
            FAILED -> "❌"
            CANCELED -> "🚫"
            PARTIALLY_REFUNDED -> "↩️"
            UNPAID -> "💳"
            REFUNDED -> "💰"
        }
    }

    /**
     * Vérifie si le paiement est considéré comme réussi.
     */
    fun isSuccess(): Boolean {
        return this in listOf(SUCCEEDED, PAID)
    }

    /**
     * Vérifie si le paiement est en cours.
     */
    fun isPending(): Boolean {
        return this in listOf(PENDING, PROCESSING)
    }

    /**
     * Vérifie si le paiement a échoué.
     */
    fun isFailed(): Boolean {
        return this in listOf(FAILED, CANCELED)
    }

    /**
     * Vérifie si le paiement a été (partiellement) remboursé.
     */
    fun isRefunded(): Boolean {
        return this in listOf(REFUNDED, PARTIALLY_REFUNDED)
    }
}

// ============================================
// REQUEST DTOs
// ============================================

/**
 * DTO pour créer une nouvelle réservation.
 *
 * IMPORTANT : Correspond au CreateBookingRequest du backend.
 */
@Serializable
data class CreateBookingRequest(
    val salonId: String,
    val clientId: String,
    val staffId: String? = null,
    val serviceId: String,
    val bookingDatetime: String, // Format ISO-8601
    val notesClient: String? = null
)

/**
 * DTO pour mettre à jour le statut d'une réservation.
 */
@Serializable
data class UpdateBookingStatusRequest(
    val status: BookingStatus,
    val notesSalon: String? = null
)

/**
 * DTO pour mettre à jour le paiement d'une réservation.
 */
@Serializable
data class UpdateBookingPaymentRequest(
    val paymentStatus: PaymentStatus,
    val paymentMethod: String? = null,
    val priceFinal: Double? = null
)

/**
 * DTO pour rechercher les créneaux disponibles.
 */
@Serializable
data class AvailableSlotsRequest(
    val salonId: String,
    val serviceId: String,
    val staffId: String? = null,
    val date: String // Format ISO-8601
)

// ============================================
// RESPONSE DTOs
// ============================================

/**
 * DTO de réponse pour une réservation complète.
 *
 * IMPORTANT : Correspond au BookingResponse du backend.
 */
@Serializable
data class BookingResponse(
    val id: String,
    val salonId: String,
    val salonName: String,
    val clientId: String,
    val clientName: String,
    val clientEmail: String,
    val clientPhone: String? = null,
    val staffId: String? = null,
    val staffName: String? = null,
    val serviceId: String,
    val serviceName: String,
    val serviceCategory: String,
    val bookingDatetime: String, // Format ISO-8601
    val endDatetime: String, // Format ISO-8601
    val durationMinutes: Int,
    val formattedDuration: String,
    val status: BookingStatus,
    val statusLabel: String,
    val priceFinal: Double? = null,
    val formattedPrice: String? = null,
    val paymentStatus: PaymentStatus,
    val paymentStatusLabel: String,
    val paymentMethod: String? = null,
    val notesClient: String? = null,
    val notesSalon: String? = null,
    val reminderSentAt: String? = null,
    val confirmedAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val createdAt: String? = null,
    val canBeCancelled: Boolean,
    val canBeConfirmed: Boolean,
    val isPast: Boolean
) {
    /**
     * Badge de statut avec emoji.
     */
    val statusBadge: String
        get() = "${status.getEmoji()} ${status.getDisplayName()}"

    /**
     * Badge de paiement avec emoji.
     */
    val paymentBadge: String
        get() = "${paymentStatus.getEmoji()} ${paymentStatus.getDisplayName()}"

    /**
     * Résumé court de la réservation.
     */
    val summary: String
        get() = "$serviceName • $formattedDuration • ${formattedPrice ?: "Prix non défini"}"

    /**
     * Informations du coiffeur (ou "Non assigné").
     */
    val staffInfo: String
        get() = staffName ?: "Non assigné"

    /**
     * Vérifie si la réservation nécessite une action.
     */
    val needsAction: Boolean
        get() = status == BookingStatus.PENDING && canBeConfirmed
}

/**
 * DTO simplifié pour les listes de réservations.
 */
@Serializable
data class BookingSummary(
    val id: String,
    val salonName: String,
    val clientName: String,
    val staffName: String? = null,
    val serviceName: String,
    val bookingDatetime: String,
    val durationMinutes: Int,
    val status: BookingStatus,
    val statusLabel: String,
    val price: String
) {
    /**
     * Badge de statut avec emoji.
     */
    val statusBadge: String
        get() = "${status.getEmoji()} ${status.getDisplayName()}"
}

/**
 * DTO représentant un créneau horaire disponible.
 */
@Serializable
data class TimeSlot(
    val datetime: String, // Format ISO-8601
    val staffId: String? = null,
    val staffName: String? = null,
    val available: Boolean
) {
    /**
     * Heure formatée (ex: "14:30").
     */
    val formattedTime: String
        get() = try {
            // Format basique: extraire l'heure de l'ISO-8601
            val time = datetime.substring(11, 16)
            time
        } catch (e: Exception) {
            datetime
        }

    /**
     * Informations du coiffeur (ou "Tous disponibles").
     */
    val staffInfo: String
        get() = staffName ?: "Tous disponibles"
}

/**
 * DTO de réponse pour la recherche de créneaux disponibles.
 */
@Serializable
data class AvailableSlotsResponse(
    val date: String, // Format ISO-8601
    val salonId: String,
    val serviceId: String,
    val slots: List<TimeSlot>
) {
    /**
     * Nombre de créneaux disponibles.
     */
    val availableCount: Int
        get() = slots.count { it.available }

    /**
     * Vérifie si des créneaux sont disponibles.
     */
    val hasSlots: Boolean
        get() = slots.isNotEmpty()

    /**
     * Texte résumé.
     */
    val summary: String
        get() = if (hasSlots) {
            "$availableCount créneau${if (availableCount > 1) "x" else ""} disponible${if (availableCount > 1) "s" else ""}"
        } else {
            "Aucun créneau disponible"
        }
}

/**
 * DTO pour les statistiques des réservations d'un salon.
 */
@Serializable
data class BookingStatistics(
    val salonId: String,
    val totalBookings: Int,
    val pendingBookings: Int,
    val confirmedBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,
    val totalRevenue: Double,
    val averagePrice: Double
) {
    /**
     * Taux de complétion (%).
     */
    val completionRate: Int
        get() = if (totalBookings > 0) {
            ((completedBookings.toDouble() / totalBookings) * 100).toInt()
        } else {
            0
        }

    /**
     * Taux d'annulation (%).
     */
    val cancellationRate: Int
        get() = if (totalBookings > 0) {
            ((cancelledBookings.toDouble() / totalBookings) * 100).toInt()
        } else {
            0
        }

    /**
     * Revenu total formaté.
     */
    val formattedRevenue: String
        get() = formatPrice(totalRevenue)

    /**
     * Prix moyen formaté.
     */
    val formattedAveragePrice: String
        get() = formatPrice(averagePrice)

    /**
     * Résumé textuel.
     */
    val summary: String
        get() = "$totalBookings réservation${if (totalBookings > 1) "s" else ""} • $formattedRevenue de revenu"

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
}
