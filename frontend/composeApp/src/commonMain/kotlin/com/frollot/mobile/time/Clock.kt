package com.frollot.mobile.time

import kotlinx.datetime.Instant

/**
 * Fonctions expect/actual pour l'horloge système multiplatform.
 *
 * Remplace Clock.System.now() qui est JVM-only et ne peut pas être utilisé
 * dans commonMain pour Compose Multiplatform.
 */

/**
 * Retourne le timestamp actuel en millisecondes depuis l'époque Unix.
 * Equivalent à Clock.System.now().toEpochMilliseconds()
 */
expect fun currentTimeMillis(): Long

/**
 * Retourne l'instant actuel.
 * Equivalent à Clock.System.now()
 */
expect fun currentInstant(): Instant
