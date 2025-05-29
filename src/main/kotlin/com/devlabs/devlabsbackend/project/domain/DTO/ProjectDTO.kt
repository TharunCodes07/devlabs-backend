package com.devlabs.devlabsbackend.project.domain.DTO

import java.util.*

data class CreateProjectRequest(
    val title: String,
    val description: String,
    val objectives: String? = null,
    val teamId: UUID,
    val courseId: UUID
)

data class UpdateProjectRequest(
    val title: String? = null,
    val description: String? = null,
    val objectives: String? = null
)
