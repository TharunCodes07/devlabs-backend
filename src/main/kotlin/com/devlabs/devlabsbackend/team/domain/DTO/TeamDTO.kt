package com.devlabs.devlabsbackend.team.domain.DTO

import com.devlabs.devlabsbackend.user.domain.User
import java.sql.Timestamp
import java.util.*

data class CreateTeamRequest(
    val name: String,
    val description: String? = null,
    val memberIds: List<UUID> = emptyList(),
    val creatorId: UUID
)

data class TeamResponse(
    val id: UUID?,
    val name: String,
    val description: String?,
    val members: Set<User>,
    val projectCount: Int,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
