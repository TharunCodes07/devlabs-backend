package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.*


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

data class TeamMemberInfo(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)


data class CriterionInfo(
    val id: UUID,
    val name: String,
    val description: String,
    val maxScore: Double,
    val courseSpecific: Boolean
)

data class ParticipantScoreData(
    val participantId: String,
    val criterionScores: List<CriterionScoreData>
)

data class CriterionScoreData(
    val criterionId: UUID,
    val score: Double,
    val comment: String?
)
