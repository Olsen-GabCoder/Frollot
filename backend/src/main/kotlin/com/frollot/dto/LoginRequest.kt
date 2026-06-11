package com.frollot.dto

/**
 * Data Transfer Object pour la connexion d'un utilisateur.
 *
 * Ce DTO contient les identifiants fournis par l'utilisateur
 * lors de la connexion. Il est reçu en JSON par l'endpoint POST /api/users/login.
 *
 * @property email Adresse email de l'utilisateur
 * @property password Mot de passe en clair saisi par l'utilisateur
 */
data class LoginRequest(
    val email: String = "",
    val password: String = ""
)