package com.devlabs.devlabsbackend.batch.controller

import com.devlabs.devlabsbackend.batch.domain.dto.BatchResponse
import com.devlabs.devlabsbackend.batch.domain.dto.CreateBatchRequest
import com.devlabs.devlabsbackend.batch.domain.dto.SemesterBatchResponse
import com.devlabs.devlabsbackend.batch.domain.dto.UpdateBatchRequest
import com.devlabs.devlabsbackend.batch.service.BatchService
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.semester.domain.Semester
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/batch")
class BatchController(
    private val batchService: BatchService
) {

    @PostMapping
    fun createBatch(@RequestBody createBatchRequest: CreateBatchRequest): ResponseEntity<BatchResponse> {
        val batch = batchService.createBatch(createBatchRequest)
        return ResponseEntity.ok(batch)
    }

    @PutMapping("/{batchId}")
    fun updateBatch(
        @PathVariable batchId: UUID,
        @RequestBody updateBatchRequest: UpdateBatchRequest
    ): ResponseEntity<BatchResponse> {
        val batch = batchService.updateBatch(batchId, updateBatchRequest)
        return ResponseEntity.ok(batch)
    }

    @DeleteMapping("/{batchId}")
    fun deleteBatch(@PathVariable batchId: UUID): ResponseEntity<Void> {
        batchService.deleteBatch(batchId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{batchId}")
    fun getBatchById(@PathVariable batchId: UUID): ResponseEntity<Any> {
        return try {
            val batch = batchService.getBatchById(batchId)
            ResponseEntity.ok(batch)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to fetch batch: ${e.message}"))
        }
    }

    @GetMapping("/{batchId}/students")
    fun getBatchStudents(
        @PathVariable batchId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<Any> {
        return try {
            val students = batchService.getBatchStudents(batchId, page, size, sort_by, sort_order)
            ResponseEntity.ok(students)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to fetch batch students: ${e.message}"))
        }
    }

    @GetMapping("/{batchId}/students/search")
    fun searchBatchStudents(
        @PathVariable batchId: UUID,
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<Any> {
        return try {
            val students = batchService.searchBatchStudents(batchId, query, page, size, sort_by, sort_order)
            ResponseEntity.ok(students)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to search batch students: ${e.message}"))
        }
    }

    @PutMapping("/{batchId}/add-students")
    fun addStudents(@RequestBody userIds: List<String>, @PathVariable batchId: UUID) {
        batchService.addStudentsToBatch(batchId, userIds)
    }

    @PutMapping("/{batchId}/delete-students")
    fun deleteStudents(@RequestBody userIds: List<String>, @PathVariable batchId: UUID) {
        batchService.removeStudentsFromBatch(batchId, userIds)
    }

    @GetMapping
    fun getAllBatches(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String?
    ): ResponseEntity<Any> {
        return try {
            val batches = batchService.getAllBatches(page, size, sort_by, sort_order)
            ResponseEntity.ok(batches)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to fetch batches: ${e.message}"))
        }
    }

    @GetMapping("/search")
    fun searchBatches(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false) sort_by: String?,
        @RequestParam(required = false) sort_order: String?
    ): ResponseEntity<PaginatedResponse<BatchResponse>> {
        return try {
            val batches = batchService.searchBatches(query, page, size, sort_by, sort_order)
            ResponseEntity.ok(batches)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaginatedResponse(
                    data = emptyList(),
                    pagination = PaginationInfo(0, size, 0, 0)
                ))
        }
    }

    @GetMapping("/active")
    fun getAllActiveBatches(): ResponseEntity<Any> {
        return try {
            val batches = batchService.getAllActiveBatches()
            ResponseEntity.ok(batches)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to retrieve active batches: ${e.message}"))
        }
    }

}

fun Semester.toSemesterBatchResponse(): SemesterBatchResponse {
    return SemesterBatchResponse(
        id = this.id.toString(),
        name = this.name,
        year = this.year.toString(),
        isActive = this.isActive
    )
}
