package com.frollot.model

/**
 * Enum pour les types d'entités pouvant être suivies.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 * 
 * Sérialisé automatiquement par Jackson dans le backend Spring Boot.
 */
enum class FollowingType {
    USER,      // Suivre un utilisateur (client)
    SALON,     // Suivre un salon
    COIFFEUR   // Suivre un coiffeur (User avec userType = hairstylist)
}

