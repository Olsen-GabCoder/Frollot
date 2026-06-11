package com.frollot.repository

import com.frollot.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {

    /**
     * Vérifie si un email existe déjà dans la base de données.
     *
     * @param email L'adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email L'adresse email à rechercher
     * @return L'utilisateur trouvé ou null si aucun utilisateur n'a cet email
     */
    fun findByEmail(email: String): User?

    /**
     * Recherche des utilisateurs par nom, prénom ou email (pour les mentions @).
     *
     * @param query Terme de recherche
     * @return Liste des utilisateurs correspondants
     */
    fun findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        firstName: String,
        lastName: String,
        email: String
    ): List<User>

    /**
     * Recherche un utilisateur par son token de vérification d'email.
     *
     * @param token Le token de vérification
     * @return L'utilisateur trouvé ou null si aucun utilisateur n'a ce token
     */
    fun findByEmailVerificationToken(token: String): User?

    /**
     * Recherche un utilisateur par son token de réinitialisation de mot de passe.
     *
     * @param token Le token de réinitialisation
     * @return L'utilisateur trouvé ou null si aucun utilisateur n'a ce token
     */
    fun findByPasswordResetToken(token: String): User?
}