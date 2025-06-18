package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*


data class AvailableEvaluationRequest(
    val userId: String,
    val semester: UUID? = null
)

data class AvailableEvaluationResponse(
    val evaluations: List<CourseEvaluationInfo>,
    val totalCount: Int
)

data class ReviewEvaluationInfo(
    val reviewId: UUID,
    val reviewName: String,
    val dueDate: Date?,
    val projects: List<ProjectEvaluationInfo>
)


data class ProjectEvaluationInfo(
    val projectId: UUID,
    val projectTitle: String,
    val teamName: String,
    val courses: List<CourseEvaluationInfo>
)
