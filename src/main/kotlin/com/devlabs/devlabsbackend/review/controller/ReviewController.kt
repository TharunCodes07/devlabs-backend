package com.devlabs.devlabsbackend.review.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.review.domain.DTO.CreateReviewRequest
import com.devlabs.devlabsbackend.review.domain.DTO.ReviewPublicationResponse
import com.devlabs.devlabsbackend.review.domain.DTO.ReviewResultsRequest
import com.devlabs.devlabsbackend.review.domain.DTO.UserIdRequest
import com.devlabs.devlabsbackend.review.service.ReviewService
import com.devlabs.devlabsbackend.review.service.toReviewResponse
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/review")
class ReviewController(
    private val reviewService: ReviewService,
    private val userRepository: UserRepository
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


    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: Map<String, String>
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




    @GetMapping("/search/{userId}")
    fun searchReviews(
        @PathVariable userId: String,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) courseId: UUID?,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "startDate") sortBy: String,
        @RequestParam(defaultValue = "desc") sortOrder: String
    ): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.searchReviews(userId, name, courseId, status, page, size, sortBy, sortOrder)
            ResponseEntity.ok(reviews)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search reviews: ${e.message}"))
        }
    }

    @GetMapping("/user")
    fun getReviewsForUser(
        @RequestParam userId: String,
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

    @GetMapping("/{reviewId}/publication")
    fun getPublicationStatus(
        @PathVariable reviewId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            val user = userRepository.findById(request.userId).orElseThrow {
                NotFoundException("User with id ${request.userId} not found")
            }
            val publicationStatus = reviewService.getPublicationStatus(reviewId)
            val review = reviewService.getReviewById(reviewId)

            // Check if user can publish based on role and ownership
            val canPublish = when (user.role) {
                Role.ADMIN, Role.MANAGER -> true
                Role.FACULTY -> review.createdBy?.id == user.id
                else -> false
            }

            val responseWithPermission = ReviewPublicationResponse(
                reviewId = publicationStatus.reviewId,
                reviewName = publicationStatus.reviewName,
                isPublished = publicationStatus.isPublished,
                publishDate = publicationStatus.publishDate,
                canPublish = canPublish
            )

            ResponseEntity.ok(responseWithPermission)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get publication status: ${e.message}"))
        }
    }
    @PostMapping("/{reviewId}/publish")
    fun publishReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            val publicationStatus = reviewService.publishReview(reviewId, request.userId)
            ResponseEntity.ok(publicationStatus)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: ForbiddenException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to publish review: ${e.message}"))
        }
    }


    @PostMapping("/{reviewId}/unpublish")
    fun unpublishReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            val publicationStatus = reviewService.unpublishReview(reviewId, request.userId)
            ResponseEntity.ok(publicationStatus)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: ForbiddenException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to unpublish review: ${e.message}"))
        }
    }

}