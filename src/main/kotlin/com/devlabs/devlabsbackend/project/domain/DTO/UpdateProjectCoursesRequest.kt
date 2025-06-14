package com.devlabs.devlabsbackend.project.domain.DTO

import java.util.*

data class UpdateProjectCoursesRequest(
    val userId: UUID,
    val courseIds: List<UUID>
)

data class RejectProjectRequest(
    val userId: UUID,
    val reason: String? = null
)
