package com.devlabs.devlabsbackend.evaluation.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.evaluation.domain.dto.EvaluationRequest
import com.devlabs.devlabsbackend.evaluation.service.EvaluationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/evaluations")
class EvaluationController(
    private val evaluationService: EvaluationService
) {

    /**
     * Get all criteria for a specific review
     */
    @GetMapping("/review/{reviewId}/criteria")
    fun getReviewCriteria(@PathVariable reviewId: UUID): ResponseEntity<Any> {
        return try {
            val criteria = evaluationService.getReviewCriteria(reviewId)
            ResponseEntity.ok(criteria)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get review criteria: ${e.message}"))
        }
    }
    
    /**
     * Submit or update an evaluation for a project in a review
     */
    @PostMapping("/submit")
    fun submitEvaluation(
        @RequestBody request: EvaluationRequest,
        @RequestParam userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val evaluation = evaluationService.submitEvaluation(request, userId)
            ResponseEntity.ok(evaluation)
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
                .body(mapOf("error" to "Failed to submit evaluation: ${e.message}"))
        }
    }
    
    /**
     * Get evaluation results for a project in a review
     */
    @GetMapping("/results")
    fun getEvaluationResults(
        @RequestParam reviewId: UUID,
        @RequestParam projectId: UUID
    ): ResponseEntity<Any> {
        return try {
            val results = evaluationService.getEvaluationResults(reviewId, projectId)
            ResponseEntity.ok(results)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get evaluation results: ${e.message}"))
        }
    }
    
    /**
     * Get a single evaluation by ID
     */
    @GetMapping("/{evaluationId}")
    fun getEvaluationById(@PathVariable evaluationId: UUID): ResponseEntity<Any> {
        return try {
            val evaluation = evaluationService.getEvaluationById(evaluationId)
            ResponseEntity.ok(evaluation)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get evaluation: ${e.message}"))
        }
    }
}
