package com.frollot.repository

import com.frollot.model.WaitingQueue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WaitingQueueRepository : JpaRepository<WaitingQueue, String> {
    fun findBySalonId(salonId: String): WaitingQueue?
    fun existsBySalonId(salonId: String): Boolean
}

