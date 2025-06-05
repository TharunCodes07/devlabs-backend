package com.devlabs.devlabsbackend.user.controller

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.user.domain.DTO.CreateUserRequest
import com.devlabs.devlabsbackend.user.domain.DTO.UpdateUserRequest
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/user")
class UserController (private val userService: UserService)
{    
    @GetMapping
    fun getAllUsers(
        @RequestParam(required = false) role: String?
    ): ResponseEntity<Any> {
        return try {
            if (role != null) {
                try {
                    val roleEnum = Role.valueOf(role.uppercase())
                    val filteredUsers = userService.getAllUsersByRole(roleEnum)
                    ResponseEntity.ok(filteredUsers)
                } catch (e: IllegalArgumentException) {
                    ResponseEntity.badRequest().body(mapOf("message" to "Invalid role: $role. Valid roles are: ${Role.values().joinToString(", ")}"))
                }
            } else {
                val users = userService.getAllUsers()
                ResponseEntity.ok(users)
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to fetch users: ${e.message}"))
        }
    }
    
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam query: String
    ): ResponseEntity<Any> {
        return try {
            val users = userService.searchUsers(query)
            ResponseEntity.ok(users)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to search users: ${e.message}"))
        }
    }

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<Any> {
        return try {
            val user = userService.createUser(request)
            ResponseEntity.status(HttpStatus.CREATED).body(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to create user: ${e.message}"))
        }
    }

    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<Any> {
        return try {
            val user = userService.updateUser(userId, request)
            ResponseEntity.ok(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to update user: ${e.message}"))
        }
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: UUID): ResponseEntity<Any> {
        return try {
            userService.deleteUser(userId)
            ResponseEntity.ok(mapOf("message" to "User deleted successfully"))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to delete user: ${e.message}"))
        }    }

    // Legacy endpoints for backwards compatibility
    @PostMapping("/bulk")
    fun createUsers(@RequestBody users: List<User>): ResponseEntity<List<User>>{
        return ResponseEntity(
            userService.createUsers(users),
            HttpStatus.CREATED
        )
    }
}