package com.frollot.model

/**
 * Enum pour les périodes de trending.
 * Phase C.3 - Trending Coiffure
 * 
 * Sérialisé automatiquement par Jackson dans le backend Spring Boot.
 */
enum class TrendPeriod {
    LAST_24H,    // Dernières 24 heures
    LAST_7D,     // 7 derniers jours
    LAST_30D     // 30 derniers jours
}

