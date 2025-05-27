package com.luismibm.android.models

import java.util.Date

data class Transaction(
    val id: String,
    val amount: Float,
    val category: String,
    val objective: String?,
    val userId: String,
    val spaceId: String,
    val date: Date,
    val description: String
)

data class CreateTransactionRequest(
    val amount: Float,
    val category: String,
    val objective: String,
    val userId: String,
    val spaceId: String,
    val date: Date? = null,
    val description: String
)