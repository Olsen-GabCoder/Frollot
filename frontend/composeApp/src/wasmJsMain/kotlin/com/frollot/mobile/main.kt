package com.frollot.mobile

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        CanvasBasedWindow(canvasElementId = "composeApp") {
            App()
        }
    } catch (e: Exception) {
        println("FATAL ERROR in main: ${e.message}")
        e.printStackTrace()
    }
}
