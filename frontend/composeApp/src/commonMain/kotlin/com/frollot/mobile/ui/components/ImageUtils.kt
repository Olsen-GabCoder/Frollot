package com.frollot.mobile.ui.components

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Convertit un ByteArray en ImageBitmap.
 * Implémentation spécifique à chaque plateforme.
 */
expect fun ByteArray.toImageBitmap(): ImageBitmap?
