package com.trackify.auth.dto

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)