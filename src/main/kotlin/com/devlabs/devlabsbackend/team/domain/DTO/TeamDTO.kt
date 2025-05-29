package com.devlabs.devlabsbackend.team.domain.DTO

import java.util.*

data class CreateTeamRequest(
    val name: String,
    val description: String? = null,
    val memberIds: List<UUID> = emptyList(),
    val creatorId: UUID
)
