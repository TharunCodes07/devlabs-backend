package com.devlabs.devlabsbackend.review.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.review.domain.DTO.CreateReviewRequest
import com.devlabs.devlabsbackend.review.domain.DTO.ReviewResultsRequest
import com.devlabs.devlabsbackend.review.domain.DTO.UpdateReviewRequest
import com.devlabs.devlabsbackend.review.service.ReviewService
import com.devlabs.devlabsbackend.review.service.toReviewResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/review")
class ReviewController(
    private val reviewService: ReviewService
) {
    
    @PostMapping
    fun createReview(
        @RequestBody request: CreateReviewRequest
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.createReview(request, request.userId)
            ResponseEntity.status(HttpStatus.CREATED).body(review.toReviewResponse())
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
                .body(mapOf("error" to "Failed to create review: ${e.message}"))
        }
    }
      @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: UpdateReviewRequest
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.updateReview(reviewId, request, request.userId)
            ResponseEntity.ok(review.toReviewResponse())
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
                .body(mapOf("error" to "Failed to update review: ${e.message}"))
        }
    }
      @GetMapping("/{reviewId}")
    fun getReviewById(@PathVariable reviewId: UUID): ResponseEntity<Any> {
        return try {
            val review = reviewService.getReviewById(reviewId)
            ResponseEntity.ok(review)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get review: ${e.message}"))
        }
    }
    
    @GetMapping
    fun getAllReviews(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "startDate") sortBy: String,
        @RequestParam(required = false, defaultValue = "desc") sortOrder: String
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getAllReviews(page, size, sortBy, sortOrder)
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews: ${e.message}"))
        }
    }
      @GetMapping("/user")
    fun getReviewsForUser(
        @RequestParam userId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getReviewsForUser(userId, page, size)
            ResponseEntity.ok(reviews)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews: ${e.message}"))
        }
    }
    
    @GetMapping("/allForUser")
    fun getAllReviewsForUser(
        @RequestParam userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getAllReviewsForUser(userId)
            ResponseEntity.ok(reviews)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews: ${e.message}"))
        }
    }
    
    @GetMapping("/recentlyCompleted")
    fun getRecentlyCompletedReviews(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getRecentlyCompletedReviews(page, size)
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get recently completed reviews: ${e.message}"))
        }
    }
    
    @GetMapping("/live")
    fun getLiveReviews(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getLiveReviews(page, size)
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get live reviews: ${e.message}"))
        }
    }
    
    @GetMapping("/upcoming")
    fun getUpcomingReviews(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getUpcomingReviews(page, size)
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get upcoming reviews: ${e.message}"))
        }
    }
    
    @GetMapping("/project/{projectId}")
    fun getReviewsForProject(@PathVariable projectId: UUID): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getReviewsForProject(projectId)
            ResponseEntity.ok(reviews)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews: ${e.message}"))
        }
    }
    
    @GetMapping("/course/{courseId}/active")
    fun getActiveReviewsForCourse(@PathVariable courseId: UUID): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getActiveReviewsForCourse(courseId)
            ResponseEntity.ok(reviews)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get active reviews: ${e.message}"))
        }
    }    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: Map<String, UUID>
    ): ResponseEntity<Any> {
        return try {
            val userId = request["userId"] ?: throw IllegalArgumentException("userId is required")
            val result = reviewService.deleteReview(reviewId, userId)
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
                .body(mapOf("error" to "Failed to delete review: ${e.message}"))
        }
    }
    
    @GetMapping("/search")
    fun searchReviews(
        @RequestParam name: String?,
        @RequestParam(required = false) courseId: UUID?,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "startDate") sortBy: String,
        @RequestParam(defaultValue = "desc") sortOrder: String
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.searchReviews(name, courseId, status, page, size, sortBy, sortOrder)
            ResponseEntity.ok(reviews)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search reviews: ${e.message}"))
        }
    }

    /**
     * Checks if a project has any reviews assigned to it (directly or indirectly)
     * and returns those that are live or completed.
     * 
     * @param projectId UUID of the project to check
     * @return ReviewAssignmentResponse containing information about assigned reviews
     */
    @GetMapping("/project-assignment/{projectId}")
    fun checkProjectReviewAssignment(
        @PathVariable projectId: UUID
    ): ResponseEntity<Any> {
        return try {
            val response = reviewService.checkProjectReviewAssignment(projectId)
            ResponseEntity.ok(response)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to check project review assignment: ${e.message}"))
        }
    }    /**
     * Get review results for a specific review and project
     * Access control:
     * - Students: Only see their own scores and only if review is published
     * - Faculty: See all scores for projects in courses they teach
     * - Admin/Manager: See all scores regardless of publication status
     */
    @PostMapping("/{reviewId}/results")
    fun getReviewResults(
        @PathVariable reviewId: UUID,
        @RequestBody request: ReviewResultsRequest
    ): ResponseEntity<Any> {
        return try {
            val results = reviewService.getReviewResults(reviewId, request.projectId, request.userId)
            ResponseEntity.ok(results)
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
                .body(mapOf("error" to "Failed to get review results: ${e.message}"))
        }
    }
}
