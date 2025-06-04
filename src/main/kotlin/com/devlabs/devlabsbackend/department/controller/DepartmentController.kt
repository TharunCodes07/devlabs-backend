package com.devlabs.devlabsbackend.department.controller

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentBatchResponse
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentResponse
import com.devlabs.devlabsbackend.department.repository.DepartmentRepository
import com.devlabs.devlabsbackend.department.service.DepartmentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/departments")
class DepartmentController(
    private val departmentService: DepartmentService,
){
    @GetMapping
    fun getAllDepartments(): List<DepartmentResponse>{
        return departmentService.getAllDepartments().map{it.toDepartmentResponse()}
    }

    @PostMapping
    fun addDepartment(@RequestBody department: Department): DepartmentResponse {
        val newDepartment = departmentService.addDepartment(department)
        return newDepartment.toDepartmentResponse()
    }

    @GetMapping("{departmentId}/batches")
    fun getBatches(@PathVariable("departmentId") departmentId: UUID): List<DepartmentBatchResponse> {
        return departmentService.getBatchesByDepartmentId(departmentId)
    }
}

fun Department.toDepartmentResponse(): DepartmentResponse {
    return DepartmentResponse(
        id = this.id,
        name = this.name,
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
