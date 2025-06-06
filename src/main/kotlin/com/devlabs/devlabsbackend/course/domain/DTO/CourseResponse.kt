package com.devlabs.devlabsbackend.course.domain.DTO

import java.util.UUID

data class CourseResponse(
    val id: UUID,
    val name: String,
    val code: String = "",
    val description: String = ""
)
