package com.frollot.service

import com.frollot.model.TwoFactorRecoveryCode
import com.frollot.model.UserTwoFactor
import com.frollot.repository.TwoFactorRecoveryCodeRepository
import com.frollot.repository.UserTwoFactorRepository
import com.frollot.security.TotpEncryptionService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Service 2FA TOTP — RFC 6238 (S9a).
 *
 * - Secret : 20 octets aléatoires (160 bits), encodés Base32 (RFC 4648),
 *   chiffrés AES-256-GCM au repos (TotpEncryptionService).
 * - Code : HMAC-SHA1, pas de 30 s, 6 chiffres, tolérance ±1 pas (décalage d'horloge).
 * - Codes de récupération : 10 codes XXXX-XXXX (alphabet sans ambiguïté O/0/I/1,
 *   8 caractères × 5 bits = 40 bits), hachés BCrypt, montrés UNE seule fois.
 *
 * Base32 implémenté à la main (~40 lignes) : le JDK n'a pas de Base32 natif et
 * une dépendance (commons-codec) pour 2 fonctions ne se justifie pas.
 *
 * PÉRIMÈTRE S9a : activation uniquement. Le login (interception, jeton 2fa_pending)
 * est S9b ; la désactivation (password + TOTP, purge) est S9c.
 */
@Service
class TwoFactorService(
    private val userTwoFactorRepository: UserTwoFactorRepository,
    private val recoveryCodeRepository: TwoFactorRecoveryCodeRepository,
    private val totpEncryptionService: TotpEncryptionService,
    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        private const val SECRET_LENGTH_BYTES = 20      // 160 bits, recommandation RFC 4226
        private const val TIME_STEP_SECONDS = 30L
        private const val CODE_DIGITS = 6
        private const val WINDOW_TOLERANCE = 1          // ±1 pas de 30 s

        private const val RECOVERY_CODE_COUNT = 10
        // 32 caractères (5 bits), sans O/0/I/1 pour éviter toute ambiguïté de lecture
        private const val RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        private const val ISSUER = "Frollot"
    }

    private val secureRandom = SecureRandom()

    data class SetupResult(val secret: String, val otpauthUri: String)

    /**
     * (Re)génère un secret TOTP pour l'utilisateur — étape 1 de l'activation.
     *
     * - Refuse si la 2FA est déjà activée (il faut la désactiver d'abord — S9c).
     * - Écrase une ligne non confirmée existante (sémantique S4 pending_email).
     * - Seul endroit où le secret circule en clair : la réponse de setup,
     *   tant que enabled=false.
     */
    @Transactional
    fun setup(userId: String, email: String): SetupResult {
        val existing = userTwoFactorRepository.findById(userId).orElse(null)
        if (existing != null && existing.enabled) {
            throw IllegalStateException(
                "La double authentification est déjà activée. Désactivez-la avant de générer un nouveau secret."
            )
        }

        val secretBytes = ByteArray(SECRET_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val secret = base32Encode(secretBytes)
        val encrypted = totpEncryptionService.encrypt(secret)

        if (existing != null) {
            // Écrasement du setup non confirmé précédent
            existing.secretEncrypted = encrypted
            existing.createdAt = LocalDateTime.now()
            existing.confirmedAt = null
            userTwoFactorRepository.save(existing)
        } else {
            userTwoFactorRepository.save(
                UserTwoFactor(userId = userId, secretEncrypted = encrypted, enabled = false)
            )
        }

        // Hygiène : aucun code de récupération ne doit exister tant que la 2FA
        // n'est pas confirmée (résidus d'états antérieurs)
        recoveryCodeRepository.deleteAllByUserId(userId)

        return SetupResult(secret = secret, otpauthUri = buildOtpauthUri(email, secret))
    }

    /**
     * Confirme l'activation avec un premier code TOTP valide — étape 2.
     *
     * @return les 10 codes de récupération EN CLAIR (seule et unique exposition ;
     *         stockés hachés BCrypt)
     * @throws IllegalStateException si aucun setup en cours ou déjà activée
     * @throws IllegalArgumentException si le code est invalide
     */
    @Transactional
    fun confirm(userId: String, code: String): List<String> {
        val twoFactor = userTwoFactorRepository.findById(userId).orElse(null)
            ?: throw IllegalStateException("Aucune configuration 2FA en cours. Lancez d'abord la génération du secret.")

        if (twoFactor.enabled) {
            throw IllegalStateException("La double authentification est déjà activée.")
        }

        val secret = totpEncryptionService.decrypt(twoFactor.secretEncrypted, userId)
        if (!verifyCode(secret, code)) {
            throw IllegalArgumentException("Code de vérification invalide. Vérifiez votre application d'authentification.")
        }

        twoFactor.enabled = true
        twoFactor.confirmedAt = LocalDateTime.now()
        userTwoFactorRepository.save(twoFactor)

        // Génération des codes de récupération (remplace tout lot antérieur)
        recoveryCodeRepository.deleteAllByUserId(userId)
        val plainCodes = (1..RECOVERY_CODE_COUNT).map { generateRecoveryCode() }
        recoveryCodeRepository.saveAll(
            plainCodes.map { TwoFactorRecoveryCode(userId = userId, codeHash = passwordEncoder.encode(it)) }
        )

        return plainCodes
    }

    /**
     * Statut 2FA de l'utilisateur (jamais le secret).
     */
    @Transactional(readOnly = true)
    fun isEnabled(userId: String): Boolean {
        return userTwoFactorRepository.findById(userId).orElse(null)?.enabled ?: false
    }

    /**
     * Vérifie le code de l'étape 2 du login (S9b) : TOTP courant OU code de récupération.
     *
     * - TOTP : 6 chiffres, fenêtre ±1 pas de 30 s.
     * - Code de récupération : format XXXX-XXXX, comparé aux hashs BCrypt non utilisés ;
     *   en cas de succès, le code est CONSOMMÉ (used_at posé, usage unique strict).
     *
     * @return true si le code est valide (et consommé s'il s'agit d'un code de récupération)
     */
    @Transactional
    fun verifyLoginCode(userId: String, code: String): Boolean {
        val twoFactor = userTwoFactorRepository.findById(userId).orElse(null) ?: return false
        if (!twoFactor.enabled) return false

        val normalized = code.trim()

        // 1. Tentative TOTP (6 chiffres)
        if (normalized.replace(" ", "").matches(Regex("\\d{$CODE_DIGITS}"))) {
            val secret = totpEncryptionService.decrypt(twoFactor.secretEncrypted, userId)
            return verifyCode(secret, normalized)
        }

        // 2. Tentative code de récupération (insensible à la casse, tiret optionnel)
        val candidate = normalized.uppercase().replace(" ", "").let {
            if (it.length == 8 && !it.contains('-')) it.substring(0, 4) + "-" + it.substring(4) else it
        }
        if (!candidate.matches(Regex("[A-Z2-9]{4}-[A-Z2-9]{4}"))) return false

        val unused = recoveryCodeRepository.findAllByUserId(userId).filter { it.usedAt == null }
        val match = unused.firstOrNull { passwordEncoder.matches(candidate, it.codeHash) } ?: return false

        // Usage unique strict : consommer le code
        match.usedAt = LocalDateTime.now()
        recoveryCodeRepository.save(match)
        return true
    }

    /**
     * Vérifie un code TOTP à 6 chiffres contre un secret Base32,
     * avec tolérance de ±1 pas de 30 s. Comparaison en temps constant.
     */
    fun verifyCode(secretBase32: String, code: String): Boolean {
        val normalized = code.trim().replace(" ", "")
        if (!normalized.matches(Regex("\\d{$CODE_DIGITS}"))) return false

        val key = base32Decode(secretBase32)
        val currentStep = Instant.now().epochSecond / TIME_STEP_SECONDS

        var valid = false
        for (offset in -WINDOW_TOLERANCE..WINDOW_TOLERANCE) {
            val expected = totpCode(key, currentStep + offset)
            // Pas de court-circuit : on parcourt toute la fenêtre (temps constant)
            if (MessageDigest.isEqual(expected.toByteArray(), normalized.toByteArray())) {
                valid = true
            }
        }
        return valid
    }

    /**
     * Désactive la 2FA (S9c) — le mot de passe a déjà été vérifié par l'appelant.
     *
     * Ordre STRICT : vérifier le code AVANT de purger. Si le code est un code de
     * récupération, verifyLoginCode le consomme (used_at) — sans effet de bord
     * visible : la purge qui suit supprime tous les codes dans la MÊME transaction
     * (échec de purge -> rollback complet, 2FA intacte). Un code de récupération
     * DÉJÀ consommé est refusé (verifyLoginCode ne compare que les codes non utilisés).
     *
     * Purge EXPLICITE des deux tables (la cascade FK V044 ne joue qu'à la
     * suppression du user) : état final strictement identique à un compte
     * n'ayant jamais eu de 2FA.
     *
     * @throws IllegalStateException si la 2FA n'est pas activée
     * @throws IllegalArgumentException si le code est invalide
     */
    @Transactional
    fun disable(userId: String, code: String) {
        val twoFactor = userTwoFactorRepository.findById(userId).orElse(null)
        if (twoFactor == null || !twoFactor.enabled) {
            throw IllegalStateException("La double authentification n'est pas activée sur ce compte.")
        }

        if (!verifyLoginCode(userId, code)) {
            throw IllegalArgumentException("Code de vérification incorrect.")
        }

        recoveryCodeRepository.deleteAllByUserId(userId)
        userTwoFactorRepository.delete(twoFactor)
    }

    /**
     * Régénère le lot de 10 codes de récupération (S9c) — mot de passe déjà
     * vérifié par l'appelant, code (TOTP OU récupération) vérifié ici.
     *
     * Le secret TOTP n'est PAS touché (2FA reste active, même secret).
     * TOUS les anciens codes sont invalidés (supprimés, utilisés ou non).
     *
     * @return les 10 NOUVEAUX codes EN CLAIR (seule exposition, stockés BCrypt)
     * @throws IllegalStateException si la 2FA n'est pas activée
     * @throws IllegalArgumentException si le code est invalide
     */
    @Transactional
    fun regenerateRecoveryCodes(userId: String, code: String): List<String> {
        if (!isEnabled(userId)) {
            throw IllegalStateException("La double authentification n'est pas activée sur ce compte.")
        }

        if (!verifyLoginCode(userId, code)) {
            throw IllegalArgumentException("Code de vérification incorrect.")
        }

        recoveryCodeRepository.deleteAllByUserId(userId)
        val plainCodes = (1..RECOVERY_CODE_COUNT).map { generateRecoveryCode() }
        recoveryCodeRepository.saveAll(
            plainCodes.map { TwoFactorRecoveryCode(userId = userId, codeHash = passwordEncoder.encode(it)) }
        )
        return plainCodes
    }

    // ========== TOTP (RFC 6238 / HOTP RFC 4226) ==========

    private fun totpCode(key: ByteArray, timeStep: Long): String {
        val counterBytes = ByteBuffer.allocate(8).putLong(timeStep).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(counterBytes)

        // Troncature dynamique (RFC 4226 §5.3)
        val dynamicOffset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[dynamicOffset].toInt() and 0x7F) shl 24) or
            ((hash[dynamicOffset + 1].toInt() and 0xFF) shl 16) or
            ((hash[dynamicOffset + 2].toInt() and 0xFF) shl 8) or
            (hash[dynamicOffset + 3].toInt() and 0xFF)

        return String.format("%0${CODE_DIGITS}d", binary % 1_000_000)
    }

    // ========== Base32 (RFC 4648) ==========

    private fun base32Encode(data: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in data) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                sb.append(BASE32_ALPHABET[(buffer shr (bitsLeft - 5)) and 0x1F])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_ALPHABET[(buffer shl (5 - bitsLeft)) and 0x1F])
        }
        return sb.toString()
    }

    private fun base32Decode(encoded: String): ByteArray {
        val clean = encoded.trim().replace(" ", "").trimEnd('=').uppercase()
        val out = ByteArrayOutputStream()
        var buffer = 0
        var bitsLeft = 0
        for (c in clean) {
            val value = BASE32_ALPHABET.indexOf(c)
            require(value >= 0) { "Caractère Base32 invalide : $c" }
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                out.write((buffer shr (bitsLeft - 8)) and 0xFF)
                bitsLeft -= 8
            }
        }
        return out.toByteArray()
    }

    // ========== Codes de récupération ==========

    private fun generateRecoveryCode(): String {
        val chars = (1..8).map { RECOVERY_ALPHABET[secureRandom.nextInt(RECOVERY_ALPHABET.length)] }
        return chars.subList(0, 4).joinToString("") + "-" + chars.subList(4, 8).joinToString("")
    }

    // ========== URI otpauth (QR rendu côté client en S9d) ==========

    private fun buildOtpauthUri(email: String, secret: String): String {
        val label = URLEncoder.encode("$ISSUER:$email", Charsets.UTF_8).replace("+", "%20")
        val issuer = URLEncoder.encode(ISSUER, Charsets.UTF_8)
        return "otpauth://totp/$label?secret=$secret&issuer=$issuer&algorithm=SHA1&digits=$CODE_DIGITS&period=$TIME_STEP_SECONDS"
    }
}
