package com.devlabs.devlabsbackend.individualscore.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.individualscore.domain.DTO.AvailableEvaluationRequest
import com.devlabs.devlabsbackend.individualscore.domain.DTO.SubmitCourseScoreRequest
import com.devlabs.devlabsbackend.individualscore.domain.DTO.SubmitScoreRequest
import com.devlabs.devlabsbackend.individualscore.service.IndividualScoreService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/individualScore")
class IndividualScoreController(
    private val individualScoreService: IndividualScoreService
) {
    
    @PostMapping
    fun submitScores(
        @RequestBody request: SubmitScoreRequest,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.submitScores(request, userId)
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
                .body(mapOf("error" to "Failed to submit scores: ${e.message}"))
        }
    }
    
    @GetMapping("/{scoreId}")
    fun getScoreById(@PathVariable scoreId: UUID): ResponseEntity<Any> {
        return try {
            val score = individualScoreService.getIndividualScoreById(scoreId)
            ResponseEntity.ok(score)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get score: ${e.message}"))
        }
    }
    
    @GetMapping("/review/{reviewId}/project/{projectId}/participant/{participantId}")
    fun getScoresForParticipant(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable participantId: UUID
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.getScoresForParticipant(reviewId, projectId, participantId)
            ResponseEntity.ok(scores)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get scores: ${e.message}"))
        }
    }
    
    @GetMapping("/review/{reviewId}/project/{projectId}")
    fun getScoresForProject(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.getScoresForProject(reviewId, projectId)
            ResponseEntity.ok(scores)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get scores: ${e.message}"))
        }
    }
    
    @DeleteMapping("/review/{reviewId}/project/{projectId}/participant/{participantId}")
    fun deleteScoresForParticipant(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable participantId: UUID,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val result = individualScoreService.deleteScoresForParticipant(
                reviewId, projectId, participantId, userId
            )
            ResponseEntity.ok(mapOf("success" to result))
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
                .body(mapOf("error" to "Failed to delete scores: ${e.message}"))
        }
    }

    /**
     * Submit course-specific scores - Faculty can only submit for courses they teach
     */    @PostMapping("/course")
    fun submitCourseScores(
        @RequestBody request: SubmitCourseScoreRequest,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.submitCourseScores(request, userId)
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

    /**
     * Get available evaluations for the current user (faculty gets only their courses)
     */    @PostMapping("/evaluations/available")
    fun getAvailableEvaluations(
        @RequestBody request: AvailableEvaluationRequest
    ): ResponseEntity<Any> {
        return try {
            val evaluations = individualScoreService.getAvailableEvaluations(request)
            ResponseEntity.ok(evaluations)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: ForbiddenException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get available evaluations: ${e.message}"))
        }
    }

    /**
     * Get course-specific scores for a participant
     */
    @GetMapping("/review/{reviewId}/project/{projectId}/course/{courseId}/participant/{participantId}")
    fun getCourseScoresForParticipant(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable courseId: UUID,
        @PathVariable participantId: UUID
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.getCourseScoresForParticipant(reviewId, projectId, participantId, courseId)
            ResponseEntity.ok(scores)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get course scores: ${e.message}"))
        }
    }

    /**
     * Get course-specific scores for a project (all team members)
     */
    @GetMapping("/review/{reviewId}/project/{projectId}/course/{courseId}")
    fun getCourseScoresForProject(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable courseId: UUID
    ): ResponseEntity<Any> {
        return try {
            val scores = individualScoreService.getCourseScoresForProject(reviewId, projectId, courseId)
            ResponseEntity.ok(scores)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get course scores: ${e.message}"))
        }
    }

    /**
     * Get project evaluation summary by course
     */
    @GetMapping("/review/{reviewId}/project/{projectId}/summary")
    fun getProjectEvaluationSummary(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID
    ): ResponseEntity<Any> {
        return try {
            val summary = individualScoreService.getProjectEvaluationSummary(reviewId, projectId)
            ResponseEntity.ok(summary)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get evaluation summary: ${e.message}"))
        }
    }

    /**
     * Delete course-specific scores for a participant
     */
    @DeleteMapping("/review/{reviewId}/project/{projectId}/course/{courseId}/participant/{participantId}")
    fun deleteCourseScoresForParticipant(
        @PathVariable reviewId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable courseId: UUID,
        @PathVariable participantId: UUID,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val result = individualScoreService.deleteCourseScoresForParticipant(
                reviewId, projectId, participantId, courseId, userId
            )
            ResponseEntity.ok(mapOf("success" to result))
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
                .body(mapOf("error" to "Failed to delete course scores: ${e.message}"))
        }
    }
}