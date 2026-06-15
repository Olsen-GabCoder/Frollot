package com.frollot.controller

import com.frollot.dto.TwoFactorConfirmRequest
import com.frollot.dto.TwoFactorConfirmResponse
import com.frollot.dto.TwoFactorDisableRequest
import com.frollot.dto.TwoFactorDisableResponse
import com.frollot.dto.TwoFactorErrorResponse
import com.frollot.dto.TwoFactorRegenerateRequest
import com.frollot.dto.TwoFactorSetupResponse
import com.frollot.dto.TwoFactorStatusResponse
import com.frollot.model.User
import com.frollot.repository.UserRepository
import com.frollot.service.TwoFactorService
import com.frollot.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Endpoints d'activation de la 2FA TOTP (S9a).
 *
 * Couvert par anyRequest().authenticated() (SecurityConfig) : aucun matcher dédié requis.
 *
 * PÉRIMÈTRE S9a : setup / confirm / status uniquement.
 * - L'interception du login (jeton 2fa_pending) est S9b.
 * - La désactivation (password + TOTP, purge) est S9c.
 */
@RestController
@RequestMapping("/api/users/me/2fa")
@Tag(
    name = "Double authentification (2FA TOTP)",
    description = "Activation en deux temps de la 2FA TOTP (RFC 6238)"
)
class TwoFactorController(
    private val twoFactorService: TwoFactorService,
    private val userService: UserService,
    private val userRepository: UserRepository
) {

    private fun getAuthenticatedUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifié")
        }
        return authentication.principal as User
    }

    /**
     * Étape 1 : (re)génère un secret TOTP (enabled=false tant que non confirmé).
     *
     * Seule réponse de toute l'API où le secret circule en clair.
     * Un nouveau setup écrase un setup non confirmé ; refus si déjà activée.
     */
    @Operation(
        summary = "Générer un secret 2FA",
        description = "Génère (ou régénère) le secret TOTP. La 2FA reste inactive jusqu'à confirmation d'un premier code."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Secret généré (2FA non encore active)"),
            ApiResponse(responseCode = "400", description = "2FA déjà activée"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    fun setup(): ResponseEntity<*> {
        val user = getAuthenticatedUser()
        return try {
            val result = twoFactorService.setup(user.id!!, user.email)
            ResponseEntity.ok(TwoFactorSetupResponse(secret = result.secret, otpauthUri = result.otpauthUri))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Opération refusée"))
        }
    }

    /**
     * Étape 2 : active la 2FA après preuve d'un premier code TOTP valide.
     *
     * Retourne les 10 codes de récupération EN CLAIR — une seule fois.
     */
    @Operation(
        summary = "Confirmer l'activation 2FA",
        description = "Valide un premier code TOTP ; si correct, active la 2FA et retourne les codes de récupération (affichés une seule fois)."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "2FA activée, codes de récupération retournés"),
            ApiResponse(responseCode = "400", description = "Code invalide, aucun setup en cours, ou 2FA déjà activée"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    fun confirm(@Valid @RequestBody request: TwoFactorConfirmRequest): ResponseEntity<*> {
        val user = getAuthenticatedUser()
        return try {
            val recoveryCodes = twoFactorService.confirm(user.id!!, request.code)
            ResponseEntity.ok(
                TwoFactorConfirmResponse(
                    success = true,
                    message = "Double authentification activée. Conservez vos codes de récupération en lieu sûr : ils ne seront plus jamais affichés.",
                    recoveryCodes = recoveryCodes
                )
            )
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Opération refusée"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Code de vérification invalide"))
        }
    }

    /**
     * Statut 2FA de l'utilisateur courant (jamais le secret).
     */
    @Operation(
        summary = "Statut 2FA",
        description = "Indique si la double authentification est activée pour l'utilisateur courant."
    )
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    fun status(): ResponseEntity<TwoFactorStatusResponse> {
        val user = getAuthenticatedUser()
        return ResponseEntity.ok(TwoFactorStatusResponse(enabled = twoFactorService.isEnabled(user.id!!)))
    }

    /**
     * Désactivation de la 2FA (S9c) — endpoint AUTHENTIFIÉ (access token normal,
     * modèle opposé à /login/2fa qui était public).
     *
     * Action sensible : exige les DEUX preuves — mot de passe ET code (TOTP courant
     * OU code de récupération non utilisé). Le mot de passe est vérifié EN PREMIER :
     * un mot de passe faux ne consomme jamais de code de récupération.
     * À la réussite : purge complète (secret + tous les codes), état identique à
     * un compte n'ayant jamais eu de 2FA.
     */
    @Operation(
        summary = "Désactiver la 2FA",
        description = "Désactive la double authentification (mot de passe + code TOTP ou de récupération requis) et purge le secret et les codes de récupération."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "2FA désactivée, secret et codes purgés"),
            ApiResponse(responseCode = "400", description = "Mot de passe incorrect, code invalide ou 2FA non activée"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    fun disable(@Valid @RequestBody request: TwoFactorDisableRequest): ResponseEntity<*> {
        val user = getAuthenticatedUser()

        // 1. Mot de passe D'ABORD — ne jamais évaluer (et consommer) un code
        //    de récupération sur la foi d'un mot de passe faux.
        //    PIÈGE : le principal est reconstruit depuis les claims JWT (sans
        //    passwordHash) — recharger depuis la BDD (même pattern que le login).
        val dbUser = userRepository.findById(user.id!!).orElse(user)
        if (!userService.checkPassword(request.password, dbUser.passwordHash)) {
            return ResponseEntity.badRequest()
                .body(TwoFactorErrorResponse(message = "Mot de passe incorrect."))
        }

        return try {
            twoFactorService.disable(user.id!!, request.code)
            ResponseEntity.ok(
                TwoFactorDisableResponse(
                    success = true,
                    message = "Double authentification désactivée. Votre secret et vos codes de récupération ont été supprimés."
                )
            )
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Opération refusée"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Code de vérification invalide"))
        }
    }

    /**
     * Régénération des codes de récupération (S9c) — même niveau de preuve que
     * la désactivation (mot de passe + code TOTP ou de récupération) : remettre
     * la main sur un lot frais de codes est équivalent à détenir la 2FA.
     *
     * Invalide TOUS les anciens codes (utilisés ou non) ; le secret TOTP n'est
     * pas touché. Les 10 nouveaux codes sont montrés UNE seule fois.
     */
    @Operation(
        summary = "Régénérer les codes de récupération",
        description = "Invalide tous les anciens codes et génère 10 nouveaux codes (affichés une seule fois). La 2FA reste active, même secret TOTP."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "10 nouveaux codes générés, anciens invalidés"),
            ApiResponse(responseCode = "400", description = "Mot de passe incorrect, code invalide ou 2FA non activée"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    @PostMapping("/recovery-codes/regenerate")
    @PreAuthorize("isAuthenticated()")
    fun regenerateRecoveryCodes(@Valid @RequestBody request: TwoFactorRegenerateRequest): ResponseEntity<*> {
        val user = getAuthenticatedUser()

        // Mot de passe d'abord, rechargé depuis la BDD (mêmes raisons que disable)
        val dbUser = userRepository.findById(user.id!!).orElse(user)
        if (!userService.checkPassword(request.password, dbUser.passwordHash)) {
            return ResponseEntity.badRequest()
                .body(TwoFactorErrorResponse(message = "Mot de passe incorrect."))
        }

        return try {
            val recoveryCodes = twoFactorService.regenerateRecoveryCodes(user.id!!, request.code)
            ResponseEntity.ok(
                TwoFactorConfirmResponse(
                    success = true,
                    message = "Nouveaux codes de récupération générés. Les anciens codes ne sont plus valables. Conservez-les en lieu sûr : ils ne seront plus jamais affichés.",
                    recoveryCodes = recoveryCodes
                )
            )
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Opération refusée"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(TwoFactorErrorResponse(message = e.message ?: "Code de vérification invalide"))
        }
    }
}
