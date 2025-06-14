package com.devlabs.devlabsbackend.review.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.review.domain.DTO.ReviewPublicationResponse
import com.devlabs.devlabsbackend.review.domain.DTO.UserIdRequest
import com.devlabs.devlabsbackend.review.service.ReviewService
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/review")
class ReviewPublicationController(
    private val reviewService: ReviewService,
    private val userRepository: UserRepository
) {
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
            
            // Create a new response with canPublish flag based on user role
            val responseWithPermission = ReviewPublicationResponse(
                reviewId = publicationStatus.reviewId,
                reviewName = publicationStatus.reviewName,
                isPublished = publicationStatus.isPublished,
                publishDate = publicationStatus.publishDate,
                canPublish = user.role == Role.ADMIN || user.role == Role.MANAGER
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
