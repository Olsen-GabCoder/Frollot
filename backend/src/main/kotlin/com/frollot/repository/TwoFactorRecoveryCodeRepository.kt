package com.frollot.repository

import com.frollot.model.TwoFactorRecoveryCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TwoFactorRecoveryCodeRepository : JpaRepository<TwoFactorRecoveryCode, String> {

    fun findAllByUserId(userId: String): List<TwoFactorRecoveryCode>

    fun deleteAllByUserId(userId: String)
}
