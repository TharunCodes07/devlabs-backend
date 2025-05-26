package com.devlabs.devlabsbackend.course.repository

import com.devlabs.devlabsbackend.course.domain.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "courses")
interface CourseRepository : JpaRepository<Course, UUID> {
}