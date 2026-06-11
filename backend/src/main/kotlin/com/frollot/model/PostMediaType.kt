package com.frollot.model

/**
 * Énumération des types de médias pour les posts.
 * 
 * Utilisé principalement pour les posts de type AVANT_APRES,
 * mais peut être utilisé pour d'autres types de posts nécessitant plusieurs images.
 */
enum class PostMediaType {
    /**
     * Image "avant" la transformation.
     */
    before,

    /**
     * Image "après" la transformation.
     */
    after,

    /**
     * Image du processus (étape intermédiaire).
     */
    process,

    /**
     * Image de détail (zoom, technique, etc.).
     */
    detail;

    /**
     * Retourne le libellé utilisateur du type.
     */
    fun getDisplayName(): String {
        return when (this) {
            before -> "Avant"
            after -> "Après"
            process -> "Processus"
            detail -> "Détail"
        }
    }

    /**
     * Retourne l'emoji associé au type.
     */
    fun getEmoji(): String {
        return when (this) {
            before -> "📸"
            after -> "✨"
            process -> "⚙️"
            detail -> "🔍"
        }
    }
}

