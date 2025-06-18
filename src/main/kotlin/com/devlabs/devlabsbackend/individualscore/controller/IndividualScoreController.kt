package com.devlabs.devlabsbackend.individualscore.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.individualscore.domain.DTO.AvailableEvaluationRequest
import com.devlabs.devlabsbackend.individualscore.domain.DTO.SubmitCourseScoreRequest
import com.devlabs.devlabsbackend.individualscore.domain.DTO.SubmitScoreRequest
import com.devlabs.devlabsbackend.individualscore.domain.DTO.UserIdRequest
import com.devlabs.devlabsbackend.individualscore.service.IndividualScoreService
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/individualScore")
class IndividualScoreController(
    private val individualScoreService: IndividualScoreService,
    private val reviewRepository: ReviewRepository,
    private val projectRepository: ProjectRepository
) {

    @PostMapping("/review/{reviewId}/project/{projectId}/summary")
    @Transactional(readOnly = true)
    fun getProjectEvaluationSummary(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            val review = reviewRepository.findById(reviewId).orElseThrow {
                NotFoundException("Review with id $reviewId not found")
            }

            val project = projectRepository.findById(projectId).orElseThrow {
                NotFoundException("Project with id $projectId not found")
            }
            individualScoreService.checkScoreAccessRights(request.userId, review, project)

            val summary = individualScoreService.getProjectEvaluationSummary(reviewId, projectId)
            ResponseEntity.ok(summary)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: ForbiddenException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get evaluation summary: ${e.message}"))
        }
    }

    @PostMapping("/review/{reviewId}/project/{projectId}/course/{courseId}/data")
    fun getCourseEvaluationData(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable courseId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            val data = individualScoreService.getCourseEvaluationData(reviewId, projectId, courseId, request.userId)
            ResponseEntity.ok(data)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: ForbiddenException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get course evaluation data: ${e.message}"))
        }
    }

    @PostMapping("/course")
    fun submitCourseScores(
        @RequestBody request: SubmitCourseScoreRequest
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.submitCourseScores(request, request.userId)
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("success" to true, "count" to scores.size))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: ForbiddenException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to submit course scores: ${e.message}"))
        }
    }


}