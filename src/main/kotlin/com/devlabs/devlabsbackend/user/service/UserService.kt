package com.devlabs.devlabsbackend.user.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.user.domain.DTO.CreateUserRequest
import com.devlabs.devlabsbackend.user.domain.DTO.UpdateUserRequest
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse

import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
){

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
    
    fun getAllUsersByRole(role: Role, page: Int, size: Int, sortBy: String = "name", sortOrder: String = "asc"): PaginatedResponse<UserResponse> {
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

    private fun createSort(sortBy: String, sortOrder: String): Sort {
        val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return Sort.by(direction, sortBy)
    }

    // Legacy methods for backwards compatibility
    fun getAllUsers(): List<UserResponse>{
        return userRepository.findAll().map { user -> user.toUserResponse() }
    }
      fun searchUsers(query: String): List<UserResponse> {
        return userRepository.findByNameOrEmailContainingIgnoreCase(query).map { user -> user.toUserResponse() }
    }

    fun searchStudents(query: String): List<UserResponse> {
        return userRepository.findByNameOrEmailContainingIgnoreCase(query)
            .filter { it.role == Role.STUDENT }
            .map { it.toUserResponse() }
    }

    fun getAllUsersByRole(role: Role): List<UserResponse> {
        return userRepository.findByRole(role).map { user -> user.toUserResponse() }
    }

    fun createUser(request: CreateUserRequest): UserResponse {
        // Check if user already exists
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        try {
            val user = User(
                name = request.name,
                email = request.email,
                password = passwordEncoder.encode(request.password),
                role = Role.valueOf(request.role.uppercase()),
                phoneNumber = request.phoneNumber,
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

    fun updateUser(userId: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        // Check if email is being changed and if it's already taken by another user
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
    }    fun deleteUser(userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        userRepository.delete(user)
    }

    fun bulkDeleteUsers(userIds: List<UUID>) {
        val users = userRepository.findAllById(userIds)
        if (users.isEmpty()) {
            throw NotFoundException("No users found for the provided IDs")
        }
        userRepository.deleteAll(users)
    }
    
    fun createUser(user: User): User{
        return userRepository.save(user)
    }
    
    fun getUserById(userId: UUID): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        return user.toUserResponse()
    }

    fun createUsers(users: List<User>): List<User> {
    return userRepository.saveAll(users)
    }

    @Transactional(readOnly = true)
    fun searchFaculty(query: String): List<UserResponse> {
        // Get all users with role FACULTY whose name or email contains the query
        val allUsers = userRepository.findByNameOrEmailContainingIgnoreCase(query)
        val facultyUsers = allUsers.filter { it.role == Role.FACULTY }
        
        return facultyUsers.map { it.toUserResponse() }
    }
}

// Extension function to convert User to UserResponse
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