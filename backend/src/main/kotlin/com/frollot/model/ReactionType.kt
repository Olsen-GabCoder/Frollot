package com.frollot.model

/**
 * Énumération des types de réactions spécialisées pour l'univers de la coiffure.
 * 
 * Permet aux utilisateurs de réagir aux posts avec des réactions au-delà du simple like,
 * adaptées au contexte coiffure.
 * Phase D.4 - Réactions Spécialisées Coiffure
 */
enum class ReactionType {
    /**
     * Like classique.
     */
    LIKE,

    /**
     * J'adore cette couleur !
     */
    LOVE,

    /**
     * Transformation incroyable !
     */
    WOW,

    /**
     * Je veux la même chose !
     */
    INSPIRANT,

    /**
     * Travail de qualité !
     */
    MAGNIFIQUE,

    /**
     * Félicitations au coiffeur !
     */
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

