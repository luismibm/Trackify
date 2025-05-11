package com.luismibm.android.auth

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)