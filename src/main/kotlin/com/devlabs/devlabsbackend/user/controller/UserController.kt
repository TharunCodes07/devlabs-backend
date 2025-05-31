package com.devlabs.devlabsbackend.user.controller

import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController (private val userService: UserService)
{    @GetMapping
    fun getAllUsers(
        @RequestParam(required = false) role: String?
    ): ResponseEntity<Any> {
        return if (role != null) {
            try {
                val roleEnum = Role.valueOf(role.uppercase())
                val filteredUsers = userService.getAllUsersByRole(roleEnum)
                ResponseEntity(filteredUsers, HttpStatus.OK)
            } catch (e: IllegalArgumentException) {
                ResponseEntity.badRequest().body(mapOf("error" to "Invalid role: $role. Valid roles are: ${Role.values().joinToString(", ")}"))
            }
        } else {
            ResponseEntity(
                userService.getAllUsers(),
                HttpStatus.OK
            )
        }
    }
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam query: String
    ): ResponseEntity<Any> {
        return ResponseEntity(
            userService.searchUsers(query),
            HttpStatus.OK
        )
    }

    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<User>{
        return ResponseEntity(
            userService.createUser(user),
            HttpStatus.CREATED
        )
    }

    @PostMapping("/bulk")
    fun createUsers(@RequestBody users: List<User>): ResponseEntity<List<User>>{
        return ResponseEntity(
            userService.createUsers(users),
            HttpStatus.CREATED
        )
    }
}