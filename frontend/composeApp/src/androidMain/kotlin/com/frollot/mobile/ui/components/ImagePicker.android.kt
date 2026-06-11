package com.frollot.mobile.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.scale
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(DelicateCoroutinesApi::class)
@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val maxDimension = 1920
                    val resizedBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                        val ratio = minOf(maxDimension.toFloat() / originalBitmap.width, maxDimension.toFloat() / originalBitmap.height)
                        originalBitmap.scale(
                            (originalBitmap.width * ratio).toInt(),
                            (originalBitmap.height * ratio).toInt()
                        )
                    } else originalBitmap

                    val outputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    val bytes = outputStream.toByteArray()

                    if (resizedBitmap != originalBitmap) resizedBitmap.recycle()
                    originalBitmap.recycle()

                    withContext(Dispatchers.Main) { onImagePicked(bytes) }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) { onImagePicked(null) }
                }
            }
        } else onImagePicked(null)
    }

    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

