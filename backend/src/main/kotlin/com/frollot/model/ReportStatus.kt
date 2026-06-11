package com.frollot.model

/**
 * Énumération des statuts d'un signalement.
 * Phase H.1 - Signalement de Contenu
 * 
 * Permet de suivre l'état de traitement d'un signalement.
 */
enum class ReportStatus {
    /**
     * Signalement en attente de traitement par un modérateur
     */
    PENDING,

    /**
     * Signalement en cours d'examen par un modérateur
     */
    REVIEWED,

    /**
     * Signalement traité et résolu (action prise)
     */
    RESOLVED,

    /**
     * Signalement rejeté (non fondé)
     */
    DISMISSED;

    /**
     * Retourne le libellé utilisateur du statut.
     */
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "En attente"
            REVIEWED -> "En cours d'examen"
            RESOLVED -> "Résolu"
            DISMISSED -> "Rejeté"
        }
    }
}

