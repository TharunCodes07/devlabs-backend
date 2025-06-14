package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*

/**
 * DTO for comprehensive course evaluation data
 */
data class CourseEvaluationData(
    val courseId: UUID,
    val courseName: String,
    val projectId: UUID,
    val reviewId: UUID,
    val teamMembers: List<TeamMemberInfo>,
    val criteria: List<CriterionInfo>,
    val existingScores: List<ParticipantScoreData>,
    val isPublished: Boolean
)

/**
 * DTO for team member information
 */
data class TeamMemberInfo(
    val id: UUID,
    val name: String,
    val email: String,
    val role: String
)

/**
 * DTO for criterion information
 */
data class CriterionInfo(
    val id: UUID,
    val name: String,
    val description: String,
    val maxScore: Double,
    val courseSpecific: Boolean
)

/**
 * DTO for participant score data
 */
data class ParticipantScoreData(
    val participantId: UUID,
    val criterionScores: List<CriterionScoreData>
)

/**
 * DTO for criterion score data
 */
data class CriterionScoreData(
    val criterionId: UUID,
    val score: Double,
    val comment: String?
)
