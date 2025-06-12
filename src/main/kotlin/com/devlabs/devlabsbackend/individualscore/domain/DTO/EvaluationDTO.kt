package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*

/**
 * Request DTO for checking available evaluations
 */
data class AvailableEvaluationRequest(
    val userId: UUID,
    val semester: UUID? = null
)

/**
 * Response DTO for available evaluations
 */
data class AvailableEvaluationResponse(
    val evaluations: List<CourseEvaluationInfo>,
    val totalCount: Int
)

/**
 * DTO for review evaluation information
 */
data class ReviewEvaluationInfo(
    val reviewId: UUID,
    val reviewName: String,
    val dueDate: Date?,
    val projects: List<ProjectEvaluationInfo>
)

/**
 * DTO for project evaluation information
 */
data class ProjectEvaluationInfo(
    val projectId: UUID,
    val projectTitle: String,
    val teamName: String,
    val courses: List<CourseEvaluationInfo>
)
