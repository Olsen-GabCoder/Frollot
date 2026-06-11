package com.frollot.model

/**
 * Énumération des statuts d'appel pour les actions de modération.
 * Alias pour AppealStatus pour compatibilité frontend.
 *
 * Phase H.3 - Modération de Contenu Coiffure
 */
enum class ModerationAppealStatus {
    /**
     * Aucun appel n'a été fait.
     */
    NONE,

    /**
     * Un appel est en attente de traitement.
     */
    PENDING,

    /**
     * L'appel a été approuvé, l'action de modération est annulée.
     */
    APPROVED,

    /**
     * L'appel a été rejeté, l'action de modération est maintenue.
     */
    REJECTED;

    /**
     * Retourne le libellé utilisateur du statut d'appel.
     */
    fun getDisplayName(): String {
        return when (this) {
            NONE -> "Aucun appel"
            PENDING -> "En attente"
            APPROVED -> "Approuvé"
            REJECTED -> "Rejeté"
        }
    }
}