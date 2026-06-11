package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.frollot.mobile.model.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Phase I.4 - Mode Offline Basique
 * Implémentation par défaut pour wasmJs (non supporté pour l'instant)
 */
actual class NetworkMonitor {
    actual suspend fun getNetworkState(): NetworkState {
        // Par défaut, supposer qu'on est online sur wasmJs
        return NetworkState(isOnline = true)
    }
    
    actual fun observeNetworkState(): Flow<NetworkState> {
        // Retourner un flow qui émet toujours online
        return flowOf(NetworkState(isOnline = true))
    }
}

actual fun createNetworkMonitor(): NetworkMonitor? {
    return NetworkMonitor()
}

@Composable
actual fun rememberNetworkMonitor(): NetworkMonitor? {
    return remember {
        NetworkMonitor()
    }
}

