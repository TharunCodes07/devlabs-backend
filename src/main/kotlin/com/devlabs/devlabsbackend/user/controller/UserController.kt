package com.devlabs.devlabsbackend.user.controller

import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController (private val userService: UserService)
{
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>>{
        return ResponseEntity(
            userService.getAllUsers(),
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
}