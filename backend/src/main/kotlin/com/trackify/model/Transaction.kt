package com.trackify.model

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TransactionRepository : JpaRepository<Transaction, UUID> {
    fun findByUserId(userId: UUID): List<Transaction>
    fun findBySpaceId(spaceId: UUID): List<Transaction>
}

@Entity
@Table(name = "transactions")
class Transaction {
    @Id
    var id: UUID = UUID.randomUUID()
    
    @Column(nullable = false)
    var amount: Float = 0f

    @Column(nullable = false)
    var description: String = ""
    
    @Column(nullable = false)
    var category: String = ""

    @Column(nullable = true)
    var objective: String = ""
    
    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID()
    
    @Column(name = "space_id", nullable = false)
    var spaceId: UUID = UUID.randomUUID()
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var date: Date = Date()
    
    constructor()
    
    constructor(
        id: UUID = UUID.randomUUID(),
        amount: Float,
        category: String,
        objective: String,
        userId: UUID,
        spaceId: UUID,
        date: Date = Date(),
        description: String
    ) {
        this.id = id
        this.amount = amount
        this.category = category
        this.objective = objective
        this.userId = userId
        this.spaceId = spaceId
        this.date = date
        this.description = description
    }
} 