package com.devlabs.devlabsbackend.user.service

import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
){
    fun getAllUsers(): List<UserResponse>{
        return userRepository.findAll().map { user -> user.toUserResponse() }
    }
    
    fun searchUsers(query: String): List<UserResponse> {
        return userRepository.findByNameOrEmailContainingIgnoreCase(query).map { user -> user.toUserResponse() }
    }

    fun getAllUsersByRole(role: Role): List<UserResponse> {
        return userRepository.findByRole(role).map { user -> user.toUserResponse() }
    }

    fun createUser(user: User): User{
        return userRepository.save(user)
    }

    fun createUsers(users: List<User>): List<User> {
        return userRepository.saveAll(users)
    }
}

// Extension function to convert User to UserResponse
fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        name = this.name,
        email = this.email,
        profileId = this.profileId,
        image = this.image,
        role = this.role,
        phoneNumber = this.phoneNumber,
        isActive = this.isActive,
        createdAt = this.createdAt
    )
}