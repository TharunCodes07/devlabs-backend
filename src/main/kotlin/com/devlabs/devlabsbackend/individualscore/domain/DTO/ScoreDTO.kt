package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*

data class SubmitScoreRequest(
    val reviewId: UUID,
    val projectId: UUID,
    val scores: List<ParticipantScore>
)

data class ParticipantScore(
    val participantId: UUID,
    val criterionScores: List<CriterionScore>
)

data class CriterionScore(
    val criterionId: UUID,
    val score: Double,
    val comment: String? = null
)

data class IndividualScoreResponse(
    val id: UUID,
    val participantId: UUID,
    val participantName: String,
    val criterionId: UUID,
    val criterionName: String,
    val score: Double,
    val comment: String?,
    val reviewId: UUID,
    val projectId: UUID
)

data class ParticipantScoresSummary(
    val participantId: UUID,
    val participantName: String,
    val criterionScores: List<CriterionScoreDetail>,
    val totalScore: Double,
    val maxPossibleScore: Double,
    val percentage: Double
)

data class CriterionScoreDetail(
    val criterionId: UUID,
    val criterionName: String,
    val maxScore: Double,
    val score: Double,
    val comment: String?
)

