package com.devlabs.devlabsbackend.department.controller

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.department.domain.dto.*
import com.devlabs.devlabsbackend.department.service.DepartmentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/department")
class DepartmentController(
    private val departmentService: DepartmentService,
){
    // Get all departments with pagination and sorting
    @GetMapping
    fun getAllDepartments(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<Any> {
        return try {
            val departments = departmentService.getAllDepartments(page, size, sort_by, sort_order)
            ResponseEntity.ok(departments)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to fetch departments: ${e.message}"))
        }
    }
    
    // Search departments with pagination
    @GetMapping("/search")
    fun searchDepartments(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false) sort_by: String?,
        @RequestParam(required = false) sort_order: String?
    ): ResponseEntity<PaginatedResponse<DepartmentResponse>> {
        return try {
            val departments = departmentService.searchDepartments(query, page, size, sort_by, sort_order)
            ResponseEntity.ok(departments)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaginatedResponse(
                    data = emptyList(),
                    pagination = PaginationInfo(0, size, 0, 0)
                ))
        }
    }

    @PostMapping
    fun createDepartment(@RequestBody request: CreateDepartmentRequest): DepartmentResponse {
        val newDepartment = departmentService.createDepartment(request)
        return newDepartment.toDepartmentResponse()
    }

    @PutMapping("/{departmentId}")
    fun updateDepartment(
        @PathVariable departmentId: UUID,
        @RequestBody request: UpdateDepartmentRequest
    ): DepartmentResponse {
        val updatedDepartment = departmentService.updateDepartment(departmentId, request)
        return updatedDepartment.toDepartmentResponse()
    }

    @DeleteMapping("/{departmentId}")
    fun deleteDepartment(@PathVariable departmentId: UUID): ResponseEntity<Any> {
        departmentService.deleteDepartment(departmentId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/all")
    fun getAllDepartmentsLegacy(): List<SimpleDepartmentResponse>{
        return departmentService.getAllDepartments().map{it.toSimpleDepartmentResponse()}
    }


    @GetMapping("/{departmentId}/batches")
    fun getBatches(@PathVariable("departmentId") departmentId: UUID): List<DepartmentBatchResponse> {
        return departmentService.getBatchesByDepartmentId(departmentId)
    }
}

fun Department.toSimpleDepartmentResponse(): SimpleDepartmentResponse {
    return SimpleDepartmentResponse(
        id = this.id ?: UUID.randomUUID(),
        name = this.name
    )
}

fun Department.toDepartmentResponse(): DepartmentResponse {
    val batchResponses = this.batches.map { batch ->
        DepartmentBatchResponse(
            id = batch.id,
            name = batch.name,
            graduationYear = batch.graduationYear,
            section = batch.section
        )
    }
    
    return DepartmentResponse(
        id = this.id,
        name = this.name,
        batches = batchResponses
    )

}

fun Batch.toDepartmentBatchResponse(): DepartmentBatchResponse {
    return DepartmentBatchResponse(
        id = this.id,
        name = this.name,
        graduationYear = this.graduationYear,
        section = this.section,
    )


}
