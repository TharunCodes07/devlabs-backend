package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*


data class SubmitCourseScoreRequest(
    val userId: String,
    val reviewId: UUID,
    val projectId: UUID,
    val courseId: UUID,
    val scores: List<ParticipantScore>
)

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

data class InstructorInfo(
    val id: String,
    val name: String
)

data class CourseEvaluationSummary(
    val courseId: UUID,
    val courseName: String,
    val instructors: List<InstructorInfo>,
    val hasEvaluation: Boolean,
    val evaluationCount: Int
)


data class ProjectEvaluationSummary(
    val reviewId: UUID,
    val reviewName: String,
    val projectId: UUID,
    val projectTitle: String,
    val teamName: String,
    val courseEvaluations: List<CourseEvaluationSummary>
)
