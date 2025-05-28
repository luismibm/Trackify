package com.trackify.api

import com.trackify.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController 
@RequestMapping("/api")
class APIController(
    private val userRepository: UserRepository,
    private val spaceRepository: SpaceRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
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
    
    @GetMapping("/users/me")
    fun getCurrentUser(): ResponseEntity<User> {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name
        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        
        return ResponseEntity.ok(user)
    }
    
    @PostMapping("/users/space")
    fun updateUserSpace(@RequestBody request: UpdateSpaceRequest): ResponseEntity<User> {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name
        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        
        user.spaceId = request.spaceId?.let {
            try {
                UUID.fromString(it)
            } catch (e: IllegalArgumentException) {
                return ResponseEntity.badRequest().build()
            }
        }
        return ResponseEntity.ok(userRepository.save(user))
    }
    
    // Space endpoints
    @PostMapping("/spaces")
    fun createSpace(@RequestBody spaceRequest: SpaceRequest): ResponseEntity<Space> {
        val space = Space(
            name = spaceRequest.name,
            accessCode = encoder.encode(spaceRequest.accessCode)
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceRepository.save(space))
    }
    
    @GetMapping("/spaces")
    fun getAllSpaces(): ResponseEntity<List<Space>> {
        return ResponseEntity.ok(spaceRepository.findAll())
    }

    @PostMapping("/spaces/{spaceId}/verify")
    fun verifySpaceAccessCode(@PathVariable spaceId: UUID, @RequestBody verifyRequest: VerifySpaceAccessRequest): ResponseEntity<Void> {
        val space = spaceRepository.findById(spaceId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (encoder.matches(verifyRequest.accessCode, space.accessCode)) {
            return ResponseEntity.ok().build()
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
    
    // Transaction endpoints
    @PostMapping("/transactions")
    fun createTransaction(@RequestBody transactionRequest: TransactionRequest): ResponseEntity<Transaction> {
        val transaction = Transaction(
            amount = transactionRequest.amount,
            category = transactionRequest.category,
            objective = transactionRequest.objective,
            userId = transactionRequest.userId,
            spaceId = transactionRequest.spaceId,
            date = transactionRequest.date ?: Date(),
            description = transactionRequest.description
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

    @DeleteMapping("/transactions/{id}")
    fun deleteTransaction(@PathVariable id: UUID): ResponseEntity<Void> {
        if (!transactionRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        transactionRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    // Budget endpoints
    @PostMapping("/budgets")
    fun createBudget(@RequestBody budgetRequest: BudgetRequest): ResponseEntity<Budget> {
        val budget = Budget(
            name = budgetRequest.name,
            amount = budgetRequest.amount,
            userId = budgetRequest.userId,
            spaceId = budgetRequest.spaceId
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetRepository.save(budget))
    }

    @GetMapping("/budgets")
    fun getAllBudgets(): ResponseEntity<List<Budget>> {
        return ResponseEntity.ok(budgetRepository.findAll())
    }

    @GetMapping("/budgets/user/{userId}")
    fun getBudgetsByUser(@PathVariable userId: UUID): ResponseEntity<List<Budget>> {
        return ResponseEntity.ok(budgetRepository.findByUserId(userId))
    }

    @GetMapping("/budgets/space/{spaceId}")
    fun getBudgetsBySpace(@PathVariable spaceId: UUID): ResponseEntity<List<Budget>> {
        return ResponseEntity.ok(budgetRepository.findBySpaceId(spaceId))
    }

    @DeleteMapping("/budgets/{id}")
    fun deleteBudget(@PathVariable id: UUID): ResponseEntity<Void> {
        if (!budgetRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        budgetRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
