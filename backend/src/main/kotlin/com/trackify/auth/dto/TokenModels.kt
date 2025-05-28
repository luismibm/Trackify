package com.trackify.auth.dto

data class RefreshTokenRequest(
    val token: String
)

data class RefreshTokenResponse(
    val token: String
)