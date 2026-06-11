package com.frollot.model

/**
 * Énumération des types d'actions de modération.
 * Phase H.3 - Modération de Contenu Coiffure
 * 
 * Permet de définir les actions possibles lors de la modération de contenu.
 */
enum class ModerationActionType {
    /**
     * Masquer le contenu (visible uniquement par l'auteur et les admins).
     */
    HIDE,

    /**
     * Supprimer définitivement le contenu.
     */
    DELETE,

    /**
     * Avertir l'auteur sans modifier le contenu.
     */
    WARN;

    /**
     * Retourne le libellé utilisateur de l'action de modération.
     */
    fun getDisplayName(): String {
        return when (this) {
            HIDE -> "Masquer"
            DELETE -> "Supprimer"
            WARN -> "Avertir"
        }
    }

    /**
     * Retourne la description de l'action de modération.
     */
    fun getDescription(): String {
        return when (this) {
            HIDE -> "Le contenu sera masqué pour tous les utilisateurs sauf l'auteur et les administrateurs."
            DELETE -> "Le contenu sera supprimé définitivement et ne pourra pas être restauré."
            WARN -> "Un avertissement sera envoyé à l'auteur sans modifier le contenu."
        }
    }
}

