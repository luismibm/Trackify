package com.trackify.model

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SpaceRepository : JpaRepository<Space, UUID>

@Entity
@Table(name = "spaces")
class Space {
    @Id
    var id: UUID = UUID.randomUUID()
    
    @Column(nullable = false)
    var name: String = ""
    
    constructor()
    
    constructor(
        id: UUID = UUID.randomUUID(),
        name: String
    ) {
        this.id = id
        this.name = name
    }
} 