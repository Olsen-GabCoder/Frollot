package com.frollot.mobile.ui.components

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Implémentation WebAssembly JS de la conversion ByteArray -> ImageBitmap.
 *
 * Note: Cette implémentation est un stub pour permettre la compilation.
 * L'API ImageBitmap de Compose n'est pas directement supportée sur WASM.
 */
actual fun ByteArray.toImageBitmap(): ImageBitmap? {
    // TODO: Implémenter avec l'API Canvas du navigateur si nécessaire
    println("⚠️ toImageBitmap non implémenté sur WebAssembly")
    return null
}