package com.devlabs.devlabsbackend.review.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.review.domain.TeamMemberScore
import com.devlabs.devlabsbackend.review.dto.CompleteReviewRequest
import com.devlabs.devlabsbackend.review.dto.CreateReviewRequest
import com.devlabs.devlabsbackend.review.dto.TeamMemberScoreRequest
import com.devlabs.devlabsbackend.review.dto.UpdateReviewRequest
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import com.devlabs.devlabsbackend.review.repository.TeamMemberScoreRepository
import com.devlabs.devlabsbackend.rubric.repository.RubricRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
@Transactional
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val rubricRepository: RubricRepository,
    private val teamMemberScoreRepository: TeamMemberScoreRepository
) {

    fun createReview(reviewData: CreateReviewRequest, reviewerId: UUID): Review {
        val project = projectRepository.findById(reviewData.projectId).orElseThrow {
            NotFoundException("Project with id ${reviewData.projectId} not found")
        }
        
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        
        // Validate reviewer is course instructor or faculty
        if (reviewer.role !in listOf(Role.FACULTY, Role.ADMIN) || 
            !project.course.instructors.contains(reviewer)) {
            throw IllegalArgumentException("Only course instructors can create reviews")
        }
        val review = Review(
            title = reviewData.title,
            description = reviewData.description,
            startDate = reviewData.startDate,
            endDate = reviewData.endDate,
            project = project,
            reviewer = reviewer,
            presentationDate = reviewData.presentationDate,
            presentationDuration = reviewData.presentationDuration,            presentationLocation = reviewData.presentationLocation
        )
        
        return reviewRepository.save(review)
    }

    fun updateReview(reviewId: UUID, updateData: UpdateReviewRequest, reviewerId: UUID): Review {
        val review = getReviewById(reviewId)
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can update review")
        }
          // Update fields if provided
        updateData.title?.let { review.title = it }
        updateData.description?.let { review.description = it }
        updateData.startDate?.let { review.startDate = it }
        updateData.endDate?.let { review.endDate = it }
        updateData.presentationDate?.let { review.presentationDate = it }
        updateData.presentationDuration?.let { review.presentationDuration = it }
        updateData.presentationLocation?.let { review.presentationLocation = it }
        updateData.presentationNotes?.let { review.presentationNotes = it }
        
        review.updatedAt = Timestamp.from(Instant.now())
        
        return reviewRepository.save(review)
    }    fun startReview(reviewId: UUID, reviewerId: UUID): Review {
        val review = getReviewById(reviewId)
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can start review")
        }
        
        // Simply update the updatedAt timestamp to indicate activity
        review.updatedAt = Timestamp.from(Instant.now())
        
        return reviewRepository.save(review)
    }

    fun completeReview(reviewId: UUID, completionData: CompleteReviewRequest, reviewerId: UUID): Review {
        val review = getReviewById(reviewId)
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can complete review")
        }
        
        // Update review with completion data
        review.totalScore = completionData.totalScore
        review.maxPossibleScore = completionData.maxPossibleScore
        review.percentageScore = completionData.percentageScore ?: review.calculatePercentageScore()
        review.overallFeedback = completionData.overallFeedback
        review.strengths = completionData.strengths
        review.improvements = completionData.improvements
        review.nextSteps = completionData.nextSteps
        review.updatedAt = Timestamp.from(Instant.now())
        
        return reviewRepository.save(review)    }

    fun markAttendance(reviewId: UUID, attendanceData: Map<UUID, Boolean>, reviewerId: UUID): Review {
        val review = getReviewById(reviewId)
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can mark attendance")
        }
        
        // Get team members and update attendance
        val teamMembers = review.project.team.members
        attendanceData.forEach { (userId, attended) ->
            val user = userRepository.findById(userId).orElseThrow {
                NotFoundException("User with id $userId not found")
            }
            
            // Validate user is team member
            if (!teamMembers.contains(user)) {
                throw IllegalArgumentException("User $userId is not a team member")
            }
            
            review.attendance[user] = attended
        }
        
        review.updatedAt = Timestamp.from(Instant.now())
          return reviewRepository.save(review)
    }

    // Individual Team Member Scoring Methods
    
    fun scoreTeamMember(
        reviewId: UUID, 
        teamMemberId: UUID, 
        scoreData: TeamMemberScoreRequest, 
        reviewerId: UUID
    ): TeamMemberScore {
        val review = getReviewById(reviewId)
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        val teamMember = userRepository.findById(teamMemberId).orElseThrow {
            NotFoundException("User with id $teamMemberId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can score team members")
        }
        
        // Validate team member is part of the project team
        if (!review.project.team.members.contains(teamMember)) {
            throw IllegalArgumentException("User is not a member of the project team")        }
        
        // Find existing score or create new one
        val existingScore = teamMemberScoreRepository.findByReviewAndTeamMember(review, teamMember)
        
        val teamMemberScore = existingScore ?: TeamMemberScore(
            review = review,
            teamMember = teamMember
        )
        
        // Update score data
        teamMemberScore.individualScore = scoreData.individualScore
        teamMemberScore.maxPossibleScore = scoreData.maxPossibleScore
        teamMemberScore.percentageScore = scoreData.percentageScore ?: 
            if (scoreData.individualScore != null && scoreData.maxPossibleScore != null && scoreData.maxPossibleScore > 0) {
                (scoreData.individualScore / scoreData.maxPossibleScore) * 100
            } else null
        teamMemberScore.individualFeedback = scoreData.individualFeedback
        teamMemberScore.strengths = scoreData.strengths
        teamMemberScore.improvements = scoreData.improvements
        teamMemberScore.updatedAt = Timestamp.from(Instant.now())
        
        val savedScore = teamMemberScoreRepository.save(teamMemberScore)
          // Add to review if it's a new score
        if (existingScore == null) {
            review.teamMemberScores.add(savedScore)
        }
        
        return savedScore
    }
    
    fun getTeamMemberScores(reviewId: UUID): List<TeamMemberScore> {
        val review = getReviewById(reviewId)
        return teamMemberScoreRepository.findByReview(review)
    }
    
    fun getTeamMemberScore(reviewId: UUID, teamMemberId: UUID): TeamMemberScore? {
        val review = getReviewById(reviewId)
        val teamMember = userRepository.findById(teamMemberId).orElseThrow {
            NotFoundException("User with id $teamMemberId not found")
        }
        return teamMemberScoreRepository.findByReviewAndTeamMember(review, teamMember)
    }
    
    fun getTeamMemberScoreHistory(teamMemberId: UUID): List<TeamMemberScore> {
        val teamMember = userRepository.findById(teamMemberId).orElseThrow {
            NotFoundException("User with id $teamMemberId not found")
        }
        return teamMemberScoreRepository.findByTeamMember(teamMember)
    }
    
    fun getAverageScoreForTeamMember(teamMemberId: UUID): Double? {
        return teamMemberScoreRepository.findAverageScoreByTeamMember(teamMemberId)
    }
    
    fun deleteTeamMemberScore(reviewId: UUID, teamMemberId: UUID, reviewerId: UUID): Boolean {
        val review = getReviewById(reviewId)
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        val teamMember = userRepository.findById(teamMemberId).orElseThrow {
            NotFoundException("User with id $teamMemberId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can delete team member scores")        }
        
        val score = teamMemberScoreRepository.findByReviewAndTeamMember(review, teamMember)
        return if (score != null) {
            teamMemberScoreRepository.delete(score)
            review.teamMemberScores.remove(score)
            true
        } else {
            false
        }
    }    fun getReviewsByProject(projectId: UUID): List<Review> {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")        }
        val reviews = reviewRepository.findByProject(project)
        return reviews
    }    fun getReviewsByReviewer(reviewerId: UUID): List<Review> {
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        return reviewRepository.findByReviewer(reviewer)
    }

    private fun getReviewById(reviewId: UUID): Review {
        return reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
    }

    fun getAllReviews(): List<Review> {
        return reviewRepository.findAll()
    }
}
