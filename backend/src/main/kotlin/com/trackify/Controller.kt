package com.trackify

import com.trackify.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController 
@RequestMapping("/api")
class APIController(
    private val userRepository: UserRepository,
    private val spaceRepository: SpaceRepository,
    private val transactionRepository: TransactionRepository,
    private val encoder: PasswordEncoder
) {

    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> {
        return ResponseEntity.ok("Hello, Authorized User!")
    }
    
    // User endpoints
    @PostMapping("/users")
    fun createUser(@RequestBody userRequest: UserRequest): ResponseEntity<User> {
        val user = User(
            email = userRequest.email,
            password = encoder.encode(userRequest.password),
            role = userRequest.role ?: Role.USER,
            spaceId = userRequest.spaceId
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user))
    }
    
    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<User>> {
        return ResponseEntity.ok(userRepository.findAll())
    }
    
    // Space endpoints
    @PostMapping("/spaces")
    fun createSpace(@RequestBody spaceRequest: SpaceRequest): ResponseEntity<Space> {
        val space = Space(
            name = spaceRequest.name
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceRepository.save(space))
    }
    
    @GetMapping("/spaces")
    fun getAllSpaces(): ResponseEntity<List<Space>> {
        return ResponseEntity.ok(spaceRepository.findAll())
    }
    
    // Transaction endpoints
    @PostMapping("/transactions")
    fun createTransaction(@RequestBody transactionRequest: TransactionRequest): ResponseEntity<Transaction> {
        val transaction = Transaction(
            amount = transactionRequest.amount,
            category = transactionRequest.category,
            userId = transactionRequest.userId,
            spaceId = transactionRequest.spaceId,
            date = transactionRequest.date ?: Date()
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionRepository.save(transaction))
    }
    
    @GetMapping("/transactions")
    fun getAllTransactions(): ResponseEntity<List<Transaction>> {
        return ResponseEntity.ok(transactionRepository.findAll())
    }
    
    @GetMapping("/transactions/user/{userId}")
    fun getTransactionsByUser(@PathVariable userId: UUID): ResponseEntity<List<Transaction>> {
        return ResponseEntity.ok(transactionRepository.findByUserId(userId))
    }
    
    @GetMapping("/transactions/space/{spaceId}")
    fun getTransactionsBySpace(@PathVariable spaceId: UUID): ResponseEntity<List<Transaction>> {
        return ResponseEntity.ok(transactionRepository.findBySpaceId(spaceId))
    }
}

// DTO para requests
data class UserRequest(
    val email: String,
    val password: String,
    val role: Role? = Role.USER,
    val spaceId: UUID? = null
)

data class SpaceRequest(
    val name: String
)

data class TransactionRequest(
    val amount: Float,
    val category: String,
    val userId: UUID,
    val spaceId: UUID,
    val date: Date? = null
)