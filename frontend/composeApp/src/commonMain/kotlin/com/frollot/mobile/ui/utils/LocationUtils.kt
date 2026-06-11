package com.frollot.mobile.ui.utils

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.round

/**
 * Utilitaires pour les calculs de localisation et de distance.
 * Phase C.4 - Découverte par Localisation
 */

/**
 * Calcule la distance entre deux points géographiques en utilisant la formule de Haversine.
 * 
 * @param lat1 Latitude du premier point
 * @param lng1 Longitude du premier point
 * @param lat2 Latitude du deuxième point
 * @param lng2 Longitude du deuxième point
 * @return Distance en kilomètres
 */
fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0 // Rayon de la Terre en kilomètres
    
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLng = (lng2 - lng1) * PI / 180.0
    
    val sinDLatHalf = sin(dLat / 2)
    val cosLat1 = cos(lat1 * PI / 180.0)
    val cosLat2 = cos(lat2 * PI / 180.0)
    val sinDLngHalf = sin(dLng / 2)

    val a = sinDLatHalf * sinDLatHalf + cosLat1 * cosLat2 * sinDLngHalf * sinDLngHalf

    val sqrtA = sqrt(a)
    val oneMinusA = 1.0 - a
    val sqrt1MinusA = sqrt(oneMinusA)
    val c = 2.0 * atan2(sqrtA, sqrt1MinusA)
    
    return r * c
}

/**
 * Formate une distance en kilomètres en une chaîne lisible.
 * 
 * @param distanceKm Distance en kilomètres
 * @return Chaîne formatée (ex: "500 m", "2.5 km", "15 km")
 */
fun formatDistance(distanceKm: Double): String {
    return when {
        distanceKm < 0.1 -> {
            val meters = (distanceKm * 1000).toInt()
            "$meters m"
        }
        distanceKm < 1.0 -> {
            val meters = (distanceKm * 1000).toInt()
            "$meters m"
        }
        distanceKm < 10.0 -> {
            val km = round(distanceKm * 10).toInt() / 10.0
            if (km == km.toInt().toDouble()) {
                "${km.toInt()} km"
            } else {
                "$km km"
            }
        }
        else -> {
            val km = distanceKm.toInt()
            "$km km"
        }
    }
}

/**
 * Calcule la distance entre un salon et une position donnée.
 * 
 * @param salonLatitude Latitude du salon (peut être null)
 * @param salonLongitude Longitude du salon (peut être null)
 * @param userLatitude Latitude de l'utilisateur
 * @param userLongitude Longitude de l'utilisateur
 * @return Distance en kilomètres, ou null si les coordonnées du salon ne sont pas disponibles
 */
fun calculateDistanceToSalon(
    salonLatitude: Double?,
    salonLongitude: Double?,
    userLatitude: Double,
    userLongitude: Double
): Double? {
    if (salonLatitude == null || salonLongitude == null) {
        return null
    }
    return calculateDistance(userLatitude, userLongitude, salonLatitude, salonLongitude)
}

