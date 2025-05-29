package com.devlabs.devlabsbackend.rubric.controller

import com.devlabs.devlabsbackend.rubric.service.RubricService
import com.devlabs.devlabsbackend.rubric.dto.CreateTemplateRequest
import com.devlabs.devlabsbackend.rubric.dto.UpdateTemplateRequest
import com.devlabs.devlabsbackend.rubric.dto.CreateCustomRubricRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/rubrics")
class RubricController(
    private val rubricService: RubricService
) {

    // Template management
    @PostMapping("/templates")
    fun createTemplate(
        @RequestBody request: CreateTemplateRequest,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<Any> {
        return try {
            val template = rubricService.createTemplate(request, creatorId)
            ResponseEntity.status(HttpStatus.CREATED).body(template)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create template"))
        }
    }

    @PutMapping("/templates/{templateId}")
    fun updateTemplate(
        @PathVariable templateId: UUID,
        @RequestBody request: UpdateTemplateRequest,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val template = rubricService.updateTemplate(templateId, request, userId)
            ResponseEntity.ok(template)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update template"))
        }
    }

    @GetMapping("/templates/available")
    fun getAvailableTemplates(@RequestHeader("X-User-Id") userId: UUID): ResponseEntity<Any> {
        return try {
            val templates = rubricService.getAvailableTemplates(userId)
            ResponseEntity.ok(templates)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get available templates"))
        }
    }

    @GetMapping("/templates/creator/{creatorId}")
    fun getTemplatesByCreator(@PathVariable creatorId: UUID): ResponseEntity<Any> {
        return try {
            val templates = rubricService.getTemplatesByCreator(creatorId)
            ResponseEntity.ok(templates)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get templates"))
        }
    }

    // Rubric management
    @PostMapping("/review/{reviewId}/from-template/{templateId}")
    fun createRubricFromTemplate(
        @PathVariable reviewId: UUID,
        @PathVariable templateId: UUID,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<Any> {
        return try {
            val rubric = rubricService.createRubricFromTemplate(reviewId, templateId, creatorId)
            ResponseEntity.status(HttpStatus.CREATED).body(rubric)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create rubric from template"))
        }
    }

    @PostMapping("/review/{reviewId}/custom")
    fun createCustomRubric(
        @PathVariable reviewId: UUID,
        @RequestBody request: CreateCustomRubricRequest,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<Any> {
        return try {
            val rubric = rubricService.createCustomRubric(reviewId, request, creatorId)
            ResponseEntity.status(HttpStatus.CREATED).body(rubric)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create custom rubric"))
        }
    }

    @PutMapping("/items/{itemId}/score")
    fun scoreRubricItem(
        @PathVariable itemId: UUID,
        @RequestBody request: ScoreItemRequest,
        @RequestHeader("X-User-Id") reviewerId: UUID
    ): ResponseEntity<Any> {
        return try {
            val item = rubricService.scoreRubricItem(itemId, request.score, request.feedback, reviewerId)
            ResponseEntity.ok(item)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to score rubric item"))
        }
    }

    @GetMapping("/review/{reviewId}")
    fun getRubricByReview(@PathVariable reviewId: UUID): ResponseEntity<Any> {
        return try {
            val rubric = rubricService.getRubricByReview(reviewId)
            if (rubric != null) {
                ResponseEntity.ok(rubric)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get rubric"))
        }
    }
}

data class ScoreItemRequest(
    val score: Double,
    val feedback: String? = null
)
