package com.frollot.mobile.auth

import androidx.compose.runtime.Composable

/**
 * Factory pour créer une instance de AuthDataStore selon la plateforme.
 * 
 * Cette interface abstrait le stockage sécurisé des tokens d'authentification,
 * permettant différentes implémentations selon la plateforme :
 * - Android : EncryptedDataStore (chiffrement automatique)
 * - Web : localStorage (non chiffré, acceptable pour le web)
 * 
 * Les tokens stockés :
 * - accessToken : Token JWT d'accès (15 min)
 * - refreshToken : Token UUID de refresh (7 jours)
 * 
 * Sécurité :
 * - Android : Les tokens sont automatiquement chiffrés par EncryptedDataStore
 * - Web : Les tokens sont stockés en clair dans localStorage (limitation du web)
 * 
 * NOTE : Sur Android, cette fonction nécessite un Context et doit être appelée
 * depuis un composable pour accéder à LocalContext.
 */
@Composable
expect fun createAuthDataStore(): AuthDataStore

/**
 * Interface pour la persistance sécurisée des tokens d'authentification.
 * 
 * Cette interface abstrait le stockage des tokens d'authentification,
 * permettant différentes implémentations selon la plateforme.
 * 
 * Les tokens sont stockés de manière persistante pour permettre :
 * - La restauration de session au redémarrage de l'app
 * - Le refresh automatique des tokens
 * - La persistance entre les sessions
 */
interface AuthDataStore {
    /**
     * Récupère le token d'accès stocké.
     * 
     * @return Le token JWT d'accès, ou null si aucun token n'est stocké
     */
    suspend fun getAccessToken(): String?
    
    /**
     * Récupère le refresh token stocké.
     * 
     * @return Le refresh token UUID, ou null si aucun token n'est stocké
     */
    suspend fun getRefreshToken(): String?
    
    /**
     * Sauvegarde les tokens d'authentification.
     * 
     * @param accessToken Le token JWT d'accès
     * @param refreshToken Le refresh token UUID
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    
    /**
     * Supprime tous les tokens stockés (logout).
     * 
     * Cette méthode doit être appelée lors de la déconnexion pour
     * s'assurer que les tokens ne persistent pas après le logout.
     */
    suspend fun clearTokens()
    
    /**
     * Vérifie si des tokens sont stockés.
     * 
     * @return true si au moins un token est stocké, false sinon
     */
    suspend fun hasTokens(): Boolean
}

