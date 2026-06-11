package com.frollot.mobile.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.math.pow
import kotlin.time.ExperimentalTime
import com.frollot.mobile.time.currentInstant
import com.frollot.mobile.time.inWholeMinutesCompat

/**
 * Helpers de formatage localisÃ© pour dates, devises et nombres.
 * 
 * Phase 4 - FonctionnalitÃ© Langue : Formatage localisÃ©
 * Conforme Ã  l'ADR-001 - DÃ‰CISION 5 : Formatage dates/devises.
 * 
 * Ces helpers utilisent la langue courante depuis LocalLanguage
 * pour formater les valeurs selon les conventions locales.
 */

// ========================================
// FORMATAGE DE DATES
// ========================================

/**
 * Formate une date ISO-8601 en format localisÃ© (date seulement).
 * 
 * Exemples :
 * - FR: "15/01/2024"
 * - EN: "01/15/2024" ou "15 Jan 2024"
 * - ES: "15/01/2024"
 * - DE: "15.01.2024"
 * - AR: "15/01/2024" (RTL)
 * 
 * @param isoDate Date au format ISO-8601 (ex: "2024-01-15")
 * @return Date formatÃ©e selon la locale
 */
@Composable
fun formatLocalizedDate(isoDate: String): String {
    val language = LocalLanguage.current
    return remember(isoDate, language) {
        formatDateForLanguage(isoDate, language)
    }
}

/**
 * Formate une date-heure ISO-8601 en format localisÃ©.
 * 
 * Exemples :
 * - FR: "15/01/2024 Ã  14:30"
 * - EN: "01/15/2024 at 2:30 PM"
 * - ES: "15/01/2024 a las 14:30"
 * - DE: "15.01.2024 um 14:30"
 * - AR: "15/01/2024 Ø§Ù„Ø³Ø§Ø¹Ø© 14:30" (RTL)
 * 
 * @param isoDateTime Date-heure au format ISO-8601 (ex: "2024-01-15T14:30:00")
 * @return Date-heure formatÃ©e selon la locale
 */
@Composable
fun formatLocalizedDateTime(isoDateTime: String): String {
    val language = LocalLanguage.current
    return remember(isoDateTime, language) {
        formatDateTimeForLanguage(isoDateTime, language)
    }
}

/**
 * Formate une date relative (il y a X minutes/heures/jours).
 * 
 * Exemples :
 * - FR: "Il y a 5min", "Il y a 2h", "Il y a 3j"
 * - EN: "5min ago", "2h ago", "3d ago"
 * - ES: "Hace 5min", "Hace 2h", "Hace 3d"
 * - DE: "vor 5 Min", "vor 2 Std", "vor 3 T"
 * - AR: "Ù…Ù†Ø° 5 Ø¯Ù‚Ø§Ø¦Ù‚", "Ù…Ù†Ø° Ø³Ø§Ø¹ØªÙŠÙ†", "Ù…Ù†Ø° 3 Ø£ÙŠØ§Ù…"
 * 
 * @param isoDateTime Date-heure au format ISO-8601
 * @return Date relative formatÃ©e selon la locale
 */
@Composable
fun formatLocalizedRelativeTime(isoDateTime: String): String {
    val language = LocalLanguage.current
    return remember(isoDateTime, language) {
        formatRelativeTimeForLanguage(isoDateTime, language)
    }
}

// ========================================
// FORMATAGE DE DEVISES
// ========================================

/**
 * Formate un prix en devise localisÃ©e.
 * 
 * Exemples :
 * - FR: "25,50 â‚¬"
 * - EN: "â‚¬25.50"
 * - ES: "25,50 â‚¬"
 * - DE: "25,50 â‚¬"
 * - AR: "25.50 â‚¬" (RTL)
 * 
 * @param price Prix en euros (Double)
 * @return Prix formatÃ© avec symbole de devise selon la locale
 */
@Composable
fun formatLocalizedCurrency(price: Double): String {
    val language = LocalLanguage.current
    return remember(price, language) {
        formatCurrencyForLanguage(price, language)
    }
}

/**
 * Formate un prix en devise localisÃ©e (depuis un String).
 * 
 * @param priceString Prix en format string (ex: "25.50")
 * @return Prix formatÃ© avec symbole de devise selon la locale
 */
@Composable
fun formatLocalizedCurrency(priceString: String): String {
    val price = priceString.toDoubleOrNull() ?: 0.0
    return formatLocalizedCurrency(price)
}

// ========================================
// FORMATAGE DE NOMBRES
// ========================================

/**
 * Formate un nombre avec sÃ©parateurs de milliers localisÃ©s.
 * 
 * Exemples :
 * - FR: "1 234,56"
 * - EN: "1,234.56"
 * - ES: "1.234,56"
 * - DE: "1.234,56"
 * - AR: "1,234.56" (RTL)
 * 
 * @param number Nombre Ã  formater
 * @param decimals Nombre de dÃ©cimales (dÃ©faut: 2)
 * @return Nombre formatÃ© selon la locale
 */
@Composable
fun formatLocalizedNumber(number: Double, decimals: Int = 2): String {
    val language = LocalLanguage.current
    return remember(number, decimals, language) {
        formatNumberForLanguage(number, decimals, language)
    }
}

// ========================================
// IMPLÃ‰MENTATIONS INTERNES
// ========================================

/**
 * Formate une date selon la langue.
 */
private fun formatDateForLanguage(isoDate: String, language: String): String {
    return try {
        val parts = isoDate.split("T")[0].split("-")
        if (parts.size != 3) return isoDate
        
        val year = parts[0]
        val month = parts[1]
        val day = parts[2]
        
        when (language) {
            "en" -> "$month/$day/$year" // MM/DD/YYYY
            "de" -> "$day.$month.$year" // DD.MM.YYYY
            "es" -> "$day/$month/$year" // DD/MM/YYYY
            "ar" -> "$day/$month/$year" // DD/MM/YYYY (RTL)
            else -> "$day/$month/$year" // DD/MM/YYYY (FR par dÃ©faut)
        }
    } catch (e: Exception) {
        isoDate
    }
}

/**
 * Formate une date-heure selon la langue.
 */
private fun formatDateTimeForLanguage(isoDateTime: String, language: String): String {
    return try {
        val parts = isoDateTime.split("T")
        if (parts.size != 2) return isoDateTime
        
        val datePart = parts[0]
        val timePart = parts[1].substring(0, 5) // HH:mm
        
        val formattedDate = formatDateForLanguage(datePart, language)
        
        when (language) {
            "en" -> "$formattedDate at $timePart"
            "es" -> "$formattedDate a las $timePart"
            "de" -> "$formattedDate um $timePart"
            "ar" -> "$formattedDate Ø§Ù„Ø³Ø§Ø¹Ø© $timePart"
            else -> "$formattedDate Ã  $timePart" // FR
        }
    } catch (e: Exception) {
        isoDateTime
    }
}

/**
 * Formate une date relative selon la langue.
 */
@OptIn(ExperimentalTime::class)
private fun formatRelativeTimeForLanguage(isoDateTime: String, language: String): String {
    return try {
        val now = currentInstant()
        val dateTime = Instant.parse(isoDateTime)
        val duration = now - dateTime
        val minutes = duration.inWholeMinutesCompat().toInt()
        
        when {
            minutes < 1 -> when (language) {
                "en" -> "Just now"
                "es" -> "Ahora mismo"
                "de" -> "Gerade"
                "ar" -> "Ø§Ù„Ø¢Ù†"
                else -> "Ã€ l'instant" // FR
            }
            minutes < 60 -> when (language) {
                "en" -> "$minutes min ago"
                "es" -> "Hace $minutes min"
                "de" -> "vor $minutes Min"
                "ar" -> "Ù…Ù†Ø° $minutes Ø¯Ù‚ÙŠÙ‚Ø©"
                else -> "Il y a ${minutes}min" // FR
            }
            minutes < 1440 -> {
                val hours = minutes / 60
                when (language) {
                    "en" -> "$hours h ago"
                    "es" -> "Hace $hours h"
                    "de" -> "vor $hours Std"
                    "ar" -> "Ù…Ù†Ø° $hours Ø³Ø§Ø¹Ø©"
                    else -> "Il y a ${hours}h" // FR
                }
            }
            else -> {
                val days = minutes / 1440
                when (language) {
                    "en" -> "$days d ago"
                    "es" -> "Hace $days d"
                    "de" -> "vor $days T"
                    "ar" -> "Ù…Ù†Ø° $days ÙŠÙˆÙ…"
                    else -> "Il y a ${days}j" // FR
                }
            }
        }
    } catch (e: Exception) {
        isoDateTime.substring(0, 10) // Retourner juste la date
    }
}

/**
 * Formate une devise selon la langue.
 */
private fun formatCurrencyForLanguage(price: Double, language: String): String {
    val rounded = (price * 100).toLong() / 100.0
    val euros = rounded.toInt()
    val cents = ((rounded - euros) * 100).toInt()
    
    val centsStr = cents.toString().padStart(2, '0')
    
    return when (language) {
        "en" -> "â‚¬$euros.$centsStr" // â‚¬25.50
        "es" -> "$euros,$centsStr â‚¬" // 25,50 â‚¬
        "de" -> "$euros,$centsStr â‚¬" // 25,50 â‚¬
        "ar" -> "$euros.$centsStr â‚¬" // 25.50 â‚¬ (RTL)
        else -> "$euros,$centsStr â‚¬" // 25,50 â‚¬ (FR)
    }
}

/**
 * Formate un nombre selon la langue.
 */
private fun formatNumberForLanguage(number: Double, decimals: Int, language: String): String {
    // Utiliser une approche plus simple sans pow
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        else -> pow(10.0, decimals.toDouble())
    }
    
    val rounded = (number * multiplier).toLong() / multiplier
    val integerPart = rounded.toInt()
    val decimalPart = ((rounded - integerPart) * multiplier).toInt()
    
    val decimalStr = decimalPart.toString().padStart(decimals, '0')
    
    // Formatage simple sans sÃ©parateurs de milliers pour l'instant
    return when (language) {
        "en" -> "$integerPart.$decimalStr" // 1234.56
        "es" -> "$integerPart,$decimalStr" // 1234,56
        "de" -> "$integerPart,$decimalStr" // 1234,56
        "ar" -> "$integerPart.$decimalStr" // 1234.56 (RTL)
        else -> "$integerPart,$decimalStr" // 1234,56 (FR)
    }
}

/**
 * Fonction helper pour calculer la puissance (Ã©vite les problÃ¨mes d'import).
 */
private fun pow(base: Double, exponent: Double): Double {
    var result = 1.0
    repeat(exponent.toInt()) {
        result *= base
    }
    return result
}

/**
 * Formate une note (rating) avec 1 dÃ©cimale.
 * 
 * Exemples :
 * - FR: "4,5"
 * - EN: "4.5"
 * - ES: "4,5"
 * - DE: "4,5"
 * - AR: "4.5"
 * 
 * @param rating Note Ã  formater (ex: 4.5)
 * @return Note formatÃ©e avec 1 dÃ©cimale selon la locale
 */
@Composable
fun formatLocalizedRating(rating: Double): String {
    return formatLocalizedNumber(rating, decimals = 1)
}

// ========================================
// VERSIONS NON-COMPOSABLE (pour data classes)
// ========================================

/**
 * Formate une date relative selon une langue spÃ©cifique (non-composable).
 * 
 * UtilisÃ© dans les data classes oÃ¹ @Composable n'est pas disponible.
 * 
 * @param isoDateTime Date-heure au format ISO-8601
 * @param language Code de la langue (dÃ©faut: "fr")
 * @return Date relative formatÃ©e selon la langue
 */
fun formatRelativeTimeForLanguageStatic(isoDateTime: String, language: String = "fr"): String {
    return formatRelativeTimeForLanguage(isoDateTime, language)
}

/**
 * Formate une devise selon une langue spÃ©cifique (non-composable).
 * 
 * @param price Prix en euros
 * @param language Code de la langue (dÃ©faut: "fr")
 * @return Prix formatÃ© selon la langue
 */
fun formatCurrencyForLanguageStatic(price: Double, language: String = "fr"): String {
    return formatCurrencyForLanguage(price, language)
}

