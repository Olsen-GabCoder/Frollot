package com.frollot.model

/**
 * Énumération des types de posts spécialisés pour l'univers de la coiffure.
 * 
 * Permet de catégoriser les posts selon leur contexte et leur objectif.
 */
enum class PostType {
    /**
     * Post général, sans catégorie spécifique.
     */
    GENERAL,

    /**
     * Format avant/après pour montrer une transformation coiffure.
     * Idéal pour les transformations visuelles impactantes.
     */
    AVANT_APRES,

    /**
     * Post destiné à être organisé dans un portfolio.
     * Utilisé par les coiffeurs pour présenter leurs créations.
     */
    PORTFOLIO,

    /**
     * Post mettant en avant une tendance coiffure.
     * Pour partager les dernières tendances, styles populaires, etc.
     */
    TENDANCE,

    /**
     * Post contenant des conseils et astuces.
     * Pour partager des connaissances, techniques, bonnes pratiques.
     */
    CONSEIL,

    /**
     * Post montrant une réalisation concrète.
     * Résultat d'une prestation réelle effectuée.
     */
    REALISATION,

    /**
     * Post d'inspiration.
     * Pour partager des idées, des styles à reproduire, etc.
     */
    INSPIRATION;

    /**
     * Retourne le libellé utilisateur du type de post.
     */
    fun getDisplayName(): String {
        return when (this) {
            GENERAL -> "Général"
            AVANT_APRES -> "Avant/Après"
            PORTFOLIO -> "Portfolio"
            TENDANCE -> "Tendance"
            CONSEIL -> "Conseil"
            REALISATION -> "Réalisation"
            INSPIRATION -> "Inspiration"
        }
    }

    /**
     * Retourne l'emoji associé au type de post.
     */
    fun getEmoji(): String {
        return when (this) {
            GENERAL -> "📝"
            AVANT_APRES -> "✨"
            PORTFOLIO -> "🎨"
            TENDANCE -> "🔥"
            CONSEIL -> "💡"
            REALISATION -> "✂️"
            INSPIRATION -> "💫"
        }
    }

    /**
     * Retourne la description du type de post.
     */
    fun getDescription(): String {
        return when (this) {
            GENERAL -> "Post général"
            AVANT_APRES -> "Montrer une transformation avant/après"
            PORTFOLIO -> "Ajouter à votre portfolio"
            TENDANCE -> "Partager une tendance coiffure"
            CONSEIL -> "Donner des conseils et astuces"
            REALISATION -> "Montrer une réalisation"
            INSPIRATION -> "Partager une inspiration"
        }
    }
}

