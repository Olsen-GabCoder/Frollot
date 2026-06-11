package com.frollot.mobile.localization.resources

import com.frollot.mobile.localization.StringKey

/**
 * Bundle de strings pour une langue donnée.
 * 
 * Contient toutes les traductions pour une langue spécifique.
 * Les strings sont stockées dans une Map interne pour accès rapide.
 * 
 * Conforme à l'ADR-001 - DÉCISION 9 : Performance et caching.
 */
class StringsBundle(
    private val strings: Map<String, String>
) {
    /**
     * Récupère la string correspondant à la clé, ou la clé elle-même si non trouvée.
     * 
     * En cas de clé manquante, on retourne la clé pour faciliter le débogage
     * et éviter les crashes. La clé sera visible dans l'UI, signalant un problème.
     */
    fun get(key: StringKey): String {
        return strings[key.key] ?: "[${key.key}]"
    }
    
    /**
     * Vérifie si une clé existe dans le bundle.
     */
    fun contains(key: StringKey): Boolean {
        return strings.containsKey(key.key)
    }
    
    /**
     * Retourne le nombre de strings dans le bundle.
     */
    fun size(): Int = strings.size
}

