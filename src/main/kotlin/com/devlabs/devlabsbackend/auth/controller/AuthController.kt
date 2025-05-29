package com.devlabs.devlabsbackend.auth.controller

import com.devlabs.devlabsbackend.auth.domain.dto.AuthResponse
import com.devlabs.devlabsbackend.auth.domain.dto.LoginRequest
import com.devlabs.devlabsbackend.auth.domain.dto.RegisterRequest
import com.devlabs.devlabsbackend.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    @PostMapping("/refresh-token")
    fun refreshToken(): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken()
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }
}
