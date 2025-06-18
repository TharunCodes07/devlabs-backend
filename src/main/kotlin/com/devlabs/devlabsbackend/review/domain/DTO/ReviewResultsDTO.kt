package com.devlabs.devlabsbackend.review.domain.DTO

import java.util.*


data class ReviewResultsResponse(
    val id: UUID,
    val title: String,
    val results: List<StudentResult>
)


data class StudentResult(
    val id: String,
    val name: String,
    val individualScore: Double
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
