package com.frollot.mobile.ui.components

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Implémentation Android pour convertir ByteArray en ImageBitmap.
 */
actual fun ByteArray.toImageBitmap(): ImageBitmap? {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        println("❌ Erreur lors de la conversion de l'image: ${e.message}")
        null
    }
}
