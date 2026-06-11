package com.frollot.service

import com.frollot.dto.RegisterRequest
import com.frollot.dto.UpdateProfileRequest
import com.frollot.exception.EmailAlreadyExistsException
import com.frollot.model.User
import com.frollot.model.PendingRegistration
import com.frollot.repository.UserRepository
import com.frollot.repository.PendingRegistrationRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.time.LocalDateTime

    /**
     * Résultat de l'inscription d'un utilisateur.
     */
    data class RegistrationResult(
        val user: User,
        val emailSendResult: EmailSendResult
    )

    /**
     * Résultat d'une pré-inscription (avant vérification email).
     */
    data class PreRegistrationResult(
        val pendingId: String,
        val emailSendResult: EmailSendResult,
        val message: String
    )

/**
 * Exception levée lorsqu'un email de vérification ne peut pas être envoyé.
 * Utilisée pour déclencher un rollback automatique de l'inscription.
 */
class EmailSendException(
    message: String,
    val userId: String? = null
) : RuntimeException(message)

/**
 * Service gérant la logique métier liée aux utilisateurs.
 *
 * Cette couche intermédiaire entre le Controller et le Repository
 * contient les règles métier et les validations.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val pendingRegistrationRepository: PendingRegistrationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailVerificationService: EmailVerificationService
) {

    /**
     * Récupère tous les utilisateurs de la base de données.
     *
     * @return Liste de tous les utilisateurs
     */
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    /**
     * Pré-inscrit un utilisateur (étape 1 : vérification email obligatoire).
     *
     * Cette méthode crée une pré-inscription et envoie un email de vérification.
     * AUCUN compte utilisateur n'est créé tant que l'email n'est pas vérifié.
     *
     * @param request Données d'inscription fournies par l'utilisateur
     * @return Le résultat de la pré-inscription
     * @throws EmailAlreadyExistsException Si l'email est déjà utilisé (HTTP 409)
     */
    @Transactional
    fun preRegisterUser(request: RegisterRequest): PreRegistrationResult {
        println("🔵 [UserService] === DÉBUT PRÉ-INSCRIPTION ===")
        println("🔵 [UserService] Email demandé: ${request.email}")
        println("🔵 [UserService] Nom: ${request.firstName} ${request.lastName}")
        println("🔵 [UserService] Type: ${request.userType}")

        // 🔍 Vérifier si l'email existe déjà
        val existingUser = userRepository.findByEmail(request.email)
        println("🔍 [UserService] Vérification utilisateur existant: ${existingUser?.let { "OUI (id: ${it.id})" } ?: "NON"}")
        if (existingUser != null) {
            // Si l'utilisateur existe mais n'est pas vérifié, permettre la "réinscription"
            if (!existingUser.emailVerified) {
                println("🔄 [UserService] Utilisateur non vérifié trouvé, suppression pour permettre la réinscription: ${request.email}")
                userRepository.delete(existingUser)
                // Note: Les tokens de refresh associés seront automatiquement supprimés par les contraintes de base de données
            } else {
                throw EmailAlreadyExistsException("Un compte existe déjà avec cette adresse email.")
            }
        }

        // Vérifier les pré-inscriptions existantes
        val existingPending = pendingRegistrationRepository.findByEmail(request.email)
        println("🔍 [UserService] Vérification pré-inscription existante: ${existingPending?.let { "OUI (id: ${it.id}, expires: ${it.tokenExpiresAt})" } ?: "NON"}")

        if (existingPending != null) {
            // Supprimer la pré-inscription expirée ou permettre la recréation
            if (existingPending.tokenExpiresAt?.isBefore(LocalDateTime.now()) == true) {
                println("🔄 [UserService] Pré-inscription expirée supprimée: ${request.email}")
                pendingRegistrationRepository.delete(existingPending)
            } else {
                throw EmailAlreadyExistsException("Une demande d'inscription est déjà en cours pour cette adresse email. Vérifiez votre email.")
            }
        }

        // 📧 Validation basique du format de l'email
        if (!isValidEmail(request.email)) {
            throw IllegalArgumentException("Format d'email invalide")
        }

        // 📧 Vérification de l'existence réelle de l'email (MX records)
        if (!emailVerificationService.verifyEmailExists(request.email)) {
            throw IllegalArgumentException("Cette adresse email n'existe pas ou n'est pas valide. Veuillez vérifier votre adresse email.")
        }

        // 🔒 Validation du mot de passe
        if (request.password.isBlank()) {
            throw IllegalArgumentException("Le mot de passe est obligatoire")
        }
        if (request.password.length < 8) {
            throw IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères")
        }

        // 🔒 HASHAGE BCRYPT
        val hashedPassword = passwordEncoder.encode(request.password)
        if (hashedPassword.isBlank() || !hashedPassword.startsWith("\$2a\$")) {
            throw IllegalStateException("Erreur lors du hashage du mot de passe")
        }

        // 🎫 Générer le token de vérification
        val verificationToken = emailVerificationService.generateVerificationToken()
        val tokenExpiresAt = LocalDateTime.now().plusHours(24) // 24h pour vérifier

        // 📝 Créer la pré-inscription
        val pendingRegistration = PendingRegistration(
            id = UUID.randomUUID().toString(),
            email = request.email.trim().lowercase(),
            passwordHash = hashedPassword,
            userType = request.userType,
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            phoneNumber = null, // Non fourni dans RegisterRequest pour le moment
            verificationToken = verificationToken,
            tokenExpiresAt = tokenExpiresAt,
            attempts = 0
        )

        // 💾 Sauvegarder la pré-inscription
        val savedPending = pendingRegistrationRepository.save(pendingRegistration)
        println("✅ [UserService] Pré-inscription SAUVEGARDÉE en base:")
        println("   └─ ID: ${savedPending.id}")
        println("   └─ Email: ${savedPending.email}")
        println("   └─ Token: ${savedPending.verificationToken}")
        println("   └─ Expires: ${savedPending.tokenExpiresAt}")
        println("   └─ Table: pending_registrations (PAS users)")

        // Vérifier qu'aucun utilisateur n'a été créé
        val userAfterSave = userRepository.findByEmail(request.email)
        println("🔍 [UserService] Vérification post-sauvegarde:")
        println("   └─ Utilisateur dans 'users': ${userAfterSave?.let { "ERREUR - OUI (id: ${it.id})" } ?: "CORRECT - NON"}")

        // 📧 Envoyer l'email de vérification
        println("📧 [UserService] Envoi de l'email de vérification...")
        val emailSendResult = emailVerificationService.sendVerificationEmailForPendingRegistration(savedPending, verificationToken)

        // 📋 Évaluation du résultat d'envoi d'email
        return when (emailSendResult) {
            is EmailSendResult.Success, is EmailSendResult.DevSend -> {
                println("✅ [UserService] Email de vérification envoyé à ${savedPending.email}")
                PreRegistrationResult(
                    pendingId = savedPending.id!!,
                    emailSendResult = emailSendResult,
                    message = "Un email de vérification a été envoyé à ${savedPending.email}. Cliquez sur le lien pour activer votre compte."
                )
            }

            is EmailSendResult.DevMode -> {
                println("📧 [UserService] Mode DEV - Token pour ${savedPending.email}: ${emailSendResult.token}")
                PreRegistrationResult(
                    pendingId = savedPending.id!!,
                    emailSendResult = emailSendResult,
                    message = "Mode développement : utilisez le token ${emailSendResult.token} pour vérifier votre email."
                )
            }

            is EmailSendResult.Disabled -> {
                // Supprimer la pré-inscription si l'email est désactivé
                pendingRegistrationRepository.delete(savedPending)
                throw EmailSendException("Service d'email désactivé. Impossible de procéder à l'inscription.")
            }

            is EmailSendResult.Failed -> {
                // Supprimer la pré-inscription en cas d'échec
                pendingRegistrationRepository.delete(savedPending)
                throw EmailSendException("Impossible d'envoyer l'email de vérification: ${emailSendResult.error}")
            }

            else -> {
                pendingRegistrationRepository.delete(savedPending)
                throw EmailSendException("Erreur inattendue lors de l'envoi d'email")
            }
        }
    }

    /**
     * Finalise l'inscription après vérification email (étape 2).
     *
     * @param verificationToken Le token de vérification reçu par email
     * @return L'utilisateur créé
     * @throws IllegalArgumentException Si le token est invalide ou expiré
     */
    @Transactional
    fun completeRegistration(verificationToken: String): User {
        println("🔵 [UserService] === DÉBUT FINALISATION INSCRIPTION ===")
        println("🔵 [UserService] Token fourni: $verificationToken")

        // 🔍 Validation du format OTP (6 chiffres uniquement)
        if (!verificationToken.matches(Regex("^[0-9]{6}$"))) {
            println("❌ [UserService] Token invalide - format incorrect (doit être 6 chiffres)")
            throw IllegalArgumentException("Le code de vérification doit contenir exactement 6 chiffres.")
        }

        println("✅ [UserService] Token format valide (6 chiffres)")

        // 🔍 Rechercher la pré-inscription par token
        val pendingRegistration = pendingRegistrationRepository.findByVerificationToken(verificationToken)
            ?: throw IllegalArgumentException("Token de vérification invalide ou expiré.")

        // ⏰ Vérifier que le token n'a pas expiré
        if (pendingRegistration.tokenExpiresAt?.isBefore(LocalDateTime.now()) == true) {
            // Supprimer la pré-inscription expirée
            pendingRegistrationRepository.delete(pendingRegistration)
            throw IllegalArgumentException("Le lien de vérification a expiré. Veuillez vous réinscrire.")
        }

        // 🔢 Incrémenter le compteur de tentatives
        pendingRegistration.attempts += 1
        pendingRegistration.lastAttemptAt = LocalDateTime.now()

        // Limiter les tentatives (max 10)
        if (pendingRegistration.attempts > 10) {
            pendingRegistrationRepository.delete(pendingRegistration)
            throw IllegalArgumentException("Trop de tentatives de vérification. Veuillez vous réinscrire.")
        }

        pendingRegistrationRepository.save(pendingRegistration)

        // ✅ PROTECTION ANTI-DOUBLON : Vérifier qu'aucun utilisateur vérifié n'existe déjà
        val existingUser = userRepository.findByEmail(pendingRegistration.email)
        if (existingUser != null) {
            println("🚨 [UserService] PROTECTION ANTI-DOUBLON: Utilisateur existant trouvé pour ${pendingRegistration.email}")
            println("   └─ ID existant: ${existingUser.id}")
            println("   └─ Email vérifié: ${existingUser.emailVerified}")
            println("   └─ Vérifié professionnellement: ${existingUser.isVerified}")

            if (existingUser.emailVerified) {
                // L'utilisateur existe ET son email est déjà vérifié - INTERDIRE la revalidation
                pendingRegistrationRepository.delete(pendingRegistration)
                println("❌ [UserService] TENTATIVE DE REVALIDATION INTERDITE: ${pendingRegistration.email} est déjà vérifié")
                throw IllegalArgumentException("Cet email est déjà enregistré et vérifié. Veuillez vous connecter directement.")
            } else {
                // L'utilisateur existe mais n'est pas vérifié - permettre la revalidation (cas edge)
                println("⚠️ [UserService] Revalidation autorisée pour utilisateur non vérifié: ${pendingRegistration.email}")
                // Supprimer l'ancien utilisateur non vérifié pour permettre la recréation
                userRepository.delete(existingUser)
                println("🗑️ [UserService] Ancien utilisateur non vérifié supprimé: ${existingUser.id}")
            }
        }

        // 👤 Créer l'utilisateur définitif
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = pendingRegistration.email,
            passwordHash = pendingRegistration.passwordHash,
            userType = pendingRegistration.userType,
            firstName = pendingRegistration.firstName,
            lastName = pendingRegistration.lastName,
            phoneNumber = pendingRegistration.phoneNumber,
            isVerified = false,
            emailVerified = true, // Email vérifié !
            isActive = true,
            emailVerificationToken = null,
            emailVerificationTokenExpiresAt = null,
            emailVerificationSentAt = LocalDateTime.now()
        )

        // 💾 Sauvegarder l'utilisateur
        val savedUser = userRepository.save(newUser)

        // 🗑️ Supprimer la pré-inscription
        pendingRegistrationRepository.delete(pendingRegistration)
        println("🗑️ [UserService] Pré-inscription supprimée de la base")

        println("✅ [UserService] === INSCRIPTION FINALISÉE ===")
        println("   └─ Utilisateur CRÉÉ dans 'users': ${savedUser.email}")
        println("   └─ ID: ${savedUser.id}")
        println("   └─ Email vérifié: ${savedUser.emailVerified}")
        println("   └─ Table 'pending_registrations': VIDE pour cet email")
        return savedUser
    }

    /**
     * Inscrit un nouvel utilisateur dans le système (méthode LEGACY - conservée pour compatibilité).
     *
     * ⚠️ DEPRECATED : Utilisez preRegisterUser() + completeRegistration() pour une sécurité renforcée.
     *
     * @param request Données d'inscription fournies par l'utilisateur
     * @return Le résultat de l'inscription incluant l'utilisateur et le statut d'envoi d'email
     * @throws EmailAlreadyExistsException Si l'email est déjà utilisé (HTTP 409)
     */
    @Deprecated("Utilisez preRegisterUser() à la place pour une sécurité renforcée")
    fun registerUser(request: RegisterRequest): RegistrationResult {
        throw UnsupportedOperationException("Cette méthode est obsolète. Utilisez le système de pré-inscription.")
    }

    /**
     * Supprime un utilisateur non vérifié (utilisé en cas de rollback automatique).
     * Cette méthode est utilisée uniquement en cas d'échec critique d'envoi d'email.
     *
     * @param userId ID de l'utilisateur à supprimer
     */
    @Transactional
    fun deleteUnverifiedUser(userId: String) {
        try {
            val user = userRepository.findById(userId).orElse(null)
            if (user != null && !user.emailVerified) {
                userRepository.delete(user)
                println("🗑️ [UserService] Utilisateur non vérifié supprimé: $userId")
            } else {
                println("⚠️ [UserService] Utilisateur $userId déjà vérifié ou inexistant")
            }
        } catch (e: Exception) {
            println("❌ [UserService] Erreur lors de la suppression de l'utilisateur $userId: ${e.message}")
            throw e
        }
    }

    /**
     * Vérifie un mot de passe en clair contre un hash BCrypt.
     *
     * @param rawPassword Mot de passe en clair
     * @param encodedPassword Hash BCrypt stocké en BDD
     * @return true si le mot de passe correspond, false sinon
     */
    fun checkPassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }

    /**
     * Change le mot de passe d'un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @param currentPassword Mot de passe actuel
     * @param newPassword Nouveau mot de passe
     * @throws IllegalArgumentException Si l'utilisateur n'existe pas ou mot de passe actuel incorrect
     */
    @Transactional
    fun changePassword(userId: String, currentPassword: String, newPassword: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }
        
        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, user.passwordHash)) {
            throw IllegalArgumentException("Mot de passe actuel incorrect")
        }
        
        // Hasher et sauvegarder le nouveau mot de passe
        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)
    }

    /**
     * Demande un changement d'email avec re-vérification (V040).
     *
     * NE bascule PAS l'email immédiatement : le nouvel email est stocké dans
     * pendingEmail et un code de vérification est envoyé À LA NOUVELLE adresse.
     * L'email actif et emailVerified restent inchangés tant que le code n'est pas
     * confirmé via confirmEmailChange. Une nouvelle demande écrase la précédente
     * (pendingEmail + token remplacés, l'ancien code devient caduc).
     *
     * @param userId ID de l'utilisateur authentifié
     * @param newEmail Nouvel email demandé
     * @param password Mot de passe pour confirmation
     * @return Le résultat de l'envoi de l'email de vérification
     * @throws IllegalArgumentException Si mot de passe incorrect, email déjà utilisé ou email invalide
     */
    @Transactional
    fun requestEmailChange(userId: String, newEmail: String, password: String): EmailSendResult {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Mot de passe incorrect")
        }

        // Vérifier que le nouvel email n'existe pas déjà
        val existingUser = userRepository.findByEmail(newEmail)
        if (existingUser != null && existingUser.id != userId) {
            throw IllegalArgumentException("Cet email est déjà utilisé")
        }

        // Vérifier que le nouvel email existe réellement (format + MX)
        if (!emailVerificationService.verifyEmailExists(newEmail)) {
            throw IllegalArgumentException("Cette adresse email semble invalide ou inexistante")
        }

        // Stocker la demande (écrase toute demande précédente non confirmée)
        user.pendingEmail = newEmail
        userRepository.save(user)

        // Générer le token et envoyer à la NOUVELLE adresse
        // (sendEmailChangeVerification sauvegarde le token sur le user, écrasant l'ancien)
        val token = emailVerificationService.generateVerificationToken()
        return emailVerificationService.sendEmailChangeVerification(user, token, newEmail)
    }

    /**
     * Confirme un changement d'email avec le code de vérification (V040).
     *
     * Validation isolée du flux d'inscription : le token est comparé au token
     * stocké de CET utilisateur (pas de recherche globale par token).
     * Si valide : email = pendingEmail, pendingEmail et token nettoyés,
     * emailVerified reste true.
     *
     * @param userId ID de l'utilisateur authentifié
     * @param token Code de vérification reçu sur la nouvelle adresse
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException Si aucune demande en attente, token invalide/expiré
     *         ou email pris entre-temps
     */
    @Transactional
    fun confirmEmailChange(userId: String, token: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }

        val pendingEmail = user.pendingEmail
            ?: throw IllegalArgumentException("Aucune demande de changement d'email en attente")

        // Token comparé au token stocké de cet utilisateur uniquement
        if (user.emailVerificationToken == null || user.emailVerificationToken != token) {
            throw IllegalArgumentException("Code de vérification invalide")
        }

        if (user.emailVerificationTokenExpiresAt == null ||
            user.emailVerificationTokenExpiresAt!!.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Code de vérification expiré")
        }

        // Re-vérifier l'unicité (l'email a pu être pris entre la demande et la confirmation)
        val existingUser = userRepository.findByEmail(pendingEmail)
        if (existingUser != null && existingUser.id != userId) {
            throw IllegalArgumentException("Cet email est déjà utilisé")
        }

        // Basculer l'email pour de bon
        user.email = pendingEmail
        user.pendingEmail = null
        user.emailVerificationToken = null
        user.emailVerificationTokenExpiresAt = null
        // emailVerified reste true : la nouvelle adresse vient d'être vérifiée

        println("✅ [EmailChange] Email basculé avec succès vers $pendingEmail pour l'utilisateur $userId")
        return userRepository.save(user)
    }

    /**
     * Change le téléphone d'un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @param newPhone Nouveau numéro de téléphone
     * @param password Mot de passe pour confirmation
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException Si mot de passe incorrect
     */
    @Transactional
    fun changePhone(userId: String, newPhone: String?, password: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }
        
        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Mot de passe incorrect")
        }
        
        // Mettre à jour le téléphone
        user.phoneNumber = newPhone
        return userRepository.save(user)
    }

    /**
     * Supprime définitivement un compte utilisateur.
     *
     * Note: Cette opération est irréversible.
     * Elle supprime l'utilisateur et toutes ses données associées.
     *
     * @param userId ID de l'utilisateur
     * @param password Mot de passe pour confirmation
     * @throws IllegalArgumentException Si l'utilisateur n'existe pas ou mot de passe incorrect
     */
    @Transactional
    fun deleteAccount(userId: String, password: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }
        
        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Mot de passe incorrect")
        }
        
        // Supprimer l'utilisateur (les cascades supprimeront les données liées)
        userRepository.delete(user)
    }

    /**
     * Met à jour l'avatar d'un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @param avatarUrl URL de l'avatar
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException Si l'utilisateur n'existe pas
     */
    fun updateUserAvatar(userId: String, avatarUrl: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur introuvable: $userId") }
        
        user.avatarUrl = avatarUrl
        return userRepository.save(user)
    }

    /**
     * Met à jour le profil d'un utilisateur.
     * 
     * Mise à jour partielle : seuls les champs non-null dans la requête seront mis à jour.
     *
     * @param userId ID de l'utilisateur
     * @param request Données du profil à mettre à jour
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException Si l'utilisateur n'existe pas
     */
    fun updateUserProfile(userId: String, request: UpdateProfileRequest): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur introuvable: $userId") }
        
        // Mise à jour partielle : seuls les champs non-null sont modifiés
        request.firstName?.let { user.firstName = it.trim() }
        request.lastName?.let { user.lastName = it.trim() }
        request.phoneNumber?.let { user.phoneNumber = it.trim() }
        request.bio?.let { user.bio = it.trim() }
        request.avatarUrl?.let { user.avatarUrl = it }
        request.preferredLanguage?.let { user.preferredLanguage = it }
        request.instagramHandle?.let { user.instagramHandle = it.trim() }
        request.yearsExperience?.let { user.yearsExperience = it }
        
        return userRepository.save(user)
    }

    /**
     * Met à jour la photo de couverture d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @param coverImageUrl L'URL de la nouvelle photo de couverture
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     * @throws RuntimeException si l'utilisateur n'est pas autorisé à modifier ce profil
     */
    fun updateUserCoverImage(userId: String, coverImageUrl: String, authenticatedUserId: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur avec l'ID $userId introuvable") }

        // Vérifier que l'utilisateur est bien le propriétaire du compte
        if (user.id != authenticatedUserId) {
            throw RuntimeException("Seul le propriétaire du compte peut modifier la photo de couverture")
        }

        // Valider la longueur de l'URL
        if (coverImageUrl.length > 500) {
            throw IllegalArgumentException("L'URL de la photo de couverture ne peut pas dépasser 500 caractères")
        }

        user.coverImageUrl = coverImageUrl.trim().takeIf { it.isNotBlank() }
        return userRepository.save(user)
    }

    /**
     * Valide le format d'une adresse email.
     *
     * Validation simple : doit contenir @ et un point après le @
     *
     * @param email L'adresse email à valider
     * @return true si le format est valide, false sinon
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}