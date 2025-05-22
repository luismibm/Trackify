package com.trackify.auth.dto

data class AuthenticationRequest(
    val username: String,
    val password: String
)