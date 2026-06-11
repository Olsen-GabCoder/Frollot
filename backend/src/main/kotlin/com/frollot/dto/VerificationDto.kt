package com.frollot.dto

import com.frollot.model.VerificationType

// ============================================
// REQUEST DTOs (Client → Serveur)
// Phase H.2 - Vérification Salons/Coiffeurs
// ============================================

/**
 * DTO pour demander une vérification.
 * Phase H.2 - Vérification Salons/Coiffeurs
 * 
 * L'utilisateur ou le salon peut demander une vérification en spécifiant le type souhaité.
 */
data class RequestVerificationRequest(
    val verificationType: VerificationType,
    val additionalInfo: String? = null // Informations supplémentaires (documents, SIRET, etc.)
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (additionalInfo != null && additionalInfo.length > 2000) {
            throw IllegalArgumentException("Les informations supplémentaires ne peuvent pas dépasser 2000 caractères")
        }
    }
}

/**
 * DTO pour vérifier un utilisateur (admin uniquement).
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
data class VerifyUserRequest(
    val verificationType: VerificationType,
    val adminNote: String? = null // Note interne pour l'admin
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (adminNote != null && adminNote.length > 500) {
            throw IllegalArgumentException("La note d'administration ne peut pas dépasser 500 caractères")
        }
    }
}

/**
 * DTO pour vérifier un salon (admin uniquement).
 * Phase H.2 - Vérification Salons/Coiffeurs
 */
data class VerifySalonRequest(
    val verificationType: VerificationType,
    val adminNote: String? = null // Note interne pour l'admin
) {
    /**
     * Valide les données de la requête.
     */
    fun validate() {
        if (adminNote != null && adminNote.length > 500) {
            throw IllegalArgumentException("La note d'administration ne peut pas dépasser 500 caractères")
        }
    }
}

