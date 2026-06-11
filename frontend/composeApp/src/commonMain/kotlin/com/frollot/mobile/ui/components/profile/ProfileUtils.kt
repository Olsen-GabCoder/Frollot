package com.frollot.mobile.ui.components.profile

/**
 * Utilitaires pour les composants de profil.
 */

/**
 * Formate un nombre pour l'affichage dans les statistiques de profil.
 * Exemples : 123 -> "123", 1234 -> "1k", 1234567 -> "1M"
 */
fun formatProfileNumber(count: Long): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> "${count / 1000}k"
        else -> "${count / 1000000}M"
    }
}

/**
 * Formate un nombre entier pour l'affichage dans les statistiques de profil.
 */
fun formatProfileNumber(count: Int): String {
    return formatProfileNumber(count.toLong())
}

