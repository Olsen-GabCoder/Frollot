package com.frollot.mobile.ui.utils

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Phase I.2 - Partage Externe
 * Implémentation Android utilisant Intent.ACTION_SEND
 */
actual class ExternalShare(private val context: Context) {
    actual suspend fun share(
        text: String,
        imageUrl: String?,
        onSuccess: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        try {
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (imageUrl != null) "image/*" else "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, "Partagé depuis Frollot")
                    
                    // Pour les images, on pourrait ajouter l'URI de l'image ici
                    // Pour l'instant, on partage uniquement le texte avec le lien
                }
                
                val chooser = Intent.createChooser(intent, "Partager via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                
                onSuccess?.invoke()
            }
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }
}

/**
 * Factory function pour créer une instance d'ExternalShare sur Android.
 * Phase I.2 - Partage Externe
 * Note: Cette fonction doit être appelée depuis un composable pour accéder à LocalContext
 */
actual fun createExternalShare(): ExternalShare? {
    // Cette fonction ne peut pas être appelée directement depuis commonMain
    // Elle doit être appelée depuis un composable Android qui a accès à LocalContext
    return null
}

/**
 * Helper function pour créer ExternalShare depuis un composable Android.
 * Phase I.2 - Partage Externe
 */
@Composable
actual fun rememberExternalShare(): ExternalShare? {
    val context = LocalContext.current
    return remember(context) {
        ExternalShare(context)
    }
}


