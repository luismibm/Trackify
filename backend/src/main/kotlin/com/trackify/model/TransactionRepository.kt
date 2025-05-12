package com.trackify.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TransactionRepository : JpaRepository<Transaction, UUID> {
    fun findByUserId(userId: UUID): List<Transaction>
    fun findBySpaceId(spaceId: UUID): List<Transaction>
    fun findByUserIdAndSpaceId(userId: UUID, spaceId: UUID): List<Transaction>
} 