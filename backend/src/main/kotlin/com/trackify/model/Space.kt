package com.trackify.model

import jakarta.persistence.*
import java.util.UUID

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