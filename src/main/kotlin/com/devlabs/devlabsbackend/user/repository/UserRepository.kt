package com.devlabs.devlabsbackend.user.repository

import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.domain.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "user")
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByProfileId(profileId: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByProfileId(profileId: String): Boolean
    
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByNameOrEmailContainingIgnoreCase(@Param("query") query: String): List<User>
    
    // Filter users by role
    fun findByRole(role: Role): List<User>
}