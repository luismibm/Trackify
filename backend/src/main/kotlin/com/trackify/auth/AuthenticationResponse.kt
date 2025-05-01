package com.trackify.auth

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
) 