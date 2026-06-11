package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable
import com.frollot.mobile.model.NetworkState

/**
 * Phase I.4 - Mode Offline Basique
 * Interface pour surveiller l'état de la connectivité réseau
 */
expect class NetworkMonitor {
    /**
     * Récupère l'état actuel de la connectivité.
     */
    suspend fun getNetworkState(): NetworkState
    
    /**
     * Observe les changements de connectivité.
     * Retourne un Flow qui émet les changements d'état.
     */
    fun observeNetworkState(): kotlinx.coroutines.flow.Flow<NetworkState>
}

/**
 * Factory function pour créer une instance de NetworkMonitor.
 */
expect fun createNetworkMonitor(): NetworkMonitor?

/**
 * Helper function pour obtenir NetworkMonitor depuis un composable.
 */
@Composable
expect fun rememberNetworkMonitor(): NetworkMonitor?

