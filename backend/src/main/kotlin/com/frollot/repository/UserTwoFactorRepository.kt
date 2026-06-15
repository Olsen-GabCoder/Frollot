package com.frollot.repository

import com.frollot.model.UserTwoFactor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTwoFactorRepository : JpaRepository<UserTwoFactor, String>
