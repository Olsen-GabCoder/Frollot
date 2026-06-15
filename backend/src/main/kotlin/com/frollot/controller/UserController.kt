package com.frollot.controller

import com.frollot.dto.*
import com.frollot.dto.ChangePasswordRequest
import com.frollot.dto.ChangePasswordResponse
import com.frollot.dto.ChangeEmailRequest
import com.frollot.dto.ChangeEmailResponse
import com.frollot.dto.ChangePhoneRequest
import com.frollot.dto.ChangePhoneResponse
import com.frollot.dto.DeleteAccountRequest
import com.frollot.dto.DeleteAccountResponse
import com.frollot.dto.SessionResponse
import com.frollot.dto.SessionsListResponse
import com.frollot.dto.RevokeSessionResponse
import com.frollot.model.User
import com.frollot.repository.UserRepository
import com.frollot.security.JwtTokenProvider
import com.frollot.security.RateLimitFilter
import com.frollot.service.RefreshTokenService
import com.frollot.service.UserService
import com.frollot.service.EmailVerificationService
import com.frollot.service.EmailSendResult
import com.frollot.service.EmailSendException
import com.frollot.service.PreRegistrationResult
import com.frollot.service.PasswordResetService
import com.frollot.service.TwoFactorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/users")
@Tag(
    name = "Gestion des Utilisateurs",
    description = "API d'authentification et de gestion des utilisateurs"
)
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val rateLimitFilter: RateLimitFilter,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val twoFactorService: TwoFactorService
) {

    private val logger = org.slf4j.LoggerFactory.getLogger(UserController::class.java)

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Récupère l'utilisateur authentifié depuis le SecurityContext.
     */
    private fun getAuthenticatedUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifié")
        }
        return authentication.principal as User
    }

    // ========== ENDPOINTS ==========

    /**
     * Récupère tous les utilisateurs (admin seulement).
     */
    @Operation(
        summary = "Lister tous les utilisateurs",
        description = "Retourne la liste de tous les utilisateurs. Réservé aux administrateurs."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        // Vue ADMIN : le numéro reste visible quelle que soit la visibilité choisie (V045)
        val users = userService.getAllUsers().map { UserResponse.fromEntity(it, includePrivatePhone = true) }
        return ResponseEntity.ok(users)
    }

    /**
     * Finalise l'inscription après vérification email.
     */
    @Operation(
        summary = "Finaliser l'inscription",
        description = "Active le compte utilisateur après vérification de l'email"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Inscription finalisée avec succès"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Token invalide ou expiré"
            ),
            ApiResponse(
                responseCode = "409",
                description = "Email déjà utilisé"
            )
        ]
    )
    @RequestMapping(value = ["/complete-registration"], method = [RequestMethod.GET, RequestMethod.POST])
    fun completeRegistration(
        @Parameter(description = "Token de vérification reçu par email", required = true)
        @RequestParam token: String
    ): ResponseEntity<AuthResponse> {
        try {
            println("🔵 [UserController] === FINALISATION INSCRIPTION ===")
            println("🔵 [UserController] Token reçu: $token")

            val user = userService.completeRegistration(token)

            println("✅ [UserController] Utilisateur CRÉÉ avec succès:")
            println("   └─ Email: ${user.email}")
            println("   └─ ID: ${user.id}")
            println("   └─ Email vérifié: ${user.emailVerified}")

            // Générer les tokens JWT
            val accessToken = jwtTokenProvider.generateToken(user)
            val refreshToken = refreshTokenService.createRefreshTokenWithDeviceInfo(
                userId = user.id!!,
                userAgent = "Frollot Mobile App",
                ipAddress = null
            )

            println("🔐 [UserController] Tokens JWT générés:")
            println("   └─ Access token: ${accessToken.take(20)}...")
            println("   └─ Refresh token: ${refreshToken.take(20)}...")

            val response = AuthResponse.fromUser(
                user = user,
                accessToken = accessToken,
                refreshToken = refreshToken,
                message = "🎉 Bienvenue ! Votre compte a été activé avec succès.",
                emailSendStatus = "verified"
            )

            println("📤 [UserController] Réponse HTTP 200 (OK) avec tokens")
            return ResponseEntity.ok(response)

        } catch (e: IllegalArgumentException) {
            println("❌ [UserController] Token invalide: ${e.message}")
            return ResponseEntity.badRequest()
                .body(AuthResponse.error(e.message ?: "Token invalide ou expiré"))
        }
    }

    /**
     * Inscription d'un nouvel utilisateur (pré-inscription).
     */
    @Operation(
        summary = "Pré-inscription",
        description = "Crée une pré-inscription et envoie un email de vérification"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Pré-inscription réussie, vérification email requise"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides"
            ),
            ApiResponse(
                responseCode = "409",
                description = "Email déjà utilisé"
            )
        ]
    )
    @PostMapping("/register")
    fun register(
        @Parameter(description = "Données d'inscription", required = true)
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        println("🔵 [UserController] Requête d'inscription reçue")
        println("🔵 [UserController] Email: ${request.email}")
        println("🔵 [UserController] Password: ${if (request.password.isNotBlank()) "${request.password.length} chars" else "VIDE!"}")
        println("🔵 [UserController] FirstName: ${request.firstName}, LastName: ${request.lastName}")

        try {
            println("🔵 [UserController] === TRAITEMENT INSCRIPTION ===")
            println("🔵 [UserController] Requête reçue pour: ${request.email}")

            // 🆕 NOUVEAU SYSTÈME : Pré-inscription avec vérification email obligatoire
            val preRegistrationResult = userService.preRegisterUser(request)

            println("✅ [UserController] Pré-inscription RÉUSSIE:")
            println("   └─ Pending ID: ${preRegistrationResult.pendingId}")
            println("   └─ Email: ${request.email}")
            println("   └─ Status email: ${preRegistrationResult.emailSendResult.toStatusString()}")

            // IMPORTANT : Vérifier que l'utilisateur n'est PAS créé dans 'users' à ce stade
            println("🔍 [UserController] Vérification d'intégrité:")
            println("   └─ Utilisateur doit être dans 'pending_registrations' UNIQUEMENT")
            println("   └─ PAS dans 'users' avant vérification email")

            // Retourner une réponse spéciale pour la pré-inscription
            val response = AuthResponse.preRegistration(
                pendingId = preRegistrationResult.pendingId,
                email = request.email,
                message = preRegistrationResult.message,
                emailSendStatus = preRegistrationResult.emailSendResult.toStatusString()
            )

            println("📤 [UserController] Réponse HTTP 202 (Accepted) envoyée")
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)

        } catch (e: EmailSendException) {
            println("❌ [UserController] Pré-inscription annulée: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Erreur lors de l'envoi de l'email de vérification. Veuillez réessayer."))
        }
    }

    /**
     * Connexion d'un utilisateur.
     * 
     * Sécurité : Blocage progressif après échecs répétés (brute force protection)
     */
    @Operation(
        summary = "Connexion",
        description = "Authentifie un utilisateur et retourne un token JWT"
    )
    @PostMapping("/login")
    fun login(
        @Parameter(description = "Identifiants de connexion", required = true)
        @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val clientIp = getClientIpAddress(httpRequest)

        // Hygiène S9b : plus aucun email/IP en clair au niveau INFO ; PII réservée au DEBUG
        logger.debug("Tentative de connexion depuis {}", clientIp)

        val user = userRepository.findByEmail(request.email)
        if (user == null) {
            logger.debug("Connexion refusée : email inconnu")
            // Enregistrer l'échec pour le rate limiting
            rateLimitFilter.recordLoginFailure(clientIp)
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Email ou mot de passe incorrect"))
        }

        // FORCER LE RECHARGEMENT DEPUIS LA BASE POUR ÉVITER LES PROBLÈMES DE CACHE
        // Cela garantit que emailVerified est à jour après vérification email
        val refreshedUser = userRepository.findById(user.id!!).orElse(user)

        if (!refreshedUser.isActive) {
            logger.warn("Connexion refusée : compte désactivé (userId={})", refreshedUser.id)
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(AuthResponse.error("Compte désactivé"))
        }

        // Vérifier que l'email est vérifié (obligatoire pour utiliser le compte)
        if (!refreshedUser.emailVerified) {
            logger.debug("Connexion refusée : email non vérifié (userId={})", refreshedUser.id)
            // Permettre à l'utilisateur de recevoir un nouveau code de vérification
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(AuthResponse.error("Votre adresse email n'a pas été vérifiée. Veuillez utiliser l'endpoint /api/users/me/resend-verification pour recevoir un nouveau code de vérification."))
        }

        if (!userService.checkPassword(request.password, refreshedUser.passwordHash)) {
            // Enregistrer l'échec pour le rate limiting
            rateLimitFilter.recordLoginFailure(clientIp)
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Email ou mot de passe incorrect"))
        }

        // Login réussi - réinitialiser le compteur d'échecs
        rateLimitFilter.clearLoginFailures(clientIp)

        // === S9b : INTERCEPTION 2FA ===
        // Point exact identifié au diagnostic : APRÈS la validation du mot de passe
        // (et clearLoginFailures), AVANT toute émission de token. Pour un compte
        // 2FA-activé : AUCUN access token, AUCUNE ligne refresh_tokens — seulement
        // un jeton de défi 2fa_pending (5 min), accepté uniquement par
        // POST /api/users/login/2fa (rejeté partout ailleurs par JwtAuthenticationFilter).
        if (twoFactorService.isEnabled(refreshedUser.id!!)) {
            val (twoFactorToken, _) = jwtTokenProvider.generateTwoFactorPendingToken(refreshedUser.id!!)
            logger.debug("Défi 2FA émis (userId={})", refreshedUser.id)
            return ResponseEntity.ok(AuthResponse.twoFactorChallenge(twoFactorToken))
        }

        // Récupérer les informations du device
        val userAgent = httpRequest.getHeader("User-Agent") ?: "Unknown"

        val accessToken = jwtTokenProvider.generateToken(user)
        val refreshToken = refreshTokenService.createRefreshTokenWithDeviceInfo(
            userId = user.id!!,
            userAgent = userAgent,
            ipAddress = clientIp
        )

        logger.debug("Connexion réussie (userId={})", refreshedUser.id)

        val response = AuthResponse.fromUser(
            user = refreshedUser,
            accessToken = accessToken,
            refreshToken = refreshToken,
            message = "Connexion réussie"
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Étape 2 du login pour les comptes 2FA (S9b) : transforme un défi 2FA en vrais tokens.
     *
     * Endpoint PUBLIC (pas de Bearer) : l'authentification repose sur le jeton
     * 2fa_pending (signé, 5 min, type vérifié) + un code TOTP courant OU un code
     * de récupération non utilisé (usage unique strict).
     *
     * Anti-brute-force : 5 tentatives max par jti (compteur RateLimitFilter),
     * puis le jeton est invalidé — il faut repasser par /login (lui-même rate-limité).
     * L'IP est aussi couverte par le bucket login du RateLimitFilter.
     *
     * L'émission finale passe par le chemin EXISTANT (generateToken +
     * createRefreshTokenWithDeviceInfo) : le plafond S8b de 5 sessions s'applique
     * automatiquement. Le device est capturé sur CETTE requête.
     */
    @Operation(
        summary = "Vérification 2FA du login",
        description = "Valide le code TOTP (ou un code de récupération) et émet les tokens définitifs"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Code valide, tokens émis"),
            ApiResponse(responseCode = "401", description = "Jeton 2FA invalide/expiré/épuisé ou code incorrect")
        ]
    )
    @PostMapping("/login/2fa")
    fun loginTwoFactor(
        @Valid @RequestBody request: TwoFactorLoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val clientIp = getClientIpAddress(httpRequest)

        // 1. Valider le jeton de défi LUI-MÊME (signature + type=2fa_pending + non expiré).
        //    Tout autre token (access, refresh, expiré, malformé) -> 401.
        val pending = jwtTokenProvider.validateTwoFactorPendingToken(request.twoFactorToken)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Jeton de vérification invalide ou expiré. Veuillez vous reconnecter."))
        val (userId, jti) = pending

        // 2. Compteur anti-brute-force par jti : 5 tentatives max sur la vie du jeton (5 min)
        if (!rateLimitFilter.registerTwoFactorAttempt(jti)) {
            logger.warn("Jeton 2FA épuisé après 5 tentatives (userId={})", userId)
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Trop de tentatives. Veuillez vous reconnecter."))
        }

        // 3. Valider le code : TOTP courant (fenêtre ±1) OU code de récupération non utilisé
        if (!twoFactorService.verifyLoginCode(userId, request.code)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Code de vérification incorrect."))
        }

        // 4. Code valide : invalider le jeton de défi (usage unique) puis émettre les
        //    vrais tokens par le chemin EXISTANT (plafond S8b appliqué automatiquement)
        rateLimitFilter.consumeTwoFactorChallenge(jti)

        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Utilisateur non trouvé."))

        if (!user.isActive) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(AuthResponse.error("Compte désactivé"))
        }

        val userAgent = httpRequest.getHeader("User-Agent") ?: "Unknown"
        val accessToken = jwtTokenProvider.generateToken(user)
        val refreshToken = refreshTokenService.createRefreshTokenWithDeviceInfo(
            userId = user.id!!,
            userAgent = userAgent,
            ipAddress = clientIp
        )

        logger.debug("Connexion 2FA réussie (userId={})", user.id)

        return ResponseEntity.ok(
            AuthResponse.fromUser(
                user = user,
                accessToken = accessToken,
                refreshToken = refreshToken,
                message = "Connexion réussie"
            )
        )
    }
    
    @Operation(
        summary = "Demander une réinitialisation de mot de passe",
        description = "Envoie un email avec un lien de réinitialisation de mot de passe"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Email de réinitialisation envoyé avec succès"),
            ApiResponse(responseCode = "400", description = "Email invalide ou compte non trouvé"),
            ApiResponse(responseCode = "500", description = "Erreur lors de l'envoi de l'email")
        ]
    )
    @PostMapping("/forgot-password")
    fun requestPasswordReset(
        @Valid @RequestBody request: ForgotPasswordRequest
    ): ResponseEntity<ForgotPasswordResponse> {
        println("🔵 [UserController] === DEMANDE RÉINITIALISATION MOT DE PASSE ===")
        println("🔵 [UserController] Email: ${request.email}")

        return try {
            val result = passwordResetService.requestPasswordReset(request.email)

            when (result) {
                is EmailSendResult.Success -> ResponseEntity.ok(ForgotPasswordResponse(
                    success = true,
                    message = "Un email de réinitialisation de mot de passe a été envoyé à votre adresse.",
                    email = request.email
                ))
                is EmailSendResult.DevMode -> ResponseEntity.ok(ForgotPasswordResponse(
                    success = true,
                    message = "Mode développement : consultez les logs pour le token de réinitialisation.",
                    email = request.email
                ))
                is EmailSendResult.DevRedirect -> ResponseEntity.ok(ForgotPasswordResponse(
                    success = true,
                    message = "Mode développement : email de réinitialisation envoyé à ${result.redirectEmail}.",
                    email = request.email
                ))
                else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ForgotPasswordResponse(
                    success = false,
                    message = "Erreur lors de l'envoi de l'email de réinitialisation."
                ))
            }
        } catch (e: IllegalArgumentException) {
            println("❌ [UserController] Erreur lors de la demande de réinitialisation: ${e.message}")
            ResponseEntity.badRequest().body(ForgotPasswordResponse(
                success = false,
                message = e.message ?: "Email invalide ou compte non trouvé"
            ))
        } catch (e: EmailSendException) {
            println("❌ [UserController] Erreur lors de l'envoi de l'email: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ForgotPasswordResponse(
                success = false,
                message = "Erreur lors de l'envoi de l'email de réinitialisation. Veuillez réessayer."
            ))
        } catch (e: Exception) {
            println("❌ [UserController] Erreur inattendue lors de la demande de réinitialisation: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ForgotPasswordResponse(
                success = false,
                message = "Une erreur inattendue s'est produite."
            ))
        }
    }

    @Operation(
        summary = "Réinitialiser le mot de passe",
        description = "Réinitialise le mot de passe de l'utilisateur avec un token valide"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
            ApiResponse(responseCode = "400", description = "Token invalide, expiré ou mots de passe non correspondants"),
            ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
        ]
    )
    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<ResetPasswordResponse> {
        println("🔵 [UserController] === RÉINITIALISATION MOT DE PASSE ===")
        println("🔵 [UserController] Token: ${request.token.take(10)}...")

        // Vérifier la correspondance des mots de passe
        if (request.newPassword != request.confirmPassword) {
            println("❌ [UserController] Les mots de passe ne correspondent pas")
            return ResponseEntity.badRequest().body(ResetPasswordResponse(
                success = false,
                message = "Les mots de passe ne correspondent pas."
            ))
        }

        return try {
            passwordResetService.resetPassword(request.token, request.newPassword)
            ResponseEntity.ok(ResetPasswordResponse(
                success = true,
                message = "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter."
            ))
        } catch (e: IllegalArgumentException) {
            println("❌ [UserController] Erreur lors de la réinitialisation: ${e.message}")
            ResponseEntity.badRequest().body(ResetPasswordResponse(
                success = false,
                message = e.message ?: "Token invalide, expiré ou erreur interne."
            ))
        } catch (e: Exception) {
            println("❌ [UserController] Erreur inattendue lors de la réinitialisation: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResetPasswordResponse(
                success = false,
                message = "Une erreur inattendue s'est produite lors de la réinitialisation du mot de passe."
            ))
        }
    }

    /**
     * Extrait l'adresse IP réelle du client (prenant en compte les proxies).
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }
        return request.remoteAddr ?: "unknown"
    }

    /**
     * Récupère le profil de l'utilisateur authentifié.
     * 
     * NOTE IMPORTANTE : Cette méthode récupère les données fraîches depuis la base de données
     * pour garantir que les informations modifiables (avatarUrl, isVerified, etc.) sont à jour.
     * L'objet User du SecurityContext (créé depuis le JWT) ne contient pas ces données dynamiques.
     */
    @Operation(
        summary = "Profil utilisateur",
        description = "Retourne les informations du compte de l'utilisateur authentifié"
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(): ResponseEntity<UserResponse> {
        // Récupérer l'ID depuis le token JWT (via SecurityContext)
        val authenticatedUser = getAuthenticatedUser()
        
        // Récupérer les données fraîches depuis la base de données
        // Cela garantit que les champs modifiables (avatarUrl, isVerified, etc.) sont à jour
        val freshUser = userRepository.findById(authenticatedUser.id!!)
            .orElseThrow { IllegalStateException("Utilisateur non trouvé en base de données") }
        
        // Vue PROPRIÉTAIRE : il voit toujours son propre numéro (V045)
        return ResponseEntity.ok(UserResponse.fromEntity(freshUser, includePrivatePhone = true))
    }

    /**
     * Met à jour le profil de l'utilisateur authentifié.
     * 
     * Mise à jour partielle : seuls les champs non-null dans la requête seront mis à jour.
     */
    @Operation(
        summary = "Mettre à jour le profil",
        description = "Met à jour les informations du profil de l'utilisateur authentifié. Seuls les champs fournis seront modifiés."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
            ApiResponse(responseCode = "400", description = "Données invalides"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun updateCurrentUser(
        @Parameter(description = "Données du profil à mettre à jour", required = true)
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val authenticatedUser = getAuthenticatedUser()
        val updatedUser = userService.updateUserProfile(authenticatedUser.id!!, request)
        // Vue PROPRIÉTAIRE : includePrivatePhone=true (V045)
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser, includePrivatePhone = true))
    }

    /**
     * Rafraîchit un access token avec un refresh token.
     */
    @Operation(
        summary = "Rafraîchir le token",
        description = "Génère un nouveau access token et refresh token à partir d'un refresh token valide"
    )
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> {
        // Valider le refresh token et récupérer l'utilisateur AVANT de le révoquer
        val refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Refresh token invalide ou expiré"))
        
        val user = userRepository.findById(refreshToken.userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }
        
        // Maintenant on peut révoquer l'ancien token et créer les nouveaux
        val (newAccessToken, newRefreshToken) = refreshTokenService.rotateRefreshToken(request.refreshToken)
        
        return ResponseEntity.ok(AuthResponse.fromUser(
            user = user,
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            message = "Token rafraîchi avec succès"
        ))
    }

    /**
     * Déconnecte un utilisateur en révoquant son refresh token.
     */
    @Operation(
        summary = "Déconnexion",
        description = "Révoque le refresh token pour déconnecter l'utilisateur"
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestBody request: LogoutRequest): ResponseEntity<Void> {
        refreshTokenService.revokeRefreshToken(request.refreshToken)
        return ResponseEntity.noContent().build()
    }

    /**
     * Récupère la langue préférée de l'utilisateur authentifié.
     * Phase 3 - Fonctionnalité Langue
     */
    @Operation(
        summary = "Langue préférée",
        description = "Retourne la langue préférée de l'utilisateur authentifié"
    )
    @GetMapping("/me/language")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUserLanguage(): ResponseEntity<Map<String, String>> {
        val user = getAuthenticatedUser()
        return ResponseEntity.ok(mapOf("language" to (user.preferredLanguage ?: "fr")))
    }

    /**
     * Met à jour la langue préférée de l'utilisateur authentifié.
     * Phase 3 - Fonctionnalité Langue
     */
    @Operation(
        summary = "Mettre à jour la langue préférée",
        description = "Met à jour la langue préférée de l'utilisateur authentifié. Langues supportées : fr, en, es, de, ar"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Langue mise à jour avec succès"),
            ApiResponse(responseCode = "400", description = "Langue non supportée"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PutMapping("/me/language")
    @PreAuthorize("isAuthenticated()")
    fun updateCurrentUserLanguage(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, String>> {
        val user = getAuthenticatedUser()
        val languageCode = request["language"] ?: throw IllegalArgumentException("Le champ 'language' est requis")
        
        // Valider que la langue est supportée
        val supportedLanguages = setOf("fr", "en", "es", "de", "ar")
        if (!supportedLanguages.contains(languageCode)) {
            return ResponseEntity.badRequest().body(mapOf(
                "error" to "Langue non supportée. Langues supportées : fr, en, es, de, ar"
            ))
        }
        
        // Mettre à jour la langue
        user.preferredLanguage = languageCode
        userRepository.save(user)
        
        return ResponseEntity.ok(mapOf(
            "message" to "Langue mise à jour avec succès",
            "language" to languageCode
        ))
    }

    /**
     * Recherche des utilisateurs pour les mentions @.
     */
    @Operation(
        summary = "Rechercher des utilisateurs",
        description = "Recherche des utilisateurs par nom, prénom ou email (pour les mentions @)"
    )
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    fun searchUsers(
        @Parameter(description = "Terme de recherche", required = true)
        @RequestParam query: String
    ): ResponseEntity<List<UserResponse>> {
        if (query.length < 2) {
            return ResponseEntity.ok(emptyList())
        }
        val users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            firstName = query,
            lastName = query,
            email = query
        ).take(10) // Limiter à 10 résultats
        return ResponseEntity.ok(users.map { UserResponse.fromEntity(it) })
    }

    /**
     * Met à jour l'avatar d'un utilisateur.
     */
    @Operation(
        summary = "Mettre à jour l'avatar",
        description = "Met à jour la photo de profil de l'utilisateur authentifié"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Avatar mis à jour avec succès"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Non authentifié"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Accès refusé - Vous ne pouvez modifier que votre propre avatar"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utilisateur introuvable"
            )
        ]
    )
    @PatchMapping("/{userId}/avatar")
    @PreAuthorize("isAuthenticated()")
    fun updateUserAvatar(
        @Parameter(description = "ID de l'utilisateur", required = true)
        @PathVariable userId: String,
        @Parameter(description = "URL de l'avatar", required = true)
        @RequestBody request: UpdateAvatarRequest
    ): ResponseEntity<UserResponse> {
        val authenticatedUser = getAuthenticatedUser()
        
        // Vérifier que l'utilisateur modifie son propre avatar
        if (authenticatedUser.id != userId) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        }
        
        val updatedUser = userService.updateUserAvatar(userId, request.avatarUrl)
        // Vue PROPRIÉTAIRE : includePrivatePhone=true (V045)
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser, includePrivatePhone = true))
    }

    // ========== SÉCURITÉ ==========

    /**
     * Change le mot de passe de l'utilisateur authentifié.
     */
    @Operation(
        summary = "Changer le mot de passe",
        description = "Change le mot de passe de l'utilisateur authentifié"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Mot de passe changé avec succès"),
            ApiResponse(responseCode = "400", description = "Mot de passe actuel incorrect ou nouveau mot de passe invalide"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ChangePasswordResponse> {
        // Vérifier que les mots de passe correspondent
        if (request.newPassword != request.confirmPassword) {
            return ResponseEntity.badRequest().body(
                ChangePasswordResponse(
                    success = false,
                    message = "Les mots de passe ne correspondent pas"
                )
            )
        }
        
        val user = getAuthenticatedUser()
        
        try {
            userService.changePassword(user.id!!, request.currentPassword, request.newPassword)
            
            // Révoquer tous les autres tokens (sécurité)
            refreshTokenService.revokeAllUserTokens(user.id!!)
            
            return ResponseEntity.ok(
                ChangePasswordResponse(
                    success = true,
                    message = "Mot de passe changé avec succès. Veuillez vous reconnecter."
                )
            )
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(
                ChangePasswordResponse(
                    success = false,
                    message = e.message ?: "Erreur lors du changement de mot de passe"
                )
            )
        }
    }

    /**
     * Récupère toutes les sessions actives de l'utilisateur.
     * 
     * @param currentToken Le refresh token actuel (optionnel, pour marquer la session courante)
     */
    @Operation(
        summary = "Sessions actives",
        description = "Récupère la liste de toutes les sessions actives de l'utilisateur"
    )
    @GetMapping("/me/sessions")
    @PreAuthorize("isAuthenticated()")
    fun getActiveSessions(
        @RequestHeader("X-Refresh-Token", required = false) currentToken: String?
    ): ResponseEntity<SessionsListResponse> {
        val user = getAuthenticatedUser()
        val sessions = refreshTokenService.getActiveSessions(user.id!!)
        
        // Récupérer l'ID du token actuel si fourni
        val currentTokenId = currentToken?.let { refreshTokenService.getTokenId(it) }
        
        return ResponseEntity.ok(
            SessionsListResponse(
                sessions = sessions.map { SessionResponse.fromEntity(it, currentTokenId) },
                totalCount = sessions.size,
                currentSessionId = currentTokenId
            )
        )
    }

    /**
     * Révoque une session spécifique.
     */
    @Operation(
        summary = "Révoquer une session",
        description = "Révoque (déconnecte) une session spécifique par son ID"
    )
    @DeleteMapping("/me/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    fun revokeSession(
        @PathVariable sessionId: Long
    ): ResponseEntity<RevokeSessionResponse> {
        val user = getAuthenticatedUser()
        val success = refreshTokenService.revokeTokenById(sessionId, user.id!!)
        
        return if (success) {
            ResponseEntity.ok(
                RevokeSessionResponse(
                    success = true,
                    message = "Session révoquée avec succès"
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                RevokeSessionResponse(
                    success = false,
                    message = "Session non trouvée ou non autorisée",
                    revokedCount = 0
                )
            )
        }
    }

    /**
     * Révoque toutes les autres sessions (logout all devices except current).
     * 
     * @param currentToken Le refresh token actuel (pour préserver la session courante)
     */
    @Operation(
        summary = "Révoquer toutes les autres sessions",
        description = "Révoque toutes les sessions sauf la session actuelle"
    )
    @DeleteMapping("/me/sessions")
    @PreAuthorize("isAuthenticated()")
    fun revokeAllOtherSessions(
        @RequestHeader("X-Refresh-Token", required = false) currentToken: String?
    ): ResponseEntity<RevokeSessionResponse> {
        val user = getAuthenticatedUser()
        
        // Récupérer l'ID du token actuel si fourni
        val currentTokenId = currentToken?.let { refreshTokenService.getTokenId(it) }
        
        val revokedCount = if (currentTokenId != null) {
            refreshTokenService.revokeAllOtherSessions(user.id!!, currentTokenId)
        } else {
            val count = refreshTokenService.countActiveSessions(user.id!!)
            refreshTokenService.revokeAllUserTokens(user.id!!)
            count
        }
        
        return ResponseEntity.ok(
            RevokeSessionResponse(
                success = true,
                message = "Toutes les autres sessions ont été révoquées",
                revokedCount = revokedCount
            )
        )
    }

    /**
     * Demande un changement d'email avec re-vérification (V040).
     *
     * NE bascule PAS l'email immédiatement : envoie un code de vérification
     * à la nouvelle adresse. L'email actif reste inchangé tant que le code
     * n'est pas confirmé via POST /me/email/confirm.
     */
    @Operation(
        summary = "Demander un changement d'email",
        description = "Envoie un code de vérification à la nouvelle adresse (nécessite le mot de passe). " +
                "L'email actif ne change qu'après confirmation du code."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Code de vérification envoyé à la nouvelle adresse"),
            ApiResponse(responseCode = "400", description = "Email invalide ou mot de passe incorrect"),
            ApiResponse(responseCode = "409", description = "Email déjà utilisé")
        ]
    )
    @PutMapping("/me/email")
    @PreAuthorize("isAuthenticated()")
    fun changeEmail(
        @Valid @RequestBody request: ChangeEmailRequest
    ): ResponseEntity<ChangeEmailResponse> {
        val user = getAuthenticatedUser()

        try {
            val sendResult = userService.requestEmailChange(user.id!!, request.newEmail, request.password)

            if (!sendResult.isSuccessful) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ChangeEmailResponse(
                        success = false,
                        message = "Erreur lors de l'envoi du code de vérification. Veuillez réessayer."
                    )
                )
            }

            return ResponseEntity.ok(
                ChangeEmailResponse(
                    success = true,
                    message = "Un code de vérification a été envoyé à votre nouvelle adresse. " +
                            "Saisissez-le pour confirmer le changement.",
                    newEmail = request.newEmail
                )
            )
        } catch (e: IllegalArgumentException) {
            val status = if (e.message?.contains("déjà utilisé") == true) {
                HttpStatus.CONFLICT
            } else {
                HttpStatus.BAD_REQUEST
            }
            return ResponseEntity.status(status).body(
                ChangeEmailResponse(
                    success = false,
                    message = e.message ?: "Erreur lors de la demande de changement d'email"
                )
            )
        }
    }

    /**
     * Confirme un changement d'email avec le code de vérification (V040).
     *
     * Endpoint dédié, isolé du flux de vérification d'inscription :
     * le token est validé contre le token stocké de l'utilisateur authentifié.
     */
    @Operation(
        summary = "Confirmer un changement d'email",
        description = "Valide le code reçu sur la nouvelle adresse et bascule l'email définitivement"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Email changé avec succès"),
            ApiResponse(responseCode = "400", description = "Code invalide, expiré ou aucune demande en attente"),
            ApiResponse(responseCode = "409", description = "Email pris entre-temps")
        ]
    )
    @PostMapping("/me/email/confirm")
    @PreAuthorize("isAuthenticated()")
    fun confirmEmailChange(
        @Valid @RequestBody request: ConfirmEmailChangeRequest
    ): ResponseEntity<ChangeEmailResponse> {
        val user = getAuthenticatedUser()

        try {
            val updatedUser = userService.confirmEmailChange(user.id!!, request.token)

            return ResponseEntity.ok(
                ChangeEmailResponse(
                    success = true,
                    message = "Email changé avec succès",
                    newEmail = updatedUser.email
                )
            )
        } catch (e: IllegalArgumentException) {
            val status = if (e.message?.contains("déjà utilisé") == true) {
                HttpStatus.CONFLICT
            } else {
                HttpStatus.BAD_REQUEST
            }
            return ResponseEntity.status(status).body(
                ChangeEmailResponse(
                    success = false,
                    message = e.message ?: "Erreur lors de la confirmation du changement d'email"
                )
            )
        }
    }

    /**
     * Change le téléphone de l'utilisateur authentifié.
     */
    @Operation(
        summary = "Changer le téléphone",
        description = "Change le numéro de téléphone de l'utilisateur authentifié (nécessite le mot de passe)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Téléphone changé avec succès"),
            ApiResponse(responseCode = "400", description = "Mot de passe incorrect")
        ]
    )
    @PutMapping("/me/phone")
    @PreAuthorize("isAuthenticated()")
    fun changePhone(
        @Valid @RequestBody request: ChangePhoneRequest
    ): ResponseEntity<ChangePhoneResponse> {
        val user = getAuthenticatedUser()
        
        try {
            val updatedUser = userService.changePhone(
                user.id!!, request.newPhone, request.phonePublic, request.password
            )

            return ResponseEntity.ok(
                ChangePhoneResponse(
                    success = true,
                    message = if (updatedUser.phoneNumber == null)
                        "Numéro de téléphone supprimé avec succès"
                    else
                        "Numéro de téléphone changé avec succès",
                    newPhone = updatedUser.phoneNumber,
                    phonePublic = updatedUser.phonePublic
                )
            )
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(
                ChangePhoneResponse(
                    success = false,
                    message = e.message ?: "Erreur lors du changement de téléphone"
                )
            )
        }
    }

    /**
     * Supprime définitivement le compte de l'utilisateur authentifié.
     * 
     * ⚠️ ATTENTION: Cette opération est irréversible.
     */
    @Operation(
        summary = "Supprimer le compte",
        description = "Supprime définitivement le compte utilisateur et toutes ses données. Cette opération est irréversible."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Compte supprimé avec succès"),
            ApiResponse(responseCode = "400", description = "Mot de passe incorrect ou confirmation manquante")
        ]
    )
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun deleteAccount(
        @Valid @RequestBody request: DeleteAccountRequest
    ): ResponseEntity<DeleteAccountResponse> {
        if (!request.confirmDeletion) {
            return ResponseEntity.badRequest().body(
                DeleteAccountResponse(
                    success = false,
                    message = "Veuillez confirmer la suppression de votre compte"
                )
            )
        }
        
        val user = getAuthenticatedUser()
        
        try {
            // Révoquer tous les tokens d'abord
            refreshTokenService.revokeAllUserTokens(user.id!!)
            
            // Supprimer le compte
            userService.deleteAccount(user.id!!, request.password)
            
            return ResponseEntity.ok(
                DeleteAccountResponse(
                    success = true,
                    message = "Votre compte a été supprimé avec succès"
                )
            )
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(
                DeleteAccountResponse(
                    success = false,
                    message = e.message ?: "Erreur lors de la suppression du compte"
                )
            )
        }
    }

    // ========== VÉRIFICATION D'EMAIL ==========

    /**
     * Demande l'envoi d'un nouveau code de vérification pour un email existant.
     * Utile pour les utilisateurs bloqués avec un email non vérifié.
     */
    @Operation(
        summary = "Demander un nouveau code de vérification",
        description = "Envoie un nouveau code de vérification pour un email existant sans nécessiter de connexion"
    )
    @PostMapping("/request-verification")
    fun requestVerification(
        @RequestParam email: String
    ): ResponseEntity<Map<String, String>> {
        try {
            // Vérifier que l'email existe
            val user = userRepository.findByEmail(email)
                ?: return ResponseEntity.badRequest().body(mapOf(
                    "error" to "Aucun compte trouvé avec cet email"
                ))

            // Vérifier que l'utilisateur n'est pas déjà vérifié
            if (user.emailVerified) {
                return ResponseEntity.badRequest().body(mapOf(
                    "error" to "Cet email est déjà vérifié"
                ))
            }

            // Générer et envoyer un nouveau code
            val token = emailVerificationService.generateVerificationToken()
            val result = emailVerificationService.sendVerificationEmail(user, token)

            return when (result) {
                is EmailSendResult.Success -> ResponseEntity.ok(mapOf(
                    "success" to "true",
                    "message" to "Un nouveau code de vérification a été envoyé à votre email",
                    "email" to email
                ))
                is EmailSendResult.DevMode -> ResponseEntity.ok(mapOf(
                    "success" to "true",
                    "message" to "Mode développement : consultez les logs pour le code de vérification",
                    "email" to email,
                    "token" to result.token
                ))
                is EmailSendResult.DevRedirect -> ResponseEntity.ok(mapOf(
                    "success" to "true",
                    "message" to "Mode développement : email envoyé à ${result.redirectEmail}",
                    "email" to email
                ))
                else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                    "error" to "Erreur lors de l'envoi de l'email de vérification"
                ))
            }
        } catch (e: Exception) {
            println("❌ [UserController] Erreur lors de la demande de vérification: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "error" to "Une erreur inattendue s'est produite"
            ))
        }
    }

    /**
     * Vérifie un token de vérification d'email.
     */
    @Operation(
        summary = "Vérifier l'email",
        description = "Vérifie un token de vérification d'email et active le compte utilisateur"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Email vérifié avec succès"),
            ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
        ]
    )
    @PostMapping("/verify-email")
    fun verifyEmail(
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, String>> {
        val token = request["token"] ?: return ResponseEntity.badRequest().body(
            mapOf("error" to "Token manquant")
        )

        val user = emailVerificationService.verifyToken(token)
        return if (user != null) {
            ResponseEntity.ok(mapOf(
                "success" to "true",
                "message" to "Email vérifié avec succès",
                "email" to user.email
            ))
        } else {
            ResponseEntity.badRequest().body(mapOf(
                "error" to "Token invalide ou expiré"
            ))
        }
    }

    /**
     * Renvoie un email de vérification.
     */
    @Operation(
        summary = "Renvoyer l'email de vérification",
        description = "Renvoie un email de vérification à l'utilisateur authentifié"
    )
    @PostMapping("/me/resend-verification")
    @PreAuthorize("isAuthenticated()")
    fun resendVerificationEmail(): ResponseEntity<Map<String, String>> {
        val user = getAuthenticatedUser()
        
        val success = emailVerificationService.resendVerificationEmail(user.id!!)
        return if (success) {
            ResponseEntity.ok(mapOf(
                "success" to "true",
                "message" to "Email de vérification renvoyé avec succès"
            ))
        } else {
            ResponseEntity.badRequest().body(mapOf(
                "error" to "Impossible de renvoyer l'email de vérification. Votre email est peut-être déjà vérifié."
            ))
        }
    }

    // ========== GESTION DES ERREURS ==========

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf(
                "error" to "Unauthorized",
                "message" to (ex.message ?: "Authentication required")
            ))
    }
}