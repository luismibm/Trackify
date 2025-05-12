package com.trackify.auth

import com.trackify.model.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class JwtUserDetailsService (
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User $username not found")

        return User.builder()
            .username(user.email)
            .password(user.password)
            .roles(user.role.name)
            .build()
    }

}