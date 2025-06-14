package com.devlabs.devlabsbackend.semester.service

import com.devlabs.devlabsbackend.batch.repository.BatchRepository
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.course.domain.DTO.CourseResponse
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.semester.domain.DTO.SemesterResponse
import com.devlabs.devlabsbackend.semester.domain.Semester
import com.devlabs.devlabsbackend.semester.repository.SemesterRepository
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class SemesterService
    (
    val semesterRepository: SemesterRepository,
    val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val batchRepository: BatchRepository
) {
    fun assignManagersToSemester(semesterId: UUID, managersId: List<UUID>) {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester $semesterId not found")
        }
        val managers = userRepository.findAllById(managersId);
        if (managers.size != managersId.size) {
            throw NotFoundException("Some managers could not be found")
        }
        semester.managers.addAll(managers)
        semesterRepository.save(semester)
    }

    fun removeManagersFromSemester(semesterId: UUID, managersId: List<UUID>) {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester $semesterId not found")
        }
        val managers = userRepository.findAllById(managersId)
        if (managers.size != managersId.size) {
            throw NotFoundException("Some managers could not be found")
        }
        semester.managers.removeAll(managers)
        semesterRepository.save(semester)
    }

    fun addCourseToSemester(semesterId: UUID, courseId: List<UUID>) {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester with id $semesterId not found")
        }
        val courses = courseRepository.findAllById(courseId);
        semester.courses.addAll(courses)
        semesterRepository.save(semester)
    }

    fun removeCourseFromSemester(semesterId: UUID, courseId: List<UUID>) {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester with id $semesterId not found")
        }
        val courses = courseRepository.findAllById(courseId);
        semester.courses.removeAll(courses)
        semesterRepository.save(semester)
    }

    fun getAllSemestersPaginated(page: Int, size: Int, sortBy: String = "name", sortOrder: String = "asc"): PaginatedResponse<SemesterResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable = PageRequest.of(page, size, sort)
        val semesterPage: Page<Semester> = semesterRepository.findAll(pageable)
        
        return PaginatedResponse(
            data = semesterPage.content.map { it.toSemesterResponse() },
            pagination = PaginationInfo(
                current_page = page + 1,
                per_page = size,
                total_pages = semesterPage.totalPages,
                total_count = semesterPage.totalElements.toInt()
            )
        )
    }
    
    fun searchSemesterPaginated(query: String, page: Int, size: Int, sortBy: String = "name", sortOrder: String = "asc"): PaginatedResponse<SemesterResponse> {
        val sort = createSort(sortBy, sortOrder)
        val pageable = PageRequest.of(page, size, sort)
        val semesterPage: Page<Semester> = semesterRepository.findByNameOrYearContainingIgnoreCase(query, pageable)
        
        return PaginatedResponse(
            data = semesterPage.content.map { it.toSemesterResponse() },
            pagination = PaginationInfo(
                current_page = page + 1,
                per_page = size,
                total_pages = semesterPage.totalPages,
                total_count = semesterPage.totalElements.toInt()
            )
        )
    }
    
    @Transactional
    fun getAllActiveSemesters(): List<SemesterResponse> {
        return semesterRepository.findByIsActiveTrue().map { it.toSemesterResponse() }
    }
    
    private fun createSort(sortBy: String, sortOrder: String): Sort {
        val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return Sort.by(direction, sortBy)
    }

    // Legacy methods for backwards compatibility
    fun getAllSemester(): List<SemesterResponse> {
        return semesterRepository.findAll().map { it.toSemesterResponse() }
    }

    fun searchSemester(query: String): List<SemesterResponse> {
        return semesterRepository.findByNameOrYearContainingIgnoreCase(query).map { user -> user.toSemesterResponse() }
    }

    fun getSemesterById(semesterId: UUID): SemesterResponse {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester with id $semesterId not found")
        }
        return semester.toSemesterResponse()
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    fun getCoursesBySemesterId(semesterId: UUID): List<CourseResponse> {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester with id $semesterId not found")
        }
        // Force initialization of the courses collection to avoid LazyInitializationException
        semester.courses.size
        
        return semester.courses.map { course -> 
            CourseResponse(
                id = course.id!!,
                name = course.name,
                code = course.code,
                description = course.description
            )
        }
    }

    fun createCourseForSemester(semesterId: UUID, courseRequest: com.devlabs.devlabsbackend.course.domain.DTO.CreateCourseRequest): CourseResponse {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester with id $semesterId not found")
        }
        // Do not set review at all
        val course = com.devlabs.devlabsbackend.course.domain.Course(
            name = courseRequest.name,
            code = courseRequest.code,
            description = courseRequest.description,
            type = courseRequest.type,
            semester = semester
            // review is not set here
        )
        val savedCourse = courseRepository.save(course)
        return CourseResponse(
            id = savedCourse.id!!,
            name = savedCourse.name,
            code = savedCourse.code,
            description = savedCourse.description
        )
    }

    fun deleteCourseFromSemester(semesterId: UUID, courseId: UUID): CourseResponse {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester with id $semesterId not found")
        }
        
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        if (course.semester.id != semester.id) {
            throw IllegalArgumentException("Course with id $courseId does not belong to semester with id $semesterId")
        }
        
        val courseResponse = CourseResponse(
            id = course.id!!,
            name = course.name,
            code = course.code,
            description = course.description
        )
        
        courseRepository.delete(course)
        return courseResponse
    }

    fun createSemester(request: com.devlabs.devlabsbackend.semester.domain.DTO.CreateSemesterRequest): SemesterResponse {
        val semester = Semester(
            name = request.name,
            year = request.year,
            isActive = request.isActive
        )
        val savedSemester = semesterRepository.save(semester)
        return savedSemester.toSemesterResponse()
    }
}

fun Semester.toSemesterResponse(): SemesterResponse {
    return SemesterResponse(
        id = this.id!!,
        name = this.name,
        year = this.year,
        isActive = this.isActive
    )
}
