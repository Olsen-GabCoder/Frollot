package com.frollot.service

import com.frollot.exception.EmailAlreadyExistsException
import com.frollot.model.User
import com.frollot.repository.UserRepository
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.util.*
import javax.naming.NamingException

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordEncoder: PasswordEncoder,
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    private val emailConfigService: EmailConfigurationService,
    @Value("\${app.email.from}")
    private val fromEmail: String
) {

    // Durée de validité du token de réinitialisation (1 heure)
    private val EXPIRATION_HOURS: Long = 1

    /**
     * Gère la demande de réinitialisation de mot de passe.
     * Génère un token, l'enregistre pour l'utilisateur et envoie un e-mail.
     * @param email L'email de l'utilisateur demandant la réinitialisation.
     * @return Le résultat de l'envoi de l'email.
     * @throws IllegalArgumentException si l'utilisateur n'est pas trouvé.
     */
    @Transactional
    fun requestPasswordReset(email: String): EmailSendResult {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Aucun compte trouvé avec cet email.")

        val resetToken = generateResetToken()
        val expiryDate = LocalDateTime.now().plusHours(EXPIRATION_HOURS)

        user.passwordResetToken = resetToken
        user.passwordResetTokenExpiry = expiryDate
        userRepository.save(user)

        // Envoyer l'email de réinitialisation
        println("🔵 [PasswordResetService] Envoi de l'email de réinitialisation pour: ${user.email}")
        println("🔵 [PasswordResetService] Token: $resetToken")

        return sendPasswordResetEmail(user, resetToken)
    }

    /**
     * Envoie un email de réinitialisation de mot de passe à un utilisateur.
     *
     * @param user L'utilisateur à qui envoyer l'email
     * @param token Le token de réinitialisation
     * @return Le résultat de l'envoi d'email selon le mode effectif
     */
    @Transactional
    fun sendPasswordResetEmail(user: User, token: String): EmailSendResult {
        val mode = emailConfigService.getEffectiveEmailMode()

        return when (mode) {
            EmailConfigurationService.EmailMode.DISABLED -> {
                println("🚫 [PasswordResetService] Email désactivé - Aucun envoi pour ${user.email}")
                EmailSendResult.Disabled("Service d'email désactivé")
            }

            EmailConfigurationService.EmailMode.DEV_LOG -> {
                // Sauvegarder le token et logger pour développement
                logDevToken(user.email, token)
                EmailSendResult.DevMode(token)
            }

            EmailConfigurationService.EmailMode.DEV_REDIRECT -> {
                // Envoyer sur l'adresse de redirection de développement
                val redirectEmail = emailConfigService.getDevRedirectEmail()
                sendRealPasswordResetEmail(user, token, redirectEmail)
            }

            EmailConfigurationService.EmailMode.DEV_SEND -> {
                // Envoyer sur l'adresse réelle de l'utilisateur (même en développement)
                val result = sendRealPasswordResetEmail(user, token, user.email)
                if (result is EmailSendResult.Success) {
                    EmailSendResult.DevSend(token, "Email envoyé en développement")
                } else {
                    result
                }
            }

            EmailConfigurationService.EmailMode.PRODUCTION -> {
                // Envoyer sur l'adresse réelle de l'utilisateur
                sendRealPasswordResetEmail(user, token, user.email)
            }
        }
    }

    /**
     * Gère la réinitialisation effective du mot de passe.
     * Valide le token, met à jour le mot de passe et invalide le token.
     * @param token Le token de réinitialisation reçu par l'utilisateur.
     * @param newPassword Le nouveau mot de passe.
     * @throws IllegalArgumentException si le token est invalide, expiré ou si les mots de passe ne correspondent pas.
     */
    fun resetPassword(token: String, newPassword: String) {
        val user = userRepository.findByPasswordResetToken(token)
            ?: throw IllegalArgumentException("Token de réinitialisation invalide.")

        if (user.passwordResetTokenExpiry == null || user.passwordResetTokenExpiry!!.isBefore(LocalDateTime.now())) {
            user.passwordResetToken = null
            user.passwordResetTokenExpiry = null
            userRepository.save(user)
            throw IllegalArgumentException("Token de réinitialisation expiré.")
        }

        // Mettre à jour le mot de passe
        user.passwordHash = passwordEncoder.encode(newPassword)
        user.passwordResetToken = null // Invalider le token
        user.passwordResetTokenExpiry = null
        userRepository.save(user)

        println("✅ [PasswordResetService] Mot de passe réinitialisé pour l'utilisateur: ${user.email}")
    }

    /**
     * Envoie réellement un email de réinitialisation de mot de passe.
     */
    private fun sendRealPasswordResetEmail(user: User, token: String, recipientEmail: String): EmailSendResult {
        return try {
            println("📧 [PasswordResetService] === ENVOI EMAIL RÉINITIALISATION ===")
            println("📧 [PasswordResetService] Destinataire demandé: $recipientEmail")
            println("📧 [PasswordResetService] Email de l'utilisateur: ${user.email}")
            println("📧 [PasswordResetService] Token: $token")

            val resetUrl = "https://frollot.com/reset-password?token=$token"
            val expiresAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS)

            // Déterminer le template selon la langue préférée de l'utilisateur
            val templateName = when (user.preferredLanguage ?: "fr") {
                "en" -> "email/password-reset_en"
                "es" -> "email/password-reset_es"
                "de" -> "email/password-reset_de"
                "ar" -> "email/password-reset_ar"
                else -> "email/password-reset_fr"
            }

            println("📧 [PasswordResetService] Template utilisé: $templateName")

            // Préparer le contexte pour le template Thymeleaf
            val context = Context().apply {
                setVariable("user", user)
                setVariable("resetUrl", resetUrl)
                setVariable("token", token)
                setVariable("expiresAt", expiresAt)
            }

            println("📧 [PasswordResetService] URL de réinitialisation: $resetUrl")

            // Générer le contenu HTML de l'email
            val htmlContent = templateEngine.process(templateName, context)
            println("📧 [PasswordResetService] Template généré, longueur: ${htmlContent.length} caractères")

            // Créer et envoyer l'email
            println("📧 [PasswordResetService] Création du message MIME...")
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(InternetAddress(fromEmail, "Frollot"))
            helper.setTo(recipientEmail)
            helper.setSubject(when (user.preferredLanguage ?: "fr") {
                "en" -> "Reset Your Password - Frollot"
                "es" -> "Restablece tu contraseña - Frollot"
                "de" -> "Setzen Sie Ihr Passwort zurück - Frollot"
                "ar" -> "إعادة تعيين كلمة المرور - Frollot"
                else -> "Réinitialisez votre mot de passe - Frollot"
            })
            helper.setText(htmlContent, true)

            println("📧 [PasswordResetService] Envoi de l'email via SMTP...")
            println("📧 [PasswordResetService] Connexion SMTP et envoi...")
            mailSender.send(message)
            println("✅ [PasswordResetService] EMAIL ENVOYÉ AVEC SUCCÈS via SMTP!")
            println("   └─ Destinataire: $recipientEmail")
            println("   └─ Token inclus: $token")
            println("   └─ URL: $resetUrl")

            val recipientInfo = if (recipientEmail != user.email) {
                "redirigé vers $recipientEmail"
            } else {
                "envoyé à ${user.email}"
            }

            println("✅ [PasswordResetService] Email de réinitialisation $recipientInfo")
            EmailSendResult.Success(token, "Email de réinitialisation $recipientInfo")

        } catch (e: Exception) {
            println("❌ [PasswordResetService] Erreur envoi email à $recipientEmail: ${e.message}")
            e.printStackTrace()
            EmailSendResult.Failed(e.message ?: "Erreur SMTP", token)
        }
    }

    /**
     * Log le token pour le développement.
     */
    private fun logDevToken(email: String, token: String) {
        val devUrl = "http://localhost:8090/api/users/reset-password?token=$token"
        println("🎯 [DEV MODE] Token pour $email : $token")
        println("🎯 [DEV MODE] URL directe : $devUrl")
        println("🎯 [DEV MODE] Ou utiliser l'endpoint POST /api/users/reset-password avec {\"token\":\"$token\", \"newPassword\":\"nouveau_mot_de_passe\", \"confirmPassword\":\"nouveau_mot_de_passe\"}")
    }

    /**
     * Génère un token de réinitialisation unique.
     */
    private fun generateResetToken(): String {
        return UUID.randomUUID().toString()
    }
}
