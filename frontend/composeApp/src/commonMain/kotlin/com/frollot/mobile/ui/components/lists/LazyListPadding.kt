package com.frollot.mobile.ui.components.lists

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Charte Graphique Frollot — Padding Standardisé pour LazyList
 *
 * Padding standardisé conforme à la charte graphique :
 * - Vertical : 16dp (grille 8dp)
 * - Horizontal : 16dp (grille 8dp)
 */
object LazyListPadding {
    /**
     * Padding vertical standard pour les listes.
     */
    val Vertical = PaddingValues(vertical = 16.dp)

    /**
     * Padding horizontal standard pour les listes.
     */
    val Horizontal = PaddingValues(horizontal = 16.dp)

    /**
     * Padding complet (vertical + horizontal) pour les listes.
     */
    val All = PaddingValues(16.dp)

    /**
     * Padding pour le contenu de liste avec header.
     */
    val WithHeader = PaddingValues(
        top = 8.dp,
        bottom = 16.dp,
        start = 16.dp,
        end = 16.dp
    )

    /**
     * Padding minimal pour les listes compactes.
     */
    val Compact = PaddingValues(8.dp)

    /**
     * Padding pour le bas de liste (pagination, footer).
     */
    val Bottom = PaddingValues(bottom = 16.dp)
}

