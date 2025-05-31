package com.devlabs.devlabsbackend.user.domain.DTO

import com.devlabs.devlabsbackend.user.domain.Role
import java.sql.Timestamp
import java.util.*

data class UserResponse(
    val id: UUID?,
    val name: String,
    val email: String,
    val profileId: String,
    val image: String?,
    val role: Role,
    val phoneNumber: String,
    val isActive: Boolean,
    val createdAt: Timestamp
)
