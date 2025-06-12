package com.devlabs.devlabsbackend.review.domain.DTO

/**
 * Response model for checking if a project has any reviews assigned (directly or indirectly)
 */
data class ReviewAssignmentResponse(
    /**
     * Indicates whether the project has any reviews assigned
     */
    val hasReview: Boolean,
    
    /**
     * Indicates how the review is associated with the project:
     * - DIRECT: Project is directly assigned to the review
     * - COURSE: Project's course is assigned to the review
     * - BATCH: Project's team members' batch is assigned to the review
     * - SEMESTER: Project's course's semester has courses with reviews
     * - NONE: No reviews found
     */
    val assignmentType: String,
      /**
     * List of reviews that are currently active (today falls between startDate and endDate)
     */
    val liveReviews: List<ReviewResponse>,
    
    /**
     * List of reviews that are upcoming (startDate is in the future)
     */
    val upcomingReviews: List<ReviewResponse> = emptyList(),
    
    /**
     * List of reviews that are completed (endDate is in the past)
     */
    val completedReviews: List<ReviewResponse>
)
