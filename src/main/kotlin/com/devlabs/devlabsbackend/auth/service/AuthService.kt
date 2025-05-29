package com.devlabs.devlabsbackend.auth.service

import com.devlabs.devlabsbackend.auth.domain.dto.*
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    fun register(request: RegisterRequest): AuthResponse {
        // Check if user already exists
        if (userRepository.findByEmail(request.email) != null) {
            return AuthResponse(false, "Email already registered")
        }

        if (userRepository.findByProfileId(request.profileId) != null) {
            return AuthResponse(false, "Profile ID already exists")
        }

        try {
            val user = User(
                name = request.name,
                email = request.email,
                profileId = request.profileId,
                password = passwordEncoder.encode(request.password),
                role = Role.valueOf(request.role.uppercase()),
                phoneNumber = request.phoneNumber,
                isActive = true,
                createdAt = Timestamp.from(Instant.now())
            )

            val savedUser = userRepository.save(user)
            val token = jwtService.generateToken(savedUser)

            return AuthResponse(
                success = true,
                message = "User registered successfully",
                data = LoginResponse(token = token, user = savedUser.toDTO())
            )
        } catch (e: Exception) {
            return AuthResponse(false, "Registration failed: ${e.message}")
        }
    }

    fun login(request: LoginRequest): AuthResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )

            val user = userRepository.findByEmail(request.email)
                ?: return AuthResponse(false, "User not found")

            if (!user.isActive) {
                return AuthResponse(false, "Account is deactivated")
            }

            val token = jwtService.generateToken(user)

            return AuthResponse(
                success = true,
                message = "Login successful",
                data = LoginResponse(token = token, user = user.toDTO())
            )
        } catch (e: BadCredentialsException) {
            return AuthResponse(false, "Invalid email or password")
        } catch (e: Exception) {
            return AuthResponse(false, "Login failed: ${e.message}")
        }
    }

    fun changePassword(request: ChangePasswordRequest): AuthResponse {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return AuthResponse(false, "User not authenticated")

        val user = userRepository.findByEmail(authentication.name)
            ?: return AuthResponse(false, "User not found")

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            return AuthResponse(false, "Current password is incorrect")
        }

        try {
            user.password = passwordEncoder.encode(request.newPassword)
            user.lastPasswordChange = Timestamp.from(Instant.now())
            userRepository.save(user)

            return AuthResponse(true, "Password changed successfully")
        } catch (e: Exception) {
            return AuthResponse(false, "Failed to change password: ${e.message}")
        }
    }

    fun getCurrentUser(): AuthResponse {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return AuthResponse(false, "User not authenticated")

        val user = userRepository.findByEmail(authentication.name)
            ?: return AuthResponse(false, "User not found")

        return AuthResponse(
            success = true,
            message = "User found",
            data = user.toDTO()
        )
    }

    fun refreshToken(): AuthResponse {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return AuthResponse(false, "User not authenticated")

        val user = userRepository.findByEmail(authentication.name)
            ?: return AuthResponse(false, "User not found")

        val newToken = jwtService.generateToken(user)

        return AuthResponse(
            success = true,
            message = "Token refreshed",
            data = mapOf("token" to newToken)
        )
    }

    private fun User.toDTO(): UserDTO {
        return UserDTO(
            id = this.id.toString(),
            name = this.name,
            email = this.email,
            profileId = this.profileId,
            role = this.role.name,
            phoneNumber = this.phoneNumber,
            image = this.image,
            isActive = this.isActive
        )
    }
}
