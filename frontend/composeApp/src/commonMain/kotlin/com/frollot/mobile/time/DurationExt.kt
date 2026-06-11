package com.frollot.mobile.time

/**
 * Extensions multiplatform pour kotlin.time.Duration.
 *
 * Remplace les extensions Kotlin qui peuvent ne pas être résolues
 * correctement lors de la compilation metadata commonMain.
 */

/**
 * Retourne le nombre total de minutes complètes dans cette durée.
 *
 * Remplace `duration.inWholeMinutes` qui peut ne pas être résolu
 * en metadata selon les versions de Kotlin/Kotlinx.
 */
fun kotlin.time.Duration.inWholeMinutesCompat(): Long {
    return this.inWholeSeconds / 60
}

/**
 * Retourne le nombre total d'heures complètes dans cette durée.
 */
fun kotlin.time.Duration.inWholeHoursCompat(): Long {
    return this.inWholeSeconds / 3600
}

/**
 * Retourne le nombre total de jours complets dans cette durée.
 */
fun kotlin.time.Duration.inWholeDaysCompat(): Long {
    return this.inWholeSeconds / 86400
}
