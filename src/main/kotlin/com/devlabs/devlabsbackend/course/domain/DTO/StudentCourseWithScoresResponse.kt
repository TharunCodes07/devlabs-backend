package com.devlabs.devlabsbackend.course.domain.DTO

import java.util.*

data class StudentCourseWithScoresResponse(
    val id: UUID,
    val name: String,
    val code: String = "",
    val description: String = "",
    val averageScorePercentage: Double = 0.0,
    val reviewCount: Int = 0
)
