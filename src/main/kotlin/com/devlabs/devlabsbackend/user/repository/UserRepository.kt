package com.devlabs.devlabsbackend.user.repository

import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource(path = "user")
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByProfileId(profileId: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByProfileId(profileId: String): Boolean
}