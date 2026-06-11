package com.frollot.model

/**
 * Énumération des raisons de signalement de contenu.
 * Phase H.1 - Signalement de Contenu
 * 
 * Permet de catégoriser les signalements selon leur nature.
 */
enum class ReportReason {
    /**
     * Contenu inapproprié (violence, harcèlement, etc.)
     */
    INAPPROPRIE,

    /**
     * Spam publicitaire ou contenu non sollicité
     */
    SPAM,

    /**
     * Faux avant/après ou contenu trompeur
     */
    FAUX,

    /**
     * Violation de droits d'auteur
     */
    COPYRIGHT,

    /**
     * Autre raison (à préciser dans additional_info)
     */
    AUTRE;

    /**
     * Retourne le libellé utilisateur de la raison.
     */
    fun getDisplayName(): String {
        return when (this) {
            INAPPROPRIE -> "Contenu inapproprié"
            SPAM -> "Spam publicitaire"
            FAUX -> "Faux avant/après"
            COPYRIGHT -> "Violation de droits d'auteur"
            AUTRE -> "Autre"
        }
    }

    /**
     * Retourne la description de la raison.
     */
    fun getDescription(): String {
        return when (this) {
            INAPPROPRIE -> "Contenu violent, harcelant ou offensant"
            SPAM -> "Publicité non sollicitée ou contenu répétitif"
            FAUX -> "Transformation ou résultat trompeur"
            COPYRIGHT -> "Utilisation non autorisée de contenu protégé"
            AUTRE -> "Autre raison à préciser"
        }
    }
}

