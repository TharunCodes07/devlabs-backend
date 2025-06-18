package com.devlabs.devlabsbackend.user.domain.DTO

data class UserResponse(
    val id: String?,
    val name: String,
    val email: String,
    val profileId: String? = null,
    val image: String?,
    val role: String,
    val phoneNumber: String,
    val isActive: Boolean,
    val createdAt: String
)

data class CreateUserRequest(
    val name: String,
    val email: String,
    val role: String,
    val phoneNumber: String,
    val isActive: Boolean = true
)

data class UpdateUserRequest(
    val name: String,
    val email: String,
    val role: String,
    val phoneNumber: String,
    val isActive: Boolean
)


