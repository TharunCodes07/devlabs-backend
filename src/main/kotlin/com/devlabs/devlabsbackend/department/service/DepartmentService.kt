package com.devlabs.devlabsbackend.department.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.department.domain.dto.CreateDepartmentRequest
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentBatchResponse
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentResponse
import com.devlabs.devlabsbackend.department.domain.dto.UpdateDepartmentRequest
import com.devlabs.devlabsbackend.department.repository.DepartmentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class DepartmentService(
    private val departmentRepository: DepartmentRepository
) {    @Transactional(readOnly = true)
    fun getAllDepartments(
        page: Int = 0, 
        size: Int = 10, 
        sortBy: String? = null, 
        sortOrder: String? = null
    ): PaginatedResponse<DepartmentResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable: Pageable = PageRequest.of(page, size, sort)

        val departmentsPage = departmentRepository.findAllWithBatches(pageable)
        
        return PaginatedResponse(
            data = departmentsPage.content.map { it.toDepartmentResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = departmentsPage.totalPages,
                total_count = departmentsPage.totalElements.toInt()
            )
        )
    }
    @Transactional(readOnly = true)
    fun searchDepartments(
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String? = null,
        sortOrder: String? = null
    ): PaginatedResponse<DepartmentResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val departmentsPage = departmentRepository.findByNameContainingIgnoreCaseWithBatches(query, pageable)
        
        return PaginatedResponse(
            data = departmentsPage.content.map { it.toDepartmentResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = departmentsPage.totalPages,
                total_count = departmentsPage.totalElements.toInt()
            )
        )
    }
    
    private fun createSort(sortBy: String?, sortOrder: String?): Sort {
        return if (sortBy != null) {
            val direction = if (sortOrder?.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
            Sort.by(direction, sortBy)
        } else {
            Sort.by(Sort.Direction.ASC, "name")
        }    }


    @Transactional(readOnly = true)
    fun getAllDepartments(): List<Department>{
        return departmentRepository.findAllWithBatches()
    }

    fun createDepartment(request: CreateDepartmentRequest): Department {
        val department = Department(name = request.name)
        val savedDepartment = departmentRepository.save(department)
        return savedDepartment
    }fun updateDepartment(departmentId: UUID, request: UpdateDepartmentRequest): Department {
        val department = departmentRepository.findById(departmentId).orElseThrow {
            NotFoundException("Department with id $departmentId not found")
        }
        
        request.name?.let { department.name = it }
        
        val savedDepartment = departmentRepository.save(department)

        return savedDepartment
    }

    @Transactional(readOnly = true)
    fun getDepartmentById(departmentId: UUID): Department {
        val department = departmentRepository.findByIdWithBatches(departmentId) ?: throw NotFoundException("Department with id $departmentId not found")
        return department
    }

    fun deleteDepartment(departmentId: UUID) {
        val department = departmentRepository.findById(departmentId).orElseThrow {
            NotFoundException("Department with id $departmentId not found")
        }
        departmentRepository.delete(department)
    }

    fun getBatchesByDepartmentId(departmentId: UUID): List<DepartmentBatchResponse> {
        return departmentRepository.findBatchesByDepartmentId(departmentId)
    }
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

