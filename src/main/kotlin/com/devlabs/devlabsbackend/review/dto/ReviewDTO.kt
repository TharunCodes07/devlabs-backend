package com.devlabs.devlabsbackend.review.dto

import java.sql.Timestamp
import java.util.*

data class CreateReviewRequest(
    val title: String,
    val description: String? = null,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val projectId: UUID,
    val presentationDate: Timestamp? = null,
    val presentationDuration: Int? = null,
    val presentationLocation: String? = null
)

data class UpdateReviewRequest(
    val title: String? = null,
    val description: String? = null,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val presentationDate: Timestamp? = null,
    val presentationDuration: Int? = null,
    val presentationLocation: String? = null,
    val presentationNotes: String? = null
)

data class CompleteReviewRequest(
    val totalScore: Double? = null,
    val maxPossibleScore: Double? = null,
    val percentageScore: Double? = null,
    val overallFeedback: String? = null,
    val strengths: String? = null,
    val improvements: String? = null,
    val nextSteps: String? = null
)

data class TeamMemberScoreRequest(
    val individualScore: Double? = null,
    val maxPossibleScore: Double? = null,
    val percentageScore: Double? = null,
    val individualFeedback: String? = null,
    val strengths: String? = null,
    val improvements: String? = null
)
