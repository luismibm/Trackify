package com.trackify.api

import com.trackify.model.Role
import java.util.Date
import java.util.UUID

data class UserRequest(
    val email: String,
    val password: String,
    val role: Role? = Role.USER,
    val spaceId: UUID? = null
)

data class SpaceRequest(
    val name: String,
    val accessCode: String
)

data class UpdateSpaceRequest(
    val spaceId: String?
)

data class VerifySpaceAccessRequest(
    val accessCode: String
)

data class TransactionRequest(
    val amount: Float,
    val category: String,
    val objective: String,
    val userId: UUID,
    val spaceId: UUID,
    val date: Date? = null,
    val description: String
)

data class BudgetRequest(
    val name: String,
    val amount: Float,
    val userId: UUID,
    val spaceId: UUID
)