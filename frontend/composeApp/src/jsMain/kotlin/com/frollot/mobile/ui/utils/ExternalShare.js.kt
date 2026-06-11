package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.browser.window

/**
 * Phase I.2 - Partage Externe
 * Implémentation JavaScript utilisant Web Share API
 */
actual class ExternalShare {
    actual suspend fun share(
        text: String,
        imageUrl: String?,
        onSuccess: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        try {
            withContext(Dispatchers.Default) {
                // Vérifier si Web Share API est disponible
                if (window.navigator.asDynamic().share != null) {
                    val shareData = js("{}")
                    shareData.text = text
                    shareData.title = "Partagé depuis Frollot"
                    
                    // Web Share API ne supporte pas directement les images
                    // On inclut l'URL de l'image dans le texte si disponible
                    val shareText = if (imageUrl != null) {
                        "$text\n\n$imageUrl"
                    } else {
                        text
                    }
                    shareData.text = shareText
                    
                    window.navigator.asDynamic().share(shareData).then {
                        onSuccess?.invoke()
                    }.catch { error ->
                        onError?.invoke(Exception(error.toString()))
                    }
                } else {
                    // Fallback : copier dans le presse-papiers
                    copyToClipboard(text)
                    onSuccess?.invoke()
                }
            }
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }
    
    private fun copyToClipboard(text: String) {
        // Implémentation basique de copie dans le presse-papiers pour le web
        // Cette fonctionnalité nécessiterait une approche plus complexe en JavaScript pur
        // Pour l'instant, on laisse vide car le Web Share API devrait être disponible
    }
}

/**
 * Factory function pour créer une instance d'ExternalShare sur JavaScript.
 * Phase I.2 - Partage Externe
 */
actual fun createExternalShare(): ExternalShare? {
    return ExternalShare()
}

/**
 * Helper function pour créer ExternalShare depuis un composable JavaScript.
 * Phase I.2 - Partage Externe
 */
@Composable
actual fun rememberExternalShare(): ExternalShare? {
    return remember { ExternalShare() }
}

