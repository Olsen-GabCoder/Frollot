package com.frollot.dto

/**
 * DTO pour enregistrer un token de device FCM.
 */
data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: String? = "android",
    val deviceInfo: String? = null
)

