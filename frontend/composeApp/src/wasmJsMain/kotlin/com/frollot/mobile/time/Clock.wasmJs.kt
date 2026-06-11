package com.frollot.mobile.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implémentations WebAssembly JS des fonctions d'horloge système.
 * Utilise Clock.System.now() qui est disponible sur WASM.
 */
actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

actual fun currentInstant(): Instant = Clock.System.now()
