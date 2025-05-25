package com.devlabs.devlabsbackend.semester.controller

import com.devlabs.devlabsbackend.semester.service.SemesterService
import com.evalify.evalifybackend.semester.domain.DTO.AssignManagersDTO
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("semester")
class SemesterController (val semesterService: SemesterService){

    @PutMapping("/{semesterId}/assign-manager")
    fun assignManagers(@RequestBody assignManagersDTO: AssignManagersDTO, @PathVariable semesterId: UUID){
        semesterService.assignManagersToSemester(managersId = assignManagersDTO.managersId, semesterId = semesterId)
    }

    @PutMapping("/{semesterId}/remove-manager")
    fun removeManagers(@RequestBody assignManagersDTO: AssignManagersDTO,@PathVariable semesterId:UUID){
        semesterService.removeManagersFromSemester(managersId = assignManagersDTO.managersId, semesterId = semesterId)
    }

//    @PutMapping("/{semesterId}/add-course")
//    fun addCourseToSemester(@RequestBody courseDto:AddOrRemoveCourseDTO,@PathVariable semesterId: UUID){
//        semesterService.addCourseToSemester(semesterId = semesterId, courseId = courseDto.courseId)
//    }
//    @PutMapping("/{semesterId}/remove-course")
//    fun removeCourseFromSemester(@RequestBody courseDto:AddOrRemoveCourseDTO,@PathVariable semesterId: UUID){
//        semesterService.removeCourseFromSemester(semesterId = semesterId, courseId = courseDto.courseId)
//    }

}