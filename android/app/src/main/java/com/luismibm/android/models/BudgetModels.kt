package com.luismibm.android.models

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