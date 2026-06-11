package com.frollot.mobile.model

import kotlinx.serialization.Serializable

/**
 * Énumération des catégories de services.
 * DOIT correspondre EXACTEMENT au backend (ServiceCategory.kt backend).
 *
 * Utilisée pour :
 * - Affichage avec emojis dans l'UI
 * - Filtrage des services par catégorie
 * - Sérialisation/désérialisation JSON
 */
@Serializable
enum class ServiceCategory {
    /** Coupes de cheveux pour hommes, femmes, enfants */
    COUPE,

    /** Coloration, mèches, balayage, ombré hair */
    COLORATION,

    /** Soins capillaires, masques, traitements */
    SOIN,

    /** Coiffage pour événements, brushing, lissage */
    COIFFAGE,

    /** Soins de barbe, taille, rasage traditionnel */
    BARBE,

    /** Techniques spéciales (dreadlocks, tresses africaines, perruques) */
    TECHNIQUE,

    /** Autres prestations non catégorisées */
    AUTRE;

    /**
     * Retourne l'emoji associé à la catégorie pour l'UI.
     *
     * Utilisé dans les cartes de services pour une identification visuelle rapide.
     */
    fun getEmoji(): String {
        return when (this) {
            COUPE -> "✂️"
            COLORATION -> "🎨"
            SOIN -> "💆"
            COIFFAGE -> "💇"
            BARBE -> "🧔"
            TECHNIQUE -> "🌟"
            AUTRE -> "📋"
        }
    }

    /**
     * Retourne le libellé utilisateur de la catégorie.
     *
     * Version française lisible pour l'affichage dans l'interface.
     */
    fun getDisplayName(): String {
        return when (this) {
            COUPE -> "Coupe & Taille"
            COLORATION -> "Coloration"
            SOIN -> "Soins"
            COIFFAGE -> "Coiffage"
            BARBE -> "Barbier"
            TECHNIQUE -> "Techniques Spéciales"
            AUTRE -> "Autres Prestations"
        }
    }

    /**
     * Retourne une couleur d'accentuation pour la catégorie (format hex).
     * Utilisé pour personnaliser l'UI par catégorie.
     */
    fun getAccentColor(): Long {
        return when (this) {
            COUPE -> 0xFF2196F3       // Bleu
            COLORATION -> 0xFFE91E63   // Rose
            SOIN -> 0xFF4CAF50         // Vert
            COIFFAGE -> 0xFF9C27B0     // Violet
            BARBE -> 0xFF795548        // Marron
            TECHNIQUE -> 0xFFFF9800    // Orange
            AUTRE -> 0xFF607D8B        // Gris bleuté
        }
    }
}
