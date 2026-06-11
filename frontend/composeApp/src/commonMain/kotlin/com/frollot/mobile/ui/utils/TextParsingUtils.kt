package com.frollot.mobile.ui.utils

/**
 * Utilitaires pour extraire les hashtags et mentions d'un texte.
 */

/**
 * Extrait tous les hashtags d'un texte (mots commençant par #).
 * Exemple: "J'adore #balayage et #coupebob" -> ["balayage", "coupebob"]
 */
fun extractHashtags(text: String): List<String> {
    val hashtagPattern = Regex("#(\\w+)")
    return hashtagPattern.findAll(text)
        .map { it.groupValues[1].lowercase() }
        .distinct()
        .toList()
}

/**
 * Extrait toutes les mentions d'un texte (mots commençant par @).
 * Retourne une liste de paires (mention, type supposé).
 * Exemple: "@salon_elegance @jean_dupont" -> [("salon_elegance", "salon"), ("jean_dupont", "user")]
 */
fun extractMentions(text: String): List<Pair<String, String>> {
    val mentionPattern = Regex("@(\\w+)")
    return mentionPattern.findAll(text)
        .map { 
            val mention = it.groupValues[1]
            // Pour l'instant, on ne peut pas déterminer le type sans recherche
            // On retourne juste la mention, le type sera déterminé lors de la création du post
            Pair(mention, "unknown")
        }
        .distinct()
        .toList()
}

/**
 * Vérifie si le texte à la position du curseur contient une mention en cours de frappe.
 * Retourne la requête de recherche si c'est le cas.
 */
fun getCurrentMentionQuery(text: String, cursorPosition: Int): String? {
    val textBeforeCursor = text.take(cursorPosition)
    val mentionMatch = Regex("@(\\w*)$").find(textBeforeCursor)
    return mentionMatch?.groupValues?.get(1)
}

