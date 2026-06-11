package com.frollot.mobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Phase I.1 - Implémentation manuelle du pull-to-refresh
 * Compatible avec Compose Multiplatform (toutes plateformes)
 */
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    val pullProgress = (dragOffset / 100f).coerceIn(0f, 1f)
    val indicatorAlpha = animateFloatAsState(
        targetValue = if (isRefreshing || dragOffset > 0) 1f else 0f,
        animationSpec = tween(200)
    )
    
    Box(modifier = modifier) {
        // Contenu principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = dragOffset.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            // Début du drag
                        },
                        onDragEnd = {
                            if (dragOffset > 80f && !isRefreshing) {
                                scope.launch {
                                    onRefresh()
                                }
                            }
                            // Animation de retour
                            scope.launch {
                                dragOffset = 0f
                            }
                        },
                        onVerticalDrag = { change: PointerInputChange, dragAmount: Float ->
                            if (!isRefreshing) {
                                val newOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                                // Limiter le pull à 150dp
                                dragOffset = newOffset.coerceAtMost(150f)
                            }
                        }
                    )
                }
        ) {
            content()
        }
        
        // Indicateur de pull-to-refresh
        if (isRefreshing || dragOffset > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .alpha(indicatorAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        progress = pullProgress,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

