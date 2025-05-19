package com.luismibm.android.auth

import java.util.Date

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class UpdateSpaceRequest(
    val spaceId: String
)

data class SpaceRequest(
    val name: String
)

data class Space(
    val id: String,
    val name: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val spaceId: String?
)

data class Transaction(
    val id: String,
    val amount: Float,
    val category: String,
    val userId: String,
    val spaceId: String,
    val date: Date
)

data class CreateTransactionRequest(
    val amount: Float,
    val category: String,
    val userId: String,
    val spaceId: String,
    val date: Date? = null
)

data class Budget(
    val id: String,
    val name: String,
    val amount: Float,
    val userId: String,
    val spaceId: String
)

data class CreateBudgetRequest(
    val name: String,
    val amount: Float,
    val userId: String,
    val spaceId: String
)