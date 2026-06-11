package com.frollot.model

/**
 * Énumération des catégories de hashtags coiffure.
 */
enum class HairHashtagCategory {
    /**
     * Techniques de coiffure (balayage, dégradé, carré, etc.)
     */
    TECHNIQUE,

    /**
     * Styles de coupe (bob, pixie, franges, etc.)
     */
    STYLE,

    /**
     * Couleurs (blond, brun, rouge, etc.)
     */
    COULEUR,

    /**
     * Longueurs de cheveux (court, mi-long, long, etc.)
     */
    LONGUEUR,

    /**
     * Textures capillaires (fins, épais, ondulés, etc.)
     */
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

