package com.frollot.mobile.localization

import kotlin.jvm.JvmInline

/**
 * Clé type-safe pour accéder aux strings localisées.
 * 
 * Cette classe encapsule une clé de string sous forme de valeur, garantissant
 * la type-safety et évitant les erreurs de typo dans les clés.
 * 
 * Conforme à l'ADR-001 - DÉCISION 8 : Structure hiérarchique type-safe.
 * 
 * @property key La clé unique identifiant la string (ex: "login.title")
 */
@JvmInline
value class StringKey(val key: String) {
    override fun toString(): String = key
}

