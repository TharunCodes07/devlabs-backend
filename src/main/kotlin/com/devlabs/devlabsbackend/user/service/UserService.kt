package com.devlabs.devlabsbackend.user.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.user.domain.DTO.CreateUserRequest
import com.devlabs.devlabsbackend.user.domain.DTO.KeycloakSyncRequest
import com.devlabs.devlabsbackend.user.domain.DTO.UpdateUserRequest
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getAllUsersByRole(
        role: Role,
        page: Int,
        size: Int,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<UserResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable = PageRequest.of(page, size, sort)
        val userPage: Page<User> = userRepository.findByRolePaged(role, pageable)

        return PaginatedResponse(
            data = userPage.content.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page + 1,
                per_page = size,
                total_pages = userPage.totalPages,
                total_count = userPage.totalElements.toInt()
            )
        )
    }

    fun getAllUsersByRole(role: Role): List<UserResponse> {
        return userRepository.findByRole(role).map { user -> user.toUserResponse() }
    }

    fun getAllUsers(page: Int, size: Int, sortBy: String = "name", sortOrder: String = "asc"): PaginatedResponse<UserResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable = PageRequest.of(page, size, sort)
        val userPage: Page<User> = userRepository.findAll(pageable)

        return PaginatedResponse(
            data = userPage.content.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page + 1,
                per_page = size,
                total_pages = userPage.totalPages,
                total_count = userPage.totalElements.toInt()
            )
        )
    }

    fun searchUsers(query: String, page: Int, size: Int, sortBy: String = "name", sortOrder: String = "asc"): PaginatedResponse<UserResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable = PageRequest.of(page, size, sort)
        val userPage: Page<User> = userRepository.searchByNameOrEmailContainingIgnoreCase(query, pageable)

        return PaginatedResponse(
            data = userPage.content.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page + 1,
                per_page = size,
                total_pages = userPage.totalPages,
                total_count = userPage.totalElements.toInt()
            )
        )
    }

    fun updateUser(userId: String, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        if (request.email != user.email && userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        try {
            user.name = request.name
            user.email = request.email
            user.phoneNumber = request.phoneNumber
            user.role = Role.valueOf(request.role.uppercase())
            user.isActive = request.isActive

            val updatedUser = userRepository.save(user)
            return updatedUser.toUserResponse()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        } catch (e: Exception) {
            throw RuntimeException("Failed to update user: ${e.message}")
        }
    }    fun deleteUser(userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        userRepository.delete(user)
    }

    fun bulkDeleteUsers(userIds: List<String>) {
        val users = userRepository.findAllById(userIds)
        if (users.isEmpty()) {
            throw NotFoundException("No users found for the provided IDs")
        }
        userRepository.deleteAll(users)
    }

    fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }
        try {
            val user = User(
                name = request.name,
                email = request.email,
                role = Role.valueOf(request.role.uppercase()),
                phoneNumber = request.phoneNumber?.takeIf { it.isNotBlank() },
                isActive = request.isActive,
                createdAt = Timestamp.from(Instant.now())
            )

            val savedUser = userRepository.save(user)
            return savedUser.toUserResponse()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        } catch (e: Exception) {
            throw RuntimeException("Failed to create user: ${e.message}")
        }
    }


    fun getUserById(userId: String): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        return user.toUserResponse()
    }

    fun checkUserExists(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }    fun createUserFromKeycloakSync(request: KeycloakSyncRequest): UserResponse {
        try {
            val existingUser = userRepository.findById(request.id).orElse(null)
            
            val user = if (existingUser != null) {
                existingUser.apply {
                    name = request.name
                    email = request.email
                    role = Role.valueOf(request.role.trim().uppercase())
                    phoneNumber = request.phoneNumber.takeIf { it.isNotBlank() }
                    isActive = request.isActive
                }
            } else {
                User(
                    id = request.id,
                    name = request.name,
                    email = request.email,
                    role = Role.valueOf(request.role.trim().uppercase()),
                    phoneNumber = request.phoneNumber.takeIf { it.isNotBlank() },
                    isActive = request.isActive,
                    createdAt = Timestamp.from(Instant.now())
                )
            }

            val savedUser = userRepository.save(user)
            return savedUser.toUserResponse()
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("No enum constant") == true) {
                throw IllegalArgumentException("Invalid role: '${request.role}'. Valid roles are: ${Role.values().joinToString()}")
            }
            throw IllegalArgumentException("Invalid role: ${request.role}")
        } catch (e: Exception) {
            throw RuntimeException("Failed to sync user from Keycloak: ${e.message}")
        }
    }

    private fun createSort(sortBy: String, sortOrder: String): Sort {
        val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return Sort.by(direction, sortBy)

    }
}

fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id?.toString(),
        name = this.name,
        email = this.email,
        profileId = this.profileId,
        image = this.image,
        role = this.role.name,
        phoneNumber = this.phoneNumber,
        isActive = this.isActive,
        createdAt = this.createdAt.toString()
    )
}