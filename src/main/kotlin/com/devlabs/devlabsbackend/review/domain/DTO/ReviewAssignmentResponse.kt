package com.devlabs.devlabsbackend.review.domain.DTO

data class ReviewAssignmentResponse(

    val hasReview: Boolean,
    val assignmentType: String,
    val liveReviews: List<ReviewResponse>,
    val upcomingReviews: List<ReviewResponse> = emptyList(),
    val completedReviews: List<ReviewResponse>
)
