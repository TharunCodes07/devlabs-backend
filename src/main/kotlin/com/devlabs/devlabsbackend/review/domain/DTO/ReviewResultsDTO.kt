package com.devlabs.devlabsbackend.review.domain.DTO

import java.util.*


data class ReviewResultsResponse(
    val id: UUID,
    val title: String,
    val projectTitle: String,
    val reviewName: String,
    val isPublished: Boolean,
    val canViewAllResults: Boolean,
    val results: List<StudentResult>
)


data class StudentResult(
    val id: String,
    val name: String,
    val studentId: String,
    val studentName: String,
    val individualScore: Double,
    val totalScore: Double,
    val maxPossibleScore: Double,
    val percentage: Double,
    val scores: List<CriterionResult>
)

data class CriterionResult(
    val criterionId: UUID,
    val criterionName: String,
    val score: Double,
    val maxScore: Double,
    val comment: String?
)

data class ReviewResultsRequest(
    val userId: String,
    val projectId: UUID
)
