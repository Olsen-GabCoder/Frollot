package com.frollot.repository

import com.frollot.model.PendingRegistration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PendingRegistrationRepository : JpaRepository<PendingRegistration, String> {

    /**
     * Vérifie si un email est déjà en attente de vérification.
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Recherche une pré-inscription par email.
     */
    fun findByEmail(email: String): PendingRegistration?

    /**
     * Recherche une pré-inscription par token de vérification.
     */
    fun findByVerificationToken(token: String): PendingRegistration?

    /**
     * Supprime les pré-inscriptions expirées.
     */
    fun deleteByTokenExpiresAtBefore(expiryDate: LocalDateTime): Int
}
