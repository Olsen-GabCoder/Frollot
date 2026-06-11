package com.frollot.repository

import com.frollot.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    /**
     * Trouve un refresh token par sa valeur.
     */
    fun findByToken(token: String): RefreshToken?

    /**
     * Supprime tous les tokens d'un utilisateur (logout all devices).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    fun deleteByUserId(@Param("userId") userId: String)

    /**
     * Supprime tous les tokens expirés avant une date donnée.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :date")
    fun deleteByExpiresAtBefore(@Param("date") date: LocalDateTime)

    /**
     * Trouve tous les tokens d'un utilisateur.
     */
    fun findAllByUserId(userId: String): List<RefreshToken>

    /**
     * Trouve tous les tokens valides (non révoqués et non expirés) d'un utilisateur.
     */
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.userId = :userId 
        AND rt.revoked = false 
        AND rt.expiresAt > :now
    """)
    fun findValidTokensByUserId(
        @Param("userId") userId: String,
        @Param("now") now: LocalDateTime
    ): List<RefreshToken>
}

