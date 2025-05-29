package com.devlabs.devlabsbackend.auth.domain.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserDTO
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val profileId: String,
    val password: String,
    val phoneNumber: String,
    val role: String = "STUDENT"
)

data class UserDTO(
    val id: String?,
    val name: String,
    val email: String,
    val profileId: String,
    val role: String,
    val phoneNumber: String,
    val image: String?,
    val isActive: Boolean
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)
