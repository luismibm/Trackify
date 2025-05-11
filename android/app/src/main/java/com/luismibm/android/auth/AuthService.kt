package com.luismibm.android.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/api/auth")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}