package com.devlabs.devlabsbackend.review.domain.DTO

import java.util.*

/**
 * Response DTO for review results
 */
data class ReviewResultsResponse(
    val reviewId: UUID,
    val reviewName: String,
    val projectId: UUID,
    val projectTitle: String,
    val isPublished: Boolean,
    val userRole: String,
    val canViewAllResults: Boolean,
    val results: List<ParticipantResult>
)

/**
 * Individual participant result
 */
data class ParticipantResult(
    val participantId: UUID,
    val participantName: String,
    val scores: List<CriterionResult>,
    val totalScore: Double,
    val maxPossibleScore: Double,
    val percentage: Double
)

/**
 * Score for a specific criterion
 */
data class CriterionResult(
    val criterionId: UUID,
    val criterionName: String,
    val score: Double,
    val maxScore: Double,
    val comment: String?
)

/**
 * Request DTO for getting review results
 */
data class ReviewResultsRequest(
    val userId: UUID,
    val projectId: UUID
)
