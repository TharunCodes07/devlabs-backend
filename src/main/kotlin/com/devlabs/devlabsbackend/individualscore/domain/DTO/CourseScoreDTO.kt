package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*

/**
 * DTO for submitting course-specific scores
 */
data class SubmitCourseScoreRequest(
    val reviewId: UUID,
    val projectId: UUID,
    val courseId: UUID,
    val scores: List<ParticipantScore>
)

/**
 * DTO for course evaluation information
 */
data class CourseEvaluationInfo(
    val reviewId: UUID,
    val reviewName: String,
    val projectId: UUID,
    val projectTitle: String,
    val courseId: UUID,
    val courseName: String,
    val teamName: String,
    val startDate: java.time.LocalDate,
    val endDate: java.time.LocalDate,
    val hasExistingEvaluation: Boolean
)

/**
 * DTO for course evaluation summary
 */
data class CourseEvaluationSummary(
    val courseId: UUID,
    val courseName: String,
    val instructors: List<String>,
    val hasEvaluation: Boolean,
    val evaluationCount: Int
)

/**
 * DTO for project evaluation summary
 */
data class ProjectEvaluationSummary(
    val reviewId: UUID,
    val reviewName: String,
    val projectId: UUID,
    val projectTitle: String,
    val teamName: String,
    val courseEvaluations: List<CourseEvaluationSummary>
)
