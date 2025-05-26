package com.devlabs.devlabsbackend.course.service

import com.devlabs.devlabsbackend.batch.repository.BatchRepository
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val batchRepository: BatchRepository
)
{
    fun assignStudents(courseId: UUID, studentId:List<UUID>){
            val course = courseRepository.findById(courseId).orElseThrow {
                NotFoundException("Could not find course with id $courseId")
            }
        val users = userRepository.findAllById(studentId)
        course.students.addAll(users)
        courseRepository.save(course)
    }

    fun removeStudents(courseId: UUID, studentId: List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Could not find course with id $courseId")
        }
        val users = userRepository.findAllById(studentId)
        course.students.removeAll(users)
        courseRepository.save(course)
    }

    fun assignInstructors(courseId: UUID, instructorId:List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Could not find course with id $courseId")
        }
        val users = userRepository.findAllById(instructorId)
        course.instructors.addAll(users)
        courseRepository.save(course)
    }

    fun removeInstructors(courseId: UUID, instructorId:List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow{
            NotFoundException("Could not find course with id $courseId")
        }
        val users = userRepository.findAllById(instructorId)
        course.instructors.removeAll(users)
        courseRepository.save(course)
    }

    fun addBatchesToCourse(courseId: UUID, batchId: List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Could not find course with id $courseId")
        }
        val batches = batchRepository.findAllById(batchId)
        course.batches.addAll(batches)
        courseRepository.save(course)
    }

    fun removeBatchesFromCourse(courseId: UUID, batchId: List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow{
            NotFoundException("Could not find course with id $courseId")
        }
        val batches = batchRepository.findAllById(batchId)
        course.batches.addAll(batches)
        courseRepository.save(course)
    }
}