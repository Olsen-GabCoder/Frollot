package com.frollot.dto

import com.frollot.model.Follow
import com.frollot.model.FollowingType
import java.time.LocalDateTime

/**
 * DTO pour représenter une relation de suivi.
 * Phase D.2 - Système de Follow Salons/Coiffeurs
 */
data class FollowResponse(
    val id: String,
    val followerId: String,
    val followingType: FollowingType,
    val followingId: String,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(follow: Follow): FollowResponse {
            return FollowResponse(
                id = follow.id!!,
                followerId = follow.follower!!.id!!,
                followingType = follow.followingType,
                followingId = follow.followingId,
                createdAt = follow.createdAt
            )
        }
    }
}

