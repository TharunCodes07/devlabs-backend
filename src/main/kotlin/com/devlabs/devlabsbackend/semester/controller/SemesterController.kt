package com.devlabs.devlabsbackend.semester.controller

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.course.domain.DTO.CourseResponse
import com.devlabs.devlabsbackend.course.domain.DTO.CreateCourseRequest
import com.devlabs.devlabsbackend.semester.domain.DTO.AddOrRemoveCourseDTO
import com.devlabs.devlabsbackend.semester.domain.DTO.AssignManagersDTO
import com.devlabs.devlabsbackend.semester.domain.DTO.SemesterResponse
import com.devlabs.devlabsbackend.semester.service.SemesterService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/semester")
class SemesterController(val semesterService: SemesterService) {

    @PostMapping
    fun createSemester(@RequestBody request: com.devlabs.devlabsbackend.semester.domain.DTO.CreateSemesterRequest): ResponseEntity<SemesterResponse> {
        return try {
            val semester = semesterService.createSemester(request)
            ResponseEntity(semester, HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PutMapping("/{semesterId}/assign-manager")
    fun assignManagers(@RequestBody assignManagersDTO: AssignManagersDTO, @PathVariable semesterId: UUID) {
        semesterService.assignManagersToSemester(managersId = assignManagersDTO.managersId, semesterId = semesterId)
    }

    @PutMapping("/{semesterId}/remove-manager")
    fun removeManagers(@RequestBody assignManagersDTO: AssignManagersDTO, @PathVariable semesterId: UUID) {
        semesterService.removeManagersFromSemester(managersId = assignManagersDTO.managersId, semesterId = semesterId)
    }

    @PutMapping("/{semesterId}/add-course")
    fun addCourseToSemester(@RequestBody courseDto: AddOrRemoveCourseDTO, @PathVariable semesterId: UUID) {
        semesterService.addCourseToSemester(semesterId = semesterId, courseId = courseDto.courseId)
    }    @PutMapping("/{semesterId}/remove-course")
    fun removeCourseFromSemester(@RequestBody courseDto: AddOrRemoveCourseDTO, @PathVariable semesterId: UUID) {
        semesterService.removeCourseFromSemester(semesterId = semesterId, courseId = courseDto.courseId)
    }
    
    @GetMapping
    fun getAllSemesters(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<PaginatedResponse<SemesterResponse>> {
        return ResponseEntity.ok(
            semesterService.getAllSemestersPaginated(page, size, sort_by, sort_order)
        )
    }

    @GetMapping("/search")
    fun search(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<PaginatedResponse<SemesterResponse>> {
        return ResponseEntity.ok(
            semesterService.searchSemesterPaginated(query, page, size, sort_by, sort_order)
        )
    }    @GetMapping("/{id}")
    fun getSemesterById(@PathVariable id: UUID): ResponseEntity<SemesterResponse> {
        return ResponseEntity(
            semesterService.getSemesterById(id),
            HttpStatus.OK
        )
    }

      @GetMapping("/{id}/courses")
    fun getCoursesBySemesterId(@PathVariable id: UUID): ResponseEntity<List<CourseResponse>> {
        return ResponseEntity(
            semesterService.getCoursesBySemesterId(id),
            HttpStatus.OK
        )
    }
    
    @PostMapping("/{id}/courses")
    fun createCourseForSemester(
        @PathVariable id: UUID,
        @RequestBody courseRequest: CreateCourseRequest
    ): ResponseEntity<CourseResponse> {
        return try {
            val course = semesterService.createCourseForSemester(id, courseRequest)
            ResponseEntity(course, HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }
    
    @DeleteMapping("/{semesterId}/courses/{courseId}")
    fun deleteCourseFromSemester(
        @PathVariable semesterId: UUID,
        @PathVariable courseId: UUID
    ): ResponseEntity<CourseResponse> {
        return try {
            val course = semesterService.deleteCourseFromSemester(semesterId, courseId)
            ResponseEntity(course, HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(null)
        } catch (e: NotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }
    
    @GetMapping("/active")
    fun getAllActiveSemesters(): ResponseEntity<List<SemesterResponse>> {
        return try {
            val semesters = semesterService.getAllActiveSemesters()
            ResponseEntity.ok(semesters)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }
}