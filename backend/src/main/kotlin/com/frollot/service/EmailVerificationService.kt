package com.frollot.service

import com.frollot.model.User
import com.frollot.model.PendingRegistration
import com.frollot.repository.UserRepository
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.util.*
import javax.naming.NamingException
import kotlin.random.Random
import javax.naming.directory.Attribute
import javax.naming.directory.InitialDirContext
import java.util.Hashtable

/**
 * Résultat de l'envoi d'un email de vérification.
 */
sealed class EmailSendResult {
    /**
     * Email envoyé avec succès (mode production).
     */
    data class Success(val token: String, val message: String = "Email envoyé avec succès") : EmailSendResult()

    /**
     * Email non envoyé mais token sauvegardé et loggé (mode développement).
     */
    data class DevMode(val token: String, val message: String = "Token sauvegardé pour développement") : EmailSendResult()

    /**
     * Email envoyé sur adresse de redirection (mode développement).
     */
    data class DevRedirect(val token: String, val redirectEmail: String, val message: String = "Email envoyé sur adresse de test") : EmailSendResult()

    /**
     * Email envoyé réellement en développement (mode DEV_SEND).
     */
    data class DevSend(val token: String, val message: String = "Email envoyé en développement") : EmailSendResult()

    /**
     * Email désactivé par configuration.
     */
    data class Disabled(val reason: String = "Email désactivé") : EmailSendResult()

    /**
     * Échec de l'envoi d'email.
     */
    data class Failed(val error: String, val token: String? = null) : EmailSendResult()

    /**
     * Indique si l'opération a réussi (token créé/utilisable).
     */
    val isSuccessful: Boolean
        get() = this is Success || this is DevMode || this is DevRedirect || this is DevSend

    /**
     * Convertit le résultat en chaîne de statut pour le frontend.
     */
    fun toStatusString(): String {
        return when (this) {
            is Success -> "success"
            is DevMode -> "dev_mode"
            is DevRedirect -> "dev_redirect"
            is DevSend -> "dev_send"
            is Disabled -> "disabled"
            is Failed -> "failed"
        }
    }
}

/**
 * Service de vérification d'email.
 * 
 * Ce service gère :
 * - La validation de l'existence réelle d'une adresse email (vérification MX et SMTP)
 * - L'envoi d'emails de vérification avec code/token
 * - La vérification des tokens de vérification
 * 
 * Sécurité : Vérifie que l'email existe réellement avant de permettre l'inscription.
 */
@Service
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    private val emailConfigService: EmailConfigurationService,
    @Value("\${app.email.from}")
    private val fromEmail: String,
    @Value("\${app.email.verification.enabled:true}")
    private val verificationEnabled: Boolean,
    @Value("\${app.frontend.base-url:http://localhost:9090}")
    private val frontendBaseUrl: String
) {

    companion object {
        // Durée de validité du token de vérification (24 heures)
        private const val VERIFICATION_TOKEN_EXPIRY_HOURS = 24L
    }

    /**
     * Vérifie si une adresse email existe réellement.
     * 
     * Cette méthode effectue plusieurs vérifications :
     * 1. Validation du format (déjà fait par @Email)
     * 2. Vérification de l'existence du domaine (MX records)
     * 3. Vérification SMTP (optionnel, peut être désactivé si trop restrictif)
     * 
     * @param email L'adresse email à vérifier
     * @return true si l'email semble valide, false sinon
     */
    fun verifyEmailExists(email: String): Boolean {
        if (!verificationEnabled) {
            // Si la vérification est désactivée, on accepte tous les emails valides
            return isValidEmailFormat(email)
        }

        try {
            // 1. Validation du format
            if (!isValidEmailFormat(email)) {
                return false
            }

            // 2. Extraction du domaine
            val domain = email.substringAfter("@")
            if (domain.isBlank()) {
                return false
            }

            // 3. Vérification des enregistrements MX du domaine
            if (!hasMxRecords(domain)) {
                println("⚠️ [EmailVerification] Aucun enregistrement MX trouvé pour le domaine: $domain")
                return false
            }

            // 4. Vérification SMTP (optionnel, peut être désactivé si trop restrictif)
            // Note: Cette vérification peut être bloquée par certains serveurs SMTP
            // On la laisse optionnelle pour éviter les faux négatifs
            // val smtpValid = verifySmtp(email, domain)
            // if (!smtpValid) {
            //     println("⚠️ [EmailVerification] Vérification SMTP échouée pour: $email")
            //     return false
            // }

            println("✅ [EmailVerification] Email vérifié avec succès: $email")
            return true
        } catch (e: Exception) {
            println("❌ [EmailVerification] Erreur lors de la vérification de l'email $email: ${e.message}")
            e.printStackTrace()
            // En cas d'erreur, on refuse par sécurité
            return false
        }
    }

    /**
     * Vérifie le format d'une adresse email.
     */
    private fun isValidEmailFormat(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * Vérifie si un domaine possède des enregistrements MX (Mail Exchange).
     * 
     * Les enregistrements MX indiquent qu'un domaine peut recevoir des emails.
     * 
     * @param domain Le domaine à vérifier
     * @return true si le domaine a des enregistrements MX, false sinon
     */
    private fun hasMxRecords(domain: String): Boolean {
        return try {
            val env = Hashtable<String, String>().apply {
                put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
                put("java.naming.provider.url", "dns:")
            }
            
            val dirContext = InitialDirContext(env)
            val attributes = dirContext.getAttributes(domain, arrayOf("MX"))
            val mxAttribute: Attribute? = attributes.get("MX")
            
            mxAttribute != null && mxAttribute.size() > 0
        } catch (e: NamingException) {
            println("⚠️ [EmailVerification] Erreur DNS pour le domaine $domain: ${e.message}")
            false
        } catch (e: Exception) {
            println("⚠️ [EmailVerification] Erreur inattendue lors de la vérification MX pour $domain: ${e.message}")
            false
        }
    }

    /**
     * Génère un code OTP de vérification (6 chiffres) pour un utilisateur.
     *
     * @return Un code OTP à 6 chiffres (ex: "123456")
     */
    fun generateVerificationToken(): String {
        val otp = String.format("%06d", Random.nextInt(100000, 999999))
        println("🎯 [DEV MODE] OTP généré: $otp")
        return otp
    }

    /**
     * Envoie un email de vérification pour une pré-inscription.
     *
     * @param pending La pré-inscription en attente
     * @param token Le token de vérification
     * @return Le résultat de l'envoi d'email selon le mode effectif
     */
    @Transactional
    fun sendVerificationEmailForPendingRegistration(pending: PendingRegistration, token: String): EmailSendResult {
        val mode = emailConfigService.getEffectiveEmailMode()
        println("📧 [EmailVerification] Mode email effectif: $mode")

        return when (mode) {
            EmailConfigurationService.EmailMode.DISABLED -> {
                println("🚫 [EmailVerification] Email désactivé - Aucun envoi pour ${pending.email}")
                EmailSendResult.Disabled("Service d'email désactivé")
            }

            EmailConfigurationService.EmailMode.DEV_LOG -> {
                logDevToken(pending.email, token)
                EmailSendResult.DevMode(token)
            }

            EmailConfigurationService.EmailMode.DEV_REDIRECT -> {
                val redirectEmail = emailConfigService.getDevRedirectEmail()
                sendRealEmailForPendingRegistration(pending, token, redirectEmail)
            }

            EmailConfigurationService.EmailMode.DEV_SEND -> {
                sendRealEmailForPendingRegistration(pending, token, pending.email)
            }

            EmailConfigurationService.EmailMode.PRODUCTION -> {
                sendRealEmailForPendingRegistration(pending, token, pending.email)
            }
        }
    }

    /**
     * Envoie un email de vérification à un utilisateur selon le mode configuré.
     *
     * @param user L'utilisateur à qui envoyer l'email
     * @param token Le token de vérification
     * @return Le résultat de l'envoi d'email selon le mode effectif
     */
    @Transactional
    fun sendVerificationEmail(user: User, token: String): EmailSendResult {
        val mode = emailConfigService.getEffectiveEmailMode()

        return when (mode) {
            EmailConfigurationService.EmailMode.DISABLED -> {
                println("🚫 [EmailVerification] Email désactivé - Aucun envoi pour ${user.email}")
                EmailSendResult.Disabled("Service d'email désactivé")
            }

            EmailConfigurationService.EmailMode.DEV_LOG -> {
                // Sauvegarder le token et logger pour développement
                saveToken(user, token)
                logDevToken(user.email, token)
                EmailSendResult.DevMode(token)
            }

            EmailConfigurationService.EmailMode.DEV_REDIRECT -> {
                // Envoyer sur l'adresse de redirection de développement
                val redirectEmail = emailConfigService.getDevRedirectEmail()
                sendRealEmail(user, token, redirectEmail)
            }

            EmailConfigurationService.EmailMode.DEV_SEND -> {
                // Envoyer sur l'adresse réelle de l'utilisateur (même en développement)
                val result = sendRealEmail(user, token, user.email)
                if (result is EmailSendResult.Success) {
                    EmailSendResult.DevSend(token, "Email envoyé en développement")
                } else {
                    result
                }
            }

            EmailConfigurationService.EmailMode.PRODUCTION -> {
                // Envoyer sur l'adresse réelle de l'utilisateur
                sendRealEmail(user, token, user.email)
            }
        }
    }

    /**
     * Envoie réellement un email à l'adresse spécifiée.
     */
    private fun sendRealEmail(user: User, token: String, recipientEmail: String): EmailSendResult {
        return try {
            // Sauvegarder le token
            saveToken(user, token)

            val verificationUrl = if (emailConfigService.isDevelopmentProfile()) {
                "$frontendBaseUrl/api/users/verify-email?token=$token"
            } else {
                "$frontendBaseUrl/verify-email?token=$token"
            }
            val expiresAt = LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS)

            // Préparer le contexte pour le template Thymeleaf
            val context = Context().apply {
                setVariable("user", user)
                setVariable("verificationUrl", verificationUrl)
                setVariable("token", token)
                setVariable("expiresAt", expiresAt)
            }

            // Générer le contenu HTML de l'email
            val htmlContent = templateEngine.process("email/email-verification_fr", context)

            // Créer et envoyer l'email
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(InternetAddress(fromEmail, "Frollot"))
            helper.setTo(recipientEmail)
            helper.setSubject("Vérifiez votre adresse email - Frollot")
            helper.setText(htmlContent, true)

            mailSender.send(message)

            val recipientInfo = if (recipientEmail != user.email) {
                "redirigé vers $recipientEmail"
            } else {
                "envoyé à ${user.email}"
            }

            println("✅ [EmailVerification] Email de vérification $recipientInfo")
            EmailSendResult.Success(token, "Email $recipientInfo")

        } catch (e: Exception) {
            println("❌ [EmailVerification] Erreur envoi email à $recipientEmail: ${e.message}")
            e.printStackTrace()
            EmailSendResult.Failed(e.message ?: "Erreur SMTP", token)
        }
    }

    /**
     * Envoie réellement un email de vérification pour une pré-inscription.
     */
    private fun sendRealEmailForPendingRegistration(pending: PendingRegistration, token: String, recipientEmail: String): EmailSendResult {
        return try {
            println("📧 [EmailVerification] === ENVOI EMAIL VÉRIFICATION ===")
            println("📧 [EmailVerification] Destinataire demandé: $recipientEmail")
            println("📧 [EmailVerification] Email du pending: ${pending.email}")
            println("📧 [EmailVerification] Token: $token")

            // VÉRIFICATION CRITIQUE : L'email doit être envoyé à l'adresse de l'utilisateur, pas à une adresse fixe
            if (recipientEmail != pending.email) {
                println("⚠️ [EmailVerification] ATTENTION: Email envoyé à $recipientEmail au lieu de ${pending.email}")
            } else {
                println("✅ [EmailVerification] Email envoyé à la bonne adresse: $recipientEmail")
            }

            val verificationUrl = if (emailConfigService.isDevelopmentProfile()) {
                "http://localhost:8081/verify-email?token=$token"
            } else {
                "$frontendBaseUrl/verify-registration?token=$token"
            }
            val expiresAt = pending.tokenExpiresAt

            println("📧 [EmailVerification] URL de vérification: $verificationUrl")

            // Créer un objet temporaire pour le template (similaire à User)
            val tempUser = object {
                val email = pending.email
                val firstName = pending.firstName ?: "Utilisateur"
                val lastName = pending.lastName ?: ""
            }

            // Préparer le contexte pour le template Thymeleaf
            val context = Context().apply {
                setVariable("user", tempUser)
                setVariable("verificationUrl", verificationUrl)
                setVariable("token", token)
                setVariable("expiresAt", expiresAt)
            }

            println("📧 [EmailVerification] Génération du template HTML...")
            // Générer le contenu HTML de l'email
            val htmlContent = templateEngine.process("email/registration-verification_fr", context)
            println("📧 [EmailVerification] Template généré, longueur: ${htmlContent.length} caractères")

            // Créer et envoyer l'email
            println("📧 [EmailVerification] Création du message MIME...")
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(InternetAddress(fromEmail, "Frollot"))
            helper.setTo(recipientEmail)
            helper.setSubject("Activez votre compte Frollot - Vérification requise")
            helper.setText(htmlContent, true)

            println("📧 [EmailVerification] Envoi de l'email via SMTP...")
            println("📧 [EmailVerification] Connexion SMTP et envoi...")
            mailSender.send(message)
            println("✅ [EmailVerification] EMAIL ENVOYÉ AVEC SUCCÈS via SMTP!")
            println("   └─ Destinataire: $recipientEmail")
            println("   └─ Token inclus: $token")
            println("   └─ URL: $verificationUrl")

            val recipientInfo = if (recipientEmail != pending.email) {
                "redirigé vers $recipientEmail"
            } else {
                "envoyé à ${pending.email}"
            }

            println("✅ [EmailVerification] Email d'activation $recipientInfo")
            EmailSendResult.Success(token, "Email d'activation $recipientInfo")

        } catch (e: Exception) {
            println("❌ [EmailVerification] Erreur envoi email à $recipientEmail: ${e.message}")
            e.printStackTrace()
            EmailSendResult.Failed(e.message ?: "Erreur SMTP", token)
        }
    }

    /**
     * Envoie un email de vérification de CHANGEMENT d'email à la NOUVELLE adresse.
     *
     * Flux distinct de l'inscription : le code OTP est saisi dans l'application
     * (POST /api/users/me/email/confirm), aucun lien cliquable dans l'email.
     * Le token est sauvegardé sur le user via les colonnes de vérification existantes.
     *
     * @param user L'utilisateur authentifié qui demande le changement
     * @param token Le token de vérification (OTP 6 chiffres)
     * @param newEmail La nouvelle adresse (destinataire de l'email)
     * @return Le résultat de l'envoi selon le mode effectif
     */
    @Transactional
    fun sendEmailChangeVerification(user: User, token: String, newEmail: String): EmailSendResult {
        val mode = emailConfigService.getEffectiveEmailMode()
        println("📧 [EmailChange] Mode email effectif: $mode")

        return when (mode) {
            EmailConfigurationService.EmailMode.DISABLED -> {
                println("🚫 [EmailChange] Email désactivé - Aucun envoi pour $newEmail")
                EmailSendResult.Disabled("Service d'email désactivé")
            }

            EmailConfigurationService.EmailMode.DEV_LOG -> {
                saveToken(user, token)
                println("🎯 [DEV MODE] [EmailChange] Token pour $newEmail : $token")
                println("🎯 [DEV MODE] [EmailChange] Confirmer via POST /api/users/me/email/confirm avec {\"token\":\"$token\"}")
                EmailSendResult.DevMode(token)
            }

            EmailConfigurationService.EmailMode.DEV_REDIRECT -> {
                val redirectEmail = emailConfigService.getDevRedirectEmail()
                sendRealEmailChange(user, token, newEmail, redirectEmail)
            }

            EmailConfigurationService.EmailMode.DEV_SEND -> {
                val result = sendRealEmailChange(user, token, newEmail, newEmail)
                if (result is EmailSendResult.Success) {
                    EmailSendResult.DevSend(token, "Email envoyé en développement")
                } else {
                    result
                }
            }

            EmailConfigurationService.EmailMode.PRODUCTION -> {
                sendRealEmailChange(user, token, newEmail, newEmail)
            }
        }
    }

    /**
     * Envoie réellement l'email de changement d'adresse au destinataire spécifié.
     */
    private fun sendRealEmailChange(user: User, token: String, newEmail: String, recipientEmail: String): EmailSendResult {
        return try {
            saveToken(user, token)

            val expiresAt = LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS)

            val context = Context().apply {
                setVariable("user", user)
                setVariable("newEmail", newEmail)
                setVariable("token", token)
                setVariable("expiresAt", expiresAt)
            }

            val htmlContent = templateEngine.process("email/email-change_fr", context)

            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(InternetAddress(fromEmail, "Frollot"))
            helper.setTo(recipientEmail)
            helper.setSubject("Confirmez votre nouvelle adresse email - Frollot")
            helper.setText(htmlContent, true)

            mailSender.send(message)

            val recipientInfo = if (recipientEmail != newEmail) {
                "redirigé vers $recipientEmail"
            } else {
                "envoyé à $newEmail"
            }

            println("✅ [EmailChange] Email de confirmation $recipientInfo (token: $token)")
            EmailSendResult.Success(token, "Email $recipientInfo")

        } catch (e: Exception) {
            println("❌ [EmailChange] Erreur envoi email à $recipientEmail: ${e.message}")
            e.printStackTrace()
            EmailSendResult.Failed(e.message ?: "Erreur SMTP", token)
        }
    }

    /**
     * Sauvegarde le token dans la base de données.
     */
    private fun saveToken(user: User, token: String) {
        user.emailVerificationToken = token
        user.emailVerificationTokenExpiresAt = LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS)
        user.emailVerificationSentAt = LocalDateTime.now()
        userRepository.save(user)
    }

    /**
     * Log le token pour le développement.
     */
    private fun logDevToken(email: String, token: String) {
        val devUrl = "http://localhost:9090/api/users/verify-email?token=$token"
        println("🎯 [DEV MODE] Token pour $email : $token")
        println("🎯 [DEV MODE] URL directe : $devUrl")
        println("🎯 [DEV MODE] Ou utiliser l'endpoint POST /api/users/verify-email avec {\"token\":\"$token\"}")
    }

    /**
     * Vérifie un token de vérification d'email.
     * 
     * @param token Le token à vérifier
     * @return L'utilisateur associé au token si valide, null sinon
     */
    @Transactional
    fun verifyToken(token: String): User? {
        val user = userRepository.findByEmailVerificationToken(token)
            ?: return null

        // Vérifier que le token n'a pas expiré
        if (user.emailVerificationTokenExpiresAt == null || 
            user.emailVerificationTokenExpiresAt!!.isBefore(LocalDateTime.now())) {
            println("⚠️ [EmailVerification] Token expiré pour l'utilisateur ${user.email}")
            return null
        }

        
        // Marquer l'email comme vérifié
        user.emailVerified = true
        user.emailVerificationToken = null
        user.emailVerificationTokenExpiresAt = null
        userRepository.save(user)

        println("✅ [EmailVerification] Email vérifié avec succès pour ${user.email}")
        return user
    }

    /**
     * Renvoie un email de vérification à un utilisateur.
     *
     * @param userId L'ID de l'utilisateur
     * @return true si l'email a été renvoyé, false si l'utilisateur n'existe pas ou est déjà vérifié
     */
    @Transactional
    fun resendVerificationEmail(userId: String): Boolean {
        val user = userRepository.findById(userId).orElse(null)
            ?: return false

        if (user.emailVerified) {
            println("⚠️ [EmailVerification] L'email de l'utilisateur ${user.email} est déjà vérifié")
            return false
        }

        val token = generateVerificationToken()
        val result = sendVerificationEmail(user, token)
        return result is EmailSendResult.Success || result is EmailSendResult.DevMode
    }
}

