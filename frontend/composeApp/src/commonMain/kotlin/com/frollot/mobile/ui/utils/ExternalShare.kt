package com.frollot.mobile.ui.utils

/**
 * Phase I.2 - Partage Externe
 * Interface multiplateforme pour partager du contenu vers des applications externes
 */
expect class ExternalShare {
    /**
     * Partage du texte et optionnellement une image vers des applications externes.
     * 
     * @param text Le texte à partager
     * @param imageUrl URL de l'image à partager (optionnel)
     * @param onSuccess Callback appelé en cas de succès
     * @param onError Callback appelé en cas d'erreur
     */
    suspend fun share(
        text: String,
        imageUrl: String? = null,
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    )
}

/**
 * Factory function pour créer une instance d'ExternalShare.
 * Phase I.2 - Partage Externe
 */
expect fun createExternalShare(): ExternalShare?

/**
 * Helper function pour créer ExternalShare depuis un composable.
 * Phase I.2 - Partage Externe
 */
@androidx.compose.runtime.Composable
expect fun rememberExternalShare(): ExternalShare?

/**
 * Fonction utilitaire pour générer un deep link vers un post.
 * Phase I.2 - Partage Externe
 */
fun generatePostDeepLink(postId: String, baseUrl: String = "https://app.frollot.com"): String {
    return "$baseUrl/post/$postId"
}

/**
 * Génère le texte de partage pour un post.
 * Phase I.2 - Partage Externe
 */
fun generatePostShareText(
    postContent: String,
    authorName: String,
    postId: String,
    baseUrl: String = "https://app.frollot.com"
): String {
    val deepLink = generatePostDeepLink(postId, baseUrl)
    return "$postContent\n\n— Par $authorName sur Frollot\n$deepLink"
}


