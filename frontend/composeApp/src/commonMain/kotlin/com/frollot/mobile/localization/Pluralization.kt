package com.frollot.mobile.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Système de pluralisation localisé.
 * 
 * Phase 4 - Fonctionnalité Langue : Pluralisation
 * Conforme à l'ADR-001 - DÉCISION 6 : Système de pluralisation.
 * 
 * Gère les règles de pluralisation spécifiques à chaque langue supportée :
 * - Français : 0 ou >1 = pluriel
 * - Anglais : != 1 = pluriel
 * - Espagnol : != 1 = pluriel
 * - Allemand : != 1 = pluriel
 * - Arabe : règles complexes (0, 1, 2, 3-10, 11+)
 */

/**
 * Récupère la forme appropriée (singulier/pluriel) d'une clé selon le nombre.
 * 
 * Le système utilise deux clés dans Strings.kt :
 * - Clé de base (singulier) : ex. "home.salons_found"
 * - Clé avec suffixe "_plural" : ex. "home.salons_found_plural"
 * 
 * @param singularKey Clé pour la forme singulière
 * @param count Nombre à évaluer
 * @return Clé appropriée (singulier ou pluriel)
 */
@Composable
fun getPluralKey(singularKey: StringKey, count: Int): StringKey {
    val language = LocalLanguage.current
    val pluralKey = "${singularKey.key}_plural"
    
    return remember(count, language, singularKey.key) {
        val shouldUsePlural = when (language) {
            "fr" -> count == 0 || count > 1 // Français : 0 ou >1 = pluriel
            "en" -> count != 1 // Anglais : != 1 = pluriel
            "es" -> count != 1 // Espagnol : != 1 = pluriel
            "de" -> count != 1 // Allemand : != 1 = pluriel
            "ar" -> {
                // Arabe : règles complexes
                when {
                    count == 0 -> true // Zéro = pluriel
                    count == 1 -> false // Un = singulier
                    count == 2 -> true // Deux = pluriel (forme duale)
                    count in 3..10 -> true // 3-10 = pluriel
                    else -> true // 11+ = pluriel
                }
            }
            else -> count != 1 // Par défaut (FR)
        }
        
        if (shouldUsePlural) {
            StringKey(pluralKey)
        } else {
            singularKey
        }
    }
}

/**
 * Récupère une string localisée avec pluralisation automatique.
 * 
 * Cette fonction combine stringResource() et getPluralKey() pour simplifier l'utilisation.
 * 
 * @param singularKey Clé pour la forme singulière
 * @param count Nombre à évaluer pour déterminer la forme
 * @param replaceCount Si true, remplace {count} dans la string par la valeur réelle
 * @return String localisée avec la forme appropriée (singulier/pluriel)
 * 
 * Exemple d'utilisation :
 * ```
 * Text(pluralizedString(Strings.Home.SalonsFound, salons.size, replaceCount = true))
 * // Affiche "1 salon trouvé" ou "5 salons trouvés" selon la langue
 * ```
 */
@Composable
fun pluralizedString(
    singularKey: StringKey,
    count: Int,
    replaceCount: Boolean = true
): String {
    val pluralKey = getPluralKey(singularKey, count)
    val string = stringResource(pluralKey)
    
    return if (replaceCount) {
        string.replace("{count}", count.toString())
    } else {
        string
    }
}

/**
 * Récupère une string localisée avec pluralisation et remplacement de variables.
 * 
 * Version avancée permettant de remplacer plusieurs variables dans la string.
 * 
 * @param singularKey Clé pour la forme singulière
 * @param count Nombre à évaluer pour déterminer la forme
 * @param replacements Map de variables à remplacer (ex: mapOf("salonName" to "Salon ABC"))
 * @return String localisée avec toutes les variables remplacées
 * 
 * Exemple d'utilisation :
 * ```
 * pluralizedStringWithReplacements(
 *     Strings.SalonDetail.ChooseFromServices,
 *     services.size,
 *     mapOf("count" to services.size.toString(), "salonName" to salon.name)
 * )
 * ```
 */
@Composable
fun pluralizedStringWithReplacements(
    singularKey: StringKey,
    count: Int,
    replacements: Map<String, String> = emptyMap()
): String {
    val pluralKey = getPluralKey(singularKey, count)
    var string = stringResource(pluralKey)
    
    // Remplacer {count} par défaut
    string = string.replace("{count}", count.toString())
    
    // Remplacer les autres variables
    replacements.forEach { (key, value) ->
        string = string.replace("{$key}", value)
    }
    
    return string
}

