package com.frollot.mobile.network

import com.frollot.mobile.auth.AuthDataStore
import com.frollot.mobile.config.AppConfig
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.client.request.forms.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Client HTTP pour communiquer avec le backend Frollot.
 *
 * Gère toutes les requêtes API pour :
 * - Authentification (login, register) avec JWT
 * - Gestion des utilisateurs
 * - Gestion des salons
 * - Gestion des services (prestations)
 * - Gestion du staff (équipe)
 * - Gestion des réservations (bookings)
 * - Gestion de la file d'attente (queue)
 * - Réseau social (posts, commentaires, likes)
 * 
 * Inclut un système de refresh automatique des tokens JWT.
 */
class FrollotApi(
    private val authDataStore: AuthDataStore? = null
) {

    companion object {
        private const val TAG = "FrollotApi"
        
        private var globalAuthToken: String? = null
        private var globalRefreshToken: String? = null
        
        // Mutex pour éviter les refresh simultanés
        private val refreshMutex = Mutex()
        private var isRefreshing = false
        
        /**
         * Crée un HttpClient temporaire sans intercepteurs pour les appels de refresh.
         * Évite la récursion infinie lors du refresh automatique.
         */
        private fun createTempHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = true
                        encodeDefaults = true
                    })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                    connectTimeoutMillis = 10000
                }
            }
        }
    }

    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 60000
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
            })
        }

        // Injection automatique du token Bearer
        defaultRequest {
            val token = globalAuthToken
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }

    }

    private val baseUrl = AppConfig.baseUrl
    
    // ========================================
    // 🔄 REFRESH AUTOMATIQUE DES TOKENS
    // ========================================
    
    /**
     * Exécute une requête avec gestion automatique du refresh token.
     * 
     * Si la requête échoue avec 401 (Unauthorized) :
     * 1. Tente de rafraîchir le token avec le refresh token
     * 2. Relance la requête originale avec le nouveau token
     * 3. Si le refresh échoue, lève une AuthenticationException
     * 
     * @param block Lambda contenant la requête à exécuter
     * @return Le résultat de la requête
     * @throws AuthenticationException si l'authentification échoue définitivement
     */
    private suspend inline fun <reified T> executeWithAutoRefresh(
        crossinline block: suspend () -> HttpResponse
    ): T {
        return try {
            val response = block()
            
            if (response.status == HttpStatusCode.Unauthorized) {
                FrollotLogger.debug(TAG, "401 reçu, tentative de refresh...")
                handleUnauthorizedAndRetry(block)
            } else {
                response.body()
            }
        } catch (e: Exception) {
            // Vérifier si c'est une erreur 401 encapsulée
            if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                FrollotLogger.debug(TAG, "Exception 401, tentative de refresh...")
                handleUnauthorizedAndRetry(block)
            } else {
                throw e
            }
        }
    }
    
    /**
     * Gère une erreur 401 en rafraîchissant le token et relançant la requête.
     */
    private suspend inline fun <reified T> handleUnauthorizedAndRetry(
        crossinline block: suspend () -> HttpResponse
    ): T {
        // Utiliser un mutex pour éviter les refresh simultanés
        refreshMutex.withLock {
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    val refreshed = tryRefreshToken()
                    if (!refreshed) {
                        throw AuthenticationException("Session expirée, veuillez vous reconnecter")
                    }
                } finally {
                    isRefreshing = false
                }
            }
        }
        
        // Relancer la requête avec le nouveau token
        FrollotLogger.debug(TAG, "Retry de la requête avec nouveau token...")
        val retryResponse = block()
        
        if (retryResponse.status == HttpStatusCode.Unauthorized) {
            throw AuthenticationException("Session expirée, veuillez vous reconnecter")
        }
        
        return retryResponse.body()
    }
    
    /**
     * Tente de rafraîchir le token d'accès.
     * 
     * @return true si le refresh a réussi, false sinon
     */
    private suspend fun tryRefreshToken(): Boolean {
        val refreshToken = globalRefreshToken
        
        if (refreshToken.isNullOrBlank()) {
            FrollotLogger.warning(TAG, "Pas de refresh token disponible")
            return false
        }
        
        return try {
            FrollotLogger.debug(TAG, "Tentative de refresh du token...")
            
            val tempClient = createTempHttpClient()
            val response = tempClient.post("$baseUrl/users/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }
            
            if (response.status.value in 200..299) {
                val authResponse = response.body<AuthResponse>()
                setTokens(authResponse.accessToken, authResponse.refreshToken)
                FrollotLogger.success(TAG, "Token rafraîchi avec succès")
                true
            } else {
                FrollotLogger.warning(TAG, "Refresh échoué: ${response.status}")
                // Nettoyer les tokens invalides
                clearAuthToken()
                false
            }
        } catch (e: Exception) {
            FrollotLogger.error(TAG, "Erreur lors du refresh", e)
            clearAuthToken()
            false
        }
    }

    // ========================================
    // 🔒 GESTION DU TOKEN JWT
    // ========================================

    /**
     * Initialise les tokens depuis le stockage persistant.
     * 
     * Cette méthode doit être appelée au démarrage de l'application
     * pour restaurer la session de l'utilisateur.
     * 
     * @return true si des tokens ont été restaurés, false sinon
     */
    suspend fun initializeTokens(): Boolean {
        if (authDataStore == null) {
            FrollotLogger.debug(TAG, "AuthDataStore non disponible, fallback mémoire")
            return false
        }
        
        val accessToken = authDataStore.getAccessToken()
        val refreshToken = authDataStore.getRefreshToken()
        
        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            globalAuthToken = accessToken
            globalRefreshToken = refreshToken
            FrollotLogger.success(TAG, "Tokens restaurés depuis le stockage persistant")
            return true
        }
        
        return false
    }

    /**
     * Définit les tokens JWT à utiliser pour toutes les requêtes authentifiées.
     * 
     * Stocke les tokens à la fois en mémoire (pour l'accès rapide) et
     * dans le stockage persistant (pour la restauration au redémarrage).
     *
     * @param accessToken Le token JWT d'accès reçu du backend
     * @param refreshToken Le refresh token UUID reçu du backend
     */
    suspend fun setTokens(accessToken: String, refreshToken: String) {
        globalAuthToken = accessToken
        globalRefreshToken = refreshToken
        
        authDataStore?.saveTokens(accessToken, refreshToken)
        
        FrollotLogger.secureToken(TAG, "AccessToken", accessToken)
        FrollotLogger.secureToken(TAG, "RefreshToken", refreshToken)
    }

    /**
     * Définit le token JWT à utiliser pour toutes les requêtes authentifiées.
     * 
     * Méthode de compatibilité pour l'ancien code qui ne passe que l'access token.
     * Le refresh token doit être défini séparément.
     *
     * @param token Le token JWT reçu du backend
     * @deprecated Utiliser setTokens(accessToken, refreshToken) à la place
     */
    @Deprecated("Utiliser setTokens(accessToken, refreshToken) à la place")
    fun setAuthToken(token: String) {
        globalAuthToken = token
        FrollotLogger.secureToken(TAG, "AccessToken (legacy)", token)
    }

    /**
     * Supprime les tokens JWT (logout).
     * 
     * Supprime les tokens à la fois de la mémoire et du stockage persistant.
     * Toutes les requêtes suivantes seront non authentifiées.
     */
    suspend fun clearAuthToken() {
        globalAuthToken = null
        globalRefreshToken = null
        
        authDataStore?.clearTokens()
        
        FrollotLogger.info(TAG, "Tokens supprimés (mémoire + persistance)")
    }

    /**
     * Récupère le token JWT d'accès actuel.
     *
     * @return Le token JWT d'accès ou null si non authentifié
     */
    fun getAuthToken(): String? = globalAuthToken

    /**
     * Récupère le refresh token actuel.
     *
     * @return Le refresh token ou null si non authentifié
     */
    fun getRefreshToken(): String? = globalRefreshToken

    /**
     * Vérifie si l'utilisateur est authentifié.
     *
     * @return true si un token JWT est présent, false sinon
     */
    fun isAuthenticated(): Boolean = globalAuthToken != null

    // ========================================
    // ENDPOINTS UTILISATEURS
    // ========================================

    /**
     * Récupère l'utilisateur actuellement authentifié.
     * 
     * Utilise l'endpoint /api/users/me pour récupérer les informations
     * de l'utilisateur depuis le token JWT.
     * 
     * @return L'utilisateur authentifié
     * @throws Exception si l'utilisateur n'est pas authentifié ou si le token est invalide
     */
    suspend fun getCurrentUser(): User {
        return httpClient.get("$baseUrl/users/me").body()
    }

    /**
     * Récupère la liste de tous les utilisateurs.
     */
    suspend fun getAllUsers(): List<User> {
        return httpClient.get("$baseUrl/users").body()
    }

    /**
     * Recherche des utilisateurs pour les mentions @.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun searchUsers(query: String): List<User> {
        if (query.length < 2) return emptyList()
        return httpClient.get("$baseUrl/users/search") {
            url {
                parameters.append("query", query)
            }
        }.body()
    }

    /**
     * Inscrit un nouvel utilisateur.
     * Le token JWT est automatiquement stocké après inscription réussie.
     *
     * @param request Données d'inscription
     * @return Réponse contenant l'utilisateur créé + les tokens JWT
     */
    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = httpClient.post("$baseUrl/users/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponse>()

        setTokens(response.accessToken, response.refreshToken)
        FrollotLogger.success(TAG, "Inscription réussie: ${response.firstName} ${response.lastName}")

        return response
    }

    /**
     * Pré-inscrit un nouvel utilisateur (nouveau système sécurisé).
     *
     * Contrairement à register(), cette méthode ne stocke PAS automatiquement les tokens
     * car elle retourne HTTP 202 (pré-inscription) avec un message de vérification email.
     * L'utilisateur doit compléter l'inscription via completeRegistration().
     *
     * @param request Données d'inscription
     * @return Réponse de pré-inscription (sans tokens JWT)
     */
    suspend fun preRegister(request: RegisterRequest): AuthResponse {
        return httpClient.post("$baseUrl/users/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponse>()
        // ⚠️ INTENTIONNELLEMENT PAS d'appel à setTokens() car c'est une pré-inscription
    }

    /**
     * Finalise l'inscription après vérification email (nouveau système sécurisé).
     *
     * Cette méthode complète le processus d'inscription commencé par preRegister().
     * Elle stocke automatiquement les tokens JWT car l'utilisateur est maintenant vérifié.
     *
     * @param token Token de vérification reçu par email
     * @return Réponse complète avec tokens JWT (utilisateur prêt à être connecté)
     */
    suspend fun completeRegistration(token: String): AuthResponse {
        val response = httpClient.post("$baseUrl/users/complete-registration") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("token=$token")
        }.body<AuthResponse>()

        // ✅ Stockage automatique des tokens car l'inscription est maintenant complète
        setTokens(response.accessToken, response.refreshToken)
        FrollotLogger.success(TAG, "Inscription finalisée: ${response.firstName} ${response.lastName}")

        return response
    }

    /**
     * Connecte un utilisateur existant.
     * Le token JWT est automatiquement stocké après connexion réussie.
     *
     * @param request Identifiants de connexion (email + password)
     * @return Réponse contenant l'utilisateur connecté + les tokens JWT
     */
    suspend fun login(request: LoginRequest): AuthResponse {
        val response = httpClient.post("$baseUrl/users/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponse>()

        setTokens(response.accessToken, response.refreshToken)
        FrollotLogger.success(TAG, "Connexion réussie: ${response.firstName} ${response.lastName}")

        return response
    }

    /**
     * Vérifie un token de vérification d'email.
     * 
     * @param token Le token de vérification reçu par email
     * @return Map avec success et message
     */
    suspend fun verifyEmail(token: String): Map<String, String> {
        return httpClient.post("$baseUrl/users/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("token" to token))
        }.body()
    }

    /**
     * Renvoie un email de vérification à l'utilisateur authentifié.
     *
     * @return Map avec success et message
     */
    suspend fun resendVerificationEmail(): Map<String, String> {
        return httpClient.post("$baseUrl/users/me/resend-verification") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    /**
     * Demande une réinitialisation de mot de passe.
     *
     * Envoie un email avec un lien de réinitialisation à l'adresse fournie.
     *
     * @param request Données de la demande (email)
     * @return Réponse indiquant si l'email a été envoyé
     */
    suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
        return httpClient.post("$baseUrl/users/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ForgotPasswordResponse>()
    }

    /**
     * Réinitialise le mot de passe de l'utilisateur.
     *
     * Utilise le token reçu par email pour valider et changer le mot de passe.
     *
     * @param request Données de réinitialisation (token + nouveau mot de passe)
     * @return Réponse indiquant si la réinitialisation a réussi
     */
    suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        return httpClient.post("$baseUrl/users/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ResetPasswordResponse>()
    }

    /**
     * Rafraîchit l'access token en utilisant le refresh token.
     * 
     * Cette méthode peut être appelée manuellement si nécessaire.
     * Le refresh est également géré automatiquement via executeWithAutoRefresh.
     * 
     * @return AuthResponse avec les nouveaux tokens (rotation activée)
     * @throws AuthenticationException si le refresh token est invalide ou expiré
     */
    suspend fun refreshToken(): AuthResponse {
        val refreshToken = getRefreshToken()
            ?: throw AuthenticationException("Aucun refresh token disponible")
        
        val tempClient = createTempHttpClient()
        val response = tempClient.post("$baseUrl/users/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken))
        }.body<AuthResponse>()
        
        setTokens(response.accessToken, response.refreshToken)
        FrollotLogger.success(TAG, "Token rafraîchi avec succès")
        
        return response
    }

    /**
     * Met à jour l'avatar d'un utilisateur.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param userId ID de l'utilisateur
     * @param avatarUrl URL de l'avatar uploadé
     * @return Utilisateur mis à jour
     */
    suspend fun updateUserAvatar(userId: String, avatarUrl: String): User {
        return httpClient.patch("$baseUrl/users/$userId/avatar") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("avatarUrl" to avatarUrl))
        }.body()
    }

    /**
     * Récupère la langue préférée de l'utilisateur authentifié.
     * Phase 3 - Fonctionnalité Langue
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @return Code de la langue (ex: "fr", "en", "es", "de", "ar")
     */
    suspend fun getCurrentUserLanguage(): String {
        val response = httpClient.get("$baseUrl/users/me/language").body<Map<String, String>>()
        return response["language"] ?: "fr"
    }

    /**
     * Met à jour la langue préférée de l'utilisateur authentifié.
     * Phase 3 - Fonctionnalité Langue
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param languageCode Code de la langue (ex: "fr", "en", "es", "de", "ar")
     * @return Code de la langue mise à jour
     */
    suspend fun updateCurrentUserLanguage(languageCode: String): String {
        val response = httpClient.put("$baseUrl/users/me/language") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("language" to languageCode))
        }.body<Map<String, String>>()
        return response["language"] ?: languageCode
    }

    // ========================================
    // ENDPOINTS SÉCURITÉ
    // ========================================

    /**
     * Change le mot de passe de l'utilisateur authentifié.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun changePassword(request: ChangePasswordRequest): ChangePasswordResponse {
        return httpClient.put("$baseUrl/users/me/password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère la liste des sessions actives de l'utilisateur.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * Envoie le refresh token actuel pour identifier la session courante.
     */
    suspend fun getActiveSessions(): SessionsListResponse {
        return httpClient.get("$baseUrl/users/me/sessions") {
            // Envoyer le refresh token pour identifier la session courante
            globalRefreshToken?.let { 
                header("X-Refresh-Token", it)
            }
        }.body()
    }

    /**
     * Révoque une session spécifique.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * @param sessionId ID de la session à révoquer
     */
    suspend fun revokeSession(sessionId: Long): RevokeSessionResponse {
        return httpClient.delete("$baseUrl/users/me/sessions/$sessionId").body()
    }

    /**
     * Révoque toutes les autres sessions (logout all devices except current).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * Envoie le refresh token actuel pour préserver la session courante.
     */
    suspend fun revokeAllOtherSessions(): RevokeSessionResponse {
        return httpClient.delete("$baseUrl/users/me/sessions") {
            // Envoyer le refresh token pour préserver la session courante
            globalRefreshToken?.let { 
                header("X-Refresh-Token", it)
            }
        }.body()
    }

    /**
     * Change l'email de l'utilisateur authentifié.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun changeEmail(request: ChangeEmailRequest): ChangeEmailResponse {
        return httpClient.put("$baseUrl/users/me/email") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Change le téléphone de l'utilisateur authentifié.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun changePhone(request: ChangePhoneRequest): ChangePhoneResponse {
        return httpClient.put("$baseUrl/users/me/phone") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Supprime définitivement le compte de l'utilisateur authentifié.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * ⚠️ OPÉRATION IRRÉVERSIBLE
     */
    suspend fun deleteAccount(request: DeleteAccountRequest): DeleteAccountResponse {
        return httpClient.delete("$baseUrl/users/me") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ========================================
    // ENDPOINTS SALONS
    // ========================================

    /**
     * Crée un nouveau salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createSalon(request: CreateSalonRequest): Salon {
        return httpClient.post("$baseUrl/salons") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère la liste des salons avec filtres optionnels.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     *
     * @param query    Recherche texte (nom / description)
     * @param city     Ville ou morceau d'adresse
     * @param category Catégorie de service (enum ServiceCategory)
     */
    suspend fun getSalons(
        query: String? = null,
        city: String? = null,
        category: ServiceCategory? = null
    ): List<Salon> {
        // Le backend retourne un PageResponse, on extrait le contenu
        val pageResponse = httpClient.get("$baseUrl/salons") {
            val normalizedQuery = query?.takeIf { it.isNotBlank() }
            val normalizedCity = city?.takeIf { it.isNotBlank() }

            normalizedQuery?.let { parameter("q", it) }
            normalizedCity?.let { parameter("city", it) }
            category?.let { parameter("category", it.name) }
        }.body<PageResponse<Salon>>()
        
        return pageResponse.content
    }

    /**
     * Récupère la liste de tous les salons (compatibilité ascendante).
     * Équivalent à getSalons() sans filtres.
     */
    suspend fun getAllSalons(): List<Salon> {
        return getSalons()
    }

    /**
     * Récupère un salon spécifique par son ID.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     *
     * AVEC FALLBACK INTELLIGENT :
     * - Essaie d'abord GET /salons/{id}
     * - Si échec, utilise GET /salons et filtre côté client
     *
     * @param salonId Identifiant du salon
     * @return Le salon correspondant
     * @throws Exception si le salon n'existe pas
     */
    suspend fun getSalonById(salonId: String): Salon {
        return try {
            FrollotLogger.debug("API", "🔍 Tentative GET /salons/$salonId")
            httpClient.get("$baseUrl/salons/$salonId").body<Salon>()
        } catch (e: Exception) {
            FrollotLogger.warning("API", "⚠️ Endpoint direct non disponible, fallback sur getAllSalons")
            val allSalons = getAllSalons()
            allSalons.firstOrNull { it.id == salonId }
                ?: throw Exception("Salon avec l'ID $salonId introuvable parmi ${allSalons.size} salons")
        }
    }

    /**
     * Récupère tous les salons d'un propriétaire.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     *
     * @param ownerId Identifiant du propriétaire
     * @return Liste des salons appartenant au propriétaire
     */
    suspend fun getSalonsByOwner(ownerId: String): List<Salon> {
        return try {
            httpClient.get("$baseUrl/salons/owner/$ownerId").body<List<Salon>>()
        } catch (e: Exception) {
            FrollotLogger.warning("API", "⚠️ Erreur lors de la récupération des salons du propriétaire: ${e.message}")
            emptyList()
        }
    }

    /**
     * Met à jour la photo de couverture d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Seul le propriétaire du salon peut effectuer cette action.
     *
     * @param salonId Identifiant du salon
     * @param coverPhotoUrl URL de la nouvelle photo de couverture
     * @return Le salon mis à jour
     * @throws Exception en cas d'erreur
     */
    suspend fun updateSalonCoverPhoto(salonId: String, coverPhotoUrl: String): Salon {
        return httpClient.put("$baseUrl/salons/$salonId/cover-photo") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("coverPhotoUrl" to coverPhotoUrl))
        }.body<Salon>()
    }

    /**
     * Met à jour la photo de couverture d'un utilisateur.
     * Seul le propriétaire du compte peut effectuer cette action.
     *
     * @param userId Identifiant de l'utilisateur
     * @param coverImageUrl URL de la nouvelle photo de couverture
     * @return Map contenant le message de succès et l'URL de la photo de couverture
     * @throws Exception en cas d'erreur
     */
    suspend fun updateUserCoverImage(userId: String, coverImageUrl: String): Map<String, String> {
        return executeWithAutoRefresh {
            httpClient.put("$baseUrl/social/users/$userId/cover-image") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("coverImageUrl" to coverImageUrl))
            }
        }
    }

    // ========================================
    // ENDPOINTS SERVICES (PRESTATIONS)
    // ========================================

    /**
     * Récupère toutes les prestations d'un salon.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSalonServices(salonId: String): List<SalonService> {
        return httpClient.get("$baseUrl/salons/$salonId/services").body()
    }

    /**
     * Récupère un service spécifique par son ID.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getServiceById(salonId: String, serviceId: String): SalonService {
        return httpClient.get("$baseUrl/salons/$salonId/services/$serviceId").body()
    }

    /**
     * Crée un nouveau service pour un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * VERSION CORRIGÉE avec gestion d'erreurs complète.
     *
     * @param request Requête de création contenant toutes les données du service
     * @return SalonService créé avec tous les champs remplis par le backend
     * @throws Exception en cas d'erreur serveur ou de validation
     */
    suspend fun createSalonService(request: CreateServiceRequest): SalonService {
        FrollotLogger.debug("API", "📤 API: Envoi CreateServiceRequest")
        FrollotLogger.debug("API", "   → salonId: ${request.salonId}")
        FrollotLogger.debug("API", "   → name: ${request.name}")
        FrollotLogger.debug("API", "   → description: ${request.description}")
        FrollotLogger.debug("API", "   → durationMinutes: ${request.durationMinutes}")
        FrollotLogger.debug("API", "   → price: ${request.price}")
        FrollotLogger.debug("API", "   → category: ${request.category}")

        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/salons/${request.salonId}/services") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val statusCode = response.status.value
            FrollotLogger.debug("API", "📥 API: Réponse reçue - Status $statusCode")

            when (statusCode) {
                in 200..299 -> {
                    // Succès - désérialiser la réponse
                    val service = response.body<SalonService>()
                    FrollotLogger.success("API", "✅ API: Service créé avec succès")
                    FrollotLogger.debug("API", "   → id: ${service.id}")
                    FrollotLogger.debug("API", "   → name: ${service.name}")
                    FrollotLogger.debug("API", "   → salonId: ${service.salonId}")
                    service
                }
                400 -> {
                    // Bad Request - Erreur de validation
                    val errorBody = response.body<String>()
                    FrollotLogger.error("API", "❌ API: Erreur 400 - $errorBody")
                    throw Exception("Données invalides: $errorBody")
                }
                401 -> {
                    // Unauthorized - Token JWT manquant ou invalide
                    FrollotLogger.error("API", "❌ API: Erreur 401 - Non authentifié")
                    throw Exception("Authentification requise. Veuillez vous reconnecter.")
                }
                403 -> {
                    // Forbidden - Pas les droits
                    FrollotLogger.error("API", "❌ API: Erreur 403 - Accès refusé")
                    throw Exception("Vous n'avez pas les droits pour cette action")
                }
                404 -> {
                    // Not Found - Salon inexistant
                    FrollotLogger.error("API", "❌ API: Erreur 404 - Salon non trouvé")
                    throw Exception("Le salon avec l'ID ${request.salonId} n'existe pas")
                }
                409 -> {
                    // Conflict - Service déjà existant
                    FrollotLogger.error("API", "❌ API: Erreur 409 - Service déjà existant")
                    throw Exception("Un service avec ce nom existe déjà dans ce salon")
                }
                500 -> {
                    // Internal Server Error
                    val errorBody = response.body<String>()
                    FrollotLogger.error("API", "❌ API: Erreur 500 - $errorBody")
                    throw Exception("Erreur serveur. Veuillez réessayer.")
                }
                else -> {
                    // Autre erreur
                    val errorBody = response.body<String>()
                    FrollotLogger.error("API", "❌ API: Erreur $statusCode - $errorBody")
                    throw Exception("Erreur serveur (code $statusCode)")
                }
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            // Erreur de désérialisation
            FrollotLogger.error("API", "❌ API: Erreur de désérialisation - ${e.message}")
            throw Exception("Format de réponse invalide du serveur")
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            // Erreur client (4xx)
            FrollotLogger.error("API", "❌ API: ClientRequestException - ${e.message}")
            throw Exception("Erreur de requête: ${e.message}")
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            // Erreur serveur (5xx)
            FrollotLogger.error("API", "❌ API: ServerResponseException - ${e.message}")
            throw Exception("Erreur serveur: ${e.message}")
        } catch (e: Exception) {
            // Toute autre erreur
            FrollotLogger.error("API", "❌ API: Exception - ${e.message}")
            throw Exception("Erreur de connexion: ${e.message ?: "Erreur inconnue"}")
        }
    }

    /**
     * Met à jour un service existant.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param salonId ID du salon
     * @param serviceId ID du service à modifier
     * @param request Requête de mise à jour (champs optionnels)
     * @return Service mis à jour
     */
    suspend fun updateSalonService(
        salonId: String,
        serviceId: String,
        request: UpdateServiceRequest
    ): SalonService {
        FrollotLogger.debug("API", "📤 API: Mise à jour service $serviceId")
        return httpClient.put("$baseUrl/salons/$salonId/services/$serviceId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Supprime un service.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param salonId ID du salon
     * @param serviceId ID du service à supprimer
     */
    suspend fun deleteSalonService(salonId: String, serviceId: String) {
        FrollotLogger.debug("API", "🗑️ API: Suppression service $serviceId")
        httpClient.delete("$baseUrl/salons/$salonId/services/$serviceId")
    }

    /**
     * Recherche des services dans un salon par terme.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun searchSalonServices(salonId: String, searchTerm: String): List<SalonService> {
        return httpClient.get("$baseUrl/salons/$salonId/services/search?q=$searchTerm").body()
    }

    /**
     * Filtre les services par catégorie.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getServicesByCategory(salonId: String, category: ServiceCategory): List<SalonService> {
        return httpClient.get("$baseUrl/salons/$salonId/services/categories/${category.name}").body()
    }

    /**
     * Récupère les statistiques des services d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getSalonServiceStatistics(salonId: String): Map<String, Any> {
        return httpClient.get("$baseUrl/salons/$salonId/services/statistics").body()
    }

    // ========================================
    // ENDPOINTS STAFF (ÉQUIPE)
    // ========================================

    /**
     * Récupère tous les membres du staff d'un salon.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSalonStaff(salonId: String): List<StaffMember> {
        return httpClient.get("$baseUrl/salons/$salonId/staff").body()
    }

    /**
     * Récupère les membres actifs du staff d'un salon.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getActiveSalonStaff(salonId: String): List<StaffMember> {
        return httpClient.get("$baseUrl/salons/$salonId/staff/active").body()
    }

    /**
     * Récupère un membre du staff par son ID.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getStaffById(salonId: String, staffId: String): StaffMember {
        return httpClient.get("$baseUrl/salons/$salonId/staff/$staffId").body()
    }

    /**
     * Récupère les membres du staff ayant une spécialité donnée.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getStaffBySpecialty(salonId: String, specialty: ServiceCategory): List<StaffMember> {
        return httpClient.get("$baseUrl/salons/$salonId/staff/specialties/${specialty.name}").body()
    }

    /**
     * Ajoute un nouveau membre au staff d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun addStaffMember(request: CreateStaffRequest): StaffMember {
        return httpClient.post("$baseUrl/salons/${request.salonId}/staff") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère les statistiques du staff d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getStaffStatistics(salonId: String): StaffStatistics {
        return httpClient.get("$baseUrl/salons/$salonId/staff/statistics").body()
    }

    // ========================================
    // ENDPOINTS BOOKINGS (RÉSERVATIONS)
    // ========================================

    /**
     * Crée une nouvelle réservation.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createBooking(request: CreateBookingRequest): BookingResponse {
        FrollotLogger.debug("API", "📤 API: Envoi CreateBookingRequest")
        FrollotLogger.debug("API", "   → salonId: ${request.salonId}")
        FrollotLogger.debug("API", "   → serviceId: ${request.serviceId}")
        FrollotLogger.debug("API", "   → staffId: ${request.staffId}")
        FrollotLogger.debug("API", "   → bookingDatetime: ${request.bookingDatetime}")
        FrollotLogger.debug("API", "   → clientId: ${request.clientId}")
        // 1. On récupère la réponse brute sans la convertir tout de suite
        val response = httpClient.post("$baseUrl/bookings") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        FrollotLogger.debug("API", "📥 API: Réponse reçue - Status ${response.status.value}")
        FrollotLogger.debug("API", "   → Content-Type: ${response.headers["Content-Type"]}")

        // 2. On vérifie le statut
        when (response.status.value) {
            in 200..299 -> {
                // Succès : On peut convertir en BookingResponse
                FrollotLogger.success("API", "✅ API: Réservation créée avec succès")
                return try {
                    val booking = response.body<BookingResponse>()
                    FrollotLogger.debug("API", "   → Booking ID: ${booking.id}")
                    FrollotLogger.debug("API", "   → Status: ${booking.status}")
                    FrollotLogger.debug("API", "   → Salon: ${booking.salonName}")
                    booking
                } catch (e: Exception) {
                    FrollotLogger.error("API", "❌ API: Erreur de désérialisation - ${e.message}")
                    throw Exception("Format de réponse invalide du serveur")
                }
            }
            403 -> {
                // Erreur 403 spécifique
                FrollotLogger.error("API", "⛔ API: 403 Forbidden sur createBooking")
                FrollotLogger.debug("FrollotApi", "   User Token: ${getAuthToken()?.take(10)}...") // Debug (partiel)

                // Essayer de lire le corps d'erreur s'il existe
                val errorBody = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    "Accès refusé. Vérifiez que vous êtes bien connecté en tant que Client."
                }

                throw Exception("Accès refusé (403): $errorBody")
            }
            401 -> {
                FrollotLogger.debug("Security", "🔐 API: 401 Unauthorized - Token JWT invalide ou expiré")
                throw Exception("Session expirée. Veuillez vous reconnecter.")
            }
            400 -> {
                // Bad Request - Erreur de validation
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Données invalides" }
                FrollotLogger.error("API", "❌ API: Erreur 400 - $errorBody")
                throw Exception("Données invalides: $errorBody")
            }
            409 -> {
                // Conflict - Réservation en conflit (créneau déjà pris, etc.)
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Conflit de réservation" }
                FrollotLogger.error("API", "❌ API: Erreur 409 - $errorBody")
                throw Exception("Conflit de réservation: $errorBody")
            }
            404 -> {
                // Not Found - Salon, service ou staff inexistant
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Ressource non trouvée" }
                FrollotLogger.error("API", "❌ API: Erreur 404 - $errorBody")
                throw Exception("Ressource non trouvée: $errorBody")
            }
            500 -> {
                // Internal Server Error
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Erreur serveur interne" }
                FrollotLogger.error("API", "❌ API: Erreur 500 - $errorBody")
                throw Exception("Erreur serveur. Veuillez réessayer.")
            }
            else -> {
                // Autre erreur
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Aucun détail" }
                FrollotLogger.error("API", "❌ API: Erreur ${response.status.value} - $errorBody")
                throw Exception("Erreur serveur (${response.status.value}): $errorBody")
            }
        }
    }

    /**
     * Récupère une réservation par son ID.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getBookingById(bookingId: String): BookingResponse {
        return httpClient.get("$baseUrl/bookings/$bookingId").body()
    }

    /**
     * Récupère toutes les réservations d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getSalonBookings(salonId: String): List<BookingResponse> {
        return httpClient.get("$baseUrl/salons/$salonId/bookings").body()
    }

    /**
     * Récupère les réservations à venir d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getUpcomingSalonBookings(salonId: String): List<BookingResponse> {
        return httpClient.get("$baseUrl/salons/$salonId/bookings/upcoming").body()
    }

    /**
     * Récupère toutes les réservations d'un client.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getClientBookings(clientId: String): List<BookingResponse> {
        FrollotLogger.debug("API", "📥 API: Chargement des réservations pour client $clientId")
        return try {
            val response = httpClient.get("$baseUrl/clients/$clientId/bookings").body<List<BookingResponse>>()
            FrollotLogger.success("API", "✅ API: ${response.size} réservations récupérées")
            response
        } catch (e: Exception) {
            FrollotLogger.error("API", "❌ API: Erreur lors du chargement des réservations: ${e.message}")
            throw e
        }
    }

    /**
     * Récupère les réservations à venir d'un client.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getUpcomingClientBookings(clientId: String): List<BookingResponse> {
        return httpClient.get("$baseUrl/clients/$clientId/bookings/upcoming").body()
    }

    /**
     * Récupère les réservations d'un coiffeur.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getStaffBookings(staffId: String): List<BookingResponse> {
        return httpClient.get("$baseUrl/staff/$staffId/bookings").body()
    }

    /**
     * Calcule les créneaux disponibles pour un service.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getAvailableSlots(salonId: String, request: AvailableSlotsRequest): AvailableSlotsResponse {
        return httpClient.post("$baseUrl/salons/$salonId/available-slots") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Met à jour le statut d'une réservation.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun updateBookingStatus(bookingId: String, request: UpdateBookingStatusRequest): BookingResponse {
        return httpClient.patch("$baseUrl/bookings/$bookingId/status") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Met à jour le paiement d'une réservation.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun updateBookingPayment(bookingId: String, request: UpdateBookingPaymentRequest): BookingResponse {
        return httpClient.patch("$baseUrl/bookings/$bookingId/payment") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Annule une réservation.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun cancelBooking(bookingId: String): BookingResponse {
        return httpClient.delete("$baseUrl/bookings/$bookingId").body()
    }

    /**
     * Récupère les statistiques des réservations d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getBookingStatistics(salonId: String): BookingStatistics {
        return httpClient.get("$baseUrl/salons/$salonId/bookings/statistics").body()
    }

    // ========================================
    // ENDPOINTS FILE D'ATTENTE (QUEUE)
    // ========================================

    /**
     * Récupère le statut complet de la file d'attente d'un salon.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getQueueStatus(salonId: String): QueueStatusResponse {
        return httpClient.get("$baseUrl/salons/$salonId/queue").body()
    }

    /**
     * Rejoindre la file d'attente d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun joinQueue(request: JoinQueueRequest): QueueEntryResponse {
        return httpClient.post("$baseUrl/salons/${request.salonId}/queue/join") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Quitter la file d'attente d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun leaveQueue(request: LeaveQueueRequest, salonId: String): QueueEntryResponse {
        return httpClient.post("$baseUrl/salons/$salonId/queue/leave") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Appeler le prochain client de la file (réservé au propriétaire).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun callNextClient(salonId: String): QueueEntryResponse {
        return httpClient.post("$baseUrl/salons/$salonId/queue/call-next").body()
    }

    /**
     * Retire une entrée de la file (annulation côté salon).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun removeQueueEntry(
        salonId: String,
        entryId: String
    ): QueueEntryResponse {
        val request = LeaveQueueRequest(entryId = entryId)
        return leaveQueue(request, salonId)
    }

    // ========================================
    // ENDPOINTS SOCIAL NETWORK
    // ========================================

    /**
     * Récupère le feed (tous les posts) avec pagination.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getFeed(page: Int = 0, size: Int = 20): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/feed") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère les posts d'un salon avec filtres et tri.
     * Phase C.2 - Feed par Salon
     */
    suspend fun getPostsBySalon(
        salonId: String,
        postType: PostType? = null,
        serviceId: String? = null,
        sortBy: SortBy = SortBy.RECENT,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/salons/$salonId/posts") {
            url {
                postType?.let { parameters.append("postType", it.name) }
                serviceId?.let { parameters.append("serviceId", it) }
                parameters.append("sortBy", sortBy.name)
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère les posts trending (les plus populaires) pour une période donnée.
     * Phase C.3 - Trending Coiffure
     */
    suspend fun getTrendingPosts(
        period: TrendPeriod = TrendPeriod.LAST_7D,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/posts/trending") {
            url {
                parameters.append("period", period.name)
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère les salons les plus engagés (trending).
     * Phase C.3 - Trending Coiffure
     */
    suspend fun getTrendingSalons(limit: Int = 10): List<Salon> {
        return httpClient.get("$baseUrl/salons/trending") {
            url {
                parameters.append("limit", limit.toString())
            }
        }.body()
    }

    /**
     * Récupère les salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     */
    suspend fun getSalonsNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): List<Salon> {
        return httpClient.get("$baseUrl/salons/nearby") {
            url {
                parameters.append("lat", latitude.toString())
                parameters.append("lng", longitude.toString())
                parameters.append("radius", radiusKm.toString())
            }
        }.body()
    }

    /**
     * Récupère les posts des salons dans un rayon donné autour d'une position géographique.
     * Phase C.4 - Découverte par Localisation
     */
    suspend fun getPostsNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/posts/nearby") {
            url {
                parameters.append("lat", latitude.toString())
                parameters.append("lng", longitude.toString())
                parameters.append("radius", radiusKm.toString())
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Suit un salon.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun followSalon(salonId: String): FollowResponse {
        return httpClient.post("$baseUrl/social/salons/$salonId/follow").body()
    }

    /**
     * Ne plus suivre un salon.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun unfollowSalon(salonId: String) {
        httpClient.delete("$baseUrl/social/salons/$salonId/follow")
    }

    /**
     * Suit un coiffeur.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun followCoiffeur(coiffeurId: String): FollowResponse {
        return httpClient.post("$baseUrl/social/coiffeurs/$coiffeurId/follow").body()
    }

    /**
     * Ne plus suivre un coiffeur.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun unfollowCoiffeur(coiffeurId: String) {
        httpClient.delete("$baseUrl/social/coiffeurs/$coiffeurId/follow")
    }

    /**
     * Récupère la liste des entités suivies par un utilisateur.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun getFollowing(userId: String): List<FollowResponse> {
        return httpClient.get("$baseUrl/social/users/$userId/following").body()
    }

    /**
     * Récupère la liste des followers d'un salon.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun getSalonFollowers(salonId: String): List<User> {
        return httpClient.get("$baseUrl/social/salons/$salonId/followers").body()
    }

    /**
     * Récupère la liste des followers d'un coiffeur.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun getCoiffeurFollowers(coiffeurId: String): List<User> {
        return httpClient.get("$baseUrl/social/coiffeurs/$coiffeurId/followers").body()
    }

    /**
     * Récupère le feed des entités suivies par l'utilisateur authentifié.
     * Phase D.2 - Système de Follow Salons/Coiffeurs
     */
    suspend fun getFollowingFeed(
        page: Int = 0,
        size: Int = 20
    ): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/feed/following") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Crée un nouveau post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createPost(request: CreatePostRequest): PostResponse {
        return httpClient.post("$baseUrl/social/posts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère un post par son ID.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getPostById(postId: String): PostResponse {
        return httpClient.get("$baseUrl/social/posts/$postId").body()
    }

    /**
     * Toggle le like d'un utilisateur sur un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun toggleLike(postId: String): PostResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/like").body()
    }

    /**
     * Vérifie si un utilisateur a liké un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun isPostLiked(postId: String): Boolean {
        val response = httpClient.get("$baseUrl/social/posts/$postId/liked").body<Map<String, Boolean>>()
        return response["isLiked"] ?: false
    }

    /**
     * Toggle le favori d'un utilisateur sur un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun toggleFavorite(postId: String): PostResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/favorite").body()
    }

    /**
     * Récupère les favoris d'un utilisateur avec pagination.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getFavoritesByUser(userId: String, page: Int = 0, size: Int = 20): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/users/$userId/favorites") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Archive un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun archivePost(postId: String): PostResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/archive").body()
    }

    /**
     * Désarchive un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun unarchivePost(postId: String): PostResponse {
        return httpClient.delete("$baseUrl/social/posts/$postId/archive").body()
    }

    /**
     * Récupère les archives d'un utilisateur avec pagination.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getArchivedPosts(userId: String, page: Int = 0, size: Int = 20): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/users/$userId/archives") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    // ========== GESTION DES PARTAGES ==========
    // Phase D.3 - Partage de Posts (Reposts)

    /**
     * Partage un post avec un commentaire optionnel.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun sharePost(postId: String, sharedContent: String? = null): PostResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/share") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("sharedContent" to (sharedContent ?: "")))
        }.body()
    }

    /**
     * Annule le partage d'un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun unsharePost(postId: String): PostResponse {
        return httpClient.delete("$baseUrl/social/posts/$postId/share").body()
    }

    /**
     * Vérifie si l'utilisateur authentifié a partagé un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun isPostShared(postId: String): Boolean {
        val response = httpClient.get("$baseUrl/social/posts/$postId/shared").body<Map<String, Boolean>>()
        return response["isShared"] ?: false
    }

    /**
     * Récupère la liste des partages d'un post.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSharesByPost(postId: String, page: Int = 0, size: Int = 20): PageResponse<PostShareResponse> {
        return httpClient.get("$baseUrl/social/posts/$postId/shares") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    // ========== RÉACTIONS SPÉCIALISÉES ==========
    // Phase D.4 - Réactions Spécialisées Coiffure

    /**
     * Ajoute ou modifie une réaction sur un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * @param postId ID du post
     * @param reactionType Type de réaction (LIKE, LOVE, WOW, INSPIRANT, MAGNIFIQUE, BRAVO)
     * @return PostResponse avec reactions et currentUserReaction mis à jour
     */
    suspend fun addReaction(postId: String, reactionType: ReactionType): PostResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/reactions") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("reactionType" to reactionType.name))
        }.body()
    }

    /**
     * Supprime la réaction d'un utilisateur sur un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * @param postId ID du post
     * @return PostResponse avec reactions et currentUserReaction mis à jour
     */
    suspend fun removeReaction(postId: String): PostResponse {
        return httpClient.delete("$baseUrl/social/posts/$postId/reactions").body()
    }

    /**
     * Récupère les compteurs de réactions par type pour un post.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     * 
     * @param postId ID du post
     * @return Map<String, Int> avec les compteurs de réactions (clé: nom de ReactionType en lowercase)
     */
    suspend fun getReactionsByPost(postId: String): Map<String, Int> {
        return httpClient.get("$baseUrl/social/posts/$postId/reactions").body()
    }

    /**
     * Crée un nouveau commentaire.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createComment(postId: String, request: CreateCommentRequest): CommentResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/comments") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère les commentaires d'un post.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getCommentsByPost(postId: String, page: Int = 0, size: Int = 50): PageResponse<CommentResponse> {
        return httpClient.get("$baseUrl/social/posts/$postId/comments") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Supprime un commentaire.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun deleteComment(commentId: String) {
        httpClient.delete("$baseUrl/social/comments/$commentId")
    }

    /**
     * Supprime un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun deletePost(postId: String) {
        httpClient.delete("$baseUrl/social/posts/$postId")
    }

    // ========================================
    // ENDPOINTS TAGS/MENTIONS
    // ========================================

    /**
     * Ajoute un tag (salon ou utilisateur) à un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun addTag(postId: String, request: CreateTagRequest): TagResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/tags") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Supprime un tag d'un post.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun removeTag(postId: String, tagId: String) {
        httpClient.delete("$baseUrl/social/posts/$postId/tags/$tagId")
    }

    /**
     * Récupère tous les tags d'un post.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getTagsByPost(postId: String): List<TagResponse> {
        return httpClient.get("$baseUrl/social/posts/$postId/tags").body()
    }

    // ========================================
    // ENDPOINTS HASHTAGS
    // ========================================

    /**
     * Récupère les hashtags les plus utilisés (trending).
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getTrendingHashtags(limit: Int = 20): List<HairHashtagResponse> {
        return httpClient.get("$baseUrl/social/hashtags/trending") {
            url {
                parameters.append("limit", limit.toString())
            }
        }.body()
    }

    /**
     * Recherche des hashtags par nom.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun searchHashtags(query: String): List<HairHashtagResponse> {
        return httpClient.get("$baseUrl/social/hashtags/search") {
            url {
                parameters.append("q", query)
            }
        }.body()
    }

    /**
     * Suggère des hashtags basés sur un préfixe.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun suggestHashtags(prefix: String, limit: Int = 10): List<HairHashtagResponse> {
        return httpClient.get("$baseUrl/social/hashtags/suggest") {
            url {
                parameters.append("prefix", prefix)
                parameters.append("limit", limit.toString())
            }
        }.body()
    }

    /**
     * Récupère les posts associés à un hashtag.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getPostsByHashtag(hashtagName: String, page: Int = 0, size: Int = 20): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/hashtags/$hashtagName/posts") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère les hashtags par catégorie.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getHashtagsByCategory(category: HairHashtagCategory): List<HairHashtagResponse> {
        return httpClient.get("$baseUrl/social/hashtags/categories/${category.name}").body()
    }

    // ========================================
    // ENDPOINTS DE RECHERCHE (Phase C.1 - Recherche spécialisée coiffure)
    // ========================================

    /**
     * Recherche des posts par contenu (texte).
     * Phase C.1 - Recherche spécialisée coiffure
     */
    suspend fun searchPostsByContent(query: String, page: Int = 0, size: Int = 20): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/posts/search") {
            url {
                parameters.append("q", query)
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Recherche des posts avec filtres avancés.
     * Phase C.1 - Recherche spécialisée coiffure
     */
    suspend fun searchPostsWithFilters(
        query: String? = null,
        postType: PostType? = null,
        serviceId: String? = null,
        salonId: String? = null,
        hashtagName: String? = null,
        authorId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/social/posts/search/advanced") {
            url {
                query?.let { parameters.append("q", it) }
                postType?.let { parameters.append("postType", it.name) }
                serviceId?.let { parameters.append("serviceId", it) }
                salonId?.let { parameters.append("salonId", it) }
                hashtagName?.let { parameters.append("hashtagName", it) }
                authorId?.let { parameters.append("authorId", it) }
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Recherche unifiée dans tous les types de contenu.
     * Phase C.1 - Recherche spécialisée coiffure
     */
    suspend fun unifiedSearch(
        query: String? = null,
        type: SearchType = SearchType.ALL,
        postType: PostType? = null,
        serviceId: String? = null,
        salonId: String? = null,
        hashtagName: String? = null,
        authorId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): SearchResponse {
        return httpClient.get("$baseUrl/social/search") {
            url {
                query?.let { parameters.append("q", it) }
                parameters.append("type", type.name)
                postType?.let { parameters.append("postType", it.name) }
                serviceId?.let { parameters.append("serviceId", it) }
                salonId?.let { parameters.append("salonId", it) }
                hashtagName?.let { parameters.append("hashtagName", it) }
                authorId?.let { parameters.append("authorId", it) }
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    // ========================================
    // ENDPOINTS MEDIA UPLOAD
    // ========================================

    /**
     * Upload un fichier image.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param fileBytes Les bytes du fichier
     * @param fileName Le nom du fichier
     * @return L'URL complète de l'image uploadée
     */
    suspend fun uploadImage(fileBytes: ByteArray, fileName: String): String {
        FrollotLogger.debug("API", "📤 API: Upload image - $fileName (${fileBytes.size} bytes)")

        val response = httpClient.post("$baseUrl/media/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", fileBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            // Détection automatique du content-type basé sur l'extension
                            val contentType = when {
                                fileName.endsWith(".jpg", ignoreCase = true) ||
                                        fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                                fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
                                fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                                else -> "image/jpeg" // Par défaut
                            }
                            append(HttpHeaders.ContentType, contentType)
                        })
                    }
                )
            )
        }

        FrollotLogger.debug("API", "📥 API: Réponse upload - Status ${response.status.value}")

        return when (response.status.value) {
            in 200..299 -> {
                val result = response.body<Map<String, String>>()
                val url = result["url"] ?: throw Exception("URL manquante dans la réponse du serveur")
                FrollotLogger.success("API", "✅ API: Image uploadée - $url")
                url
            }
            400 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Fichier invalide" }
                FrollotLogger.error("API", "❌ API: Erreur 400 - $errorBody")
                throw Exception("Fichier invalide: $errorBody")
            }
            401 -> {
                FrollotLogger.error("API", "❌ API: Erreur 401 - Non authentifié")
                throw Exception("Session expirée. Veuillez vous reconnecter.")
            }
            413 -> {
                FrollotLogger.error("API", "❌ API: Erreur 413 - Fichier trop volumineux")
                throw Exception("Le fichier est trop volumineux (max 10MB)")
            }
            500 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Erreur serveur" }
                FrollotLogger.error("API", "❌ API: Erreur 500 - $errorBody")
                throw Exception("Erreur serveur: $errorBody")
            }
            else -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Erreur inconnue" }
                FrollotLogger.error("API", "❌ API: Erreur ${response.status.value} - $errorBody")
                throw Exception("Erreur upload (${response.status.value}): $errorBody")
            }
        }
    }

    // ========================================
    // ENDPOINTS REVIEWS (AVIS & NOTES)
    // ========================================

    /**
     * Crée un nouvel avis pour une réservation terminée.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createReview(request: CreateReviewRequest): Review {
        return httpClient.post("$baseUrl/reviews") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère tous les avis d'un salon avec pagination.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSalonReviews(salonId: String, page: Int = 0, size: Int = 20): PageResponse<Review> {
        return httpClient.get("$baseUrl/salons/$salonId/reviews") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère tous les avis d'un salon (sans pagination).
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getAllSalonReviews(salonId: String): List<Review> {
        return httpClient.get("$baseUrl/salons/$salonId/reviews/all").body()
    }

    /**
     * Récupère un avis par son ID.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getReviewById(reviewId: String): Review {
        return httpClient.get("$baseUrl/reviews/$reviewId").body()
    }

    /**
     * Récupère tous les avis d'un client.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getClientReviews(clientId: String): List<Review> {
        return httpClient.get("$baseUrl/clients/$clientId/reviews").body()
    }

    /**
     * Vérifie si un avis existe pour une réservation donnée.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun hasReviewForBooking(bookingId: String): Boolean {
        FrollotLogger.debug("API", "🔍 API: Vérification avis pour booking $bookingId")

        return try {
            val response = httpClient.get("$baseUrl/bookings/$bookingId/review/exists")

            // Vérifier la réponse
            val result = response.body<Map<String, Boolean>>()
            val exists = result["exists"] ?: false

            FrollotLogger.debug("API", "📊 API: Avis existe pour booking $bookingId? $exists")
            exists
        } catch (e: Exception) {
            FrollotLogger.error("API", "❌ API: Erreur vérification avis - ${e.message}")
            e.printStackTrace()
            false  // En cas d'erreur, on assume qu'il n'y a pas d'avis
        }
    }

    /**
     * Récupère les statistiques d'avis d'un salon.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSalonReviewStats(salonId: String): SalonReviewStats {
        return httpClient.get("$baseUrl/salons/$salonId/reviews/stats").body()
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Teste la connexion au serveur.
     *
     * @return true si le serveur répond, false sinon
     */
    suspend fun ping(): Boolean {
        return try {
            httpClient.get("$baseUrl/salons")
            true
        } catch (e: Exception) {
            FrollotLogger.error("API", "❌ Ping failed: ${e.message}")
            false
        }
    }

    // ========================================
    // ENDPOINTS PAYMENTS (STRIPE)
    // ========================================

    /**
     * Crée un PaymentIntent Stripe pour une réservation.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param bookingId ID de la réservation
     * @return PaymentIntentResponse avec clientSecret pour Stripe Elements
     */
    suspend fun createPaymentIntent(bookingId: String): PaymentIntentResponse {
        return httpClient.post("$baseUrl/payments/create-intent") {
            url {
                parameters.append("bookingId", bookingId)
            }
        }.body()
    }

    /**
     * Confirme un paiement après complétion côté frontend.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     *
     * @param paymentIntentId ID du PaymentIntent Stripe
     * @return PaymentResponse avec statut final
     */
    suspend fun confirmPayment(paymentIntentId: String): PaymentResponse {
        return httpClient.post("$baseUrl/payments/confirm") {
            url {
                parameters.append("paymentIntentId", paymentIntentId)
            }
        }.body()
    }

    /**
     * Récupère un paiement par son ID.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getPayment(paymentId: String): PaymentResponse {
        return httpClient.get("$baseUrl/payments/$paymentId").body()
    }

    // ========== STRIPE CHECKOUT (RECOMMANDÉ) ==========

    /**
     * Crée une Stripe Checkout Session pour un paiement sécurisé.
     * C'est la méthode RECOMMANDÉE pour les paiements.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * @param request Requête avec bookingId et URLs de retour
     * @return CheckoutSessionResponse avec l'URL de redirection Stripe
     */
    suspend fun createCheckoutSession(request: CheckoutSessionRequest): CheckoutSessionResponse {
        return httpClient.post("$baseUrl/payments/create-checkout-session") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère le statut d'une Checkout Session.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * 
     * @param sessionId ID de la session Stripe
     * @return PaymentSessionStatus avec le statut du paiement
     */
    suspend fun getCheckoutSessionStatus(sessionId: String): PaymentSessionStatus {
        return httpClient.get("$baseUrl/payments/checkout-session/$sessionId").body()
    }

    /**
     * Récupère tous les paiements d'une réservation.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getPaymentsByBooking(bookingId: String): List<PaymentResponse> {
        return httpClient.get("$baseUrl/payments/booking/$bookingId").body()
    }

    /**
     * Récupère tous les paiements d'un client.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getPaymentsByClient(clientId: String): List<PaymentResponse> {
        return httpClient.get("$baseUrl/payments/client/$clientId").body()
    }

    /**
     * Récupère tous les paiements d'un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun getPaymentsBySalon(salonId: String): List<PaymentResponse> {
        return httpClient.get("$baseUrl/payments/salon/$salonId").body()
    }

    // ========================================
    // ENDPOINTS PORTFOLIOS
    // ========================================

    /**
     * Crée un nouveau portfolio.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createPortfolio(request: CreatePortfolioRequest): PortfolioResponse {
        return httpClient.post("$baseUrl/portfolios") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Met à jour un portfolio.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun updatePortfolio(portfolioId: String, request: UpdatePortfolioRequest): PortfolioResponse {
        return httpClient.put("$baseUrl/portfolios/$portfolioId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Supprime un portfolio.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun deletePortfolio(portfolioId: String) {
        httpClient.delete("$baseUrl/portfolios/$portfolioId")
    }

    /**
     * Récupère un portfolio par son ID.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise pour les portfolios publics)
     */
    suspend fun getPortfolioById(portfolioId: String): PortfolioResponse {
        return httpClient.get("$baseUrl/portfolios/$portfolioId").body()
    }

    /**
     * Récupère tous les portfolios d'un propriétaire.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise pour les portfolios publics)
     */
    suspend fun getPortfoliosByOwner(
        ownerId: String,
        ownerType: PortfolioOwnerType,
        includePrivate: Boolean = false
    ): List<PortfolioResponse> {
        return httpClient.get("$baseUrl/portfolios/owner/$ownerId") {
            url {
                parameters.append("ownerType", ownerType.name)
                if (includePrivate) {
                    parameters.append("includePrivate", "true")
                }
            }
        }.body()
    }

    /**
     * Ajoute un post à un portfolio.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun addPostToPortfolio(portfolioId: String, postId: String): PortfolioPostResponse {
        return httpClient.post("$baseUrl/portfolios/$portfolioId/posts/$postId").body()
    }

    /**
     * Retire un post d'un portfolio.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun removePostFromPortfolio(portfolioId: String, postId: String) {
        httpClient.delete("$baseUrl/portfolios/$portfolioId/posts/$postId")
    }

    /**
     * Récupère tous les posts d'un portfolio.
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise pour les portfolios publics)
     */
    suspend fun getPortfolioPosts(
        portfolioId: String,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<PostResponse> {
        return httpClient.get("$baseUrl/portfolios/$portfolioId/posts") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Réorganise les posts d'un portfolio.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun reorderPortfolioPosts(portfolioId: String, postIds: List<String>) {
        httpClient.put("$baseUrl/portfolios/$portfolioId/posts/reorder") {
            contentType(ContentType.Application.Json)
            setBody(postIds)
        }
    }

    // ========================================
    // ENDPOINTS PROFILS (Phase E)
    // ========================================

    /**
     * Récupère le profil enrichi d'un coiffeur.
     * Phase E.1 - Profil Coiffeur Enrichi
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getCoiffeurProfile(coiffeurId: String): CoiffeurProfileResponse {
        return httpClient.get("$baseUrl/social/coiffeurs/$coiffeurId/profile").body()
    }

    /**
     * Met à jour le profil enrichi d'un coiffeur.
     * Phase E.1 - Profil Coiffeur Enrichi
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire uniquement
     */
    suspend fun updateCoiffeurProfile(
        coiffeurId: String,
        request: UpdateCoiffeurProfileRequest
    ): CoiffeurProfileResponse {
        return httpClient.put("$baseUrl/social/coiffeurs/$coiffeurId/profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère le profil social enrichi d'un salon.
     * Phase E.2 - Profil Salon Social
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSalonSocialProfile(salonId: String): SalonSocialProfileResponse {
        return httpClient.get("$baseUrl/social/salons/$salonId/profile").body()
    }

    /**
     * Récupère le profil enrichi d'un client.
     * Phase E.4 - Profil Client
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getClientProfile(clientId: String): ClientProfileResponse {
        return httpClient.get("$baseUrl/social/clients/$clientId/profile").body()
    }

    /**
     * Récupère le profil enrichi d'un propriétaire de salon.
     * Phase E.5 - Profil Propriétaire de Salon
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getSalonOwnerProfile(ownerId: String): SalonOwnerProfileResponse {
        return httpClient.get("$baseUrl/social/owners/$ownerId/profile").body()
    }

    /**
     * Met à jour le profil social d'un salon.
     * Phase E.2 - Profil Salon Social
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire uniquement
     */
    suspend fun updateSalonSocialProfile(
        salonId: String,
        request: UpdateSalonSocialProfileRequest
    ): SalonSocialProfileResponse {
        return httpClient.put("$baseUrl/social/salons/$salonId/profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ========================================
    // ENDPOINTS BADGES (Phase E.3)
    // ========================================

    /**
     * Récupère tous les badges disponibles.
     * Phase E.3 - Badges et Certifications
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getAvailableBadges(category: BadgeCategory? = null): List<BadgeResponse> {
        return httpClient.get("$baseUrl/social/badges") {
            category?.let { cat ->
                url {
                    parameters.append("category", cat.name)
                }
            }
        }.body()
    }

    /**
     * Récupère les badges d'un utilisateur.
     * Phase E.3 - Badges et Certifications
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getUserBadges(
        userId: String,
        includeHidden: Boolean = false
    ): List<UserBadgeResponse> {
        return httpClient.get("$baseUrl/social/users/$userId/badges") {
            url {
                if (includeHidden) {
                    parameters.append("includeHidden", "true")
                }
            }
        }.body()
    }

    /**
     * Attribue un badge à un utilisateur.
     * Phase E.3 - Badges et Certifications
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Admin uniquement
     */
    suspend fun awardBadge(userId: String, request: AwardBadgeRequest): UserBadgeResponse {
        return httpClient.post("$baseUrl/social/users/$userId/badges") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Affiche ou masque un badge sur le profil d'un utilisateur.
     * Phase E.3 - Badges et Certifications
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire uniquement
     */
    suspend fun toggleBadgeDisplay(userId: String, badgeId: String): UserBadgeResponse {
        return httpClient.put("$baseUrl/social/users/$userId/badges/$badgeId").body()
    }

    /**
     * Retire un badge d'un utilisateur.
     * Phase E.3 - Badges et Certifications
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Admin uniquement
     */
    suspend fun removeBadge(userId: String, badgeId: String) {
        httpClient.delete("$baseUrl/social/users/$userId/badges/$badgeId")
    }

    // ========== POSTS ÉPINGLÉS ==========
    // Phase F.2 - Posts Épinglés pour Salons

    /**
     * Épingle un post.
     * Phase F.2 - Posts Épinglés pour Salons
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Seul l'auteur du post peut l'épingler. Maximum 3 posts épinglés par auteur.
     */
    suspend fun pinPost(postId: String): PostResponse {
        return httpClient.post("$baseUrl/social/posts/$postId/pin").body()
    }

    /**
     * Désépingle un post.
     * Phase F.2 - Posts Épinglés pour Salons
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Seul l'auteur du post peut le désépingler.
     */
    suspend fun unpinPost(postId: String): PostResponse {
        return httpClient.delete("$baseUrl/social/posts/$postId/pin").body()
    }

    /**
     * Récupère les posts épinglés d'un utilisateur.
     * Phase F.2 - Posts Épinglés pour Salons
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getPinnedPosts(authorId: String): List<PostResponse> {
        return httpClient.get("$baseUrl/social/users/$authorId/pinned-posts").body()
    }

    // ========================================
    // ENDPOINTS COLLECTIONS (Phase F.1)
    // ========================================

    /**
     * Crée une nouvelle collection.
     * Phase F.1 - Collections Thématiques
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     */
    suspend fun createCollection(request: CreateCollectionRequest): CollectionResponse {
        return httpClient.post("$baseUrl/social/collections") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Met à jour une collection.
     * Phase F.1 - Collections Thématiques
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire uniquement
     */
    suspend fun updateCollection(collectionId: String, request: UpdateCollectionRequest): CollectionResponse {
        return httpClient.put("$baseUrl/social/collections/$collectionId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Supprime une collection.
     * Phase F.1 - Collections Thématiques
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire uniquement
     */
    suspend fun deleteCollection(collectionId: String) {
        httpClient.delete("$baseUrl/social/collections/$collectionId")
    }

    /**
     * Récupère les collections d'un utilisateur.
     * Phase F.1 - Collections Thématiques
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise pour les collections publiques)
     */
    suspend fun getCollectionsByUser(userId: String, includePrivate: Boolean = false): List<CollectionResponse> {
        return httpClient.get("$baseUrl/social/users/$userId/collections") {
            url {
                parameters.append("includePrivate", includePrivate.toString())
            }
        }.body()
    }

    /**
     * Récupère une collection par son ID.
     * Phase F.1 - Collections Thématiques
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise pour les collections publiques)
     */
    suspend fun getCollectionById(collectionId: String): CollectionResponse {
        return httpClient.get("$baseUrl/social/collections/$collectionId").body()
    }

    /**
     * Ajoute un post à une collection.
     * Phase F.1 - Collections Thématiques
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire de la collection uniquement
     */
    suspend fun addPostToCollection(collectionId: String, postId: String): CollectionPostResponse {
        return httpClient.post("$baseUrl/social/collections/$collectionId/posts/$postId").body()
    }

    /**
     * Retire un post d'une collection.
     * Phase F.1 - Collections Thématiques
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) - Propriétaire de la collection uniquement
     */
    suspend fun removePostFromCollection(collectionId: String, postId: String) {
        httpClient.delete("$baseUrl/social/collections/$collectionId/posts/$postId")
    }

    /**
     * Récupère les posts d'une collection.
     * Phase F.1 - Collections Thématiques
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise pour les collections publiques)
     */
    suspend fun getCollectionPosts(
        collectionId: String,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<CollectionPostResponse> {
        return httpClient.get("$baseUrl/social/collections/$collectionId/posts") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère les collections publiques.
     * Phase F.1 - Collections Thématiques
     * ✅ ENDPOINT PUBLIC (pas d'authentification requise)
     */
    suspend fun getPublicCollections(
        category: CollectionCategory? = null,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<CollectionResponse> {
        return httpClient.get("$baseUrl/social/collections/public") {
            url {
                category?.let { cat ->
                    parameters.append("category", cat.name)
                }
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    // ========== ENDPOINTS DE SIGNALEMENT ==========
    // Phase H.1 - Signalement de Contenu

    /**
     * Signale du contenu inapproprié.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Phase H.1 - Signalement de Contenu
     * 
     * @param request Données du signalement
     * @return ReportResponse avec les détails du signalement créé
     */
    suspend fun reportContent(request: CreateReportRequest): ReportResponse {
        return httpClient.post("$baseUrl/social/reports") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère les signalements de l'utilisateur authentifié.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Phase H.1 - Signalement de Contenu
     * 
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @return PageResponse<ReportResponse> avec les signalements de l'utilisateur
     */
    suspend fun getMyReports(page: Int = 0, size: Int = 20): PageResponse<ReportResponse> {
        return httpClient.get("$baseUrl/social/reports/my-reports") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère tous les signalements (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.1 - Signalement de Contenu
     * 
     * @param status Filtre par statut (optionnel)
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @return PageResponse<ReportResponse> avec tous les signalements
     */
    suspend fun getReports(
        status: ReportStatus? = null,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<ReportResponse> {
        return httpClient.get("$baseUrl/social/reports") {
            url {
                status?.let { st ->
                    parameters.append("status", st.name)
                }
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère les signalements en attente (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.1 - Signalement de Contenu
     * 
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @return PageResponse<ReportResponse> avec les signalements en attente
     */
    suspend fun getPendingReports(page: Int = 0, size: Int = 20): PageResponse<ReportResponse> {
        return httpClient.get("$baseUrl/social/reports/pending") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Traite un signalement (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.1 - Signalement de Contenu
     * 
     * @param reportId ID du signalement à traiter
     * @param request Données de traitement (nouveau statut)
     * @return ReportResponse avec le statut mis à jour
     */
    suspend fun handleReport(reportId: String, request: HandleReportRequest): ReportResponse {
        return httpClient.put("$baseUrl/social/moderation/reports/$reportId/handle") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ========== GESTION DE LA VÉRIFICATION (H.2) ==========

    /**
     * Demande une vérification pour un utilisateur ou un salon.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Phase H.2 - Vérification Salons/Coiffeurs
     * 
     * @param entityType Type d'entité ('user' ou 'salon')
     * @param entityId ID de l'entité à vérifier
     * @param request Données de la demande de vérification
     * @return Message de confirmation
     */
    suspend fun requestVerification(
        entityType: String,
        entityId: String,
        request: RequestVerificationRequest
    ): Map<String, String> {
        return httpClient.post("$baseUrl/verification/request/$entityType/$entityId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Vérifie un utilisateur (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.2 - Vérification Salons/Coiffeurs
     * 
     * @param userId ID de l'utilisateur à vérifier
     * @param request Données de vérification
     * @return UserResponse avec l'utilisateur vérifié
     */
    suspend fun verifyUser(userId: String, request: VerifyUserRequest): UserResponse {
        return httpClient.put("$baseUrl/verification/users/$userId/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Vérifie un salon (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.2 - Vérification Salons/Coiffeurs
     * 
     * @param salonId ID du salon à vérifier
     * @param request Données de vérification
     * @return SalonResponse avec le salon vérifié
     */
    suspend fun verifySalon(salonId: String, request: VerifySalonRequest): Salon {
        return httpClient.put("$baseUrl/verification/salons/$salonId/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<Salon>()
    }

    /**
     * Révoque la vérification d'un utilisateur ou d'un salon (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.2 - Vérification Salons/Coiffeurs
     * 
     * @param entityType Type d'entité ('user' ou 'salon')
     * @param entityId ID de l'entité
     * @return Message de confirmation
     */
    suspend fun revokeVerification(entityType: String, entityId: String): Map<String, String> {
        return httpClient.delete("$baseUrl/verification/$entityType/$entityId/revoke") {
        }.body()
    }

    // ========== GESTION DE LA MODÉRATION (H.3) ==========

    /**
     * Modère un contenu (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param request Données de modération
     * @return ModerationActionResponse avec l'action créée
     */
    suspend fun moderateContent(request: ModerateContentRequest): ModerationActionResponse {
        return httpClient.post("$baseUrl/social/moderation/moderate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Fait appel d'une action de modération.
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT)
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param request Données de l'appel
     * @return ModerationActionResponse avec le statut d'appel mis à jour
     */
    suspend fun appealModeration(request: AppealModerationRequest): ModerationActionResponse {
        return httpClient.post("$baseUrl/social/moderation/appeal") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Traite un appel de modération (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param moderationActionId ID de l'action de modération
     * @param request Données de traitement de l'appel
     * @return ModerationActionResponse avec le statut d'appel mis à jour
     */
    suspend fun handleAppeal(moderationActionId: String, request: HandleAppealRequest): ModerationActionResponse {
        return httpClient.put("$baseUrl/social/moderation/appeals/$moderationActionId/handle") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Récupère les actions de modération pour une entité spécifique (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param entityType Type d'entité
     * @param entityId ID de l'entité
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @return PageResponse avec les actions de modération
     */
    suspend fun getModerationActionsByEntity(
        entityType: ReportedEntityType,
        entityId: String,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<ModerationActionResponse> {
        return httpClient.get("$baseUrl/social/moderation/actions/entity/$entityType/$entityId") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Récupère tous les appels en attente (admin uniquement).
     * 🔒 NÉCESSITE AUTHENTIFICATION (token JWT) + ADMIN
     * Phase H.3 - Modération de Contenu Coiffure
     * 
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @return PageResponse avec les appels en attente
     */
    suspend fun getPendingAppeals(page: Int = 0, size: Int = 20): PageResponse<ModerationActionResponse> {
        return httpClient.get("$baseUrl/social/moderation/appeals/pending") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }

    /**
     * Demande l'envoi d'un nouveau code de vérification pour un email existant.
     * Utile pour les utilisateurs bloqués avec un email non vérifié.
     */
    suspend fun requestVerificationCode(email: String): Map<String, String> {
        return httpClient.post("$baseUrl/users/request-verification") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("email=$email")
        }.body()
    }

    /**
     * Ferme le client HTTP (à appeler lors de la destruction de l'app).
     */
    fun close() {
        httpClient.close()
    }
}
