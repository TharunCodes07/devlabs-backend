package com.devlabs.devlabsbackend.project.domain.DTO

import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse
import com.devlabs.devlabsbackend.user.service.toUserResponse
import java.sql.Timestamp
import java.util.*

data class ProjectResponse(
    val id: UUID?,
    val title: String,
    val description: String,
    val objectives: String?,
    val githubUrl: String?,
    val status: ProjectStatus,
    val teamId: UUID?,
    val teamName: String?,
    val teamMembers: List<UserResponse>,
    val courses: List<CourseInfo>,
    val reviewCount: Int,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)

data class CourseInfo(
    val id: UUID,
    val name: String,
    val code: String
)

fun Project.toProjectResponse(): ProjectResponse {
    return ProjectResponse(
        id = this.id,
        title = this.title,
        description = this.description,
        objectives = this.objectives,
        githubUrl = this.githubUrl,
        status = this.status,
        teamId = this.team.id,
        teamName = this.team.name,
        teamMembers = this.team.members.map { it.toUserResponse() },
        courses = this.courses.map { course ->
            CourseInfo(
                id = course.id!!,
                name = course.name,
                code = course.code
            )
        },
        reviewCount = this.reviews.size,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
