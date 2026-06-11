package com.frollot.model

/**
 * Énumération des types de vérification pour les utilisateurs et salons.
 * Phase H.2 - Vérification Salons/Coiffeurs
 * 
 * Permet de distinguer le niveau de vérification accordé.
 */
enum class VerificationType {
    /**
     * Vérification basique : email et/ou téléphone vérifiés
     */
    EMAIL,

    /**
     * Vérification téléphone
     */
    PHONE,

    /**
     * Vérification entreprise : SIRET, documents d'entreprise, etc.
     */
    BUSINESS,

    /**
     * Vérification professionnelle : diplômes, certifications professionnelles
     */
    PROFESSIONAL;

    /**
     * Retourne le libellé utilisateur du type de vérification.
     */
    fun getDisplayName(): String {
        return when (this) {
            EMAIL -> "Email vérifié"
            PHONE -> "Téléphone vérifié"
            BUSINESS -> "Entreprise vérifiée"
            PROFESSIONAL -> "Professionnel vérifié"
        }
    }

    /**
     * Retourne la description du type de vérification.
     */
    fun getDescription(): String {
        return when (this) {
            EMAIL -> "Email vérifié par confirmation"
            PHONE -> "Numéro de téléphone vérifié"
            BUSINESS -> "Entreprise vérifiée (SIRET, documents)"
            PROFESSIONAL -> "Diplômes et certifications vérifiés"
        }
    }

    /**
     * Retourne l'emoji associé au type de vérification.
     */
    fun getEmoji(): String {
        return when (this) {
            EMAIL -> "📧"
            PHONE -> "📱"
            BUSINESS -> "🏢"
            PROFESSIONAL -> "🎓"
        }
    }
}

