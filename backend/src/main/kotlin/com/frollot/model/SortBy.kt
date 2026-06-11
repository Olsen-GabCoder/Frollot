package com.frollot.model

/**
 * Enum pour le tri des posts.
 * Phase C.2 - Feed par Salon
 * 
 * Sérialisé automatiquement par Jackson dans le backend Spring Boot.
 */
enum class SortBy {
    RECENT,    // Plus récents en premier
    POPULAR    // Plus populaires en premier (likes + commentaires + shares)
}


