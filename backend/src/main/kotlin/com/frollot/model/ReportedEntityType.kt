package com.frollot.model

/**
 * Énumération des types d'entités pouvant être signalées.
 * Phase H.1 - Signalement de Contenu
 * 
 * Permet d'identifier le type de contenu signalé.
 */
enum class ReportedEntityType {
    /**
     * Post signalé
     */
    POST,

    /**
     * Commentaire signalé
     */
    COMMENT,

    /**
     * Utilisateur signalé
     */
    USER,

    /**
     * Salon signalé
     */
    SALON;

    /**
     * Retourne le libellé utilisateur du type d'entité.
     */
    fun getDisplayName(): String {
        return when (this) {
            POST -> "Post"
            COMMENT -> "Commentaire"
            USER -> "Utilisateur"
            SALON -> "Salon"
        }
    }
}

