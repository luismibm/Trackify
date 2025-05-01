package com.trackify.model

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepository (
    private val encoder: PasswordEncoder
) {

    // Datos de prueba
    private val users = mutableSetOf(
        User(
            id = UUID.randomUUID(),
            name = "email1@gmail.com",
            password = encoder.encode("pass1"),
            role = Role.USER,
        ),
        User(
            id = UUID.randomUUID(),
            name = "email2@gmail.com",
            password = encoder.encode("pass2"),
            role = Role.ADMIN,
        ),
        User(
            id = UUID.randomUUID(),
            name = "email3@gmail.com",
            password = encoder.encode("pass3"),
            role = Role.USER,
        )
    )

    fun findByUsername(email: String): User? = users.firstOrNull { it.name == email }

}