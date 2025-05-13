package com.luismibm.android.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {
    @POST("/api/auth")
    suspend fun login(@Body request: AuthRequest): AuthResponse
    
    @POST("/api/users")
    suspend fun register(@Body request: RegisterRequest): UserResponse
    
    @GET("/api/spaces")
    suspend fun getSpaces(@Header("Authorization") token: String): List<Space>
    
    @POST("/api/spaces")
    suspend fun createSpace(
        @Header("Authorization") token: String,
        @Body request: SpaceRequest
    ): Space
    
    @GET("/api/users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): UserResponse
    
    @POST("/api/users/space")
    suspend fun updateUserSpace(
        @Header("Authorization") token: String,
        @Body request: UpdateSpaceRequest
    ): UserResponse
    
    @GET("/api/transactions/space/{spaceId}")
    suspend fun getTransactionsBySpace(
        @Header("Authorization") token: String,
        @Path("spaceId") spaceId: String
    ): List<Transaction>
}