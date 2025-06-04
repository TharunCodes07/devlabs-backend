package com.devlabs.devlabsbackend.department.service

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.department.repository.DepartmentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID


@Service
class DepartmentService(
    private val departmentRepository: DepartmentRepository
) {
    fun getAllDepartments(): List<Department>{
        return departmentRepository.findAll()
    }

    fun addDepartment(department: Department): Department{
        return departmentRepository.save(department)
    }

    fun findDepartmentById(departmentId: UUID): Department?{
        return departmentRepository.findByIdOrNull(departmentId)
    }

    fun getBatches(department: Department): MutableSet<Batch>{
        return department.batches
    }
}