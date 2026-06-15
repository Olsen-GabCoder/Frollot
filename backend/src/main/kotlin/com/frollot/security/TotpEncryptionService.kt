package com.frollot.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Chiffrement au repos des secrets TOTP (S9a).
 *
 * AES-256-GCM via javax.crypto (zéro dépendance nouvelle).
 *
 * ## TOTP_ENCRYPTION_KEY
 * - Variable d'environnement OBLIGATOIRE, distincte de JWT_SECRET.
 * - Format : 32 octets encodés Base64 (`openssl rand -base64 32`).
 * - Validée au démarrage avec la même discipline que JWT_SECRET
 *   (JwtTokenProvider.validateJwtSecret) mais SANS fallback : le backend
 *   refuse de démarrer si elle est absente ou invalide, même en dev.
 *   Raison : un fallback par défaut rendrait tous les secrets TOTP
 *   déchiffrables par quiconque lit le code source.
 *
 * ## Scénario clé perdue/changée (garde obligatoire S9a)
 * Si la clé change, les secrets déjà chiffrés deviennent indéchiffrables :
 * [decrypt] lève alors une erreur EXPLICITE avec log clair
 * (« Secret 2FA indéchiffrable pour user X »), jamais un échec silencieux.
 * La rotation de clé n'est PAS implémentée (hors périmètre S9a) : en cas de
 * changement de clé, les utilisateurs concernés devront désactiver/réactiver
 * leur 2FA (via support tant que S9c n'existe pas).
 *
 * Format de sortie : Base64( IV 12 octets || ciphertext+tag GCM ).
 */
@Component
class TotpEncryptionService(
    @Value("\${app.security.totp.encryption-key:}")
    private val encodedKey: String
) {

    private val logger = LoggerFactory.getLogger(TotpEncryptionService::class.java)
    private val secureRandom = SecureRandom()
    private val key: SecretKeySpec

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }

    init {
        if (encodedKey.isBlank()) {
            throw IllegalStateException(
                """
                TOTP_ENCRYPTION_KEY est absente : le backend refuse de démarrer (S9a).

                Cette clé chiffre les secrets TOTP (2FA) au repos. AUCUN fallback n'est prévu.
                Pour la générer :
                    openssl rand -base64 32
                Puis la déclarer dans backend/.env :
                    TOTP_ENCRYPTION_KEY=<valeur générée>
                """.trimIndent()
            )
        }

        val keyBytes = try {
            Base64.getDecoder().decode(encodedKey.trim())
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException(
                "TOTP_ENCRYPTION_KEY n'est pas du Base64 valide. Générez-la avec : openssl rand -base64 32"
            )
        }

        if (keyBytes.size != 32) {
            throw IllegalStateException(
                "TOTP_ENCRYPTION_KEY doit décoder exactement 32 octets (AES-256). " +
                    "Longueur actuelle : ${keyBytes.size} octets. Générez-la avec : openssl rand -base64 32"
            )
        }

        key = SecretKeySpec(keyBytes, "AES")
        logger.info("TOTP_ENCRYPTION_KEY validée avec succès (AES-256-GCM)")
    }

    /**
     * Chiffre un secret TOTP (Base32 en clair) pour stockage en base.
     */
    fun encrypt(plainSecret: String): String {
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(plainSecret.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + ciphertext)
    }

    /**
     * Déchiffre un secret TOTP stocké en base.
     *
     * @param userId uniquement pour le log d'erreur explicite (garde clé perdue/changée)
     * @throws IllegalStateException si le déchiffrement échoue — jamais silencieux
     */
    fun decrypt(encryptedSecret: String, userId: String): String {
        try {
            val data = Base64.getDecoder().decode(encryptedSecret)
            require(data.size > IV_LENGTH_BYTES) { "Données chiffrées trop courtes" }
            val iv = data.copyOfRange(0, IV_LENGTH_BYTES)
            val ciphertext = data.copyOfRange(IV_LENGTH_BYTES, data.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            logger.error(
                "Secret 2FA indéchiffrable pour user {} : TOTP_ENCRYPTION_KEY a probablement été perdue ou changée. " +
                    "La rotation de clé n'est pas implémentée (S9a) : la 2FA de cet utilisateur doit être réinitialisée.",
                userId
            )
            throw IllegalStateException("Secret 2FA indéchiffrable pour l'utilisateur $userId (clé de chiffrement changée ?)")
        }
    }
}
