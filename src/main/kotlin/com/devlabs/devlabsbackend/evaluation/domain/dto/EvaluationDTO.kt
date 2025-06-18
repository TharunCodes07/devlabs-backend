package com.devlabs.devlabsbackend.evaluation.domain.dto

import com.devlabs.devlabsbackend.evaluation.domain.EvaluationStatus
import java.time.Instant
import java.util.*

data class EvaluationRequest(
    val reviewId: UUID,
    val projectId: UUID,
    val comments: String?,
    val criterionScores: List<CriterionScoreRequest>
)

data class CriterionScoreRequest(
    val criterionId: UUID,
    val score: Float,
    val comment: String?
)

data class EvaluationResponse(
    val id: UUID,
    val reviewId: UUID,
    val reviewName: String,
    val projectId: UUID,
    val projectTitle: String,
    val evaluatorId: String,
    val evaluatorName: String,
    val comments: String?,
    val criterionScores: List<CriterionScoreResponse>,
    val totalScore: Float,
    val maxPossibleScore: Float,
    val status: EvaluationStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class CriterionScoreResponse(
    val id: UUID,
    val criterionId: UUID,
    val criterionName: String,
    val score: Float,
    val maxScore: Float,
    val comment: String?
)

data class ReviewCriteriaResponse(
    val reviewId: UUID,
    val reviewName: String,
    val criteria: List<ReviewCriterionDetail>
)

data class ReviewCriterionDetail(
    val id: UUID,
    val name: String,
    val description: String,
    val maxScore: Float,
    val isCommon: Boolean
)

data class EvaluationResultsResponse(
    val reviewId: UUID,
    val reviewName: String,
    val projectId: UUID,
    val projectTitle: String,
    val evaluations: List<EvaluationSummary>,
    val averageScore: Float,
    val maxPossibleScore: Float
)

data class EvaluationSummary(
    val id: UUID,
    val evaluatorId: String,
    val evaluatorName: String,
    val totalScore: Float,
    val status: EvaluationStatus,
    val updatedAt: Instant
)
