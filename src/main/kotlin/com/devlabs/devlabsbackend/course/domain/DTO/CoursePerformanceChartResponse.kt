package com.devlabs.devlabsbackend.course.domain.DTO

import java.time.LocalDate
import java.util.*

data class CoursePerformanceChartResponse(
    val reviewId: UUID,
    val reviewName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String, // "completed", "missed", "ongoing"
    val showResult: Boolean,
    val score: Double? = null,
    val totalScore: Double? = null,
    val scorePercentage: Double? = null,
    val courseName: String,
    val courseCode: String
)
