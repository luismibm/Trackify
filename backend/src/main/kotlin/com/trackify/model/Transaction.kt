package com.trackify.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "transactions")
class Transaction {
    @Id
    var id: UUID = UUID.randomUUID()
    
    @Column(nullable = false)
    var amount: Float = 0f
    
    @Column(nullable = false)
    var category: String = ""
    
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
        userId: UUID,
        spaceId: UUID,
        date: Date = Date()
    ) {
        this.id = id
        this.amount = amount
        this.category = category
        this.userId = userId
        this.spaceId = spaceId
        this.date = date
    }
} 