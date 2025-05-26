package com.devlabs.devlabsbackend.course.controller

import com.devlabs.devlabsbackend.course.service.CourseService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/courses")
class CourseController(private val courseService: CourseService) {
    @PutMapping("{courseId}/assign-students")
    fun assignStudentsToCourses(@PathVariable courseId: UUID, @RequestBody studentIds: List<UUID>){
        courseService.assignStudents(courseId, studentIds)
    }

    @PutMapping("{courseId}/remove-students")
    fun removeStudentsFromCourse(@PathVariable courseId: UUID, @RequestBody studentIds: List<UUID>){
        courseService.removeStudents(courseId, studentIds)
    }

    @PutMapping("{courseId}/assign-instructor")
    fun assignInstructorsToCourses(@PathVariable courseId: UUID, @RequestBody userId: List<UUID>){
        courseService.assignInstructors(courseId, userId)
    }

    @PutMapping("{courseId}/remove-instructor")
    fun removeInstructorsFromCourse(@PathVariable courseId: UUID, @RequestBody userId: List<UUID>) {
        courseService.removeInstructors(courseId, userId)
    }

    @PutMapping("{courseId}/addBatch")
    fun addBatchToCourse(@PathVariable courseId: UUID, @RequestBody studentIds: List<UUID>){
        courseService.addBatchesToCourse(courseId, studentIds)
    }

    @PutMapping("{courseId}/removeBatch")
    fun removeBatchFromCourse(@PathVariable courseId: UUID, @RequestBody studentIds: List<UUID>){
        courseService.removeBatchesFromCourse(courseId, studentIds)
    }
}