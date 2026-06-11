package com.frollot.mobile.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.frollot.mobile.model.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Phase I.4 - Mode Offline Basique
 * Implémentation Android utilisant ConnectivityManager
 */
actual class NetworkMonitor(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    actual suspend fun getNetworkState(): NetworkState {
        val network = connectivityManager.activeNetwork ?: return NetworkState(isOnline = false)
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkState(isOnline = false)
        
        val isOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                       capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        return NetworkState(isOnline = isOnline)
    }
    
    actual fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val getCurrentState = {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                NetworkState(isOnline = false)
            } else {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities == null) {
                    NetworkState(isOnline = false)
                } else {
                    val isOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    NetworkState(isOnline = isOnline)
                }
            }
        }
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkState(isOnline = true))
            }
            
            override fun onLost(network: Network) {
                trySend(NetworkState(isOnline = false))
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isOnline = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                               networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(NetworkState(isOnline = isOnline))
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Envoyer l'état initial
        trySend(getCurrentState())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

actual fun createNetworkMonitor(): NetworkMonitor? {
    // Cette fonction nécessite un Context, on la gère via rememberNetworkMonitor
    return null
}

@Composable
actual fun rememberNetworkMonitor(): NetworkMonitor? {
    val context = LocalContext.current
    return remember(context) {
        NetworkMonitor(context)
    }
}

