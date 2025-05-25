package com.devlabs.devlabsbackend.semester.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.semester.domain.Semester
import com.devlabs.devlabsbackend.semester.repository.SemesterRepository
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SemesterService
    (
    val semesterRepository: SemesterRepository,
    val userRepository: UserRepository
) {
    fun assignManagersToSemester(semesterId: UUID, managersId: List<UUID>) {
        val semester = semesterRepository.findById(semesterId).orElseThrow {
            NotFoundException("Semester $semesterId not found")
        }
        val managers = userRepository.findAllById(managersId);
        if(managers.size != managersId.size){
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
        if(managers.size != managersId.size){
            throw NotFoundException("Some managers could not be found")
        }
        semester.managers.removeAll(managers)
        semesterRepository.save(semester)
    }

//    fun addCourseToSemester(semesterId: UUID, courseId: List<UUID>) {
//        val semester = semesterRepository.findById(semesterId).orElseThrow {
//            NotFoundException("Semester with id ${semesterId} not found")
//        }
//        val courses = courseRepository.findAllById(courseId);
//        semester.courses.addAll(courses)
//        semesterRepository.save(semester)
//    }
//
//    fun removeCourseFromSemester(semesterId: UUID, courseId: List<UUID>) {
//        val semester = semesterRepository.findById(semesterId).orElseThrow {
//            NotFoundException("Semester with id ${semesterId} not found")
//        }
//        val courses = courseRepository.findAllById(courseId);
//        semester.courses.removeAll(courses)
//        semesterRepository.save(semester)
//    }
}