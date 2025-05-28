package com.trackify.auth

import com.trackify.auth.dto.AuthenticationRequest
import com.trackify.auth.dto.AuthenticationResponse
import com.trackify.auth.dto.RefreshTokenRequest
import com.trackify.auth.dto.RefreshTokenResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping
    fun authenticate(
        @RequestBody authRequest: AuthenticationRequest
    ): AuthenticationResponse =
        authService.authentication(authRequest)

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @RequestBody request: RefreshTokenRequest
    ): RefreshTokenResponse = RefreshTokenResponse(token = authService.refreshAccessToken(request.token))
}