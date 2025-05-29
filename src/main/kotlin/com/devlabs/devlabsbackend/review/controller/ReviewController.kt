package com.devlabs.devlabsbackend.review.controller

import com.devlabs.devlabsbackend.review.dto.CompleteReviewRequest
import com.devlabs.devlabsbackend.review.dto.CreateReviewRequest
import com.devlabs.devlabsbackend.review.dto.TeamMemberScoreRequest
import com.devlabs.devlabsbackend.review.dto.UpdateReviewRequest
import com.devlabs.devlabsbackend.review.service.ReviewService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    @PostMapping
    fun createReview(
        @RequestBody request: CreateReviewRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.createReview(request, reviewerId)
            ResponseEntity.status(HttpStatus.CREATED).body(review)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create review"))
        }
    }

    @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: UpdateReviewRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.updateReview(reviewId, request, reviewerId)
            ResponseEntity.ok(review)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update review"))
        }
    }

    @PutMapping("/{reviewId}/start")
    fun startReview(
        @PathVariable reviewId: UUID,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.startReview(reviewId, reviewerId)
            ResponseEntity.ok(review)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to start review"))
        }
    }

    @PutMapping("/{reviewId}/complete")
    fun completeReview(
        @PathVariable reviewId: UUID,
        @RequestBody request: CompleteReviewRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.completeReview(reviewId, request, reviewerId)
            ResponseEntity.ok(review)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to complete review"))        }
    }

    @PutMapping("/{reviewId}/attendance")
    fun markAttendance(
        @PathVariable reviewId: UUID,
        @RequestBody request: AttendanceRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val review = reviewService.markAttendance(reviewId, request.attendance, reviewerId)
            ResponseEntity.ok(review)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to mark attendance"))
        }
    }

    @GetMapping("/project/{projectId}")
    fun getReviewsByProject(@PathVariable projectId: UUID): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getReviewsByProject(projectId)
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews"))
        }    }

    @GetMapping("/reviewer")
    fun getReviewsByReviewer(@RequestHeader("X-User-Id") reviewerId: UUID): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getReviewsByReviewer(reviewerId)
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews"))
        }
    }

    // Individual Team Member Scoring Endpoints

    @PostMapping("/{reviewId}/team-members/{teamMemberId}/score")
    fun scoreTeamMember(
        @PathVariable reviewId: UUID,
        @PathVariable teamMemberId: UUID,
        @RequestBody request: TeamMemberScoreRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val score = reviewService.scoreTeamMember(reviewId, teamMemberId, request, reviewerId)
            ResponseEntity.status(HttpStatus.CREATED).body(score)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to score team member"))
        }
    }

    @PutMapping("/{reviewId}/team-members/{teamMemberId}/score")
    fun updateTeamMemberScore(
        @PathVariable reviewId: UUID,
        @PathVariable teamMemberId: UUID,
        @RequestBody request: TeamMemberScoreRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val score = reviewService.scoreTeamMember(reviewId, teamMemberId, request, reviewerId)
            ResponseEntity.ok(score)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update team member score"))
        }
    }

    @GetMapping("/{reviewId}/team-members/scores")
    fun getTeamMemberScores(@PathVariable reviewId: UUID): ResponseEntity<Any> {
        return try {
            val scores = reviewService.getTeamMemberScores(reviewId)
            ResponseEntity.ok(scores)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get team member scores"))
        }
    }

    @GetMapping("/{reviewId}/team-members/{teamMemberId}/score")
    fun getTeamMemberScore(
        @PathVariable reviewId: UUID,
        @PathVariable teamMemberId: UUID
    ): ResponseEntity<Any> {
        return try {
            val score = reviewService.getTeamMemberScore(reviewId, teamMemberId)
            if (score != null) {
                ResponseEntity.ok(score)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get team member score"))
        }
    }

    @GetMapping("/team-members/{teamMemberId}/scores/history")
    fun getTeamMemberScoreHistory(@PathVariable teamMemberId: UUID): ResponseEntity<Any> {
        return try {
            val scores = reviewService.getTeamMemberScoreHistory(teamMemberId)
            ResponseEntity.ok(scores)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get team member score history"))
        }
    }

    @GetMapping("/team-members/{teamMemberId}/scores/average")
    fun getAverageScoreForTeamMember(@PathVariable teamMemberId: UUID): ResponseEntity<Any> {
        return try {
            val averageScore = reviewService.getAverageScoreForTeamMember(teamMemberId)
            ResponseEntity.ok(mapOf("averageScore" to averageScore))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get average score"))
        }
    }

    @DeleteMapping("/{reviewId}/team-members/{teamMemberId}/score")
    fun deleteTeamMemberScore(
        @PathVariable reviewId: UUID,
        @PathVariable teamMemberId: UUID,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val deleted = reviewService.deleteTeamMemberScore(reviewId, teamMemberId, reviewerId)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete team member score"))
        }
    }

    @GetMapping
    fun getAllReviews(): ResponseEntity<Any> {
        return try {
            val reviews = reviewService.getAllReviews()
            ResponseEntity.ok(reviews)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get reviews"))
        }
    }
}

data class AttendanceRequest(
    val attendance: Map<UUID, Boolean>
)
