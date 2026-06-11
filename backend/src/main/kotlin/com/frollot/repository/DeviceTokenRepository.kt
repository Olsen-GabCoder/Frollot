package com.frollot.repository

import com.frollot.model.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, String> {

    /**
     * Trouve un token par sa valeur.
     */
    fun findByToken(token: String): DeviceToken?

    /**
     * Trouve tous les tokens actifs d'un utilisateur.
     */
    fun findByUserIdAndIsActiveTrue(userId: String): List<DeviceToken>

    /**
     * Trouve tous les tokens d'un utilisateur.
     */
    fun findByUserId(userId: String): List<DeviceToken>

    /**
     * Désactive tous les tokens d'un utilisateur.
     */
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.user.id = :userId")
    fun deactivateAllByUserId(@Param("userId") userId: String)

    /**
     * Supprime les tokens inactifs depuis plus de X jours.
     */
    @Modifying
    @Query("DELETE FROM DeviceToken dt WHERE dt.isActive = false AND dt.updatedAt < :cutoffDate")
    fun deleteInactiveTokensBefore(@Param("cutoffDate") cutoffDate: LocalDateTime)

    /**
     * Met à jour last_used_at pour un token.
     */
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.lastUsedAt = :now WHERE dt.id = :tokenId")
    fun updateLastUsedAt(@Param("tokenId") tokenId: String, @Param("now") now: LocalDateTime)
}

