package com.devlabs.devlabsbackend.department.controller

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.department.domain.Department
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
    fun getBatches(@PathVariable("departmentId") departmentId: UUID): MutableSet<Batch>{
        val department = departmentService.findDepartmentById(departmentId)
        if (department == null){
            return mutableSetOf()
        }
        return departmentService.getBatches(department)
    }
}

fun Department.toDepartmentResponse(): DepartmentResponse {
    return DepartmentResponse(
        id = this.id,
        name = this.name,
    )
}
