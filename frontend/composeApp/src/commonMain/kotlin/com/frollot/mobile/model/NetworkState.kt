package com.frollot.mobile.model

/**
 * Phase I.4 - Mode Offline Basique
 * État de la connectivité réseau
 */
data class NetworkState(
    val isOnline: Boolean,
    val isConnected: Boolean = isOnline // Alias pour cohérence
)

