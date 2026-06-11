package com.frollot.mobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.frollot.mobile.model.PostMediaResponse
import com.frollot.mobile.localization.*

/**
 * Viewer d'images en plein écran avec zoom et swipe.
 * 
 * Fonctionnalités :
 * - Affichage en plein écran avec fond sombre
 * - Zoom pinch-to-zoom
 * - Swipe entre les images
 * - Indicateurs de page
 * - Bouton de fermeture
 * - Design conforme à la charte graphique
 */
@Composable
fun FullScreenImageViewer(
    images: List<PostMediaResponse>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, images.size - 1),
        pageCount = { images.size }
    )

    // État pour le zoom de chaque image
    val zoomStates = remember { mutableStateMapOf<Int, ZoomState>() }
    
    // Fonction pour obtenir ou créer le zoom state pour une page
    fun getZoomState(page: Int): ZoomState {
        return zoomStates.getOrPut(page) {
            ZoomState()
        }
    }

    // Animation d'apparition
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Viewer en plein écran, au-dessus de tout (comme Instagram/Facebook)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .zIndex(10000f) // zIndex très élevé pour être au-dessus de tout
    ) {
        // Viewer d'images avec pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val zoomState = getZoomState(page)
            
            ZoomableImage(
                imageUrl = images[page].mediaUrl,
                contentDescription = stringResource(Strings.Components.FullScreenImageViewer.ImageContentDescription).replace("{number}", (page + 1).toString()),
                zoomState = zoomState,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Indicateurs de page (seulement si plus d'une image)
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }
        }

        // Bouton de fermeture
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Strings.Components.FullScreenImageViewer.Close),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // Compteur d'images (ex: "1 / 5")
        if (images.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * État de zoom pour une image
 */
private data class ZoomState(
    var scale: Float = 1f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)

/**
 * Image avec support du zoom pinch-to-zoom
 */
@Composable
private fun ZoomableImage(
    imageUrl: String,
    contentDescription: String?,
    zoomState: ZoomState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offsetX
                    translationY = zoomState.offsetY
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Zoom
                        zoomState.scale = (zoomState.scale * zoom).coerceIn(1f, 5f)
                        
                        // Pan (déplacement)
                        zoomState.offsetX += pan.x
                        zoomState.offsetY += pan.y
                        
                        // Limiter le déplacement selon le zoom
                        val maxOffset = size.width * (zoomState.scale - 1f) / 2f
                        zoomState.offsetX = zoomState.offsetX.coerceIn(-maxOffset, maxOffset)
                        zoomState.offsetY = zoomState.offsetY.coerceIn(-maxOffset, maxOffset)
                    }
                },
            contentScale = ContentScale.Fit
        )
    }
}

