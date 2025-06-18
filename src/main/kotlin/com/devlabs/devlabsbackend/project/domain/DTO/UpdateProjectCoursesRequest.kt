package com.devlabs.devlabsbackend.project.domain.DTO

import java.util.*

data class UpdateProjectCoursesRequest(
    val userId: String,
    val courseIds: List<UUID>
)

data class RejectProjectRequest(
    val userId: String,
    val reason: String? = null
)
