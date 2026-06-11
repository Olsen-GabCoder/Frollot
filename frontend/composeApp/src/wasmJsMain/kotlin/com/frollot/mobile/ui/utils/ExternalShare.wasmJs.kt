package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable

/**
 * Phase I.2 - Partage Externe
 * Implémentation par défaut pour wasmJs (non supporté pour l'instant)
 */
actual class ExternalShare {
    actual suspend fun share(
        text: String,
        imageUrl: String?,
        onSuccess: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        onError?.invoke(Exception("Partage externe non supporté sur cette plateforme"))
    }
}

/**
 * Factory function pour créer une instance d'ExternalShare sur wasmJs.
 * Phase I.2 - Partage Externe
 */
actual fun createExternalShare(): ExternalShare? {
    return null
}

/**
 * Helper function pour créer ExternalShare depuis un composable wasmJs.
 * Phase I.2 - Partage Externe
 */
@Composable
actual fun rememberExternalShare(): ExternalShare? {
    return null
}

