package com.devlabs.devlabsbackend.batch.service

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.batch.domain.dto.BatchResponse
import com.devlabs.devlabsbackend.batch.domain.dto.CreateBatchRequest
import com.devlabs.devlabsbackend.batch.domain.dto.UpdateBatchRequest
import com.devlabs.devlabsbackend.batch.repository.BatchRepository
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentResponse
import com.devlabs.devlabsbackend.department.repository.DepartmentRepository
import com.devlabs.devlabsbackend.semester.repository.SemesterRepository
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse
import com.devlabs.devlabsbackend.user.repository.UserRepository
import com.devlabs.devlabsbackend.user.service.toUserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BatchService(
    private val userRepository: UserRepository,
    private val batchRepository: BatchRepository,
    private val semesterRepository: SemesterRepository,
    private val departmentRepository: DepartmentRepository
) {
    @Transactional
    fun createBatch(request: CreateBatchRequest): BatchResponse {
        val department = request.departmentId?.let { departmentId ->
            departmentRepository.findById(departmentId).orElseThrow {
                NotFoundException("Department with id $departmentId not found")
            }
        }

        val batch = Batch(
            name = request.name,
            graduationYear = request.graduationYear,
            section = request.section,
            isActive = request.isActive,
            department = department
        )

        val savedBatch = batchRepository.save(batch)
        return savedBatch.toBatchResponse()
    }

    @Transactional
    fun updateBatch(batchId: UUID, request: UpdateBatchRequest): BatchResponse {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Batch with id $batchId not found")
        }

        request.name?.let { batch.name = it }
        request.graduationYear?.let { batch.graduationYear = it }
        request.section?.let { batch.section = it }
        request.isActive?.let { batch.isActive = it }
        request.departmentId?.let { departmentId ->
            val department = departmentRepository.findById(departmentId).orElseThrow {
                NotFoundException("Department with id $departmentId not found")
            }
            batch.department = department
        }

        val savedBatch = batchRepository.save(batch)
        return savedBatch.toBatchResponse()
    }

    @Transactional
    fun deleteBatch(batchId: UUID) {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Batch with id $batchId not found")
        }
        batchRepository.delete(batch)
    }


    @Transactional(readOnly = true)
    fun getBatchById(batchId: UUID): BatchResponse {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Batch with id $batchId not found")
        }
        return batch.toBatchResponse()
    }

    @Transactional(readOnly = true)
    fun searchBatchStudents(
        batchId: UUID,
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<UserResponse> {
        batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Batch with id $batchId not found")
        }

        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortBy)
        val pageable: Pageable = PageRequest.of(page, size, sort)

        val studentsPage = batchRepository.searchStudentsByBatchId(batchId, query, pageable)

        return PaginatedResponse(
            data = studentsPage.content.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = studentsPage.totalPages,
                total_count = studentsPage.totalElements.toInt()
            )
        )
    }

    fun getAllBatches(
        page: Int = 0,
        size: Int = 10,
        sortBy: String? = "name",
        sortOrder: String? = "asc"
    ): PaginatedResponse<BatchResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val batchesPage: Page<Batch> = batchRepository.findAll(pageable)

        return PaginatedResponse(
            data = batchesPage.content.map { it.toBatchResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = batchesPage.totalPages,
                total_count = batchesPage.totalElements.toInt()
            )
        )
    }

    @Transactional
    fun addStudentsToBatch(batchId: UUID, studentId: List<String>) {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Could not find course with id $batchId")
        }
        val users = userRepository.findAllById(studentId)
        batch.students.addAll(users)
        batchRepository.save(batch)
    }

    @Transactional
    fun removeStudentsFromBatch(batchId: UUID, studentId: List<String>) {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Could not find course with id $batchId")
        }
        val users = userRepository.findAllById(studentId)
        batch.students.removeAll(users)
        batchRepository.save(batch)
    }

    @Transactional
    fun addSemestersToBatches(batchId: UUID, semesterId: List<UUID>) {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Could not find course with id $batchId")
        }
        val semesters = semesterRepository.findAllById(semesterId)
        batch.semester.addAll(semesters)
        batchRepository.save(batch)
    }

    @Transactional
    fun removeSemestersFromBatch(batchId: UUID, semesterId: List<UUID>) {
        val batch = batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Could not find course with id $batchId")
        }
        val semesters = semesterRepository.findAllById(semesterId)
        batch.semester.removeAll(semesters)
        batchRepository.save(batch)
    }

    private fun createSort(sortBy: String?, sortOrder: String?): Sort {
        return if (sortBy != null) {
            val direction = if (sortOrder?.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
            Sort.by(direction, sortBy)
        } else {
            Sort.by(Sort.Direction.ASC, "name")
        }
    }

    fun searchBatches(
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String? = null,
        sortOrder: String? = null
    ): PaginatedResponse<BatchResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val batchesPage: Page<Batch> = batchRepository.searchByNameOrYearContainingIgnoreCase(query, pageable)

        return PaginatedResponse(
            data = batchesPage.content.map { it.toBatchResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = batchesPage.totalPages,
                total_count = batchesPage.totalElements.toInt()
            )
        )
    }

    @Transactional
    fun getAllActiveBatches(): List<BatchResponse> {
        return batchRepository.findByIsActiveTrue().map { it.toBatchResponse() }
    }

    @Transactional(readOnly = true)
    fun getBatchStudents(
        batchId: UUID,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<UserResponse> {
        batchRepository.findById(batchId).orElseThrow {
            NotFoundException("Batch with id $batchId not found")
        }

        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortBy)
        val pageable: Pageable = PageRequest.of(page, size, sort)

        val studentsPage = batchRepository.findStudentsByBatchId(batchId, pageable)

        return PaginatedResponse(
            data = studentsPage.content.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = studentsPage.totalPages,
                total_count = studentsPage.totalElements.toInt()
            )
        )
    }

    fun getAllActiveBatchesForUser(userId: String): List<BatchResponse> {
        userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        return batchRepository.findByIsActiveTrueAndStudentsId(userId).map {
            it.toBatchResponse()
        }

    }
}

@Transactional(readOnly = true)
fun Batch.toBatchResponse(): BatchResponse {
    return BatchResponse(
        id = this.id,
        name = this.name,
        graduationYear = this.graduationYear,
        section = this.section,
        isActive = this.isActive,
        department = this.department?.let {
            DepartmentResponse(
                id = it.id,
                name = it.name
            )
        }
    )
}


