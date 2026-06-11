package com.frollot.model

/**
 * Énumération des niveaux de visibilité des posts.
 * Phase F.3 - Visibilité des Posts (Public/Followers/Private)
 * 
 * Permet de contrôler qui peut voir les posts selon le niveau de visibilité choisi.
 */
enum class PostVisibility {
    /**
     * Post public : visible par tous les utilisateurs, même non connectés.
     * Idéal pour promouvoir le salon, partager des créations, etc.
     */
    PUBLIC,

    /**
     * Post visible uniquement par les followers de l'auteur.
     * Pour du contenu exclusif, des offres spéciales, etc.
     */
    FOLLOWERS,

    /**
     * Post privé : visible uniquement par l'auteur.
     * Pour les brouillons, archives personnelles, etc.
     */
    PRIVATE;

    /**
     * Retourne le libellé utilisateur du niveau de visibilité.
     */
    fun getDisplayName(): String {
        return when (this) {
            PUBLIC -> "Public"
            FOLLOWERS -> "Abonnés uniquement"
            PRIVATE -> "Privé"
        }
    }

    /**
     * Retourne l'emoji associé au niveau de visibilité.
     */
    fun getEmoji(): String {
        return when (this) {
            PUBLIC -> "🌐"
            FOLLOWERS -> "👥"
            PRIVATE -> "🔒"
        }
    }

    /**
     * Retourne la description du niveau de visibilité.
     */
    fun getDescription(): String {
        return when (this) {
            PUBLIC -> "Visible par tous"
            FOLLOWERS -> "Visible uniquement par vos abonnés"
            PRIVATE -> "Visible uniquement par vous"
        }
    }
}

