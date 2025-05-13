package com.trackify.model

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
}

@Entity
@Table(name = "users")
class User {
    @Id
    var id: UUID = UUID.randomUUID()
    
    @Column(unique = true, nullable = false)
    var email: String = ""
    
    @Column(nullable = false)
    var password: String = ""
    
    @Column(name = "space_id")
    var spaceId: UUID? = null
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
    
    constructor()
    
    constructor(
        id: UUID = UUID.randomUUID(),
        email: String,
        password: String,
        spaceId: UUID? = null,
        role: Role = Role.USER
    ) {
        this.id = id
        this.email = email
        this.password = password
        this.spaceId = spaceId
        this.role = role
    }
}

enum class Role {
    USER, ADMIN
}