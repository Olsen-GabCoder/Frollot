package com.frollot.mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.frollot.mobile.model.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Phase I.4 - Mode Offline Basique
 * Implémentation JavaScript utilisant navigator.onLine
 */
actual class NetworkMonitor {
    private fun isOnline(): Boolean {
        return if (js("typeof window !== 'undefined' && window.navigator")) {
            js("window.navigator.onLine") as Boolean
        } else {
            true // Par défaut, supposer online si on ne peut pas détecter
        }
    }
    
    actual suspend fun getNetworkState(): NetworkState {
        return NetworkState(isOnline = isOnline())
    }
    
    actual fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        if (js("typeof window !== 'undefined'")) {
            val onlineHandler: () -> Unit = { trySend(NetworkState(isOnline = true)) }
            val offlineHandler: () -> Unit = { trySend(NetworkState(isOnline = false)) }
            
            js("window.addEventListener")("online", onlineHandler)
            js("window.addEventListener")("offline", offlineHandler)
            
            // Envoyer l'état initial
            trySend(NetworkState(isOnline = isOnline()))
            
            awaitClose {
                js("window.removeEventListener")("online", onlineHandler)
                js("window.removeEventListener")("offline", offlineHandler)
            }
        } else {
            // Si on n'est pas dans un environnement avec window, retourner un flow qui émet toujours online
            trySend(NetworkState(isOnline = true))
            awaitClose { }
        }
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

