package com.frollot.mobile.ui.components

import androidx.compose.runtime.Composable

/**
 * Interface pour le lanceur de sélection d'image.
 * Permet de déclencher la sélection d'image depuis la plateforme.
 */
interface ImagePickerLauncher {
    /**
     * Lance le sélecteur d'image de la plateforme.
     */
    fun launch()
}

/**
 * Fonction expect pour obtenir un ImagePickerLauncher.
 * 
 * @param onImagePicked Callback appelé avec les bytes de l'image sélectionnée, ou null si annulé
 * @return Un ImagePickerLauncher qui peut être utilisé pour lancer la sélection
 */
@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher
