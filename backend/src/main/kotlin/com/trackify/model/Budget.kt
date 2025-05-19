package com.trackify.model

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BudgetRepository : JpaRepository<Budget, UUID> {
    fun findByUserId(userId: UUID): List<Budget>
    fun findBySpaceId(spaceId: UUID): List<Budget>
}

@Entity
@Table(name = "budgets")
class Budget {
    @Id
    var id: UUID = UUID.randomUUID()
    
    @Column(nullable = false)
    var name: String = ""
    
    @Column(nullable = false)
    var amount: Float = 0f
    
    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID()
    
    @Column(name = "space_id", nullable = false)
    var spaceId: UUID = UUID.randomUUID()
    
    constructor()
    
    constructor(
        id: UUID = UUID.randomUUID(),
        name: String,
        amount: Float,
        userId: UUID,
        spaceId: UUID
    ) {
        this.id = id
        this.name = name
        this.amount = amount
        this.userId = userId
        this.spaceId = spaceId
    }
} 