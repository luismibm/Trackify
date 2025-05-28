package com.trackify.auth.dto

data class AuthenticationRequest(
    val username: String,
    val password: String
)

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)