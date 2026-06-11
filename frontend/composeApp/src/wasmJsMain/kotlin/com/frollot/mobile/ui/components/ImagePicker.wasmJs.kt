package com.frollot.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Implémentation WebAssembly JS du sélecteur d'image.
 *
 * Note: Cette implémentation est un stub pour permettre la compilation.
 * Pour une vraie implémentation web, il faudrait utiliser l'API File du navigateur.
 */
class WasmJsImagePickerLauncher(
    private val onImagePicked: (ByteArray?) -> Unit
) : ImagePickerLauncher {

    override fun launch() {
        // TODO: Implémenter avec l'API File du navigateur
        // Pour l'instant, on signale simplement qu'aucune image n'a été sélectionnée
        println("⚠️ ImagePicker non implémenté sur WebAssembly - utiliser l'API File du navigateur")
        onImagePicked(null)
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher {
    return remember { WasmJsImagePickerLauncher(onImagePicked) }
}