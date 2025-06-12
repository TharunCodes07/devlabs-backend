package com.devlabs.devlabsbackend.evaluation.service

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.evaluation.domain.CriterionScore
import com.devlabs.devlabsbackend.evaluation.domain.Evaluation
import com.devlabs.devlabsbackend.evaluation.domain.EvaluationStatus
import com.devlabs.devlabsbackend.evaluation.domain.dto.*
import com.devlabs.devlabsbackend.evaluation.repository.CriterionScoreRepository
import com.devlabs.devlabsbackend.evaluation.repository.EvaluationRepository
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
class EvaluationService(
    private val evaluationRepository: EvaluationRepository,
    private val criterionScoreRepository: CriterionScoreRepository,
    private val reviewRepository: ReviewRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val criterionRepository: CriterionRepository
) {

    /**
     * Get all criteria for a specific review
     */
    @Transactional(readOnly = true)
    fun getReviewCriteria(reviewId: UUID): ReviewCriteriaResponse {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        // Get rubrics from review
        val rubrics = review.rubrics ?: throw NotFoundException("Review does not have associated rubrics")
        
        // Get criteria from rubrics
        val criteria = rubrics.criteria.map { criterion ->
            ReviewCriterionDetail(
                id = criterion.id ?: throw IllegalStateException("Criterion must have an ID"),
                name = criterion.name,
                description = criterion.description,
                maxScore = criterion.maxScore,
                isCommon = criterion.isCommon
            )
        }
        
        return ReviewCriteriaResponse(
            reviewId = reviewId,
            reviewName = review.name,
            criteria = criteria
        )
    }
    
    /**
     * Submit or update an evaluation for a project in a review
     */
    @Transactional
    fun submitEvaluation(request: EvaluationRequest, userId: UUID): EvaluationResponse {
        // Validate user
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        // Only faculty, managers, and admins can submit evaluations
        if (user.role != Role.FACULTY && user.role != Role.MANAGER && user.role != Role.ADMIN) {
            throw ForbiddenException("Only faculty, managers, and admins can submit evaluations")
        }
        
        // Get review and project
        val review = reviewRepository.findById(request.reviewId).orElseThrow {
            NotFoundException("Review with id ${request.reviewId} not found")
        }
        
        val project = projectRepository.findById(request.projectId).orElseThrow {
            NotFoundException("Project with id ${request.projectId} not found")
        }
        
        // Verify the project is part of the review
        if (!review.projects.contains(project)) {
            // Check if project is indirectly related to the review through courses
            val projectCourses = project.courses
            val reviewCourses = review.courses
            
            val hasCommonCourse = projectCourses.any { projectCourse -> 
                reviewCourses.any { reviewCourse -> projectCourse.id == reviewCourse.id }
            }
            
            if (!hasCommonCourse) {
                throw ForbiddenException("Project is not part of this review")
            }
        }
        
        // Check if faculty has permission for this project
        if (user.role == Role.FACULTY) {
            val isFacultyOfProject = project.courses.any { course ->
                course.instructors.contains(user)
            }
            
            if (!isFacultyOfProject) {
                throw ForbiddenException("Faculty can only evaluate projects for their courses")
            }
        }
        
        // Check if evaluation already exists or create new one
        val evaluation = evaluationRepository
            .findByReviewAndProjectAndEvaluator(review, project, user)
            .orElse(
                Evaluation(
                    review = review,
                    project = project,
                    evaluator = user
                )
            )
        
        // Update evaluation
        evaluation.comments = request.comments
        evaluation.status = EvaluationStatus.SUBMITTED
        evaluation.updatedAt = Timestamp.from(Instant.now())
        
        // Save evaluation first to get ID
        val savedEvaluation = evaluationRepository.save(evaluation)
        
        // Process criterion scores
        request.criterionScores.forEach { scoreRequest ->
            // Get criterion
            val criterion = criterionRepository.findById(scoreRequest.criterionId).orElseThrow {
                NotFoundException("Criterion with id ${scoreRequest.criterionId} not found")
            }
            
            // Verify criterion belongs to the review's rubrics
            if (criterion.rubrics?.id != review.rubrics?.id) {
                throw ForbiddenException("Criterion does not belong to this review's rubrics")
            }
            
            // Verify score is within range
            if (scoreRequest.score < 0 || scoreRequest.score > criterion.maxScore) {
                throw IllegalArgumentException("Score must be between 0 and ${criterion.maxScore}")
            }
            
            // Check if score already exists or create new one
            val criterionScore = criterionScoreRepository
                .findByEvaluationAndCriterion(savedEvaluation, criterion)
                .orElse(
                    CriterionScore(
                        evaluation = savedEvaluation,
                        criterion = criterion,
                        score = 0f
                    )
                )
            
            // Update score
            criterionScore.score = scoreRequest.score
            criterionScore.comment = scoreRequest.comment
            
            criterionScoreRepository.save(criterionScore)
        }
        
        // Reload scores after saving
        val savedScores = criterionScoreRepository.findByEvaluation(savedEvaluation)
        
        // Convert to response
        return savedEvaluation.toEvaluationResponse(savedScores)
    }
    
    /**
     * Get evaluation results for a project in a review
     */
    @Transactional(readOnly = true)
    fun getEvaluationResults(reviewId: UUID, projectId: UUID): EvaluationResultsResponse {
        // Get review and project
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        // Get all evaluations for this review and project
        val evaluations = evaluationRepository.findByReviewIdAndProjectId(reviewId, projectId)
            .filter { it.status == EvaluationStatus.SUBMITTED }
        
        if (evaluations.isEmpty()) {
            return EvaluationResultsResponse(
                reviewId = reviewId,
                reviewName = review.name,
                projectId = projectId,
                projectTitle = project.title,
                evaluations = emptyList(),
                averageScore = 0f,
                maxPossibleScore = 0f
            )
        }
        
        // Calculate max possible score from rubrics
        val rubrics = review.rubrics ?: throw NotFoundException("Review does not have associated rubrics")
        val maxPossibleScore = rubrics.criteria.sumOf { it.maxScore.toDouble() }.toFloat()
        
        // Calculate average score
        val evaluationSummaries = evaluations.map { evaluation ->
            val scores = criterionScoreRepository.findByEvaluation(evaluation)
            val totalScore = scores.sumOf { it.score.toDouble() }.toFloat()
              EvaluationSummary(
                id = evaluation.id ?: throw IllegalStateException("Evaluation must have an ID"),
                evaluatorId = evaluation.evaluator.id ?: throw IllegalStateException("Evaluator must have an ID"),
                evaluatorName = evaluation.evaluator.name,
                totalScore = totalScore,
                status = evaluation.status,
                updatedAt = evaluation.updatedAt.toInstant()
            )
        }
        
        val averageScore = if (evaluationSummaries.isNotEmpty()) {
            evaluationSummaries.sumOf { it.totalScore.toDouble() }.toFloat() / evaluationSummaries.size
        } else {
            0f
        }
        
        return EvaluationResultsResponse(
            reviewId = reviewId,
            reviewName = review.name,
            projectId = projectId,
            projectTitle = project.title,
            evaluations = evaluationSummaries,
            averageScore = averageScore,
            maxPossibleScore = maxPossibleScore
        )
    }
    
    /**
     * Get a single evaluation by ID
     */
    @Transactional(readOnly = true)
    fun getEvaluationById(evaluationId: UUID): EvaluationResponse {
        val evaluation = evaluationRepository.findById(evaluationId).orElseThrow {
            NotFoundException("Evaluation with id $evaluationId not found")
        }
        
        val scores = criterionScoreRepository.findByEvaluation(evaluation)
        return evaluation.toEvaluationResponse(scores)
    }
}

/**
 * Convert an Evaluation entity to an EvaluationResponse DTO
 */
fun Evaluation.toEvaluationResponse(scores: List<CriterionScore>): EvaluationResponse {
    val totalScore = scores.sumOf { it.score.toDouble() }.toFloat()
    val maxPossibleScore = scores.sumOf { it.criterion.maxScore.toDouble() }.toFloat()
    
    val criterionScores = scores.map { score ->
        CriterionScoreResponse(
            id = score.id ?: throw IllegalStateException("Score must have an ID"),
            criterionId = score.criterion.id ?: throw IllegalStateException("Criterion must have an ID"),
            criterionName = score.criterion.name,
            score = score.score,
            maxScore = score.criterion.maxScore,
            comment = score.comment
        )
    }
    
    return EvaluationResponse(
        id = this.id ?: throw IllegalStateException("Evaluation must have an ID"),
        reviewId = this.review.id ?: throw IllegalStateException("Review must have an ID"),
        reviewName = this.review.name,
        projectId = this.project.id ?: throw IllegalStateException("Project must have an ID"),
        projectTitle = this.project.title,        evaluatorId = this.evaluator.id ?: throw IllegalStateException("Evaluator must have an ID"),
        evaluatorName = this.evaluator.name,
        comments = this.comments,
        criterionScores = criterionScores,
        totalScore = totalScore,
        maxPossibleScore = maxPossibleScore,
        status = this.status,
        createdAt = this.createdAt.toInstant(),
        updatedAt = this.updatedAt.toInstant()
    )
}
