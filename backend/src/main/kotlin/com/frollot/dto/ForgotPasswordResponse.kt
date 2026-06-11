package com.frollot.dto

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String,
    val email: String? = null
)