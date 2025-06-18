package com.devlabs.devlabsbackend.course.domain.DTO

import java.util.*

data class CourseResponse(
    val id: UUID,
    val name: String,
    val code: String = "",
    val description: String = ""
)
